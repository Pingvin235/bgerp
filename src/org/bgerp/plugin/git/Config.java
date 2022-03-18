package org.bgerp.plugin.git;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.event.ParamChangingEvent;
import ru.bgcrm.event.process.ProcessChangingEvent;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgerp.l10n.Localization;

public class Config extends ru.bgcrm.util.Config {
    /** Branch and commit name prefix. */
    private final String prefix;
    /** Main branch name. */
    private final String mainBranch;
    /** Process parameter with branch name. */
    private final int paramBranchId;
    /** Parameter with user GIT email. */
    private final int paramEmailId;
    /** Restricted process status IDs when branch values is set. */
    private final Set<Integer> statusWithBranchIds;

    protected Config(ParameterMap config) throws InitStopException {
        super(null);
        config = config.sub(Plugin.ID + ":");
        paramBranchId = config.getInt("param.branch");
        paramEmailId = config.getInt("param.email");
        statusWithBranchIds = Collections.unmodifiableSet(Utils.toIntegerSet(config.get("process.allowed.status.with.branch")));

        prefix = config.get("prefix", "p");
        mainBranch = config.get("main.branch", "master");

        initWhen(paramBranchId > 0);
    }

    public String getMainBranch() {
        return mainBranch;
    }

    /**
     * Generates branch name parameter value.
     * @param e
     * @param conSet
     * @throws Exception
     */
    public void paramChanging(ParamChangingEvent e, ConnectionSet conSet) throws Exception {
        if (e.getParameter().getId() != paramBranchId)
            return;

        String value = (String) e.getValue();

        final String prefix = prefix(e.getObjectId()) + "-";
        if (Utils.notBlankString(value) && !value.startsWith(prefix)) {
            value = prefix + value.replace("_", "-");
            new ParamValueDAO(conSet.getConnection()).updateParamText(e.getObjectId(), paramBranchId, value);
        }
    }

    /**
     * Checks allowance of status change when branch name is filled.
     * @param e
     * @param conSet
     * @throws SQLException
     * @throws BGMessageException when status change isn't allowed.
     */
    public void processChanging(ProcessChangingEvent e, ConnectionSet conSet) throws SQLException, BGMessageException {
        if (!e.isStatus() || statusWithBranchIds.isEmpty() || statusWithBranchIds.contains(e.getStatusChange().getStatusId()))
            return;

        String branchName = branch(conSet, e.getProcess().getId());
        if (Utils.isBlankString(branchName))
            return;

        throw new BGMessageException(Localization.getLocalizer(Plugin.ID, e.getForm().getHttpRequest()),
                "You can't change to the status when GIT branch isn't empty");
    }

    /**
     * Prefix for commit and branch.
     * @param processId
     * @return
     */
    public String prefix(int processId) {
        return this.prefix + processId;
    }

    /**
     * Branch name from process parameter.
     * @param conSet
     * @param processId
     * @return
     * @throws SQLException
     */
    public String branch(ConnectionSet conSet, int processId) throws SQLException {
        return new ParamValueDAO(conSet.getSlaveConnection()).getParamText(processId, paramBranchId);
    }

    /**
     * GIT commit author.
     * @param conSet
     * @param p
     * @return
     * @throws Exception
     */
    public String author(ConnectionSet conSet, Process p) throws Exception {
        Integer userId = Utils.getFirst(
            p.getExecutorIds().size() > 1 ?
            p.getExecutorIdsWithRole(0) :
            p.getExecutorIds());

        if (userId == null)
            return null;

        var user = UserCache.getUser(userId);

        var email = Utils.getFirst(new ParamValueDAO(conSet.getSlaveConnection()).getParamEmail(userId, paramEmailId).values());
        if (email == null)
            return null;

        return
            (Utils.notBlankString(email.getComment()) ? email.getComment() : user.getTitle()) +
            " <" + email.getValue() + ">";
    }
}
