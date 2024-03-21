package org.bgerp.plugin.pln.callboard.action.admin;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.struts.action.ActionForward;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.cache.ParameterCache;
import org.bgerp.model.Pageable;
import org.bgerp.model.base.IdTitle;
import org.bgerp.plugin.pln.callboard.Plugin;
import org.bgerp.plugin.pln.callboard.cache.CallboardCache;
import org.bgerp.plugin.pln.callboard.dao.ShiftDAO;
import org.bgerp.plugin.pln.callboard.dao.WorkTypeDAO;
import org.bgerp.plugin.pln.callboard.model.Shift;
import org.bgerp.plugin.pln.callboard.model.WorkType;
import org.bgerp.plugin.pln.callboard.model.WorkTypeTime;
import org.bgerp.plugin.pln.callboard.model.config.ShortcutConfig;

import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

@Action(path = "/admin/plugin/callboard/work")
public class WorkAction extends org.bgerp.plugin.pln.callboard.action.WorkAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_ADMIN;

    public ActionForward workTypeList(DynActionForm form, Connection con) throws Exception {
        int categoryId = form.getParamInt("categoryId", 0);

        form.getHttpRequest().setAttribute("allowOnlyCategories", getAvailableCategories(form.getPermission()));

        new WorkTypeDAO(con).searchWorkType(new Pageable<WorkType>(form), categoryId);

        return html(con, form, PATH_JSP + "/type/list.jsp");
    }

    public ActionForward workTypeGet(DynActionForm form, Connection con) throws Exception {
        int paramater = Setup.getSetup().getInt("callboard.serviceListId", 0);
        WorkType workType = new WorkTypeDAO(con).getWorkType(form.getId());
        var request = form.getHttpRequest();

        if (paramater > 0) {
            request.setAttribute("servicesList", ParameterCache.getParameter(paramater).getListParamValues());
        }

        if (workType != null) {
            form.getResponse().setData("workType", workType);
        }

        request.setAttribute("shortcutMap", setup.getConfig(ShortcutConfig.class).getShortcutMap());
        request.setAttribute("allowOnlyCategories", getAvailableCategories(form.getPermission()));

        return html(con, form, PATH_JSP + "/type/update.jsp");
    }

    public ActionForward workTypeUpdate(DynActionForm form, Connection con) throws Exception {
        if (form.getParam("title").length() == 0) {
            throw new BGException("Не выбрано название для этого типа работ");
        }

        int category = form.getParamInt("categoryId", 0);

        Set<Integer> allowCategorySet = new HashSet<Integer>();
        for (IdTitle item : getAvailableCategories(form.getPermission())) {
            allowCategorySet.add(item.getId());
        }

        if (!allowCategorySet.contains(category)) {
            throw new BGException("Не выбрана категория, либо нет прав на выбранную категорию");
        }

        WorkType workType = null;

        if (form.getId() > 0) {
            workType = new WorkTypeDAO(con).getWorkType(form.getId());

            if (workType == null) {
                throw new BGMessageException("Не удалось найти в БД тип работы с ID=" + form.getId());
            }
        } else {
            workType = new WorkType();
        }

        workType.setCategory(category);
        workType.setTitle(form.getParam("title"));
        workType.setComment(form.getParam("comment"));

        workType.setColor(form.getParam("color"));
        workType.setShortcutList(Utils.toList(form.getParam("shortcuts", "")));
        workType.setTimeSetStep(form.getParamInt("timeSetStep"));
        workType.setTimeSetMode(form.getParamInt("timeSetMode"));

        workType.setNonWorkHours(form.getParamBoolean("nonWorkHours", false));
        workType.setRuleConfig(form.getParam("ruleConfig"));

        new WorkTypeDAO(con).updateWorkType(workType);

        CallboardCache.flush(con);

        return html(con, form, PATH_JSP + "/type/list.jsp");
    }

    public ActionForward workTypeDelete(DynActionForm form, Connection con) throws Exception {
        new WorkTypeDAO(con).deleteWorkType(form.getId());

        CallboardCache.flush(con);

        return json(con, form);
    }

    public ActionForward shiftList(DynActionForm form, Connection con) throws Exception {
        int categoryId = form.getParamInt("categoryId", 0);

        form.getHttpRequest().setAttribute("allowOnlyCategories", getAvailableCategories(form.getPermission()));

        new ShiftDAO(con).searchShift(new Pageable<Shift>(form), categoryId);

        return html(con, form, PATH_JSP + "/shift/list.jsp");
    }

    public ActionForward shiftGet(DynActionForm form, Connection con) throws Exception {
        Map<Integer, WorkType> workTypeMap = new HashMap<Integer, WorkType>();
        Set<Integer> categoryIds = getAvailableCategoryIds(form.getPermission());

        for (Entry<Integer, WorkType> entry : CallboardCache.getWorkTypeMap().entrySet()) {
            if (categoryIds.contains(entry.getValue().getCategory())) {
                workTypeMap.put(entry.getKey(), entry.getValue());
            }
        }

        Shift shift = new ShiftDAO(con).getShift(form.getId());

        if (shift != null) {
            form.getResponse().setData("shift", shift);
            form.getResponse().setData("workTypeMap", workTypeMap);
        }

        form.getHttpRequest().setAttribute("allowOnlyCategories", getAvailableCategories(form.getPermission()));
        form.getResponse().setData("workTypeList", new ArrayList<WorkType>(workTypeMap.values()));

        return html(con, form, PATH_JSP + "/shift/update.jsp");
    }

    public ActionForward shiftUpdate(DynActionForm form, Connection con) throws Exception {
        if (form.getParam("title").length() == 0) {
            throw new BGException("Не выбрано название для этой смены");
        }

        int category = form.getParamInt("categoryId", 0);
        String symbol = form.getParam("symbol", "");

        Set<Integer> allowCategorySet = getAvailableCategoryIds(form.getPermission());
        if (!allowCategorySet.contains(category)) {
            throw new BGException("Не выбрана категория, либо нет прав на выбранную категорию");
        }

        //цвет смены
        boolean useOwnColor = form.getParamValues("useOwnColor").size() > 0;
        String color = form.getParam("color", "");

        //правила
        List<WorkTypeTime> ruleList = new ArrayList<WorkTypeTime>();
        try {
            for (String item : form.getParamArray("rule")) {
                WorkTypeTime workTypeTime = new WorkTypeTime();

                String[] tokens = item.split(":");
                if (tokens.length < 3) {
                    continue;
                }

                workTypeTime.setWorkTypeId(Utils.parseInt(tokens[0]));
                workTypeTime.setDayMinuteFrom(Utils.parseInt(tokens[1]));
                workTypeTime.setDayMinuteTo(Utils.parseInt(tokens[2]));

                ruleList.add(workTypeTime);
            }
        } catch (Exception e) {
            throw new BGException("Не выбрано ни одного правила для этого типа работ");
        }

        Shift shift = form.getId() > 0 ? new ShiftDAO(con).getShift(form.getId()) : new Shift();
        shift.setWorkTypeTimeList(ruleList);
        shift.setTitle(form.getParam("title"));
        shift.setCategory(category);
        shift.setUseOwnColor(useOwnColor);
        shift.setColor(color);
        shift.setSymbol(symbol);

        new ShiftDAO(con).updateShift(shift);

        return html(con, form, PATH_JSP + "/shift/list.jsp");
    }

    public ActionForward shiftDelete(DynActionForm form, Connection con) throws Exception {
        new ShiftDAO(con).deleteShift(form.getId());

        return json(con, form);
    }
}
