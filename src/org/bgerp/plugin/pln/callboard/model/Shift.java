package org.bgerp.plugin.pln.callboard.model;

import java.util.ArrayList;
import java.util.List;

import ru.bgcrm.model.IdTitle;
import ru.bgcrm.util.Utils;

public class Shift extends IdTitle {
    private boolean useOwnColor;
    private int category;
    private String comment = "";
    private String color = "";
    private String symbol = "";
    private List<WorkTypeTime> workTypeTimeList = new ArrayList<WorkTypeTime>();

    public boolean isUseOwnColor() {
        return useOwnColor;
    }

    public void setUseOwnColor(boolean useOwnColor) {
        this.useOwnColor = useOwnColor;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public List<WorkTypeTime> getWorkTypeTimeList() {
        return workTypeTimeList;
    }

    public void setWorkTypeTimeList(List<WorkTypeTime> workTypeTimeList) {
        this.workTypeTimeList = workTypeTimeList;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String serializeToData() {
        StringBuilder result = new StringBuilder();
        int index = 0;

        for (WorkTypeTime item : workTypeTimeList) {
            Utils.addSetupPair(result, "", "rule." + index + ".workTypeId", String.valueOf(item.getWorkTypeId()));
            // TODO: Поддержка оригинального формата хранения для старой ЦРМки. Убрать в
            // дальнейшем.
            Utils.addSetupPair(result, "", "rule." + index + ".timeFrom",
                    "01/01/2010 " + WorkTypeTime.minutesToHourMin(item.getDayMinuteFrom()));
            Utils.addSetupPair(result, "", "rule." + index + ".timeTo",
                    "01/01/2010 " + WorkTypeTime.minutesToHourMin(item.getDayMinuteTo()));
            Utils.addSetupPair(result, "", "rule." + index + ".isDynamic", String.valueOf(item.isDynamic() ? 1 : 0));

            index++;
        }

        return result.toString();
    }

    public int getWorkTypeCount() {
        return workTypeTimeList.size();
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
