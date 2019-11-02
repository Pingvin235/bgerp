<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="linkedProcessList" value="linkedProcessList-${u:uiid()}" />
<c:set var="processTypeTree" value="processTypeTree-${u:uiid()}" scope="request" />

<div id="${processTypeTree}" class="tableIndent center1020 editorStopReload" style="display: none;">
	<form action="process.do">
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
			<b>Описание:</b><br />
			<textarea name="description" rows="10" style="width: 100%;"></textarea>

			<c:set var="returnToShow">$('#${processTypeTree}').hide(); $('#${linkedProcessList}').show();</c:set>
			<c:set var="saveCommand">
				var result = sendAJAXCommand( formUrl( this.form ) ); 
				if( result )
				{ 
					if( result.data.wizard )
					{
						var url = 'process.do?wizard=1&id=' + result.data.process.id + '&returnUrl=${u:urlEncode( form.requestUrl )}';
						openUrlToParent( url, $('#${linkedProcessList}') );
					}
					else
					{
						${returnToShow} openUrlToParent( '${form.requestUrl}', $('#${processTypeTree}') );
					} 
				};
			</c:set>

			<div class="mt1">
				<button type="button" class="btn-grey mr1" onclick="${saveCommand}">ОК</button>
				<button type="button" class="btn-grey" onclick="${returnToShow}">Отмена</button>
			</div>
		</div>
	</form>
</div>
<c:remove var="saveCommand" />

