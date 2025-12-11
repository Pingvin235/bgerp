<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>
<%@ taglib tagdir="/WEB-INF/tags/plugin/report" prefix="report"%>

<shell:title text="${l.l('Report')}"/>
<shell:state text="${l.l('Процессы')}"/>

<div class="report">
	<html:form action="${form.requestURI}">
		<ui:combo-single hiddenName="mode" widthTextValue="5em" value="${form.param.mode}">
			<jsp:attribute name="valuesHtml">
				<li value="create">${l.l('Созданные')}</li>
				<li value="close">${l.l('Closed')}</li>
			</jsp:attribute>
		</ui:combo-single>
		<span> </span>${l.l("с")}<span> </span>
		<ui:date-time name="dateFrom" value="${form.param.dateFrom}"/>
		${l.l("по")}<span> </span>
		<ui:date-time name="dateTo" value="${form.param.dateTo}"/>

		<ui:combo-check name="type" values="${form.getParamValues('type')}"
			list="${frd.types}" map="${ctxProcessTypeMap}"
			prefixText="${l.l('Type')}:" showFilter="1" widthTextValue="10em"/>

		<ui:button type="out" styleClass="ml1 mr1 more out" onclick="$$.ajax.loadContent(this)"/>

		<report:more data="${data}"/>

		<ui:page-control nextCommand="; $$.ajax.loadContent(this)" styleClass="more"/>
	</html:form>

	<%-- TODO: move to tag together with table --%>
	<div class="data mt1 w100p" style="overflow: auto;">
		<table class="data hl">
			<report:headers data="${data}"/>
			<c:forEach var="r" items="${frd.list}">
				<tr>
					<td><ui:process-link id="${r.get('id')}"/></td>
					<td>${r.get('type_title')}</td>
					<%-- u:int call is not needed here, just a sample of type convertion --%>
					<td><ui:user-link id="${u:int(r.get('user_id'))}"/></td>
					<td>${r.getString('time')}</td>
					<td>${r.get('process_description')}</td>
					<td>${r.get('executors')}</td>
				</tr>
			</c:forEach>
		</table>
	</div>
</div>