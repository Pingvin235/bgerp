<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<!DOCTYPE html>

<html>
<head>
	<title><%@ include file="/WEB-INF/jspf/title.jsp"%></title>
	<%@ include file="/WEB-INF/jspf/script_css.jsp"%>
	<%@ include file="/WEB-INF/jspf/datepicker_l10n.jsp"%>
</head>

<body>
	<c:set var="uiidButtonMenu" value="${u:uiid()}"/>

	<%@ include file="/WEB-INF/jspf/login_dialog.jsp"%>
	<%@ include file="/WEB-INF/jspf/message_dialog.jsp"%>

	<div id="headWrap">
		<div id="head">
			<button id="${uiidButtonMenu}" class="btn-green btn-start icon"><i class="ti-menu"></i></button>

			<div id="taskPanelWrap"><span id="taskPanel"></span></div>

			<span class="right">
				<span id="filterCounterPanel"></span>

				<div id="colorPickerModal" style="display: none;">
					<span>${l.l('Цвет для счетчика фильтра')}:</span>
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
		<div title="${l.l('Буфер открытых объектов')}">
			<ui:combo-single id="objectBuffer" hiddenName="object">
				<jsp:attribute name="prefixText">
					${l.l('Объектов в буфере')}:&nbsp;<span class="object-count">0</span>
				</jsp:attribute>
			</ui:combo-single>
		</div>
	</div>

	<div id="content"></div>

	<div style="height: 0px; max-height: 0px;">
		<!-- при размещении меню перед таблицей при его отображении сдвигается кнопка -->
		<c:set var="uiidCommandMenu" value="${u:uiid()}"/>

		<ul id="${uiidCommandMenu}" style="display: none;" class="menu">
			<%-- the include collects menu init to JSP variable menuItemsJS --%>
			<%@ include file="/WEB-INF/jspf/user/menu.jsp"%>
		</ul>

		<ul id="messagesMenu" style="display: none;"></ul>

		<c:set var="uiidProfileMenu" value="${u:uiid()}"/>

		<ul id="${uiidProfileMenu}" style="display: none;">
			<li><ui:user-link id="${ctxUser.id}" text="${l.l('Профиль')}"/></li>
			<li><a href="/user/process/my" onclick="$$.shell.followLink(this.href, event)">${l.l('My Processes')}</a></li>
			<%-- href is required for menu item --%>
			<li><a href="#" onclick="$$.ajax.post('/login.do?method=logout').done(() => window.location.href = '/user'); return false;">${l.l('Выход')}</a></li>
		</ul>

		<ul id="activeContextMenu" style="display: none;">
			<li><a href="#" onclick="$$.shell.closeOthers(); return false;">${l.l('Закрыть другие')}</a></li>
			<li><a href="#" onclick="$$.shell.refreshCurrent(); return false;">${l.l('Обновить')}</a></li>
		</ul>
	</div>

	<script>
		const menuItems = $$.shell.menuItems;
		<%-- generated string, filling out menuItems --%>
		${menuItemsJS}

		<%-- move personal settings to $$.pers for using in JS --%>
		<c:forEach var="pair" items="${ctxUser.personalizationMap}">
			$$.pers['${pair.key}'] = '${pair.value}';
		</c:forEach>

		<%-- mapping for opened objects URLs --%>
		$$.shell.mapUrl = function (href) {
			let url = null;
			let bgcolor = null;

			let m = null;
			if (false) {}
			// add else expressions
			<plugin:include endpoint="user.url.jsp"/>

			return url ? {url: url, bgcolor: bgcolor} : null;
		};

		$$.shell.initBuffer();

		$(function () {
			const createMenu = $$.ui.menuInit;

			createMenu($("#${uiidButtonMenu}"), $("#${uiidCommandMenu}"), "left");

			createMenu($("#messagesLink"), $("#messagesMenu"), "right");

			createMenu($("#${uiidProfile}"), $("#${uiidProfileMenu}"), "right");

			const href = window.location.href;

			<c:forEach var="openPinned" items="${u.toList(ctxUser.configMap['on.login.open.pinned'])}">
				$$.shell.contentLoad('${openPinned}', {pinned: true});
			</c:forEach>

			$$.shell.contentLoad(href);

			<c:if test="${setup['pooling.enable'] ne 0 and ctxUser.configMap['pooling.enable'] ne 0}">
				$$.timer.init();
			</c:if>

			const setSize = function () {
				// isn't clear where these 10 pixels are gone
				const width = $('#headWrap').width() -
							$('#head > button').outerWidth() -
							$('#head > span.right').outerWidth() - 10;
				$('#taskPanel').css({'max-width': width});
			};

			setSize();

			$(window).resize(setSize);
			$('#head > span.right').bind('DOMSubtreeModified', setSize);

			<%@ include file="/WEB-INF/jspf/ui_init_js.jsp"%>
		});
	</script>

	<span id="scroll-to-top">
		<i class="ti-arrow-up"></i>
	</span>
</body>
</html>
