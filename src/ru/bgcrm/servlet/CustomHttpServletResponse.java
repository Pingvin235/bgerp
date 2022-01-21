package ru.bgcrm.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import ru.bgcrm.struts.form.DynActionForm;

/**
 * Implementation of {@link javax.servlet.ServletResponse} allowing
 * to intercept JSP template output.
 */
public class CustomHttpServletResponse extends HttpServletResponseWrapper {
    private CustomServletOutPutStream outputStream;
    private PrintWriter printWriter;

    public CustomHttpServletResponse(HttpServletResponse response, OutputStream result) throws UnsupportedEncodingException {
        super(response);

        this.outputStream = new CustomServletOutPutStream(result);
        this.printWriter = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
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

    /**
     * Renders JSP page. Clears {@link ru.bgcrm.struts.form.Response#getData()} in {@link DynActionForm#getResponse()}.
     * @param form form object, contains servlet request and response.
     * @param jsp path to JSP pattern, starting from webapps.
     * @param approxSize expected size of resulting HTML.
     * @return byte with HTML.
     * @throws Exception
     */
    public static byte[] jsp(DynActionForm form, String jsp, int approxSize) throws Exception {
        var bos = new ByteArrayOutputStream(approxSize);
        var resp = new CustomHttpServletResponse(form.getHttpResponse(), bos);
        var req = form.getHttpRequest();

        req.getRequestDispatcher(jsp).include(req, resp);
        resp.flush();

        form.getResponse().getData().clear();

        return bos.toByteArray();
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
        public void setWriteListener(WriteListener writeListener) {}
    }
}