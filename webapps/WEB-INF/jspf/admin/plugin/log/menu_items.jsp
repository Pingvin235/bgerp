<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-item title="Action Log" href="admin/log/action" icon="ti-eye"
	action="org.bgerp.plugin.svc.log.action.admin.ActionLogAction:null"
	command="/admin/plugin/log/action.do" />
