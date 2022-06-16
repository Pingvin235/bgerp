<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="dbInfo" value="${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager}"/>
<c:forEach items="${dbInfo.dbInfoList}" var="db">
	$$.objectTypeTitles['contract_${db.id}'] = "${l.l('Договор')}:${db.title}";
</c:forEach>