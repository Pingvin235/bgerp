package ru.bgcrm.plugin.bgbilling.dao;

import java.sql.Connection;
import java.util.Date;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.exception.BGMessageExceptionTransparent;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.Log;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.dao.CustomerLinkDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractParamDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.util.Utils;

/**
 * Логика взаимодействия с сервером биллинга должна быть постепенно перенесена в
 * {@link ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO},
 * {@link ContractParamDAO}.
 *
 * Возможно, в этом классе останется что-то связанное одновременно с CRM и
 * биллингом, например, копирование параметров.
 */
@Deprecated
public class ContractDAO extends BillingDAO {
    private static final Log log = Log.getLog();

    public static class TaskStatus {
        public static final int OPENED = 0;
        public static final int ACCEPTED = 1;
        public static final int CLOSED = 2;
    }

    public ContractDAO(User user, String billingId) throws BGException {
        super(user, billingId);
    }

    public ContractDAO(User user, DBInfo dbInfo) throws BGException {
        super(user, dbInfo);
    }

    @Deprecated
    public void copyAddress(ParameterAddressValue address, int contractId, int toParamId) throws BGException {
        Request request = new Request();

        request.setModule("contract");
        request.setAction("UpdateAddressInfo");

        request.setAttribute("cid", contractId);
        request.setAttribute("pid", toParamId);

        request.setAttribute("hid", address.getHouseId());
        request.setAttribute("flat", address.getFlat());
        request.setAttribute("floor", address.getFloor() == -1 ? "" : address.getFloor());
        request.setAttribute("pod", address.getPod());
        request.setAttribute("room", address.getRoom());
        request.setAttribute("comment", address.getComment());

        transferData.postData(request, user);
    }

    public void copyParametersToBilling(Connection con, int customerId, int contractId, String title) throws Exception {
        Customer customer = new CustomerDAO(con).getCustomerById(customerId);

        String copyParamsMapping = dbInfo.getSetup().get("copyParamMapping", "");

        copyObjectParamsToContract(con, copyParamsMapping, customerId, contractId, customer);
    }

