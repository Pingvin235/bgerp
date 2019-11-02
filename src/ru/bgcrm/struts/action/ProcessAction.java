package ru.bgcrm.struts.action;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.cache.ProcessQueueCache;
import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.IfaceStateDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.dao.process.ProcessTypeDAO;
import ru.bgcrm.dao.process.SavedFilterDAO;
import ru.bgcrm.dao.process.StatusChangeDAO;
import ru.bgcrm.dao.process.StatusDAO;
import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.ProcessMarkedActionEvent;
import ru.bgcrm.event.QueuePrintEvent;
import ru.bgcrm.event.UserEvent;
import ru.bgcrm.event.client.ProcessOpenEvent;
import ru.bgcrm.event.link.LinkAddedEvent;
import ru.bgcrm.event.link.LinkAddingEvent;
import ru.bgcrm.event.listener.TemporaryObjectOpenListener;
import ru.bgcrm.event.process.ProcessChangedEvent;
import ru.bgcrm.event.process.ProcessChangingEvent;
import ru.bgcrm.event.process.ProcessCreatedAsLinkEvent;
import ru.bgcrm.event.process.ProcessRequestEvent;
import ru.bgcrm.model.ArrayHashMap;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.EntityLogItem;
import ru.bgcrm.model.IfaceState;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessExecutor;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.Queue;
import ru.bgcrm.model.process.Queue.ColumnConf;
import ru.bgcrm.model.process.StatusChange;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.process.Wizard;
import ru.bgcrm.model.process.config.LinkProcessCreateConfig;
import ru.bgcrm.model.process.config.LinkProcessCreateConfigItem;
import ru.bgcrm.model.process.config.ProcessReferenceConfig;
import ru.bgcrm.model.process.queue.Processor;
import ru.bgcrm.model.process.queue.config.SavedCommonFiltersConfig;
import ru.bgcrm.model.process.queue.config.SavedFilter;
import ru.bgcrm.model.process.queue.config.SavedFiltersConfig;
import ru.bgcrm.model.process.queue.config.SavedFiltersConfig.SavedFilterSet;
import ru.bgcrm.model.process.queue.config.SavedPanelConfig;
import ru.bgcrm.model.process.wizard.WizardData;
import ru.bgcrm.model.user.Group;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.report.model.PrintQueueConfig;
import ru.bgcrm.plugin.report.model.PrintQueueConfig.PrintType;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.PatternFormatter;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SingleConnectionConnectionSet;

public class ProcessAction extends BaseAction {
    private static final Logger log = Logger.getLogger(ProcessAction.class);

    // выбранные в полном фильтре фильтры
    private static final String QUEUE_FULL_FILTER_SELECTED_FILTERS = "queueSelectedFilters";
    // параметры полного фильтра
    private static final String QUEUE_FULL_FILTER_PARAMS = "queueCurrentSavedFiltersParam.";

