package org.bgerp.action;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;

import org.apache.struts.action.ActionForward;
import org.bgerp.app.exception.BGIllegalArgumentException;
import org.bgerp.dao.DemoDAO;
import org.bgerp.model.DemoEntity;
import org.bgerp.model.Pageable;
import org.bgerp.util.Log;

import ru.bgcrm.model.user.PermissionNode;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/demo")
public class DemoAction extends org.bgerp.action.open.DemoAction {
    private static final Log log = Log.getLog();

    private static final String PATH_JSP = PATH_JSP_USER + "/demo";

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) {
        super.unspecified(form, conSet);
        permTreesValues(form);
        return html(conSet, form, PATH_JSP + "/demo.jsp");
    }

    private void permTreesValues(DynActionForm form) {
        form.setRequestAttribute("permTrees", PermissionNode.getPermissionTrees());
    }

    public ActionForward enumValues(DynActionForm form, ConnectionSet conSet) {
        List<String> values = List.of(
                "mail1@domain.com",
                "Ivan2 Pupkin <mail2@domain.com>",
                "Ivan3 Pupkin <mail3@domain.com>");
        form.setResponseData("values", values);
        return json(conSet, form);
    }

    public ActionForward tabContentFirst(DynActionForm form, ConnectionSet conSet) {
        form.setResponseData("content", "Tab content first");
        return html(conSet, form, PATH_JSP + "/tab.jsp");
    }

    public ActionForward tabContentSecond(DynActionForm form, ConnectionSet conSet) {
        form.setResponseData("content", "Tab content second");
        return html(conSet, form, PATH_JSP + "/tab.jsp");
    }

    public ActionForward formSend(DynActionForm form, ConnectionSet conSet) throws BGIllegalArgumentException {
        String title = form.getParam("title", Utils::notBlankString);
        form.setResponseData("messageTitle", "The form was successfully accepted");
        form.setResponseData("messageText", Log.format("The sent value was: <b>{}</b>", title));
        log.info("formSend was called, title: {}", title);
        return json(conSet, form);
    }

    public ActionForward entityList(DynActionForm form, Connection con) throws SQLException {
        String filter = form.getParam("filter");

        Pageable<DemoEntity> pageable = new Pageable<>(form, 5);
        new DemoDAO(con).list(pageable, filter);

        return html(con, form, PATH_JSP + "/entity/list.jsp");
    }

    public ActionForward entityGet(DynActionForm form, Connection con) throws Exception {
        if (form.getId() > 0) {
            var entity = new DemoDAO(con).getOrThrow(form.getId());
            form.setResponseData("entity", entity);
        }

        // for simulation of a really loaded DB
        Thread.sleep(Duration.ofSeconds(1).toMillis());

        return html(con, form, PATH_JSP + "/entity/edit.jsp");
    }

    public ActionForward entityUpdate(DynActionForm form, Connection con) throws Exception {
        var entity = new DemoEntity();
        entity.setId(form.getId());
        entity.setTitle(form.getParam("title", Utils::notBlankString));
        entity.setConfig(form.getParam("config"));

        new DemoDAO(con).update(entity);

        return json(con, form);
    }

    public ActionForward entityDelete(DynActionForm form, Connection con) throws Exception {
        new DemoDAO(con).delete(form.getId());

        // for simulation of a really loaded DB
        Thread.sleep(Duration.ofSeconds(1).toMillis());

        return json(con, form);
    }

    // TODO: Some helper methods for testing parameters validatation and so on.
}
