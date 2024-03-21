package ru.bgcrm.struts.action;

import java.sql.Connection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.cache.UserCache;

import ru.bgcrm.dao.NewsDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.UserEvent;
import ru.bgcrm.event.process.ProcessDoActionEvent;
import ru.bgcrm.model.News;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessExecutor;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.StatusChange;
import ru.bgcrm.model.process.config.ProcessReferenceConfig;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SingleConnectionSet;

public class ProcessCommandExecutor {
    public static final String COMMAND_ADD_GROUPS = "addGroups";
    public static final String COMMAND_SET_STATUS = "setStatus";
    public static final String COMMAND_ADD_EXECUTORS = "addExecutors";
    public static final String COMMAND_SET_PARAM = "setParam";

    public static void processDoCommands(Connection con, DynActionForm form, Process process, UserEvent event, List<String> commands)
            throws Exception {
        ProcessType type = ProcessAction.getProcessType(process.getTypeId());

        for (String command : commands) {
            //linked:<typeId>:...
            if (command.startsWith("linkedProcess:")) {
                command = StringUtils.substringAfter(command, ":");
                int typeId = Utils.parseInt(StringUtils.substringBefore(command, ":"));
                if (typeId <= 0) {
                    throw new BGException("Не указан тип привязанного процесса в команде " + command);
                }

                process = Utils.getFirst(new ProcessLinkDAO(con).getLinkedProcessList(process.getId(), null, false, Collections.singleton(typeId)));
                if (process == null) {
                    throw new BGException("Не найден привязанный процесс: " + command);
                }

                command = StringUtils.substringAfter(command, ":");
            }

            if ("setCurrentExecutor".equals(command)) {
                command = "addExecutors:" + form.getUserId();
            }

            if (command.startsWith(COMMAND_SET_STATUS)) {
                int status = Utils.parseInt(StringUtils.substringAfter(command, ":"));

                StatusChange change = new StatusChange();
                change.setDate(new Date());
                change.setProcessId(process.getId());
                change.setUserId(form.getUserId());
                change.setStatusId(status);
                change.setComment("Автоматическая смена статуса");

                ProcessAction.processStatusUpdate(form, con, process, change);
            } else if (command.startsWith(COMMAND_ADD_EXECUTORS)) {
                String param = StringUtils.substringAfter(command, ":");

                Set<Integer> addingExecutorIds = Utils.toIntegerSet(param);

                // определение единственной группороли в которую добавляются исполнители
                ProcessGroup processGroup = null;
                for (ProcessGroup pg : process.getGroups()) {
                    for (Integer executorId : addingExecutorIds) {
                        User user = UserCache.getUser(executorId);
                        if (user.getGroupIds().contains(pg.getGroupId())) {
                            if (processGroup != null && processGroup.getGroupId() != pg.getGroupId()) {
                                throw new BGMessageException("Устанавливаемые исполнители относится к нескольким группам процесса.");
                            }
                            processGroup = pg;
                        }
                    }
                }

                if (processGroup == null) {
                    throw new BGMessageException("The set executors are not members of process execution groups.");
                }

                // добавление в текущих исполнителей группороли
                Set<ProcessExecutor> executors = ProcessExecutor.getProcessExecutors(process.getExecutors(), Collections.singleton(processGroup));
                executors.addAll(ProcessExecutor.toProcessExecutorSet(addingExecutorIds, processGroup));

                ProcessAction.processExecutorsUpdate(form, con, process, Collections.singleton(processGroup), executors);
            } else if (command.equals("clearGroups")) {
                ProcessAction.processGroupsUpdate(form, con, process, new HashSet<ProcessGroup>());
            } else if (command.equals("clearExecutors")) {
                ProcessAction.processExecutorsUpdate(form, con, process, process.getGroups(), new HashSet<ProcessExecutor>());
            } else if (command.equals("refreshCurrentQueue")) {
                form.getResponse().addEvent(new ru.bgcrm.event.client.ProcessCurrentQueueRefreshEvent());
            } else if (command.equals("open")) {
                form.getResponse().addEvent(new ru.bgcrm.event.client.ProcessOpenEvent(process.getId()));
            } else if (command.equals("close")) {
                form.getResponse().addEvent(new ru.bgcrm.event.client.ProcessCloseEvent(process.getId()));
            } else if (command.startsWith("newsNotifyExecutors") || command.startsWith("newsPopupNotifyExecutors")) {
                String subject = Utils.maskEmpty(StringUtils.substringAfterLast(command, ":"), "Изменился процесс ");

                String text = "Изменился процесс, в котором вы числитесь исполнителем.\n\n" + "Описание:<br/>" + process.getDescription();

                ProcessReferenceConfig config = type.getProperties().getConfigMap().getConfig(ProcessReferenceConfig.class);
                process.setReference(config.getReference(con, form, process, "newsNotifySubject", "processCard"));

                if (Utils.notBlankString(process.getReference())) {
                    subject += process.getReference();
                } else {
                    subject += type.getTitle() + "#" + process.getId();
                }

                NewsDAO newsDao = new NewsDAO(con);

                News news = new News();
                news.setCreateDate(new Date());
                news.setLifeTime(200);
                news.setReadTime(400);
                news.setUserId(event.getForm().getUserId());
                news.setPopup(command.startsWith("newsPopup"));

                Set<Integer> userIds = new HashSet<Integer>(process.getExecutorIds());

                news.setTitle(subject);

                text += "<br/><a href='#' onClick='$$.process.open( " + process.getId() + " )'>Перейти к процессу</a>";

                news.setDescription(text);

                userIds.remove(event.getForm().getUserId());

                if (userIds.size() > 0 && Utils.notBlankString(news.getTitle())) {
                    newsDao.updateNewsUsers(news, userIds);
                }
            } else if (command.startsWith("createProcessLink")) {
                int createTypeId = Utils.parseInt(StringUtils.substringAfter(command, ":"));
                if (createTypeId <= 0) {
                    throw new BGException("Не определён тип для создания");
                }

                ProcessLinkAction.linkProcessCreate(con, form, process, -1, null, createTypeId, "", -1);
            } else {
                EventProcessor.processEvent(new ProcessDoActionEvent(form, process, command), new SingleConnectionSet(con));
            }
        }
    }

    public static ProcessGroup getProcessGroup(Process process, Set<Integer> groupIds, int roleId) throws BGMessageException {
        ProcessGroup processGroup = null;

        Set<ProcessGroup> processGroupSet;
        if (roleId > -1) {
            processGroupSet = process.getGroups().stream()
                .filter(g -> g.getRoleId() == roleId)
                .collect(Collectors.toSet());
        } else {
            processGroupSet = process.getGroups();
        }

        for (ProcessGroup pg : processGroupSet) {
            if (groupIds.contains(pg.getGroupId())) {
                if (processGroup != null && processGroup.getGroupId() != pg.getGroupId()) {
                    throw new BGMessageException("Среди групп процесса, в этой роли, несколько соответствуют предложенному набору.");
                }
                processGroup = pg;
            }
        }

        if (processGroup == null) {
            throw new BGMessageException("Среди групп процесса, в этой роли, нет предложенных.");
        }

        return processGroup;
    }
}