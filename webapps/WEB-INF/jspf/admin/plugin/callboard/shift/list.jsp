<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="/admin/plugin/callboard/work">
	<input type="hidden" name="action" value="shiftList"/>

	 <c:url var="url" value="/admin/plugin/callboard/work.do">
	    <c:param name="action" value="shiftGet"/>
	    <c:param name="id" value="-1"/>
	    <c:param name="returnUrl" value="${form.requestUrl}"/>
  	</c:url>
  	<button type="button" class="btn-green mr1" onclick="openUrlContent('${url}' )">+</button>

	<u:sc>
		<c:set var="list" value="${allowOnlyCategories}"/>
		<c:set var="hiddenName" value="categoryId"/>
		<c:set var="value" value="${form.param.categoryId}"/>
		<c:set var="style" value="width: 200px;"/>
		<c:set var="placeholder" value="Выберите категорию"/>
		<c:set var='onSelect'>openUrlContent( formUrl( $hidden[0].form ) )</c:set>
		<%@ include file="/WEB-INF/jspf/select_single.jsp"%>
	</u:sc>

	<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
</html:form>

<table style="width: 100%;" class="data mt1">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>
		<td width="30">Обозн.</td>
		<td width="50%">${l.l('Наименование')}</td>
		<td width="50%">Комментарий</td>
	</tr>

	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
			<c:url var="editUrl" value="/admin/plugin/callboard/work.do">
				<c:param name="action" value="shiftGet"/>
				<c:param name="id" value="${item.id}"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
			</c:url>
			<c:url var="deleteAjaxUrl" value="/admin/plugin/callboard/work.do">
				<c:param name="action" value="shiftDelete"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>
			<c:url var="deleteAjaxCommandAfter" value="openUrlContent( '${form.requestUrl}' )"/>

			<td nowrap="nowrap"><%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%></td>

			<td>${item.id}</td>
			<c:set var="color"><c:if test="${not empty item.color}">style="background-color:${item.color}"</c:if></c:set>
			<td ${color}>
				${item.symbol}
			</td>
			<td>${item.title}</td>
			<td>${item.comment}</td>
		</tr>
	</c:forEach>
</table>

<c:set var="title" value="${l.l('Шаблоны смен')}"/>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>