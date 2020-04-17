package ru.bgcrm.struts.action.admin;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.cache.CallboardCache;
import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.dao.work.ShiftDAO;
import ru.bgcrm.dao.work.TabelDAO;
import ru.bgcrm.dao.work.WorkTypeDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.user.Group;
import ru.bgcrm.model.user.User;
import ru.bgcrm.model.user.UserGroup;
import ru.bgcrm.model.work.DayType;
import ru.bgcrm.model.work.Shift;
import ru.bgcrm.model.work.WorkDaysCalendar;
import ru.bgcrm.model.work.WorkShift;
import ru.bgcrm.model.work.WorkType;
import ru.bgcrm.model.work.WorkTypeTime;
import ru.bgcrm.model.work.config.CalendarConfig;
import ru.bgcrm.model.work.config.CallboardConfig;
import ru.bgcrm.model.work.config.CallboardConfig.Callboard;
import ru.bgcrm.model.work.config.CategoryConfig;
import ru.bgcrm.model.work.config.CategoryConfig.Category;
import ru.bgcrm.model.work.config.DayTypeConfig;
import ru.bgcrm.model.work.config.ShortcutConfig;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class WorkAction extends BaseAction {
    public ActionForward workTypeList(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        int categoryId = form.getParamInt("categoryId", 0);

        form.getHttpRequest().setAttribute("allowOnlyCategories", getAvailableCategories(form.getPermission()));

        new WorkTypeDAO(con).searchWorkType(new SearchResult<WorkType>(form), categoryId);

        return processUserTypedForward(con, mapping, form, "typeList");
    }

    public ActionForward workTypeGet(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
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

        return processUserTypedForward(con, mapping, form, "typeUpdate");
    }

    public ActionForward workTypeUpdate(ActionMapping mapping, DynActionForm form, HttpServletRequest request, HttpServletResponse response,
            Connection con) throws Exception {
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

        return processUserTypedForward(con, mapping, form, "typeList");
    }

    public ActionForward workTypeDelete(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        new WorkTypeDAO(con).deleteWorkType(form.getId());

        CallboardCache.flush(con);

        return processJsonForward(con, form);
    }

    public ActionForward shiftList(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        int categoryId = form.getParamInt("categoryId", 0);

        form.getHttpRequest().setAttribute("allowOnlyCategories", getAvailableCategories(form.getPermission()));

        new ShiftDAO(con).searchShift(new SearchResult<Shift>(form), categoryId);

        return processUserTypedForward(con, mapping, form, "shiftList");
    }

    public ActionForward shiftGet(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
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

        return processUserTypedForward(con, mapping, form, "shiftUpdate");
    }

    public ActionForward shiftUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
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
        boolean useOwnColor = form.getSelectedValues("useOwnColor").size() > 0;
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

        return processUserTypedForward(con, mapping, form, "shiftList");
    }

    public ActionForward shiftDelete(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        new ShiftDAO(con).deleteShift(form.getId());

        return processJsonForward(con, form);
    }

    public ActionForward callboardGet(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        long time = System.currentTimeMillis();

        int graphId = form.getParamInt("graphId", 0);

        ParameterMap perm = form.getPermission();

        CallboardConfig config = setup.getConfig(CallboardConfig.class);

        log.debug("callboardGet1: " + (System.currentTimeMillis() - time) + " ms.");

        form.getResponse().setData("callboardList",
                config.getCallboards(Utils.toIntegerSet(perm.get("allowOnlyCallboards", perm.get("allowOnlyTabels")))));

        //определние начальной и конечной даты, формирование сета с датами для шапки графика
        Date fromDate = form.getParamDate("fromDate");
        Date toDate = form.getParamDate("toDate");

        Map<Integer, List<Integer>> groupWithUsersMap = new LinkedHashMap<Integer, List<Integer>>();

        if (fromDate != null && toDate != null) {
            if (fromDate.compareTo(toDate) > 0) {
                throw new BGException("Дата начала позже даты конца");
            }

            List<Date> dateSet = new ArrayList<Date>();

            Date day = fromDate;
            while (day.compareTo(toDate) <= 0) {
                dateSet.add(day);
                day = TimeUtils.getNextDay(day);
            }

            form.getResponse().setData("dateSet", dateSet);

            form.getHttpRequest().setAttribute("prevDate", TimeUtils.getPrevDay(fromDate));

            //график, которой нужно строить выбран
            if (graphId > 0) {
                Callboard callboard = config.get(graphId);
                if (callboard.getCalendarId() > 0) {
                    WorkDaysCalendar calendar = setup.getConfig(CalendarConfig.class).getCalendar(callboard.getCalendarId());
                    Map<Date, Integer> excludeDates = new WorkTypeDAO(con).getWorkDaysCalendarExcludes(callboard.getCalendarId());

                    Map<Date, Pair<DayType, Boolean>> dateTypeMap = new HashMap<Date, Pair<DayType, Boolean>>();
                    form.getResponse().setData("dateTypeMap", dateTypeMap);

                    for (Date date : dateSet) {
                        dateTypeMap.put(date, calendar.getDayType(date, excludeDates));
                    }
                }

                groupWithUsersMap = getGroupWithUsersMap(con, callboard,
                        getGroupList(form, callboard, true, Utils.toIntegerSet(form.getPermission().get("allowOnlyGroups"))),
                        TimeUtils.convertDateToCalendar(fromDate), TimeUtils.convertDateToCalendar(toDate));

                form.getResponse().setData("callboard", callboard);

                log.debug("callboardGet2: " + (System.currentTimeMillis() - time) + " ms.");

                Map<Integer, Shift> allShiftMap = new ShiftDAO(con).getAllShiftMap();
                Map<Integer, Shift> avaiableShiftMap = new LinkedHashMap<Integer, Shift>();
                Set<Integer> availableCategoryIds = getAvailableCategoryIds(perm);

                for (Entry<Integer, Shift> entry : allShiftMap.entrySet()) {
                    if (availableCategoryIds.contains(entry.getValue().getCategory())) {
                        avaiableShiftMap.put(entry.getKey(), entry.getValue());
                    }
                }

                log.debug("callboardGet3: " + (System.currentTimeMillis() - time) + " ms.");

                form.getResponse().setData("shiftMap", allShiftMap);
                form.getResponse().setData("avaiableShiftMap", avaiableShiftMap);

                Map<Integer, List<WorkShift>> workShiftMap = new ShiftDAO(con).getWorkShift(callboard, fromDate, toDate, groupWithUsersMap);
                form.getResponse().setData("workShiftMap", workShiftMap);
                form.getResponse().setData("availableDays",
                        new ShiftDAO(con).getAvailableDateForShift(callboard, groupWithUsersMap, fromDate, toDate));

                log.debug("callboardGet4: " + (System.currentTimeMillis() - time) + " ms.");

                form.getResponse().setData("groupWithUsersMap", groupWithUsersMap);

                log.debug("callboardGet5: " + (System.currentTimeMillis() - time) + " ms.");

                form.getResponse().setData("workTypeList", getAvailableWorkTypeList(con, perm));

                log.debug("callboardGet6: " + (System.currentTimeMillis() - time) + " ms.");

                form.getResponse().setData("allowOnlyCategories", getAvailableCategories(perm));
            }

            log.debug("callboardGet: " + (System.currentTimeMillis() - time) + " ms.");
        }

        return processUserTypedForward(con, mapping, form, "callboardUpdate");
    }

    public ActionForward callboardGetTabel(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        //TODO: Добавить проверку прав, причём нормальную и также для получения графиков. С ограничением по ID табеля в пермишенах.

        int graphId = form.getParamInt("graphId", 0);

        //определние начальной и конечной даты, формирование сета с датами для шапки графика
        Date fromDate = form.getParamDate("fromDate");
        Date toDate = form.getParamDate("toDate");

        Callboard callboard = setup.getConfig(CallboardConfig.class).get(graphId);
        if (callboard == null) {
            throw new BGException("Not found callboard " + graphId);
        }

        HSSFWorkbook book = new TabelDAO(con).generateTabel(callboard, fromDate, toDate);

        var response = form.getHttpResponse();
        Utils.setFileNameHeades(response, "tabel_" + TimeUtils.format(fromDate, TimeUtils.FORMAT_TYPE_YMD) + "_"
                + TimeUtils.format(toDate, TimeUtils.FORMAT_TYPE_YMD) + ".xls");

        book.write(response.getOutputStream());

        return null;
    }

    public ActionForward callboardAvailableShift(ActionMapping mapping, DynActionForm form, HttpServletRequest request, HttpServletResponse response,
            Connection con) throws Exception {
        int categoryId = form.getParamInt("categoryId", 0);
        int graphId = form.getParamInt("graphId", 0);
        Set<Integer> shiftIds = Utils.toIntegerSet(form.getParam("shiftIds", ""));

        if ((categoryId > 0 || shiftIds.size() == 0) && !getAvailableCategoryIds(form.getPermission()).contains(categoryId)) {
            throw new BGException("У вас нет прав на просмотр шаблонов смен в этой категории");
        }

        form.getResponse().setData("workTypeMap", CallboardCache.getWorkTypeMap());
        form.getResponse().setData("shiftList",
                categoryId > 0 ? new ShiftDAO(con).getShiftList(categoryId) : new ShiftDAO(con).getShiftList(shiftIds));

        if (graphId > 0 && setup.subIndexed("callboard.").containsKey(graphId)) {
            form.getResponse().setData("minimalVersion", setup.subIndexed("callboard.").get(graphId).getInt("minimalVersion", 0));
        } else {
            form.getResponse().setData("minimalVersion", 0);
        }

        return processUserTypedForward(con, mapping, form, "availableShift");
    }

    private Calendar convertToCalendarAndTruncateTime(Date date) {
        Calendar calendar = null;

        if (date != null) {
            calendar = new GregorianCalendar();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }

        return calendar;
    }

    /**
     * Проверяет членство пользователя в группе на указанную дату
     * без учёта часов, минут, секунд и миллисекунд
     * @param userId ID пользователя
     * @param groupId ID группы
     * @param date Дата на которую проверяется членство пользователя в группе
     * @return true если пользователь числился в укзанной группе
     * на заданную дату, иначе - false
     */
    private boolean hasMembershipAtDate(int userId, int groupId, Date date) {
        boolean hasMembership = false;

        List<UserGroup> userGroupList = UserCache.getUserGroupList(userId);
        Iterator<UserGroup> iterator = userGroupList.iterator();

        while (iterator.hasNext() && !hasMembership) {
            UserGroup userGroup = iterator.next();

            if (userGroup.getGroupId() == groupId && TimeUtils.dateInRange(convertToCalendarAndTruncateTime(date),
                    convertToCalendarAndTruncateTime(userGroup.getDateFrom()), convertToCalendarAndTruncateTime(userGroup.getDateTo()))) {
                hasMembership = true;
            }
        }

        return hasMembership;
    }

    public ActionForward callboardUpdateFilters(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        Boolean hideEmptyGroups = form.getParamBoolean("hideEmptyGroups", false);
        Boolean hideEmptyShifts = form.getParamBoolean("hideEmptyShifts", false);
        int graphId = form.getParamInt("graphId", 0);

        CallboardConfig config = setup.getConfig(CallboardConfig.class);

        Callboard callboard = config.get(graphId);
        if (callboard == null) {
            throw new BGException("График не найден");
        }
        callboard.setHideEmptyGroups(hideEmptyGroups);
        callboard.setHideEmptyShifts(hideEmptyShifts);

        return processJsonForward(con, form);
    }

    public ActionForward callboardUpdateShift(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        int graphId = form.getParamInt("graphId", 0);
        int userId = form.getParamInt("userId", 0);
        // 0 - корневая группа графика
        int groupId = form.getParamInt("groupId", -1);
        int teamId = form.getParamInt("team", 0);
        int shiftId = form.getParamInt("shiftId", 0);

        //String workTypeTime = form.getParam( "workTypeTime" );
        ShiftDAO shiftDAO = new ShiftDAO(con);

        if (graphId == 0) {
            throw new BGException("Не указан номер графика");
        }

        if (userId == 0) {
            throw new BGException("Не указан пользователь");
        }

        if (groupId < 0) {
            throw new BGException("Не указана группа");
        }

        if (form.getParam("date", "").length() == 0) {
            throw new BGException("Не указана дата");
        }

        Set<Integer> allowedCallboards = Utils.toIntegerSet(form.getPermission().get("allowOnlyCallboards"));
        if (allowedCallboards.size() != 0 && !allowedCallboards.contains(graphId)) {
            throw new BGMessageException("Запрещено редактирование этого графика.");
        }

        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy");
        Date date = format.parse(form.getParam("date"));

        CallboardConfig config = setup.getConfig(CallboardConfig.class);

        Callboard callboard = config.get(graphId);
        if (callboard == null) {
            throw new BGException("График не найден");
        }

        //проверка, можно ли править график прошлым
        boolean allowed = callboard.getConfigMap().getBoolean("pastEditAllowed", true);
        if (!allowed && TimeUtils.dateBefore(date, new Date())) {
            throw new BGException("Запрещено править график за прошедшие дни");
        }

        //состоит ли пользователь в выбранной группе на выбранный день	
        /* FIXME: Открывались группы с дневным интервалом в ЦКиБС уфанета.*/

        if (!hasMembershipAtDate(userId, groupId > 0 ? groupId : callboard.getGroupId(), date)) {
            if (callboard.getConfigMap().getBoolean("autoAddGroup", false)) {
                UserDAO userDao = new UserDAO(con);

                UserGroup group = new UserGroup(groupId, date, date);

                Date prevDate = TimeUtils.getPrevDay(date);
                Date nextDate = TimeUtils.getNextDay(date);

                boolean existChanged = false;

                List<UserGroup> existGroups = UserCache.getUserGroupList(userId);
                for (UserGroup userGroup : existGroups) {
                    if (userGroup.getGroupId() != groupId) {
                        continue;
                    }

                    // существующая группа левая граница сдвиг влево
                    if (TimeUtils.dateEqual(userGroup.getDateFrom(), nextDate)) {
                        userDao.removeUserGroup(userId, groupId, userGroup.getDateFrom(), userGroup.getDateTo());
                        userGroup.setDateFrom(date);
                        userDao.addUserGroup(userId, userGroup);
                        existChanged = true;
                        break;
                    }
                    // существующая группа 
                    else if (TimeUtils.dateEqual(userGroup.getDateTo(), prevDate)) {
                        userDao.removeUserGroup(userId, groupId, userGroup.getDateFrom(), userGroup.getDateTo());
                        userGroup.setDateTo(date);
                        userDao.addUserGroup(userId, userGroup);
                        existChanged = true;
                        break;
                    }
                }

                if (!existChanged) {
                    new UserDAO(con).addUserGroup(userId, group);
                }

                UserCache.flush(con);
            } else {
                throw new BGException("Выбранный пользователь не состоит в заданной группе в этот день");
            }
        }

        shiftDAO.deleteWorkShift(graphId, groupId, userId, date);

        if (shiftId > 0) {
            Shift shift = shiftDAO.getShift(shiftId);

            WorkShift workShift = new WorkShift();
            workShift.setGraphId(graphId);
            workShift.setGroupId(groupId);
            workShift.setUserId(userId);
            workShift.setTeam(teamId);
            workShift.setDate(date);
            workShift.setWorkTypeTimeList(shift.getWorkTypeTimeList());
            workShift.setShiftId(shiftId);

            shiftDAO.updateWorkShift(workShift);

            form.getResponse().setData("minutes",
                    WorkTypeTime.getWorkMinutesInDay(shift.getWorkTypeTimeList(), date, form.getParamBoolean("lastDate", false) ? date : null));
        } else {
            form.getResponse().setData("minutes", 0);
        }

        return processJsonForward(con, form);
    }

    public ActionForward workDaysCalendarList(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        form.getResponse().setData("workDaysCalendarList", setup.getConfig(CalendarConfig.class).getCalendars());
        return processUserTypedForward(con, mapping, form, "workDaysCalendarList");
    }

    public ActionForward workDaysCalendarGet(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        int selectedYear = form.getParamInt("year", new GregorianCalendar().get(Calendar.YEAR));
        form.setParam("year", String.valueOf(selectedYear));

        DayTypeConfig dayTypesConfig = setup.getConfig(DayTypeConfig.class);

        if (dayTypesConfig.getTypes().size() == 0) {
            throw new BGException("Конфигурация не содержит описаний типов рабочих дней");
        }

        WorkDaysCalendar calendar = setup.getConfig(CalendarConfig.class).getCalendar(form.getId());
        if (calendar == null) {
            throw new BGException("Календарь с таким номером не найден в конфигурации");
        }

        Map<Date, Integer> excludeDates = new WorkTypeDAO(con).getWorkDaysCalendarExcludes(form.getId());

        Map<Date, Pair<DayType, Boolean>> dateTypeMap = new HashMap<Date, Pair<DayType, Boolean>>();
        form.getResponse().setData("dateTypeMap", dateTypeMap);

        Calendar dateFrom = new GregorianCalendar(selectedYear, Calendar.JANUARY, 1);
        Calendar dateTo = new GregorianCalendar(selectedYear, Calendar.DECEMBER, 31);

        while (TimeUtils.dateBeforeOrEq(dateFrom, dateTo)) {
            Date date = dateFrom.getTime();
            dateTypeMap.put(date, calendar.getDayType(date, excludeDates));

            dateFrom.add(Calendar.DAY_OF_YEAR, 1);
        }

        form.getResponse().setData("calendar", calendar);
        form.getResponse().setData("dayTypes", dayTypesConfig.getTypes());

        return processUserTypedForward(con, mapping, form, "workDaysCalendarGet");
    }

    public ActionForward workDaysCalendarUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        int calendarId = form.getParamInt("calendarId", 0);
        int type = form.getParamInt("type", 0);
        Date date = form.getParamDate("date");

        WorkDaysCalendar calendar = setup.getConfig(CalendarConfig.class).getCalendar(calendarId);
        if (calendar == null) {
            throw new BGException("Календарь с таким номером не найден в конфигурации");
        }

        if (date == null) {
            throw new BGException("Ошибка при обновлении календаря: дата не распознана");
        }

        // определение типа исключительно по правилу календаря
        Pair<DayType, Boolean> byRule = calendar.getDayType(date, null);
        // если такой же тип определён и правилом
        if (byRule != null && byRule.getFirst().getId() == type) {
            type = 0;
        }

        new WorkTypeDAO(con).updateWorkDaysCalendar(calendarId, type, date);

        return processJsonForward(con, form);
    }

    public ActionForward workDaysCalendarCopy(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        int calendarId = form.getParamInt("calendarId", 0);
        int from = form.getParamInt("from", 0);
        int to = form.getParamInt("to", 0);

        if (from == 0 || to == 0) {
            throw new BGException("Выберите обе даты");
        }

        if (from == to) {
            throw new BGException("Нельзя копировать самого в себя");
        }

        new WorkTypeDAO(con).copyWorkDaysCalendar(calendarId, from, to);

        return processJsonForward(con, form);
    }

    public ActionForward callboardChangeOrder(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        int graphId = form.getParamInt("graphId", 0);
        int groupId = form.getParamInt("groupId", 0);

        Map<Integer, Integer> userOrderMap = new HashMap<Integer, Integer>();

        for (String item : Utils.toSet(form.getParam("order", ""))) {
            if (item.contains(":")) {
                userOrderMap.put(Integer.parseInt(item.substring(0, item.indexOf(":"))), Integer.parseInt(item.substring(item.indexOf(":") + 1)));
            }
        }

        if (userOrderMap.size() > 0) {
            new ShiftDAO(con).updateShiftOrder(graphId, groupId, userOrderMap);
        }

        return processJsonForward(con, form);
    }

    // Список групп
    protected List<Integer> getGroupList(DynActionForm form, Callboard callboard, boolean excludeHidden, Set<Integer> allowOnlyGroups)
            throws BGException {
        List<Integer> result = new ArrayList<Integer>();

        Set<Integer> groupsFilter = Collections.emptySet();
        if (form != null) {
            groupsFilter = form.getSelectedValues("groupId");
        }

        Group parentGroup = UserCache.getUserGroup(callboard.getGroupId());
        if (parentGroup == null) {
            throw new BGException("Группа не найдена с кодом: " + callboard.getGroupId());
        }

        Set<Integer> groups = parentGroup.getChildSet();

        //поверять только среди тех групп, в которые входит пользователь
        for (Group group : UserCache.getUserGroupList()) {
            final int groupId = group.getId();

            if (!groups.contains(groupId) || (excludeHidden && group.getArchive() > 0) || (groupsFilter.size() > 0 && !groupsFilter.contains(groupId))
                    || (CollectionUtils.isNotEmpty(allowOnlyGroups) && !allowOnlyGroups.contains(groupId))) {
                continue;
            }
            result.add(groupId);
        }

        if (groupsFilter.size() == 0 && CollectionUtils.isEmpty(allowOnlyGroups)) {
            result.add(callboard.getGroupId());
        }

        return result;
    }

    //Группа - Список пользователей, входящих в эту группу
    protected Map<Integer, List<Integer>> getGroupWithUsersMap(Connection con, Callboard callboard, List<Integer> groupIds, Calendar dateFrom,
            Calendar dateTo) throws BGException {
        Map<Integer, List<Integer>> resultMap = new LinkedHashMap<Integer, List<Integer>>();

        Set<Integer> userInSubGroups = new HashSet<Integer>();

        //поверять только среди тех групп, в которые входит пользователь
        for (Integer groupId : groupIds) {
            if (groupId != callboard.getGroupId()) {
                List<Integer> userList = getGroupUsers(con, callboard, groupId, dateFrom, dateTo);
                resultMap.put(groupId, userList);
                userInSubGroups.addAll(userList);
            } else {
                // корень группы - под ключом 0
                List<Integer> userList = getGroupUsers(con, callboard, callboard.getGroupId(), dateFrom, dateTo);
                userList.removeAll(userInSubGroups);
                resultMap.put(0, userList);

                break;
            }
        }

        return resultMap;
    }

    private List<Integer> getGroupUsers(Connection con, Callboard callboard, int groupId, Calendar dateFrom, Calendar dateTo) throws BGException {
        List<Integer> userList = new ArrayList<Integer>();

        Map<Integer, Integer> shiftOrderMap = new ShiftDAO(con).getShiftOrder(callboard.getId(), groupId);
        for (User user : UserCache.getUserList()) {
            for (UserGroup userGroup : UserCache.getUserGroupList(user.getId())) {
                if (userGroup.getGroupId() == groupId
                        && TimeUtils.checkDateIntervalsIntersection(TimeUtils.convertDateToCalendar(userGroup.getDateFrom()),
                                TimeUtils.convertDateToCalendar(userGroup.getDateTo()), dateFrom, dateTo)) {
                    userList.add(user.getId());
                    break;
                }
            }
        }
        Collections.sort(userList, shiftOrderMap.size() > 0 ? new UserComparator(shiftOrderMap) : new UserComparator());

        return userList;
    }

    private List<Category> getAvailableCategories(ParameterMap perm) throws Exception {
        return setup.getConfig(CategoryConfig.class).getCategoryList(Utils.toIntegerSet(perm.get("allowOnlyCategories", "")));
    }

    protected Set<Integer> getAvailableCategoryIds(ParameterMap perm) throws Exception {
        return setup.getConfig(CategoryConfig.class).getCategoryIds(Utils.toIntegerSet(perm.get("allowOnlyCategories", "")));
    }

    private List<WorkType> getAvailableWorkTypeList(Connection con, ParameterMap perm) throws Exception {
        List<WorkType> resultList = new ArrayList<WorkType>();
        Set<Integer> availableCategoryIds = getAvailableCategoryIds(perm);

        for (WorkType workType : new WorkTypeDAO(con).getWorkTypeList()) {
            if (availableCategoryIds.contains(workType.getCategory())) {
                resultList.add(workType);
            }
        }

        return resultList;
    }

    /*
     * Сортирует пользователей в группе по ФИО если не задан порядок сортировки
     * Если порядок сортировки задан, сортирует согласно ему
     */
    private class UserComparator implements Comparator<Integer> {
        private Map<Integer, Integer> orderMap;

        public UserComparator() {
        }

        public UserComparator(Map<Integer, Integer> orderMap) {
            this.orderMap = orderMap;
        }

        @Override
        public int compare(Integer u1, Integer u2) {
            if (orderMap != null && orderMap.containsKey(u1) && orderMap.containsKey(u2)) {
                return orderMap.get(u1) - orderMap.get(u2);
            }

            return UserCache.getUser(u1).getTitle().compareTo(UserCache.getUser(u2).getTitle());
        }
    }

    public ActionForward userChangeGroup(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        int userId = form.getParamInt("userId", 0);
        int groupId = form.getParamInt("group", -1);
        int graphId = form.getParamInt("graphId", -1);
        Date dateFrom = form.getParamDate("fromDate");
        Date dateTo = form.getParamDate("toDate");

        CallboardConfig config = setup.getConfig(CallboardConfig.class);
        Callboard callboard = config.get(graphId);

        List<Integer> groups = WorkAction.class.getDeclaredConstructor().newInstance().getGroupList(form, callboard, false, null);
        List<UserGroup> userGroupList = UserCache.getUserGroupList(userId);

        List<UserGroup> result = new ArrayList<UserGroup>();

        for (Integer group : groups) {
            for (UserGroup userGroup : userGroupList) {
                if (userGroup.getGroupId() == group) {
                    result.add(userGroup);
                }
            }
        }

        if (result.size() > 1) {
            throw new BGException("Специалист числится в более одной группы, одну из которых надо закрыть и переназначить группу заново.");
        }

        Calendar cal = Calendar.getInstance();

        UserAction.class.getDeclaredConstructor().newInstance().addGroup(form, con, dateFrom, dateTo, groupId, userId);
        Integer oldGroupId = result.get(0).getGroupId();

        cal.setTime(dateFrom);
        cal.add(Calendar.DATE, -1);
        Date closeDate = cal.getTime();

        UserAction.class.getDeclaredConstructor().newInstance().closeGroup(form, con, userId, oldGroupId, closeDate, result.get(0).getDateFrom(),
                null);

        cal.setTime(dateTo);
        cal.add(Calendar.DATE, 1);
        dateFrom = cal.getTime();

        UserAction.class.getDeclaredConstructor().newInstance().addGroup(form, con, dateFrom, null, oldGroupId, userId);

        return processJsonForward(con, form);
    }
}
