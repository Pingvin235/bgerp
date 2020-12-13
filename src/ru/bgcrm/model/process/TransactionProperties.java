package ru.bgcrm.model.process;

import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

/**
 * Properties of process' status change.
 * 
 * @author Shamil Vakhitova
 */
public class TransactionProperties {
    public static final TransactionProperties ENABLED = new TransactionProperties(true);

    /** The transaction is enabled. */
    private boolean enable = true;
    /** Not used yet. */
    private String reference = "";

    public TransactionProperties(boolean enabled) {
        this.enable = enabled;
    }

    public TransactionProperties(ParameterMap data, String prefix) {
        this.enable = data.getBoolean(prefix + "enable", true);
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void serializeToData(StringBuilder data, String prefix) {
        Utils.addSetupPair(data, prefix, "enable", Utils.booleanToStringInt(enable));
        data.append("#\n");
    }
    public static class TransactionKey {
        public int fromStatus;
        public int toStatus;

        public TransactionKey(int fromStatus, int toStatus) {
            this.fromStatus = fromStatus;
            this.toStatus = toStatus;
        }

        public TransactionKey(String fromToStatus) {
            String[] from_to_status = fromToStatus.split("\\-");
            this.fromStatus = Utils.parseInt(from_to_status[0]);
            this.toStatus = Utils.parseInt(from_to_status[1]);
        }

        @Override
        public boolean equals(Object obj) {
            TransactionKey key = (TransactionKey) obj;
            return key.fromStatus == fromStatus && key.toStatus == toStatus;
        }

        @Override
        public int hashCode() {
            return fromStatus;
        }
    }
}