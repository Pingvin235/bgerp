package org.bgerp.dao.param;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.model.Pageable;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.Log;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterPhoneValueItem;
import ru.bgcrm.model.param.ParameterSearchedObject;
import ru.bgcrm.util.Utils;

/**
 * Old style parameter value search DAO.
 *
 * @author Shamil Vakhitov
 */
@Deprecated
public class OldParamSearchDAO extends CommonDAO {
    private static Log log = Log.getLog();

    public OldParamSearchDAO(Connection con) {
        super(con);
    }

    public static interface Extractor<T> {
        public T extract(ResultSet rs) throws SQLException;
    }

    /**
     * Добавление INNER JOIN фильтров по параметрам, пока поддерживается только значение вида для списковых.
     * param:<code>:value in 1,2,3
     * @param objectId
     * @param valuesCache
     * @param equation
     * @return
     */
    public static String getParamJoinFilters(String expression, String objectId) throws SQLException {
        StringBuilder result = new StringBuilder();

        String[] tokens = expression.split("\\s+");
        if (tokens.length != 3) {
            log.error("Incorrect filter expression: " + expression);
            return "";
        }

        String paramMacro = tokens[0];
        String function = tokens[1];
        Set<Integer> values = Utils.toIntegerSet(tokens[2]);

        // пока единственная разрешённая функция
        if (!function.equals("in")) {
            log.error("Incorrect function: " + function + " in expression: " + expression);
            return "";
        }

        tokens = paramMacro.split(":");
        if (!paramMacro.startsWith("param:") || tokens.length != 3) {
            log.error("Incorrect param macro: " + paramMacro + " in expression: " + expression);
            return "";
        }

        String paramId = tokens[1];

        Parameter param = ParameterCache.getParameter(Utils.parseInt(paramId));
        if (param == null) {
            log.error("Param not found: " + paramId + " or not address in expression: " + expression);
            return "";
        }

        if ("in".equals(function)) {
            if (Parameter.TYPE_LIST.equals(param.getType())) {
                String tableName = "param_" + param.getId() + "_val";

                result.append(SQL_INNER_JOIN);
                result.append(Tables.TABLE_PARAM_LIST);
                result.append("AS " + tableName);
                result.append(" ON " + tableName + ".id=" + objectId + " AND " + tableName + ".param_id="
                        + param.getId() + " AND " + tableName + ".value IN (" + Utils.toString(values) + ") ");
            }
        }

        return result.toString();
    }

    /**
     * Поиск объектов по значениям связанного телефонного параметра
     * @param parameterId - ID параметра
     * @param parameterPhoneValue - набор телефонов для поиска
     * @return
     * @throws SQLException
     */
    public Set<Integer> searchObjectByParameterPhone(int parameterId, ParameterPhoneValue parameterPhoneValue) throws SQLException {
        Set<Integer> result = new HashSet<Integer>();

        StringBuilder query = new StringBuilder();
        query.append(SQL_SELECT);
        query.append("item.id AS object_id");
        query.append(SQL_FROM);
        query.append(Tables.TABLE_PARAM_PHONE_ITEM);
        query.append(" AS item ");
        query.append(SQL_WHERE);
        query.append("item.param_id = ? ");
        query.append("AND (");

        List<ParameterPhoneValueItem> phoneItems = parameterPhoneValue.getItemList();

        for (int index = 0; index < phoneItems.size(); index++) {
            if (index > 0) {
                query.append(" OR");
            }

            query.append(" item.phone LIKE '%" + phoneItems.get(index).getPhone() + "'");
        }

        query.append(" )");

        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setInt(1, parameterId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            result.add(rs.getInt("object_id"));
        }

        ps.close();

        return result;
    }

