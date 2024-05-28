package org.bgerp.plugin.pln.callboard.action;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGIllegalArgumentException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.cache.UserCache;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.plugin.pln.callboard.Plugin;
import org.bgerp.plugin.pln.callboard.cache.CallboardCache;
import org.bgerp.plugin.pln.callboard.dao.ShiftDAO;
import org.bgerp.plugin.pln.callboard.dao.TabelDAO;
import org.bgerp.plugin.pln.callboard.dao.WorkTaskDAO;
import org.bgerp.plugin.pln.callboard.dao.WorkTypeDAO;
import org.bgerp.plugin.pln.callboard.model.DayType;
import org.bgerp.plugin.pln.callboard.model.Shift;
import org.bgerp.plugin.pln.callboard.model.UserComparator;
import org.bgerp.plugin.pln.callboard.model.WorkDaysCalendar;
import org.bgerp.plugin.pln.callboard.model.WorkShift;
import org.bgerp.plugin.pln.callboard.model.WorkTask;
import org.bgerp.plugin.pln.callboard.model.WorkType;
import org.bgerp.plugin.pln.callboard.model.WorkTypeTime;
import org.bgerp.plugin.pln.callboard.model.config.CalendarConfig;
import org.bgerp.plugin.pln.callboard.model.config.CallboardConfig;
import org.bgerp.plugin.pln.callboard.model.config.CallboardConfig.Callboard;
import org.bgerp.plugin.pln.callboard.model.config.CategoryConfig;
import org.bgerp.plugin.pln.callboard.model.config.CategoryConfig.Category;
import org.bgerp.plugin.pln.callboard.model.config.DayTypeConfig;
import org.bgerp.plugin.pln.callboard.model.config.ProcessTimeSetConfig;
import org.bgerp.plugin.pln.callboard.model.work.CellRange;
import org.bgerp.plugin.pln.callboard.model.work.FreeSlotRange;
import org.bgerp.plugin.pln.callboard.model.work.ShiftData;
import org.bgerp.plugin.pln.callboard.model.work.SlotRange;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.event.ParamChangedEvent;
import ru.bgcrm.event.ParamChangingEvent;
import ru.bgcrm.event.client.ProcessChangedEvent;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessExecutor;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.StatusChange;
import ru.bgcrm.model.process.config.ProcessReferenceConfig;
import ru.bgcrm.model.user.Group;
import ru.bgcrm.model.user.User;
import ru.bgcrm.model.user.UserGroup;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.ProcessAction;
import ru.bgcrm.struts.action.admin.UserAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SingleConnectionSet;

