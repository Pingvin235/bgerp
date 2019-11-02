package ru.bgcrm.servlet.jsp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.core.ConditionalTagSupport;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.filter.AuthFilter;

public class PermissionTag extends ConditionalTagSupport {
	private String[] actions;

	public PermissionTag() {
		super();
		init();
	}

	public void release() {
		super.release();
		init();
	}

	protected boolean condition() {
		User user = AuthFilter.getUser((HttpServletRequest) pageContext.getRequest());

		for (int i = 0; i < actions.length; i++) {
			String action = actions[i];
			if (!action.contains(":")) {
				action += ":null";
			}
			if (UserCache.getPerm(user.getId(), action) != null) {
				return true;
			}
		}

		return false;
	}

	public void setAction(String allowdeActions) {
		actions = allowdeActions.split(",");
	}

	private void init() {
		actions = null;
	}
}