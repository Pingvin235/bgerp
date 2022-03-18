<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${processType.properties.configMap['git:processShowGit'] eq 1}">
	<c:url var="url" value="/user/plugin/git/git.do">
		<c:param name="action" value="git" />
		<c:param name="processId" value="${process.id}" />
	</c:url>
	$tabs.tabs('add', "${url}", "GIT");
</c:if>