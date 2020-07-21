package ru.bgcrm.util.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Обёртка для кодирования base64 потока.
 * После использования обязательно надо закрыть, только догда остаток байтов
 * запишется в поток (или добьётся символами '=', как положено).
 */
public class Base64OutputStream extends OutputStream {
    /**
     * Спрятанный стрим.
     */
    private OutputStream outputStream = null;

    /**
     * Текущей буфер (тут копится по 3 символа входных=4выходных)
     */
    private int currentBuffer = 0;

    /**
     * Кол-во обработанных входных байтов в буфере.
     */
    private int currentBufferCounter = 0;

    /**
     * Длина составленной строки.
     */
    private int outputLineCounter = 0;

    /**
     * Нужная длина строки.
     */
    private int outputLineMaxlength = 0;

    /**
     * Создание обёртки из потока готового. Каждые 76 символов (по rfc)
     * переносится на новую строку через CRLF.
     * 
     * @param outputStream
     *            Исходный поток.
     */
    public Base64OutputStream(OutputStream outputStream) {
        this(outputStream, 76);
    }

    /**
     * Создание обёртки из потока готового и длины строки. Каждые несколько
     * символов переносится на новую строку через CRLF.
     * 
     * @param outputStream
     *            Исходный поток.
     * @param linelength
     *            максимальная длина одной строки (или 0 если не надо разбивать
     *            выход на строки)
     */
    public Base64OutputStream(OutputStream outputStream, int linelength) {
        this.outputStream = outputStream;
        this.outputLineMaxlength = linelength;
    }

    @Override
    public void write(int b) throws IOException {
        int value = (b & 0xFF) << (16 - (currentBufferCounter * 8));
        currentBuffer = currentBuffer | value;
        ++currentBufferCounter;
        if (currentBufferCounter == 3) {
            flushbuffer();
        }
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        flushbuffer();
        outputStream.close();
    }

    /**
     * Внутранняя - преобразует входные байты в выходные. Вызывается через
     * каждые 3 байта или перед закрытием.
     */
    protected void flushbuffer() throws IOException {
        if (currentBufferCounter > 0) {
            if (outputLineMaxlength > 0 && outputLineCounter == outputLineMaxlength) {
                outputStream.write("\r\n".getBytes());
                outputLineCounter = 0;
            }
            char b1 = Base64.CHARS.charAt((currentBuffer << 8) >>> 26);
            char b2 = Base64.CHARS.charAt((currentBuffer << 14) >>> 26);
            char b3 = (currentBufferCounter < 2) ? Base64.PAD : Base64.CHARS.charAt((currentBuffer << 20) >>> 26);
            char b4 = (currentBufferCounter < 3) ? Base64.PAD : Base64.CHARS.charAt((currentBuffer << 26) >>> 26);
            outputStream.write(b1);
            outputStream.write(b2);
            outputStream.write(b3);
            outputStream.write(b4);
            outputLineCounter += 4;
            currentBufferCounter = 0;
            currentBuffer = 0;
        }
    }
}