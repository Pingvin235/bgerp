<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="listUiid" value="${form.param.billingId }-${form.param.contractId }-parameters-info"/>

<div id="${listUiid}">
	<c:if test="${empty onlyData}">
		<form action="/user/plugin/bgbilling/proto/contract.do" class="mb1 in-mr1" style="display: inline-block;">
			<input type="hidden" name="action" value="parameterList"/>
			<input type="hidden" name="billingId" value="${form.param.billingId }" />
			<input type="hidden" name="contractId" value="${form.param.contractId }" />

			<u:sc>
				<c:set var="valuesHtml">
					<li value="0">Да</li>
					<li value="1">Нет</li>
				</c:set>
				<c:set var="hiddenName" value="showEmptyParameters"/>
				<c:set var="value" value="${form.param.showEmptyParameters}"/>
				<c:set var="prefixText" value="Только заполненные:"/>
				<c:set var="widthTextValue" value="20px"/>
				<c:set var="onSelect" value="$$.ajax.load(this.form, $('#${listUiid}'));"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
			</u:sc>

			<u:sc>
				<c:set var="valuesHtml">
					<li value="1">Да</li>
					<li value="0">Нет</li>
				</c:set>
				<c:set var="hiddenName" value="onlyFromGroup"/>
				<c:set var="value" value="${form.param.onlyFromGroup}"/>
				<c:set var="prefixText" value="Только из группы:"/>
				<c:set var="widthTextValue" value="20px"/>
				<c:set var="onSelect" value="$$.ajax.load(this.form, $('#${listUiid}'));"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
			</u:sc>
		</form>
		<form action="/user/plugin/bgbilling/proto/contract.do" class="mb1 in-mr1" style="display: inline-block;">
			<input type="hidden" name="action" value="parameterGroupUpdate"/>
			<input type="hidden" name="billingId" value="${form.param.billingId }" />
			<input type="hidden" name="contractId" value="${form.param.contractId }" />

			<u:sc>
				<c:set var="list" value="${form.response.data.group.values}"/>
				<c:set var="hiddenName" value="paramGroupId"/>
				<c:set var="value" value="${form.response.data.group.id}"/>
				<c:set var="prefixText" value="Группа параметров (изменить):"/>
				<c:set var="widthTextValue" value="150px"/>
				<c:set var="onSelect" value="if( sendAJAXCommand( formUrl( $hidden.closest('form') ) ) ){ $$.ajax.load('${form.requestUrl}', $('#${listUiid}')); }"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
			</u:sc>
		</form>
	</c:if>

	<c:if test="${not empty form.response.data.contractParameterList}">
		<table class="hdata" width="100%">
			<c:if test="${empty onlyData}">
				<tr class="header">
					<td width="30">ID</td>
					<td>Параметр</td>
					<td width="100%">Значение</td>
				</tr>
			</c:if>
			<c:forEach var="contractParameter" items="${form.response.data.contractParameterList}">
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
											<input type="hidden" name="action" value="parameterGet" />
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