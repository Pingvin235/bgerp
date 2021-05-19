<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:choose>
	<c:when test="${item.linkedObjectType eq 'slack-channel'}">
		<tr>
			<td>${delButton}</td>
			<td>0</td>
			<td>Slack</td>
			<td>${item.linkedObjectTitle}</td>
		</tr>
	</c:when>
</c:choose>	
