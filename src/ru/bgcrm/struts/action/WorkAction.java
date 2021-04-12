package ru.bgcrm.struts.action;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.cache.CallboardCache;
import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.work.ShiftDAO;
import ru.bgcrm.dao.work.WorkTaskDAO;
import ru.bgcrm.dao.work.WorkTypeDAO;
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
import ru.bgcrm.model.work.WorkDaysCalendar;
import ru.bgcrm.model.work.WorkShift;
import ru.bgcrm.model.work.WorkTask;
import ru.bgcrm.model.work.WorkType;
import ru.bgcrm.model.work.WorkTypeConfig;
import ru.bgcrm.model.work.WorkTypeTime;
import ru.bgcrm.model.work.config.CalendarConfig;
import ru.bgcrm.model.work.config.CallboardConfig;
import ru.bgcrm.model.work.config.CallboardConfig.Callboard;
import ru.bgcrm.model.work.config.CallboardPlanConfig;
import ru.bgcrm.model.work.config.ProcessTimeSetConfig;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class WorkAction extends ru.bgcrm.struts.action.admin.WorkAction {
    // дневная смена в какой-то группе
    public class ShiftData {
        private int team;
        private int shiftId;
        private Set<Integer> userIds;
        private List<WorkTypeTime> workTypeTimeList;
        private List<WorkTask> taskList;

        public int getTeam() {
            return team;
        }

        public int getShiftId() {
            return shiftId;
        }

        public Set<Integer> getUserIds() {
            return userIds;
        }

        public List<WorkTypeTime> getWorkTypeTimeList() {
            return workTypeTimeList;
        }

        public List<WorkTask> getTaskList() {
            return taskList;
        }

        public void addTask(WorkTask task) {
            if (taskList == null) {
                taskList = new ArrayList<WorkTask>();
            }
            taskList.add(task);
        }

        public List<CellRange> getCellRanges(CallboardPlanConfig planConfig) {
            // для каждого wtt указано сколько слотов он занимает, если не указано 
            List<CellRange> result = new ArrayList<CellRange>();

            CellRange currentRange = null;

            Iterator<WorkTypeTime> iterator = workTypeTimeList.iterator();

            // WorkTypeTime приближающийся либо текущий (ПТ)
            WorkTypeTime currentWtt = null;

            final int timeTo = planConfig.getDayMinuteTo();

            for (int currentTime = planConfig.getDayMinuteFrom(); currentTime < timeTo; currentTime += planConfig.getDayMinuteStep()) {
                // нет ПТ вида работ диапазона либо он завершился
                if (currentWtt == null || currentWtt.getDayMinuteTo() <= currentTime) {
                    currentWtt = iterator.hasNext() ? iterator.next() : null;
                }

                // нет ПТ вида работ либо не добрались до вида работ 
                if (currentWtt == null || currentTime < currentWtt.getDayMinuteFrom()) {
                    if (currentRange == null || currentRange.workTypeTime != null) {
                        result.add(currentRange = new CellRange(null, null, 1));
                    } else {
                        currentRange.cells++;
                    }
                }
                // нет текущего диапазона, либо он не соответствует текущему виду работ
                else if (currentRange == null || currentRange.workTypeTime != currentWtt) {
                    WorkType workType = null;
                    if (currentWtt != null) {
                        workType = CallboardCache.getWorkType(currentWtt.getWorkTypeId());
                    }
                    result.add(currentRange = new CellRange(workType, currentWtt, 1));
                }
                // продление диапазона
                else {
                    currentRange.cells++;
                }

            }

            // разброс тасков по диапазонам
            if (taskList != null) {
                for (WorkTask task : taskList) {
                    int minuteFrom = task.getMinuteFrom();
                    for (CellRange range : result) {
                        // пустое время
                        if (range.workTypeTime == null) {
                            continue;
                        }

                        int workTypeId = range.workTypeTime.getWorkTypeId();

                        WorkType workType = CallboardCache.getWorkType(workTypeId);
                        if (workType == null) {
                            log.warn("Not found WorkType with id: " + workTypeId);
                            continue;
                        }

                        // попадает в диапазон - далее размещение по позиции
                        if (range.workTypeTime.getDayMinuteFrom() <= minuteFrom && minuteFrom < range.workTypeTime.getDayMinuteTo()) {
                            range.taskList.add(task);
                            task.setSlotTo(task.getSlotFrom() + (task.getDuration() / workType.getTimeSetStep()));
                        }
                    }
                }
            }
            return result;
        }
    }

    // диапазон ячеек в смене, занятый типом работ либо свободный
    public static class CellRange {
        private static final Logger log = Logger.getLogger(WorkAction.class);

        // null - свободное время
        private final WorkType workType;
        // null - свободное время
        private final WorkTypeTime workTypeTime;
        /*private final int startDayMinute;*/
        // сколько ячеек занимает в дневной таблице
        private int cells;
        // на сколько слотов разбит
        private final int slotCount;
        private List<WorkTask> taskList = new ArrayList<WorkTask>();

        private CellRange(WorkType workType, WorkTypeTime workTypeTime/*, int startDayMinute*/, int cells) {
            this.workType = workType;
            this.workTypeTime = workTypeTime;
            /*this.startDayMinute = startDayMinute;*/
            this.cells = cells;
            this.slotCount = workType != null ? workType.getSlotCount(workTypeTime.getDayMinuteFrom(), workTypeTime.getDayMinuteTo()) : 1;
        }

        public WorkType getWorkType() {
            return workType;
        }

        public WorkTypeTime getWorkTypeTime() {
            return workTypeTime;
        }

        /*	public int getStartDayMinute()
            {
                return startDayMinute;
            }*/

        public int getCells() {
            return cells;
        }

        // возвращает свободные слоты либо занятые каким-то процессом
        public List<SlotRange> getSlotRanges() {
            List<SlotRange> result = new ArrayList<SlotRange>();

            if (workTypeTime != null) {
                Iterator<WorkTask> taskIterator = taskList.iterator();

                SlotRange currentRange = null;

                // WorkTask на подходе либо текущий
                WorkTask currentTask = null;

                for (int slot = 0; slot < slotCount; slot++) {
                    if (currentTask == null || currentTask.getSlotTo() <= slot) {
                        currentTask = taskIterator.hasNext() ? taskIterator.next() : null;
                    }

                    // есть текущий диапазон и слот попал в него
                    if (currentTask != null && (currentTask.getSlotFrom() <= slot && slot < currentTask.getSlotTo())) {
                        if (currentRange == null || currentRange.task != currentTask) {
                            result.add(currentRange = new SlotRange(currentTask, slot, 1));
                        } else {
                            currentRange.slotCount++;
                        }
                    }
                    // нет текущего диапазона, либо не дошли до него
                    else {
                        result.add(new SlotRange(null, slot, 1));
                    }
                }
            }

            return result;
        }

        public int getDayMinuteFrom(SlotRange slotRange) {
            return workTypeTime.getDayMinuteFrom() + slotRange.slotFrom * workType.getTimeSetStep();
        }

        /**
         * Возвращает свободные диапазоны слотов.
         * @return
         * 
         * 11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - Checking free ranges: 
        11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - SlotRange from: 0; count: 1; task: null
        11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - SlotRange from: 1; count: 1; task: null
        11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - SlotRange from: 2; count: 1; task: null
        11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - SlotRange from: 3; count: 1; task: null
        11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - SlotRange from: 4; count: 4; task: ru.bgcrm.model.work.WorkTask@b4114
        11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - SlotRange from: 8; count: 1; task: null
        11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - SlotRange from: 9; count: 1; task: null
        11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - SlotRange from: 10; count: 1; task: null
        11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - SlotRange from: 11; count: 1; task: null
        11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - SlotRange from: 12; count: 1; task: null
        11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - SlotRange from: 13; count: 1; task: null
        11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - Date: 22.11.2014; Shift team: 1; userIds: [3443]; shiftId: 165; time: 120; slotSize: 30
        11-22/16:03:30 DEBUG [http-bio-9089-exec-9] WorkAction - Checking free ranges: 0-4, 5-11
         */
        public List<int[]> getFreeSlotRanges() {
            List<int[]> rangeList = new ArrayList<int[]>();

            int[] currentRange = null;

            for (SlotRange pair : getSlotRanges()) {
                if (log.isDebugEnabled()) {
                    log.debug("SlotRange from: " + pair.slotFrom + "; count: " + pair.slotCount + "; task: " + pair.task);
                }

                // слот не занят
                if (pair.task != null) {
                    currentRange = null;
                } else {
                    if (currentRange == null) {
                        rangeList.add(currentRange = new int[] { pair.slotFrom, pair.slotFrom });
                    } else {
                        currentRange[1] = pair.slotFrom;
                    }
                }
            }

            return rangeList;
        }
    }

    public static class SlotRange {
        private final WorkTask task;
        // с какого слота
        private final int slotFrom;
        // сколько занимают
        private int slotCount;

        private SlotRange(WorkTask task, int slotFrom, int slotCount) {
            this.task = task;
            this.slotFrom = slotFrom;
            this.slotCount = slotCount;
        }

        public WorkTask getTask() {
            return task;
        }

        public int getSlotFrom() {
            return slotFrom;
        }

        public int getSlotCount() {
            return slotCount;
        }
    }

    public static class FreeSlotRange {
        private final Date date;
        private final int groupId;
        private final ShiftData shiftData;
        private final CellRange cellRange;
        private final int slotFrom;
        private final int duration;
        private final int dayMinuteFrom;

        private FreeSlotRange(Date date, int groupId, ShiftData shiftData, CellRange cellRange, int slotFrom, int duration) {
            this.date = date;
            this.groupId = groupId;
            this.shiftData = shiftData;
            this.cellRange = cellRange;
            this.slotFrom = slotFrom;
            this.duration = duration;
            this.dayMinuteFrom = getDayMinuteFrom();
        }

        private int getDayMinuteFrom() {
            int result = cellRange.workTypeTime.getDayMinuteFrom();

            if (cellRange.workType.getTimeSetMode() == WorkTypeConfig.MODE_TIME_ON_STEP) {
                result += slotFrom * cellRange.workType.getTimeSetStep();
            }

            return result;
        }

        public Date getTime() {
            Calendar cal = TimeUtils.convertDateToCalendar(date);
            cal.add(Calendar.MINUTE, dayMinuteFrom);
            return TimeUtils.convertCalendarToDate(cal);
        }
    }

    public ActionForward planGet(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        int graphId = form.getParamInt("graphId", 0);

        ParameterMap perm = form.getPermission();

        CallboardConfig config = setup.getConfig(CallboardConfig.class);

        form.getResponse().setData("callboardList", config.getCallboards(Utils.toIntegerSet(perm.get("allowOnlyCallboards"))));

        //определние начальной и конечной даты, формирование сета с датами для шапки графика
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

        return html(con, mapping, form, "plan");
    }

    public ActionForward processTime(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {
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

            // только времена
            /*Set<Date> timeSet = new LinkedHashSet<Date>();
            for( FreeSlotRange slot : freeSlots )
            {
                timeSet.add( slot.getTime( date ) );
            }
            
            form.setResponseData( "timeSet", timeSet );*/
        }

        return html(con, mapping, form, "processTimeSet");
    }

    private static final Object SET_TIME_MUTEX = new Object();

    public ActionForward processTimeSet(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
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

                    Set<ProcessExecutor> currentExecutors = process.getProcessExecutors();
                    ProcessExecutor.updateProcessExecutors(currentExecutors, processGroup, allowedSlot.shiftData.getUserIds());

                    process.setProcessExecutors(currentExecutors);

                    processDao.updateProcessExecutors(currentExecutors, processId);

                    // установка параметра
                    new ParamValueDAO(con).updateParamDateTime(processId, timeSetConfig.getParam().getId(), time);

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

    public ActionForward processTimeLock(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {

        return processTimeLockAction(form, con, true);
    }

    public ActionForward processTimeUnlock(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {
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

        Set<Integer> processGroupIds = ProcessGroup.getGroupsWithRole(process.getProcessGroups(), 0);
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
                                log.debug("Date: " + TimeUtils.format(date, TimeUtils.FORMAT_TYPE_YMD) + "; Shift team: " + data.team + "; userIds: "
                                        + data.userIds + "; shiftId: " + data.shiftId + "; time: " + time + "; slotSize: " + slotSize);

                                StringBuilder ranges = new StringBuilder();
                                for (int[] r : freeSlotRanges) {
                                    Utils.addSeparated(ranges, ", ", r[0] + "-" + r[1]);
                                }

                                log.debug("Checking free ranges: " + ranges);
                            }

                            for (int[] freeRange : freeSlotRanges) {
                                for (int rangeStart = freeRange[0]; rangeStart < freeRange[1]; rangeStart++) {
                                    if ((freeRange[1] - rangeStart + 1) * slotSize >= time) {
                                        result.add(new FreeSlotRange(date, groupId, data, range, rangeStart, time));

                                        if (log.isDebugEnabled()) {
                                            log.debug("FreeSlotRange: " + rangeStart);
                                        }
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
