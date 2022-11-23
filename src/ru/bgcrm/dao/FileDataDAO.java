package ru.bgcrm.dao;

import static ru.bgcrm.dao.Tables.TABLE_FILE_DATA;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bgerp.util.Log;
import org.bgerp.util.TimeConvert;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.FileData;
import ru.bgcrm.util.Utils;

/**
 * Files DAO. Stores metadata in DB and bodies in filesystem.
 *
 * @author Shamil Vakhitov
 */
public class FileDataDAO extends CommonDAO {
    private static final Log log = Log.getLog();

    private static final DateTimeFormatter DIR_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DecimalFormat NAME_FORMAT = new DecimalFormat("0000000000");

    private File storeDir;

    /**
     * Constructor. Creates a store directory if not exists.
     * @param con might be null if only {@link #getFile(FileData)} is used and {@code time} is already set.
     */
    public FileDataDAO(Connection con) {
        super(con);
        storeDir = Utils.createDirectoryIfNoExistInWorkDir("filestorage");
    }

    public static FileData getFromRs(ResultSet rs, String prefix) throws SQLException {
        FileData result = new FileData();

        result.setId(rs.getInt(prefix + "id"));
        result.setTitle(rs.getString(prefix + "title"));
        result.setTime(rs.getTimestamp(prefix + "time"));
        result.setSecret(rs.getString("secret"));

        return result;
    }

    private void checkDir() throws BGException {
        if (!storeDir.exists() || !storeDir.isDirectory()) {
            throw new BGException("Not found directory file storage");
        }
        if (!storeDir.canRead() || !storeDir.canWrite()) {
            throw new BGException("Can't access the file storage directory");
        }
    }

    /**
     * Sets {@code secret}, {@code time} for {@code fileData} and inserts the entity in DB table.
     * @param fileData file data.
     * @return output stream to the related file in filesystem.
     * @throws Exception
     */
    public FileOutputStream add(FileData fileData) throws Exception {
        checkDir();

        fileData.setSecret(Utils.generateSecret());
        fileData.setTime(new Date());

        String query = "INSERT INTO " + TABLE_FILE_DATA + " (title, time, secret) VALUES (?, ?, ?)";
        try (var ps = con.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fileData.getTitle());
            ps.setTimestamp(2, TimeConvert.toTimestamp(fileData.getTime()));
            ps.setString(3, fileData.getSecret());
            ps.executeUpdate();

            fileData.setId(lastInsertId(ps));
        }

        var outputStream = new FileOutputStream(getFile(fileData));
        fileData.setOutputStream(outputStream);

        return outputStream;
    }

    public void delete(FileData fileData) throws Exception {
        checkDir();

        getFile(fileData).delete();

        String query = SQL_DELETE + TABLE_FILE_DATA + SQL_WHERE + "id=? AND secret=?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, fileData.getId());
            ps.setString(2, fileData.getSecret());
            ps.executeUpdate();
        }
    }

    public List<FileData> list(List<Integer> ids) throws SQLException {
        List<FileData> result = new ArrayList<>();

        var idsStr = Utils.toString(ids, "-1", ",");

        var query = SQL_SELECT + "*" + SQL_FROM + TABLE_FILE_DATA + "AS fd" +
            SQL_WHERE + "id IN (" + idsStr + ")" +
            SQL_ORDER_BY + "FIELD(id," + idsStr + ")";

        try (var st = con.createStatement()) {
            var rs = st.executeQuery(query);
            while (rs.next()) {
                result.add(getFromRs(rs, "fd."));
            }
        }

        return result;
    }

    /**
     * Gets file object for a given file data. File's {@code time} is loaded from DB case is not defined.
     * @param fileData
     * @return
     * @throws SQLException
     */
    public File getFile(FileData fileData) throws SQLException {
        if (fileData.getTime() == null) {
            try (var ps = con.prepareStatement(SQL_SELECT + "time" + SQL_FROM + TABLE_FILE_DATA + SQL_WHERE + "id=?")) {
                ps.setInt(1, fileData.getId());
                var rs = ps.executeQuery();
                if (rs.next())
                    fileData.setTime(rs.getTimestamp(1));
            }
        }

        String name = NAME_FORMAT.format(fileData.getId()) + "_" + fileData.getSecret();

        // old format in a flat list
        var result = new File(storeDir, name);
        // new format in subdirectories
        if (!result.exists()) {
            var dir = new File(storeDir, DIR_FORMAT.format(TimeConvert.toLocalDate(fileData.getTime())));
            if (!dir.exists())
                dir.mkdirs();
            result = new File(dir, name);
        }

        return result;
    }

    /**
     * Moves batch of files placed in the root dir to subdirectories yyyy/MM/dd.
     * @param batchSize batch size.
     * @return is there something more to move.
     * @throws SQLException, IOException
     */
    public boolean moveBatch(int batchSize) throws SQLException, IOException {
        var files = storeDir.list();

        String query = SQL_SELECT_ALL_FROM + TABLE_FILE_DATA + SQL_WHERE + "id=?";
        try (var ps = con.prepareStatement(query)) {
            for (int i = 0; i < Math.min(batchSize, files.length); i++) {
                String fileName = files[i];
                File file = new File(storeDir, fileName);

                if (file.isDirectory())
                    continue;

                log.info("Moving file: {}", fileName);

                ps.setInt(1, Utils.parseInt(StringUtils.substringBefore(fileName, "_")));
                var rs = ps.executeQuery();
                if (!rs.next()) {
                    log.error("Not found DB entry for file: {}", fileName);
                    continue;
                }

                var fileData = getFromRs(rs, "");

                FileUtils.moveFileToDirectory(file, new File(storeDir, DIR_FORMAT.format(TimeConvert.toLocalDate(fileData.getTime()))), true);
            }
        }

        return batchSize < files.length;
    }

    @Deprecated
    public File getFile(String url) {
        return new File(storeDir.getAbsolutePath() + "/" + url);
    }
}
