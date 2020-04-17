<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-group title="Создать">
	<jsp:attribute name="subitems">
		<ui:menu-item title="Контрагент"
			action="ru.bgcrm.struts.action.CustomerAction:customerCreate"
			command="createCustomerAndEdit(this)" />
	</jsp:attribute>
</ui:menu-group>

<ui:menu-item title="Профиль" href="profile"
	action="ru.bgcrm.struts.action.SearchAction"
	command="profile.do?action=getUserProfile" hidden="1" />

<ui:menu-item title="Мои процессы" href="userProcesses"
	action="ru.bgcrm.struts.action.ProcessAction"
	command="/user/process.do?action=userProcessList" hidden="1" />

<ui:menu-item title="Поиск" href="search"
	action="ru.bgcrm.struts.action.SearchAction" command="/user/search.do" />

<ui:menu-item title="Новости" href="news"
	action="ru.bgcrm.struts.action.NewsAction:newsList"
	command="/user/news.do?action=newsList&read=0" />

<ui:menu-item title="Адреса" href="directory/address"
	action="ru.bgcrm.struts.action.DirectoryAddressAction"
	command="/user/directory/address.do" />

<ui:menu-item title="Сообщения" href="messageQueue"
	action="ru.bgcrm.struts.action.MessageAction:messageList"
	command="/user/message.do?action=messageList" />

<ui:menu-item title="Процессы" href="process/queue"
	action="ru.bgcrm.struts.action.ProcessAction:queue"
	command="/user/process.do?action=queue" />

<ui:menu-item title="Распределения адресов" href="addressDistribution"
	action="ru.bgcrm.struts.action.AddressDistributionAction"
	command="/user/distribution.do" />

<ui:menu-group title="Организация работ">
	<jsp:attribute name="subitems">
	
		<ui:menu-item title="Календарь рабочих дней" href="workCalendar"
			action="ru.bgcrm.struts.action.admin.WorkAction:workDaysCalendarList"
			command="/admin/work.do?action=workDaysCalendarList" />
	
		<ui:menu-item title="Типы работ" href="workTypes"
			action="ru.bgcrm.struts.action.admin.WorkAction:workTypeList"
			command="/admin/work.do?action=workTypeList" />
	
		<ui:menu-item title="Шаблоны смен" href="shiftTypes"
			action="ru.bgcrm.struts.action.admin.WorkAction:shiftList"
			command="/admin/work.do?action=shiftList" />
	
		<ui:menu-item title="График дежурств" href="callboard"
			action="ru.bgcrm.struts.action.admin.WorkAction:callboardGet"
			command="/admin/work.do?action=callboardGet" />
	
		<ui:menu-item title="План работ" href="workPlan"
			action="ru.bgcrm.struts.action.WorkAction:planGet"
			command="/user/work.do?action=planGet" />
	</jsp:attribute>
</ui:menu-group>

<ui:menu-group title="Администрирование">
	<jsp:attribute name="subitems">
	
		<ui:menu-item title="Параметры" href="param"
			action="ru.bgcrm.struts.action.admin.DirectoryAction"
			command="/admin/directory.do" />
		
		<ui:menu-group title="Пользователи">
			<jsp:attribute name="subitems">

				<ui:menu-item title="Наборы прав" href="userPermset"
					action="ru.bgcrm.struts.action.admin.UserAction:permsetList"
					command="/admin/user.do?action=permsetList" />

				<ui:menu-item title="Группы" href="userGroup"
					action="ru.bgcrm.struts.action.admin.UserAction:groupList"
					command="/admin/user.do?action=groupList" />

				<ui:menu-item title="Пользователи" href="users"
					action="ru.bgcrm.struts.action.admin.UserAction:userList"
					command="/admin/user.do?action=userList" />
			</jsp:attribute>
		</ui:menu-group>
		
		<ui:menu-group title="Процессы">
			<jsp:attribute name="subitems">
			
				<ui:menu-item title="Статусы процессов" href="processStatus"
					action="ru.bgcrm.struts.action.admin.ProcessAction:statusList"
					command="/admin/process.do?action=statusList" />
				
				<ui:menu-item title="Типы процессов" href="processType"
					action="ru.bgcrm.struts.action.admin.ProcessAction:typeList"
					command="/admin/process.do?action=typeList" />
				
				<ui:menu-item title="Очереди процессов" href="processQueueDir"
					action="ru.bgcrm.struts.action.admin.ProcessAction:queueList"
					command="/admin/process.do?action=queueList" />
			</jsp:attribute>
		</ui:menu-group>
		
		<ui:menu-item title="Динамический код" href="dyncode"
			action="ru.bgcrm.struts.action.admin.DynamicAction"
			command="/admin/dynamic.do" />
		
		<ui:menu-item title="WEB запросы" href="requestLog"
			action="ru.bgcrm.struts.action.admin.WebRequestAction"
			command="/admin/webRequest.do" />
		
		<ui:menu-item title="Конфигурация" href="config"
			action="ru.bgcrm.struts.action.admin.ConfigAction:list"
			command="/admin/config.do?action=list" />
		
		<ui:menu-group title="Приложение">
			<jsp:attribute name="subitems">
			
				<ui:menu-item title="Статус приложения" href="app/status"
					action="ru.bgcrm.struts.action.admin.AppAction:status"
					command="/admin/app.do?action=status"/>
					
				<ui:menu-item title="Авторизовавашиеся пользователи" href="app/logged"
					action="ru.bgcrm.struts.action.admin.AppAction:userLoggedList"
					command="/admin/app.do?action=userLoggedList"/>
			</jsp:attribute>
		</ui:menu-group>
		
		<c:set var="endpoint" value="user.admin.menu.items.jsp" />
		<%@ include file="/WEB-INF/jspf/plugin_include.jsp"%>
	</jsp:attribute>
</ui:menu-group>

<ui:menu-item title="Логирование" href="log"
	action="ru.bgcrm.struts.action.LogAction" command="/user/log.do" />

<c:set var="endpoint" value="user.menu.items.jsp"/>
<%@ include file="/WEB-INF/jspf/plugin_include.jsp"%>