package ru.bgcrm.model.process.wizard;

import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.param.ParameterValuePair;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.expression.ParamValueFunction;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

public class FillParamsStepData extends StepData<FillParamsStep> {
    private List<ParameterValuePair> values;
    private int objectId = -1;

    public FillParamsStepData(FillParamsStep step, WizardData data) {
        super(step, data);
    }

    @Override
    public boolean isFilled(DynActionForm form, Connection connection) throws Exception {
        boolean filled = true;
        objectId = getObjectId(connection);

        ParamValueDAO paramValueDao = new ParamValueDAO(connection);
        values = paramValueDao.loadParameters(step.getParamList(), objectId, false);

        if (step.getCheckParamIds().isEmpty()) {
            for (ParameterValuePair pair : values) {
                filled = pair.getValue() != null;
                if (filled) {
                    break;
                }
            }
        } else {
            for (ParameterValuePair pair : values) {
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
            Set<Entry<Integer, ConfigMap>> showParamSet = processType.getProperties().getConfigMap()
                    .subIndexed("showParam.").entrySet();

            Set<Integer> hideParamIds = new HashSet<>();
            // показывает параметры процесса только в том случае, если выполняется JEXL выражение: showParam.<paramId>.checkExpression=<expr>
            for (Entry<Integer, ConfigMap> entry : showParamSet) {
                String expression = entry.getValue().get(Expression.CHECK_EXPRESSION_CONFIG_KEY);

                Map<String, Object> context = new HashMap<>();
                context.put(User.OBJECT_TYPE, data.getUser());
                context.put(Process.OBJECT_TYPE, process);
                context.put(Process.OBJECT_TYPE + ParamValueFunction.PARAM_FUNCTION_SUFFIX,
                        new ParamValueFunction(connection, process.getId()));

                // TODO: Use DefaultProcessChangeListener#initExpression()
                if (Utils.notBlankString(expression) && !(new Expression(context).check(expression))) {
                    hideParamIds.add(entry.getKey());
                }
            }

            Iterator<ParameterValuePair> iterator = values.iterator();

            while (iterator.hasNext()) {
                ParameterValuePair parameterValuePair = iterator.next();
                if (hideParamIds.contains(parameterValuePair.getParameter().getId())) {
                    iterator.remove();
                }
            }
        }

        return filled;
    }

    public List<ParameterValuePair> getValues() {
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
        List<CommonObjectLink> processCustomerlinkList = processLinkDAO
                .getObjectLinksWithType(data.getProcess().getId(), Customer.OBJECT_TYPE);

        if (processCustomerlinkList.size() > 0) {
            customerId = processCustomerlinkList.get(0).getLinkObjectId();
        }

        return customerId;
    }
}