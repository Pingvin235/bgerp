package ru.bgcrm.model.param;

public class JumpRegexp {
    private String regexp;
    private boolean moveLastChar = false;

    public JumpRegexp(String regexp, boolean moveLastChar) {
        this.regexp = regexp;
        this.moveLastChar = moveLastChar;
    }

    public String getRegexp() {
        return regexp;
    }

    public void setRegexp(String regexp) {
        this.regexp = regexp;
    }

    public boolean isMoveLastChar() {
        return moveLastChar;
    }

    public void setMoveLastChar(boolean moveLastChar) {
        this.moveLastChar = moveLastChar;
    }
}
