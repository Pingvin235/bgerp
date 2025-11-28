<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<script>
	<c:set var="dbInfo" value="${plugin.dbInfoManager}"/>
	<c:forEach items="${dbInfo.dbInfoList}" var="db">
		$$.objectTypeTitles['contract_${db.id}'] = "Договор:${db.title}";
	</c:forEach>
</script>