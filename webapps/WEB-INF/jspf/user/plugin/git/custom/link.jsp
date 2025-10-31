<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="error" value="${frd.error}"/>
<c:set var="branch" value="${frd.branch}"/>

<c:if test="${not empty error or not empty branch}">
	<span class="normal"> [GIT:
	<c:choose>
		<c:when test="${not empty error}">
			<span class="error">${error}</span><%--
	--%></c:when>
		<c:otherwise>
			<c:set var="action" value="${su.substringBefore(form.requestURI, '.')}:update"/>
			<c:url var="url" value="${form.requestURI}">
				<c:param name="method" value="update"/>
				<c:param name="branch" value="${branch}"/>
			</c:url>
			<c:set var="onclick">
				<c:choose>
					<c:when test="${ctxUser.checkPerm(action)}">$$.ajax.post('${url}').done(() => $$.ajax.loadContent('${form.returnUrl}', this))</c:when>
					<c:otherwise>$$.shell.message.show('${l.l('Insufficient Permissions')}', 'Plugin GIT / Custom / Update')</c:otherwise>
				</c:choose>;
				return false;
			</c:set>
			<a href="#" onclick="${onclick}" title="${l.l('Update')}">${branch}</a>
			${frd.commit}<%--
	--%></c:otherwise>
	</c:choose><%--
	--%>]</span>
</c:if>