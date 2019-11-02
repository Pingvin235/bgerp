<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="memoList" value="${form.response.data.memoList}"/>

<c:if test="${not empty memoList}">
	<h2>Примечения</h2>
	
	<table class="data mt1" style="width: 100%;">
		<tr>
			<td width="100%">Тема</td>
			<td>Дата</td>
			<td>Пользователь</td>
		</tr>
		<c:forEach var="memo" items="${memoList}">
			<tr>
	  			<td>${memo.title}</td>
				<td nowrap="nowrap">${u:formatDate( memo.time, 'ymdhms' )}</td>
				<td nowrap="nowrap">${memo.user}</td>
			</tr>
			<tr>
				<td colspan="3">${u:htmlEncode( memo.text )}</td>
			</tr>
		</c:forEach>
	</table>
</c:if>