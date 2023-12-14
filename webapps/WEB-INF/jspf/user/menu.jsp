<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-item ltitle="Поиск" href="search" icon="ti-search"
	action="ru.bgcrm.struts.action.SearchAction:null" command="/user/search.do" />

<ui:menu-item ltitle="News" href="news" icon="ti-bell"
	action="ru.bgcrm.struts.action.NewsAction:newsList"
	command="/user/news.do?action=newsList&read=0" />

<ui:menu-item ltitle="Адреса" href="directory/address" icon="ti-book"
	action="ru.bgcrm.struts.action.DirectoryAddressAction:null"
	command="/user/directory/address.do" />

<ui:menu-item ltitle="Сообщения" href="message/queue" icon="ti-email"
	action="ru.bgcrm.struts.action.MessageAction:messageList"
	command="/user/message.do?action=messageList" />

<ui:menu-group ltitle="Процессы" icon="ti-control-shuffle">
	<ui:menu-item ltitle="Очереди процессов" href="process/queue"
		action="ru.bgcrm.struts.action.ProcessQueueAction:queue"
		command="/user/process/queue.do?action=queue" />

	<ui:menu-item ltitle="My Processes" href="process/my"
		action="ru.bgcrm.struts.action.ProcessAction:userProcessList"
		command="/user/process.do?action=userProcessList"/>

	<plugin:include endpoint="<%=ru.bgcrm.plugin.Endpoint.USER_PROCESS_MENU_ITEMS%>"/>
</ui:menu-group>

<ui:menu-group ltitle="Customer" icon="ti-face-smile">
	<ui:menu-item ltitle="Создать"
		action="ru.bgcrm.struts.action.CustomerAction:customerCreate"
		command="$$.customer.createAndEdit()" />
</ui:menu-group>

<ui:menu-item title="Log" href="log" icon="ti-receipt"
	action="ru.bgcrm.struts.action.LogAction:null" command="/user/log.do" />

<ui:menu-item title="Demo" href="demo" hidden="true"
	action="org.bgerp.action.TestAction:null" command="/user/demo.do" />

<plugin:include endpoint="<%=ru.bgcrm.plugin.Endpoint.USER_MENU_ITEMS%>"/>

<ui:menu-group ltitle="Администрирование" icon="ti-settings">
	<ui:menu-group ltitle="Приложение" icon="ti-package">
		<ui:menu-item ltitle="Status" href="admin/app/status"
			action="org.bgerp.action.admin.AppAction:status"
			command="/admin/app.do?action=status"/>

		<ui:menu-item ltitle="Авторизовавшиеся пользователи" href="admin/app/logged"
			action="org.bgerp.action.admin.AppAction:userLoggedList"
			command="/admin/app.do?action=userLoggedList"/>
	</ui:menu-group>

	<ui:menu-item ltitle="License" href="admin/license" icon="ti-money"
		action="org.bgerp.action.admin.LicenseAction:null"
		command="/admin/license.do" />

	<ui:menu-item ltitle="Конфигурация" href="admin/config" icon="ti-panel"
		action="ru.bgcrm.struts.action.admin.ConfigAction:list"
		command="/admin/config.do?action=list" />

	<ui:menu-item ltitle="Параметры" href="admin/param" icon="ti-palette"
		action="ru.bgcrm.struts.action.admin.DirectoryAction:null"
		command="/admin/directory.do" />

	<ui:menu-group ltitle="Пользователи" icon="ti-user">
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

	<ui:menu-group ltitle="Процессы" icon="ti-control-shuffle">
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


	<ui:menu-item title="Custom" href="admin/custom" icon="ti-hummer"
		action="org.bgerp.action.admin.CustomAction:null"
		command="/admin/custom.do" />

	<ui:menu-group ltitle="Execution" icon="ti-pulse">
		<ui:menu-item ltitle="Run" href="admin/run" icon="ti-rocket"
			action="org.bgerp.action.admin.RunAction:null"
			command="/admin/run.do" />

		<ui:menu-item ltitle="Scheduler" href="admin/scheduler" icon="ti-alarm-clock"
			action="org.bgerp.action.admin.RunAction:scheduler"
			command="/admin/run.do?action=scheduler"/>
	</ui:menu-group>

	<plugin:include endpoint="<%=ru.bgcrm.plugin.Endpoint.USER_ADMIN_MENU_ITEMS%>"/>
</ui:menu-group>
