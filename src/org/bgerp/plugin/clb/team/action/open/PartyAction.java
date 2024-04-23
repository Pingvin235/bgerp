package org.bgerp.plugin.clb.team.action.open;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.BaseAction;
import org.bgerp.plugin.clb.team.Plugin;
import org.bgerp.plugin.clb.team.dao.PartyDAO;
import org.bgerp.plugin.clb.team.model.Party;
import org.bgerp.plugin.clb.team.model.PartyBalance;
import org.bgerp.plugin.clb.team.model.PartyMember;
import org.bgerp.plugin.clb.team.model.PartyPayment;

import javassist.NotFoundException;
import ru.bgcrm.model.Pair;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import java.math.BigDecimal;


@Action(path = "/open/plugin/team/party")
public class PartyAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_OPEN + "/party";

    @Override
    public ActionForward unspecified(DynActionForm form, Connection con) throws Exception {
        return show(form, con);
    }

    public ActionForward show(DynActionForm form, Connection con) throws Exception {
        String secret = form.getParam("secret");

        if (Utils.notBlankString(secret)) {
            var dao = new PartyDAO(con);
            var party = dao.get(secret);
            if (party != null) {
                List<PartyMember> members = dao.getMembersWithPayments(party.getId());
                form.setResponseData("party", party);
                form.setResponseData("members", members);

                List<Pair<Integer, BigDecimal>> amounts = members.stream()
                    .map(m -> new Pair<>(m.getId(), m.paymentsAmount()))
                    .collect(Collectors.toList());
                form.setResponseData("balance", new PartyBalance(amounts));
            }
        }

        return html(con, form, PATH_JSP + "/show.jsp");
    }

    public ActionForward update(DynActionForm form, Connection con) throws Exception {
        Party party = new Party();
        party.setTitle(form.getParam("title", Utils::notBlankString));
        party.setSecret(Utils.generateSecret());

        new PartyDAO(con).update(party);

        form.setResponseData("party", party);

        return json(con, form);
    }

    public ActionForward paymentUpdate(DynActionForm form, Connection con) throws Exception {
        var dao = new PartyDAO(con);
        var party = getPartyOrThrow(dao, form.getParam("secret", Utils::notBlankString));
        var member = dao.getOrCreateMember(party.getId(), form.getParam("member", Utils::notBlankString));

        var payment = new PartyPayment();
        payment.setPartyId(party.getId());
        payment.setMemberId(member.getId());
        payment.setAmount(Utils.parseBigDecimal(form.getParam("amount", Utils::notBlankString)));
        payment.setDescription(form.getParam("description", ""));

        dao.paymentUpdate(payment);

        return json(con, form);
    }

    public ActionForward paymentDelete(DynActionForm form, Connection con) throws Exception {
        var dao = new PartyDAO(con);
        getPartyOrThrow(dao, form.getParam("secret", Utils::notBlankString));

        dao.paymentDelete(form.getId());

        return json(con, form);
    }

    private Party getPartyOrThrow(PartyDAO dao, String secret) throws SQLException, NotFoundException {
        var result = dao.get(secret);
        if (result == null)
            throw new NotFoundException("Not found party by secret.");
        return result;
    }
}
