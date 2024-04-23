package ru.bgcrm.event;

import java.util.Date;
import java.util.List;

import org.bgerp.event.base.UserEvent;
import org.bgerp.model.param.Parameter;

import ru.bgcrm.struts.form.DynActionForm;

/**
 * Раскрашивает календарь(datetimepicker)
 * dayColorList месяцы с 1 по 12
 *
 * List<DateInfo> test = new ArrayList<DateInfo>();

   if( e.getNewDate().getMonth() > 2 )
    {
            test.add( new DateInfo( "11-1", "red", "red" ) );
            test.add( new DateInfo( "11-11", "green", "" ) );
            test.add( new DateInfo( "11-21", "blue", "123" ) );
            test.add( new DateInfo( "11-25", "", "123fsdgfdfgdf" ) );
    }
    e.getForm().setResponseData( "dayColorList", test );
 *
 */
public class DateChangingEvent extends UserEvent {
    public static class DateInfo {
        private String monthDay;
        private String color;
        private String comment;
        private List<String> timeList;

        public DateInfo(String monthDay, String color, String comment) {
            setMonthDay(monthDay);
            setColor(color);
            setComment(comment);
        }

        public DateInfo(String monthDay, String color, String comment, List<String> timeList) {
            this(monthDay, color, comment);
            setTimeList(timeList);
        }

        public List<String> getTimeList() {
            return timeList;
        }

        public void setTimeList(List<String> timeList) {
            this.timeList = timeList;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getMonthDay() {
            return monthDay;
        }

        public void setMonthDay(String monthDay) {
            this.monthDay = monthDay;
        }
    }

    private final int objectId;
    private final Parameter parameter;
    private final Date newDate;

    public DateChangingEvent(DynActionForm form, int objectId, Parameter parameter, Date newDate) {
        super(form);

        this.objectId = objectId;
        this.parameter = parameter;
        this.newDate = newDate;
    }

    public int getObjectId() {
        return objectId;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public Date getNewDate() {
        return newDate;
    }
}