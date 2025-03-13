<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="parametersInfo" value= "${form.param.billingId }-${form.param.contractId }-parameters-info"/>
<c:set var="dbInfo" value="${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[form.param.billingId]}"/>

<form action="/user/plugin/bgbilling/proto/contract.do" onsubmit="return false;" class="editorStopReload">
	<c:set var="data" value="${frd}"/>
	<c:set var="parameter" value="${data.parameter}"/>

	<input type="hidden" name="method" value="parameterUpdate"/>
	<input type="hidden" name="billingId" value="${form.param.billingId}"/>
	<input type="hidden" name="contractId" value="${form.param.contractId}"/>
	<input type="hidden" name="paramId" value="${form.param.paramId}"/>
	<input type="hidden" name="paramType" value="${form.param.paramType}"/>

	<c:set var="saveCommand">$$.ajax.post(this.form).done(() => $$.ajax.load('${form.returnUrl}', $('#${parametersInfo}').parent()))</c:set>
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

		<c:when test="${paramType eq 3}"> <!-- email + subscriptions -->
			<c:choose>
				<c:when test="${dbInfo.getVersion().compareTo( '9.1' ) gt 0 }">
					<div class="mb1">
						<c:set var="addButtonUiid" value="${u:uiid()}"/>
						<table class="data">
							<tr>
								<td width="50%">Email</td>
								<td width="50%">Имя</td>
								<td><ui:button type="add" id="${addButtonUiid}" styleClass="btn-small" onclick="$$.param.email.addValue(this)"/></td>
							</tr>
							<c:forEach var="item" items="${data.emails.entityAttrEmail.contactList}">
								<tr>
									<td><input type="text" name="address" value="${item.address}" class="w100p"/></td>
									<td><input type="text" name="name" value="${item.name}" class="w100p"/></td>
									<td><button class="btn-white btn-small icon" onclick="$$.param.email.delValue(this)"><i class="ti-trash"></i></button></td>
								</tr>
							</c:forEach>
						</table>
						<c:if test="${empty data.emails.entityAttrEmail.contactList}">
							<script>
								$(function() {
									document.getElementById('${addButtonUiid}').click();
								})
							</script>
						</c:if>
					</div>
				</c:when>
				<c:otherwise>
					<textarea name="emails" style="width:100%;" rows="4" id="${focusFieldUiid}">${frd.emails.getEmailsAsString()}</textarea>
					<div class="hint">Вводите каждый адрес с новой строки</div>
					<input type="hidden" name="eid" value="${frd.emails.eid}">
				</c:otherwise>
			</c:choose>
			<c:if test="${dbInfo.getVersion().compareTo( '5.2' ) lt 0 }">
				Рассылки: <br/>
				<c:set var="treeValueId" value="${u:uiid()}"/>
				<ul id="${treeValueId}">
					<c:set var="values" value="${frd.emails.subscrs}" scope="request"/>
					<c:set var="paramName" value="value" scope="request"/>
					<c:forEach var="node" items="${frd.emails.subscrsTree}">
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
			<ui:date-time paramName="value" value="${form.param.value}" saveCommand="${saveCommand}" id="${focusFieldUiid}" styleClass="mr1"/>
			<c:set var="hideButtons" value="1"/>
		</c:when>

		<c:when test="${paramType eq 7}"> <!-- list -->
			<c:set var="value" value="${data.value}"/>
			<div class="mb1">
				<ui:select-single list="${value.values}" hiddenName="value" value="${value.id}" style="width: 250px;"/>
			</div>
		</c:when>

		<c:when test="${paramType eq 9}"> <!-- phone -->
			<div class="mb1">
				<%@ include file="/WEB-INF/jspf/user/parameter/edit/phone/editor.jsp"%>
			</div>
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
			<button type="button" class="btn-white ml1" onclick="$$.ajax.load('${form.returnUrl}', $('#${parametersInfo}').parent())">Отмена</button>
		</div>
	</c:if>
</form>