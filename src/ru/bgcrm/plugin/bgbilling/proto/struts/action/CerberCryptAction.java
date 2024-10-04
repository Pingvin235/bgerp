package ru.bgcrm.plugin.bgbilling.proto.struts.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.struts.action.ActionForward;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.app.exception.BGMessageExceptionWithoutL10n;

import ru.bgcrm.plugin.bgbilling.Plugin;
import ru.bgcrm.plugin.bgbilling.proto.dao.CerberCryptDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.cerbercrypt.CardPacket;
import ru.bgcrm.plugin.bgbilling.proto.model.cerbercrypt.UserCard;
import ru.bgcrm.plugin.bgbilling.struts.action.BaseAction;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/bgbilling/proto/cerbercrypt")
public class CerberCryptAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER + "/cerbercrypt";

    public ActionForward cardPacketList(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");
        Integer cardId = form.getParamInt("cardId");

        CerberCryptDAO cerbercryptDAO = new CerberCryptDAO(form.getUser(), billingId, form.getParamInt("moduleId"));
        List<CardPacket> cardPacketList = cerbercryptDAO.getCardPackets(contractId, cardId);

        form.setResponseData("list", cardPacketList);

        return html(conSet, form, PATH_JSP + "/card_packet_list.jsp");
    }

    public ActionForward contractCards(DynActionForm form, ConnectionSet conSet) throws BGMessageException {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        boolean includeSlaveCards = form.getParamBoolean("includeSlaveCards", true);
        int moduleId = form.getParamInt("moduleId");

        if (moduleId <= 0) {
            throw new BGMessageExceptionWithoutL10n("Не указан ID модуля!");
        }

        CerberCryptDAO cerbercryptDAO = new CerberCryptDAO(form.getUser(), billingId, moduleId);
        List<UserCard> cards = cerbercryptDAO.getUserCardList(contractId, includeSlaveCards);

        form.setResponseData("list", cards);

        return html(conSet, form, PATH_JSP + "/card_list.jsp");
    }

    public ActionForward updateCardPacket(DynActionForm form, ConnectionSet conSet) throws BGMessageException {
        String billingId = form.getParam("billingId");
        int id = form.getId();
        int contractId = form.getParamInt("contractId");
        int cardNumber = form.getParamInt("cardNumber");
        int packetId = form.getParamInt("packetId");
        String date1 = form.getParam("date1");
        String date2 = form.getParam("date2");

        if ("now".equals(date1)) {
            date1 = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
        }
        if ("now".equals(date2)) {
            date2 = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
        }

        CerberCryptDAO cerbercryptDAO = new CerberCryptDAO(form.getUser(), billingId, form.getParamInt("moduleId"));
        cerbercryptDAO.updateCardPacket(contractId, id, cardNumber, packetId, date1, date2);

        return json(conSet, form);
    }

    public ActionForward updateCard(DynActionForm form,  ConnectionSet conSet) {
        String billingId = form.getParam("billingId");

        UserCard userCard = new UserCard();
        userCard.setId(form.getId());
        userCard.setContractId(form.getParamInt("contractId"));
        userCard.setDateFrom(form.getParamDate("dateFrom"));
        userCard.setDateTo(form.getParamDate("dateTo"));
        userCard.setNumber(form.getParamLong("cardNumber"));
        userCard.setSubscrDate(new Date());
        userCard.setComment(Utils.maskNull(form.getParam("comment")));
        userCard.setBaseCardId(form.getParamInt("baseCardId", -1));
        userCard.setNeedSync(false);

        CerberCryptDAO cerbercryptDAO = new CerberCryptDAO(form.getUser(), billingId, form.getParamInt("moduleId"));
        cerbercryptDAO.updateUserCard(userCard);

        /*
         * if( Utils.notEmptyString( date1 ) ) { userCard.setDate1( date1 ); } else {
         * userCard.setDate1( new SimpleDateFormat( "yyyy-MM-dd" ).format( new Date() )
         * ); }
         *
         * if( Utils.notEmptyString( date2 ) && !"now".equals( date2 ) ) {
         * userCard.setDate2( date2 ); } else if( "now".equals( date2 ) ) {
         * userCard.setDate2( new SimpleDateFormat( "yyyy-MM-dd" ).format( new Date() )
         * ); }
         *
         * WSUserCard userCardWs = new UserCardWebServiceDAO( billingId, form.getUser()
         * ).getWSUserCard(); userCardWs.updateUserCard( userCard );
         */

        return json(conSet, form);
    }

    public ActionForward getFreeCards(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        int moduleId = form.getParamInt("moduleId");

        CerberCryptDAO cerbercryptDAO = new CerberCryptDAO(form.getUser(), billingId, moduleId);
        form.getResponse().getData().put("cards", cerbercryptDAO.getFreeCards());

        return html(conSet, form, PATH_JSP + "");
    }

    public ActionForward getPacketList(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        boolean virtualCinema = form.getParamBoolean("virtualCinema", false);

        CerberCryptDAO cerbercryptDAO = new CerberCryptDAO(form.getUser(), billingId, form.getParamInt("moduleId"));
        form.getResponse().getData().put("packets", cerbercryptDAO.getPacketList(virtualCinema));

        return html(conSet, form, PATH_JSP + "");
    }
}
