<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="/user/process" styleClass="mb05" styleId="${uiid}">
	<input type="hidden" name="action" value="userProcessList"/>

	<div class="tableIndent in-mb05-all">
		<ui:date-time
			paramName="createDate" value="${form.param.createDate}"
			placeholder="${l.l('Дата создания')}"
			styleClass="mr1"/>

		<ui:date-time
			paramName="closeDate" value="${form.param.closeDate}"
			placeholder="${l.l('Дата закрытия')}"
			styleClass="mr1"/>

		<ui:combo-single
			hiddenName="open" value="${form.param.open}" prefixText="${l.l('process.closed')}:"
			styleClass="mr1" widthTextValue="100px"
			onSelect="$$.ajax.load(this.form, $(this.form).parent());">
			<jsp:attribute name="valuesHtml">
				<li value="1">${l.l('Open')}</li>
				<li value="0">${l.l('Closed')}</li>
				<li value="">${l.l('Any')}</li>
			</jsp:attribute>
		</ui:combo-single>

		<ui:page-control nextCommand="; $$.ajax.load(this.form, $(this.form).parent());"/>
	</div>
</html:form>

<table class="data hl">
	<tr>
		<td>ID</td>
		<td>${l.l('Creation time')}</td>
		<td class="min">${l.l('Время закрытия')}</td>
		<td>${l.l('Type')}</td>
		<td>${l.l('Status')}</td>
		<td>${l.l('Description')}</td>
	</tr>
	<c:forEach var="process" items="${form.response.data.list}">
		<tr openCommand="$$.process.open(${process.id })">
			<td class="min"><a href="#" onclick="$$.process.open(${process.id}); return false;">${process.id}</a></td>
			<td class="min">${tu.format(process.createTime, 'ymdhms')}</td>
			<td class="min">${tu.format(process.closeTime, 'ymdhms')}</td>
			<td>${ctxProcessTypeMap[process.typeId].title}</td>
			<td>${ctxProcessStatusMap[process.statusId].title}</td>
			<td>
				<%@ include file="/WEB-INF/jspf/user/process/reference.jsp"%>
			</td>
		</tr>
	</c:forEach>
</table>

<shell:title text="${l.l('My Processes')}"/>
<shell:state/>
