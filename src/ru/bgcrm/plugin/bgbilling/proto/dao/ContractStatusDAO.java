package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bgerp.util.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fasterxml.jackson.databind.JsonNode;

import ru.bgcrm.model.Page;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.directory.UserInfoDirectory;
import ru.bgcrm.plugin.bgbilling.proto.model.status.ContractStatus;
import ru.bgcrm.plugin.bgbilling.proto.model.status.ContractStatusLogItem;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class ContractStatusDAO extends BillingDAO {
    private static final String CONTRACT_STATUS_MODULE_ID = "ru.bitel.bgbilling.kernel.contract.status";
    private static final String MODULE = "contract.status";

    public ContractStatusDAO(User user, String billingId) {
        super(user, billingId);
    }

    public ContractStatusDAO(User user, DBInfo dbInfo) {
        super(user, dbInfo);
    }

    /**
     * Возвращает список статусов договора с периодами.
     * @param contractId
     * @return
     */
    public List<ContractStatus> statusList(int contractId, Map<Integer, String> statusTitleMap) {
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.api", "ContractStatusService", "contractStatusList");
            req.setParam("contractId", contractId);
            JsonNode ret = transferData.postDataReturn(req, user);
            List<ContractStatus> result = readJsonValue(ret.traverse(), jsonTypeFactory.constructCollectionType(List.class, ContractStatus.class));
            result.forEach(item -> item.setStatus(statusTitleMap.getOrDefault(Utils.parseInt(item.getStatus()), "??? " + item.getStatus())));
            // reverse sort by IDs
            result.sort((item1, item2) -> item2.getId() - item1.getId());
            return result;
        } else {
            List<ContractStatus> statusList = new ArrayList<>();

            Request request = new Request();
            request.setModule(MODULE);
            request.setAction("ContractStatusTable");
            request.setContractId(contractId);

            Document doc = transferData.postData(request, user);
            for (Element element : XMLUtils.selectElements(doc, "/data/table/data/row")) {
                ContractStatus status = new ContractStatus();
                loadContractStatusLogItem(element, status);
                statusList.add(status);
            }

            return statusList;
        }
    }

    /**
     * Лог изменений статуса договора.
     * @param contractId
     * @return
     *
     */
    public List<ContractStatusLogItem> statusLog(int contractId, Map<Integer, String> statusTitleMap) {
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.api", "ContractStatusService", "contractStatusLogSearch");
            req.setParam("contractId", contractId);
            req.setParam("objectId", 0);
            req.setParam("page", new Page());
            JsonNode ret = transferData.postDataReturn(req, user);
            List<ContractStatusLogItem> result = readJsonValue(ret.findValue("list").traverse(), jsonTypeFactory.constructCollectionType(List.class, ContractStatusLogItem.class));
            UserInfoDirectory directory = dbInfo.directory(UserInfoDirectory.class);
            result.forEach(item -> item.setUser(directory.get(user, item.getUserId()).getName()));
            // reverse sort by IDs
            result.sort((item1, item2) -> item2.getId() - item1.getId());
            return result;
        } else {
            List<ContractStatusLogItem> result = new ArrayList<>();

            Request request = new Request();
            request.setModule(MODULE);
            request.setAction("ContractStatusLog");
            request.setContractId(contractId);

            Document doc = transferData.postData(request, user);

            for (Element element : XMLUtils.selectElements(doc, "/data/table/data/row")) {
                ContractStatusLogItem status = new ContractStatusLogItem();
                loadContractStatusLogItem(element, status);
                status.setDate(TimeUtils.parse(element.getAttribute("date"), TimeUtils.PATTERN_DDMMYYYYHHMMSS));
                status.setUser(element.getAttribute("user"));
                result.add(status);
            }

            return result;
        }
    }

    private void loadContractStatusLogItem(Element element, ContractStatus status) {
        status.setId(Utils.parseInt(element.getAttribute("id")));
        status.setComment(element.getAttribute("comment"));
        status.setStatus(element.getAttribute("status"));
        TimeUtils.parsePeriod(element.getAttribute("period"), status);
    }

    public void updateStatus(int contractId, int statusId, Date dateFrom, Date dateTo, String comment) {
        if (dbInfo.versionCompare("8.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(ContractDAO.KERNEL_CONTRACT_API, "ContractStatusService", "changeContractStatus");
            if (dbInfo.versionCompare("9.2") >= 0) {
                req.setParam("contractId", Collections.singletonList(contractId));
                req.setParam("objectId", 0);
            } else {
                req.setParam("cid", Collections.singletonList(contractId));
            }
            req.setParam("statusId", statusId);
            req.setParam("dateFrom", dateFrom);
            req.setParam("dateTo", dateTo);
            req.setParam("comment", comment);
            req.setParam("confirmChecked", true);

            transferData.postDataReturn(req, user);
        } else {
            RequestJsonRpc req = new RequestJsonRpc(CONTRACT_STATUS_MODULE_ID, "ContractStatusMonitorService", "changeContractStatus");
            req.setParam("cid", Collections.singletonList(contractId));
            req.setParam("statusId", statusId);
            req.setParam("dateFrom", dateFrom);
            req.setParam("dateTo", dateTo);
            req.setParam("comment", comment);
            req.setParam("confirmChecked", true);

            transferData.postDataReturn(req, user);
        }
    }
}