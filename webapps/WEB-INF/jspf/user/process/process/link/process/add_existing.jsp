<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h1>${l.l('Link existing process')}</h1>

<c:set var="listBuffer" value="${frd.listBuffer}"/>
<c:set var="list" value="${frd.list}"/>

<html:form action="${form.requestURI}" styleClass="mb1">
	<html:hidden property="method"/>
	<html:hidden property="id"/>
	<html:hidden property="categoryId"/>
	<html:hidden property="returnUrl"/>

	${l.l('Shown')}&nbsp;<b>${listBuffer.size() + list.size()}</b>&nbsp;${l.l('records.shown.of')}&nbsp;<b>${form.page.recordCount}</b>
	<ui:input-text name="filter" value="${form.param.filter}" onSelect="$$.ajax.load(this.form, $(this.form).parent())" placeholder="${l.l('ID or Description part')}" size="20" styleClass="ml05"/>
</html:form>

<html:form action="${form.requestURI}">
	<input type="hidden" name="method" value="linkProcessExisting"/>
	<html:hidden property="id"/>
	<html:hidden property="categoryId"/>

	<table class="data hl mb1">
		<tr>
			<td class="min">&nbsp;</td>
			<td class="min">ID</td>
			<td>${l.l('Type')}</td>
			<td>${l.l('Status')}</td>
			<td>${l.l('Description')}</td>
		</tr>
		<c:forEach var="process" items="${listBuffer}" varStatus="status">
			<u:sc>
				<c:set var="tdClass"><c:if test="${status.last}">class="group-border-b"</c:if></c:set>
				<%@ include file="add_existing_table_row.jsp"%>
			</u:sc>
		</c:forEach>
		<c:forEach var="process" items="${list}">
			<%@ include file="add_existing_table_row.jsp"%>
		</c:forEach>
	</table>

	<ui:form-ok-cancel/>
</html:form>