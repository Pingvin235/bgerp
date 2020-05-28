<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="admin/user">
	<input type="hidden" name="action" value="groupList"/>
	
	<table>
		<tr>
			<td width="100%">
				 <c:url var="url" value="/admin/work.do">
				    <c:param name="action" value="shiftGet"/>
				    <c:param name="id" value="-1"/>
				    <c:param name="returnUrl" value="${form.requestUrl}"/>
			  	</c:url>			  	
			</td>
			<td>
				<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
			</td>
		</tr>
	</table>
</html:form>

<table style="width: 100%;" class="data">
	<tr>
		<!-- <td width="30">&#160;</td> -->
		<td width="30">ID</td>
		<td width="50%">Наименование</td>
		<td width="50%">Комментарий</td>
	</tr>
	 
	<c:forEach var="item" items="${form.response.data.workDaysCalendarList}">
		<tr>
			<c:url var="editUrl" value="/admin/work.do">
				<c:param name="action" value="workDaysCalendarGet"/>
				<c:param name="id" value="${item.id}"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
			</c:url>			
						
			<td>${item.id}</td>
			<td><a href="#UNDEF" onclick="openUrlContent( '${editUrl}' ); return false;">${item.title}</a></td>
			<td>${item.comment}</td>
		</tr>
	</c:forEach>
</table>

<c:set var="title" value="Календарь рабочих дней"/>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>