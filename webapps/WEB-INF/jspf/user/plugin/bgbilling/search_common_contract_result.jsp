<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="pageFormId" value="searchForm-customer" scope="request"/>
<%@ include file="/WEB-INF/jspf/page_control.jsp"%>

<table style="width: 100%; text-align: center;" class="data">	
		<tr>
			<td width="30">ID</td>		
			<td>Договор</td>
			<td>Адрес</td>
		</tr>
	
		<c:forEach var="item" items="${form.response.data.list}">
			<tr>
				<td>${item.id}</td>
				<td><a href="#UNDEF" onclick="bgbilling_openCommonContract( '${item.id}' ); return false;">${item.formatedNumber}</a></td>			
				<td>${item.address.value}</td>			
			</tr>
		</c:forEach>
	
</table>
