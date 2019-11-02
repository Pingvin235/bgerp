<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="customer" value="${form.response.data.customer}"/>

<c:set var="formUiid" value="${u:uiid()}"/>
<html:form action="/user/customer" onsubmit="return false;" styleClass="center500 in-w100p" styleId="${formUiid}">
	<html:hidden property="id"/>
	<input type="hidden" name="action" value="customerUpdate"/>
	
	<h2>ID</h2>
	<input type="text" readonly="readonly" value="${customer.id}"/>
	
	<h2>Название</h2>
	<html:text property="title" value="${customer.title}" disabled="${customer.titlePatternId ne -1}"/>
	
	<h2>Шаблон названия</h2>
	<u:sc>
		<c:set var="valuesHtml">
			<li value="-1">Без шаблона</li>
			<li value="0">Персональный шаблон</li>
			<c:forEach var="item" items="${patternList}">
				<li value="${item.id}">${item.title} (${item.pattern})</li>
			</c:forEach>
		</c:set>
		<c:set var="hiddenName" value="titlePatternId"/>
		<c:set var="value" value="${customer.titlePatternId}"/>
		<c:set var="onSelect">
			var form = $('#${formUiid}')[0];
			form.elements['title'].disabled = form.elements.titlePatternId.value!=-1;
			form.elements['titlePattern'].disabled = form.elements.titlePatternId.value!=0;
		</c:set>	
		<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
	</u:sc>		
			
	<h2>Персональный шаблон названия</h2>
	<html:text property="titlePattern" disabled="${customer.titlePatternId ne 0}" value="${customer.titlePattern}"/>
	
	<h2>Группа параметров</h2>
	<u:sc>
		<c:set var="valuesHtml">
			<li value="0">Группа не установлена (все параметры)</li>
			<c:forEach var="item" items="${parameterGroupList}">
				<li value="${item.id}">${item.title}</li>
			</c:forEach>
		</c:set>
		<c:set var="hiddenName" value="parameterGroupId"/>
		<c:set var="value" value="${customer.paramGroupId}"/>
		<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
	</u:sc>	
		
	<div class="in-mr05 mt1">
	 	<c:url var="url" value="customer.do" >
			<c:param name="id" value="${customer.id}"/>
		</c:url>
	 
		<button class="btn-grey" onclick="if( sendAJAXCommand( formUrl( this.form ) ) ){ openUrlContent( '${url}' ) }">ОК</button>
		<button class="btn-grey" onclick="openUrlContent( '${url}' )">Отмена</button>
	</div>
</html:form>

<c:set var="state">Редактирование</c:set>
<c:set var="help">http://www.bgcrm.ru/doc/3.0/manual/kernel/customer.html</c:set>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>