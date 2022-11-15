<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
<html:form styleId="${uiid}" action="${form.httpRequestURI}" styleClass="mb1">
	<html:hidden property="action"/>
	<html:hidden property="id"/>
	<html:hidden property="ifaceId"/>
	<html:hidden property="ifaceState"/>

	<ui:combo-single hiddenName="open" value="${form.param.open}" onSelect="$$.ajax.load($('#${uiid}'), $('#${uiid}').parent())"
		prefixText="${l.l('Открыт')}:" styleClass="mr1" widthTextValue="50px">
		<jsp:attribute name="valuesHtml">
			<li value="">${l.l('Все')}</li>
			<li value="true">${l.l('Yes')}</li>
			<li value="false">${l.l('No')}</li>
		</jsp:attribute>
	</ui:combo-single>
</html:form>

<% out.flush(); %>

<div id="${u:uiid()}">
	<c:url var="url" value="${form.httpRequestURI}">
		<c:param name="action" value="linkedProcessList"/>
		<c:param name="id" value="${form.id}"/>
		<c:param name="open" value="${form.param.open}"/>
	</c:url>
	<c:import url="${url}"/>
</div>

<div id="${u:uiid()}">
	<c:url var="url" value="${form.httpRequestURI}">
		<c:param name="action" value="linkProcessList"/>
		<c:param name="id" value="${form.id}"/>
		<c:param name="open" value="${form.param.open}"/>
	</c:url>
	<%-- otherwise page.pageSize parameter stays the same after the previous import --%>
	<c:remove var="form"/>
	<c:import url="${url}"/>
</div>
