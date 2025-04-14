package org.bgerp.model.file;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.bgerp.dao.FileDataDAO;
import org.bgerp.model.base.IdTitle;
import org.bgerp.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ru.bgcrm.util.Utils;

/**
 * DB table entity with file metadata: ID, title, upload time.
 * Files themselves are persisted in filestorage directory.
 * @see FileDataDAO
 *
 * @author Shamil Vakhitov
 */
public class FileData extends IdTitle {
    private static final Log log = Log.getLog();

    private String secret;
    private Date time;
    /** Data to be stored. Not provided when reading. */
    private byte[] data;
    @JsonIgnore
    private OutputStream outputStream;

    public FileData() {}

    public FileData(int id, String title, String secret) {
        super(id, title);
        this.secret = secret;
    }

    public FileData(String title, byte[] data) {
        this.title = title;
        this.data = data;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String key) {
        this.secret = key;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * @return output stream to the stored file.
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public static String serialize(List<FileData> fileList) {
        StringBuilder result = new StringBuilder(250);

        for (FileData data : fileList) {
            if (result.length() > 0) {
                result.append(";");
            }
            result.append(data.getId());
            result.append(":");
            result.append(Base64.getEncoder().encodeToString(data.getTitle().getBytes(StandardCharsets.UTF_8)));
            result.append(":");
            result.append(data.getSecret());
        }

        return result.toString();
    }

    public static List<FileData> parse(String str) {
        List<FileData> result = new ArrayList<>();

        StringTokenizer st = new StringTokenizer(str, ";");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();

            String[] tokens = token.split(":");
            if (tokens.length != 3) {
                log.error("Incorrect file data: " + str + "; token: " + token);
                continue;
            }

            result.add(new FileData(Utils.parseInt(tokens[0]), new String(Base64.getDecoder().decode(tokens[1]), StandardCharsets.UTF_8), tokens[2]));
        }

        return result;
    }
}
