package ru.bgcrm.struts.action;

import java.util.Date;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

public class UserAction extends BaseAction {

	public ActionForward userList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		SearchResult<User> searchResult = new SearchResult<User>(form);
		new UserDAO(conSet.getSlaveConnection()).searchUser(searchResult, 
				CommonDAO.getLikePattern(form.getParam("title"), "subs"),
				form.getSelectedValues("group"), null, new Date(), form.getSelectedValues("permset"), 0);

		for (User user : searchResult.getList()) {
			user.setPassword("");
		}
		
		return data(conSet, mapping, form, "userList");
	}

}
