<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="../address_page_control.jsp"%>

<table style="width: 100%;" class="data">
	<tr>
		<td width="30">&nbsp;</td>
		<td width="30">ID</td>
		<td width="100%">Страна</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}" varStatus="status">
		<tr>
			<c:url var="editUrl" value="${doUrl}">
				<c:param name="action" value="addressGet"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
				<c:param name="selectTab" value="${form.param.selectTab}"/>
				<c:param name="addressCountryId" value="${item.id}"/>				
			</c:url>
			<c:url var="deleteAjaxUrl" value="${doUrl}">
				<c:param name="action" value="addressDelete"/>
				<c:param name="addressCountryId" value="${item.id}"/>
			</c:url>
			<c:set var="deleteAjaxCommandAfter">openUrlContent('${form.requestUrl}')</c:set>
		
			<c:url var="url" value="${doUrl}">
				<c:param name="selectTab" value="${form.param.selectTab}"/>
				<c:param name="addressCountryId" value="${item.id}"/>
				<c:param name="searchMode" value="city"/>
			</c:url>
			 
			<td nowrap="nowrap"><%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%></td>
			<td>${item.id}</td>			
			<td><a href="" onclick="openUrlContent('${url}'); return false">${item.title}</a></td>
		</tr>
	</c:forEach>
</table>
