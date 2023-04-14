package ru.bgcrm.event.listener;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.bgerp.util.Log;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.event.Event;
import ru.bgcrm.event.ProcessMarkedActionEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.plugin.document.docgen.CommonDocumentGenerator;
import ru.bgcrm.plugin.document.event.DocumentGenerateEvent;
import ru.bgcrm.plugin.document.model.Pattern;
import ru.bgcrm.struts.action.ProcessCommandExecutor;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class DefaultMarkedProcessor implements EventListener<Event> {
    private static final Log log = Log.getLog();

    private static final String COMMAND_SET_STATUS = ProcessCommandExecutor.COMMAND_SET_STATUS;
    private static final String COMMAND_ADD_GROUPS = ProcessCommandExecutor.COMMAND_ADD_GROUPS;
    private static final String COMMAND_ADD_EXECUTORS = ProcessCommandExecutor.COMMAND_ADD_EXECUTORS;
    private static final String COMMAND_SET_PARAM = ProcessCommandExecutor.COMMAND_SET_PARAM;
    private static final String COMMAND_PRINT = "print";

    public static class Config extends ru.bgcrm.util.Config {
        private final List<Command> commandList = new ArrayList<Command>();
        private final String doExpression;

        public Config(ParameterMap config) {
            super(null);
            for (String command : config.get("commands", "").split(";")) {
                commandList.add(new Command(command));
            }
            doExpression = config.get(Expression.DO_EXPRESSION_CONFIG_KEY);
        }

        // called from JSP
        public List<Command> getCommandList() {
            return commandList;
        }
    }

    public static class Command {
        private final String name;
        // для setStatus - коды статусов, для setExecutors - коды групп исполнителей
        private List<Integer> allowedIds;
        private int patternId;
        private int paramId;

        public Command(String command) {
            String[] tokens = command.split(":");

            this.name = tokens[0];
            if (tokens.length > 1) {
                if (COMMAND_SET_STATUS.equals(name) || COMMAND_ADD_EXECUTORS.equals(name)) {
                    allowedIds = Utils.toIntegerList(tokens[1]);
                } else if (COMMAND_PRINT.equals(name)) {
                    patternId = Utils.parseInt(tokens[1]);
                } else if (COMMAND_SET_PARAM.equals(name)) {
                    paramId = Utils.parseInt(tokens[1]);
                }
            }
        }

        public String getName() {
            return name;
        }

        public List<Integer> getAllowedIds() {
            return allowedIds;
        }

        public int getPatternId() {
            return patternId;
        }

        public int getParamId() {
            return paramId;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void notify(Event e, ConnectionSet conSet) throws BGException {
        if (!(e instanceof ProcessMarkedActionEvent)) {
            return;
        }

        ProcessMarkedActionEvent event = (ProcessMarkedActionEvent) e;

        Connection con = conSet.getConnection();

        ProcessDAO processDAO = new ProcessDAO(conSet.getConnection());

        Config config = event.getProcessor().getConfigMap().getConfig(Config.class);

        Command firstCommand = Utils.getFirst(config.commandList);

        // команда печати может стоять только одна
        if (config.commandList.size() == 1 && firstCommand.getName().equals(COMMAND_PRINT)) {
            ru.bgcrm.plugin.document.Config documentConfig = Setup.getSetup()
                    .getConfig(ru.bgcrm.plugin.document.Config.class);

            Pattern pattern = documentConfig.getPattern("processQueue", firstCommand.getPatternId());
            if (pattern == null) {
                throw new BGException("Pattern not found.");
            }

            HttpServletResponse response = event.getForm().getHttpResponse();

            CommonDocumentGenerator generator = new CommonDocumentGenerator();

            try {
                OutputStream out = event.getForm().getHttpResponseOutputStream();

                // режим отладки
                if (DocumentGenerateEvent.isDebug(event.getForm())) {
                    response.setContentType("text/plain; charset=" + StandardCharsets.UTF_8.name());

                    DocumentGenerateEvent docGenEvent = new DocumentGenerateEvent(event.getForm(), pattern,
                            Process.OBJECT_TYPE, event.getProcessIds());
                    generator.notify(docGenEvent, conSet);
                    out.write(docGenEvent.getResultBytes());

                    out.flush();
                } else if (pattern.getType() == Pattern.TYPE_PDF_FORM) {
                    Utils.setFileNameHeaders(response, pattern.getDocumentTitle());

                    Document document = new Document();

                    PdfCopy copy = new PdfCopy(document, out);

                    document.open();

                    for (Integer processId : event.getProcessIds()) {
                        DocumentGenerateEvent docGenEvent = new DocumentGenerateEvent(event.getForm(), pattern,
                                Process.OBJECT_TYPE, Collections.singletonList(processId));
                        generator.notify(docGenEvent, conSet);

                        PdfReader reader = new PdfReader(docGenEvent.getResultBytes());

                        int n = reader.getNumberOfPages();
                        for (int page = 0; page < n;) {
                            copy.addPage(copy.getImportedPage(reader, ++page));
                        }

                        copy.freeReader(reader);
                        reader.close();
                    }

                    document.close();
                } else if (pattern.getType() == Pattern.TYPE_XSLT_HTML || pattern.getType() == Pattern.TYPE_JSP_HTML) {
                    response.setContentType("text/html; charset=" + StandardCharsets.UTF_8.name());

                    DocumentGenerateEvent docGenEvent = new DocumentGenerateEvent(event.getForm(), pattern,
                            Process.OBJECT_TYPE, event.getProcessIds());
                    generator.notify(docGenEvent, conSet);

                    out.write(docGenEvent.getResultBytes());

                    out.flush();
                }

                event.setStreamResponse(true);
            } catch (Exception ex) {
                throw new BGException(ex);
            }
        } else {
            try {
                for (int processId : event.getProcessIds()) {
                    Process process = processDAO.getProcess(processId);

                    // набор стандартных команд по обработке процесса
                    List<String> commandList = new ArrayList<String>();

                    for (Command command : config.commandList) {
                        String name = command.getName();
                        if (name.equals(COMMAND_SET_STATUS)) {
                            int statusId = event.getForm().getParamInt("statusId");
                            if (statusId != process.getStatusId())
                                commandList.add(COMMAND_SET_STATUS + ":" + statusId);
                        } else if (name.equals(COMMAND_ADD_EXECUTORS)) {
                            Collection<Integer> groupIds = event.getForm().getSelectedValues("group");
                            groupIds = CollectionUtils.subtract(groupIds, process.getGroupIds());
                            if (!CollectionUtils.isEmpty(groupIds) )
                                commandList.add(COMMAND_ADD_GROUPS + ":" + Utils.toString(groupIds));

                            String executorIds = Utils.toString(event.getForm().getSelectedValues("executor"));
                            if (Utils.notBlankString(executorIds)) {
                                commandList.add(COMMAND_ADD_EXECUTORS + ":" + executorIds);
                            }
                        } else if (name.equals(COMMAND_SET_PARAM)) {
                            String value = event.getForm().getParam("param" + command.paramId);
                            if (Utils.notBlankString(value)) {
                                commandList.add(COMMAND_SET_PARAM + ":" + command.paramId + ":" + value);
                            }
                        }
                    }


                    ProcessCommandExecutor.processDoCommands(con, event.getForm(), process, null, commandList);

                    if (Utils.notBlankString(config.doExpression)) {
                        log.debug("Executing expression: {}", config.doExpression);
                        Expression.init(conSet, event, process).executeScript(config.doExpression);
                    }

                    conSet.commit();
                }
            } catch (Exception ex) {
                throw new BGException(ex);
            }
        }
    }
}
