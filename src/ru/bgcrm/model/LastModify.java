package ru.bgcrm.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;

import com.fasterxml.jackson.annotation.JsonFormat;

public class LastModify {
    private int userId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TimeUtils.PATTERN_YYYYMMDDHHMMSS)
    private Date time;

    public LastModify() {
    }

    public LastModify(int userId, Date time) {
        this.userId = userId;
        this.time = time;
    }

    public LastModify(DynActionForm form) {
        try {
            this.userId = form.getParamInt("lastModifyUserId");
            this.time = new SimpleDateFormat(TimeUtils.PATTERN_YYYYMMDDHHMMSS).parse(form.getParam("lastModifyTime"));
        } catch (ParseException e) {
            this.userId = form.getUserId();
            this.time = new Date();
        }
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date date) {
        this.time = date;
    }
}