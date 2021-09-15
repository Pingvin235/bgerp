package ru.bgcrm.dao;

import static ru.bgcrm.dao.Tables.TABLE_FILE_DATA;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.FileData;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class FileDataDAO extends CommonDAO {
    private static final DecimalFormat nameFormatPattern = new DecimalFormat("0000000000");

    private File storeDir;

    public FileDataDAO(Connection con) {
        super(con);
        storeDir = Utils.createDirectoryIfNoExistInWorkDir("filestorage");
    }

    public static FileData getFromRs(ResultSet rs, String prefix) throws SQLException {
        FileData result = new FileData();

        result.setId(rs.getInt(prefix + "id"));
        result.setTitle(rs.getString(prefix + "title"));
        result.setTime(TimeUtils.convertTimestampToDate(rs.getTimestamp(prefix + "time")));
        result.setSecret(rs.getString("secret"));

        return result;
    }

    private void checkDir() throws BGException {
        if (!storeDir.exists() || !storeDir.isDirectory()) {
            throw new BGException("Не найден каталог хранения файлов.");
        }
        if (!storeDir.canRead() || !storeDir.canWrite()) {
            throw new BGException("Ошибка доступа к каталогу хранения файлов.");
        }
    }

    public FileOutputStream add(FileData file) throws BGException, FileNotFoundException {
        checkDir();

        file.setSecret(Utils.generateSecret());

        try {
            String query = "INSERT INTO " + TABLE_FILE_DATA + " (title, time, secret) VALUES (?, NOW(), ?)";
            PreparedStatement ps = con.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, file.getTitle());
            ps.setString(2, file.getSecret());
            ps.executeUpdate();

            file.setId(lastInsertId(ps));

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        var outputStream = new FileOutputStream(getFile(file));
        file.setOutputStream(outputStream);

        return outputStream;
    }

    public void delete(FileData fileData) throws BGException {
        try {
            checkDir();

            String query = "DELETE FROM " + TABLE_FILE_DATA + " WHERE id=? AND secret=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, fileData.getId());
            ps.setString(2, fileData.getSecret());
            ps.executeUpdate();
            ps.close();

            File file = getFile(fileData);
            file.delete();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public File getFile(FileData fileData) {
        return new File(storeDir.getAbsolutePath() + "/" + nameFormatPattern.format(fileData.getId()) + "_" + fileData.getSecret());
    }

    public File getFile(String url) {
        return new File(storeDir.getAbsolutePath() + "/" + url);
    }
}
