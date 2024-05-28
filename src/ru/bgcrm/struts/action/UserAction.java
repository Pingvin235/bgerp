package ru.bgcrm.struts.action;

import java.util.Date;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.model.Pageable;
import org.bgerp.util.sql.LikePattern;

import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/directory/user")
public class UserAction extends BaseAction {

    public ActionForward userList(DynActionForm form, ConnectionSet conSet) throws Exception {
        Pageable<User> searchResult = new Pageable<>(form);
        new UserDAO(conSet.getSlaveConnection()).searchUser(searchResult,
                LikePattern.SUB.get(form.getParam("title")),
                form.getParamValues("group"), null, new Date(), form.getParamValues("permset"), 0);

        for (User user : searchResult.getList()) {
            user.setPassword("");
        }

        return html(conSet, form, PATH_JSP_USER + "/directory/user/list.jsp");
    }

}
