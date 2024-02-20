<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="parametersInfo" value= "${form.param.billingId }-${form.param.contractId }-objectParametersInfo"/>

<c:choose>
	<c:when test="${ not empty frd.parameter}">
		<form action="/user/plugin/bgbilling/proto/contract.do">

			<c:set var="data" value="${frd}"/>
			<c:set var="parameter" value="${data.parameter}"/>

			<input type="hidden" name="action" value="updateObjectParameter"/>
			<input type="hidden" name="billingId" value="${form.param.billingId }" />
			<input type="hidden" name="contractId" value="${form.param.contractId }" />
			<input type="hidden" name="objectId" value="${form.param.objectId }" />
			<input type="hidden" name="paramId" value="${parameter.getParamId() }" />
			<input type="hidden" name="paramType" value="${parameter.getParamType() }" />

			<c:choose>
				<c:when test="${parameter.getParamType() eq 1}"> <!-- text -->
					<input type="text" style="width:100%;" name="textValue" value="${parameter.getValue()}"/>
				</c:when>

				<c:when test="${parameter.getParamType() eq 4}"> <!-- address -->
					<script>
							$(function() {
								addAddressSearch( "#${parametersInfo}" );
							})
						</script>

						<c:set var="address" value="${data.address}"/>
						<c:set var="house" value="${data.house}"/>

						<c:set var="streetTitle" value=""/>
						<c:set var="houseTitle" value=""/>

						<c:if test="${not empty house}">
							<c:set var="streetTitle" value="${house.addressStreet.addressCity.title} - ${house.addressStreet.title}"/>
							<c:set var="houseTitle" value="${house.houseAndFrac}"/>
						</c:if>

						<c:set var="pod" value=""/>
						<c:if test="${address.pod gt 0}">
							<c:set var="pod" value="${address.pod}"/>
						</c:if>

						<c:set var="floor" value=""/>
						<c:if test="${address.floor gt 0}">
							<c:set var="floor" value="${address.floor}"/>
						</c:if>

						<input type="hidden" name="streetId" value="${house.addressStreet.id}"/>
						<input type="hidden" name="houseId" value="${house.id}"/>

						<table style="width: 100%;">
							<tr>
								<td>Улица: </td>
								<td width="70%">
									<html:text property="street" value="${streetTitle}" style="width: 100%"/>
								</td>
								<td nowrap="nowrap">Дом:</td>
								<td width="30%">
									<html:text property="house" value="${houseTitle}" style="width: 100%"/>
								</td>
							</tr>
							<tr>
								<td nowrap="nowrap">Кв./оф.</td>
								<td><html:text property="flat" value="${address.flat}" style="width: 100%"/></td>
								<td>Комн.</td>
								<td><html:text property="room" value="${address.room}" style="width: 100%"/></td>
							</tr>
							<tr>
								<td>Подъ.:</td>
								<td><html:text property="pod" value="${pod}" style="width: 100%"/></td>
								<td>Этаж:</td>
								<td><html:text property="floor" value="${floor}" style="width: 100%"/></td>
							</tr>
							<tr>
								<td>Ком.:</td>
								<td colspan="3"><html:text property="comment" value="${address.comment}" style="width: 100%"/></td>
							</tr>
						</table>
				</c:when>

				<c:when test="${parameter.getParamType() eq 3}"> <!-- date -->
					<ui:date-time paramName="dateValue" value="${parameter.getValue()}"/>
				</c:when>

				<c:when test="${parameter.getParamType() eq 2}"> <!-- list -->
					<select name="listValueId" style="width: 100%" multiline size="10" >
						<c:forEach var="value" items="${data.valueList}">
							 <option value="${ value.getId() }">${ value.getTitle() }</option>
						</c:forEach>
					</select>
				</c:when>

				<c:otherwise>
					параметр не редактируется
				</c:otherwise>
			</c:choose>

			</br>
			<input type="button" value="OK" onclick="$$.ajax.post(this.form).done(() => $$.ajax.load('${form.returnUrl}', $('#${parametersInfo}').parent()))"/>
			<input type="button" value="Отмена" onclick="$$.ajax.load('${form.returnUrl}', $('#${parametersInfo}').parent())"/>
		</form>
	</c:when>
	<c:otherwise>
		<input type="button" value="Отмена" onclick="$$.ajax.load('${form.returnUrl}', $('#${parametersInfo}').parent())"/>
	</c:otherwise>
</c:choose>