package org.bgerp.plugin.pln.callboard.model;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ru.bgcrm.util.TimeUtils;

public class WorkShift {
    private int id;
    private int userId;
    private int graphId;
    private int groupId;
    private int shiftId;
    private int team;
    private Date date;
    private List<WorkTypeTime> workTypeTimeList;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getGraphId() {
        return graphId;
    }

    public void setGraphId(int graphId) {
        this.graphId = graphId;
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<WorkTypeTime> getWorkTypeTimeList() {
        return workTypeTimeList;
    }

    public void setWorkTypeTimeList(List<WorkTypeTime> workTypeTime) {
        this.workTypeTimeList = workTypeTime;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getWorkTypeTimeCount() {
        return workTypeTimeList == null ? 0 : workTypeTimeList.size();
    }

    public int getShiftId() {
        return shiftId;
    }

    public void setShiftId(int shiftId) {
        this.shiftId = shiftId;
    }

    public int getWorkMinutesInDay(Date dateStart, Date inDate) {
        return WorkTypeTime.getWorkMinutesInDay(workTypeTimeList, dateStart, inDate);
    }

    public Calendar getTimeFrom(Date date) {
        int minuteFrom = workTypeTimeList.get(0).getDayMinuteFrom();

        Calendar result = TimeUtils.convertDateToCalendar(date);
        result.add(Calendar.MINUTE, minuteFrom);

        return result;
    }

    public Calendar getTimeTo(Date date) {
        int minuteTo = workTypeTimeList.get(workTypeTimeList.size() - 1).getDayMinuteTo();

        Calendar result = TimeUtils.convertDateToCalendar(date);
        result.add(Calendar.MINUTE, minuteTo);

        return result;
    }
}