package ru.bgcrm.model.process.wizard;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.bean.annotation.Bean;
import org.bgerp.cache.ParameterCache;
import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.param.Parameter;
import org.bgerp.model.param.ParameterValue;
import org.bgerp.util.Log;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.expression.ProcessExpressionObject;
import ru.bgcrm.dao.expression.ProcessParamExpressionObject;
import ru.bgcrm.dao.expression.UserExpressionObject;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.wizard.base.Step;
import ru.bgcrm.model.process.wizard.base.StepData;
import ru.bgcrm.model.process.wizard.base.WizardData;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

@Bean
public class FillParamsStep extends Step {
    private static final Log log = Log.getLog();

    private final String type;
    private final Set<Integer> checkParamIds;

    public FillParamsStep(ConfigMap config) {
        super(config);
        type = config.get("object");
        checkParamIds = Utils.toIntegerSet(config.get("checkParamIds", ""));
    }

    public String getType() {
        return type;
    }

    public Set<Integer> getCheckParamIds() {
        return checkParamIds;
    }

    public List<Parameter> getParamList() {
        List<Parameter> paramList = new ArrayList<>();

        // сохранение списка параметров убрано, чтобы из кэша брались актуальные параметры
        for (int paramId : Utils.toIntegerList(config.get("parameterIds"))) {
            Parameter param = ParameterCache.getParameter(paramId);
            if (param == null) {
                log.error("Not found param: " + paramId);
                continue;
            }
            paramList.add(param);
        }

        return paramList;
    }

    @Override
    public String getJsp() {
        return PATH_JSP + "/step_fill_params.jsp";
    }

    @Override
    public Data data(WizardData data) {
        return new Data(this, data);
    }

    public static class Data extends StepData<FillParamsStep> {
        private List<ParameterValue> values;
        private int objectId = -1;

        public Data(FillParamsStep step, WizardData data) {
            super(step, data);
        }

        @Override
        public boolean isFilled(DynActionForm form, Connection connection) throws Exception {
            boolean filled = true;
            objectId = getObjectId(connection);

            ParamValueDAO paramValueDao = new ParamValueDAO(connection);
            values = paramValueDao.loadParameters(step.getParamList(), objectId, false);

            if (step.getCheckParamIds().isEmpty()) {
                for (ParameterValue pair : values) {
                    filled = pair.getValue() != null;
                    if (filled) {
                        break;
                    }
                }
            } else {
                for (ParameterValue pair : values) {
                    if (step.getCheckParamIds().contains(pair.getParameter().getId())) {
                        if (pair.getValue() == null) {
                            filled = false;
                            break;
                        }
                    }
                }
            }

            // Если в шаге обрабатываются параметры процесса, то проверим их с JEXL выражением
            if (!"linkedCustomer".equals(step.getType())) {
                Process process = data.getProcess();
                ProcessType processType = ProcessTypeCache.getProcessType(process.getTypeId());
                Set<Entry<Integer, ConfigMap>> showParamSet = processType.getProperties().getConfigMap().subIndexed("showParam.").entrySet();

                Set<Integer> hideParamIds = new HashSet<>();
                // показывает параметры процесса только в том случае, если выполняется JEXL выражение: showParam.<paramId>.checkExpression=<expr>
                for (Entry<Integer, ConfigMap> entry : showParamSet) {
                    String expression = entry.getValue().get(Expression.CHECK_EXPRESSION_CONFIG_KEY);

                    Map<String, Object> context = new HashMap<>();
                    new UserExpressionObject(data.getUser()).toContext(context);
                    new ProcessExpressionObject(process).toContext(context);
                    new ProcessParamExpressionObject(connection, process.getId()).toContext(context);

                    // TODO: Use DefaultProcessChangeListener#initExpression()
                    if (Utils.notBlankString(expression) && !(new Expression(context).executeCheck(expression))) {
                        hideParamIds.add(entry.getKey());
                    }
                }

                Iterator<ParameterValue> iterator = values.iterator();

                while (iterator.hasNext()) {
                    ParameterValue parameterValuePair = iterator.next();
                    if (hideParamIds.contains(parameterValuePair.getParameter().getId())) {
                        iterator.remove();
                    }
                }
            }

            return filled;
        }

        public List<ParameterValue> getValues() {
            return values;
        }

        public int getObjectId() {
            return objectId;
        }

        // в id возвращается код объекта
        private int getObjectId(Connection connection) {
            int objectId = 0;

            if ("linkedCustomer".equals(step.getType())) {
                //TODO: Может сделать потом джойн.
                if (data.getLinkedCustomer() != null) {
                    objectId = data.getLinkedCustomer().getId();
                } else {
                    objectId = getLinkedCustomerId(connection);
                }
            } else {
                objectId = data.getProcess().getId();
            }

            return objectId;
        }

        /**
         * Метод возвращает идентификатор контрагента, ранее привязанного
         * к процессу через поиск связей процесс-контрагент базе данных.
         * @param connection
         * @return
         */
        private int getLinkedCustomerId(Connection connection) {
            int customerId = 0;
            ProcessLinkDAO processLinkDAO = new ProcessLinkDAO(connection);
            List<CommonObjectLink> processCustomerlinkList = processLinkDAO.getObjectLinksWithType(data.getProcess().getId(), Customer.OBJECT_TYPE);

            if (processCustomerlinkList.size() > 0) {
                customerId = processCustomerlinkList.get(0).getLinkObjectId();
            }

            return customerId;
        }
    }
}
