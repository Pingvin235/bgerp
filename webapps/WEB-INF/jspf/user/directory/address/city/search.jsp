<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:page-control pageFormId="${formUiid}"/>

<table class="data">
	<tr>
		<td width="30">&nbsp;</td>
		<td width="30">ID</td>
		<td width="30">${l.l('Страна')}</td>
		<td width="100%">${l.l('Город')}</td>
	</tr>
	<c:forEach var="item" items="${frd.list}">
		<tr>
			<c:url var="editUrl" value="${doUrl}">
				<c:param name="action" value="addressGet"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
				<c:param name="selectTab" value="${form.param.selectTab}"/>
				<c:param name="addressCountryTitle" value="${form.param.addressCountryTitle}"/>
				<c:param name="addressCityId" value="${item.id}"/>
			</c:url>
			<c:url var="delUrl" value="${doUrl}">
				<c:param name="action" value="addressDelete"/>
				<c:param name="addressCityId" value="${item.id}"/>
			</c:url>

			<c:url var="url" value="${doUrl}">
				<c:param name="selectTab" value="${form.param.selectTab}"/>
				<c:param name="addressCityId" value="${item.id}"/>
				<c:param name="searchMode" value="item"/>
			</c:url>

			<%@ include file="../edit_td.jsp"%>
			<td>${item.id}</td>
			<td nowrap="nowrap">${item.addressCountry.title}</td>
			<td><a href="#" onclick="$$.ajax.loadContent('${url}', this); return false">${item.title}</a></td>
		</tr>
	</c:forEach>
</table>
