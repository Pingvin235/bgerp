<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-item title="${l.l('Поиск')}" href="search" icon="ti-search" action="/user/search:null" />

<ui:menu-item title="${l.l('News')}" href="news" icon="ti-bell" action="/user/news:newsList" />

<ui:menu-item title="${l.l('Адреса')}" href="directory/address" icon="ti-book"
	action="ru.bgcrm.struts.action.DirectoryAddressAction:null" />

<ui:menu-item title="${l.l('Сообщения')}" href="message/queue" icon="ti-email" action="/user/message:messageList" />

<ui:menu-group title="${l.l('Процессы')}" icon="ti-control-shuffle">
	<ui:menu-item title="${l.l('Process Queues')}" href="process/queue"
		action="ru.bgcrm.struts.action.ProcessQueueAction:queue" />

	<ui:menu-item title="${l.l('My Processes')}" href="process/my"
		action="/user/process:userProcessList"/>

	<plugin:include endpoint="<%=ru.bgcrm.plugin.Endpoint.USER_PROCESS_MENU_ITEMS%>"/>
</ui:menu-group>

<ui:menu-group title="${l.l('Customer')}" icon="ti-face-smile">
	<ui:menu-item title="${l.l('Создать')}" action="/user/customer:customerCreate" command="$$.customer.createAndEdit()"/>
</ui:menu-group>

<ui:menu-item title="Log" href="log" icon="ti-receipt" action="/user/log:null" />

<plugin:include endpoint="<%=ru.bgcrm.plugin.Endpoint.USER_MENU_ITEMS%>"/>

<ui:menu-group title="${l.l('Администрирование')}" icon="ti-settings">
	<ui:menu-group title="${l.l('Приложение')}" icon="ti-package">
		<ui:menu-item title="${l.l('Status')}" href="admin/app/status" action="/admin/app:status" />

		<ui:menu-item title="${l.l('Maintenance')}" href="admin/app/maintenance"
			action="/admin/app:maintenance" />
	</ui:menu-group>

	<ui:menu-item title="${l.l('License')}" href="admin/license" icon="ti-money" action="/admin/license:null" />

	<ui:menu-item title="${l.l('Configuration')}" href="admin/config" icon="ti-panel" action="/admin/config:list" />

	<ui:menu-item title="${l.l('Parameters')}" href="admin/param" icon="ti-palette" action="/admin/directory:null" />

	<ui:menu-group title="${l.l('Пользователи')}" icon="ti-user">
		<ui:menu-item title="${l.l('Permission Sets')}" href="admin/user/permset"
			action="ru.bgcrm.struts.action.admin.UserAction:permsetList" />

		<ui:menu-item title="${l.l('Groups')}" href="admin/user/group"
			action="ru.bgcrm.struts.action.admin.UserAction:groupList" />

		<ui:menu-item title="${l.l('Пользователи')}" href="admin/user"
			action="ru.bgcrm.struts.action.admin.UserAction:userList"/>
	</ui:menu-group>

	<ui:menu-group title="${l.l('Процессы')}" icon="ti-control-shuffle">
		<ui:menu-item title="${l.l('Статусы процессов')}" href="admin/process/status"
			action="ru.bgcrm.struts.action.admin.ProcessAction:statusList" />

		<ui:menu-item title="${l.l('Типы процессов')}" href="admin/process/type"
			action="ru.bgcrm.struts.action.admin.ProcessAction:typeList" />

		<ui:menu-item title="${l.l('Process Queues')}" href="admin/process/queue"
			action="ru.bgcrm.struts.action.admin.ProcessAction:queueList" />
	</ui:menu-group>


	<ui:menu-item title="Custom" href="admin/custom" icon="ti-hummer" action="/admin/custom:null" />

	<ui:menu-group title="${l.l('Execution')}" icon="ti-pulse">
		<ui:menu-item title="${l.l('Run')}" href="admin/run" icon="ti-rocket" action="/admin/run:null" />

		<ui:menu-item title="${l.l('Scheduler')}" href="admin/scheduler" icon="ti-alarm-clock"
			action="/admin/run:scheduler" />
	</ui:menu-group>

	<plugin:include endpoint="<%=ru.bgcrm.plugin.Endpoint.USER_ADMIN_MENU_ITEMS%>"/>
</ui:menu-group>

<ui:menu-item title="Demo" href="demo" icon="ti-game" action="/user/demo:null" />
