package ru.bgcrm.plugin.bgbilling.proto.model.voice;

import org.bgerp.model.base.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PhoneResource extends Id {
    private long phoneFrom;
    private long phoneTo;

    public long getPhoneFrom() {
        return phoneFrom;
    }

    public void setPhoneFrom(long phoneFrom) {
        this.phoneFrom = phoneFrom;
    }

    public long getPhoneTo() {
        return phoneTo;
    }

    public void setPhoneTo(long phoneTo) {
        this.phoneTo = phoneTo;
    }

    @JsonIgnore
    public String getTitle() {
        return phoneFrom + " - " + phoneTo;
    }
}
