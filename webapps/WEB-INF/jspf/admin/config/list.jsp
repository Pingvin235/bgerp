<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>


<html:form action="admin/config" styleClass="in-mr1">
	<input type="hidden" name="action" value="list"/>

	<c:url var="url" value="/admin/config.do">
		<c:param name="action" value="get"/>
		<c:param name="id" value="-1"/>
		<c:param name="returnUrl" value="${form.requestUrl}"/>
	</c:url>
	<button type="button" class="btn-green" onclick="openUrlContent( '${url}' )">+</button>
	
	<ui:input-text name="filter" value="${form.param['filter']}" size="20" placeholder="Фильтр" title="Фильтр по содержимому конфигурации"
    	onSelect="openUrlContent(formUrl(this.form)); return false;"/>

	<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
</html:form>

<table style="width: 100%;" class="data mt1">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>
		<td width="50">Активный</td>
		<td width="100%">Наименование</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}">
		<c:set var="item" scope="request" value="${item}"/>
		<jsp:include page="config_item.jsp"/>
	</c:forEach>
</table>

<c:set var="title" value="Конфигурация"/>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>
<c:set var="help" value="http://www.bgcrm.ru/doc/3.0/manual/kernel/setup.html#config"/>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>