package ru.bgcrm.dao;

import static ru.bgcrm.dao.Tables.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bgerp.app.exception.BGException;
import org.bgerp.model.Pageable;

import ru.bgcrm.model.customer.CustomerGroup;
import ru.bgcrm.util.sql.SQLUtils;

/**
 * Customer groups are not visible in interface for now.
 */
public class CustomerGroupDAO extends CommonDAO {
    public CustomerGroupDAO(Connection con) {
        super(con);
    }

    public void searchGroup(Pageable<CustomerGroup> searchResult) {
        searchResult.getList().addAll(getGroupList());
    }

    public List<CustomerGroup> getGroupList() {
        List<CustomerGroup> result = new ArrayList<CustomerGroup>();

        try {
            String query = "SELECT * FROM " + TABLE_CUSTOMER_GROUP_TITLE + " ORDER BY title";
            PreparedStatement ps = con.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(getFromRs(rs));
            }
            ps.close();
        } catch (SQLException ex) {
            throw new BGException(ex);
        }

        return result;
    }

    public CustomerGroup getGroupById(int id) {
        CustomerGroup result = null;

        try {
            String query = "SELECT * FROM " + TABLE_CUSTOMER_GROUP_TITLE + " WHERE id=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = getFromRs(rs);
            }
            ps.close();
        } catch (SQLException ex) {
            throw new BGException(ex);
        }

        return result;
    }

    public void updateGroup(CustomerGroup group) {
        try {
            PreparedStatement ps = null;

            if (group.getId() <= 0) {
                ps = con.prepareStatement("INSERT INTO " + TABLE_CUSTOMER_GROUP_TITLE + " (title, comment) VALUES (?,?)",
                        PreparedStatement.RETURN_GENERATED_KEYS);
            } else {
                ps = con.prepareStatement("UPDATE " + TABLE_CUSTOMER_GROUP_TITLE + " SET title=?, comment=? WHERE id=?");
                ps.setInt(3, group.getId());
            }

            ps.setString(1, group.getTitle());
            ps.setString(2, group.getComment());
            ps.executeUpdate();

            if (group.getId() <= 0) {
                group.setId(SQLUtils.lastInsertId(ps));
            }
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public void deleteGroup(int id) throws SQLException {
        deleteById(TABLE_CUSTOMER_GROUP_TITLE, id);
    }

    private CustomerGroup getFromRs(ResultSet rs) throws SQLException {
        CustomerGroup result = new CustomerGroup();

        result.setId(rs.getInt("id"));
        result.setTitle(rs.getString("title"));
        result.setComment(rs.getString("comment"));

        return result;
    }
}