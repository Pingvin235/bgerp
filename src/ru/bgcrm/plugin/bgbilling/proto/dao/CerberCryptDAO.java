package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bgerp.model.base.IdTitle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.proto.model.cerbercrypt.CardPacket;
import ru.bgcrm.plugin.bgbilling.proto.model.cerbercrypt.UserCard;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

public class CerberCryptDAO extends BillingModuleDAO {
    private static final String CERBERCRYPT_MODULE = "ru.bitel.bgbilling.modules.cerbercrypt";
    private static final String CERBERCRYPT_MODULE_ID = "cerbercrypt";

    public CerberCryptDAO(User user, String billingId, int moduleId) throws BGException {
        super(user, billingId, moduleId);
    }

    public List<Long> getFreeCards() throws BGException {
        /*WSCardActions service = getWebService(WSCardActions_Service.class, WSCardActions.class, moduleId);
        try {
            List<Long> cardNumbers = service.getFreeCardNumbers();
            return cardNumbers;
        } catch (BGException_Exception e) {
            processWebServiceException(e);
        }
        return null;*/
        throw new UnsupportedOperationException();
    }

    public List<UserCard> getUserCardList(int contractId, boolean includeSlaveCards) throws BGException {
        if (dbInfo.versionCompare("6.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(CERBERCRYPT_MODULE, moduleId, "UserCardService", "getUserCardList");
            req.setParam("cid", contractId);
            req.setParam("slavecardsIncluding", includeSlaveCards);

            return readJsonValue(transferData.postDataReturn(req, user).traverse(),
                    jsonTypeFactory.constructCollectionType(List.class, UserCard.class));
        }
        // TODO: Убрать позже вместе со сгенерированными классами сервисов.
        else {
            List<UserCard> usetCards = new ArrayList<UserCard>();

            Request request = new Request();
            request.setModule(CERBERCRYPT_MODULE_ID);
            request.setAction("UserCardTable");
            request.setModuleID(String.valueOf(moduleId));
            request.setContractId(contractId);

            Document document = transferData.postData(request, user);
            Element dataElement = document.getDocumentElement();
            NodeList nodeList = dataElement.getElementsByTagName("row");

            for (int index = 0; index < nodeList.getLength(); index++) {
                UserCard card = new UserCard();
                Element rowElement = (Element) nodeList.item(index);
                card.setId(Utils.parseInt(rowElement.getAttribute("id")));
                card.setComment(rowElement.getAttribute("comment"));
                card.setNumber(Utils.parseLong(rowElement.getAttribute("number")));
                TimeUtils.parsePeriod(rowElement.getAttribute("period"), card);
                card.setSubscrDate(
                        TimeUtils.parse(rowElement.getAttribute("subscr_dt"), TimeUtils.PATTERN_DDMMYYYY));
                usetCards.add(card);
            }

            return usetCards;
        }
    }

    /**
     * Выбирает пакеты по указанной карте на указанную дату;
     * @param contractId
     * @param cardId
     * @param date -- дата на которую необходимо вывести пакеты, не должна быть null;
     * @return
     * @throws BGException
     */
    public List<CardPacket> getCardPackets(int contractId, int cardId, Date date) throws BGException {
        if (dbInfo.versionCompare("6.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(CERBERCRYPT_MODULE, moduleId, "CardPacketService", "cardPacketTable");
            req.setParam("cid", contractId);
            req.setParam("usercardId", cardId);
            req.setParam("date", date);
            req.setParam("objectId", 0);
            req.setParam("cinema", false);

            return readJsonValue(transferData.postDataReturn(req, user).traverse(),
                    jsonTypeFactory.constructCollectionType(List.class, CardPacket.class));
        } else {
            List<CardPacket> cardPackets = new ArrayList<CardPacket>();

            Request request = new Request();
            request.setModule(CERBERCRYPT_MODULE_ID);
            request.setAction("CardPacketTable");
            request.setModuleID(String.valueOf(moduleId));
            request.setContractId(contractId);
            request.setAttribute("card", cardId);
            request.setAttribute("date", new SimpleDateFormat(TimeUtils.PATTERN_DDMMYYYY).format(date));

            Document document = transferData.postData(request, user);
            Element dataElement = document.getDocumentElement();
            NodeList nodeList = dataElement.getElementsByTagName("row");
            if (nodeList.getLength() > 0) {
                for (int index = 0; index < nodeList.getLength(); index++) {
                    CardPacket cardPacket = new CardPacket();
                    Element rowElement = (Element) nodeList.item(index);
                    cardPacket.setId(Utils.parseInt(rowElement.getAttribute("id")));
                    cardPacket.setCard(Utils.parseInt(rowElement.getAttribute("card")));
                    cardPacket.setChangeDate(rowElement.getAttribute("change_date"));
                    cardPacket.setComment(rowElement.getAttribute("comment"));
                    cardPacket.setDateFrom(TimeUtils.parse(rowElement.getAttribute("date1"), TimeUtils.PATTERN_DDMMYYYY));
                    cardPacket.setDateTo(TimeUtils.parse(rowElement.getAttribute("date2"), TimeUtils.PATTERN_DDMMYYYY));
                    cardPacket.setPacket(rowElement.getAttribute("packet"));
                    cardPacket.setPeriod(rowElement.getAttribute("period"));
                    cardPacket.setStatus(rowElement.getAttribute("status"));
                    cardPacket.setPacketId(getPacketTypeId(cardPacket.getId()));

                    cardPackets.add(cardPacket);
                }
            }

            return cardPackets;
        }
    }

    public List<CardPacket> getCardPackets(int contractId, int cardId) throws BGException {
        return getCardPackets(contractId, cardId, new Date());
    }

    private int getPacketTypeId(int packetId) throws BGException {
        Request req = new Request();
        req.setModule(CERBERCRYPT_MODULE_ID);
        req.setAction("GetCardPacket");
        req.setModuleID(String.valueOf(moduleId));
        req.setAttribute("id", packetId);

        Document document = transferData.postData(req, user);
        Element dataElement = document.getDocumentElement();
        NodeList nodeList = dataElement.getElementsByTagName("card_packet");

        if (nodeList.getLength() > 0) {
            return Utils.parseInt(((Element) nodeList.item(0)).getAttribute("packet"));
        }

        return -1;
    }

    public void updateCardPacket(int contractId, int id, int cardNumebr, int packetId, String dateFrom, String dateTo)
            throws BGException {
        Request request = new Request();
        request.setModule(CERBERCRYPT_MODULE_ID);
        request.setAction("UpdateCardPacket");
        request.setModuleID(String.valueOf(moduleId));
        request.setContractId(contractId);
        request.setAttribute("card", cardNumebr);
        if (packetId > 0) {
            request.setAttribute("packet", packetId);
        }
        if (Utils.isEmptyString(dateFrom)) {
            request.setAttribute("date1", new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
        }
        if (Utils.notEmptyString(dateTo)) {
            request.setAttribute("date2", dateTo);
        }
        request.setAttribute("check", 1);
        request.setAttribute("id", id);

        Document doc = transferData.postData(request, user);
        String errMsg = XMLUtils.selectText(doc, "/data[@status='error']/text()");
        if (Utils.notEmptyString(errMsg)) {
            throw new BGMessageException(errMsg);
        }
    }

    public List<IdTitle> getPacketList(boolean virtualCinema) throws BGException {
        List<IdTitle> packets = new ArrayList<IdTitle>();

        Request request = new Request();
        request.setModule(CERBERCRYPT_MODULE_ID);
        request.setAction("PacketList");
        request.setModuleID(String.valueOf(moduleId));
        request.setAttribute("virtual_cinema", virtualCinema);

        Document document = transferData.postData(request, user);

        for (Element e : XMLUtils.selectElements(document, "/data/list/item")) {
            packets.add(new IdTitle(Utils.parseInt(e.getAttribute("id")), e.getAttribute("title")));
        }

        return packets;
    }

    public void updateUserCard(UserCard userCard) throws BGException {
        //TODO:Проверить работоспособность!
        RequestJsonRpc req = new RequestJsonRpc(CERBERCRYPT_MODULE, moduleId, "UserCardService", "updateUserCard");
        req.setParam("uc", userCard);
        transferData.postData(req, user);
    }

    public List<IdTitle> dealerList(Date dateFrom, Date dateTo, String title) throws BGException {
        List<IdTitle> dealerList = new ArrayList<IdTitle>();

        Request request = new Request();
        request.setAction("DealerTable");
        request.setModuleID(moduleId);
        request.setModule(CERBERCRYPT_MODULE_ID);
        request.setAttribute("date1", dateFrom);
        request.setAttribute("date2", dateTo);
        request.setAttribute("title", Utils.maskNull(title, ""));

        Document document = transferData.postData(request, user);

        for (Element element : XMLUtils.selectElements(document, "/data/table/data/row")) {
            dealerList.add(new IdTitle(Utils.parseInt(element.getAttribute("id")), element.getAttribute("title")));
        }
        return dealerList;
    }
}
