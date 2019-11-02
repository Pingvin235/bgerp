package ru.bgcrm.dao.expression;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.process.Process;

/**
 * Использовать {@link ProcessLinkDAO}.
 */
@Deprecated
public class ProcessLinkFunction {
	private static final Logger log = Logger.getLogger(ProcessLinkFunction.class);

	public static final String PROCESS_LINK_FUNCTION = "processLink";

	private final ProcessLinkDAO linkDao;
	private final int processId;

	public ProcessLinkFunction(Connection con, int processId) {
		this.linkDao = new ProcessLinkDAO(con);
		this.processId = processId;
	}

	/**
	 * Использовать {@link ProcessLinkDAO#getObjectLinksWithType(int, String)}
	 */
	@Deprecated
	public List<String> linkTitles(String typeLike) {
		List<String> result = new ArrayList<String>();

		try {
			for (CommonObjectLink link : linkDao.getObjectLinksWithType(processId, typeLike)) {
				result.add(link.getLinkedObjectTitle());
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return result;
	}

	/**
	 * Использовать {@link ProcessLinkDAO#getObjectLinksWithType(int, String)}
	 */
	@Deprecated
	public String linkTitle(String typeLike) {
		try {
			for (CommonObjectLink link : linkDao.getObjectLinksWithType(processId, typeLike)) {
				return link.getLinkedObjectTitle();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return null;
	}

	@Deprecated
	public int getStatusProcessLinkedForSame(int linkedTypeId, int linkTypeId) throws Exception {
		Process linkedProcess = linkDao.getProcessLinkedForSame(processId, linkedTypeId, linkTypeId);
		return linkedProcess != null ? linkedProcess.getStatusId() : -1;
	}
}