<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="search_common.jsp"%>

<table class="data mt1">
	<tr>
		<td width="30">ID</td>
		<td>${l.l('Title')}</td>
	</tr>
	<c:forEach var="item" items="${frd.list}">
		<tr>
			<td>${item.id}</td>
			<td><a href="/user/customer#${item.id}" onclick="$$.customer.open(${item.id}); return false;">${item.title}
					<c:if test="${not empty item.reference}">
						(${item.reference})
					</c:if>
				</a>
			</td>
		</tr>
	</c:forEach>
</table>