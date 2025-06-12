package ru.bgcrm.plugin.bgbilling;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.cache.ParameterCache;
import org.bgerp.dao.expression.ExpressionContextAccessingObject;
import org.bgerp.dao.expression.ProcessExpressionObject;
import org.bgerp.dao.expression.UserExpressionObject;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.Log;

import javassist.NotFoundException;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractParamDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class ExpressionObject extends ExpressionContextAccessingObject {
    private static final Log log = Log.getLog();

    ExpressionObject() {}

    @Override
    public void toContext(Map<String, Object> context) {
        super.toContext(context);
        context.put(Plugin.ID, this);
    }

    /**
     * Copy parameter value from link contract to the process
     * @param contractParamId billing contract parameter ID, must has a same type as {@param processParamId}
     * @param contractParamType contract parameter type, empty string if the type is the same with {@param processParamId}
     * @param processParamId process parameter ID, also defines a type, supported parameter types:
     * <li>{@code address} from {@code address} type of contract parameter
     * <li>{@code date} from {@code date} type of contract parameter
     * <li>{@code blob}, {@code text} from {@code text}, {@code address}, {@code phone} types of contract parameter
     * <li>{@code phone} from {@code phone} type of contract parameter
     */
    public void cp(int contractParamId, String contractParamType, int processParamId) throws Exception {
        Process process = (Process)context.get(ProcessExpressionObject.KEY);

        Parameter processParam = ParameterCache.getParameter(processParamId);
        if (processParam == null)
            throw new NotFoundException("Not found parameter with ID: " + processParamId);

        ConnectionSet conSet = (ConnectionSet)context.get(ConnectionSet.KEY);

        var linkDao = new ProcessLinkDAO(conSet.getSlaveConnection());
        var contractLink = Utils.getFirst(linkDao.getObjectLinksWithType(process.getId(), Contract.OBJECT_TYPE + "%"));
        if (contractLink == null) {
            log.debug("Not found link BGBilling contract for process: {}", process.getId());
            return;
        }

        int contractId = contractLink.getLinkObjectId();
        String billingId = StringUtils.substringAfter(contractLink.getLinkObjectType(), ":");

        User user = (User)context.get(UserExpressionObject.KEY);

        var contractParamDao = new ContractParamDAO(user, billingId);
        var paramDao = new ParamValueDAO(conSet.getConnection());

        switch (processParam.getTypeType()) {
            case ADDRESS -> {
                var value = contractParamDao.getAddressParam(contractId, contractParamId);
                if (value != null)
                    paramDao.updateParamAddress(process.getId(), processParamId, 0, value.toParameterAddressValue(conSet.getSlaveConnection()));
            }
            case DATE -> {
                var value = contractParamDao.getDateParam(contractId, contractParamId);
                if (value != null)
                    paramDao.updateParamDate(process.getId(), processParamId, value);
            }
            case BLOB, TEXT -> {
                String value = null;

                if (Utils.isBlankString(contractParamType) || Parameter.TYPE_TEXT.equals(contractParamType))
                    value = contractParamDao.getTextParam(contractId, contractParamId);
                else if (Parameter.TYPE_ADDRESS.equals(contractParamType))
                    value = contractParamDao.getAddressParam(contractId, contractParamId).toParameterAddressValue(conSet.getSlaveConnection()).getValue();
                else if (Parameter.TYPE_PHONE.equals(contractParamType))
                    value = contractParamDao.getPhoneParam(contractId, contractParamId).toString();
                else
                    throwException(contractParamId, contractParamType, processParamId, processParam);

                if (Utils.notBlankString(value)) {
                    if (processParam.getTypeType() == Parameter.Type.BLOB)
                        paramDao.updateParamBlob(process.getId(), processParamId, value);
                    else
                        paramDao.updateParamText(process.getId(), processParamId, value);
                }
            }
            case PHONE -> {
                var value = contractParamDao.getPhoneParam(contractId, contractParamId);
                paramDao.updateParamPhone(process.getId(), processParamId, value);
            }
            default -> {
                throwException(contractParamId, contractParamType, processParamId, processParam);
            }
        }
    }

    private void throwException(int contractParamId, String contractParamType, int processParamId, Parameter processParam) {
        throw new IllegalArgumentException(
                Log.format("Can't copy contract parameter with ID {} and type {} to process parameter with ID {} and type {}", contractParamId,
                        contractParamType, processParamId, processParam.getType()));
    }
}
