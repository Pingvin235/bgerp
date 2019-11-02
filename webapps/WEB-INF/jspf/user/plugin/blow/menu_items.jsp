<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-item title="${l.l('Blow план')}" href="/user/blow/board"
	action="ru.bgerp.plugin.blow.struts.action.BoardAction:board"
	command="/user/plugin/blow/board.do?action=board" />


