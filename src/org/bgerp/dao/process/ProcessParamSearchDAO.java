package org.bgerp.dao.process;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.bgerp.model.Pageable;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.param.ParameterSearchedObject;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

import static ru.bgcrm.dao.process.Tables.*;
import static ru.bgcrm.dao.Tables.*;

/**
 * Fluent DAO for selection processes by parameter values.
 *
 * @author Shamil Vakhitov
 */

public class ProcessParamSearchDAO extends SearchDAO {
    private Set<Integer> paramTextIds;
    private String paramTextValue;

    /**
     * {@inheritDoc}
     */
    public ProcessParamSearchDAO(Connection con) {
        super(con);
    }

    /**
     * {@inheritDoc}
     */
    public ProcessParamSearchDAO(Connection con, DynActionForm form) {
        super(con, form);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessParamSearchDAO withOpen(Boolean value) {
        return (ProcessParamSearchDAO) super.withOpen(value);
    }


    public ProcessParamSearchDAO withParamTextIds(Set<Integer> values) {
        this.paramTextIds = values;
        return this;
    }

    public ProcessParamSearchDAO withParamTextValue(String value) {
        this.paramTextValue = value;
        return this;
    }

    /**
     * Queries processes with param values.
     * @param result pageable result.
     * @throws SQLException
     */
    public void search(Pageable<ParameterSearchedObject<Process>> result) throws SQLException {
        try (var pq = new PreparedQuery(con)) {
            var page = result.getPage();
            var list = result.getList();

            pq.addQuery(SQL_SELECT_COUNT_ROWS + "p.*, param.*" + SQL_FROM + TABLE_PROCESS + "AS p");
            pq.addQuery(SQL_INNER_JOIN);

            if (Utils.notBlankString(paramTextValue)) {
                pq.addQuery(TABLE_PARAM_TEXT + "AS param ON p.id=param.id AND param.value LIKE ?");
                pq.addString(paramTextValue);

                if (CollectionUtils.isNotEmpty(paramTextIds))
                    pq.addQuery(SQL_AND + "param.param_id IN (").addQuery(Utils.toString(paramTextIds)).addQuery(")");
            } else
                throw new IllegalArgumentException("No param value filter was defined.");

            pq.addQuery(SQL_WHERE + "1>0 ");
            filterOpen(pq);

            pq.addQuery(getPageLimit(page));

            var rs = pq.executeQuery();
            while (rs.next())
                list.add(new ParameterSearchedObject<Process>(
                    ProcessDAO.getProcessFromRs(rs, "p."),
                    rs.getInt("param.param_id"),
                    rs.getString("param.value")));

            page.setRecordCount(foundRows(pq.getPrepared()));
        }
    }
}
