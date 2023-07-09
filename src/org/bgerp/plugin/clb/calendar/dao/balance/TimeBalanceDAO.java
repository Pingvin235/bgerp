package org.bgerp.plugin.clb.calendar.dao.balance;
// package org.bgerp.plugin.clb.calendar.dao;

// import java.sql.Connection;
// import java.sql.SQLException;
// import java.util.Collection;
// import java.util.HashMap;
// import java.util.Map;

// import org.bgerp.plugin.clb.calendar.model.TimeAccount;
// import org.bgerp.plugin.clb.calendar.model.TimeBalance;

// import ru.bgcrm.dao.CommonDAO;
// import ru.bgcrm.util.Utils;
// import ru.bgcrm.util.sql.PreparedDelay;

// public class TimeBalanceDAO extends CommonDAO {
//     public TimeBalanceDAO(Connection con) {
//         super(con);
//     }

//     public Map<Integer, TimeBalance> getBalances(int year, Collection<Integer> userIds) throws SQLException {
//         var result = new HashMap<Integer, TimeBalance>(userIds.size());

//         // empty values per user
//         for (int userId : userIds) {
//             result.computeIfAbsent(userId, unused -> new TimeBalance(userId, year));
//         }

//         String query = SQL_SELECT_ALL_FROM + Tables.TABLE_TIME_ACCOUNT + SQL_WHERE + "year=? AND user_id IN (";
//         try (var pd = new PreparedDelay(con, query)) {
//             var rs = pd
//                 .addInt(year)
//                 .addQuery(Utils.toString(userIds, "-1", ",")).addQuery(")")
//                 .executeQuery();
//             while (rs.next()) {
//                 var account = new TimeAccount(rs.getInt("user_id"), year);
//                 account.setTypeId(rs.getInt("type_id"));
//                 account.setAmount(rs.getInt("amount"));

//                 result
//                     .get(account.getUserId())
//                     .addAccount(account);
//             }
//         }

//         return result;
//     }
// }
