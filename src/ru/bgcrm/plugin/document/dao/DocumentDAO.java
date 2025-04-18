package ru.bgcrm.plugin.document.dao;

import static ru.bgcrm.dao.Tables.TABLE_FILE_DATA;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.bgerp.app.exception.BGException;
import org.bgerp.dao.FileDataDAO;
import org.bgerp.model.Pageable;
import org.bgerp.model.file.FileData;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.plugin.document.model.Document;

public class DocumentDAO extends CommonDAO {
    private static String TABLE_DOCUMENT = " document ";

    public DocumentDAO(Connection con) {
        super(con);
    }

    /**
     * Добавляет привязанный к объекту документ.
     * @param objectType
     * @param objectId
     * @param data
     * @param title
     */
    public void add(String objectType, int objectId, byte[] data, String title) {
        try {
            FileData fileData = new FileData();
            fileData.setTitle(title);

            FileOutputStream fos = new FileDataDAO(con).add(fileData);
            fos.write(data);
            fos.close();

            Document doc = new Document();
            doc.setFileDataId(fileData.getId());
            doc.setFileData(fileData);
            doc.setObjectId(objectId);
            doc.setObjectType(objectType);

            add(doc);
        } catch (Exception ex) {
            throw new BGException(ex);
        }
    }

    /**
     * Добавляет документ. Файл должен быть загружен ранее с помощью {@link FileDataDAO}.
     * @param b
     * @throws SQLException
     */
    public void add(Document b) throws SQLException {
        String query = SQL_INSERT_INTO + TABLE_DOCUMENT + " (object_type, object_id, file_data_id) " + " VALUES (?,?,?) ";

        PreparedStatement ps = con.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);

        ps.setString(1, b.getObjectType());
        ps.setInt(2, b.getObjectId());
        ps.setInt(3, b.getFileDataId());

        ps.executeUpdate();

        b.setId(lastInsertId(ps));

        ps.close();
    }

    /**
     * Удаляет документ и привязанный к нему файл.
     * @param doc
     * @throws Exception
     */
    public void delete(Document doc) throws Exception {
        deleteById(TABLE_DOCUMENT, doc.getId());
        if (doc.getFileData() != null) {
            new FileDataDAO(con).delete(doc.getFileData());
        }
    }

    /**
     * Возвращает документ по его ID.
     * @param id
     * @return
     */
    public Document getDocumentById(int id) {
        Document result = null;

        String query = SQL_SELECT + " doc.*, fd.*  " + SQL_FROM + TABLE_DOCUMENT + " AS doc " + SQL_INNER_JOIN
                + TABLE_FILE_DATA + " AS fd ON doc.file_data_id=fd.id " + SQL_WHERE + " doc.id=?";

        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = getFromRS(rs, "doc.");
                result.setFileData(FileDataDAO.getFromRs(rs, "fd."));
            }
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }

    /**
     * Ищет документы привязанные к объекту.
     * @param result
     * @param objectType
     * @param objectId
     */
    public void searchObjectDocuments(Pageable<Document> result, String objectType, int objectId)
            {
        List<Document> list = result.getList();

        try {
            PreparedStatement ps = con.prepareStatement(SQL_SELECT + "d.*, f.*" + SQL_FROM + TABLE_DOCUMENT + "AS d"
                    + SQL_INNER_JOIN + TABLE_FILE_DATA + "AS f ON d.file_data_id=f.id" + SQL_WHERE
                    + "d.object_type=? AND d.object_id=?" + SQL_ORDER_BY + "d.id");
            ps.setString(1, objectType);
            ps.setInt(2, objectId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Document doc = getFromRS(rs, "d.");
                doc.setFileData(FileDataDAO.getFromRs(rs, "f."));
                list.add(doc);
            }
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public OutputStream createDocumentFile(Document doc, String title) throws Exception {
        FileDataDAO fileDao = new FileDataDAO(con);
        DocumentDAO docDao = new DocumentDAO(con);

        FileData file = new FileData();
        file.setTitle(title);
        OutputStream out = fileDao.add(file);

        doc.setFileDataId(file.getId());

        docDao.add(doc);

        return out;
    }

    public static Document getFromRS(ResultSet rs, String prefix) throws SQLException {
        Document doc = new Document();

        doc.setId(rs.getInt(prefix + "id"));
        doc.setObjectType(rs.getString(prefix + "object_type"));
        doc.setObjectId(rs.getInt(prefix + "object_id"));
        doc.setFileDataId(rs.getInt(prefix + "file_data_id"));

        return doc;
    }
}
