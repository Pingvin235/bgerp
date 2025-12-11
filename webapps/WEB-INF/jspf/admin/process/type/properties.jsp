<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:url var="editUrl" value="/admin/process.do">
	<c:param name="method" value="properties"/>
	<c:param name="id" value="${processType.id}"/>
	<c:param name="returnUrl" value="${form.returnUrl}"/>
</c:url>

<c:set var="formUiid" value="${u:uiid()}"/>

<c:set var="properties" value="${frd.properties}"/>

<c:set var="saveCommand" value="$$.ajax.post($('#${formUiid}'), {control: this}).done(() => $$.ajax.load('${form.requestUrl}', $('#${formUiid}').parent()));"/>

<html:form action="/admin/process" styleId="${formUiid}">
	<input type="hidden" name="method" value="propertiesUpdate"/>
	<input type="hidden" name="returnUrl" value="${form.returnUrl}"/>
	<html:hidden property="id"/>

	<c:set var="lastModifyObject" value="${frd.properties}"/>
	<%@ include file="/WEB-INF/jspf/last_modify_hiddens.jsp"%>

	<ul>
		<li><a href="#${formUiid}-1">${l.l('Properties')}</a></li><%--
	--%><li><a href="#${formUiid}-2">${l.l('Transition matrix')}</a></li><%--
	--%><li><a href="#${formUiid}-3">${l.l('Groups')}</a></li>
	</ul>

	<div id="${formUiid}-1">
		<div class="in-table-cell in-va-top">
			<div style="min-width: 350px; max-width: 350px;">
				<c:set var="applyButtonUiid" value="${u:uiid()}"/>

				<h2>${l.l('Allowed statuses')}</h2>
				<ui:select-mult name="status"
						showId="1" moveOn="true" style="width: 100%;"
						list="${ctxProcessStatusList}" map="${ctxProcessStatusMap}" values="${properties.statusIds}"
						onChange="document.getElementById('${applyButtonUiid}').style.display = 'block'"/>

				<button id="${applyButtonUiid}" type="button" onclick="${saveCommand}" class="btn-grey mt1 w100p"
					style="${not empty properties.statusIds ? 'display: none;' : ''}">${l.l('Apply')}</button>

				<c:if test="${not empty properties.statusIds}">
					<h2>${l.l('Creating status')}</h2>
					<ui:combo-single hiddenName="createStatusId" value="${properties.createStatusId}" list="${u.getObjectList(ctxProcessStatusList, properties.statusIds)}"
						styleClass="w100p" />

					<h2>${l.l('Closing statuses')}</h2>
					<ui:select-mult name="closeStatusId" values="${properties.closeStatusIds}" list="${u.getObjectList(ctxProcessStatusList, properties.statusIds)}"
						styleClass="w100p" />
				</c:if>

				<h2>${l.l('Parameters')}</h2>
				<ui:select-mult name="param"
					showId="1" moveOn="true" showComment="true" style="width: 100%;"
					list="${parameterList}" map="${ctxParameterMap}" values="${properties.parameterIds}"/>
			</div>
			<div class="w100p pl1">
				<h2>${l.l('Configuration')}</h2>
				<c:set var="taUiid" value="${u:uiid()}"/>
				<textarea id="${taUiid}" name="config" style="resize: none; width: 100%;" rows="40">${frd.config}</textarea>
			</div>
		</div>
	</div>
	<div id="${formUiid}-2" style="height: 500px;">
		<h2>${l.l('Allowed status transitions matrix')}</h2>
		<table class="data">
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
							<c:set var="transProperties" value="${processType.properties.getTransactionProperties(itemFrom.id, itemTo.id)}"/>

							<c:set var="checked" value=""/>
							<c:set var="urlConfig" value=""/>

							<c:url var="url" value="/admin/process.do">
								<c:param name="method" value="transactionCheck"/>
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
			<h2>${l.l('statuses.initial')}</h2>
			<div id="roleTabsBegin${formUiid}" class="layout-height-rest">
				<c:set var="groups" value="${properties.groups}"/>
				<c:set var="hiddenName" value="beginGroupRole"/>
				<%@ include file="groups_editor.jsp"%>
			</div>
		</div><%--
	--%><div style="width: 50%; height: 500px;" class="pl1">
			<h2>${l.l('statuses.allowed')}</h2>
			<div id="roleTabsAllowed${formUiid}" class="layout-height-rest">
				<c:set var="groups" value="${properties.allowedGroups}"/>
				<c:set var="hiddenName" value="allowedGroupRole"/>
				<%@ include file="groups_editor.jsp"%>
			</div>
		</div>
	</div>

	<div class="mt1">
		<ui:form-ok-cancel/>
		<span style="float: right;">
			<button type="button" class="btn-grey mr1" onclick="${saveCommand}"
				title="${l.l('Save without leaving editor')}">${l.l('Save')}</button>
			<button type="button" class="btn-grey" onclick="$$.ajax.load('${editUrl}', $('#${formUiid}').parent())">${l.l('Restore')}</button>
		</span>
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

<shell:state text="${l.l('Свойства типа')}: ${processType.title} #${processType.id}" help="kernel/process/index.html#type"/>