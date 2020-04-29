<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html>
<head>
	<title><%@ include file="/WEB-INF/jspf/version.jsp"%></title>
	<%@ include file="/WEB-INF/jspf/script_css.jsp"%>
</head>

<body>
	<c:set var="uiidButtonMenu" value="${u:uiid()}"/>
	
	<%@ include file="/WEB-INF/jspf/login_form.jsp"%>
	<%@ include file="/WEB-INF/jspf/error_dialog.jsp"%>
	
	<div id="headWrap">
		<div id="head">
			<button id="${uiidButtonMenu}" class="btn-green btn-start">+</button>
			
			<div id="taskPanelWrap"><span id="taskPanel"></span></div>
			
			<span class="right">
				<span id="filterCounterPanel"></span>
				
				<div id="colorPickerModal" style="display: none;">
					<span>Цвет для счетчика фильтра:</span>
					<span class='colorPicker-picker' style='display: inline-block;'></span>
					<script>$(".colorPicker-picker").colorPicker();</script>
				</div>
				<a id="messagesLink" href="#">0</a>
				
				&nbsp;
				
				<c:set var="uiidProfile" value="${u:uiid()}"/>
				<a id="${uiidProfile}" class="profile" href="#"> 
					${ctxUser.title}
				</a>
			</span>
		</div>
	</div>	
		
	<div id="title">
		<div id="empty" class="status"></div>
		<div title="Буфер открытых объектов, 
вызвать можно также длительным нажатем ЛКМ.">
			<u:sc>
				<c:set var="id" value="objectBuffer"/>
			
				<c:set var="hiddenName" value="object"/>
				<c:set var="prefixText">
					${l.l('Объектов в буфере:')}&nbsp;<span class="object-count">0</span>
				</c:set>
								
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
			</u:sc>
		</div>	
	</div>
	
	<div id="content" class="tableIndent"></div>
	
	<div style="height: 0px; max-height: 0px;">
		<!-- при размещении меню перед таблицей при его отображении сдвигается кнопка -->
		<c:set var="uiidCommandMenu" value="${u:uiid()}"/>
		
		<ul id="${uiidCommandMenu}" style="display: none;" class="menu">
			<%-- инклуд собирает инициализацию меню в переменную menuItemsJS --%>
			<%@ include file="/WEB-INF/jspf/user/menu.jsp"%>
		</ul>
		
		<ul id="messagesMenu" style="display: none;"></ul>
		
		<c:set var="uiidProfileMenu" value="${u:uiid()}"/>
		
		<ul id="${uiidProfileMenu}" style="display: none;">
			<li><ui:user-link id="${ctxUser.id}" text="${l.l('Профиль')}"/></li>
			<li><a href="/user/userProcesses" onclick="$$.shell.followLink(this.href, event)">${l.l('Мои процессы')}</a></li>
			<%-- отсутствие onclick отключит этот пункт меню --%>
			<li><a href="UNDEF" onclick="$$.ajax.post('/login.do?action=logout').done(() => window.location.href = '/user'); return false;">${l.l('Выход')}</a></li>
		</ul>
	</div>
	
	<%-- TODO: Переместить большую часть функций в bgcrm.shell, файл crm.shell.js --%>
	<script>
		const menuItems = bgcrm.menuItems = {};
		
		${menuItemsJS}
			
		<%-- перенос настроек персонализации в bgcrm.pers для использования в JS скриптах --%>
		<c:forEach var="pair" items="${ctxUser.personalizationMap}">
			bgcrm.pers['${pair.key}'] = '${pair.value}';
		</c:forEach>

		bgerp.shell.mapUrl = function (href) {
			let url = null;
			let bgcolor = null;

			let m = null;
			if (false) {}
			// add else expressions
			<c:set var="endpoint" value="user.url.jsp"/>
			<%@ include file="/WEB-INF/jspf/plugin_include.jsp"%>

			return url ? {url: url, bgcolor: bgcolor} : null;
		};	

		bgerp.shell.initBuffer();

		$(function () {
			const createMenu = $$.ui.menuInit;
			
			createMenu($("#${uiidButtonMenu}"), $("#${uiidCommandMenu}"), "left");
			
			createMenu($("#messagesLink"), $("#messagesMenu"), "right");
			
			createMenu($("#${uiidProfile}"), $("#${uiidProfileMenu}"), "right");

			const href = window.location.href;

			<%-- <c:set var="openPinned" value="${ctxUser.configMap['on.login.open.pinned']}"/>
			<c:if test="${not empty openPinned}">
				$$.shell.contentLoad('${openPinned}', false, true);
			</c:if> --%>

			<c:forEach var="openPinned" items="${u:toList(ctxUser.configMap['on.login.open.pinned'])}">
				$$.shell.contentLoad('${openPinned}', false, true);
			</c:forEach>
			
			$$.shell.contentLoad(href);
			
			<c:if test="${setup['pooling.enable'] ne 0 and ctxUser.configMap['pooling.enable'] ne 0}">
				timer();
			</c:if>
			
			const setSize = function () {
				// куда деваются ещё 10 пикселей - непонятно
				const width = $('#headWrap').width() - 
							$('#head > button').outerWidth() - 
							$('#head > span.right').outerWidth() - 10;
				$('#taskPanel').css({'max-width': width});
			};
			
			setSize();
			
			$(window).resize(setSize);
			$('#head > span.right').bind('DOMSubtreeModified', setSize);
		});
	</script>
</body>
</html>
