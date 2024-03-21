package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.util.ArrayList;
import java.util.List;

import org.bgerp.app.exception.BGException;
import org.bgerp.model.Pageable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.JsonNode;

import ru.bgcrm.model.Page;
import ru.bgcrm.model.Period;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.script.ContractScript;
import ru.bgcrm.plugin.bgbilling.proto.model.script.ContractScriptLogItem;
import ru.bgcrm.util.Utils;

public class ContractScriptDAO extends BillingDAO {
    private static final String CONTRACT_MODULE_ID = "contract";

    public ContractScriptDAO(User user, DBInfo dbInfo) throws BGException {
        super(user, dbInfo);
    }

    public ContractScriptDAO(User user, String billingId) throws BGException {
        super(user, billingId);
    }

    public List<ContractScript> contractScriptList(int contractId) throws BGException {
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.script", "ContractScriptService", "contractScriptList");
            req.setParamContractId(contractId);
            JsonNode ret = transferData.postDataReturn(req, user);
            return readJsonValue(ret.traverse(), jsonTypeFactory.constructCollectionType(List.class, ContractScript.class));
        } else {
            List<ContractScript> scriptList = new ArrayList<>();

            Request request = new Request();
            request.setModule(CONTRACT_MODULE_ID);
            request.setAction("ContractScriptTable");
            request.setContractId(contractId);

            Document document = transferData.postData(request, user);

            Element dataElement = document.getDocumentElement();
            NodeList nodeList = dataElement.getElementsByTagName("row");

            for (int index = 0; index < nodeList.getLength(); index++)
                scriptList.add(contractScriptFromElement((Element) nodeList.item(index)));

            return scriptList;
        }
    }

    public ContractScript contractScriptGet(int scriptId) throws BGException {
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.script",
                    "ContractScriptService",
                    "contractScriptGet");
            req.setParam("id", scriptId);
            JsonNode ret = transferData.postDataReturn(req, user);
            return jsonMapper.convertValue(ret, ContractScript.class);
        } else {
            Request request = new Request();
            request.setModule(CONTRACT_MODULE_ID);
            request.setAction("GetContractScript");
            request.setAttribute("id", scriptId);

            Document document = transferData.postData(request, user);

            Element dataElement = document.getDocumentElement();
            NodeList nodeList = dataElement.getElementsByTagName("contract_script");

            if (nodeList.getLength() > 0)
                return contractScriptFromElement((Element) nodeList.item(0));

            return null;
        }
    }

    private ContractScript contractScriptFromElement(Element rowElement) {
        ContractScript script = new ContractScript();
        script.setId(Utils.parseInt(rowElement.getAttribute("id")));
        script.setScript(rowElement.getAttribute("script"));
        script.setComment(rowElement.getAttribute("comment"));
        script.setPeriod(new Period( rowElement.getAttribute("period")));
        return script;
    }

    public void contractScriptLogList(Pageable<ContractScriptLogItem> result, int contractId, String dateFrom, String dateTo) throws BGException {
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.script",
                    "ContractScriptService",
                    "getScriptLogList");
            req.setParamContractId(contractId);
            req.setParam("period", new Period(dateFrom + "-" + dateTo));
            req.setParam("title", null);
            req.setParam("pageHolder", result.getPage());

            JsonNode ret = transferData.postData(req, user);

            List<ContractScriptLogItem> list = readJsonValue(ret.get("return").traverse(),
                    jsonTypeFactory.constructCollectionType(List.class, ContractScriptLogItem.class));
            result.getList().addAll(list);
            Page page = jsonMapper.convertValue(ret.findValue("pageHolder"), Page.class);
            result.getPage().setData(page);
        } else {
            int pageIndex = result.getPage().getPageIndex();
            int pageSize = result.getPage().getPageSize();

            Request request = new Request();
            request.setModule(CONTRACT_MODULE_ID);
            request.setAction("ContractScriptLog");
            request.setContractId(contractId);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("pageIndex", pageIndex);

            if (Utils.notBlankString(dateFrom)) {
                request.setAttribute("start", dateFrom);
            }
            if (Utils.notBlankString(dateTo)) {
                request.setAttribute("end", dateTo);
            }

            Document document = transferData.postData(request, user);

            Element dataElement = document.getDocumentElement();
            NodeList nodeList = dataElement.getElementsByTagName("row");
            List<ContractScriptLogItem> logList = result.getList();

            for (int index = 0; index < nodeList.getLength(); index++) {
                Element rowElement = (Element) nodeList.item(index);
                ContractScriptLogItem logItem = new ContractScriptLogItem();
                logItem.setCid(Utils.parseInt(rowElement.getAttribute("cid")));
                logItem.setData(rowElement.getAttribute("data"));
                logItem.setDate(rowElement.getAttribute("time"));
                logItem.setTitle(rowElement.getAttribute("title"));

                logList.add(logItem);
            }

            NodeList table = dataElement.getElementsByTagName("table");
            if (table.getLength() > 0) {
                result.getPage().setRecordCount(Utils.parseInt(((Element) table.item(0)).getAttribute("recordCount")));
                result.getPage().setPageCount(Utils.parseInt(((Element) table.item(0)).getAttribute("pageCount")));
            }
        }
    }

    public void updateContractScript(int contractId, int scriptId, int scriptTypeId, String comment, String dateFrom, String dateTo)
            throws BGException {
        if (dbInfo.versionCompare("9.2") >= 0) {
            ContractScript contractScript = new ContractScript();
            contractScript.setContractId(contractId);
            contractScript.setId(scriptId);
            contractScript.setScriptId(scriptTypeId);
            contractScript.setComment(comment);
            contractScript.setPeriod(new Period(dateFrom + "-" + dateTo));

            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.script",
                    "ContractScriptService",
                    "contractScriptUpdate");
            req.setParam("contractScript", contractScript);

            transferData.postDataReturn(req, user);
        } else {
            Request request = new Request();
            request.setModule(CONTRACT_MODULE_ID);
            request.setAction("UpdateContractScript");
            if (scriptId == 0) {
                request.setAttribute("id", "new");
            } else {
                request.setAttribute("id", scriptId);
            }
            request.setContractId(contractId);
            request.setAttribute("script", scriptTypeId);
            request.setAttribute("comment", comment);
            request.setAttribute("date1", dateFrom);
            request.setAttribute("date2", dateTo);

            transferData.postData(request, user);
        }
    }

    public void deleteContractScript(int scriptId) throws BGException {
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.script",
                    "ContractScriptService",
                    "contractScriptDelete");
            req.setParam("id", scriptId);
            transferData.postDataReturn(req, user);
        } else {
            Request request = new Request();
            request.setModule(CONTRACT_MODULE_ID);
            request.setAction("DeleteContractScript");
            request.setAttribute("id", scriptId);

            transferData.postData(request, user);
        }
    }
}