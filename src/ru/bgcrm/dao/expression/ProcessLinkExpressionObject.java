package ru.bgcrm.dao.expression;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.process.Process;

/**
 * Expression object for operations with process links
 *
 * @author Shamil Vakhitov
 */
public class ProcessLinkExpressionObject implements ExpressionObject {
    private static final String KEY = Process.OBJECT_TYPE + "Link";
    private static final String KEY_SHORT = "pl";

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
     * Calls {@link ProcessLinkDAO#getObjectLinksWithType(int, String)}
     * @param typeLike SQL LIKE expression for link type.
     * @return list of links.
     */
    public List<String> linkTitles(String typeLike) throws Exception {
        List<String> result = new ArrayList<>();
        for (CommonObjectLink link : linkDao.getObjectLinksWithType(processId, typeLike)) {
            result.add(link.getLinkObjectTitle());
        }
        return result;
    }

    /**
     * Calls {@link #linkTitles(String)}, and returns first title link object.
     * @return title of the the first link or null.
     */
    public String linkTitle(String typeLike) throws Exception {
        for (CommonObjectLink link : linkDao.getObjectLinksWithType(processId, typeLike)) {
            return link.getLinkObjectTitle();
        }
        return null;
    }

    @Deprecated
    public int getStatusProcessLinkedForSame(int linkedTypeId, int linkTypeId) throws Exception {
        Process linkedProcess = linkDao.getProcessLinkedForSame(processId, linkedTypeId, linkTypeId);
        return linkedProcess != null ? linkedProcess.getStatusId() : -1;
    }
}