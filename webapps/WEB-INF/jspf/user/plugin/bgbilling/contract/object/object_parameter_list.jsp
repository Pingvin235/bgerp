<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div id="${form.param.billingId }-${form.param.contractId }-objectParametersInfo">
	<form action="/user/plugin/bgbilling/proto/contract.do">
			<input type="hidden" name="action" value="contractObjectParameterList"/>
			<input type="hidden" name="billingId" value="${form.param.billingId }" />
			<input type="hidden" name="contractId" value="${form.param.contractId }" />
			<input type="hidden" name="objectId" value="${form.param.objectId }" />
		    <input type="checkbox" name="showEmptyParameters" value="true" <c:if test="${form.param.showEmptyParameters }">checked</c:if>
				    onclick="$$.ajax.load($(this).parent(), $('#${form.param.billingId }-${form.param.contractId }-objectParametersInfo') );"/>
				    Показать незаполненные параметры</br>
	</form>

	<table class="data">
		<tr class="header">
			<td width="5%">ID</td>
			<td width="35%">Параметр</td>
			<td width="60%">Значение</td>
		</tr>

		<c:forEach var="parameter" items="${form.response.data.parameterList}">
			<c:if test="${ form.param.showEmptyParameters and empty parameter.getValue() or not empty parameter.getValue() }">
				<tr>
					<td align="center">${parameter.getParamId()}</td>
					<td nowrap="nowrap">${parameter.getTitle() }</td>
					<td>
					<c:set var="viewEditDivId" value="${u:uiid()}"/>
						<div id="${viewEditDivId}">
							<form action="/user/plugin/bgbilling/proto/contract.do">
								<input type="hidden" name="action" value="getObjectParameter" />
								<input type="hidden" name="billingId" value="${ form.param.billingId }" />
								<input type="hidden" name="contractId" value="${ form.param.contractId }" />
								<input type="hidden" name="objectId" value="${ form.param.objectId }" />
								<input type="hidden" name="paramId" value="${parameter.getParamId()}" />
								<input type="hidden" name="returnUrl" value="${form.requestUrl}" />

								<a href="#" onclick="$$.ajax.load($(this).parent(), $('#${viewEditDivId}') )">${parameter.getValue() }</a>
								<c:if test="${empty parameter.getValue()}">
									<a href="#" onclick="$$.ajax.load($(this).parent(), $('#${viewEditDivId}') )">не указан</a>
								</c:if>
							</form>
						</div>
					</td>
				</tr>
			</c:if>
		</c:forEach>
	</table>
</div>