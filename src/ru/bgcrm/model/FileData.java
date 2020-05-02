package ru.bgcrm.model;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import ru.bgcrm.model.user.User;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.io.Base64;

public class FileData extends IdTitle {
    private static final Logger log = Logger.getLogger(FileData.class);

    private String secret;
    private Date time;
    private User user;
    private String comment;
    private int version;
    private OutputStream outputStream;

    public FileData() {}

    public FileData(int id, String title, String secret) {
        super(id, title);
        this.secret = secret;
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

    public static String serialize(List<FileData> fileList) {
        StringBuilder result = new StringBuilder(250);

        for (FileData data : fileList) {
            if (result.length() > 0) {
                result.append(";");
            }
            result.append(data.getId());
            result.append(":");
            result.append(Base64.encode(data.getTitle()));
            result.append(":");
            result.append(data.getSecret());
        }

        return result.toString();
    }

    public static List<FileData> parse(String str) {
        List<FileData> result = new ArrayList<FileData>();

        StringTokenizer st = new StringTokenizer(str, ";");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();

            String[] tokens = token.split(":");
            if (tokens.length != 3) {
                log.error("Incorrect file data: " + str + "; token: " + token);
                continue;
            }

            result.add(new FileData(Utils.parseInt(tokens[0]), Base64.decode(tokens[1]), tokens[2]));
        }

        return result;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }
}
