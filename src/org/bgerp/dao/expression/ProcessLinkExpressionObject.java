package org.bgerp.dao.expression;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.util.Utils;

/**
 * Expression object for operations with process links
 *
 * @author Shamil Vakhitov
 */
public class ProcessLinkExpressionObject implements ExpressionObject {
    private static final String KEY = Process.OBJECT_TYPE + "Link";
    private static final String KEY_SHORT = "pl";

    public static final boolean called(String expression) {
        return Utils.notBlankString(expression) && (expression.contains(KEY_SHORT + ".") || expression.contains(KEY + "."));
    }

    private final ProcessLinkDAO linkDao;
    private final int processId;

    public ProcessLinkExpressionObject(Connection con, int processId) {
        this.linkDao = new ProcessLinkDAO(con);
        this.processId = processId;
    }

    @Override
    public void toContext(Map<String, Object> context) {
        context.put(KEY, this);
        context.put(KEY_SHORT, this);
    }

    /**
     * Returns titles of process links matching a link type
     * @param typeLike SQL LIKE expression for link type
     * @return list of link titles
     */
    public List<String> linkTitles(String typeLike) throws Exception {
        List<String> result = new ArrayList<>();
        for (CommonObjectLink link : linkDao.getObjectLinksWithType(processId, typeLike)) {
            result.add(link.getLinkObjectTitle());
        }
        return result;
    }

    /**
     * Returns title of the first process link matching a link type
     * @return title of the first link, or {@code null}
     */
    public String linkTitle(String typeLike) throws Exception {
        for (CommonObjectLink link : linkDao.getObjectLinksWithType(processId, typeLike)) {
            return link.getLinkObjectTitle();
        }
        return null;
    }
}