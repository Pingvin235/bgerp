<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="linkedProcessList" value="linkedProcessList-${u:uiid()}" />
<c:set var="processTypeTree" value="processTypeTree-${u:uiid()}" scope="request" />

<div id="${processTypeTree}" class="tableIndent center1020 editorStopReload" style="display: none;">
	<form action="/user/process/link.do">
		<input type="hidden" name="action" value="linkedProcessCreate" />
		<input type="hidden" name="id" value="${form.id}" />
		<input type="hidden" name="objectType" value="${form.param.objectType}" />
		<input type="hidden" name="objectTitle" value="${fn:escapeXml( form.param.objectTitle )}" />
		<input type="hidden" name="billingId" value="${fn:split(form.param.objectType,':')[1]}" />

		<div id="typeTree">
			<jsp:include page="/WEB-INF/jspf/user/process/tree/process_type_tree.jsp" />
		</div>
		<div id="groupSelect">
			<%-- сюда динамически грузятся группы решения --%>
		</div>
		<div id="additionalParamsSelect">
			<%-- сюда динамически грузятся доп параметры для данного типа процесса --%>
		</div>
		<div id="constPart">
			<b>${l.l('Description')}:</b><br />
			<textarea name="description" rows="10" style="width: 100%;"></textarea>

			<c:set var="returnToShow">$('#${processTypeTree}').hide(); $('#${linkedProcessList}').show();</c:set>
			<c:set var="saveCommand">
				$$.ajax.post(this.form).done((result) => {
					if (result.data.wizard) {
						const url = '/user/process.do?wizard=1&id=' + result.data.process.id + '&returnUrl=${u:urlEncode(form.requestUrl)}';
						$$.ajax.load(url, $('#${linkedProcessList}').parent());
					} else {
						${returnToShow} $$.ajax.load('${form.requestUrl}', $('#${processTypeTree}').parent());
					}
				});
			</c:set>

			<div class="mt1">
				<button type="button" class="btn-grey mr1" onclick="${saveCommand}">OK</button>
				<button type="button" class="btn-grey" onclick="${returnToShow}">${l.l('Отмена')}</button>
			</div>
		</div>
	</form>
</div>
<c:remove var="saveCommand" />

<div id="${linkedProcessList}">
	<html:form action="/user/process/link" styleClass="mb05">
		<input type="hidden" name="action" value="linkedProcessList" />
		<input type="hidden" name="objectType" value="${form.param.objectType}" />
		<input type="hidden" name="id" value="${form.id}" />
		<input type="hidden" name="objectTitle" value="${fn:escapeXml( form.param.objectTitle )}" />

		<div class="tableIndent in-mb05-all">
			<button class="btn-green mr1 icon" type="button"
					onclick="$('#${processTypeTree}').show(); $('#${linkedProcessList}').hide();"><i class="ti-plus"></i></button>

			<c:set var="reloadCommand" value="$$.ajax.load($('#${linkedProcessList} > form'), $('#${linkedProcessList}').parent())"/>

			<ui:combo-single hiddenName="typeId" value="${form.param.typeId}" onSelect="${reloadCommand}"
				prefixText="${l.l('Type')}:" showFilter="true" styleClass="mr1" widthTextValue="200px">
				<jsp:attribute name="valuesHtml">
					<li value="">${l.l('Любой')}</li>
					<c:forEach var="type" items="${form.response.data.typeList}">
						<li value="${type}">${ctxProcessTypeMap[type]}</li>
					</c:forEach>
				</jsp:attribute>
			</ui:combo-single>

			<ui:combo-single hiddenName="open" value="${form.param.open}" onSelect="${reloadCommand}"
				prefixText="${l.l('Открыт')}:" styleClass="mr1" widthTextValue="50px">
				<jsp:attribute name="valuesHtml">
					<li value="">${l.l('Все')}</li>
					<li value="1">${l.l('Yes')}</li>
					<li value="0">${l.l('No')}</li>
				</jsp:attribute>
			</ui:combo-single>

			<c:set var="nextCommand" value="; ${reloadCommand}"/>
			<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
		</div>
	</html:form>

	<c:choose>
		<c:when test="${not empty queue}">
			<%@ include file="/WEB-INF/jspf/user/process/queue/show_table.jsp"%>
		</c:when>
		<c:otherwise>
			<c:set var="customerLinkRoleConfig" value="${ctxSetup.getConfig('ru.bgcrm.model.customer.config.ProcessLinkModesConfig')}"/>

			<table class="data">
				<tr>
					<td>ID</td>
					<td>${l.l('Создан')}</td>
					<td>${l.l('Закрыт')}</td>
					<td>${l.l('Роль')}</td>
					<td>${l.l('Type')}</td>
					<td>${l.l('Status')}</td>
					<td>${l.l('Description')}</td>
				</tr>
				<c:forEach var="item" items="${form.response.data.list}">
					<c:set var="process" value="${item.second}" />
					<c:if test="${ fn:trim(form.param.createDate) eq tu.format( process.createTime, 'ymd' ) or empty form.param.createDate or
									(fn:trim(form.param.closeDate) eq tu.format( process.closeTime, 'ymd' ) and not empty form.param.closeDate) }">
						<tr id="${linkedProcessList}-linkedObject-${process.id}">
							<td nowrap="nowrap"><a href="#" onclick="openProcess(${process.id}); return false;">${process.id}</a></td>
							<td>${tu.format( process.createTime, 'ymdhms' )}</td>
							<td>${tu.format( process.closeTime, 'ymdhms' )}</td>
							<td nowrap="nowrap">
								<c:set var="linkedObjectType" value="${item.first}" scope="request" />
								<c:choose>
									<c:when test="${fn:startsWith( linkedObjectType, 'customer' ) }">
										${customerLinkRoleConfig.modeMap[linkedObjectType]}
									</c:when>
									<c:otherwise>
										<plugin:include endpoint="user.process.linked.list.jsp"/>
									</c:otherwise>
								</c:choose>
							</td>
							<td>${ctxProcessTypeMap[process.typeId]}</td>
							<td>${ctxProcessStatusMap[process.statusId]}</td>
							<td width="100%">
								<%@ include file="/WEB-INF/jspf/user/process/reference.jsp"%>
							</td>
						</tr>
					</c:if>
				</c:forEach>
			</table>
		</c:otherwise>
	</c:choose>
</div>