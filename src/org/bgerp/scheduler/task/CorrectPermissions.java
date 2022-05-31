package org.bgerp.scheduler.task;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.bgerp.scheduler.Task;
import org.bgerp.util.Log;

import ru.bgcrm.dao.user.Tables;
import ru.bgcrm.model.user.PermissionNode;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;

/**
 * Runnable task, performing optimizations in permission storing tables {@link Tables#TABLE_USER_PERMISSION} and {@link Tables#TABLE_PERMSET_PERMISSION}.<br>
 * <li> Delete not existing actions.
 * <li> Replace action synonyms to primary names.
 *
 * @author Shamil Vakhitov
 */
public class CorrectPermissions extends Task {
    private static final Log log = Log.getLog();

    public CorrectPermissions() {
        super(null);
    }

    @Override
    public void run() {
        log.debug("Run");
        replace(Tables.TABLE_USER_PERMISSION);
        replace(Tables.TABLE_PERMSET_PERMISSION);
    }

    private void replace(String tableName) {
        Map<String, String> replacements = replacements(tableName);

        if (replacements.isEmpty())
            return;

        delete(tableName, replacements);
        replace(tableName, replacements);
    }

    private void delete(String tableName, Map<String, String> replacements) {
        String actions = replacements.entrySet().stream()
            .filter(me -> me.getValue() == null)
            .map(Map.Entry::getKey)
            .collect(Collectors.joining("', '"));

        if (Utils.isBlankString(actions))
            return;

        log.debug("Deleting: {}", actions);

        String query = "DELETE FROM" + tableName + "WHERE action IN ('" + actions + "')";
        try (var con = Setup.getSetup().getDBConnectionFromPool()) {
            con.createStatement().executeUpdate(query);
            con.commit();
        } catch (Exception e) {
            log.error(e);
        }
    }

    private void replace(String tableName, Map<String, String> replacements) {
        replacements = replacements.entrySet().stream()
            .filter(me -> me.getValue() != null)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (replacements.isEmpty())
            return;

        log.debug("Replacing: {}", replacements);

        try (var con = Setup.getSetup().getDBConnectionFromPool()) {
            String query = "UPDATE" + tableName + "SET action=? WHERE action=?";
            var ps = con.prepareStatement(query);

            for (var me : replacements.entrySet()) {
                ps.setString(1, me.getValue());
                ps.setString(2, me.getKey());
                ps.executeUpdate();
            }

            con.commit();
        } catch (Exception e) {
            log.error(e);
        }
    }

    /**
     * Replacements for {@code action} column in {@code tableName}.
     * @param tableName DB table name.
     * @return map key - replaced value in {@code action} column, value - {@code null} to remove or not null a replacement.
     */
    private Map<String, String> replacements(String tableName) {
        Map<String, String> replacements = new TreeMap<>();

        try (var conSlave = Setup.getSetup().getDBSlaveConnectionFromPool()) {
            String query = "SELECT DISTINCT action FROM" + tableName;
            var ps = conSlave.prepareStatement(query);

            var rs = ps.executeQuery();
            while (rs.next()) {
                String action = rs.getString(1);

                var node = PermissionNode.getPermissionNode(action);
                if (node == null)
                    replacements.put(action, null);
                else if (!node.getAction().equals(action))
                    replacements.put(action, node.getAction());
            }
        } catch (Exception e) {
            log.error(e);
        }
        return replacements;
    }
}
