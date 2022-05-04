package org.bgerp.plugin.pln.callboard.action;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts.action.ActionForward;
import org.bgerp.plugin.pln.callboard.Plugin;
import org.bgerp.plugin.pln.callboard.cache.CallboardCache;
import org.bgerp.plugin.pln.callboard.dao.ShiftDAO;
import org.bgerp.plugin.pln.callboard.dao.WorkTaskDAO;
import org.bgerp.plugin.pln.callboard.dao.WorkTypeDAO;
import org.bgerp.plugin.pln.callboard.model.WorkDaysCalendar;
import org.bgerp.plugin.pln.callboard.model.WorkShift;
import org.bgerp.plugin.pln.callboard.model.WorkTask;
import org.bgerp.plugin.pln.callboard.model.WorkType;
import org.bgerp.plugin.pln.callboard.model.config.CalendarConfig;
import org.bgerp.plugin.pln.callboard.model.config.CallboardConfig;
import org.bgerp.plugin.pln.callboard.model.config.CallboardConfig.Callboard;
import org.bgerp.plugin.pln.callboard.model.config.ProcessTimeSetConfig;
import org.bgerp.plugin.pln.callboard.model.work.CellRange;
import org.bgerp.plugin.pln.callboard.model.work.FreeSlotRange;
import org.bgerp.plugin.pln.callboard.model.work.ShiftData;
import org.bgerp.plugin.pln.callboard.model.work.SlotRange;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.ParamChangedEvent;
import ru.bgcrm.event.ParamChangingEvent;
import ru.bgcrm.event.client.ProcessChangedEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessExecutor;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.StatusChange;
import ru.bgcrm.model.process.config.ProcessReferenceConfig;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.ProcessAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SingleConnectionSet;

