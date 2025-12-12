<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="listValues" value="${frd.listValues}"/>
<c:set var="hideButtons" value="${form.param.hideButtons}"/>
<c:set var="elId" value="${u:uiid()}" />
<c:set var="editFormId" value="editParamForm-${elId}" />
<c:set var="tableId">${form.param.tableId}</c:set>

<c:set var="uiid" value="${u:uiid()}"/>

<c:if test="${parameter.configMap['encrypt'] eq 'encrypted'}">
	<c:set var="encrypt" value="1"/>
	<c:set var="confirmEncryptedParam" value="if( !confirm( 'Вы действительно хотите записать значение \n'+ this.value + '\n в параметр \n'+'${parameter.title}' ) )
	{ $('#${uiid} input').removeAttr('onblurstop');
	setTimeout( function(){ $('#${uiid} input').focus(); document.getSelection().removeAllRanges(); }, 0 ); return false; }"/>
</c:if>

<c:set var="saveCommand" value="$('#${uiid} input').attr('onblurstop','1'); ${confirmEncryptedParam} $$.ajax.post($('#${editFormId}')).done(() => $$.ajax.load('${form.returnUrl}', $('#${tableId}').parent())).fail(() =>  $('#${uiid} input').removeAttr('onblurstop'))"/>
<c:set var="refreshCommand" value="$$.ajax.load('${form.returnUrl}', $('#${tableId}').parent());"/>
<c:set var="focusFieldUiid" value="${u:uiid()}"/>