    /**
     * Функция поиска объектов по значениям связанного адресного параметра
     * @param parameterId - ID параметра
     * @param parameterAddressValue - значение адресного параметра
     * @return
     * @throws SQLException
     */
    public Set<Integer> searchObjectByParameterAddress(int parameterId, ParameterAddressValue parameterAddressValue) throws SQLException {
        Set<Integer> result = new HashSet<Integer>();

        StringBuilder query = new StringBuilder();
        query.append(SQL_SELECT);
        query.append("address.id AS object_id ");
        query.append(SQL_FROM);
        query.append(Tables.TABLE_PARAM_ADDRESS);
        query.append(" AS address ");
        query.append(SQL_WHERE);
        query.append("address.param_id = ? ");
        query.append("AND address.house_id = ? ");
        query.append("AND address.flat = ? ");
        query.append("AND address.room = ? ");
        query.append("AND address.pod = ? ");
        query.append("AND address.floor = ? ");

        PreparedStatement ps = con.prepareStatement(query.toString());

        ps.setInt(1, parameterId);
        ps.setInt(2, parameterAddressValue.getHouseId());
        ps.setString(3, parameterAddressValue.getFlat());
        ps.setString(4, parameterAddressValue.getRoom());
        ps.setInt(5, parameterAddressValue.getPod());
        ps.setInt(6, parameterAddressValue.getFloor());

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            result.add(rs.getInt("object_id"));
        }

        ps.close();

        return result;
    }

    /**
     * Поиск объектов по значнию текстового параметра.
     * @param parameterId param ID.
     * @param parameterTextValue точное значение.
     * @return список с кодами объектов.
     * @throws SQLException
     */
    public Set<Integer> searchObjectByParameterText(int parameterId, String parameterTextValue) throws SQLException {
        Set<Integer> result = new HashSet<Integer>();

        StringBuilder query = new StringBuilder();
        query.append(SQL_SELECT);
        query.append("text.id AS object_id ");
        query.append(SQL_FROM);
        query.append(Tables.TABLE_PARAM_TEXT);
        query.append(" AS text ");
        query.append(SQL_WHERE);
        query.append("text.param_id = ? ");
        query.append("AND text.value = ? ");

        PreparedStatement ps = con.prepareStatement(query.toString());

        ps.setInt(1, parameterId);
        ps.setString(2, parameterTextValue);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            result.add(rs.getInt("object_id"));
        }

        ps.close();

        return result;
    }

    /**
     * Searches object IDs by list parameter value.
     * @param parameterId
     * @param value
     * @return
     * @throws Exception
     */
    public Set<Integer> searchObjectByParameterList(int parameterId, int value) throws Exception {
        Set<Integer> result = new HashSet<>();

        try (var pq = new PreparedQuery(con)) {
            pq.addQuery(SQL_SELECT);
            pq.addQuery("list.id AS object_id");
            pq.addQuery(SQL_FROM);
            pq.addQuery(Tables.TABLE_PARAM_LIST);
            pq.addQuery(" AS list ");
            pq.addQuery(SQL_WHERE);
            pq.addQuery("list.param_id=? AND list.value=?");

            pq.addInt(parameterId);
            pq.addInt(value);

            try (var rs = pq.executeQuery()) {
                while (rs.next())
                    result.add(rs.getInt(1));
            }
        }

        return result;
    }

    public <T> void searchObjectListByEmail(String tableName, Extractor<T> extractor,
            Pageable<ParameterSearchedObject<T>> searchResult, List<Integer> emailParamIdList, String email) throws BGException {
        if (searchResult != null) {
            Page page = searchResult.getPage();
            List<ParameterSearchedObject<T>> list = searchResult.getList();

            StringBuilder query = new StringBuilder();
            String ids = Utils.toString(emailParamIdList);

            query.append(SQL_SELECT);
            query.append("DISTINCT param.param_id, param.value, c.*");
            query.append(SQL_FROM);
            query.append(tableName);
            query.append("AS c ");
            query.append(SQL_INNER_JOIN);
            query.append(Tables.TABLE_PARAM_EMAIL);
            query.append("AS param ON c.id=param.id AND param.value IN (?,?)");
            if (Utils.notBlankString(ids)) {
                query.append(" AND param.param_id IN (");
                query.append(ids);
                query.append(")");
            }
            query.append(" GROUP BY c.id ");
            query.append(SQL_ORDER_BY);
            query.append("c.title");
            query.append(getPageLimit(page));

            try {
                String domainName = StringUtils.substringAfter(email, "@");

                PreparedStatement ps = con.prepareStatement(query.toString());
                ps.setString(1, email);
                ps.setString(2, domainName);

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    list.add(new ParameterSearchedObject<T>(extractor.extract(rs), rs.getInt(1), rs.getString(2)));
                }

                setRecordCount(page, ps);
                ps.close();
            } catch (SQLException ex) {
                throw new BGException(ex);
            }
        }
    }
}