@Action(path = "/user/plugin/callboard/work")
public class WorkAction extends org.bgerp.plugin.pln.callboard.action.admin.WorkAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER;

    public ActionForward planGet(DynActionForm form, Connection con) throws Exception {
        int graphId = form.getParamInt("graphId", 0);

        ParameterMap perm = form.getPermission();

        CallboardConfig config = setup.getConfig(CallboardConfig.class);

        form.getResponse().setData("callboardList", config.getCallboards(Utils.toIntegerSet(perm.get("allowOnlyCallboards"))));

        //определение начальной и конечной даты, формирование сета с датами для шапки графика
        Date date = form.getParamDate("date");

        form.getResponse().setData("date", date);

        // дата и график выбраны
        if (date != null && graphId > 0) {
            Callboard callboard = config.get(graphId);
            if (callboard.getCalendarId() > 0) {
                WorkDaysCalendar calendar = setup.getConfig(CalendarConfig.class).getCalendar(callboard.getCalendarId());
                Map<Date, Integer> excludeDates = new WorkTypeDAO(con).getWorkDaysCalendarExcludes(callboard.getCalendarId());

                // нужен ли он вообще тут?
                form.getHttpRequest().setAttribute("dayType", calendar.getDayType(date, excludeDates));
            }

            form.getResponse().setData("callboard", callboard);

            // подгруппы с пользователями в ними, под ключом 0 - не вошедшие ни в одну из подгрупп
            Map<Integer, List<Integer>> groupWithUsersMap = getGroupWithUsersMap(con, callboard,
                    getGroupList(form, callboard, true, Utils.toIntegerSet(form.getPermission().get("allowOnlyGroups"))),
                    TimeUtils.convertDateToCalendar(date), TimeUtils.convertDateToCalendar(date));

            // Возвращает мап Группа - List из Рабочих смен
            Map<Integer, List<WorkShift>> workShiftMap = new ShiftDAO(con).getWorkShift(callboard, TimeUtils.getNextDay(date), date,
                    groupWithUsersMap);

            // ключ - группа, значение - список бригад с пользователями
            Map<Integer, List<ShiftData>> groupDataMap = new LinkedHashMap<Integer, List<ShiftData>>();

            form.getResponse().setData("groupDataMap", groupDataMap);

            separateShiftData(date, workShiftMap, groupDataMap);

            new WorkTaskDAO(con).loadWorkTask(graphId, date, groupDataMap);

            form.getResponse().setData("workTypeMap", CallboardCache.getWorkTypeMap());
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

    private static final Object SET_TIME_MUTEX = new Object();

    public ActionForward processTimeSet(DynActionForm form, Connection con) throws Exception {
        int processId = form.getParamInt("processId");
        Date time = TimeUtils.parse(form.getParam("time"), TimeUtils.FORMAT_TYPE_YMDHM);

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

            FreeSlotRange allowedSlot = null;

            synchronized (SET_TIME_MUTEX) {
                // назначение времени
                if (time != null) {
                    Calendar cal = TimeUtils.convertDateToCalendar(time);
                    TimeUtils.clear_HOUR_MIN_MIL_SEC(cal);

                    Date date = TimeUtils.convertCalendarToDate(cal);

                    for (FreeSlotRange slot : getFreeSlots(con, process, timeSetConfig.getCallboard(), date, date)) {
                        if (slot.getTime().equals(time)) {
                            allowedSlot = slot;
                            break;
                        }
                    }

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

    public ActionForward processTimeLock(DynActionForm form, Connection con) throws BGException {

        return processTimeLockAction(form, con, true);
    }

    public ActionForward processTimeUnlock(DynActionForm form, Connection con) throws BGException {
        return processTimeLockAction(form, con, false);
    }

    private ActionForward processTimeLockAction(DynActionForm form, Connection con, boolean lock) throws BGException {
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
            Map<Integer, List<ShiftData>> groupDataMap = new LinkedHashMap<Integer, List<ShiftData>>();

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
                                result = new Pair<CellRange, SlotRange>(range, slotRange);
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

    private List<FreeSlotRange> getFreeSlots(Connection con, Process process, Callboard callboard, Date dateFrom, Date dateTo) throws BGException {
        List<FreeSlotRange> result = new ArrayList<FreeSlotRange>();

        Set<Integer> processGroupIds = ProcessGroup.getGroupsWithRole(process.getGroups(), 0);
        Set<Integer> groupIds = new HashSet<Integer>(getGroupList(null, callboard, false, null));

        @SuppressWarnings("unchecked")
        Integer groupId = (Integer) Utils.getFirst(CollectionUtils.intersection(processGroupIds, groupIds));

        if (groupId != null) {
            // пользователи группы
            Map<Integer, List<Integer>> groupWithUsersMap = getGroupWithUsersMap(con, callboard, Collections.singletonList(groupId),
                    TimeUtils.convertDateToCalendar(dateFrom), TimeUtils.convertDateToCalendar(dateTo));
            if (log.isDebugEnabled()) {
                log.debug("groupId: " + groupId + ";groupWithUsersMap: " + groupWithUsersMap);
            }

            // Возвращает мап Группа - List из Рабочих смен
            Map<Integer, List<WorkShift>> workShiftMap = new ShiftDAO(con).getWorkShift(callboard, TimeUtils.getNextDay(dateFrom), dateTo,
                    groupWithUsersMap);

            if (log.isDebugEnabled()) {
                log.debug("workShiftMap: " + workShiftMap);
            }

            Date date = (Date) dateFrom.clone();

            while (!date.after(dateTo)) {
                // ключ - группа, значение - список бригад с пользователями
                Map<Integer, List<ShiftData>> groupDataMap = new LinkedHashMap<Integer, List<ShiftData>>();

                separateShiftData(date, workShiftMap, groupDataMap);

                if (log.isDebugEnabled()) {
                    log.debug("Processing: " + date + "; groupDataMap: " + groupDataMap);
                }

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
                groupData = new ArrayList<ShiftData>();
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
                    existData.userIds = new HashSet<Integer>(Arrays.asList(new Integer[] { userId }));
                    existData.workTypeTimeList = shift.getWorkTypeTimeList();

                    groupData.add(existData);
                }
            }
        }
    }
}
