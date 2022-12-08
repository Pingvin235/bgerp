<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="/admin/plugin/callboard/work" styleClass="in-mr1">
	<input type="hidden" name="action" value="workTypeList"/>

	<c:url var="url" value="/admin/plugin/callboard/work.do">
	    <c:param name="action" value="workTypeGet"/>
	    <c:param name="id" value="-1"/>
	    <c:param name="returnUrl" value="${form.requestUrl}"/>
  	</c:url>
  	<button type="button" class="btn-green" onclick="openUrlContent('${url}' )">+</button>

	 <u:sc>
		<c:set var="list" value="${allowOnlyCategories}"/>
		<c:set var="hiddenName" value="categoryId"/>
		<c:set var="value" value="${form.param.categoryId}"/>
		<c:set var="style" value="width: 200px;"/>
		<c:set var="placeholder" value="Выберите категорию"/>
		<c:set var='onSelect'>openUrlContent( formUrl( $hidden[0].form ) )</c:set>
		<%@ include file="/WEB-INF/jspf/select_single.jsp"%>
	</u:sc>

	<ui:page-control/>
</html:form>

<table style="width: 100%;" class="data mt1">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>
		<td>${l.l('Title')}</td>
		<td width="30">${l.l('Цвет')}</td>
		<td width="100%">${l.l('Комментарий')}</td>
	</tr>

	<c:forEach var="item" items="${form.response.data.list}">
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
			<c:url var="deleteAjaxCommandAfter" value="openUrlContent( '${form.requestUrl}' )"/>

			<td nowrap="nowrap"><%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%></td>

			<td>${item.id}</td>
			<td nowrap="nowrap">${item.title}</td>
			<td style="background-color: ${item.color};">&nbsp;</td>
			<td>${item.comment}</td>
		</tr>
	</c:forEach>
</table>

<c:set var="title" value="${l.l('Типы работ')}"/>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>