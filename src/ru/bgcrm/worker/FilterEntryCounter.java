package ru.bgcrm.worker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.process.Queue;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.sql.SQLUtils;

public class FilterEntryCounter extends Thread {
	private Logger log = Logger.getLogger(FilterEntryCounter.class);

	private static final long TIMEOUT = 60 * 1000L;

	private static FilterEntryCounter instance;

	private FilterEntryCounter() {
		start();
	}

	private static Map<String, CountAndTime> queries = new ConcurrentHashMap<>();

	public static FilterEntryCounter getInstance() {
		if (instance == null) {
			synchronized (FilterEntryCounter.class) {
				if (instance == null) {
					instance = new FilterEntryCounter();
				}
			}
		}
		return instance;
	}

	private static class CountAndTime {
		public volatile int count;
		public volatile long time;

		public CountAndTime(int count, long time) {
			this.count = count;
			this.time = time;
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				removeOldQueries();
				updateQueries();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				try {
					Thread.sleep(TIMEOUT);
				} catch (Exception ex) {
				}
			}
		}
	}

	private void updateQueries() {
		Connection con = Setup.getSetup().getConnectionPool().getDBSlaveConnectionFromPool();
		try {
			for (String query : queries.keySet()) {
				countQuery(con, query);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			SQLUtils.closeConnection(con);
		}
	}

	private int countQuery(Connection con, String query) throws SQLException {
		int result = 0;

		PreparedStatement ps = con.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			queries.put(query, new CountAndTime(result = rs.getInt(1), System.currentTimeMillis()));
		}
		ps.close();

		return result;
	}

	/*private void addQuery( String query )
	{
		HashMap<String, Object> countAndDate = new HashMap<String, Object>();
		countAndDate.put( "count", -1 );
		countAndDate.put( "time", Calendar.getInstance().getTimeInMillis() );
		queries.put( query, countAndDate );
	}
	*/
	private void removeOldQueries() {
		long interval = Setup.getSetup().getLong("filterEntryStorageInterval", TIMEOUT * 2);
		Long currentTime = Calendar.getInstance().getTimeInMillis();

		for (Map.Entry<String, CountAndTime> me : queries.entrySet()) {
			String query = me.getKey();
			CountAndTime value = me.getValue();

			if ((currentTime - value.time) > interval) {
				queries.remove(query);
			}
		}
	}

	private int getCount(String query) {
		CountAndTime result = queries.get(query);
		// "заявка" на подсчёт количества
		if (result == null) {
			queries.put(query, result = new CountAndTime(-1, System.currentTimeMillis()));
		}
		return result.count;
	}

	public int parseUrlAndGetCount(Queue queue, String url, User user) throws Exception {
		DynActionForm filterForm = new DynActionForm(url);
		filterForm.setUser(user);

		String query = new ProcessDAO(null, user).getCountQuery(queue, filterForm);
		return getCount(query);
	}

	public int parseUrlAndGetCountSync(Queue queue, String url, User user) throws BGException {
		DynActionForm filterForm = new DynActionForm(url);
		filterForm.setUser(user);

		String query = new ProcessDAO(null).getCountQuery(queue, filterForm);

		if (queries.containsKey(query)) {
			return getCount(query);
		} else {
			int result = 0;
			Connection con = Setup.getSetup().getConnectionPool().getDBSlaveConnectionFromPool();
			try {
				result = countQuery(con, query);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				SQLUtils.closeConnection(con);
			}
			return result;
		}
	}
}