<div id="${linkedProcessList}">
	<html:form action="user/process" styleClass="mb05">
		<input type="hidden" name="action" value="linkedProcessList" />
		<input type="hidden" name="objectType" value="${form.param.objectType}" />
		<input type="hidden" name="id" value="${form.id}" />
		<input type="hidden" name="objectTitle" value="${fn:escapeXml( form.param.objectTitle )}" />

		<div class="tableIndent in-mb05-all">
			<button class="btn-green mr1" type="button" 
					onclick="$('#${processTypeTree}').show(); $('#${linkedProcessList}').hide();">+</button>

			<c:set var="saveCommand" value="openUrlTo( formUrl( $('#${linkedProcessList} > form') ), $('#${linkedProcessList}') )" />

			<input type="text" name="createDate" class="mr1" value="${form.param.createDate}" onchange="" title="Создан" placeholder="Создан"/>
	 		<c:set var="selector" value="#${linkedProcessList} input[name='createDate']" />
	 		<c:set var="editable" value="1" />
			<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
			
			<input type="text" name="closeDate" value="${form.param.closeDate}" onchange="" class="mr1" title="Закрыт" placeholder="Закрыт"/>
			<c:set var="selector" value="#${linkedProcessList} input[name='closeDate']" />
			<c:set var="editable" value="1" />
			<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>

			<u:sc>
				<c:set var="valuesHtml" >
					<li value="">Любой</li>
					<c:forEach var="type" items="${form.response.data.typeList}">
						<li value="${type}">${ctxProcessTypeMap[type]}</li>
					</c:forEach>
				</c:set>
			 	<c:set var="styleClass" value="mr1"/> 
				<c:set var="hiddenName" value="typeId" />
				<c:set var="value" value="${form.param.typeId}" />
				<c:set var="widthTextValue" value="200px" />
				<c:set var="prefixText" value="Тип:" />
				<c:set var="showFilter" value="1"/>
				<c:set var="onSelect" value="openUrlTo( formUrl( $('#${linkedProcessList} > form') ), $('#${linkedProcessList}') )" />
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
			</u:sc>
	
			<u:sc>
				<c:set var="valuesHtml">
					<li value="">Все</li>
					<li value="1">Открытые</li>
					<li value="0">Закрытые</li>
				</c:set>
				<c:set var="styleClass" value="mr1"/>	
				<c:set var="hiddenName" value="closed" />
				<c:set var="value" value="${form.param.closed}" />
				<c:set var="widthTextValue" value="100px" />
				<c:set var="prefixText" value="Закрыт:" />
				<c:set var="onSelect"
					value="openUrlTo( formUrl( $('#${linkedProcessList} > form') ), $('#${linkedProcessList}') )" />
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
			</u:sc>

			<c:set var="nextCommand" value="; openUrlToParent( formUrl( this.form ), $('#${linkedProcessList}') )" />
			<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
		</div>
	</html:form>

	<c:set var="customerLinkRoleConfig"
		value="${u:getConfig( setup, 'ru.bgcrm.model.customer.config.ProcessLinkModesConfig' )}" />

	<table style="width: 100%;">
		<tr>
			<td valign="top" style="width: 100%; padding: 0px;">
				<table class="data" style="width: 100%;">
					<tr>
						<td>ID</td>
						<td>Создан</td>
						<td>Закрыт</td>
						<td>Роль</td>
						<td>Тип</td>
						<td>Статус</td>
						<td>Описание</td>
						<%--
						<p:check action="ru.bgcrm.struts.action.ProcessAction:linkedProcessInfo">
							<td></td>
						</p:check>
						<c:if test="${not empty wizardEnable}">
							<td></td>
						</c:if>
						 --%>
					</tr>
					<c:forEach var="item" items="${form.response.data.list}">
						<c:set var="process" value="${item.second}" />
						<c:if test="${ fn:trim(form.param.createDate) eq u:formatDate( process.createTime, 'ymd' ) or empty form.param.createDate or
									 (fn:trim(form.param.closeDate) eq u:formatDate( process.closeTime, 'ymd' ) and not empty form.param.closeDate) }">
							<tr id="${linkedProcessList}-linkedObject-${process.id}">
								<td nowrap="nowrap"><a href="#UNDEF" onclick="openProcess(${process.id}); return false;">${process.id}</a></td>
								<td>${u:formatDate( process.createTime, 'ymdhms' )}</td>
								<td>${u:formatDate( process.closeTime, 'ymdhms' )}</td>
								<td nowrap="nowrap">
									<c:set var="linkedObjectType" value="${item.first}" scope="request" /> 
									<c:choose>
										<c:when test="${fn:startsWith( linkedObjectType, 'customer' ) }">
											${customerLinkRoleConfig.modeMap[linkedObjectType]}
										</c:when>
										<c:otherwise>
											<c:set var="endpoint" value="user.process.linked.list.jsp" />
											<%@ include file="/WEB-INF/jspf/plugin_include.jsp"%>
										</c:otherwise>
									</c:choose>
								</td>
								<td>${ctxProcessTypeMap[process.typeId]}</td>
								<td>${ctxProcessStatusMap[process.statusId]}</td>
								<td width="100%">
									<c:choose>
										<c:when test="${not empty process.reference}">
											${process.reference}<br />
										</c:when>
										<c:otherwise>	
											${process.description}
										</c:otherwise>
									</c:choose>
								</td>
								<%--
								<p:check action="ru.bgcrm.struts.action.ProcessAction:linkedProcessInfo">
									<td>
										<c:url var="infoUrl" value="process.do">
											<c:param name="action" value="linkedProcessInfo" />
											<c:param name="id" value="${process.id}" />
											<c:param name="returnUrl" value="${form.requestUrl}" />
										</c:url> 
										<input type="button" value="доп.инфо" onclick="openUrlTo('${infoUrl}',$('#${linkedProcessList}-linkedObject-${process.id}'))" />
									</td>
								</p:check>
								<c:if test="${not empty wizardEnable}">
									<td>
										<c:choose>
											<c:when test="${not empty wizardEnable[process.id]}">
												<c:set var="returnScript" scope="request">
													openUrlToParent('${form.requestUrl}',$('#${linkedProcessList}'))
												</c:set>

												<c:url var="url" value="process.do">
													<c:param name="id" value="${process.id}" />
													<c:param name="wizard" value="1" />
													<c:param name="returnUrl" value="${form.requestUrl}" />
												</c:url>

												<input type="button" value="мастер" onclick="openUrlToParent('${url}',$('#${linkedProcessList}'))" />
											</c:when>
											<c:otherwise>&nbsp;</c:otherwise>
										</c:choose>
									</td>
								</c:if>
								 --%>
							</tr>
						</c:if>
					</c:forEach>
				</table>
			</td>
		</tr>
	</table>
</div>