<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="parametersInfo" value= "${form.param.billingId }-${form.param.contractId }-parameters-info"/>
<c:set var="dbInfo" value="${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[form.param.billingId]}"/>

<form action="/user/plugin/bgbilling/proto/contract.do" onsubmit="return false;" class="editorStopReload">
	<c:set var="data" value="${form.response.data}"/>
	<c:set var="parameter" value="${data.parameter}"/>

	<input type="hidden" name="action" value="parameterUpdate"/>
	<input type="hidden" name="billingId" value="${form.param.billingId}"/>
	<input type="hidden" name="contractId" value="${form.param.contractId}"/>
	<input type="hidden" name="paramId" value="${form.param.paramId}"/>
	<input type="hidden" name="paramType" value="${form.param.paramType}"/>

	<c:set var="saveCommand">if( sendAJAXCommand( formUrl( this.form ) ) ){ openUrlToParent( '${form.returnUrl}',  $('#${parametersInfo}') ) }</c:set>
	<c:set var="focusFieldUiid" value="${u:uiid()}"/>

	<c:set var="paramType" value="${form.param.paramType}"/>
	<c:choose>
		<c:when test="${paramType eq 1}"> <!-- text -->
			<input type="text" style="width:100%;" id="${focusFieldUiid}" class="mb1"
					name="value" value="${u.escapeXml( form.param.value )}"
					onkeypress="if( enterPressed( event )){ ${saveCommand} }"
					onchange="$(this).attr( 'changed', '1')"/>
		</c:when>

		<c:when test="${paramType eq 2}"> <!-- address -->
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

			<table style="width: 100%;" class="mb1">
				<tr>
					<td>${l.l('Улица')}: </td>
					<td width="70%">
						<html:text property="street" value="${streetTitle}" style="width: 100%"/>
					</td>
					<td nowrap="nowrap">${l.l('Дом')}:</td>
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

		<c:when test="${paramType eq 3}"> <!-- email + subscriptions -->
			<textarea name="emails" style="width:100%;" rows="4" id="${focusFieldUiid}">${form.response.data.emails.getEmailsAsString()}</textarea>
			<div class="hint">Вводите каждый адрес с новой строки</div>
			<input type="hidden" name="eid" value="${form.response.data.emails.eid}">

			<c:if test="${dbInfo.getVersion().compareTo( '5.2' ) lt 0 }">
				Рассылки: <br/>
				<c:set var="treeValueId" value="${u:uiid()}"/>
				<ul id="${treeValueId}">
					<c:set var="values" value="${form.response.data.emails.subscrs}" scope="request"/>
					<c:set var="paramName" value="value" scope="request"/>
					<c:forEach var="node" items="${form.response.data.emails.subscrsTree}">
						<c:set var="node" value="${node}" scope="request" />
						<jsp:include page="/WEB-INF/jspf/tree_item.jsp" />
					</c:forEach>
				</ul>

				<script>
					$( function()
					{
						$("#${treeValueId}").Tree();
					});
				</script>

				<br/>
			</c:if>
		</c:when>

		<c:when test="${paramType eq 6}"> <!-- date -->
			<input type="text" name="value" value="${form.param.value}" id="${focusFieldUiid}" class="mr1"/>
			<c:set var="selector" value="#${focusFieldUiid}"/>
			<c:set var="editable" value="true"/>
			<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
			<c:set var="hideButtons" value="1"/>
		</c:when>

		<c:when test="${paramType eq 7}"> <!-- list -->
			<c:set var="value" value="${data.value}"/>
			<div class="mb1">
				<ui:select-single list="${value.values}" hiddenName="value" value="${value.id}" style="width: 250px;"/>
			</div>

			<%-- <select name="listValueId" style="width: 100%" multiline="1" size="10" >
				<c:forEach var="value" items="${data.valueList}">
					 <option value="${ value.getId() }">${ value.getTitle() }</option>
				</c:forEach>
			</select> --%>
		</c:when>

		<c:when test="${paramType eq 9}"> <!-- phone -->
			<table class="data mb1" style="width: 100%;">
				<tr><td>N</td><td colspan="2">код страны</td><td>код города</td><td>номер</td><td>коментарий</td></tr>

				<c:forEach begin="1" end="${setup['param.phone.item.count']}" varStatus="status">
					<tr>
						<td width="10">${status.count}.</td>
						<td width="5">+</td>

						<c:set var="key" value="parts${status.count}"/>
						<c:set var="parts" value="${data[key]}"/>

						<td width="35"><html:text property="part1${status.count}" size="3" styleClass="paramPhone" value="${parts[0]}"/></td>
						<td width="55"><html:text property="part2${status.count}" size="5" styleClass="paramPhone" value="${parts[1]}"/></td>
						<td width="80"><html:text property="part3${status.count}" size="11" styleClass="paramPhone" value="${parts[2]}"/></td>

						<c:set var="key" value="comment${status.count}"/>

						<td><html:text property="${key}" style="width: 100%" value="${data[key]}"/></td>
					</tr>
				</c:forEach>
			</table>
		</c:when>

		<c:when test="${paramType eq 5 }"> <!-- flag -->

			<select name="value" >
				<c:choose>
					<c:when test="${form.param.value eq 1}">
						<option value="1" selected="selected">Да</option>
						<option value="0">Нет</option>
					</c:when>
					<c:when test="${form.param.value eq 0 or empty form.param.values}">
						<option value="1">Да</option>
						<option value="0" selected="selected">Нет</option>
					</c:when>
				</c:choose>
			</select>
			<br>
			<br>
		</c:when>

		<c:otherwise>
			параметр не редактируется
		</c:otherwise>
	</c:choose>

	<script>
		$( function()
		{
			$("#${focusFieldUiid}").focus();
		});
	</script>

	<c:if test="${empty hideButtons}">
		<div style="display: inline-block;">
			<button type="button" class="btn-grey" onclick="${saveCommand}">OK</button>
			<button type="button" class="btn-grey ml1" onclick="openUrlToParent( '${form.returnUrl}',  $('#${parametersInfo}'))">${l.l('Отмена')}</button>
		</div>
	</c:if>
</form>