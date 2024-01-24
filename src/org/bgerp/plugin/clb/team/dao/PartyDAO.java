package org.bgerp.plugin.clb.team.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bgerp.plugin.clb.team.model.Party;
import org.bgerp.plugin.clb.team.model.PartyMember;
import org.bgerp.plugin.clb.team.model.PartyPayment;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.CommonDAO;

/**
 * Party DAO.
 *
 * @author Shamil Vakhitov
 */
public class PartyDAO extends CommonDAO {
    public PartyDAO(Connection con) {
        super(con);
    }

    /**
     * Creates or updates a party.
     * @param party
     * @throws SQLException
     */
    public void update(Party party) throws SQLException {
        try (var pq = new PreparedQuery(con)) {
            if (party.getId() <= 0) {
                pq.addQuery(SQL_INSERT_INTO + Tables.TABLE_PARTY + "(title, secret)" + SQL_VALUES + "(?, ?)");
                pq.addString(party.getTitle());
                pq.addString(party.getSecret());
                party.setId(pq.executeInsert());
            } else {
                pq.addQuery(SQL_UPDATE + Tables.TABLE_PARTY + SQL_SET + "title=?" + SQL_WHERE + "id=?");
                pq.addString(party.getTitle());
                pq.addInt(party.getId());
                pq.executeUpdate();
            }
        }
    }

    /**
     * Selects a single entity by secret.
     * @param secret a secret string.
     * @return
     * @throws SQLException
     */
    public Party get(String secret) throws SQLException {
        Party result = null;

        try (var pq = new PreparedQuery(con, SQL_SELECT_ALL_FROM + Tables.TABLE_PARTY + SQL_WHERE + "secret=?")) {
            pq.addString(secret);

            var rs = pq.executeQuery();
            if (rs.next()) {
                result = new Party();
                result.setId(rs.getInt("id"));
                result.setTitle(rs.getString("title"));
                result.setSecret(rs.getString("secret"));
            }
        }

        return result;
    }

    /**
     * Selects existing or creates a party member.
     * @param partyId
     * @param title
     * @return
     * @throws SQLException
     */
    public PartyMember getOrCreateMember(int partyId, String title) throws SQLException {
        PartyMember result = null;

        try (var pq = new PreparedQuery(con, SQL_SELECT_ALL_FROM + Tables.TABLE_PARTY_MEMBER + SQL_WHERE + "party_id=? AND title=?")) {
            var rs = pq.addInt(partyId).addString(title).executeQuery();
            if (rs.next())
                result = getMemberFromRs(rs, "");
            else {
                result = new PartyMember();
                result.setPartyId(partyId);
                result.setTitle(title);
                memberUpdate(result);
            }
        }

        return result;
    }

    /**
     * Selects party members with payments.
     * @param partyId party ID.
     * @return
     * @throws SQLException
     */
    public List<PartyMember> getMembersWithPayments(int partyId) throws SQLException {
        var result = new ArrayList<PartyMember>();

        String query =
            SQL_SELECT + "p.*, m.*" + SQL_FROM + Tables.TABLE_PARTY_PAYMENT + "AS p" +
            SQL_INNER_JOIN + Tables.TABLE_PARTY_MEMBER + "AS m ON p.member_id=m.id" +
            SQL_WHERE + "p.party_id=?" +
            SQL_ORDER_BY + "m.title, p.id";


        PartyMember current = null;
        try (var ps = con.prepareStatement(query)) {
            ps.setInt(1, partyId);

            var rs = ps.executeQuery();
            while (rs.next()) {
                var member = getMemberFromRs(rs, "m.");
                if (current == null || current.getId() != member.getId()) {
                    result.add(current = member);
                    current.setPayments(new ArrayList<>());
                }

                current.getPayments().add(getPaymentFromRs(rs, "p."));
            }
        }

        return result;
    }

    /**
     * Creates or updates a party member.
     * @param member
     * @throws SQLException
     */
    public void memberUpdate(PartyMember member) throws SQLException {
        try (var pq = new PreparedQuery(con)) {
            if (member.getId() <= 0) {
                pq.addQuery(SQL_INSERT_INTO + Tables.TABLE_PARTY_MEMBER + "(party_id, title)" + SQL_VALUES + "(?, ?)");
                pq.addInt(member.getPartyId());
                pq.addString(member.getTitle());
                member.setId(pq.executeInsert());
            } else {
                pq.addQuery(SQL_UPDATE + Tables.TABLE_PARTY_MEMBER + SQL_SET + "title=?" + SQL_WHERE + "id=?");
                pq.addString(member.getTitle());
                pq.addInt(member.getId());
                pq.executeUpdate();
            }
        }
    }

    /**
     * Deletes a party member.
     * @param id member DB entity ID.
     * @throws SQLException
     */
    public void memberDelete(int id) throws SQLException {
        String query = SQL_DELETE_FROM + Tables.TABLE_PARTY_PAYMENT + SQL_WHERE + "member_id=?";
        try (var ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
        query = SQL_DELETE_FROM + Tables.TABLE_PARTY_MEMBER + SQL_WHERE + "id=?";
        try (var ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void paymentUpdate(PartyPayment payment) throws SQLException {
        try (var pq = new PreparedQuery(con)) {
            if (payment.getId() <= 0) {
                pq.addQuery(SQL_INSERT_INTO + Tables.TABLE_PARTY_PAYMENT + "(party_id, member_id, amount, description)" + SQL_VALUES + "(?, ?, ?, ?)");
                pq.addInt(payment.getPartyId()).addInt(payment.getMemberId()).addBigDecimal(payment.getAmount()).addString(payment.getDescription());
                payment.setId(pq.executeInsert());
            } else {
                pq.addQuery(SQL_UPDATE + Tables.TABLE_PARTY_PAYMENT + SQL_SET + "amount=?, description=?" + SQL_WHERE + "id=?");
                pq.addBigDecimal(payment.getAmount()).addString(payment.getDescription()).addInt(payment.getId());
                pq.executeUpdate();
            }
        }
    }

    public void paymentDelete(int id) throws SQLException {
        String query = SQL_DELETE_FROM + Tables.TABLE_PARTY_PAYMENT + SQL_WHERE + "id=?";
        try (var ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private PartyMember getMemberFromRs(ResultSet rs, String prefix) throws SQLException {
        var result = new PartyMember();
        result.setId(rs.getInt(prefix + "id"));
        result.setPartyId(rs.getInt(prefix + "party_id"));
        result.setTitle(rs.getString(prefix + "title"));
        return result;
    }

    private PartyPayment getPaymentFromRs(ResultSet rs, String prefix) throws SQLException {
        var result = new PartyPayment();
        result.setId(rs.getInt(prefix + "id"));
        result.setPartyId(rs.getInt(prefix + "party_id"));
        result.setMemberId(rs.getInt(prefix + "member_id"));
        result.setAmount(rs.getBigDecimal(prefix + "amount"));
        result.setDescription(rs.getString(prefix + "description"));
        return result;
    }
}