@Action(path = "/user/plugin/callboard/work")
public class WorkAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER;

    private static final Object SET_TIME_MUTEX = new Object();

    public ActionForward planGet(DynActionForm form, Connection con) throws Exception {
        int graphId = form.getParamInt("graphId", 0);

        ConfigMap perm = form.getPermission();

        CallboardConfig config = setup.getConfig(CallboardConfig.class);

        form.setResponseData("callboardList", config.getCallboards(Utils.toIntegerSet(perm.get("allowOnlyCallboards"))));

        //определение начальной и конечной даты, формирование сета с датами для шапки графика
        Date date = form.getParamDate("date");

        form.setResponseData("date", date);

        // дата и график выбраны
        if (date != null && graphId > 0) {
            Callboard callboard = config.get(graphId);
            if (callboard.getCalendarId() > 0) {
                WorkDaysCalendar calendar = setup.getConfig(CalendarConfig.class).getCalendar(callboard.getCalendarId());
                Map<Date, Integer> excludeDates = new WorkTypeDAO(con).getWorkDaysCalendarExcludes(callboard.getCalendarId());

                // нужен ли он вообще тут?
                form.getHttpRequest().setAttribute("dayType", calendar.getDayType(date, excludeDates));
            }

            form.setResponseData("callboard", callboard);

            // подгруппы с пользователями в ними, под ключом 0 - не вошедшие ни в одну из подгрупп
            Map<Integer, List<Integer>> groupWithUsersMap = getGroupWithUsersMap(con, callboard,
                    getGroupList(form, callboard, true, Utils.toIntegerSet(form.getPermission().get("allowOnlyGroups"))),
                    TimeUtils.convertDateToCalendar(date), TimeUtils.convertDateToCalendar(date));

            // Возвращает мап Группа - List из Рабочих смен
            Map<Integer, List<WorkShift>> workShiftMap = new ShiftDAO(con).getWorkShift(callboard, TimeUtils.getNextDay(date), date,
                    groupWithUsersMap);

            // ключ - группа, значение - список бригад с пользователями
            Map<Integer, List<ShiftData>> groupDataMap = new LinkedHashMap<>();

            form.setResponseData("groupDataMap", groupDataMap);

            separateShiftData(date, workShiftMap, groupDataMap);

            new WorkTaskDAO(con).loadWorkTask(graphId, date, groupDataMap);

            form.setResponseData("workTypeMap", CallboardCache.getWorkTypeMap());
        }

        return html(con, form, PATH_JSP + "/plan.jsp");
    }

    public ActionForward processTime(DynActionForm form, Connection con) throws Exception {
        int processId = form.getParamInt("processId");

        if (processId <= 0) {
            throw new BGIllegalArgumentException();
        }

        final Process process = new ProcessDAO(con).getProcess(processId);
        ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
        if (type == null) {
            throw new BGException("Не найден тип процесса.");
        }

        ProcessTimeSetConfig timeSetConfig = type.getProperties().getConfigMap().getConfig(ProcessTimeSetConfig.class);
        if (timeSetConfig.getCallboard() != null) {
            WorkTask task = new WorkTaskDAO(con).getTaskByProcessId(processId);
            form.setResponseData("task", task);

            Date date = form.getParamDate("date");
            if (date == null) {
                date = TimeUtils.getNextDay(new Date());
                form.setParam("date", TimeUtils.format(date, TimeUtils.FORMAT_TYPE_YMD));
            }

            Callboard callboard = timeSetConfig.getCallboard();

            Calendar dateTo = TimeUtils.convertDateToCalendar(date);
            dateTo.add(Calendar.DAY_OF_YEAR, timeSetConfig.getDaysShow());

            // слоты с указанием бригад и т.п.
            List<FreeSlotRange> freeSlots = getFreeSlots(con, process, callboard, date, TimeUtils.convertCalendarToDate(dateTo));

            form.setResponseData("slotList", freeSlots);
        }

        return html(con, form, PATH_JSP + "/timeset.jsp");
    }

    public ActionForward processTimeSet(DynActionForm form, Connection con) throws Exception {
        int processId = form.getParamInt("processId");
        Date time = TimeUtils.parse(form.getParam("time"), TimeUtils.FORMAT_TYPE_YMDHM);
        Set<Integer> userIds = Utils.toIntegerSet(form.getParam("userIds"));

        if (processId <= 0) {
            throw new BGIllegalArgumentException();
        }

        ProcessDAO processDao = new ProcessDAO(con);

        final Process process = processDao.getProcess(processId);
        ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
        if (type == null) {
            throw new BGException("Не найден тип процесса.");
        }

        ProcessTimeSetConfig timeSetConfig = type.getProperties().getConfigMap().getConfig(ProcessTimeSetConfig.class);
        if (timeSetConfig.getCallboard() != null) {
            Callboard callboard = timeSetConfig.getCallboard();

            synchronized (SET_TIME_MUTEX) {
                // назначение времени
                if (time != null) {
                    Calendar cal = TimeUtils.convertDateToCalendar(time);
                    TimeUtils.clear_HOUR_MIN_MIL_SEC(cal);

                    Date date = TimeUtils.convertCalendarToDate(cal);

                    var slots = getFreeSlots(con, process, timeSetConfig.getCallboard(), date, date);

                    FreeSlotRange allowedSlot = slots.stream()
                        .filter(slot -> slot.getTime().equals(time))
                        .filter(slot -> slot.getShiftData().userIds.equals(userIds))
                        .findFirst().orElse(null);

                    if (allowedSlot == null) {
                        throw new BGMessageException("Выбранный слот занят.");
                    }

                    int userId = 0;
                    if (allowedSlot.shiftData.userIds.size() > 0) {
                        userId = Utils.getFirst(allowedSlot.shiftData.userIds);
                    }

                    WorkTask task = new WorkTask();
                    task.setGraphId(callboard.getId());
                    task.setProcessId(processId);
                    task.setDuration(allowedSlot.duration);
                    task.setSlotFrom(allowedSlot.slotFrom);
                    task.setGroupId(allowedSlot.groupId);
                    task.setTeam(allowedSlot.shiftData.team);
                    task.setUserId(allowedSlot.shiftData.team > 0 ? 0 : userId);
                    task.setTime(time);

                    // генерация описания процесса
                    ProcessReferenceConfig config = type.getProperties().getConfigMap().getConfig(ProcessReferenceConfig.class);
                    task.setReference(config.getReference(con, form, process, "callboard"));

                    new WorkTaskDAO(con).addTask(task);

                    // установка исполнителей
                    ProcessGroup processGroup = new ProcessGroup(allowedSlot.groupId > 0 ? allowedSlot.groupId : callboard.getGroupId());

                    Set<ProcessExecutor> currentExecutors = process.getExecutors();
                    ProcessExecutor.updateProcessExecutors(currentExecutors, processGroup, allowedSlot.shiftData.getUserIds());

                    process.setExecutors(currentExecutors);

                    processDao.updateProcessExecutors(currentExecutors, processId);

                    // установка параметра
                    EventProcessor.processEvent(new ParamChangingEvent(form, timeSetConfig.getParam(), processId, time), new SingleConnectionSet(con));
                    new ParamValueDAO(con).updateParamDateTime(processId, timeSetConfig.getParam().getId(), time);
                    EventProcessor.processEvent(new ParamChangedEvent(form, timeSetConfig.getParam(), processId, time), new SingleConnectionSet(con));

                    // установка статуса
                    final int changeStatusToId = timeSetConfig.getChangeStatusToId();
                    if (changeStatusToId > 0 && changeStatusToId != process.getStatusId()) {
                        StatusChange change = new StatusChange();
                        change.setProcessId(process.getId());
                        change.setStatusId(changeStatusToId);
                        change.setDate(new Date());
                        change.setComment("При установки времени");

                        ProcessAction.processStatusUpdate(form, con, process, change);
                    }

                    form.getResponse().addEvent(new ProcessChangedEvent(process.getId()));
                }
                // удаление времени
                else {
                    new ParamValueDAO(con).updateParamDateTime(processId, timeSetConfig.getParam().getId(), null);

                    new WorkTaskDAO(con).removeTaskForProcess(processId);

                    form.getResponse().addEvent(new ProcessChangedEvent(process.getId()));
                }
            }
        }

        return json(con, form);
    }

    public ActionForward processTimeLock(DynActionForm form, Connection con) throws BGMessageException {
        return processTimeLockAction(form, con, true);
    }

    public ActionForward processTimeUnlock(DynActionForm form, Connection con) throws BGMessageException {
        return processTimeLockAction(form, con, false);
    }

    private ActionForward processTimeLockAction(DynActionForm form, Connection con, boolean lock) throws BGMessageException {
        int graphId = form.getParamInt("graphId");
        Date date = form.getParamDate("date");

        int groupId = form.getParamInt("groupId");
        int team = form.getParamInt("team");
        int userId = form.getParamInt("userId");

        int dayMinuteFrom = form.getParamInt("dayMinuteFrom");

        if (graphId <= 0 || date == null) {
            throw new BGIllegalArgumentException();
        }

        Callboard callboard = Setup.getSetup().getConfig(CallboardConfig.class).get(graphId);

        Calendar cal = TimeUtils.convertDateToCalendar(date);
        cal.add(Calendar.MINUTE, dayMinuteFrom);

        Date time = TimeUtils.convertCalendarToDate(cal);

        Pair<CellRange, SlotRange> result = null;

        synchronized (SET_TIME_MUTEX) {
            // пользователи группы
            Map<Integer, List<Integer>> groupWithUsersMap = getGroupWithUsersMap(con, callboard, Collections.singletonList(groupId), cal, cal);

            // Возвращает мап Группа - List из Рабочих смен
            Map<Integer, List<WorkShift>> workShiftMap = new ShiftDAO(con).getWorkShift(callboard, TimeUtils.getNextDay(date), date,
                    groupWithUsersMap);

            // ключ - группа, значение - список бригад с пользователями
            Map<Integer, List<ShiftData>> groupDataMap = new LinkedHashMap<>();

            separateShiftData(date, workShiftMap, groupDataMap);

            // назначенные задачи
            new WorkTaskDAO(con).loadWorkTask(callboard.getId(), date, groupDataMap);

            List<ShiftData> dataList = Utils.getFirst(groupDataMap.values());
            if (dataList != null) {
                MAIN_LOOP: for (ShiftData data : dataList) {
                    if ((team > 0 && data.team != team) || !(data.userIds.contains(userId))) {
                        continue;
                    }

                    for (CellRange range : data.getCellRanges(callboard.getPlanConfig())) {
                        for (SlotRange slotRange : range.getSlotRanges()) {
                            if (range.getDayMinuteFrom(slotRange) == dayMinuteFrom) {
                                result = new Pair<>(range, slotRange);
                                break MAIN_LOOP;
                            }
                        }
                    }
                }
            }
        }

        if (lock) {
            if (result != null) {
                if (result.getSecond().task != null && result.getSecond().task.getProcessId() > 0) {
                    throw new BGMessageException("Слот занят!");
                }

                WorkTask task = new WorkTask();
                task.setGraphId(callboard.getId());
                task.setProcessId(WorkTask.PROCESS_ID_LOCK);
                task.setDuration(result.getFirst().workType.getTimeSetStep());
                task.setSlotFrom(result.getSecond().slotFrom);
                task.setGroupId(groupId);
                task.setTeam(team);
                task.setUserId(userId);
                task.setTime(time);
                task.setReference("");

                new WorkTaskDAO(con).addTask(task);
            } else {
                throw new BGException("Слот занят или не найден!");
            }
        } else {
            if (result != null) {
                if (result.getSecond().task != null) {
                    if (!result.getSecond().task.isLock()) {
                        throw new BGMessageException("Слот занят!");
                    }

                    WorkTask task = new WorkTask();
                    task.setGraphId(callboard.getId());
                    task.setGroupId(groupId);
                    task.setTeam(team);
                    task.setUserId(userId);
                    task.setTime(time);

                    new WorkTaskDAO(con).removeTask(task);
                }
            } else {
                throw new BGException("Слот занят или не найден!");
            }
        }

        return json(con, form);
    }

    private List<FreeSlotRange> getFreeSlots(Connection con, Process process, Callboard callboard, Date dateFrom, Date dateTo) {
        List<FreeSlotRange> result = new ArrayList<>();

        Set<Integer> processGroupIds = ProcessGroup.getGroupsWithRole(process.getGroups(), 0);
        Set<Integer> groupIds = new HashSet<>(getGroupList(null, callboard, false, null));

        @SuppressWarnings("unchecked")
        Integer groupId = (Integer) Utils.getFirst(CollectionUtils.intersection(processGroupIds, groupIds));

        if (groupId != null) {
            // пользователи группы
            Map<Integer, List<Integer>> groupWithUsersMap = getGroupWithUsersMap(con, callboard, Collections.singletonList(groupId),
                    TimeUtils.convertDateToCalendar(dateFrom), TimeUtils.convertDateToCalendar(dateTo));

            log.debug("groupId: {}; groupWithUsersMap: {}", groupId, groupWithUsersMap);

            // Возвращает мап Группа - List из Рабочих смен
            Map<Integer, List<WorkShift>> workShiftMap = new ShiftDAO(con).getWorkShift(callboard, TimeUtils.getNextDay(dateFrom), dateTo,
                    groupWithUsersMap);

            log.debug("workShiftMap: {}", workShiftMap);

            Date date = (Date) dateFrom.clone();

            while (!date.after(dateTo)) {
                // ключ - группа, значение - список бригад с пользователями
                Map<Integer, List<ShiftData>> groupDataMap = new LinkedHashMap<>();

                separateShiftData(date, workShiftMap, groupDataMap);

                log.debug("Processing: {}; groupDataMap: {}", date, groupDataMap);

                // назначенные задачи
                new WorkTaskDAO(con).loadWorkTask(callboard.getId(), date, groupDataMap);

                List<ShiftData> dataList = Utils.getFirst(groupDataMap.values());
                if (dataList != null) {
                    for (ShiftData data : dataList) {
                        for (CellRange range : data.getCellRanges(callboard.getPlanConfig())) {
                            if (range.workTypeTime == null) {
                                continue;
                            }

                            WorkType type = range.workType;
                            if (type == null) {
                                continue;
                            }

                            final int slotSize = type.getTimeSetStep();

                            // неположительная длительность - значит не принимает такие процессы
                            int time = type.getProcessExecuteTime(con, data, process);
                            if (time <= 0) {
                                continue;
                            }

                            List<int[]> freeSlotRanges = range.getFreeSlotRanges();

                            if (log.isDebugEnabled()) {
                                log.debug("Date: {}; Shift team: {}; userIds: {}; shiftId: {}; time: {}; slotSize: ",
                                        TimeUtils.format(date, TimeUtils.FORMAT_TYPE_YMD), data.team, data.userIds,
                                        data.shiftId, time, slotSize);

                                StringBuilder ranges = new StringBuilder();
                                for (int[] r : freeSlotRanges) {
                                    Utils.addSeparated(ranges, ", ", r[0] + "-" + r[1]);
                                }

                                log.debug("Checking free ranges: {}", ranges);
                            }

                            for (int[] freeRange : freeSlotRanges) {
                                for (int rangeStart = freeRange[0]; rangeStart < freeRange[1]; rangeStart++) {
                                    if ((freeRange[1] - rangeStart + 1) * slotSize >= time) {
                                        result.add(new FreeSlotRange(date, groupId, data, range, rangeStart, time));

                                        log.debug("FreeSlotRange: {}", rangeStart);
                                    }
                                }
                            }
                        }
                    }
                }

                date = TimeUtils.getNextDay(date);
            }
        }

        return result;
    }

    // разделение смен по пользователям в группах
    private void separateShiftData(Date date, Map<Integer, List<WorkShift>> workShiftMap, Map<Integer, List<ShiftData>> groupDataMap) {
        // раскладываем по группам
        for (Map.Entry<Integer, List<WorkShift>> me : workShiftMap.entrySet()) {
            int groupId = me.getKey();

            List<ShiftData> groupData = groupDataMap.get(groupId);
            if (groupData == null) {
                groupData = new ArrayList<>();
                groupDataMap.put(groupId, groupData);
            }

            for (WorkShift shift : me.getValue()) {
                if (!date.equals(shift.getDate())) {
                    continue;
                }

                final int team = shift.getTeam();
                final int userId = shift.getUserId();
                final int shiftId = shift.getShiftId();

                ShiftData existData = null;

                // попытка группировки смены по бригаде
                for (ShiftData data : groupData) {
                    // такая же смена и бригада - добавляем в неё пользователя
                    if (team > 0 && shiftId == data.shiftId && team == data.team) {
                        existData = data;
                        existData.userIds.add(userId);
                        break;
                    }
                }

                if (existData == null) {
                    existData = new ShiftData();
                    existData.team = team;
                    existData.shiftId = shiftId;
                    existData.userIds = new HashSet<>(Arrays.asList(new Integer[]{userId}));
                    existData.workTypeTimeList = shift.getWorkTypeTimeList();

                    groupData.add(existData);
                }
            }
        }
    }

    public ActionForward callboardGet(DynActionForm form, Connection con) throws Exception {
        long time = System.currentTimeMillis();

        int graphId = form.getParamInt("graphId", 0);

        ConfigMap perm = form.getPermission();

        CallboardConfig config = setup.getConfig(CallboardConfig.class);

        log.debug("callboardGet1: " + (System.currentTimeMillis() - time) + " ms.");

        form.setResponseData("callboardList",
                config.getCallboards(Utils.toIntegerSet(perm.get("allowOnlyCallboards", perm.get("allowOnlyTabels")))));

        //определние начальной и конечной даты, формирование сета с датами для шапки графика
        Date fromDate = form.getParamDate("fromDate");
        Date toDate = form.getParamDate("toDate");

        Map<Integer, List<Integer>> groupWithUsersMap = new LinkedHashMap<>();

        if (fromDate != null && toDate != null) {
            if (fromDate.compareTo(toDate) > 0) {
                throw new BGException("Дата начала позже даты конца");
            }

            List<Date> dateSet = new ArrayList<>();

            Date day = fromDate;
            while (day.compareTo(toDate) <= 0) {
                dateSet.add(day);
                day = TimeUtils.getNextDay(day);
            }

            form.setResponseData("dateSet", dateSet);

            form.getHttpRequest().setAttribute("prevDate", TimeUtils.getPrevDay(fromDate));

            //график, которой нужно строить выбран
            if (graphId > 0) {
                Callboard callboard = config.get(graphId);
                if (callboard.getCalendarId() > 0) {
                    WorkDaysCalendar calendar = setup.getConfig(CalendarConfig.class).getCalendar(callboard.getCalendarId());
                    Map<Date, Integer> excludeDates = new WorkTypeDAO(con).getWorkDaysCalendarExcludes(callboard.getCalendarId());

                    Map<Date, Pair<DayType, Boolean>> dateTypeMap = new HashMap<>();
                    form.setResponseData("dateTypeMap", dateTypeMap);

                    for (Date date : dateSet) {
                        dateTypeMap.put(date, calendar.getDayType(date, excludeDates));
                    }
                }

                groupWithUsersMap = getGroupWithUsersMap(con, callboard,
                        getGroupList(form, callboard, true, Utils.toIntegerSet(form.getPermission().get("allowOnlyGroups"))),
                        TimeUtils.convertDateToCalendar(fromDate), TimeUtils.convertDateToCalendar(toDate));

                form.setResponseData("callboard", callboard);

                log.debug("callboardGet2: " + (System.currentTimeMillis() - time) + " ms.");

                Map<Integer, Shift> allShiftMap = new ShiftDAO(con).getAllShiftMap();
                Map<Integer, Shift> avaiableShiftMap = new LinkedHashMap<>();
                Set<Integer> availableCategoryIds = getAvailableCategoryIds(perm);

                for (Entry<Integer, Shift> entry : allShiftMap.entrySet()) {
                    if (availableCategoryIds.contains(entry.getValue().getCategory())) {
                        avaiableShiftMap.put(entry.getKey(), entry.getValue());
                    }
                }

                log.debug("callboardGet3: " + (System.currentTimeMillis() - time) + " ms.");

                form.setResponseData("shiftMap", allShiftMap);
                form.setResponseData("avaiableShiftMap", avaiableShiftMap);

                Map<Integer, List<WorkShift>> workShiftMap = new ShiftDAO(con).getWorkShift(callboard, fromDate, toDate, groupWithUsersMap);
                form.setResponseData("workShiftMap", workShiftMap);
                form.setResponseData("availableDays",
                        new ShiftDAO(con).getAvailableDateForShift(callboard, groupWithUsersMap, fromDate, toDate));

                log.debug("callboardGet4: " + (System.currentTimeMillis() - time) + " ms.");

                form.setResponseData("groupWithUsersMap", groupWithUsersMap);

                log.debug("callboardGet5: " + (System.currentTimeMillis() - time) + " ms.");

                form.setResponseData("workTypeList", getAvailableWorkTypeList(con, perm));

                log.debug("callboardGet6: " + (System.currentTimeMillis() - time) + " ms.");

                form.setResponseData("allowOnlyCategories", getAvailableCategories(perm));
            }

            log.debug("callboardGet: " + (System.currentTimeMillis() - time) + " ms.");
        }

        return html(con, form, PATH_JSP + "/callboard/update.jsp");
    }

    protected List<Category> getAvailableCategories(ConfigMap perm) throws Exception {
        return setup.getConfig(CategoryConfig.class).getCategoryList(Utils.toIntegerSet(perm.get("allowOnlyCategories", "")));
    }

    protected Set<Integer> getAvailableCategoryIds(ConfigMap perm) throws Exception {
        return setup.getConfig(CategoryConfig.class).getCategoryIds(Utils.toIntegerSet(perm.get("allowOnlyCategories", "")));
    }

    private List<WorkType> getAvailableWorkTypeList(Connection con, ConfigMap perm) throws Exception {
        List<WorkType> resultList = new ArrayList<>();
        Set<Integer> availableCategoryIds = getAvailableCategoryIds(perm);

        for (WorkType workType : new WorkTypeDAO(con).getWorkTypeList()) {
            if (availableCategoryIds.contains(workType.getCategory())) {
                resultList.add(workType);
            }
        }

        return resultList;
    }

    public ActionForward callboardUpdateFilters(DynActionForm form, Connection con) throws Exception {
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

        return json(con, form);
    }

    public ActionForward callboardUpdateShift(DynActionForm form, Connection con) throws Exception {
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

            form.setResponseData("minutes",
                    WorkTypeTime.getWorkMinutesInDay(shift.getWorkTypeTimeList(), date, form.getParamBoolean("lastDate", false) ? date : null));
        } else {
            form.setResponseData("minutes", 0);
        }

        return json(con, form);
    }

    public ActionForward userChangeGroup(DynActionForm form, Connection con) throws Exception {
        int userId = form.getParamInt("userId", 0);
        int groupId = form.getParamInt("group", -1);
        int graphId = form.getParamInt("graphId", -1);
        Date dateFrom = form.getParamDate("fromDate");
        Date dateTo = form.getParamDate("toDate");

        CallboardConfig config = setup.getConfig(CallboardConfig.class);
        Callboard callboard = config.get(graphId);

        List<Integer> groups = WorkAction.class.getDeclaredConstructor().newInstance().getGroupList(form, callboard, false, null);
        List<UserGroup> userGroupList = UserCache.getUserGroupList(userId);

        List<UserGroup> result = new ArrayList<>();

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

        return json(con, form);
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

    public ActionForward callboardChangeOrder(DynActionForm form, Connection con) throws Exception {
        int graphId = form.getParamInt("graphId", 0);
        int groupId = form.getParamInt("groupId", 0);

        Map<Integer, Integer> userOrderMap = new HashMap<>();

        for (String item : Utils.toSet(form.getParam("order", ""))) {
            if (item.contains(":")) {
                userOrderMap.put(Integer.parseInt(item.substring(0, item.indexOf(":"))), Integer.parseInt(item.substring(item.indexOf(":") + 1)));
            }
        }

        if (userOrderMap.size() > 0) {
            new ShiftDAO(con).updateShiftOrder(graphId, groupId, userOrderMap);
        }

        return json(con, form);
    }

    public ActionForward callboardGetTabel(DynActionForm form, Connection con) throws Exception {
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
        Utils.setFileNameHeaders(response, "tabel_" + TimeUtils.format(fromDate, TimeUtils.FORMAT_TYPE_YMD) + "_"
                + TimeUtils.format(toDate, TimeUtils.FORMAT_TYPE_YMD) + ".xls");

        book.write(response.getOutputStream());

        return null;
    }

    public ActionForward callboardAvailableShift(DynActionForm form, Connection con) throws Exception {
        int categoryId = form.getParamInt("categoryId", 0);
        int graphId = form.getParamInt("graphId", 0);
        Set<Integer> shiftIds = Utils.toIntegerSet(form.getParam("shiftIds", ""));

        if ((categoryId > 0 || shiftIds.size() == 0) && !getAvailableCategoryIds(form.getPermission()).contains(categoryId)) {
            throw new BGException("У вас нет прав на просмотр шаблонов смен в этой категории");
        }

        form.setResponseData("workTypeMap", CallboardCache.getWorkTypeMap());
        form.setResponseData("shiftList",
                categoryId > 0 ? new ShiftDAO(con).getShiftList(categoryId) : new ShiftDAO(con).getShiftList(shiftIds));

        if (graphId > 0 && setup.subIndexed("callboard.").containsKey(graphId)) {
            form.setResponseData("minimalVersion", setup.subIndexed("callboard.").get(graphId).getInt("minimalVersion", 0));
        } else {
            form.setResponseData("minimalVersion", 0);
        }

        return html(con, form, PATH_JSP + "/callboard/available_shift.jsp");
    }


    public ActionForward workDaysCalendarList(DynActionForm form, Connection con) throws Exception {
        form.setResponseData("workDaysCalendarList", setup.getConfig(CalendarConfig.class).getCalendars());
        return html(con, form, PATH_JSP + "/calendar/list.jsp");
    }

    public ActionForward workDaysCalendarGet(DynActionForm form, Connection con) throws Exception {
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

        Map<Date, Pair<DayType, Boolean>> dateTypeMap = new HashMap<>();
        form.setResponseData("dateTypeMap", dateTypeMap);

        Calendar dateFrom = new GregorianCalendar(selectedYear, Calendar.JANUARY, 1);
        Calendar dateTo = new GregorianCalendar(selectedYear, Calendar.DECEMBER, 31);

        while (TimeUtils.dateBeforeOrEq(dateFrom, dateTo)) {
            Date date = dateFrom.getTime();
            dateTypeMap.put(date, calendar.getDayType(date, excludeDates));

            dateFrom.add(Calendar.DAY_OF_YEAR, 1);
        }

        form.setResponseData("calendar", calendar);
        form.setResponseData("dayTypes", dayTypesConfig.getTypes());

        return html(con, form, PATH_JSP + "/calendar/update.jsp");
    }

    public ActionForward workDaysCalendarUpdate(DynActionForm form, Connection con) throws Exception {
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

        return json(con, form);
    }

    public ActionForward workDaysCalendarCopy(DynActionForm form, Connection con) throws Exception {
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

        return json(con, form);
    }


    // Список групп
    protected List<Integer> getGroupList(DynActionForm form, Callboard callboard, boolean excludeHidden, Set<Integer> allowOnlyGroups)
            {
        List<Integer> result = new ArrayList<>();

        Set<Integer> groupsFilter = Collections.emptySet();
        if (form != null) {
            groupsFilter = form.getParamValues("groupId");
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
            Calendar dateTo) {
        Map<Integer, List<Integer>> resultMap = new LinkedHashMap<>();

        Set<Integer> userInSubGroups = new HashSet<>();

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

    private List<Integer> getGroupUsers(Connection con, Callboard callboard, int groupId, Calendar dateFrom, Calendar dateTo) {
        List<Integer> userList = new ArrayList<>();

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

}
