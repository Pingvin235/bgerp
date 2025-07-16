package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.bgerp.app.exception.BGException;
import org.bgerp.model.Pageable;
import org.bgerp.model.file.FileData;
import org.bgerp.model.msg.Message;
import org.bgerp.util.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fasterxml.jackson.databind.JsonNode;

import ru.bgcrm.model.Pair;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.BGServerFile;
import ru.bgcrm.plugin.bgbilling.proto.model.helpdesk.HdMessage;
import ru.bgcrm.plugin.bgbilling.proto.model.helpdesk.HdTopic;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class HelpDeskDAO extends BillingDAO {
    private static final String MODULE = "ru.bitel.bgbilling.plugins.helpdesk";

    public HelpDeskDAO(User user, DBInfo dbInfo) {
        super(user, dbInfo);
    }

    /**
     * Requests a single topic with related massages.
     * @param topicId the topic ID.
     * @return the topic - messages pair, or {@code null}
     */
    public Pair<HdTopic, List<HdMessage>> getTopicWithMessages(int topicId) {
        Pair<HdTopic, List<HdMessage>> result = null;

        Request req = new Request();
        req.setModule(MODULE);
        req.setAction("GetTopicMessage");
        req.setAttribute("topicId", topicId);

        Document doc = transferData.postData(req, user);
        Element topicEl = XMLUtils.selectElement(doc, "/data/topic");
        if (topicEl != null) {
            HdTopic topic = new HdTopic();
            topic.setId(topicId);
            topic.setClosed(Utils.parseBoolean(topicEl.getAttribute("state")));
            topic.setUserId(Utils.parseInt(topicEl.getAttribute("userId")));
            topic.setStatusId(Utils.parseInt(topicEl.getAttribute("status")));
            topic.setCost(Utils.parseBigDecimal(topicEl.getAttribute("cost")));
            topic.setAutoClose(Utils.parseBoolean(topicEl.getAttribute("autoclose")));

            List<HdMessage> list = new ArrayList<>();
            for (Element rowEl : XMLUtils.selectElements(doc, "/data/table/data/row"))
                list.add(parseHdMessage(rowEl));

            result = new Pair<>(topic, list);
        }

        return result;
    }

    private HdMessage parseHdMessage(Element messageEl) {
        HdMessage msg = new HdMessage();
        msg.setId(Utils.parseInt(messageEl.getAttribute("id")));
        msg.setTimeFrom(TimeUtils.parse(messageEl.getAttribute("fromDateTime"), TimeUtils.PATTERN_DDMMYYYYHHMMSS));
        msg.setUserIdFrom(Utils.parseInt(messageEl.getAttribute("fromUserId")));
        msg.setTimeTo(TimeUtils.parse(messageEl.getAttribute("toDateTime"), TimeUtils.PATTERN_DDMMYYYYHHMMSS));
        msg.setUserIdTo(Utils.parseInt(messageEl.getAttribute("toUserId")));
        if (messageEl.getAttribute("type").equals("входящее")) {
            msg.setDirection(Message.DIRECTION_INCOMING);
        } else {
            msg.setDirection(Message.DIRECTION_OUTGOING);
        }
        return msg;
    }

    public void updateMessage(int topicId, HdMessage msg) {
        Request req = new Request();
        req.setModule(MODULE);
        req.setAction("UpdateMessage");
        req.setAttribute("id", msg.getId() > 0 ? msg.getId() : (dbInfo.versionCompare("10") > 0 ? 0 : "new"));
        req.setAttribute("topicId", topicId);
        req.setAttribute("body", msg.getText());

        Document doc = transferData.postData(req, user);
        Element messageEl = XMLUtils.selectElement(doc, "/data/message");
        if (messageEl != null) {
            msg.setId(Utils.parseInt(messageEl.getAttribute("id")));
        }
    }

    public HdMessage getMessage(int topicId, int messageId) {
        HdMessage result = null;

        Request req = new Request();
        req.setModule(MODULE);
        req.setAction("GetMessage");
        req.setAttribute("topicId", topicId);
        req.setAttribute("id", messageId);

        Document doc = transferData.postData(req, user);
        Element messageEl = XMLUtils.selectElement(doc, "/data/message");
        if (messageEl != null) {
            result = new HdMessage();
            result.setId(messageId);
            result.setUserIdFrom(Utils.parseInt(messageEl.getAttribute("userIdFrom")));
            result.setUserIdTo(Utils.parseInt(messageEl.getAttribute("userIdTo")));
            result.setTimeFrom(TimeUtils.parse(messageEl.getAttribute("dateFrom"), TimeUtils.PATTERN_DDMMYYYYHHMMSS));
            result.setTimeTo(TimeUtils.parse(messageEl.getAttribute("dateTo"), TimeUtils.PATTERN_DDMMYYYYHHMMSS));

            Element bodyEl = XMLUtils.selectElement(messageEl, "body");
            if (bodyEl != null) {
                result.setText(linesToString(bodyEl));
            }

            for (Element fileEl : XMLUtils.selectElements(messageEl, "/data/files/data/row")) {
                result.addAttach(new FileData(Utils.parseInt(fileEl.getAttribute("id")), fileEl.getAttribute("title"), fileEl.getAttribute("size")));
            }
        }

        return result;
    }

    public void searchTopicsWithMessages(Pageable<Pair<HdTopic, List<HdMessage>>> result, int topicId) {
        if (dbInfo.versionCompare("7.2") >= 0) {
            Request req = new Request();
            req.setModule(MODULE);
            req.setAction("GetTopics");
            req.setAttribute("onlynew", 0);
            req.setAttribute("closed", 0);
            req.setAttribute("pageSize", 200000);
            if (topicId > 0)
                req.setAttribute("tid", topicId);

            Document doc = transferData.postData(req, user);
            for (Element topicEl : XMLUtils.selectElements(doc, "/data//topic")) {
                HdTopic topic = new HdTopic();
                topic.setId(Utils.parseInt(topicEl.getAttribute("id")));
                topic.setTitle(topicEl.getAttribute("subject"));
                topic.setStatusId(Utils.parseInt(topicEl.getAttribute("statusId")));
                topic.setCost(Utils.parseBigDecimal(topicEl.getAttribute("cost")));
                topic.setUserId(Utils.parseInt(topicEl.getAttribute("userId")));
                topic.setContractId(Utils.parseInt(topicEl.getAttribute("cid")));
                topic.setContractTitle(topicEl.getAttribute("contract"));
                topic.setLastMessageTime(TimeUtils.parse(topicEl.getAttribute("lastmessage"), TimeUtils.PATTERN_DDMMYYYYHHMMSS));
                topic.setContact(topicEl.getAttribute("comm"));

                List<HdMessage> messages = new ArrayList<>();
                for (Element messageEl : XMLUtils.selectElements(topicEl, "message"))
                    messages.add(parseHdMessage(messageEl));

                result.getList().add(new Pair<>(topic, messages));
            }
        } else
            throw new IllegalStateException("Unsupported BGBilling version");
    }

    public void markMessageRead(int messageId) {
        if (dbInfo.versionCompare("9.2") >= 0) {
            var req = new RequestJsonRpc(MODULE, "HelpdeskService", "messageAdminReadSet");
            req.setParam("messageId", messageId);
            transferData.postData(req, user);
        } else {
            Request req = new Request();
            req.setModule(MODULE);
            req.setAction("SetReadMessage");
            req.setAttribute("id", messageId);
            req.setAttribute("read", true);

            transferData.postData(req, user);
        }
    }

    public void setTopicState(int topicId, boolean stateClose) {
        Pair<HdTopic, List<HdMessage>> topic = getTopicWithMessages(topicId);
        if (topic.getFirst() == null) {
            throw new BGException("Тема не найдена:" + topicId);
        }

        // на случай, если state темы в хелпдеске уже нужный
        if (topic.getFirst().isClosed() == stateClose) {
            return;
        }

        if (dbInfo.versionCompare("8.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(MODULE, "HelpdeskService", "topicStateUpdate");
            req.setParam("topicId", topicId);
            req.setParam("state", stateClose);
            req.setParam("packageMode", false);

            transferData.postData(req, user);
        } else {
            Request req = new Request();
            req.setModule(MODULE);
            req.setAction("SetTopicState");
            req.setAttribute("id", topicId);
            req.setAttribute("packetMode", 0);
            req.setAttribute("state", stateClose);

            transferData.postData(req, user);
        }
    }

    public void setTopicExecutor(int topicId, int billingUserId) {
        if (dbInfo.versionCompare("8.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(MODULE, "HelpdeskService", "topicBindSet");
            req.setParam("topicId", topicId);
            req.setParam("userId", billingUserId);

            transferData.postData(req, user);
        } else {
            Request req = new Request();
            req.setModule(MODULE);
            req.setAction("ChangeManager");
            req.setAttribute("topicId", topicId);
            req.setAttribute("manager", billingUserId);

            transferData.postData(req, user);
        }
    }

    public void setTopicExecutorMe(int topicId) {
        if (dbInfo.versionCompare("8.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(MODULE, "HelpdeskService", "topicBindSet");
            req.setParam("topicId", topicId);
            req.setParam("userId", "me");

            transferData.postData(req, user);
        } else {
            Request req = new Request();
            req.setModule(MODULE);
            req.setAction("SetBindTopic");
            req.setAttribute("id", topicId);
            req.setAttribute("userId", "me");

            transferData.postData(req, user);
        }
    }

    public void setTopicStatus(int contractId, int topicId, int status) {
        if (dbInfo.versionCompare("8.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(MODULE, "HelpdeskService", "topicStatusUpdate");
            req.setParam("topicId", topicId);
            req.setParam("status", status);

            transferData.postData(req, user);
        } else {
            Request req = new Request();
            req.setModule(MODULE);
            req.setAction("SetTopicStatus");
            req.setContractId(contractId);
            req.setAttribute("id", topicId);
            req.setAttribute("status", status);

            transferData.postData(req, user);
        }
    }

    public void setTopicAutoClose(int contractId, int topicId, boolean value) {
        if (dbInfo.versionCompare("8.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(MODULE, "HelpdeskService", "topicAutocloseUpdate");
            req.setParam("topicId", topicId);
            req.setParam("autoclose", value);

            transferData.postData(req, user);
        } else {
            Request req = new Request();
            req.setModule(MODULE);
            req.setAction("SetTopicAutoclose");
            req.setContractId(contractId);
            req.setAttribute("id", topicId);
            req.setAttribute("value", value);

            transferData.postData(req, user);
        }
    }

    /*http://billing:8081/executer?module=ru.bitel.bgbilling.plugins.helpdesk&topicId=3353&action=ApplyTopicCost&BGBillingSecret=xetRCA4SyqpIAa65qSD0jWhJ&cost=000&cid=448&
    [ length = 106 ] xml = <?xml version="1.0" encoding="windows-1251"?><data secret="0C58E78318A89B3515C8B9A6AA3FBB6D" status="ok"/>*/
    public void setTopicCost(int contractId, int topicId, BigDecimal cost) {
        Request req = new Request();
        req.setModule(MODULE);
        req.setAction("ApplyTopicCost");
        req.setContractId(contractId);
        req.setAttribute("topicId", topicId);
        req.setAttribute("cost", Utils.format(cost));

        transferData.postData(req, user);
    }

    public byte[] getAttach(int contractId, int id) throws Exception {
        byte[] result = null;

        if (dbInfo.versionCompare("8.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(MODULE, "HelpdeskService", "fileDownload");
            req.setParamContractId(contractId);
            req.setParam("fileId", id);

            if (dbInfo.versionCompare("9.2") >= 0) {
                JsonNode node = transferData.postDataReturn(req, user);
                result = Base64.getDecoder().decode(node.path("data").textValue());
            } else {
                JsonNode node = transferData.postData(req, user);
                result = Base64.getDecoder().decode(node.path("fileData").textValue());
            }
        } else {
            Request req = new Request();
            req.setModule(MODULE);
            req.setAction("FileDownload");
            req.setContractId(contractId);
            req.setAttribute("id", id);

            Document doc = transferData.postData(req, user);
            String file = XMLUtils.selectText(doc, "/data/file/filedata/text()");
            if (file != null)
                result = Base64.getDecoder().decode(file.getBytes("ASCII"));
        }

        return result;
    }

    public void putAttach(int messageId, String title, byte[] data) throws Exception {
        if (dbInfo.versionCompare("8.2") >= 0) {
            // ru.bitel.bgbilling.plugins.helpdesk/uploadHelpdeskFile {"date":"07.07.2023 10:34:53","size":16976,"contractId":455,"comment":"","id":0,"ownerId":-1,"title":"╥юяыштю фы  ЁръхЄюъ.docx","userId":-1,"uuid":"d35e9a13-a4d0-417c-8fd9-2d24241080d3"} => 200
            var file = new BGServerFile();
            file.setDate(new Date());
            file.setSize(data.length);
            file.setOwnerId(messageId);
            file.setTitle(title);

            transferData.uploadFile(MODULE + "/uploadHelpdeskFile", file, new ByteArrayInputStream(data), user);
        } else if (dbInfo.versionCompare("6.2") >= 0)
            throw new UnsupportedOperationException("Для данной версии биллинга не поддерживается выгрузка вложений.");
        else {
            Request req = new Request();
            req.setModule(MODULE);
            req.setAction("FileUpload");
            req.setAttribute("id", messageId);
            req.setAttribute("filename", title);
            req.setAttribute("size", data.length);
            req.setAttribute("filedata", new String(Base64.getEncoder().encode(data), "ASCII"));
            req.setAttribute("comment", "");

            transferData.postData(req, user);
        }
    }
}