package org.bgerp.action;

import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.cache.UserCache;

import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/directory/user", pathId = true)
public class DirectoryUserAction extends BaseAction {

    public ActionForward userList(DynActionForm form, ConnectionSet conSet) throws Exception {
        Set<Integer> groupIds = form.getParamValues("group");

        form.setResponseData("list", UserCache.getUserList().stream()
            .filter(user -> user.getStatus() != User.STATUS_DISABLED && (groupIds.isEmpty() || !CollectionUtils.intersection(groupIds, user.getGroupIds()).isEmpty()))
            .collect(Collectors.toList())
        );

        return html(conSet, form, PATH_JSP_USER + "/directory/user/list.jsp");
    }

}