<html:form method="GET" action="/user/parameter" styleId="${editFormId}" style="width: 100%;" onsubmit="return false;">
	<html:hidden property="id" />
	<input type="hidden" name="method" value="parameterUpdate" />
	<input type="hidden" name="paramId" value="${parameter.id}" />

	<c:set var="multiple" value="${parameter.configMap.getBoolean('multiple')}" />

	<h1>${parameter.title}</h1>

	<%-- этот хитрый атрибут changed ловится в некоторых местах, например в мастере, чтобы перегрузить всё,
		 кнопки сохранения в этом случае скрыты --%>
	<c:set var="changeAttrs">onchange="$(this).attr( 'changed', '1');"</c:set>
	<c:set var="onEnter">onkeypress="if (enterPressed(event)){ ${saveCommand} }"</c:set>
	<c:set var="saveOn" value="${u.maskEmpty(parameter.configMap.saveOn, 'editor')}"/>

	<c:set var="onBlur" value=""/>
	<c:if test="${saveOn eq 'focusLost'}">
		<c:set var="onBlur">onBlur="if( $(this).attr('onblurstop') ){ return; }<c:if test="${empty encrypt}">if( $(this).attr( 'changed' ) == '1' )</c:if>{ ${saveCommand}; document.getSelection().removeAllRanges(); } <c:if test="${empty encrypt}">else { ${refreshCommand}  }</c:if>"</c:set>
		<c:set var="hideOkButton" value="1"/>
	</c:if>

	<div style="width: 100%;" id="${uiid}" >
		<c:choose>
			<c:when test="${parameter.type eq 'address'}">
				<script>
					$(function() {
						addAddressSearch( "#${editFormId}" );
					})
				</script>

				<c:set var="address" value="${frd.address}"/>
				<c:set var="house" value="${frd.house}"/>

				<c:set var="streetTitle" value="" />
				<c:set var="houseTitle" value="" />

				<c:if test="${not empty house}">
					<c:set var="streetTitle"
						value="${house.addressStreet.addressCity.title} - ${house.addressStreet.title}" />
					<c:set var="houseTitle" value="${house.houseAndFrac}" />
				</c:if>

				<c:set var="pod" value="" />
				<c:if test="${address.pod gt 0}">
					<c:set var="pod" value="${address.pod}" />
				</c:if>

				<html:hidden property="position" />

				<input type="hidden" name="streetId" value="${house.addressStreet.id}" />
				<input type="hidden" name="houseId" value="${house.id}" />

				<table style="width: 100%;">
					<tr class="in-pl05">
						<td width="70%">
							<input type="text" name="street" value="${streetTitle}"
								placeholder="${l.l('Улица')}" title="${l.l('Улица')}" style="width: 100%"/>
						</td>
						<td width="30%" nowrap="nowrap">
							<div style="display: table-cell; width: 100%;">
								<input type="text" name="house" value="${houseTitle}" onchange="this.form.houseId.value = ''"
									placeholder="${l.l('Дом')}" title="${l.l('Дом')}" style="width: 100%" />
							</div>
							<p:check action="ru.bgcrm.struts.action.DirectoryAddressAction:addressUpdate">
								<c:url var="addUrl" value="/user/directory/address.do">
									<c:param name="method" value="addressUpdate" />
									<c:param name="addressHouseId" value="0" />
								</c:url>

								<c:set var="addScript">
									const streetId = this.form.streetId.value;
									if (!streetId) {
										alert('${l.l("Не указана улица")}');
										return;
									}

									const house = this.form.house.value;
									if (!house) {
										alert('${l.l("Введите номер дома")}');
										return;
									}

									const url = '${addUrl}&addressItemId=' + streetId + '&house=' + encodeURIComponent(house);
									$$.ajax.post(url).done(() => alert('${l.l("Дом добавлен")}'));
								</c:set>
								<div style="display: table-cell;">
									<button onclick="${addScript}" title="${l.l("Добавить дом")}" class="btn-green btn-icon ml05"><i class="ti-plus"></i></button>
								</div>
							</p:check>
						</td>
					</tr>
					<tr class="in-pt05 in-pl05">
						<td><input type="text" name="flat" value="${address.flat}"
							placeholder="${l.l("Квартира / офис")}" title="${l.l("Квартира / офис")}" style="width: 100%"
							onkeyup="if($(this).val().length>0) {$(this.form).find('input[name=room]').removeAttr('disabled');}	else {$(this.form).find('input[name=room]').attr('disabled',true);}" /></td>

						<c:set var="roomState" value="" />
						<c:if test="${ empty address.flat }">
							<c:set var="roomState" value="disabled" />
						</c:if>
						<td><input type="text" name="room" value="${address.room}"
							placeholder="${l.l("Комната")}" title="${l.l("Комната")}" style="width: 100%" ${roomState} /></td>
					</tr>
					<tr class="in-pt05 in-pl05">
						<td><input type="text" name="pod" value="${pod}"
							placeholder="${l.l("Подъезд")}" title="${l.l("Подъезд")}" style="width: 100%" /></td>
						<td><input type="text" name="floor" value="${address.floor}"
							placeholder="${l.l("Этаж")}" title="${l.l("Этаж")}"  style="width: 100%" /></td>
					</tr>
					<tr class="in-pt05 in-pl05">
						<td colspan="2">
							<input type="text" name="comment" value="${address.comment}"
								placeholder="${l.l('Comment')}" title="${l.l('Comment')}" style="width: 100%" />
						</td>
					</tr>
				</table>
			</c:when>

			<c:when test="${parameter.type eq 'blob'}">
				<c:set var="rows" value="rows='${u.maskEmpty(parameter.configMap.rows, '4')}'"/>
				<textarea id="${focusFieldUiid}" name="value" ${rows}  style="width: 100%;" ${changeAttrs} ${onBlur}>${frd.value}</textarea>
			</c:when>

			<c:when test="${parameter.type eq 'date' or parameter.type eq 'datetime'}">
				<c:set var="hideButtons" value="1"/>
				<c:set var="type" value="${u.maskEmpty(parameter.configMap.type, 'ymd')}"/>
				<input type="text" name="value" value="${tu.format(frd.value, type)}" id="${focusFieldUiid}" ${changeAttrs}/>
				<ui:date-time selector="#${focusFieldUiid}" type="${type}" saveCommand="${saveCommand}"/>
			</c:when>

			<c:when test="${parameter.type eq 'email'}">
				<%@ include file="edit/email/editor.jsp"%>
			</c:when>

			<c:when test="${parameter.type eq 'list'}">
				<%-- Also used: 'listValues', 'multiple', 'listParamConfig' --%>
				<c:set var="value" value="${frd.values}"/>
				<%@ include file="edit/list/editor.jsp"%>
			</c:when>

			<c:when test="${parameter.type eq 'listcount'}">
				<%-- Also used: 'listValues', 'multiple' --%>
				<c:set var="values" value="${frd.values}"/>
				<%@ include file="edit/listcount/editor.jsp"%>
			</c:when>

			<c:when test="${parameter.type eq 'money'}">
				<input id="${focusFieldUiid}" type="text" name="value" value="${frd.value}" size="10" onkeydown="if (enterPressed(event)){ ${saveCommand} }; return isNumberKey(event)" ${changeAttrs} ${onBlur}/>
				<span class="hint">${l.l('Use dot as a decimal separator')}</span>
			</c:when>

			<c:when test="${parameter.type eq 'phone'}">
				<c:set var="value" value="${frd.value}"/>
				<%@ include file="edit/phone/editor.jsp"%>
			</c:when>

			<c:when test="${parameter.type eq 'text'}">
				<c:choose>
					<c:when test="${frd.value eq '<ЗНАЧЕНИЕ ЗАШИФРОВАНО>'}">
						<c:set var="checkedParamValue" value=""/>
					</c:when>
					<c:otherwise>
						<c:set var="checkedParamValue" value="${u.escapeXml(frd.value)}"/>
					</c:otherwise>
				</c:choose>

				<input id="${focusFieldUiid}" type="text" name="value" value="${checkedParamValue}" style="width: 100%;" ${changeAttrs} ${onBlur} ${onEnter}/>
			</c:when>

			<c:when test="${parameter.type eq 'tree'}">
				<c:set var="treeValueId" value="${u:uiid()}" />

				<ul id="${treeValueId}">
					<c:set var="values" value="${frd.values}" scope="request" />
					<c:set var="paramName" value="value" scope="request" />
					<c:forEach var="node" items="${frd.treeRootNode.children}">
						<c:set var="node" value="${node}" scope="request" />
						<jsp:include page="/WEB-INF/jspf/tree_item.jsp" />
					</c:forEach>
				</ul>

				<script>
					$(function()
					{
						$("#${treeValueId}").Tree({
							 <c:if test="${not multiple}">singleSelect : 'singleSelect'</c:if>
						});
					});
				</script>
			</c:when>

			<c:when test="${parameter.type eq 'treecount'}">
				<%-- Also used: 'multiple'? --%>
				<c:set var="values" value="${frd.values}"/>
				<c:set var="treeValues" value="${frd.treeValues}"/>
				<c:set var="treeRootNode" value="${frd.treeRootNode}"/>
				<%@ include file="edit/treecount/editor.jsp"%>
			</c:when>
		</c:choose>
	</div>

	<c:if test="${empty hideButtons}">
		<div class="hint">${parameter.comment}</div>

		<div class="mt1">
			<c:if test="${empty hideOkButton}">
				<ui:button type="ok" styleClass="mr1" onclick="${saveCommand}"/>
			</c:if>
			<ui:button type="cancel" onclick="$$.ajax.load('${form.returnUrl}', $('#${tableId}').parent())"/>
		</div>
	</c:if>
</html:form>

<script>
	$$.ui.inputFocus("${focusFieldUiid}");
</script>
