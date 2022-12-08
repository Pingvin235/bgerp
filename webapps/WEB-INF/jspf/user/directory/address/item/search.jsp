<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:page-control pageFormId="${formUiid}"/>

<table class="data">
	<tr>
		<td width="30">&nbsp;</td>
		<td width="30">ID</td>
		<td>${l.l('Страна')}</td>
		<td>${l.l('Город')}</td>
		<td width="90%">${title}</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
			<c:url var="editUrl" value="${doUrl}">
				<c:param name="action" value="addressGet"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
				<c:param name="selectTab" value="${form.param.selectTab}"/>
				<c:param name="addressCityId" value="${form.param.addressCityId}"/>
				<c:param name="addressItemId" value="${item.id}"/>
				<c:param name="addressCountryTitle" value="${form.param.addressCountryTitle}"/>
				<c:param name="addressCityTitle" value="${form.param.addressCityTitle}"/>
			</c:url>

			<c:url var="delUrl" value="${doUrl}">
				<c:param name="action" value="addressDelete"/>
				<c:param name="selectTab" value="${form.param.selectTab}"/>
				<c:param name="addressItemId" value="${item.id}"/>
			</c:url>

			<%@ include file="../edit_td.jsp"%>
			<td>${item.id}</td>
			<td nowrap="nowrap">${item.addressCity.addressCountry.title}</td>
			<td nowrap="nowrap">${item.addressCity.title}</td>

			<c:choose>
				<c:when test="${form.param.selectTab eq 'street'}">
					<c:url var="url" value="${doUrl}">
						<c:param name="selectTab" value="${form.param.selectTab}"/>
						<c:param name="addressItemId" value="${item.id}"/>
						<c:param name="searchMode" value="house"/>
					</c:url>
					<td><a href="#" onclick="$$.ajax.loadContent('${url}', this); return false">${item.title}</a></td>
				</c:when>
				<c:otherwise><td>${item.title}</td></c:otherwise>
			</c:choose>
		</tr>
	</c:forEach>
</table>
