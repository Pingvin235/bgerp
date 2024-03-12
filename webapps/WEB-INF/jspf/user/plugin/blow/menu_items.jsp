<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-item title="${l.l('Blow план')}" href="blow/board" icon="ti-layout"
	action="org.bgerp.plugin.pln.blow.action.BoardAction:board"
	command="/user/plugin/blow/board.do?action=board" />
