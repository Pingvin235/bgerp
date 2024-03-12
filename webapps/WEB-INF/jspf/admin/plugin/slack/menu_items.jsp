<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-item title="${l.l('Slack Конфигурация')}" href="admin/slack/config"
	action="ru.bgcrm.plugin.slack.action.ConfigAction:null"
	command="/admin/plugin/slack/config.do" />
