package ru.bgcrm.dao;

import static ru.bgcrm.dao.Tables.TABLE_PARAM_EMAIL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bgerp.model.Pageable;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.param.ParameterSearchedObject;
import ru.bgcrm.util.Utils;

public class ParamValueSearchDAO extends CommonDAO {
    public ParamValueSearchDAO(Connection con) {
        super(con);
    }

    public static interface Extractor<T> {
        public T extract(ResultSet rs) throws SQLException;
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
            query.append(TABLE_PARAM_EMAIL);
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
