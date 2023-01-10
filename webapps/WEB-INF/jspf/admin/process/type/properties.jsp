<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@page import="ru.bgcrm.model.process.TypeProperties"%>
<%@page import="ru.bgcrm.model.process.ProcessType"%>
<%@page import="ru.bgcrm.model.process.Status"%>
<%@page import="ru.bgcrm.model.process.TransactionProperties"%>

<c:url var="editUrl" value="/admin/process.do">
	<c:param name="action" value="properties"/>
	<c:param name="id" value="${processType.id}"/>
	<c:param name="returnUrl" value="${form.returnUrl}"/>
</c:url>

<c:set var="formUiid" value="${u:uiid()}"/>

<c:set var="properties" value="${form.response.data.properties}"/>

<html:form action="/admin/process" styleId="${formUiid}">
	<input type="hidden" name="action" value="propertiesUpdate"/>
	<input type="hidden" name="returnUrl" value="${form.returnUrl}"/>
	<html:hidden property="id"/>

	<c:set var="lastModifyObject" value="${form.response.data.properties}"/>
	<%@ include file="/WEB-INF/jspf/last_modify_hiddens.jsp"%>

	<ul>
		<li><a href="#${formUiid}-1">Свойства</a></li><%--
	--%><li><a href="#${formUiid}-2">Матрица переходов</a></li><%--
	--%><li><a href="#${formUiid}-3">Группы</a></li>
	</ul>

	<div id="${formUiid}-1">
		<div class="in-table-cell in-va-top">
			<div style="min-width: 350px; max-width: 350px;">
				<h2>Разрешённые статусы</h2>
				<ui:select-mult hiddenName="status"
						showId="true" moveOn="true" style="width: 100%;"
						list="${ctxProcessStatusList}" map="${ctxProcessStatusMap}" values="${properties.statusIds}"/>

				<div class="in-table-cell">
					<div style="width: 40%;">
						<h2>Статус нач. код</h2>
						<html:text property="create_status" style="width: 100%;" value="${properties.createStatus}"/>
					</div>
					<div style="width: 60%;" class="pl1">
						<h2>Статусы кон. через ,</h2>
						<html:text property="close_status" style="width: 100%;" value="${u:toString(properties.closeStatusIds)}"/>
					</div>
				</div>

				<h2>Параметры</h2>
				<ui:select-mult hiddenName="param"
					showId="true" moveOn="true" showComment="true" style="width: 100%;"
					list="${parameterList}" map="${ctxParameterMap}" values="${properties.parameterIds}"/>
			</div>
			<div class="w100p pl1">
				<h2>Конфигурация</h2>
				<c:set var="taUiid" value="${u:uiid()}"/>
				<textarea id="${taUiid}" name="config" style="resize: none; width: 100%;" rows="40">${form.response.data.config}</textarea>
			</div>
		</div>
	</div>
	<div id="${formUiid}-2" style="height: 500px;">
		<h2>Матрица разрешенных переходов статусов</h2>
		<table style="width: 100%;" class="data">
			<tr>
				<td>
					<input type="checkbox"
						title="${l.l('Выделить или снять выделение всех')}"
						onchange="$('#${formUiid}-2 input[name=checker]').prop('checked', this.checked).trigger('change');"/>
					С: &#8595; На: &#8594;
				</td>
				<c:forEach var="itemTo" items="${statusList}" varStatus="statusTo">
					<td>
						${itemTo.title} (${itemTo.id})
					</td>
				</c:forEach>
			</tr>
			<c:forEach var="itemFrom" items="${statusList}" varStatus="statusFrom">
				<tr>
					<td width="200">${itemFrom.title} (${itemFrom.id})</td>

					<c:forEach var="itemTo" items="${statusList}" varStatus="statusTo">
						<c:set var="cl" value="odd"/>
						<c:if test="${(statusFrom.count + statusTo.count) mod 2 == 1}">
							<c:set var="cl" value="even"/>
						</c:if>
						<td align="left">
							<%
								Status statusFrom = (Status)pageContext.getAttribute( "itemFrom" );
								Status statusTo = (Status)pageContext.getAttribute( "itemTo" );

								ProcessType type = (ProcessType)request.getAttribute( "processType" );
								if( type != null )
								{
									TypeProperties typeProperties = type.getProperties();
									TransactionProperties transProperties = typeProperties.getTransactionProperties( statusFrom.getId(), statusTo.getId() );
									pageContext.setAttribute( "transProperties", transProperties );
								}
							%>

							<c:set var="checked" value=""/>
							<c:set var="urlConfig" value=""/>

							<c:url var="url" value="/admin/process.do">
								<c:param name="action" value="transactionCheck"/>
								<c:param name="id" value="${processType.id}"/>
								<c:param name="fromStatus" value="${itemFrom.id}"/>
								<c:param name="toStatus" value="${itemTo.id}"/>

								<c:choose>
									<c:when test="${transProperties.enable}">
										<c:set var="checked" value="checked='checked'"/>
										<c:param name="enable" value="false"/>
									</c:when>
									<c:otherwise>
											<c:param name="enable" value="true"/>
									</c:otherwise>
								</c:choose>
							</c:url>

							<c:if test="${itemFrom.id ne itemTo.id}">
								<c:choose>
									<c:when test="${empty checked}">
										<c:set var="matrixValue" value="${itemFrom.id}-${itemTo.id}-false" />
									</c:when>
									<c:otherwise>
										<c:set var="matrixValue" value="${itemFrom.id}-${itemTo.id}-true" />
									</c:otherwise>
								</c:choose>
								<input type="hidden" id="${itemFrom.id}-${itemTo.id}" name="matrix" value="${matrixValue}" />
								<input name="checker" onchange="$('#${itemFrom.id}-${itemTo.id}').val('${itemFrom.id}-${itemTo.id}-' + $(this).prop('checked'))" type="checkbox" ${checked}/>
								<br/>
								${transProperties.reference}
							</c:if>
						</td>
					</c:forEach>
				</tr>
			</c:forEach>
		</table>
	</div>
	<div id="${formUiid}-3" style="height: 500px;" class="in-inline-block">
		<div style="width: 50%; height: 500px;">
			<h2>Начальные</h2>
			<div id="roleTabsBegin${formUiid}" class="layout-height-rest">
				<c:set var="groups" value="${properties.groups}"/>
				<c:set var="hiddenName" value="beginGroupRole"/>
				<%@ include file="groups_editor.jsp"%>
			</div>
		</div><%--
	--%><div style="width: 50%; height: 500px;" class="pl1">
			<h2>Разрешённые</h2>
			<div id="roleTabsAllowed${formUiid}" class="layout-height-rest">
				<c:set var="groups" value="${properties.allowedGroups}"/>
				<c:set var="hiddenName" value="allowedGroupRole"/>
				<%@ include file="groups_editor.jsp"%>
			</div>
		</div>
	</div>
</html:form>

<script>
	$('#${formUiid}').tabs();
	$("div#roleTabsBegin${formUiid}").tabs();
	$("div#roleTabsAllowed${formUiid}").tabs();

	$(function () {
		$$.ui.codeMirror('${taUiid}');
	})
</script>

<div class="mt1">
	<button type="button" class="btn-grey mr1" onclick="$$.ajax.post($('#${formUiid}')[0]).done(() => $$.ajax.load('${editUrl}', $('#${formUiid}').parent()));">OK</button>
	<button type="button" class="btn-grey mr1" onclick="$$.ajax.load('${editUrl}', $('#${formUiid}').parent())">${l.l('Восстановить')}</button>

	<button type="button" class="btn-white ml1" onclick="$$.ajax.load('${form.returnUrl}', $('#${formUiid}').parent())">${l.l('К списку типов')}</button>
</div>

<shell:state text="${l.l('Свойства типа')}: ${processType.title} #${processType.id}" help="kernel/process/index.html#type"/>