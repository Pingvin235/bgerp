<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="../address_page_control.jsp"%>

<table class="data">
	<tr>
		<td width="30">&nbsp;</td>
		<td width="30">ID</td>
		<td width="10%">${l.l('Страна')}</td>
		<td width="10%">${l.l('Город')}</td>
		<td width="10%">${l.l('Улица')}</td>
		<td>${l.l('Дом')}</td>
		<td>${l.l('Индекс')}</td>
		<td>${l.l('Коментарий')}</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
			<c:url var="editUrl" value="${doUrl}">
				<c:param name="action" value="addressGet"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
				<c:param name="selectTab" value="${form.param.selectTab}"/>
				<c:param name="addressCountryTitle" value="${form.param.addressCountryTitle}"/>
				<c:param name="addressCityTitle" value="${form.param.addressCityTitle}"/>
				<c:param name="addressItemTitle" value="${form.param.addressItemTitle}"/>
				<c:param name="addressCityId" value="${form.param.addressCityId}"/>
				<c:param name="addressItemId" value="${form.param.addressItemId}"/>
				<c:param name="addressHouseId" value="${item.id}"/>
			</c:url>

			<c:url var="delUrl" value="${doUrl}">
				<c:param name="action" value="addressDelete"/>
				<c:param name="addressHouseId" value="${item.id}"/>
			</c:url>

			<%@ include file="../edit_td.jsp"%>
			<td align="right">${item.id}</td>
			<td nowrap="nowrap">${item.addressStreet.addressCity.addressCountry.title}</td>
			<td nowrap="nowrap">${item.addressStreet.addressCity.title}</td>
			<td nowrap="nowrap">${item.addressStreet.title}</td>
			<td><c:if test="${item.house gt 0}">${item.house}</c:if>${item.frac}</td>
			<td>${item.postIndex}</td>
			<td>${item.comment}</td>
		</tr>
	</c:forEach>
</table>
