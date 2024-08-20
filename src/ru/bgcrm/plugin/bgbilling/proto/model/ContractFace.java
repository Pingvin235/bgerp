package ru.bgcrm.plugin.bgbilling.proto.model;

public class ContractFace extends UserTime {
    public static final int JURAL_FACE = 1;
    public static final int PHYSICAL_FACE = 0;

    private String face;

    public String getFace() {
        return face;
    }

    public void setFace(String face) {
        this.face = face;
    }
}
