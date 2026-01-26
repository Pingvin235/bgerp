<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<table class="data mt05 hl">
	<tr>
		<td width="30">ID</td>
		<td>${l.l('Type')}</td>
		<td>${l.l('Scheduled')}</td>
		<td>${l.l('Executed')}</td>
		<td width="30">&#160;</td>
	</tr>
	<c:forEach var="item" items="${frd.list}" >
		<tr>
			<td>${item.id}</td>
			<td>${config.getType(item.typeId).title}</td>
			<td>${tu.format(item.scheduledTime, 'ymdhm')}</td>
			<td>${tu.format(item.executedTime, 'ymdhms')}</td>
			<td nowrap="nowrap"></td>
		</tr>
	</c:forEach>
</table>
