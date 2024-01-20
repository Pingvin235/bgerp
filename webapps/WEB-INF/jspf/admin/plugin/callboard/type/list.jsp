<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="/admin/plugin/callboard/work" styleClass="in-mr1">
	<input type="hidden" name="action" value="workTypeList"/>

	<c:url var="url" value="/admin/plugin/callboard/work.do">
		<c:param name="action" value="workTypeGet"/>
		<c:param name="id" value="-1"/>
		<c:param name="returnUrl" value="${form.requestUrl}"/>
	</c:url>
	<button type="button" class="btn-green" onclick="$$.ajax.loadContent('${url}')">+</button>

	<ui:select-single list="${allowOnlyCategories}" hiddenName="categoryId" value="${form.param.categoryId}" onSelect="$$.ajax.loadContent($hidden[0].form)"
		style="width: 200px;" placeholder="Выберите категорию"/>

	<ui:page-control/>
</html:form>

<table class="data mt1">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>
		<td>${l.l('Title')}</td>
		<td width="30">${l.l('Цвет')}</td>
		<td width="100%">${l.l('Комментарий')}</td>
	</tr>

	<c:forEach var="item" items="${frd.list}">
		<tr>
			<c:url var="editUrl" value="/admin/plugin/callboard/work.do">
				<c:param name="action" value="workTypeGet"/>
				<c:param name="id" value="${item.id}"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
			</c:url>
			<c:url var="deleteAjaxUrl" value="/admin/plugin/callboard/work.do">
				<c:param name="action" value="workTypeDelete"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>
			<c:url var="deleteAjaxCommandAfter" value="$$.ajax.loadContent('${form.requestUrl}')"/>

			<td nowrap="nowrap"><%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%></td>

			<td>${item.id}</td>
			<td nowrap="nowrap">${item.title}</td>
			<td style="background-color: ${item.color};">&nbsp;</td>
			<td>${item.comment}</td>
		</tr>
	</c:forEach>
</table>

<shell:title text="${l.l('Типы работ')}"/>
<shell:state/>