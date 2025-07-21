<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="listUiid" value="${form.param.billingId }-${form.param.contractId }-parameters-info"/>

<div id="${listUiid}">
	<c:if test="${empty onlyData}">
		<form action="/user/plugin/bgbilling/proto/contract.do" class="mb1 in-mr1" style="display: inline-block;">
			<input type="hidden" name="method" value="parameterList"/>
			<input type="hidden" name="billingId" value="${form.param.billingId }" />
			<input type="hidden" name="contractId" value="${form.param.contractId }" />

			<ui:combo-single hiddenName="showEmptyParameters" value="${form.param.showEmptyParameters}" prefixText="Только заполненные:" widthTextValue="2em"
				onSelect="$$.ajax.load(this.form, $('#${listUiid}'));">
				<jsp:attribute name="valuesHtml">
					<li value="0">Да</li>
					<li value="1">Нет</li>
				</jsp:attribute>
			</ui:combo-single>

			<ui:combo-single hiddenName="onlyFromGroup" value="${form.param.onlyFromGroup}" prefixText="Только из группы:" widthTextValue="2em"
				onSelect="$$.ajax.load(this.form, $('#${listUiid}'));">
				<jsp:attribute name="valuesHtml">
					<li value="1">Да</li>
					<li value="0">Нет</li>
				</jsp:attribute>
			</ui:combo-single>
		</form><%--
	--%><form action="/user/plugin/bgbilling/proto/contract.do" class="mb1 ml1" style="display: inline-block;">
			<input type="hidden" name="method" value="parameterGroupUpdate"/>
			<input type="hidden" name="billingId" value="${form.param.billingId }" />
			<input type="hidden" name="contractId" value="${form.param.contractId }" />

			<ui:combo-single list="${frd.group.values}" hiddenName="paramGroupId" value="${frd.group.id}" prefixText="Группа параметров (изменить):"
				widthTextValue="12em" onSelect="$$.ajax.post(this.form).done(() => $$.ajax.load('${form.requestUrl}', $('#${listUiid}')))"/>
		</form>
	</c:if>

	<c:if test="${not empty frd.contractParameterList}">
		<table class="hdata hl">
			<c:if test="${empty onlyData}">
				<tr class="header">
					<td width="30">ID</td>
					<td>Параметр</td>
					<td width="100%">Значение</td>
				</tr>
			</c:if>
			<c:forEach var="contractParameter" items="${frd.contractParameterList}">
				<c:if test="${form.param.showEmptyParameters eq 1 or not empty contractParameter.value or contractParameter.paramType eq 10}">
					<tr>
						<c:choose>
	    					<c:when test="${contractParameter.paramType eq 10}">
								<td colspan="3" class="header" >${contractParameter.getTitle()}</td>
							</c:when>
							<c:otherwise>
								<c:if test="${empty onlyData}"><td align="center">${contractParameter.getParamId()}</td></c:if>
								<td nowrap="nowrap">${contractParameter.getTitle()}</td>
								<td>
								<c:set var="viewEditDivId" value="${u:uiid()}"/>
									<div id="${viewEditDivId}">
										<form action="/user/plugin/bgbilling/proto/contract.do">
											<input type="hidden" name="method" value="parameterGet" />
											<input type="hidden" name="billingId" value="${form.param.billingId}"/>
											<input type="hidden" name="contractId" value="${form.param.contractId}"/>
											<input type="hidden" name="paramId" value="${contractParameter.paramId}"/>
											<input type="hidden" name="paramType" value="${contractParameter.paramType}"/>
											<input type="hidden" name="value" value="${u.escapeXml( contractParameter.value )}"/>
											<input type="hidden" name="returnUrl" value="${form.requestUrl}" />

											<a href="#" onclick="$$.ajax.load($(this).parent(), $('#${viewEditDivId}') ); return false;">${u.escapeXml( contractParameter.value )}</a>
											<c:if test="${empty contractParameter.getValue()}">
												<a href="#" onclick="$$.ajax.load($(this).parent(), $('#${viewEditDivId}') ); return false;">не указан</a>
											</c:if>
										</form>
									</div>
								</td>
							</c:otherwise>
						</c:choose>
					</tr>
				</c:if>
			</c:forEach>
		</table>
	</c:if>
</div>