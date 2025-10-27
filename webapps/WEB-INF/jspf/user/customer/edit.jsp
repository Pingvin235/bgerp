<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="customer" value="${frd.customer}"/>

<c:set var="formUiid" value="${u:uiid()}"/>
<html:form action="/user/customer" onsubmit="return false;" styleClass="center500 in-w100p" styleId="${formUiid}">
	<html:hidden property="id"/>
	<input type="hidden" name="method" value="customerUpdate"/>

	<h2>ID</h2>
	<input type="text" readonly="readonly" value="${customer.id}"/>

	<h2>${l.l('Название')}</h2>
	<html:text property="title" value="${customer.title}" disabled="${customer.titlePatternId ne -1}"/>

	<h2>${l.l('Title pattern')}</h2>
	<ui:combo-single hiddenName="titlePatternId" value="${customer.titlePatternId}" onSelect="
		var form = $('#${formUiid}')[0];
		form.elements['title'].disabled = form.elements.titlePatternId.value!=-1;
		form.elements['titlePattern'].disabled = form.elements.titlePatternId.value!=0;
	">
		<jsp:attribute name="valuesHtml">
			<li value="-1">${l.l('No pattern')}</li>
			<li value="0">${l.l('Personal pattern')}</li>
			<c:forEach var="item" items="${patternList}">
				<li value="${item.id}">${item.title} (${item.pattern})</li>
			</c:forEach>
		</jsp:attribute>
	</ui:combo-single>

	<h2>${l.l('Personal title pattern')}</h2>
	<html:text property="titlePattern" disabled="${customer.titlePatternId ne 0}" value="${customer.titlePattern}"/>

	<h2>${l.l('Группа параметров')}</h2>
	<ui:combo-single hiddenName="parameterGroupId" value="${customer.paramGroupId}">
		<jsp:attribute name="valuesHtml">
			<li value="0">${l.l('Группа не установлена (все параметры)')}</li>
			<c:forEach var="item" items="${parameterGroupList}">
				<li value="${item.id}">${item.title}</li>
			</c:forEach>
		</jsp:attribute>
	</ui:combo-single>

	<div class="in-mr05 mt1">
	 	<c:url var="url" value="customer.do" >
			<c:param name="id" value="${customer.id}"/>
		</c:url>

		<button class="btn-grey" onclick="$$.ajax.post(this).done(() => $$.ajax.loadContent('${url}'))">OK</button>
		<button class="btn-white" onclick="$$.ajax.loadContent('${url}')">${l.l('Cancel')}</button>
	</div>
</html:form>

<shell:state text="${l.l('Editor')}" help="kernel/customer.html"/>