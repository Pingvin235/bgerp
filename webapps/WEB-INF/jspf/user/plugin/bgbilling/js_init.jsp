<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

bgcrm.objectTypeTitles['bgbilling-commonContract'] = "Единый договор";

<c:set var="dbInfo" value="${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager}"/>
<c:forEach items="${dbInfo.dbInfoList}" var="db">
	bgcrm.objectTypeTitles['contract_${db.id}'] = "Договор:${db.title}";
</c:forEach>