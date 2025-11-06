package ru.bgcrm.event.listener;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.cfg.bean.annotation.Bean;
import org.bgerp.app.exception.BGException;
import org.bgerp.cache.ParameterCache;
import org.bgerp.dao.expression.Expression;
import org.bgerp.dao.expression.ProcessChangeExpressionObject;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.Dynamic;
import org.bgerp.util.Log;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.event.ProcessMarkedActionEvent;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.StatusChange;
import ru.bgcrm.model.process.queue.Processor;
import ru.bgcrm.plugin.document.docgen.CommonDocumentGenerator;
import ru.bgcrm.plugin.document.event.DocumentGenerateEvent;
import ru.bgcrm.plugin.document.model.Pattern;
import ru.bgcrm.struts.action.ProcessAction;
import ru.bgcrm.struts.action.ProcessCommandExecutor;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Bean
public class DefaultMarkedProcessor extends Processor {
    private static final Log log = Log.getLog();

    private static final String COMMAND_SET_STATUS = ProcessCommandExecutor.COMMAND_SET_STATUS;
    private static final String COMMAND_ADD_EXECUTORS = ProcessCommandExecutor.COMMAND_ADD_EXECUTORS;
    private static final String COMMAND_SET_PARAM = ProcessCommandExecutor.COMMAND_SET_PARAM;
    private static final String COMMAND_PRINT = "print";

    public static class Config extends org.bgerp.app.cfg.Config {
        private final List<Command> commandList = new ArrayList<>();
        private final String doExpression;

        public Config(ConfigMap config) {
            super(null);

            String commands = config.get("commands", "");
            for (String command : commands.split(";"))
                commandList.add(new Command(command));

            doExpression = config.get(Expression.DO_EXPRESSION_CONFIG_KEY);
        }

        @Dynamic
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

    // end of static part

    private final ConfigMap configMap;

    public DefaultMarkedProcessor(int id, ConfigMap configMap) {
        super(id, configMap);
        this.configMap = configMap;
    }

    @Override
    public String getJsp() {
        return "/WEB-INF/jspf/user/process/queue/default_marked_processor.jsp";
    }

    @Override
    public void process(ProcessMarkedActionEvent e, ConnectionSet conSet) throws Exception {
        DynActionForm form = e.getForm();

        Config config = configMap.getConfig(Config.class);
        Command firstCommand = Utils.getFirst(config.commandList);

        // print command can be only alone
        if (config.commandList.size() == 1 && firstCommand.getName().equals(COMMAND_PRINT)) {
            ru.bgcrm.plugin.document.Config documentConfig = Setup.getSetup().getConfig(ru.bgcrm.plugin.document.Config.class);

            Pattern pattern = documentConfig.getPattern("processQueue", firstCommand.getPatternId());
            if (pattern == null) {
                throw new BGException("Pattern not found.");
            }

            HttpServletResponse response = e.getForm().getHttpResponse();

            CommonDocumentGenerator generator = new CommonDocumentGenerator();

            OutputStream out = e.getForm().getHttpResponseOutputStream();

            // debug mode
            if (DocumentGenerateEvent.isDebug(e.getForm())) {
                response.setContentType("text/plain; charset=" + StandardCharsets.UTF_8.name());

                DocumentGenerateEvent docGenEvent = new DocumentGenerateEvent(e.getForm(), pattern, Process.OBJECT_TYPE, e.getProcessIds());
                generator.notify(docGenEvent, conSet);
                out.write(docGenEvent.getResultBytes());

                out.flush();
            } else if (pattern.getType() == Pattern.TYPE_PDF_FORM) {
                Utils.setFileNameHeaders(response, pattern.getDocumentTitle());

                Document document = new Document();
                PdfCopy copy = new PdfCopy(document, out);
                document.open();

                for (Integer processId : e.getProcessIds()) {
                    DocumentGenerateEvent docGenEvent = new DocumentGenerateEvent(e.getForm(), pattern,
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

                DocumentGenerateEvent docGenEvent = new DocumentGenerateEvent(e.getForm(), pattern, Process.OBJECT_TYPE, e.getProcessIds());
                generator.notify(docGenEvent, conSet);

                out.write(docGenEvent.getResultBytes());

                out.flush();
            }

            e.setStreamResponse(true);
        } else {
            Connection con = conSet.getConnection();

            for (int processId : e.getProcessIds()) {
                Process process = new ProcessDAO(conSet.getSlaveConnection()).getProcessOrThrow(processId);

                for (Command command : config.commandList) {
                    String name = command.getName();
                    if (name.equals(COMMAND_SET_STATUS)) {
                        int statusId = e.getForm().getParamInt("statusId");
                        if (statusId != process.getStatusId()) {
                            StatusChange change = new StatusChange();
                            change.setDate(new Date());
                            change.setProcessId(process.getId());
                            change.setUserId(form.getUserId());
                            change.setStatusId(statusId);
                            change.setComment(this.getClass().getSimpleName());

                            ProcessAction.processStatusUpdate(form, con, process, change);
                        }
                    } else if (name.equals(COMMAND_ADD_EXECUTORS)) {
                        new ProcessChangeExpressionObject(con, form, process).addExecutors(form.getParamValues("executor"));
                    } else if (name.equals(COMMAND_SET_PARAM)) {
                        final Parameter param = ParameterCache.getParameter(command.paramId);
                        final var paramType = param.getTypeType();
                        final String paramName = "param" + command.paramId;

                        log.debug("Set process ID: {}, param type: {}, ID: {}", processId, paramType, command.paramId);

                        var dao = new ParamValueDAO(con);
                        if (Parameter.Type.DATE == paramType)
                            dao.updateParamDate(processId, command.paramId, form.getParamDate(paramName));
                        else if (Parameter.Type.DATETIME == paramType)
                            dao.updateParamDateTime(processId, command.paramId, form.getParamDateTime(paramName, param.getDateParamFormat()));
                        else if (Parameter.Type.LIST == paramType) {
                            dao.updateParamList(processId, command.paramId, form.getParamValues(paramName));
                        }
                    }
                }

                if (Utils.notBlankString(config.doExpression)) {
                    log.debug("Executing expression: {}", config.doExpression);
                    Expression.init(conSet, e, process).execute(config.doExpression);
                }

                conSet.commit();
            }
        }
    }
}
