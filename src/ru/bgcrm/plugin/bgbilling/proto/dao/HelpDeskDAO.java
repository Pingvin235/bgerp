package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.ws.Holder;

import org.apache.commons.lang.StringUtils;
import org.bgerp.model.Pageable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.FileData;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.helpdesk.HdMessage;
import ru.bgcrm.plugin.bgbilling.proto.model.helpdesk.HdTopic;
import ru.bgcrm.plugin.bgbilling.ws.helpdesk.BgServerFile;
import ru.bgcrm.plugin.bgbilling.ws.helpdesk.HelpdeskService;
import ru.bgcrm.plugin.bgbilling.ws.helpdesk.HelpdeskService_Service;
import ru.bgcrm.plugin.bgbilling.ws.helpdesk.param.HelpdeskParamService;
import ru.bgcrm.plugin.bgbilling.ws.helpdesk.param.HelpdeskParamService_Service;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;
import ru.bgcrm.util.io.Base64;

public class HelpDeskDAO extends BillingDAO {
    private static final String MODULE = "ru.bitel.bgbilling.plugins.helpdesk";

    public static final String MODE_OFF = "off";
    public static final String MODE_ON = "on";
    public static final String MODE_PACKAGE = "package";

    public HelpDeskDAO(User user, DBInfo dbInfo) throws BGException {
        super(user, dbInfo);
    }

    /*http://billing:8081/executer?module=ru.bitel.bgbilling.plugins.helpdesk&status=-1&pageSize=25&userselect=all&onlynew=1&
    	BGBillingSecret=xiYJbPNwknnkCDWUGBcrLZki&pageIndex=1&message=&title=&action=GetTableAdmin
    	&date2=&closed=0&tid=&cache_directory=ru.bitel.bgbilling.plugins.helpdesk.directory.status%3A1381143257497%3B&date1=&

    [ length = 12023 ] xml = <?xml version="1.0" encoding="windows-1251"?><data secret="5634DD7F89879F7710A4AFEB509E5D4E" status="ok"><table pageCount="3" pageIndex="1" pageSize="25" recordCount="74"><data>

    <row category_subcategory="неустановлен ( неустановлен )" cid="1043" comm="E-mail: bulgak@ruscomnet.ru"
     contract_comment="&lt;html&gt;BS040-09 ( &lt;b&gt;ООО &quot;РусКомНет&quot;&lt;/b&gt; )&lt;/html&gt;" date="28.08.2009 12:45:33" dateClose="" id="992"
     lastmessage="14.10.2009 15:45:13" messages="9 [1]" statClose="открыт" status="Доработка / открыт" timeFromLastMessage="1461:02:50"
     topic="Сбор трафика по SNMP"
     user="Шамиль Вахитов" userId="3"/>*/
    public void seachTopicList(Pageable<HdTopic> result, Date messagesAfterTime, Boolean closed, Boolean onlyNew, Integer tid)
            throws BGException {
        Request req = new Request();
        req.setModule(MODULE);
        req.setAction("GetTableAdmin");
        if (closed != null) {
            req.setAttribute("closed", Utils.booleanToStringInt(closed));
        }
        if (onlyNew != null) {
            req.setAttribute("onlynew", Utils.booleanToStringInt(onlyNew));
        }
        if (tid != null) {
            req.setAttribute("tid", tid);
        }

        setPage(req, result.getPage());

        Document doc = transferData.postData(req, user);
        for (Element topicEl : XMLUtils.selectElements(doc, "/data/table/data/row")) {
            HdTopic topic = new HdTopic();
            topic.setId(Utils.parseInt(topicEl.getAttribute("id")));
            topic.setTitle(topicEl.getAttribute("topic"));
            topic.setStatusId(Utils.parseInt(topicEl.getAttribute("statusId")));
            topic.setUserId(Utils.parseInt(topicEl.getAttribute("userId")));
            topic.setContractId(Utils.parseInt(topicEl.getAttribute("cid")));
            if (dbInfo.versionCompare("5.2") >= 0) {
                topic.setContractTitle(StringUtils.substringBefore(topicEl.getAttribute("contract_comment"), "(").trim());
            } else {
                topic.setContractTitle(topicEl.getAttribute("contract"));
            }
            topic.setLastMessageTime(TimeUtils.parse(topicEl.getAttribute("lastmessage"), TimeUtils.PATTERN_DDMMYYYYHHMMSS));
            topic.setContact(topicEl.getAttribute("comm"));

            /*topic.setAutoClose( Utils.parseBoolean( topicEl.getAttribute( "auto_close" ) ) );
            topic.setInPackage( Utils.parseBoolean( topicEl.getAttribute( "package" ) ) );*/

            if (messagesAfterTime != null && messagesAfterTime.after(topic.getLastMessageTime())) {
                continue;
            }

            result.getList().add(topic);
        }
    }

