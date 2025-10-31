package org.bgerp.plugin.git.action.admin;

import java.util.List;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.custom.Custom;
import org.bgerp.plugin.git.Plugin;
import org.bgerp.util.RuntimeRunner;

import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/admin/plugin/git/custom", pathId = true)
public class CustomAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER;

    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        if (Custom.DIR.exists() && Custom.DIR.isDirectory()) {
            var runner = new RuntimeRunner("git", "status").directory(Custom.DIR);
            int code = runner.runSafe();

            List<String> stderr = runner.stdErr();
            List<String> stdout = runner.stdOut();

            log.debug("STDERR: {}\nSTDOUT: {}", String.join("\n", stderr), String.join("\n", stdout));

            String firstOutLine = Utils.getFirst(stdout);

            String error = null, branch = null;

            if (code != 0)
                error = l.l("Process exit code: {}", code) + " " + Utils.getFirst(stderr);
            else if (Utils.isBlankString(firstOutLine))
                error = "No GIT output";
            else if (stdout.stream().filter(line -> line.contains("working tree clean")).findFirst().orElse(null) == null)
                error = "Working tree is not clean";
            else if (firstOutLine.startsWith("HEAD detached at "))
                branch = firstOutLine.substring(17);
            else
                error = "HEAD is not detached";

            if (Utils.notBlankString(error))
                form.setResponseData("error", error);
            else {
                form.setResponseData("branch", branch);

                runner = new RuntimeRunner("git", "show", "-s", "--format=%h%x20%ci%x20%s", "HEAD").directory(Custom.DIR).run();
                form.setResponseData("commit", Utils.getFirst(runner.stdOut()));
            }
        }

        return html(conSet, form, PATH_JSP + "/custom/link.jsp");
    }

    public ActionForward update(DynActionForm form, ConnectionSet conSet) throws Exception {
        String branch = form.getParam("branch", Utils::notBlankString);

        new RuntimeRunner("git", "fetch").directory(Custom.DIR).run();
        new RuntimeRunner("git", "checkout", branch).directory(Custom.DIR).run();

        return json(conSet, form);
    }
}