    public void copyObjectParamsToContract(Connection con, String copyParamsMapping, int objectId, int contractId,
            Customer customer) throws Exception {
        ParamValueDAO paramDAO = new ParamValueDAO(con);

        try {
            if (customer != null) {
                Request req = new Request();
                req.setModule("contract");
                req.setAction("UpdateContractTitleAndComment");
                req.setContractId(contractId);
                req.setAttribute("comment", customer.getTitle());

                transferData.postData(req, user);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BGMessageExceptionTransparent("Ошибка копирования имени контрагента: " + customer.getTitle() + "; "
                    + dbInfo.getTitle() + ", " + e.getMessage());
        }

        if (Utils.isBlankString(copyParamsMapping)) {
            return;
        }

        String[] params = copyParamsMapping.split(";");

        for (String pair : params) {
            try {
                String[] keyValue = pair.split(":");

                String fromParamId = keyValue[0].indexOf('[') == -1 ? keyValue[0]
                        : keyValue[0].substring(0, keyValue[0].indexOf('['));
                int toParamId = Utils.parseInt(keyValue[1].indexOf('[') == -1 ? keyValue[1]
                        : keyValue[1].substring(0, keyValue[1].indexOf('[')));

                Request request = new Request();
                request.setModule("contract");
                request.setAttribute("cid", contractId);
                request.setAttribute("pid", toParamId);

                if (fromParamId.equals("customerTitle")) {
                    if (customer != null) {
                        request.setAttribute("action", "UpdateParameterType1");
                        request.setAttribute("value", customer.getTitle());

                        transferData.postData(request, user);
                    }
                } else {
                    Parameter param = ParameterCache.getParameter(Integer.parseInt(fromParamId));
                    if (param == null) {
                        throw new BGMessageException(
                                "Ошибка при копировании параметра: параметр с ID=" + fromParamId + " не существует!");
                    }
                    String type = param.getType();

                    if (Parameter.TYPE_ADDRESS.equals(type)) {
                        SortedMap<Integer, ParameterAddressValue> values = paramDAO.getParamAddress(objectId,
                                Integer.parseInt(fromParamId));

                        if (values.size() > 0) {
                            ParameterAddressValue value = values.get(values.firstKey());

                            request.setAction("UpdateAddressInfo");

                            request.setAttribute("hid", value.getHouseId());
                            request.setAttribute("flat", value.getFlat());
                            request.setAttribute("floor", value.getFloor() == -1 ? "" : value.getFloor());
                            request.setAttribute("pod", value.getPod());
                            request.setAttribute("room", value.getRoom());
                            request.setAttribute("comment", value.getComment());

                            transferData.postData(request, user);
                        }
                    } else if (Parameter.TYPE_TEXT.equals(type)) {
                        String value = paramDAO.getParamText(objectId, Integer.parseInt(fromParamId));
                        if (Utils.notBlankString(value)) {
                            request.setAction("UpdateParameterType1");
                            request.setAttribute("value", value);

                            transferData.postData(request, user);
                        }
                    } else if (Parameter.TYPE_LIST.equals(type)) {
                        Set<Integer> listValue = paramDAO.getParamList(objectId, Integer.parseInt(fromParamId));
                        String fromValue = "-1";

                        if (listValue != null && listValue.size() > 0) {
                            // биллинг не поддерживает множественные значения списков, поэтому берем первый
                            fromValue = listValue.iterator().next().toString();

                            String toValue = null;
                            // преобразование по карте соответствий
                            if (keyValue[0].indexOf('[') > 0) {
                                String[] fromVals = keyValue[0]
                                        .substring(keyValue[0].indexOf('[') + 1, keyValue[0].indexOf(']')).split(",");
                                String[] toVals = keyValue[1]
                                        .substring(keyValue[1].indexOf('[') + 1, keyValue[1].indexOf(']')).split(",");

                                for (int i = 0; i < fromVals.length; i++) {
                                    if (fromVals[i].equals(fromValue)) {
                                        toValue = toVals[i];
                                        break;
                                    }
                                }
                            } else {
                                toValue = fromValue;
                            }

                            if (Utils.notBlankString(toValue)) {
                                new ContractParamDAO(user, dbInfo).updateListParameter(contractId, toParamId, toValue);
                            }
                        }
                    } else if (Parameter.TYPE_PHONE.equals(type)) {
                        ParameterPhoneValue value = paramDAO.getParamPhone(objectId, Integer.parseInt(fromParamId));
                        if (value != null) {
                            new ContractParamDAO(user, dbInfo).updatePhoneParameter(contractId, toParamId, value);
                        }
                    } else if (Parameter.TYPE_DATE.equals(type)) {
                        Date value = paramDAO.getParamDate(objectId, Integer.parseInt(fromParamId));
                        if (value != null) {
                            new ContractParamDAO(user, dbInfo).updateDateParameter(contractId, toParamId, value);
                        }
                    } else if (Parameter.TYPE_EMAIL.equals(type)) {
                        SortedMap<Integer, ParameterEmailValue> value = paramDAO.getParamEmail(objectId,
                                Integer.parseInt(fromParamId));
                        if (value.size() > 0) {
                            new ContractParamDAO(user, dbInfo).updateEmailParameter(contractId, toParamId,
                                    value.values());
                        }
                    }
                }
            } catch (BGException e) {
                log.error(e.getMessage(), e);
                throw new BGMessageException(
                        "Ошибка при копировании параметра в биллинг! [" + pair + "] " + e.getMessage());
            }
        }
    }

    public static void copyParametersToAllContracts(Connection con, User user, int customerId) throws Exception {
        CustomerLinkDAO linkDao = new CustomerLinkDAO(con);
        for (CommonObjectLink link : linkDao.getObjectLinksWithType(customerId, Contract.OBJECT_TYPE + "%")) {
            String billingId = StringUtils.substringAfter(link.getLinkObjectType(), ":");
            new ContractDAO(user, billingId).copyParametersToBilling(con, customerId, link.getLinkObjectId(),
                    link.getLinkObjectTitle());
        }
    }
}