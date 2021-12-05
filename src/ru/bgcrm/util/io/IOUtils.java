package ru.bgcrm.util.io;

import java.io.File;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class IOUtils {
    public static OutputStream urlEncode(OutputStream out) throws IOException {
        return new Base64OutputStream(new FilterOutputStream(out) {
            @Override
            public void write(int b) throws IOException {
                if (b == '+') {
                    super.write('-');
                } else if (b == '/') {
                    super.write('_');
                } else if (b == '=') {
                    super.write(',');
                } else {
                    super.write(b);
                }
            }
        }, 0); // "Base64"
    }

    public static InputStream urlDecode(InputStream in) throws IOException {
        return new Base64InputStream(new FilterInputStream(in) {
            @Override
            public int read() throws IOException {
                int b = super.read();

                if (b == '-') {
                    return '+';
                } else if (b == '_') {
                    return '/';
                } else if (b == ',') {
                    return '=';
                } else {
                    return b;
                }
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                int result = super.read(b, off, len);

                for (int i = off, size = off + result; i < size; i++) {
                    if (b[i] == '-') {
                        b[i] = '+';
                    } else if (b[i] == '_') {
                        b[i] = '/';
                    } else if (b[i] == ',') {
                        b[i] = '=';
                    }
                }

                return result;
            }

        });
    }

    /**
     * Use {@link org.apache.commons.io.IOUtils#copy(InputStream, OutputStream)}.
     */
    @Deprecated
    public static boolean flush(InputStream inputStream, OutputStream outputStream) throws IOException {
        boolean wasFlush = false;
        // буфер 1KB
        byte[] buf = new byte[1024];
        int len;
        while ((len = inputStream.read(buf)) != -1) {
            outputStream.write(buf, 0, len);
            wasFlush = true;
        }
        return wasFlush;
    }

    /**
     * Reads byte array out of file with {@code path} case if exists,
     * otherwise treats {@code path} as Java resource and reads it.
     * @param path file path or Java resource identifier, examples: {@code /org/bgerp/util/SomeFile.data} or {@code /tmp/SomeFile.data}.
     * @return
     * @throws IOException
     */
    public static final byte[] read(String path) throws IOException {
        var file = new File(path);
        if (file.exists() && file.isFile() && file.canRead())
            return Files.readAllBytes(file.toPath());
        return org.apache.commons.io.IOUtils.resourceToByteArray(path);
    }
}