package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bgerp.util.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.JsonNode;

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.proto.dao.directory.UserInfoDirectory;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractNote;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class ContractNoteDAO extends ContractDAO {
    public ContractNoteDAO(User user, String billingId) {
        super(user, billingId);
    }

    public List<ContractNote> list(int contractId) {
        if (dbInfo.versionCompare("8.0") > 0) {
            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractNoteService", "contractNoteList");
            req.setParamContractId(contractId);
            req.setParam("customer", false);

            JsonNode res = transferData.postDataReturn(req, user);
            List<ContractNote> result = readJsonValue(res.traverse(), jsonTypeFactory.constructCollectionType(List.class, ContractNote.class));

            if (!result.isEmpty()) {
                UserInfoDirectory directory = dbInfo.directory(UserInfoDirectory.class);
                result.forEach(note -> note.setUser(directory.get(user, note.getUserId()).getName()));
            }

            return result;
        } else {
            Request request = new Request();
            request.setModule("contract");
            request.setAction("ContractMemo");
            request.setContractId(contractId);

            Document document = transferData.postData(request, user);

            List<ContractNote> result = new ArrayList<>();

            Element dataElement = document.getDocumentElement();
            NodeList nodeList = dataElement.getElementsByTagName("row");

            for (int index = 0; index < nodeList.getLength(); index++) {
                Element rowElement = (Element) nodeList.item(index);
                ContractNote memo = new ContractNote();
                memo.setId(Utils.parseInt(rowElement.getAttribute("f0")));
                memo.setTitle(rowElement.getAttribute("f1"));
                memo.setDateTime(TimeUtils.parse(rowElement.getAttribute("f3"), TimeUtils.PATTERN_DDMMYYYYHHMMSS));
                memo.setComment(get(contractId, memo.getId()).getComment());

                result.add(memo);
            }

            return result;
        }
    }

    public ContractNote get(int contractId, int id) {
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractNoteService", "contractNoteGet");
            req.setParam("contractId", contractId);
            req.setParam("id", id);
            JsonNode res = transferData.postDataReturn(req, user);
            return jsonMapper.convertValue(res, ContractNote.class);
        } else if (dbInfo.versionCompare("8.0") > 0) {
            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractNoteService", "getContractNote");
            req.setParam("contractNoteId", id);
            JsonNode res = transferData.postDataReturn(req, user);
            return jsonMapper.convertValue(res, ContractNote.class);
        } else {
            ContractNote result = null;

            Request request = new Request();
            request.setModule("contract");
            request.setAction("GetContractMemo");
            request.setContractId(contractId);
            request.setAttribute("id", id);

            Document doc = transferData.postData(request, user);

            Element commentEl = XMLUtils.selectElement(doc, "/data/comment");
            if (commentEl != null) {
                result = new ContractNote();

                result.setTitle(commentEl.getAttribute("subject"));
                result.setComment(linesToString(commentEl));
                result.setVisible(Utils.parseBoolean(commentEl.getAttribute("visibled")));
            }

            return result;
        }
    }

    public void update(int contractId, int id, String title, String text) {
        update(contractId, id, title, text, false);
    }

    public void update(int contractId, int id, String title, String text, boolean visible) {
        if (dbInfo.versionCompare("8.0") > 0) {
            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractNoteService", "contractNoteUpdate");
            req.setParamContractId(contractId);

            ContractNote note = new ContractNote();
            note.setId(id);
            note.setContractId(contractId);
            note.setTitle(title);
            note.setComment(text);
            note.setVisible(visible);
            note.setDateTime(new Date());
            note.setUserId(dbInfo.loadUsers(user).getBillingUserId(user.getId()));

            req.setParam("contractNote", note);
            transferData.postDataReturn(req, user);
        } else {
            Request request = new Request();
            request.setModule("contract");
            request.setAction("UpdateContractMemo");
            request.setContractId(contractId);
            request.setAttribute("subject", title);
            request.setAttribute("comment", text);
            request.setAttribute("visibled", visible);
            if (id == 0) {
                request.setAttribute("id", "new");
            } else {
                request.setAttribute("id", id);
            }

            transferData.postData(request, user);
        }
    }

    public void delete(int contractId, int id) {
        if (dbInfo.versionCompare("8.0") > 0) {
            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractNoteService", "contractNoteDelete");
            req.setParamContractId(contractId);
            req.setParam("id", id);
            transferData.postDataReturn(req, user);
        } else {
            Request request = new Request();
            request.setModule("contract");
            request.setAction("DeleteContractMemo");
            request.setContractId(contractId);
            request.setAttribute("id", id);

            transferData.postData(request, user);
        }
    }
}
