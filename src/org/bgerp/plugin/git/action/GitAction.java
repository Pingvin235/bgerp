package org.bgerp.plugin.git.action;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.exception.BGException;
import org.bgerp.plugin.git.Config;
import org.bgerp.plugin.git.Plugin;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/git/git")
public class GitAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER;

    public ActionForward git(DynActionForm form, ConnectionSet conSet) throws Exception {
        int processId = form.getParamInt("processId", id -> id > 0);

        var config = setup.getConfig(Config.class);
        if (config == null)
            throw new BGException("GIT plugin isn't configured");

        String branch = config.branch(conSet, processId);
        String main = config.mainBranch();
        String prefix = config.prefix(processId);

        var p = new ProcessDAO(conSet.getSlaveConnection()).getProcessOrThrow(processId);

        var command = new StringBuilder(100)
            .append("git checkout ")
            .append(main)
            .append(" && git pull --rebase && git checkout -b ")
            .append(branch)
            .append(config.getCreateBranchSuffix());
        form.setResponseData("commandBranchCreate", command.toString());
        command.setLength(0);

        command
            .append("git checkout ")
            .append(branch)
            .append(" && git pull --rebase")
            .append(" && git fetch origin ")
            .append(main)
            .append(":")
            .append(main)
            .append(" && git merge ")
            .append(main)
            .append(" && git push");
        form.setResponseData("commandBranchSync", command.toString());
        command.setLength(0);

        command
            .append("git checkout ")
            .append(branch)
            .append(" && git pull --rebase")
            .append(" && git commit --allow-empty -m \"MERGED\"")
            .append(" && git push")
            .append(" && git checkout ")
            .append(main)
            .append(" && git merge --squash ")
            .append(branch)
            .append(" && git commit -am \"")
            .append(prefix)
            .append(" ")
            .append(p.getDescription())
            .append("\"");
        String author = config.author(conSet, p);
        if (Utils.notBlankString(author)) {
            command
                .append(" --author=\"")
                .append(author)
                .append("\"");
        }
        form.setResponseData("commandBranchAccept", command.toString());
        command.setLength(0);

        return html(conSet, form, PATH_JSP + "/git.jsp");
    }
}