    /*http://billing:8081/executer?module=ru.bitel.bgbilling.plugins.helpdesk&topicId=3815&pageSize=25&action=GetTopicMessage&onlynew=0&
     cache_directory=ru.bitel.bgbilling.plugins.helpdesk.directory.status%3A1381143257497%3B&BGBillingSecret=5uuskSliwBthQkRIilHAkM4s&cid=1750&pageIndex=1&
    [ length = 745 ] xml = <?xml version="1.0" encoding="windows-1251"?><data secret="74BE90F0DADB791B48FA164FE49DF2FE" status="ok">
    <table pageCount="1" pageIndex="1" pageSize="25" recordCount="1"><data><row comment="" fromDateTime="29.05.2012 11:19:18" fromName="Клиент" id="45040"
    includes="false" new="true" toDateTime="" toName="-" type="входящее"/></data></table>
    <topic autoclose="false" categoryId="0" cost="0.00" id="3815" newMessageCount="1" packageId="-1" state="false" status="5" subcategoryId="0"
    subject="При удалении договора не удаляются данные в таблицах инвентаризации модуля inet" user="Амир Абзалилов" userMy="notmy"/>
    <cache_directory><document key="ru.bitel.bgbilling.plugins.helpdesk.directory.status" version="1381143257497"/></cache_directory></data>
    */
    public Pair<HdTopic, List<HdMessage>> getTopicMessageList(int topicId) throws BGException {
        HdTopic topic = new HdTopic();
        topic.setId(topicId);

        List<HdMessage> list = new ArrayList<HdMessage>();

        Pair<HdTopic, List<HdMessage>> result = new Pair<HdTopic, List<HdMessage>>(topic, list);

        Request req = new Request();
        req.setModule(MODULE);
        req.setAction("GetTopicMessage");
        req.setAttribute("topicId", topicId);

        Document doc = transferData.postData(req, user);
        Element topicEl = XMLUtils.selectElement(doc, "/data/topic");
        if (topicEl != null) {
            topic.setState(Utils.parseBoolean(topicEl.getAttribute("state")));
            topic.setUserId(Utils.parseInt(topicEl.getAttribute("userId")));
            topic.setStatusId(Utils.parseInt(topicEl.getAttribute("status")));
            topic.setCost(Utils.parseBigDecimal(topicEl.getAttribute("cost")));
            topic.setAutoClose(Utils.parseBoolean(topicEl.getAttribute("autoclose")));
            topic.setInPackage(Utils.parseInt(topicEl.getAttribute("packageId")) > 0);
        }

        for (Element rowEl : XMLUtils.selectElements(doc, "/data/table/data/row"))
            list.add(parseHdMessage(rowEl));

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

    /*
     http://billing:8081/executer?id=new&body=ddddd&module=ru.bitel.bgbilling.plugins.helpdesk&topicId=3353&action=UpdateMessage&BGBillingSecret=qFPCeqp9OWoGIBM7BBs9W9Va&
     [ length = 160 ] xml = <?xml version="1.0" encoding="windows-1251"?><data secret="B71B660A81778F3C30F0CC1116D42261" status="ok"><message id="59225" topicId="3353" userMy="my"/></data>
     */
    public void updateMessage(int topicId, HdMessage msg) throws BGException {
        Request req = new Request();
        req.setModule(MODULE);
        req.setAction("UpdateMessage");
        if (msg.getId() > 0) {
            req.setAttribute("id", msg.getId());
        } else {
            req.setAttribute("id", "new");
        }
        req.setAttribute("topicId", topicId);
        req.setAttribute("body", msg.getText());

        Document doc = transferData.postData(req, user);
        Element messageEl = XMLUtils.selectElement(doc, "/data/message");
        if (messageEl != null) {
            msg.setId(Utils.parseInt(messageEl.getAttribute("id")));
        }
    }

    /*http://billing:8081/executer?id=22109&module=ru.bitel.bgbilling.plugins.helpdesk&topicId=1587&action=GetMessage&BGBillingSecret=fMw3m7F4koBvH9hawOKNAhMW&
    [ length = 620 ] xml = <?xml version="1.0" encoding="windows-1251"?><data secret="597838E0794C1587BF9B0F36B7FB3AA2" status="ok">
    <message comment="" dateFrom="21.09.2010 18:55:42" dateTo="23.09.2010 17:33:12" id="22109" topicId="1587" userFrom="Клиент" userId="3" userIdFrom="0"
    userIdTo="6" userIdTopic="6" userName="Шамиль Вахитов" userTo="Амир Абзалилов"><body>
    <row text="Не уверен, т.к. на стороне juniper`а вижу адрес в таком же формате... Счас обновлюсь - отпишусь. Уже достижение: на сессию назначился сервис, переданный через тегированный атрибут. Но пока не удается  через CoA изменить."/></body>
    </message><files><data><row id="1551" size="68096" title="Отчет о промежуточных результатах АСР BGBilling5-1 и Juniper ERX с SM.doc"/></data></files></data>*/
    public HdMessage getMessage(int topicId, int messageId) throws BGException {
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

    /*bgbilling/executer?pageIndex=1&module=ru.bitel.bgbilling.plugins.helpdesk&onlynew=0&action=GetTopics&closed=0&pageSize=100000&
     * <?xml version="1.0" encoding="UTF-8"?><data status="ok"><topisc pageCount="1" pageIndex="1" pageSize="200000" recordCount="329">
      <topic autoclose="false" categoryId="0" category_subcategory="неустановлен ( неустановлен )" cid="770" comm="E-mail: semen@dsi.ru" contract_comment="&lt;html&gt;&lt;nobr&gt;BS001-08 ( &lt;b&gt;ОАО &quot;ДЕЛОВАЯ СЕТЬ - ИРКУТСК&quot;&lt;/b&gt; )&lt;/nobr&gt;&lt;/html&gt;" cost="0.00" date="18.11.2009 08:29:21" dateClose="" id="1237" lastmessage="11.08.2010 12:57:47" messages="35 [0]" newMessageCount="0" notificationMode="2" notificationValue="semen@dsi.ru" packageId="-1" statClose="открыт" state="false" status="Доработка / открыт" statusId="1" subcategoryId="0" subject="Тарифные опции" timeFromLastMessage="3168Д" user="Амир Абзалилов" userId="6" userMy="notmy">
        <message comment="" fromDateTime="18.11.2009 08:29:21" fromName="Клиент" fromUserId="0" id="11606" includes="false" new="false" toDateTime="18.11.2009 12:46:45" toName="Шамиль Вахитов" toUserId="3" type="входящее"/>
        <message comment="" fromDateTime="18.11.2009 12:46:45" fromName="Шамиль Вахитов" fromUserId="3" id="11626" includes="false" new="false" toDateTime="18.11.2009 13:25:21" toName="Клиент" toUserId="0" type="исходящие"/>
     * */
    public void searchTopicMessages(Pageable<Pair<HdTopic, List<HdMessage>>> result) throws BGException {
        Request req = new Request();
        req.setModule(MODULE);
        req.setAction("GetTopics");
        req.setAttribute("onlynew", 0);
        req.setAttribute("closed", 0);
        req.setAttribute("pageSize", 200000);

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
    }

    public void markMessageRead(int messageId) throws BGException {
        Request req = new Request();
        req.setModule(MODULE);
        req.setAction("SetReadMessage");
        req.setAttribute("id", messageId);
        req.setAttribute("read", true);

        transferData.postData(req, user);
    }

    /*http://billing:8081/executer?id=3353&module=ru.bitel.bgbilling.plugins.helpdesk&packetMode=0&state=false&action=SetTopicState&BGBillingSecret=tBuQClpIBxs0tPwRYtV24v5e&cid=448&
    [ length = 106 ] xml = <?xml version="1.0" encoding="windows-1251"?><data secret="12CD2AAA49C5B8D67C880D138D899EA6" status="ok"/>*/
    public void setTopicState(int topicId, boolean stateClose) throws BGException {
        Pair<HdTopic, List<HdMessage>> topic = getTopicMessageList(topicId);
        if (topic.getFirst() == null) {
            throw new BGException("Тема не найдена:" + topicId);
        }

        // на случай, если state темы в хелпдеске уже нужный
        if (topic.getFirst().isState() == stateClose) {
            return;
        }

        Request req = new Request();
        req.setModule(MODULE);
        req.setAction("SetTopicState");
        req.setAttribute("id", topicId);
        req.setAttribute("packetMode", 0);
        req.setAttribute("state", stateClose);

        transferData.postData(req, user);
    }

    /*http://billing:8081/executer?module=ru.bitel.bgbilling.plugins.helpdesk&manager=1&topicId=3353&action=ChangeManager&comment=&BGBillingSecret=PFUQKbNfijPEyj568NVbsoX7&
    [ length = 106 ] xml = <?xml version="1.0" encoding="windows-1251"?><data secret="62E88149DDBB72BFDB058181C46A36F7" status="ok"/>*/
    public void setTopicExecutor(int topicId, int billingUserId) throws BGException {
        Request req = new Request();
        req.setModule(MODULE);
        req.setAction("ChangeManager");
        req.setAttribute("topicId", topicId);
        req.setAttribute("manager", billingUserId);

        transferData.postData(req, user);
    }

    /*http://192.168.169.25:9000/bgbilling/executer?id=1065&module=ru.bitel.bgbilling.plugins.helpdesk&userId=me&action=SetBindTopic&BGBillingSecret=qP74XTdqIBC8LmKIyFvAMvhd&*/
    public void setTopicExecutorMe(int topicId) throws BGException {
        Request req = new Request();
        req.setModule(MODULE);
        req.setAction("SetBindTopic");
        req.setAttribute("id", topicId);
        req.setAttribute("userId", "me");

        transferData.postData(req, user);
    }

    /*http://billing:8081/executer?id=3353&module=ru.bitel.bgbilling.plugins.helpdesk&status=12&action=SetTopicStatus&BGBillingSecret=8vAmLmBrRsVLNEMXo8YeLULW&
    [ length = 106 ] xml = <?xml version="1.0" encoding="windows-1251"?><data secret="C6F8ED23DFD9580A8640704B35663EAD" status="ok"/>*/
    public void setTopicStatus(int contractId, int topicId, int status) throws BGException {
        Request req = new Request();
        req.setModule(MODULE);
        req.setAction("SetTopicStatus");
        req.setContractId(contractId);
        req.setAttribute("id", topicId);
        req.setAttribute("status", status);

        transferData.postData(req, user);
    }

    /*http://billing:8081/executer?id=3353&module=ru.bitel.bgbilling.plugins.helpdesk&value=true&action=SetTopicAutoclose&BGBillingSecret=gGhhKOBSGXM2elCHF4eVYDq4&cid=448&
    [ length = 106 ] xml = <?xml version="1.0" encoding="windows-1251"?><data secret="F7321D0DFD87C5C83962112291002645" status="ok"/>*/
    public void setTopicAutoClose(int contractId, int topicId, boolean value) throws BGException {
        Request req = new Request();
        req.setModule(MODULE);
        req.setAction("SetTopicAutoclose");
        req.setContractId(contractId);
        req.setAttribute("id", topicId);
        req.setAttribute("value", value);

        transferData.postData(req, user);
    }

    /*http://billing:8081/executer?module=ru.bitel.bgbilling.plugins.helpdesk&topicId=3353&action=ApplyTopicCost&BGBillingSecret=xetRCA4SyqpIAa65qSD0jWhJ&cost=000&cid=448&
    [ length = 106 ] xml = <?xml version="1.0" encoding="windows-1251"?><data secret="0C58E78318A89B3515C8B9A6AA3FBB6D" status="ok"/>*/
    public void setTopicCost(int contractId, int topicId, BigDecimal cost) throws BGException {
        Request req = new Request();
        req.setModule(MODULE);
        req.setAction("ApplyTopicCost");
        req.setContractId(contractId);
        req.setAttribute("topicId", topicId);
        req.setAttribute("cost", cost.toPlainString());

        transferData.postData(req, user);
    }

    /*http://billing:8081/executer?id=3353&module=ru.bitel.bgbilling.plugins.helpdesk&action=SetTopicPackageState&BGBillingSecret=S2Wu0TsiHS2iT7GdN7VGbZ1a&cid=448&include=true&
    [ length = 186 ] xml = <?xml version="1.0" encoding="windows-1251"?><data errcode="3623339397" secret="2230A92B18D6EDE54B664D5777DBD3FD" status="error">Нет не использованных обращений в активных пакетах</data>*/
    public void setTopicPackageState(int contractId, int topicId, boolean inPackage) throws BGException {
        Request req = new Request();
        req.setModule(MODULE);
        req.setAction("SetTopicPackageState");
        req.setContractId(contractId);
        req.setAttribute("id", topicId);
        req.setAttribute("include", String.valueOf(inPackage));

        transferData.postData(req, user);
    }

    /*http://billing:8081/executer?id=1551&module=ru.bitel.bgbilling.plugins.helpdesk&action=FileDownload&BGBillingSecret=ImcoH6hp3e4VUqGzuI4Fk7UB&cid=917&
    [ length = 91074 ] xml = <?xml version="1.0" encoding="windows-1251"?><data secret="EFEF6940E1758774E4573647931E0401" status="ok"><file date="22.09.2010 12:02:37" id="1551" size="68096" title="Отчет о промежуточных результатах АСР BGBilling5-1 и Juniper ERX с SM.doc"><filedata>0M8R4KGxGuEAAAAAAAAAAAAAAAAAAAAAPgADAP7/CQAGAAAAAAAAAAAAAAACAAAAgAAAAAAAAAAAEAAAggAAAAEAAAD+////AAAAAH4AAAB/AAAA///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////spcEAI2AZBAAA8BK/AAAAAAAAEAAAAAAABgAAWoUAAA4AYmpiam2lbaUAAAAAAAAAAAAAAAAAAAAAAAAZBBYANJIAAA/PAAAPzwAAOjcAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD//w8AAAAAAAAAAAD//w8AAAAAAAAAAAD//w8AAAAAAAAAAAAAAAAAAAAAAKQAAAAAALADAAAAAAAAsAMAALADAAAAAAAAsAMAAAAAAACwAwAAAAAAALADAAAAAAAAsAMAABQAAAAAAAAAAAAAAMQDAAAAAAAALDAAAAAAAAAsMAAAAAAAACwwAAAAAAAALDAAAEwAAAB4MAAAZAAAAMQDAAAAAAAAqEEAAPYAAADoMAAAAAAAAOgwAAAAAAAA6DAAAAAAAADoMAAAAAAAAOgwAAAAAAAA6DAAAAAAAADoMAAAAAAAAOgwAAAAAAAAl0AAAAIAAACZQAAAAAAAAJlAAAAAAAAAmUAAAAAAAACZQAAAAAAAAJlAAAAAAAAAmUAAACQAAACeQgAAaAIAAAZFAAAcAQAAvUAAAKU...*/
    public byte[] getAttach(int contractId, int id) throws BGException {
        byte[] result = null;

        /* Попытка переписать загрузку файла на JSON-RPC, не поддерживается сериализация на сервере.
         if( dbInfo.versionCompare( "6.2" ) >= 0 )
        {
        	RequestJsonRpc req = new RequestJsonRpc( MODULE, "HelpdeskService", "fileDownload" );
        	req.setParamContractId( contractId );
        	req.setParam( "fileId", id );

        	JsonNode node = transferData.postData( req, user );
        	System.out.println( node );
        }
        else */
        if (dbInfo.getVersion().compareTo("6.1") >= 0) {
            try {
                Holder<BgServerFile> serverFileHolder = new Holder<BgServerFile>();
                Holder<byte[]> fileDataHandler = new Holder<byte[]>();

                HelpdeskService service = getWebService(HelpdeskService_Service.class, HelpdeskService.class);
                service.fileDownload(id, contractId, serverFileHolder, fileDataHandler);

                result = fileDataHandler.value;
            } catch (Exception e) {
                processWebServiceException(e);
            }
        } else {
            Request req = new Request();
            req.setModule(MODULE);
            req.setAction("FileDownload");
            req.setContractId(contractId);
            req.setAttribute("id", id);

            try {
                Document doc = transferData.postData(req, user);
                String file = XMLUtils.selectText(doc, "/data/file/filedata/text()");
                if (file != null) {
                    result = Base64.decode(file.getBytes("ASCII"));
                }
            } catch (Exception e) {
                throw new BGException(e);
            }
        }

        return result;
    }

    /*http://billing:8081/executer?filedata=UEsDBAoAAAgAADdlc0EAAAAAAAAAAAAAAAAJAAQATUVUQS1JTkYv%2FsoAAFBLAwQKAAAICAA2ZXNBFZYs6F4AAABqAAAAFAAAAE1FVEEtSU5GL01BTklGRVNU
     * action=FileUpload
     * String comment = getParameter( "comment" );
        String filename = getParameter( "filename" );
        String filedata = getParameter( "filedata" );
        int size = getIntParameter( "size", 0 );
    [ length = 106 ] xml = <?xml version="1.0" encoding="windows-1251"?><data secret="F2489F1C2B1780DC786C1B2BAD9F7862" status="ok"/>*/

    public void putAttach(int messageId, String title, byte[] data) throws BGException {
        if (dbInfo.getVersion().compareTo("6.1") >= 0) {
            try {
                HelpdeskService service = getWebService(HelpdeskService_Service.class, HelpdeskService.class);
                service.fileUpload(String.valueOf(messageId), title, data.length, new Holder<byte[]>(data));
            } catch (Exception e) {
                processWebServiceException(e);
            }
        } else {
            try {
                Request req = new Request();
                req.setModule(MODULE);
                req.setAction("FileUpload");
                req.setAttribute("id", messageId);
                req.setAttribute("filename", title);
                req.setAttribute("size", data.length);
                req.setAttribute("filedata", new String(Base64.encode(data), "ASCII"));
                req.setAttribute("comment", "");

                transferData.postData(req, user);
            } catch (Exception e) {
                throw new BGException(e);
            }
        }
    }

    public String getContractMode(int contractId) throws BGException {
        if (dbInfo.versionCompare("6.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(MODULE, "HelpdeskParamService", "getContractCurrentMode");
            req.setParamContractId(contractId);

            return transferData.postDataReturn(req, user).asText();
        } else if (dbInfo.versionCompare("5.2") >= 0) {
            try {
                HelpdeskParamService service = getWebService(HelpdeskParamService_Service.class, HelpdeskParamService.class);
                return service.getContractCurrentMode(contractId);
            } catch (Exception e) {
                processWebServiceException(e);
            }
        } else {
            /*http://192.168.169.25:9000/bgbilling/executer?module=ru.bitel.bgbilling.plugins.helpdesk&action=GetContractMode&BGBillingSecret=MVBn75Q8zFhwf8ryL2cWqhd1&cid=18335&
            [ length = 258 ] xml = <?xml version="1.0" encoding="windows-1251"?>
            <data secret="CC099C25B609B96EB1DEF8E46D81EE79" status="ok"><modes current="on"><item id="off" title="выключен"/><item id="on" title="включен обычный"/><item id="package" title="включен пакетный"/></modes></data>*/
            Request req = new Request();
            req.setModule(MODULE);
            req.setAction("GetContractMode");
            req.setContractId(contractId);

            Document doc = transferData.postData(req, user);
            return XMLUtils.selectText(doc, "/data/modes/@current");
        }

        return null;
    }
}