<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="../address_page_control.jsp"%>

<table class="data hl">
	<tr>
		<td width="30">&nbsp;</td>
		<td width="30">ID</td>
		<td width="100%">${l.l('Страна')}</td>
	</tr>
	<c:forEach var="item" items="${frd.list}" varStatus="status">
		<tr>
			<c:url var="editUrl" value="${doUrl}">
				<c:param name="action" value="addressGet"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
				<c:param name="selectTab" value="${form.param.selectTab}"/>
				<c:param name="addressCountryId" value="${item.id}"/>
			</c:url>
			<c:url var="delUrl" value="${doUrl}">
				<c:param name="action" value="addressDelete"/>
				<c:param name="addressCountryId" value="${item.id}"/>
			</c:url>

			<c:url var="url" value="${doUrl}">
				<c:param name="selectTab" value="${form.param.selectTab}"/>
				<c:param name="addressCountryId" value="${item.id}"/>
				<c:param name="searchMode" value="city"/>
			</c:url>

			<%@ include file="../edit_td.jsp"%>
			<td>${item.id}</td>
			<td><a href="#" onclick="$$.ajax.loadContent('${url}', this); return false">${item.title}</a></td>
		</tr>
	</c:forEach>
</table>
