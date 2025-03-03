<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="/user/process" styleClass="mb05" styleId="${uiid}">
	<input type="hidden" name="method" value="userProcessList"/>

	<div class="tableIndent in-mb05-all">
		<ui:date-time
			paramName="createDate" value="${form.param.createDate}"
			placeholder="${l.l('Дата создания')}"
			styleClass="mr1"/>

		<ui:date-time
			paramName="closeDate" value="${form.param.closeDate}"
			placeholder="${l.l('Дата закрытия')}"
			styleClass="mr1"/>

		<ui:combo-single hiddenName="open" value="${form.param.open}" onSelect="$$.ajax.load(this.form, $(this.form).parent())"
			prefixText="${l.l('Open')}:" styleClass="mr05" widthTextValue="5em">
			<jsp:attribute name="valuesHtml">
				<li value="">${l.l('All')}</li>
				<li value="true">${l.l('Yes')}</li>
				<li value="false">${l.l('No')}</li>
			</jsp:attribute>
		</ui:combo-single>

		<ui:page-control nextCommand="; $$.ajax.load(this.form, $(this.form).parent());"/>
	</div>
</html:form>

<table class="data hl">
	<tr>
		<td>${l.l('Process')}</td>
		<td>${l.l('Type')}</td>
		<td class="min">${l.l('Created')}</td>
		<td>${l.l('Status')}</td>
		<td class="min">${l.l('Closed')}</td>
	</tr>
	<c:forEach var="process" items="${frd.list}">
		<tr>
			<td><ui:process-link process="${process}"/></td>
			<td>${process.type.title}</td>
			<td class="nowrap">${tu.format(process.createTime, 'ymdhms')}</td>
			<td>${process.statusTitle}</td>
			<td class="nowrap">${tu.format(process.closeTime, 'ymdhms')}</td>
		</tr>
	</c:forEach>
</table>

<shell:title text="${l.l('My Processes')}"/>
<shell:state/>