    @Override
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm actionForm, Connection con) throws Exception {
        return process(mapping, actionForm, con);
    }

    public ActionForward queue(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        form.getResponse().setData("list", ProcessQueueCache.getUserQueueList(form.getUser()));

        return processUserTypedForward(conSet, mapping, form, "queue");
    }

    public ActionForward queueSavedFilterSet(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        Preferences personalizationMap = form.getUser().getPersonalizationMap();
        SavedFiltersConfig config = personalizationMap.getConfig(SavedFiltersConfig.class);

        String persConfigBefore = personalizationMap.getDataString();

        String command = form.getParam("command");
        int queueId = form.getParamInt("queueId");
        if (Utils.isBlankString(command) || queueId <= 0) {
            throw new BGIllegalArgumentException();
        }

        ArrayList<SavedFilter> commonFilters = new SavedFilterDAO(con).getFilters(queueId);
        SavedCommonFiltersConfig commonConfig = new SavedCommonFiltersConfig(commonFilters);

        if (command.equals("delete")) {
            config.removeSavedFilterSet(queueId, form.getId());
            personalizationMap.removeSub(SavedFiltersConfig.QUEUE_CURRENT_SAVED_FILTER_SET_PREFIX + queueId);
        } else if (command.equals("add")) {
            String title = form.getParam("title");
            String url = form.getParam("url");

            if (Utils.isBlankString(title) || Utils.isBlankString(url)) {
                throw new BGIllegalArgumentException();
            }

            int createdSetId = config.addSavedFilterSet(queueId, title, url);

            personalizationMap.set(SavedFiltersConfig.QUEUE_CURRENT_SAVED_FILTER_SET_PREFIX + queueId, String.valueOf(createdSetId));
        } else if (command.equals("select")) {
            if (form.getId() < 0) {
                throw new BGIllegalArgumentException();
            }
            personalizationMap.set(SavedFiltersConfig.QUEUE_CURRENT_SAVED_FILTER_SET_PREFIX + queueId, String.valueOf(form.getId()));
        } else if (command.equals("toFullFilter")) {
            // extracting from unexisting empty filter - reset full filter
            SavedFilterSet filter = config.getSavedFilterSetMap().get(form.getId());
            DynActionForm savedFiltersForm = filter != null ? new DynActionForm(filter.getUrl()) : new DynActionForm();
            saveFormFilters(queueId, savedFiltersForm, personalizationMap);
            personalizationMap.remove(QUEUE_FULL_FILTER_SELECTED_FILTERS + queueId);
        } else if (command.equals("updateFiltersOrder")) {
            config.reorderSavedFilterSets(queueId, form.getSelectedValuesList("setId"));
        } else if (command.equals("setRareStatus")) {
            int filterId = form.getParamInt("filterId");
            Boolean value = form.getParamBoolean("rare", false);

            log.debug("set rare status: " + value + " " + filterId);
            config.setRareStatus(queueId, filterId, value);
        } else if (command.equals("setStatusCounterOnPanel")) {
            int filterId = form.getParamInt("filterId");
            Boolean value = form.getParamBoolean("statusCounterOnPanel", false);
            String color = form.getParam("color");
            String title = form.getParam("title");
            String queueName = form.getParam("queueName");
            log.debug("set counter on panel status: " + value + " " + filterId);
            config.setStatusCounterOnPanel(queueId, filterId, color, value, title, queueName);
        } else if (command.equals("addCommon")) {
            String title = form.getParam("title");
            String url = form.getParam("url");
            log.debug("Adding common filter " + title + " " + url);
            if (Utils.isBlankString(title) || Utils.isBlankString(url)) {
                throw new BGIllegalArgumentException();
            }
            commonConfig.addSavedCommonFilter(queueId, title, url);

            new SavedFilterDAO(con).updateFilter(commonConfig, queueId);
        } else if (command.equals("importCommon")) {
            String title = form.getParam("title");
            int id = form.getParamInt("id");
            String url = new SavedFilterDAO(con).getFilterUrlById(id);

            log.debug("Importing common filter " + title + " " + url);

            if (Utils.isBlankString(title) || Utils.isBlankString(url)) {
                throw new BGIllegalArgumentException();
            }
            int createdSetId = config.addSavedFilterSet(queueId, title, url);

            personalizationMap.set(SavedFiltersConfig.QUEUE_CURRENT_SAVED_FILTER_SET_PREFIX + queueId, String.valueOf(createdSetId));
        } else if (command.equals("deleteCommon")) {
            String title = form.getParam("title");
            int id = form.getParamInt("id");
            String url = new SavedFilterDAO(con).getFilterUrlById(id);

            log.debug("Deleting common filter " + title + " " + url);

            if (Utils.isBlankString(title) || Utils.isBlankString(url)) {
                throw new BGIllegalArgumentException();
            }
            commonConfig.deleteSavedCommonFilter(queueId, title, url);

            new SavedFilterDAO(con).deleteFilter(id);
        }

        config.updateConfig(personalizationMap);

        new UserDAO(con).updatePersonalization(persConfigBefore, form.getUser());

        return processJsonForward(con, form);
    }

    public ActionForward queueSavedPanelSet(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        Preferences personalizationMap = form.getUser().getPersonalizationMap();
        SavedPanelConfig config = personalizationMap.getConfig(SavedPanelConfig.class);

        String persConfigBefore = personalizationMap.getDataString();

        String command = form.getParam("command");
        Integer queueId = form.getParamInt("queueId");
        log.debug(command + " " + queueId);

        if (Utils.isBlankString(command) || queueId <= 0) {
            throw new BGIllegalArgumentException();
        }

        if (command.equals("add")) {
            config.addSavedPanelSet(queueId);
        } else if (command.equals("delete")) {
            config.removeSavedPanelSet(queueId);
        } else if (command.equals("updateSelected")) {
            config.changeCurrentSelected(queueId);
        }

        config.updateConfig(personalizationMap);
        new UserDAO(con).updatePersonalization(persConfigBefore, form.getUser());

        return processJsonForward(con, form);

    }

    public ActionForward queueGet(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        User user = form.getUser();
        HttpServletRequest request = form.getHttpRequest();

        Queue queue = ProcessQueueCache.getQueue(form.getId(), user);
        if (queue != null && form.getUser().getQueueIds().contains(queue.getId())) {
            ArrayList<SavedFilter> commonFilters = new SavedFilterDAO(con).getFilters(queue.getId());
            SavedCommonFiltersConfig commonConfig = new SavedCommonFiltersConfig(commonFilters);
            request.setAttribute("commonConfig", commonConfig);

            form.getResponse().setData("queue", queue);
            form.getResponse().setData("statusList", new StatusDAO(con).getStatusList());

            List<ProcessType> typeList = ProcessTypeCache.getTypeList(queue.getProcessTypeIds());

            boolean onlyPermittedTypes = form.getPermission().getBoolean("onlyPermittedTypes", false);
            if (onlyPermittedTypes) {
                applyProcessTypePermission(typeList, user);
            }

            form.getResponse().setData("typeList", typeList);

            Preferences personalizationMap = user.getPersonalizationMap();
            String persConfigBefore = personalizationMap.getDataString();

            personalizationMap.put("queueLastSelected", String.valueOf(queue.getId()));

            String filtersValues = personalizationMap.get(QUEUE_FULL_FILTER_PARAMS + form.getId());
            if (!Utils.isEmptyString(filtersValues) || filtersValues != null) {
                ArrayHashMap ahm = (ArrayHashMap) SerializationUtils.deserialize(Base64.getDecoder().decode(filtersValues));
                DynActionForm savedParamsFilters = new DynActionForm();
                savedParamsFilters.setParam(ahm);
                request.setAttribute("savedParamsFilters", savedParamsFilters);
            }

            new UserDAO(con).updatePersonalization(persConfigBefore, user);
        }

        return processUserTypedForward(con, mapping, form, "queueFilter");
    }

    public ActionForward queueShow(ActionMapping mapping, DynActionForm form, ConnectionSet connectionSet) throws Exception {
        HttpServletRequest request = form.getHttpRequest();
        HttpServletResponse response = form.getHttpResponse();

        ProcessDAO processDAO = null;

        boolean selectQueueFromSlave = setup.getBoolean("selectQueueFromSlave", false);
        if (selectQueueFromSlave) {
            processDAO = new ProcessDAO(connectionSet.getSlaveConnection(), form.getUser());
        } else {
            processDAO = new ProcessDAO(connectionSet.getConnection(), form.getUser());
        }

        Preferences personalizationMap = form.getUser().getPersonalizationMap();

        String configBefore = personalizationMap.getDataString();

        int savedFilterSetId = form.getParamInt("savedFilterSetId");
        // выбранные в полном фильтре фильтры
        String selectedFilters = form.getParam("selectedFilters");

        personalizationMap.set(SavedFiltersConfig.QUEUE_CURRENT_SAVED_FILTER_SET_PREFIX + form.getId(), String.valueOf(savedFilterSetId));
        if (selectedFilters != null) {
            personalizationMap.set(QUEUE_FULL_FILTER_SELECTED_FILTERS + form.getId(), selectedFilters);
        }

        // полный фильтр - сохранение параметров запроса
        if (savedFilterSetId == 0) {
            /* Вроде нигде не используется уже..
            String key = "queueFilterParam." + form.getId() + ".";
            personalizationMap.removeSub( key );*/

            //TODO: Сохранение параметров стоит сделать пробегая непосредственно по фильтрам
            //и сортировкам, это исключит различные посторонние параметры вроде requestUrl.

            saveFormFilters(form.getId(), form, personalizationMap);
        }

        // параметры изменились
        new UserDAO(connectionSet.getConnection()).updatePersonalization(configBefore, form.getUser());

        if (!form.getUser().getQueueIds().contains(form.getId())) {
            throw new BGMessageException("Вам не разрешён доступ к очереди процессов с ID=" + form.getId() + "!");
        }

        Queue queue = ProcessQueueCache.getQueue(form.getId(), form.getUser());

        if (queue != null) {
            SearchResult<Object[]> searchResult = new SearchResult<Object[]>(form);
            List<String> aggregateValues = new ArrayList<>();

            processDAO.searchProcess(searchResult, aggregateValues, queue, form);

            final List<Object[]> list = searchResult.getList();

            // печать только выбранных
            Set<Integer> processIds = Utils.toIntegerSet(form.getParam("processIds"));
            if (processIds.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    Process process = ((Process[]) list.get(i)[0])[0];

                    if (!processIds.contains(process.getId())) {
                        list.remove(i--);
                    }
                }
            }

            if (Utils.notBlankString(form.getParam("print"))) {
                int printTypeId = form.getParamInt("printTypeId");

                if (printTypeId > 0) {
                    PrintQueueConfig config = queue.getConfigMap().getConfig(PrintQueueConfig.class);
                    PrintType printType = config.getPrintType(printTypeId);

                    queue.processDataForColumns(connectionSet.getConnection(), list, queue.getColumnConfList(printType.getColumnIds()), false);
                    EventProcessor.processEvent(new QueuePrintEvent(form, list, queue, printType), "", connectionSet);
                } else {
                    // TODO: В метод необходимо вынести расшифровку всех справочников.
                    // FIXME: В данный момент это событие обрабатывает модуль отчётов, наверное нужно запретить печать, если этот модуль не установлен.
                    queue.processDataForMedia(connectionSet.getConnection(), "print", list);
                    EventProcessor.processEvent(new QueuePrintEvent(form, list, queue, null), "", connectionSet);
                }
                return null;
            } else if (Utils.notBlankString(form.getParam("xls"))) {
                List<Object[]> media = list;

                queue.processDataForMedia(connectionSet.getConnection(), "xls", media);

                HSSFWorkbook workbook = new HSSFWorkbook();
                HSSFSheet sheet = workbook.createSheet("BGERP process");

                List<ColumnConf> columnList = queue.getMediaColumnList("xls");

                Row titleRow = sheet.createRow(0);

                for (int i = 0; i < columnList.size(); i++) {
                    Cell titleCell = titleRow.createCell(i);
                    titleCell.setCellValue(columnList.get(i).getTitle());
                }

                for (int k = 0; k < media.size(); k++) {
                    //Create a new row in current sheet
                    Row row = sheet.createRow(k + 1);
                    Object[] dataRow = media.get(k);

                    for (int i = 0; i < dataRow.length; i++) {
                        if (dataRow[i].equals("null")) {
                            continue;
                        } else {
                            //Create a new cell in current row
                            Cell cell = row.createCell(i);
                            cell.setCellValue(dataRow[i].toString());
                        }
                    }
                }
                response.setContentType("application/vnd.ms-excel");
                response.setHeader("Content-Disposition", "attachment; filename=bgcrm_process_list.xls");
                workbook.write(response.getOutputStream());

                return null;
            }

            request.setAttribute("columnList", queue.getMediaColumnList("html"));
            queue.processDataForMedia(connectionSet.getConnection(), "html", list);
            request.setAttribute("queue", queue);
            if (aggregateValues.size() > 0) {
                form.setResponseData("aggregateValues", aggregateValues);
            }
        } else {
            throw new BGMessageException("Очередь процессов с ID=" + form.getId() + " не найдена!");
        }

        return processUserTypedForward(connectionSet, mapping, form, "queueShow");
    }

    private void saveFormFilters(int queueId, DynActionForm form, Preferences personalizationMap) {
        ArrayHashMap ahm = new ArrayHashMap();
        for (String paramName : form.getParam().keySet()) {
            if (!paramName.equals("requestUrl") && form.getParamArray(paramName) != null && form.getParamArray(paramName).length > 0) {
                if (form.getParamArray(paramName).length > 1) {
                    ahm.put(paramName, form.getParamArray(paramName));
                } else if (Utils.notBlankString(form.getParam(paramName))) {
                    ahm.put(paramName, form.getParam(paramName));
                }
            }
        }

        String paramKey = QUEUE_FULL_FILTER_PARAMS + queueId;
        personalizationMap.set(paramKey, Base64.getEncoder().encodeToString(SerializationUtils.serialize(ahm)));
    }

    public ActionForward process(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDAO = new ProcessDAO(con);

        Process process = processDAO.getProcess(form.getId());
        if (process != null) {
            ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
            form.getResponse().setData("process", process);

            form.getHttpRequest().setAttribute("processType", type);

            // генерация описания процесса
            if (type != null) {
                ProcessReferenceConfig config = type.getProperties().getConfigMap().getConfig(ProcessReferenceConfig.class);
                process.setReference(config.getReference(con, form, process, "processCard"));
            }

            // передача мастера
            if (Utils.notBlankString(form.getParam("wizard")) || form.getId() < 0) {
                Wizard wizard = type.getProperties().getCreateWizard();
                if (wizard != null) {
                    form.getHttpRequest().setAttribute("wizardData",
                            new WizardData(con, form, wizard, process, form.getId() < 0 ? wizard.getCreateStepList() : wizard.getStepList()));
                }
            }
        }

        return processUserTypedForward(con, mapping, form, "process");
    }

    // возвращает дерево типов для создания процесса
    public ActionForward typeTree(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        int queueId = Utils.parseInt(form.getParam("queueId"));
        Queue queue = ProcessQueueCache.getQueue(queueId, form.getUser());
        User user = form.getUser();

        // очередь не разрешена пользователю
        if (queue == null)
            return processUserTypedForward(con, mapping, form, "processTypeTree");

        List<ProcessType> typeList = ProcessTypeCache.getTypeList(queue.getProcessTypeIds());

        boolean onlyPermittedTypes = form.getPermission().getBoolean("onlyPermittedTypes", false);
        if (onlyPermittedTypes) {
            applyProcessTypePermission(typeList, user);
        }

        Set<Integer> typeSet = new HashSet<Integer>();
        for (ProcessType type : typeList) {
            typeSet.add(type.getId());
        }

        form.getHttpRequest().setAttribute("typeTreeRoot", ProcessTypeCache.getTypeTreeRoot().clone(typeSet, onlyPermittedTypes));

        return processUserTypedForward(con, mapping, form, "processTypeTree");
    }

    private void applyProcessTypePermission(List<ProcessType> typeList, User user) {
        List<Integer> typeForRemove = new ArrayList<Integer>();
        Iterator<ProcessType> iterator = typeList.iterator();
        while (iterator.hasNext()) {
            ProcessType type = iterator.next();

            if (type.getProperties().getConfigMap().getBoolean("allowForNonExecutorsGroup", false)) {
                continue;
            }

            if (CollectionUtils.intersection(type.getProperties().getAllowedGroupsSet(), user.getGroupIds()).isEmpty()
                    && CollectionUtils.intersection(type.getProperties().getGroupsSet(), user.getGroupIds()).isEmpty()) {
                typeForRemove.add(type.getId());
                typeForRemove.addAll(type.getAllChildIds());
            }
        }
        iterator = typeList.iterator();
        while (iterator.hasNext()) {
            ProcessType type = iterator.next();
            if (typeForRemove.contains(type.getId())) {
                iterator.remove();
            }
        }
    }

    public ActionForward processCreateGroups(ActionMapping mapping, DynActionForm form, Connection con) {
        int typeId = form.getParamInt("typeId", 0);
        ProcessType type = ProcessTypeCache.getProcessType(typeId);

        if (type != null) {
            List<Group> groups = new ArrayList<Group>();
            for (int groupId : Utils.toIntegerSet(type.getProperties().getConfigMap().get("onCreateSelectGroup"))) {
                groups.add(UserCache.getUserGroup(groupId));
            }

            form.getResponse().setData("groups", groups);
        }

        return processUserTypedForward(con, mapping, form, "processCreateGroup");
    }

    public static Process processCreate(DynActionForm form, Connection con) throws Exception {
        Process process = new Process();

        process.setTypeId(Utils.parseInt(form.getParam("typeId")));
        process.setDescription(Utils.maskNull(form.getParam("description")));

        processCreate(form, con, process, form.getParamInt("groupId", 0));

        return process;
    }

    public static void processCreate(DynActionForm form, Connection con, Process process) throws Exception {
        processCreate(form, con, process, -1);
    }

    public static void processCreate(DynActionForm form, Connection con, Process process, int groupId) throws Exception {
        ProcessDAO processDAO = new ProcessDAO(con);
        StatusChangeDAO changeDao = new StatusChangeDAO(con);

        ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
        if (type == null) {
            throw new BGMessageException("Неверный тип процесса.");
        }

        TypeProperties typeProperties = type.getProperties();

        process.setStatusId(0);
        process.setTitle("");
        process.setCreateUserId(form.getUser().getId());

        processDAO.updateProcess(process);

        StatusChange change = new StatusChange();
        change.setDate(new Date());
        change.setProcessId(process.getId());
        change.setUserId(form.getUserId());
        change.setComment("Процесс создан");

        change.setStatusId(type.getProperties().getCreateStatus());
        if (!ProcessTypeCache.getStatusMap().containsKey(change.getStatusId())) {
            throw new BGException("Для типа процесса не определён существующий начальный статус.");
        }

        changeDao.changeStatus(process, type, change);

        if (groupId > 0) {
            // если вручную указали группу из списка в конфига типа процесса onCreateSelectGroup, то выбраем ее
            Set<ProcessGroup> processGroups = new HashSet<ProcessGroup>();
            processGroups.add(new ProcessGroup(groupId, 0));
            process.setProcessGroups(processGroups);
        } else {
            // иначе выставляем то что указано в конфигурации типа процесса
            process.setProcessGroups(new HashSet<ProcessGroup>(typeProperties.getGroups()));
        }
        processDAO.updateProcessGroups(process.getProcessGroups(), process.getId());

        // FIXME: Старый метод установки исполнителя, ещё кое-где используется в Уфанете, пока оставить.
        String typeExecutor = typeProperties.getConfigMap().get("setExecutor", "");
        if (typeExecutor.startsWith("current")) {
            log.warn("Using deprecated setExecutor=current option in process type config!");

            ProcessGroup group = Utils.getFirst(process.getProcessGroupWithRole(0));
            if (group != null) {
                processDAO.updateProcessExecutors(ProcessExecutor.toProcessExecutorSet(Collections.singleton(form.getUserId()), group),
                        process.getId());
            }
        }

        // wizard=0 в обработке сообщений
        if (form.getParamBoolean("wizard", true)) {
            doCreateWizard(form, con, process, type);
        }

        EventProcessor.processEvent(new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_CREATED),
                type.getProperties().getActualScriptName(), new SingleConnectionConnectionSet(con));

        form.getResponse().setData("process", process);
    }

    protected static void doCreateWizard(DynActionForm form, Connection con, Process process, ProcessType type) throws BGException {
        // временный процесс - с отрицательным кодом
        Wizard wizard = type.getProperties().getCreateWizard();
        if (wizard != null && !wizard.getCreateStepList().isEmpty()) {
            new ProcessDAO(con).processIdInvert(process);

            TemporaryObjectOpenListener.flushUserData(form.getUserId());
        }
    }

    /** 
     * Создаёт процесс и возвращает его код для перехода в редактор.
     */
    public ActionForward processCreate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessAction.processCreate(form, con);

        return processJsonForward(con, form);
    }

    public ActionForward processDeleteTmp(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDao = new ProcessDAO(con);

        if (form.getId() > 0) {
            throw new BGIllegalArgumentException();
        }

        Process process = getProcess(processDao, form.getId());
        processDao.deleteProcess(process.getId());

        TemporaryObjectOpenListener.flushUserData(form.getUserId());

        return processJsonForward(con, form);
    }

    public ActionForward processDelete(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDao = new ProcessDAO(con);

        Process process = getProcess(processDao, form.getId());
        processDao.deleteProcess(process.getId());

        return processJsonForward(con, form);
    }

    public ActionForward processFinishCreateTmp(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDao = new ProcessDAO(con);

        Process process = getProcess(processDao, form.getId());
        ProcessType type = getProcessType(process.getTypeId());

        processDao.processIdInvert(process);

        EventProcessor.processEvent(new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_CREATE_FINISHED),
                type.getProperties().getActualScriptName(), new SingleConnectionConnectionSet(con));

        TemporaryObjectOpenListener.flushUserData(form.getUserId());

        return processJsonForward(con, form);
    }

    public ActionForward processDoCommands(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        Process process = getProcess(new ProcessDAO(con), form.getId());

        List<String> commands = Utils.toList(form.getParam("commands"), ";");
        if (commands.size() == 0) {
            throw new BGException("Пустой список команд");
        }

        ProcessCommandExecutor.processDoCommands(con, form, process, null, commands);

        return processJsonForward(con, form);
    }

    public ActionForward processStatusUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        Process process = getProcess(new ProcessDAO(con), form.getId());

        int statusId = Utils.parseInt(form.getParam("statusId"));
        if ("prev".equals(form.getParam("statusId"))) {
            SearchResult<StatusChange> searchResult = new SearchResult<StatusChange>();
            new StatusChangeDAO(con).searchProcessStatus(searchResult, process.getId(), null);
            if (searchResult.getList().size() < 2) {
                throw new BGMessageException("У процесса не было предыдущего статуса.");
            }

            statusId = searchResult.getList().get(1).getStatusId();
        }

        StatusChange change = new StatusChange();
        change.setDate(new Date());
        change.setProcessId(process.getId());
        change.setStatusId(statusId);
        change.setUserId(form.getUserId());
        change.setComment(form.getParam("comment", ""));

        processStatusUpdate(form, con, process, change);

        return processJsonForward(con, form);
    }

    public static void processStatusUpdate(DynActionForm form, Connection con, Process process, StatusChange change) throws Exception {
        StatusChangeDAO changeDao = new StatusChangeDAO(con);

        ProcessType type = getProcessType(process.getTypeId());

        // указаны обязательные при смене статуса к заполнению параметры
        final String requireParamName = "requireFillParamIdsBeforeStatusSet." + change.getStatusId();

        if (Utils.isBlankString(change.getComment())) {
            Set<Integer> requireStatusChangeComment = Utils
                    .toIntegerSet(type.getProperties().getConfigMap().get("requireChangeCommentStatusIds", ""));
            if (requireStatusChangeComment.contains(change.getStatusId())) {
                throw new BGMessageException("Перевод в данный статус обязан содержать комментарий.");
            }
        }

        ParamValueDAO paramValueDao = new ParamValueDAO(con);

        Set<Integer> requireBeforeParams = Utils.toIntegerSet(type.getProperties().getConfigMap().get(requireParamName, ""));
        for (int requireParamId : requireBeforeParams) {
            Parameter requireParam = ParameterCache.getParameter(requireParamId);
            if (requireParam == null) {
                throw new BGMessageException(
                        "Параметр с кодом " + requireParamId + " не существует.\nУказан в " + requireParamName + " конфигурации типа процесса.");
            }

            if (!paramValueDao.isParameterFilled(process.getId(), requireParam)) {
                throw new BGMessageException("Параметр '" + requireParam.getTitle() + "' не заполнен.");
            }

            EventProcessor.processEvent(new ProcessChangingEvent(form, process, change, ProcessChangingEvent.MODE_STATUS_CHANGING),
                    requireParam.getScript(), new SingleConnectionConnectionSet(con), false);
        }

        processDoEvent(form, process, new ProcessChangingEvent(form, process, change, ProcessChangingEvent.MODE_STATUS_CHANGING), con);

        changeDao.changeStatus(process, type, change);

        processDoEvent(form, process, new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_STATUS_CHANGED), con);
    }

    public ActionForward processStatusHistory(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        new StatusChangeDAO(con).searchProcessStatus(new SearchResult<StatusChange>(form), form.getId(), form.getSelectedValues("statusId"));

        return processUserTypedForward(con, mapping, form, "processStatusHistory");
    }

    public ActionForward processPriorityUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDAO = new ProcessDAO(con, form.getUser(), true);

        Process process = getProcess(processDAO, form.getId());
        int priority = Utils.parseInt(form.getParam("priority"));

        processPriorityUpdate(form, process, con, priority);

        return processJsonForward(con, form);
    }

    public static void processPriorityUpdate(DynActionForm form, Process process, Connection con, Integer priority) throws Exception {
        ProcessDAO processDAO = new ProcessDAO(con, form.getUser(), true);

        processDoEvent(form, process, new ProcessChangingEvent(form, process, priority, ProcessChangingEvent.MODE_PRIORITY_CHANGING), con);
        process.setPriority(priority);
        processDAO.updateProcess(process);
        processDoEvent(form, process, new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_PRIORITY_CHANGED), con);
    }

    public ActionForward processTypeEdit(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        form.getHttpRequest().setAttribute("typeTreeRoot", ProcessTypeCache.getTypeTreeRoot());

        return processUserTypedForward(con, mapping, form, "processTypeChange");
    }

    public ActionForward processTypeUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDAO = new ProcessDAO(con, form.getUser(), true);
        Process process = getProcess(processDAO, form.getId());
        int typeId = Utils.parseInt(form.getParam("typeId"));
        processTypeUpdate(form, process, con, typeId);

        return processJsonForward(con, form);
    }

    private static void processTypeUpdate(DynActionForm form, Process process, Connection con, Integer typeId) throws Exception {
        ProcessDAO processDAO = new ProcessDAO(con, form.getUser(), true);
        processDoEvent(form, process, new ProcessChangingEvent(form, process, typeId, ProcessChangingEvent.MODE_TYPE_CHANGING), con);
        process.setTypeId(typeId);

        processDAO.updateProcess(process);

        processDoEvent(form, process, new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_TYPE_CHANGED), con);
    }

    public ActionForward processDescriptionUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDAO = new ProcessDAO(con, form.getUser(), true);

        Process process = getProcess(processDAO, form.getId());
        String description = form.getParam("description");

        processDoEvent(form, process, new ProcessChangingEvent(form, process, description, ProcessChangingEvent.MODE_DESCRIPTION_CHANGING), con);
        process.setDescription(description);
        processDAO.updateProcess(process);
        processDoEvent(form, process, new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_DESCRIPTION_CHANGED), con);

        return processJsonForward(con, form);
    }

    public ActionForward processDescriptionAdd(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDAO = new ProcessDAO(con, form.getUser(), true);

        final Process process = getProcess(processDAO, form.getId());
        final String description = form.getParam("description");

        if (Utils.isBlankString(description)) {
            throw new BGIllegalArgumentException();
        }

        ProcessType type = getProcessType(process.getTypeId());

        String pattern = type.getProperties().getConfigMap().get("descriptionAddPattern", "(${description}\n)(${text})\t[(${time}) (${user})]");
        final String timePattern = type.getProperties().getConfigMap().get("descriptionAddPattern.timePattern", TimeUtils.FORMAT_TYPE_YMDHMS);

        if (!pattern.contains("${description}")) {
            pattern = "(${description}\n)" + pattern;
        }

        pattern = pattern.replaceAll("\\\\n", "\n").replaceAll("\\\\t", "\t");

        String newDescription = PatternFormatter.processPattern(pattern, new PatternFormatter.PatternItemProcessor() {
            @Override
            public String processPatternItem(String variable) {
                if ("time".equals(variable)) {
                    return TimeUtils.format(new Date(), timePattern);
                } else if ("user".equals(variable)) {
                    return form.getUser().getTitle();
                } else if ("text".equals(variable)) {
                    return description;
                } else if ("description".equals(variable)) {
                    return process.getDescription();
                }
                return "";
            }
        });

        processDoEvent(form, process, new ProcessChangingEvent(form, process, description, ProcessChangingEvent.MODE_DESCRIPTION_ADDING), con);
        process.setDescription(newDescription);
        processDAO.updateProcess(process);
        processDoEvent(form, process, new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_DESCRIPTION_ADDED), con);

        return processJsonForward(con, form);
    }

    public ActionForward processGroupsUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDao = new ProcessDAO(con, form.getUser(), true);

        Process process = getProcess(processDao, form.getId());
        Set<ProcessGroup> allowedGroups = ProcessTypeCache.getProcessType(process.getTypeId()).getProperties().getAllowedGroups();

        Set<String> groupRoleSet = form.getSelectedValuesStr("groupRole");
        Set<ProcessGroup> processGroupList = new LinkedHashSet<ProcessGroup>();

        for (String item : groupRoleSet) {
            ProcessGroup processGroup = new ProcessGroup();

            if (item.indexOf(":") > -1) {
                processGroup.setGroupId(Utils.parseInt(StringUtils.substringBefore(item, ":")));
                processGroup.setRoleId(Utils.parseInt(StringUtils.substringAfter(item, ":")));
            } else {
                processGroup.setGroupId(Integer.parseInt(item));
            }

            processGroupList.add(processGroup);
        }

        if (allowedGroups.size() > 0) {
            for (ProcessGroup item : processGroupList) {
                boolean exist = false;

                for (ProcessGroup allowedItem : allowedGroups) {
                    if (item.getGroupId() == allowedItem.getGroupId() && item.getRoleId() == allowedItem.getRoleId()) {
                        exist = true;
                        break;
                    }
                }

                if (!exist)
                    throw new BGException(
                            "Запрещено добавлять группу " + UserCache.getUserGroup(item.getGroupId()).getTitle() + " с ролью " + item.getRoleId());
            }
        }

        processGroupsUpdate(form, con, process, processGroupList);

        return processJsonForward(con, form);
    }

    public static void processGroupsUpdate(DynActionForm form, Connection con, Process process, Set<ProcessGroup> processGroups) throws Exception {
        ProcessDAO processDao = new ProcessDAO(con, form.getUser(), true);

        processDoEvent(form, process, new ProcessChangingEvent(form, process, processGroups, ProcessChangingEvent.MODE_GROUPS_CHANGING), con);

        process.setProcessGroups(processGroups);
        processDao.updateProcessGroups(processGroups, process.getId());
        
        // удаление исполнителей, привязанных к удалённым группоролям
        boolean updated = false;
        Set<ProcessExecutor> processExecutors = process.getProcessExecutors();
        Iterator<ProcessExecutor> processExecutorsIt = processExecutors.iterator();
        
        while (processExecutorsIt.hasNext()) {
            ProcessExecutor executor = processExecutorsIt.next();
            if (!processGroups.contains(new ProcessGroup(executor.getGroupId(), executor.getRoleId()))) {
                log.debug("Removing executorId: " + executor.getUserId() + "; groupId:" + executor.getGroupId() + "; roleId: " + executor.getRoleId());
                processExecutorsIt.remove();
                updated = true;
            }
        }
        
        if (updated) 
            processDao.updateProcessExecutors(processExecutors, process.getId());

        processDoEvent(form, process, new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_GROUPS_CHANGED), con);
    }

    public ActionForward processExecutorsUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        Process process = getProcess(new ProcessDAO(con, form.getUser(), true), form.getId());

        // группороли в которых обновляются исполнители
        Set<ProcessGroup> updateGroups = ProcessGroup.parseFromStringSet(form.getSelectedValuesStr("group"));
        Set<ProcessExecutor> executors = ProcessExecutor.parseUnsafe(form.getSelectedValuesStr("executor"), updateGroups);
        
        processExecutorsUpdate(form, con, process, updateGroups, executors);

        return processJsonForward(con, form);
    }

    @SuppressWarnings("unchecked")
    public static void processExecutorsUpdate(DynActionForm form, Connection con, Process process, Set<ProcessGroup> processGroups,
            Set<ProcessExecutor> processExecutors) throws Exception {
        ProcessDAO processDao = new ProcessDAO(con, form.getUser(), true);
        ParameterMap perm = form.getPermission();

        // различные проверки
        Set<Integer> allowOnlyGroupIds = Utils.toIntegerSet(perm.get("allowOnlyGroups"));
        if (allowOnlyGroupIds.size() != 0) {
            Collection<Integer> denyGroupIds = CollectionUtils.subtract(ProcessGroup.toGroupSet(processGroups), allowOnlyGroupIds);
            if (denyGroupIds.size() > 0)
                throw new BGMessageException("Запрещена правка исполнителей в группах:\n"
                        + Utils.getObjectList(UserCache.getUserGroupList(), new HashSet<Integer>(denyGroupIds)));
        }

        checkExecutorRestriction(process);

        Set<Integer> allowOnlyUsers = Utils.toIntegerSet(perm.get("allowOnlyUsers"));
        if (allowOnlyUsers.size() > 0) {
            // коды исполнителей которые добавляются либо удаляются
            Collection<Integer> changingExecutorIds = CollectionUtils.disjunction(process.getExecutorIds(),
                    ProcessExecutor.toExecutorSet(processExecutors));

            Collection<Integer> denyUserIds = CollectionUtils.subtract(changingExecutorIds, allowOnlyUsers);
            if (denyUserIds.size() > 0)
                throw new BGMessageException(
                        "Запрещена правка исполнителей:\n" + Utils.getObjectList(UserCache.getUserList(), new HashSet<Integer>(denyUserIds)));
        }

        Set<Integer> allowOnlyProcessTypeIds = Utils.toIntegerSet(perm.get("allowOnlyProcessTypeIds"));
        if (allowOnlyProcessTypeIds.size() > 0 && !CollectionUtils.containsAny(allowOnlyProcessTypeIds, Arrays.asList(process.getTypeId())))
            throw new BGMessageException("Запрещена правка исполнителей у данного типа процесса!");

        // проверка обновляемых групп
        for (ProcessGroup processGroup : processGroups) {
            // удаление не привязанных к группе пользователей
            if (processGroup.getGroupId() <= 0)
                continue;

            Group group = UserCache.getUserGroup(processGroup.getGroupId());
            if (group == null)
                throw new BGException("Не найдена группа с кодом: " + processGroup.getGroupId());

            if (!process.getProcessGroups().contains(processGroup))
                throw new BGMessageException("Группа: " + group.getTitle() + " с ролью: " + processGroup.getRoleId() + " не участвует в процессе.");
        }

        // текущие исполнители
        Set<ProcessExecutor> executors = new LinkedHashSet<ProcessExecutor>(process.getProcessExecutors());

        // удаление исполнителей привязанных к обновляемым группоролям, они будут заменены
        Iterator<ProcessExecutor> currentExecutorsIt = executors.iterator();
        while (currentExecutorsIt.hasNext()) {
            ProcessExecutor executor = currentExecutorsIt.next();
            if (processGroups.contains(new ProcessGroup(executor.getGroupId(), executor.getRoleId())))
                currentExecutorsIt.remove();
        }

        // обновляемые исполнители
        executors.addAll(processExecutors);

        processDoEvent(form, process, new ProcessChangingEvent(form, process, executors, ProcessChangingEvent.MODE_EXECUTORS_CHANGING), con);

        process.setProcessExecutors(executors);
        processDao.updateProcessExecutors(executors, process.getId());

        processDoEvent(form, process, new ProcessChangedEvent(form, process, ProcessChangedEvent.MODE_EXECUTORS_CHANGED), con);
    }

    /**
     * Проверяет ограничение на количество исполнителей в процессе
     * @param process
     * @throws BGMessageException
     */
    private static void checkExecutorRestriction(Process process) throws BGMessageException {
        Set<Integer> executorIds = process.getExecutorIds();

        ProcessType processType = ProcessTypeCache.getProcessType(process.getTypeId());
        for (Map.Entry<Integer, ParameterMap> entry : processType.getProperties().getConfigMap().subIndexed("executorRestriction.").entrySet()) {
            ParameterMap paramMap = entry.getValue();
            int groupId = paramMap.getInt("groupId", 0);
            int maxCount = paramMap.getInt("maxCount", 0);

            if (groupId > 0 && maxCount > 0) {
                int count = 0;
                List<User> userList = UserCache.getUserList(new HashSet<Integer>(Arrays.asList(new Integer[] { groupId })));

                for (Integer executorId : executorIds) {
                    User executor = UserCache.getUser(executorId);
                    if (userList.contains(executor)) {
                        count++;
                        if (count > maxCount) {
                            Group group = UserCache.getUserGroup(groupId);
                            StringBuilder sb = new StringBuilder();
                            sb.append("Максимальное количество исполнителей для группы\"");
                            sb.append(group.getTitle());
                            sb.append("\" равно ");
                            sb.append(maxCount);
                            sb.append(".");
                            throw new BGMessageException(sb.toString());
                        }
                    }
                }
            }
        }
    }

    private static void processDoEvent(DynActionForm form, Process process, UserEvent event, Connection con) throws Exception {
        ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
        if (type != null) {
            EventProcessor.processEvent(event, type.getProperties().getActualScriptName(), new SingleConnectionConnectionSet(con));
        }
    }

    private Process getProcess(ProcessDAO processDao, int id) throws BGException {
        Process process = processDao.getProcess(id);
        if (process == null) {
            throw new BGMessageException("Процесс не найдён.");
        }
        return process;
    }

    /**
     * Возвращает тип процесса из кэша по его коду либо генерит исключение, если его нет.
     * @param typeId
     * @return
     * @throws BGMessageException
     */
    public static ProcessType getProcessType(int typeId) throws BGMessageException {
        ProcessType type = ProcessTypeCache.getProcessType(typeId);
        if (type == null) {
            throw new BGMessageException("Не найден тип процесса: " + typeId);
        }
        return type;
    }

    // процессы, к которым привязана сущность
    public ActionForward linkedProcessList(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessLinkDAO processLinkDAO = new ProcessLinkDAO(con, form.getUser());

        restoreRequestParams(con, form, true, true, "closed");

        User user = form.getUser();
        String objectType = form.getParam("objectType");
        int id = form.getId();

        SearchResult<Pair<String, Process>> searchResult = new SearchResult<Pair<String, Process>>(form);
        processLinkDAO.searchLinkedProcessList(searchResult, CommonDAO.getLikePattern(objectType, "start"), id, null,
                form.getSelectedValues("typeId"), form.getSelectedValues("statusId"), form.getParam("paramFilter"),
                form.getParamBoolean("closed", null));

        form.getResponse().setData("typeList", processLinkDAO.getLinkedProcessTypeIdList(objectType, id));

        List<ProcessType> typeList = ProcessTypeCache.getTypeList(con, objectType, id);

        boolean onlyPermittedTypes = form.getPermission().getBoolean("onlyPermittedTypes", false);
        if (onlyPermittedTypes) {
            applyProcessTypePermission(typeList, user);
        }

        Set<Integer> typeSet = new HashSet<Integer>();
        for (ProcessType type : typeList) {
            typeSet.add(type.getId());
        }

        form.getHttpRequest().setAttribute("typeTreeRoot", ProcessTypeCache.getTypeTreeRoot().clone(typeSet, onlyPermittedTypes));

        Map<Integer, Boolean> wizardEnable = new HashMap<Integer, Boolean>(searchResult.getList().size());
        form.getHttpRequest().setAttribute("wizardEnable", wizardEnable);

        // генерация описаний процессов
        for (Pair<String, Process> pair : searchResult.getList()) {
            setProcessReference(con, form, pair.getSecond(), objectType);

            ProcessType type = ProcessTypeCache.getProcessType(pair.getSecond().getTypeId());
            if (type != null) {
                Wizard wizard = type.getProperties().getCreateWizard();
                if (wizard != null && wizard.check(con, form, pair.getSecond())) {
                    wizardEnable.put(pair.getSecond().getId(), Boolean.TRUE);
                }
            }
        }

        return processUserTypedForward(con, mapping, form, "linkedProcessList");
    }

    public ActionForward linkedProcessInfo(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {
        int id = form.getId();
        if (id <= 0) {
            throw new BGMessageException("process id error");
        }
        ProcessDAO processDAO = new ProcessDAO(con);

        form.getResponse().setData("process", processDAO.getProcess(id));
        new StatusChangeDAO(con).searchProcessStatus(new SearchResult<StatusChange>(form), form.getId(), form.getSelectedValues("statusId"));

        return processUserTypedForward(con, mapping, form, "linkedProcessInfo");
    }

    private void setProcessReference(Connection con, DynActionForm form, Process process, String objectType) {
        try {
            ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
            if (type != null) {
                ProcessReferenceConfig config = type.getProperties().getConfigMap().getConfig(ProcessReferenceConfig.class);
                process.setReference(config.getReference(con, form, process, objectType));
            }
        } catch (Exception e) {
            process.setReference(e.getMessage());
            log.error(e.getMessage(), e);
        }
    }

    // создание процесса с привязанной сущностью
    public ActionForward linkedProcessCreate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        String objectType = form.getParam("objectType");
        int id = form.getId();
        String objectTitle = form.getParam("objectTitle");

        Process process = ProcessAction.processCreate(form, con);

        //TODO: Может потом вернуть поддержку пермишена.
        /*	ParameterMap permission = form.getPermission();
        	Set<Integer> processTypeIds = Utils.toIntegerSet( permission.get( "allowedProcessTypeIds" ) );
        */

        CommonObjectLink link = new CommonObjectLink(Process.OBJECT_TYPE, process.getId(), objectType, id, objectTitle);

        EventProcessor.processEvent(new LinkAddingEvent(form, link),
                ProcessTypeCache.getProcessType(process.getTypeId()).getProperties().getActualScriptName(), new SingleConnectionConnectionSet(con));

        new ProcessLinkDAO(con).addLink(link);

        EventProcessor.processEvent(new LinkAddedEvent(form, link),
                ProcessTypeCache.getProcessType(process.getTypeId()).getProperties().getActualScriptName(), new SingleConnectionConnectionSet(con));

        // копирование параметров
        ProcessType type = ProcessTypeCache.getProcessType(form.getParamInt("typeId", 0));
        ParameterMap configMap = type.getProperties().getConfigMap();

        new ParamValueDAO(con).copyParams(id, process.getId(), configMap.get("create.in.copyParams"));

        if (configMap.getBoolean("create.in." + objectType + ".wizardCreated", false)) {
            form.getResponse().setData("wizard", 1);
            form.getResponse().getEventList().clear();
        } else if (configMap.getBoolean("create.in." + objectType + ".openCreated", configMap.getBoolean("create.in.openCreated", true))) {
            form.getResponse().addEvent(new ProcessOpenEvent(process.getId()));
        }

        return processJsonForward(con, form);
    }

    // процессы, привязанные к процессу
    public ActionForward linkProcessList(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        HttpServletRequest request = form.getHttpRequest();
        ProcessLinkDAO processLinkDao = new ProcessLinkDAO(con, form.getUser());

        int id = form.getId();

        Process process = getProcess(new ProcessDAO(con), id);
        ProcessType type = getProcessType(process.getTypeId());

        request.setAttribute("processType", type);

        // указание типов процессов которые можно создавать с произвольным видом привязки
        Set<Integer> createTypeIds = Utils.toIntegerSet(type.getProperties().getConfigMap().get("processCreateLinkProcessTypes"));
        request.setAttribute("typeList", ProcessTypeCache.getTypeList(createTypeIds));

        // жёстко указанные в конфигурации типы процессов, с указанными видами привязки, фильтры по параметру процесса и т.п.
        final List<LinkProcessCreateConfigItem> createTypeList = type.getProperties().getConfigMap().getConfig(LinkProcessCreateConfig.class)
                .getItemList(con, process);
        /*
        Если нужен - этот фильтр можно задать в checkExpression.
        Iterator<LinkProcessCreateConfigItem> iterator = createTypeList.iterator();
        while( iterator.hasNext() )
        {
        	LinkProcessCreateConfigItem item = iterator.next();
        	ProcessType pt = ProcessTypeCache.getProcessType( item.getProcessTypeId() );
        	if(CollectionUtils.retainAll( pt.getProperties().getAllowedGroupsSet(), form.getUser().getGroupIds() ).isEmpty() )
        	{
        		iterator.remove();
        	}
        }*/

        request.setAttribute("createTypeList", createTypeList);

        // список процессов, к которым привязан данный процесс
        SearchResult<Pair<String, Process>> searchResultLinked = new SearchResult<>();
        processLinkDao.searchLinkedProcessList(searchResultLinked, Process.OBJECT_TYPE + "%", id, null, null, null, null, null);
        form.getResponse().setData("linkedProcessList", searchResultLinked.getList());

        // генерация описаний процессов
        for (Pair<String, Process> pair : searchResultLinked.getList()) {
            setProcessReference(con, form, pair.getSecond(), form.getParam("linkedReferenceName"));
        }

        // привязанные к процессу процессы
        SearchResult<Pair<String, Process>> searchResultLink = new SearchResult<Pair<String, Process>>(form);
        processLinkDao.searchLinkProcessList(searchResultLink, id);

        // генерация описаний процессов
        for (Pair<String, Process> pair : searchResultLink.getList()) {
            setProcessReference(con, form, pair.getSecond(), form.getParam("linkReferenceName"));
        }

        // проверка и обновление статуса вкладки, если нужно
        IfaceState ifaceState = new IfaceState(form);
        IfaceState currentState = new IfaceState(Process.OBJECT_TYPE, id, form, String.valueOf(searchResultLink.getPage().getRecordCount()),
                String.valueOf(searchResultLinked.getPage().getRecordCount()));
        new IfaceStateDAO(con).compareAndUpdateState(ifaceState, currentState, form);

        return processUserTypedForward(con, mapping, form, "linkProcessList");
    }

    // создание процесса, привязанного к процессу
    public ActionForward linkProcessCreate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        int id = form.getId();

        // либо тип процесса + тип отношений
        int typeId = form.getParamInt("typeId", -1);
        String objectType = form.getParam("objectType", "");

        // либо код из конфигурации
        int createTypeId = form.getParamInt("createTypeId", -1);

        String description = Utils.maskNull(form.getParam("description"));

        Process linkedProcess = getProcess(new ProcessDAO(con), id);
        linkProcessCreate(con, form, linkedProcess, typeId, objectType, createTypeId, description, form.getParamInt("groupId", -1));

        return processJsonForward(con, form);
    }

    public static Process linkProcessCreate(Connection con, DynActionForm form, Process linkedProcess, int typeId, String objectType,
            int createTypeId, String description, int groupId) throws Exception {
        final ProcessLinkDAO linkDao = new ProcessLinkDAO(con);

        int linkedId = linkedProcess.getId();

        Process process = new Process();
        if (createTypeId > 0) {
            ProcessType linkedType = getProcessType(linkedProcess.getTypeId());

            LinkProcessCreateConfigItem item = linkedType.getProperties().getConfigMap().getConfig(LinkProcessCreateConfig.class)
                    .getItem(createTypeId);
            if (item == null) {
                throw new BGException("Не найдено правило с кодом: " + createTypeId);
            }

            objectType = item.getLinkType();

            process.setTypeId(item.getProcessTypeId());
            process.setDescription(description);

            processCreate(form, con, process, groupId);

            String copyParams = item.getCopyParamsMapping();
            if ("all".equals(copyParams)) {
                ProcessType type = getProcessType(process.getTypeId());
                List<Integer> paramIds = type.getProperties().getParameterIds();
                List<Integer> linkedParamIds = linkedType.getProperties().getParameterIds();
                List<Integer> paramIdsBothHave = new ArrayList<Integer>(linkedParamIds);
                paramIdsBothHave.retainAll(paramIds);

                new ParamValueDAO(con).copyParams(linkedId, process.getId(), StringUtils.join(paramIdsBothHave, ","));
            } else {
                new ParamValueDAO(con).copyParams(linkedId, process.getId(), copyParams);
            }

            String copyLinks = item.getCopyLinks();

            // пока копирование сразу всех привязок
            if (Utils.notBlankString(copyLinks)) {
                if (copyLinks.equals("1")) {
                    linkDao.copyLinks(linkedId, process.getId(), null, Process.OBJECT_TYPE + "%");
                } else {
                    linkDao.copyLinks(linkedId, process.getId(), copyLinks, Process.OBJECT_TYPE + "%");
                }
            }
        } else {
            process.setTypeId(typeId);
            process.setDescription(description);

            processCreate(form, con, process, -1);
        }

        // добавление привязки
        linkDao.addLink(new CommonObjectLink(linkedId, objectType, process.getId(), ""));

        ProcessType createdProcessType = getProcessType(process.getTypeId());
        EventProcessor.processEvent(new ProcessCreatedAsLinkEvent(form, linkedProcess, process),
                createdProcessType.getProperties().getActualScriptName(), new SingleConnectionConnectionSet(con));

        return process;
    }

    public ActionForward processCustomClassInvoke(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        Queue queue = ProcessQueueCache.getQueue(form.getParamInt("queueId"), form.getUser());
        if (queue != null) {
            Processor processor = queue.getProcessorMap().get(form.getParamInt("processorId"));
            List<Integer> processIds = Utils.toIntegerList(form.getParam("processIds"));

            ProcessMarkedActionEvent event = new ProcessMarkedActionEvent(form, processor, processIds);
            EventProcessor.processEvent(event, processor.getClassName(), conSet);

            if (event.isStreamResponse()) {
                return null;
            } else {
                return processJsonForward(conSet, form);
            }
        }

        return processUserTypedForward(conSet, mapping, form, FORWARD_DEFAULT);
    }

    public ActionForward processRequest(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessTypeDAO pDao = new ProcessTypeDAO(con);
        int typeId = form.getParamInt("typeId");

        if (typeId == 0) {
            int createTypeId = form.getParamInt("createTypeId");
            int parentTypeId = form.getParamInt("parentTypeId");

            if (createTypeId == 0 || parentTypeId == 0) {
                throw new BGException("Ошибка параметров запроса");
            }

            typeId = ProcessTypeCache.getProcessType(parentTypeId).getProperties().getConfigMap().getConfig(LinkProcessCreateConfig.class)
                    .getItem(createTypeId).getProcessTypeId();

            if (typeId == 0) {
                throw new BGException("Ошибка параметров запроса");
            }
        }

        ProcessType type = pDao.getProcessType(typeId);

        while (type.isUseParentProperties()) {
            type = pDao.getProcessType(type.getParentId());
        }

        ProcessRequestEvent processRequestEvent = new ProcessRequestEvent(form, type);

        EventProcessor.processEvent(processRequestEvent, type.getProperties().getActualScriptName(), new SingleConnectionConnectionSet(con));

        if (Utils.notBlankString(processRequestEvent.getForwardJspName())) {
            return processUserTypedForward(con, mapping, form, processRequestEvent.getForwardJspName());
        } else {
            return processJsonForward(con, form);
        }
    }

    public ActionForward messageRelatedProcessList(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        String addressFrom = form.getParam("from");
        Boolean open = form.getParamBoolean("open", null);

        List<CommonObjectLink> objects = new ArrayList<CommonObjectLink>();
        for (String object : form.getSelectedValuesListStr("object")) {
            int pos = object.lastIndexOf(':');
            if (pos <= 0) {
                log.warn("Incorrect object: " + object);
                continue;
            }

            objects.add(new CommonObjectLink(0, object.substring(0, pos), Utils.parseInt(object.substring(pos + 1)), ""));
        }

        SearchResult<Process> processSearchResult = new SearchResult<Process>(form);
        new ProcessDAO(con, form.getUser()).searchProcessListForMessage(processSearchResult, addressFrom, objects, open);

        return processUserTypedForward(con, mapping, form, "messageRelatedProcessList");
    }

    public ActionForward unionLog(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {

        new ProcessDAO(con).searchProcessLog(getProcessType(getProcess(new ProcessDAO(con), form.getId()).getTypeId()), form.getId(),
                new SearchResult<EntityLogItem>(form));

        return processUserTypedForward(con, mapping, form, "unionLog");
    }

    public ActionForward userProcessList(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {
        new ProcessDAO(con).searchProcessListForUser(new SearchResult<Process>(form), form.getUserId(), form.getParamBoolean("open", true));

        return processUserTypedForward(con, mapping, form, "userProcessList");
    }

}