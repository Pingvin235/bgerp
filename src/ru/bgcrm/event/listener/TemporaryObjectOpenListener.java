package ru.bgcrm.event.listener;

import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bgerp.util.Log;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.GetPoolTasksEvent;
import ru.bgcrm.event.client.TemporaryObjectEvent;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Открытие
 */
public class TemporaryObjectOpenListener extends Thread {
    private static final Log log = Log.getLog();

    private static final long SLEEP_TIME = 10 * 1000L;

    // коды пользователей, для которых необходимо загрузить информацию о временных процессах
    private Set<Integer> tasksForUserLoad = Collections.newSetFromMap(new ConcurrentHashMap<>());
    // временные процессы по пользователям, если нет - пустой Set
    private static Map<Integer, Set<Integer>> userTempProcessMapIds = new ConcurrentHashMap<>();

    public TemporaryObjectOpenListener() {
        EventProcessor.subscribe((e, conSet) -> processListener(e.getForm(), conSet), GetPoolTasksEvent.class);

        start();
    }

    private void processListener(DynActionForm form, ConnectionSet conSet) {
        final int userId = form.getUserId();

        Set<Integer> processIds = userTempProcessMapIds.get(userId);
        if (processIds == null) {
            tasksForUserLoad.add(userId);
        } else {
            form.getResponse().addEvent(new TemporaryObjectEvent(processIds));
        }
    }

    public static void flushUserData(int userId) {
        userTempProcessMapIds.remove(userId);
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (tasksForUserLoad.size() > 0) {
                    Set<Integer> userIds = new HashSet<Integer>(tasksForUserLoad);
                    tasksForUserLoad.clear();

                    try (var con = Setup.getSetup().getDBConnectionFromPool()) {
                        String query = "SELECT create_user_id, id FROM " + TABLE_PROCESS + " AS process " + "WHERE id<0 AND create_user_id IN ("
                                + Utils.toString(userIds) + ")";
                        PreparedStatement ps = con.prepareStatement(query);

                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                            int userId = rs.getInt(1);
                            int processId = rs.getInt(2);

                            Set<Integer> existUserSet = userTempProcessMapIds.get(userId);
                            if (existUserSet == null) {
                                userTempProcessMapIds.put(userId, existUserSet = new HashSet<Integer>());
                            }

                            existUserSet.add(processId);

                            if (log.isDebugEnabled()) {
                                log.debug("User: " + userId + "; found temp process: " + processId);
                            }

                            userIds.remove(userId);
                        }
                        ps.close();
                    } catch (Exception e) {
                        log.error(e);
                    }

                    Set<Integer> empty = Collections.emptySet();
                    for (Integer userId : userIds) {
                        userTempProcessMapIds.put(userId, empty);
                    }
                }
                sleep(SLEEP_TIME);
            }
        } catch (Exception e) {
            log.error(e);
        }
    }
}