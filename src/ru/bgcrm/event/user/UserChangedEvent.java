package ru.bgcrm.event.user;

import ru.bgcrm.event.UserEvent;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;

/**
 * Событие генерируется после изменения свойств пользователя в редактора.
 */
public class UserChangedEvent extends UserEvent {
	private final User user;

	public UserChangedEvent(DynActionForm form, User user) {
		super(form);
		this.user = user;
	}

	/**
	 * Возвращает изменённого пользователя
	 * @return
	 */
	public User getChangedUser() {
		return user;
	}
}
