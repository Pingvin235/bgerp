<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-item ltitle="Поиск" href="search"
	action="ru.bgcrm.struts.action.SearchAction" command="/user/search.do" />

<ui:menu-item ltitle="Новости" href="news"
	action="ru.bgcrm.struts.action.NewsAction:newsList"
	command="/user/news.do?action=newsList&read=0" />

<ui:menu-item ltitle="Адреса" href="directory/address"
	action="ru.bgcrm.struts.action.DirectoryAddressAction"
	command="/user/directory/address.do" />

<ui:menu-item ltitle="Сообщения" href="message/queue"
	action="ru.bgcrm.struts.action.MessageAction:messageList"
	command="/user/message.do?action=messageList" />

<ui:menu-group ltitle="Процессы">
	<ui:menu-item ltitle="Очереди процессов" href="process/queue"
		action="ru.bgcrm.struts.action.ProcessAction:queue"
		command="/user/process/queue.do?action=queue" />
		
	<ui:menu-item ltitle="Мои процессы" href="process/my"
		action="ru.bgcrm.struts.action.ProcessAction"
		command="/user/process.do?action=userProcessList"/>
</ui:menu-group>

<ui:menu-group ltitle="Контрагент">
	<ui:menu-item ltitle="Создать"
		action="ru.bgcrm.struts.action.CustomerAction:customerCreate"
		command="$$.customer.createAndEdit()" />
</ui:menu-group>

<ui:menu-item ltitle="Логирование" href="log"
	action="ru.bgcrm.struts.action.LogAction" command="/user/log.do" />

<ui:menu-item title="Test" href="/user/test" hidden="true"
	action="org.bgerp.action.TestAction" command="/user/test.do" />

<plugin:include endpoint="user.menu.items.jsp"/>

<ui:menu-group ltitle="Администрирование">
	<ui:menu-item ltitle="Параметры" href="admin/param"
		action="ru.bgcrm.struts.action.admin.DirectoryAction"
		command="/admin/directory.do" />
	
	<ui:menu-group ltitle="Пользователи">
		<ui:menu-item ltitle="Наборы прав" href="admin/user/permset"
			action="ru.bgcrm.struts.action.admin.UserAction:permsetList"
			command="/admin/user.do?action=permsetList" />

		<ui:menu-item ltitle="Группы" href="admin/user/group"
			action="ru.bgcrm.struts.action.admin.UserAction:groupList"
			command="/admin/user.do?action=groupList" />

		<ui:menu-item ltitle="Пользователи" href="admin/user"
			action="ru.bgcrm.struts.action.admin.UserAction:userList"
			command="/admin/user.do?action=userList" />
	</ui:menu-group>
	
	<ui:menu-group ltitle="Процессы">
		<ui:menu-item ltitle="Статусы процессов" href="admin/process/status"
			action="ru.bgcrm.struts.action.admin.ProcessAction:statusList"
			command="/admin/process.do?action=statusList" />
		
		<ui:menu-item ltitle="Типы процессов" href="admin/process/type"
			action="ru.bgcrm.struts.action.admin.ProcessAction:typeList"
			command="/admin/process.do?action=typeList" />
		
		<ui:menu-item ltitle="Очереди процессов" href="admin/process/queue"
			action="ru.bgcrm.struts.action.admin.ProcessAction:queueList"
			command="/admin/process.do?action=queueList" />
	</ui:menu-group>
	
	<ui:menu-item ltitle="Выполнить" href="admin/run"
		action="org.bgerp.action.admin.RunAction"
		command="/admin/run.do" />

	<ui:menu-item title="Custom" href="admin/custom"
		action="org.bgerp.action.admin.CustomAction"
		command="/admin/custom.do" />

	<c:if test="${ctxUser.personalizationMap.get('iface.dyncode') eq '1'}">
		<ui:menu-item ltitle="Динамический код" href="admin/dyncode"
			action="ru.bgcrm.struts.action.admin.DynamicAction"
			command="/admin/dynamic.do" />
	</c:if>
	
	<ui:menu-item ltitle="WEB запросы" href="admin/log/request"
		action="ru.bgcrm.struts.action.admin.WebRequestAction"
		command="/admin/webRequest.do" />
	
	<ui:menu-item ltitle="Конфигурация" href="admin/config"
		action="ru.bgcrm.struts.action.admin.ConfigAction:list"
		command="/admin/config.do?action=list" />
	
	<ui:menu-group ltitle="Приложение">
		<ui:menu-item ltitle="Статус приложения" href="admin/app/status"
			action="ru.bgcrm.struts.action.admin.AppAction:status"
			command="/admin/app.do?action=status"/>
			
		<ui:menu-item ltitle="Авторизовавшиеся пользователи" href="admin/app/logged"
			action="ru.bgcrm.struts.action.admin.AppAction:userLoggedList"
			command="/admin/app.do?action=userLoggedList"/>
	</ui:menu-group>
	
	<plugin:include endpoint="user.admin.menu.items.jsp"/>
</ui:menu-group>
