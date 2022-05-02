<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-item title="Backup" href="admin/backup" icon="ti-harddrives"
	action="org.bgerp.plugin.svc.backup.action.admin.BackupAction:null"
	command="/admin/plugin/backup/backup.do" />
