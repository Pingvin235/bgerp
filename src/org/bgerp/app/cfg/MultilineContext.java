package org.bgerp.app.cfg;

final class MultilineContext {
    static final String MULTILINE_PREFIX = "<<";
    static final int MULTILINE_PREFIX_LENGTH = MULTILINE_PREFIX.length();

    String key;
    StringBuilder multiline = new StringBuilder(10);
    String endOfLine;
}