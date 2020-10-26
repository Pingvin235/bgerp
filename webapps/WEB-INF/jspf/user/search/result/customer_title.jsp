<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="search_common.jsp"%>

<table style="width: 100%;" class="data mt1">
	<tr>
		<td width="30">ID</td>
		<td>${l.l('Наименование')}</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
			<td>${item.id}</td>
			<td><a href="#UNDEF" onclick="openCustomer( ${item.id} ); return false;">${item.title}
					<c:if test="${not empty item.reference}">
						(${item.reference})
					</c:if>	
				</a>
			</td>			
		</tr>
	</c:forEach>
</table>