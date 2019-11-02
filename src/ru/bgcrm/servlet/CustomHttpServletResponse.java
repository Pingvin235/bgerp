package ru.bgcrm.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import ru.bgcrm.util.Utils;

public class CustomHttpServletResponse extends HttpServletResponseWrapper {
    private CustomServletOutPutStream outputStream;
    private PrintWriter printWriter;

    public CustomHttpServletResponse(HttpServletResponse response, OutputStream result) throws UnsupportedEncodingException {
        super(response);

        this.outputStream = new CustomServletOutPutStream(result);
        this.printWriter = new PrintWriter(new OutputStreamWriter(outputStream, Utils.UTF8));
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return this.outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return this.printWriter;
    }

    public void flush() {
        printWriter.flush();
    }

    private static class CustomServletOutPutStream extends ServletOutputStream {
        private final OutputStream buffer;

        private CustomServletOutPutStream(OutputStream buffer) {
            this.buffer = buffer;
        }

        @Override
        public void write(int b) throws IOException {
            buffer.write(b);
        }

        @Override
        public void write(byte b[], int off, int len) throws IOException {
            buffer.write(b, off, len);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
        }
    }
}