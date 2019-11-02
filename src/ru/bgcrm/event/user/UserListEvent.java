package ru.bgcrm.event.user;

import java.util.List;
import java.util.Map;

import ru.bgcrm.event.UserEvent;
import ru.bgcrm.struts.form.DynActionForm;

/**
 * Событие генерирутся после построения списка пользователей
 * для редактирования исполнителя в свойствах процесса.
 */
public class UserListEvent extends UserEvent {
	private final List<Map<String, String>> userList;
	
	public UserListEvent(DynActionForm form, List<Map<String, String>> userList) {
		super(form);
		this.userList = userList;
	}

	/** 
	 * Не слишком красивая структура со свойствами пользователей. 
	 * Пользователи представлены в виде Map ов с ключами id, title.
	 * Это обусловлено тем, как JSP странице editor_exexutors.jsp передаётся этот список в select_mult.  
	 * @return
	 */
	public List<Map<String, String>> getUserList() {
		return userList;
	}
}
