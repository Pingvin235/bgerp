<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${not empty part2Rules}">
	<script>
		var index = 0;
		var jump_regexp = new Array;

		<c:forEach var="item" items="${part2Rules}">
			jump_regexp[index] = new Object;

			<c:choose>
				<c:when test="${not item.regexp.startsWith('/')}">
					<c:choose>
						<c:when test="${!item.regexp.endsWith('/')}">
							jump_regexp[index].regexp = /${item.regexp}/;
						</c:when>
					</c:choose>
				</c:when>
				<c:otherwise>
					jump_regexp[index].regexp = ${item.regexp};
				</c:otherwise>
			</c:choose>

			jump_regexp[index].moveLastChar = ${item.moveLastChar};
			index++;
		</c:forEach>

		$("input.paramPhone[name^=part2]").on('keyup',
				function () {
					var len = $( this ).val().length;
					var value = $( this ).val();

					if (len != 0) {
						for (var i = 0; i < jump_regexp.length; i++) {
							var expr = jump_regexp[i];
							var nextInput = $( this ).parent().next().children();

							if (value.match(expr.regexp) != null) {
								$(nextInput).focus();

								if( expr.moveLastChar == true )
								{
									$( nextInput ).val( value.substring( len-1 ) );
									$( this ).val( value.substring( 0, len - 1 ) );
								}

								break;
							}
						}
					}
				});

	</script>
</c:if>

<c:if test="${not empty setup['param.phone.part.1.default']}">
	<c:set var="phoneDefault" value="${setup['param.phone.part.1.default']}" />

	<script type="text/javascript">
		$("input.paramPhone[name^=part1]").on('click',
			function()
			{
				if( $( this ).val().length == 0 )
				{
					$( this ).val( ${phoneDefault} );
					$( this ).parent().next().children().focus();
				}
			});
	</script>
</c:if>

<c:set var="data" value="${form.response.data}" />
<c:set var="listValues" value="${form.response.data.listValues}"/>
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

<c:set var="saveCommand" value="$('#${uiid} input').attr('onblurstop','1'); ${confirmEncryptedParam} if( sendAJAXCommand( formUrl( $('#${editFormId}') ) ) ){  $$.ajax.load('${form.returnUrl}', $('#${tableId}').parent());  } else {  $('#${uiid} input').removeAttr('onblurstop'); }"/>
<c:set var="refreshCommand" value="$$.ajax.load('${form.returnUrl}', $('#${tableId}').parent());"/>
<c:set var="focusFieldUiid" value="${u:uiid()}"/>

<html:form method="GET" action="/user/parameter" styleId="${editFormId}" style="width: 100%;" onsubmit="return false;">
	<html:hidden property="id" />
	<input type="hidden" name="action" value="parameterUpdate" />
	<input type="hidden" name="paramId" value="${parameter.id}" />

	<c:set var="multiple" value="${parameter.configMap.getBoolean('multiple')}" />

	<h1>${parameter.title}</h1>

	<%-- этот хитрый атрибут changed ловится в некоторых местах, например в мастере, чтобы перегрузить всё,
		 кнопки сохранения в этом случае скрыты --%>
	<c:set var="changeAttrs">onchange="$(this).attr( 'changed', '1');"</c:set>
	<c:set var="onEnter">onkeypress="if( enterPressed( event ) ){ ${saveCommand} }"</c:set>
	<c:set var="saveOn" value="${u:maskEmpty(parameter.configMap.saveOn, 'editor')}"/>

	<c:set var="onBlur" value=""/>
	<c:if test="${saveOn eq 'focusLost'}">
		<c:set var="onBlur">onBlur="if( $(this).attr('onblurstop') ){ return; }<c:if test="${empty encrypt}">if( $(this).attr( 'changed' ) == '1' )</c:if>{ ${saveCommand}; document.getSelection().removeAllRanges(); } <c:if test="${empty encrypt}">else { ${refreshCommand}  }</c:if>"</c:set>
		<c:set var="hideOkButton" value="1"/>
	</c:if>

	<div style="width: 100%;" id="${uiid}" >
		<c:choose>
			<c:when test="${parameter.type eq 'text'}">

				<c:choose>
					<c:when test="${data.value eq '<ЗНАЧЕНИЕ ЗАШИФРОВАНО>'}">
						<c:set var="checkedParamValue" value=""/>
					</c:when>
					<c:otherwise>
						<c:set var="checkedParamValue" value="${u.escapeXml( data.value)}"/>
					</c:otherwise>
				</c:choose>

				<input id="${focusFieldUiid}" type="text" name="value" value="${checkedParamValue}" style="width: 100%;" ${changeAttrs} ${onBlur} ${onEnter}/>
			</c:when>

			<c:when test="${parameter.type eq 'money'}">
				<input id="${focusFieldUiid}" type="text" name="value" value="${data.value}" size="10" onkeydown="return isNumberKey(event)" ${changeAttrs} ${onBlur} ${onEnter}/>
				<span class="hint">${l.l('Используйте точку как десятичный разделитель.')}</span>
			</c:when>

			<c:when test="${parameter.type eq 'blob'}">
				<c:set var="rows" value="rows='${u:maskEmpty(parameter.configMap.rows, '4')}'"/>
				<textarea id="${focusFieldUiid}" name="value" ${rows}  style="width: 100%;" ${changeAttrs} ${onBlur}>${data.value}</textarea>
			</c:when>

			<c:when test="${parameter.type eq 'date' or parameter.type eq 'datetime'}">
				<c:set var="selector">#${uiid} input[name='value']</c:set>
				<c:set var="hideButtons" value="1"/>

				<c:set var="getCommand"></c:set>
				<c:set var="getDateUrl"></c:set>

				<c:if test="${parameter.configMap.sendColorMapRequest eq 1}">
					<c:url var="getDateUrl" value="parameter.do">
						<c:param name="action" value="parameterGet"/>
						<c:param name="id" value="${form.id}"/>
						<c:param name="paramId" value="${parameter.id}"/>
					</c:url>
				</c:if>

				<c:set var="type" value="${u.maskEmpty(parameter.configMap.type, 'ymd')}"/>
				<input type="text" name="value" value="${tu.format(data.value, type)}" id="${focusFieldUiid}" ${changeAttrs} onclick="${getCommand}"/>
				<ui:date-time selector="#${focusFieldUiid}" type="${type}" saveCommand="${saveCommand}"/>
			</c:when>

			<c:when test="${parameter.type eq 'tree'}">
				<c:set var="treeValueId" value="${u:uiid()}" />

				<ul id="${treeValueId}">
					<c:set var="values" value="${data.value}" scope="request" />
					<c:set var="paramName" value="value" scope="request" />
					<c:forEach var="node" items="${treeValues.children}">
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

			<c:when test="${parameter.type eq 'listcount'}">
				<%-- Also used: 'listValues', 'multiple' --%>
				<c:set var="values" value="${data.value}" />
				<%@ include file="edit/listcount/editor.jsp"%>
			</c:when>

			<c:when test="${parameter.type eq 'list'}">
				<c:set var="value" value="${data.value}"/>

				<c:set var="listParamConfig" value="${parameter.configMap.getConfig('ru.bgcrm.model.param.config.ListParamConfig')}"/>

				<c:choose>
					<c:when test="${multiple}">
						<c:forEach var="item" items="${listValues}">
							<table style="width: 100%;" class="nopad">
								<tr>
									<c:set var="checkUiid" value="${u:uiid()}" />
									<c:set var="tdUiid" value="${u:uiid()}" />

									<c:set var="commentInputShow"
										value="${not empty listParamConfig.commentValues[item.id]}" />

									<c:set var="scriptCheck">
										<c:if test="${commentInputShow}">
											onchange="if( this.checked ){ $('#${tdUiid}').show() } else { $('#${tdUiid}').hide() }"
										</c:if>
									</c:set>
									<c:set var="scriptInput">
										<c:if test="${commentInputShow}">
											onchange="$('#${checkUiid}').val( ${item.id} + ':' + this.value )"
										</c:if>
									</c:set>

									<c:set var="hideStyle">
										<c:if test="${not commentInputShow or value[item.id] == null}">
											style="display: none"
										</c:if>
									</c:set>

									<td width="30" align="center">
										<input type="checkbox" name="value" value="${item.id}:${value[item.id]}" id="${checkUiid}"	${u:checkedFromCollection( value, item.id )} ${scriptCheck} />
									</td>
									<td>
										${item.title}
										<span id="${tdUiid}" ${hideStyle}>
											<input type="text" size="30" value="${value[item.id]}" ${scriptInput} />
											<c:if test="${not empty listParamConfig.needCommentValues[item.id]}">
												*
											</c:if>
										</span>
									</td>
								</tr>
							</table>
						</c:forEach>
					</c:when>
					<c:otherwise>
						<c:set var="valueUiid" value="${u:uiid()}"/>
						<c:set var="commentUiid" value="${u:uiid()}"/>
						<c:set var="fullUiid" value="${u:uiid()}"/>

						<c:set var="currentValue" value="0"/>
						<c:set var="currentComment" value=""/>
						<c:set var="currentFull" value=""/>

						<c:forEach var="item" items="${value}">
							<c:set var="currentValue" value="${item.key}"/>
							<c:set var="currentComment" value="${item.value}"/>
							<c:set var="currentFull" value="${item.key}:${item.value}"/>
						</c:forEach>

						<input id="${valueUiid}" type="hidden" value="${currentValue}" />
						<input id="${fullUiid}" type="hidden" name="value" value="${currentFull}" />

						<%-- значения с комментарием --%>
						<c:set var="commentValues" value="" />
						<c:forEach var="item" items="${listValues}">
							<c:if test="${not empty listParamConfig.commentValues[item.id]}">
								<c:if test="${not empty commentValues}">
									<c:set var="commentValues" value="${commentValues}," />
								</c:if>
								<c:set var="commentValues" value="${commentValues}'${item.id}'" />
							</c:if>
						</c:forEach>

						<c:set var="commentValues" value="[${commentValues}]" />

						<c:set var="changeScript">
							var val = $('#${valueUiid}').val();
							console.log( val );
							$('#${fullUiid}').val( val + ':' + $('#${commentUiid}').val() );
							if( ${commentValues}.indexOf( val ) >= 0 ){ $('#${commentUiid}').show() } else { $('#${commentUiid}').hide() };
							<c:if test="${saveOn eq 'select'}">
								${saveCommand}
							</c:if>
						</c:set>

						<c:set var="editAs" value="${parameter.configMap.editAs}"/>

						<c:choose>
							<c:when test="${editAs eq 'radio'}">
								<div>
									<input type="radio" name="rValue" value="0"
										checked="1"
										onchange="if( this.checked ){ $('#${valueUiid}').val( this.value );  ${changeScript} }"/>
										&#160;-- ${l.l('не указан')} --
								</div>
								<c:forEach var="item" items="${listValues}">
									<div class="mt05">
										<input type="radio" id="${radioId}" name="rValue"
											value="${item.id}" ${u:checkedFromCollection( value, item.id )}
											onchange="if( this.checked ){ $('#${valueUiid}').val( this.value );  ${changeScript} }"/>
										&#160;${item.title}
									</div>
								</c:forEach>
							</c:when>
							<c:when test="${editAs eq 'select'}">
								<ui:select-single list="${listValues}" value="${currentValue}" onSelect="$('#${valueUiid}').val( $hidden.val() ); ${changeScript}" styleClass="w100p"/>
							</c:when>
							<c:otherwise>
								<u:sc>
									<c:set var="valuesHtml">
										<li value="0">-- ${l.l('значение не установлено')} --</li>
										<c:forEach var="item" items="${listValues}">
											<li value="${item.id}"	${u:selectedFromCollection( value, item.id )}>${item.title}</li>
										</c:forEach>
									</c:set>
									<c:set var="value" value="${currentValue}"/>
									<c:set var="style" value="width: 100%;"/>
									<c:set var="onSelect" value="$('#${valueUiid}').val( $hidden.val() ); ${changeScript}"/>
									<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
								</u:sc>
							</c:otherwise>
						</c:choose>

						<c:set var="commentDisplayStyle">display:none;</c:set>
						<c:if test="${not empty listParamConfig.commentValues[currentValue]}">
							<c:remove var="commentDisplayStyle"/>
						</c:if>

						<input id="${commentUiid}" type="text" style="width: 100%; ${commentDisplayStyle}" onchange="${changeScript}"
								value="${currentComment}" placeholder="${l.l('Комментарий')}" class="mt1"/>
					</c:otherwise>
				</c:choose>
			</c:when>
			<c:when test="${parameter.type eq 'phone'}">
				<table style="width: 100%;" class="data">
					<tr>
						<td>N</td>
						<td colspan="2">код страны</td>
						<td>код города</td>
						<td>номер</td>
						<td>коментарий</td>
					</tr>

					<c:forEach begin="1" end="${setup['param.phone.item.count']}" varStatus="status">
						<tr>
							<td width="10">${status.count}.</td>
							<td width="5">+</td>

							<c:set var="key" value="parts${status.count}" />
							<c:set var="parts" value="${data[key]}" />

							<td width="35"><html:text property="part1${status.count}" size="3" styleClass="paramPhone" value="${parts[0]}" /></td>
							<td width="55"><html:text property="part2${status.count}" size="5" styleClass="paramPhone" value="${parts[1]}" /></td>
							<td width="80"><html:text property="part3${status.count}" size="11" styleClass="paramPhone" value="${parts[2]}" /></td>

							<c:set var="key" value="comment${status.count}" />

							<td><html:text property="${key}" style="width: 100%" value="${data[key]}" /></td>
						</tr>
					</c:forEach>
				</table>
				<%--
				буфер обмена параметров (в данный момент нигде не работает)
				<div align="right" style="width: 100%;">
					<select
						onfocus="bgcrm.buffer.fillWithStoredObjects( $(this), 'phone' )"
						style="width: 100%;"></select>
				</div>
				--%>
			</c:when>
			<c:when test="${parameter.type eq 'email'}">
				<c:set var="id" value="${form.id}" />
				<c:set var="parameter" value="${form.response.data.parameter }" />
				<c:set var="email" value="${form.response.data.email}" />

				<input type="hidden" name="action" value="parameterUpdate" />
				<html:hidden property="position" />

				<div class="in-table-cell in-pl05">
					<div><b>EMail:</b></div>
					<div>
						<input id="${focusFieldUiid}" type="text" name="value" style="width: 200px;" value="${email.value}"/>
					</div>
					<div><b>Ком.:</b></div>
					<div style="width: 100%;" class="pl05">
						<input type="text" name="comment" style="width: 100%;" value="${email.comment}"/>
					</div>
				</div>
			</c:when>
			<c:when test="${parameter.type eq 'address'}">
				<script>
					$(function() {
						addAddressSearch( "#${editFormId}" );
					})
				</script>

				<c:set var="address" value="${data.address}" />
				<c:set var="house" value="${data.house}" />

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

				<c:set var="floor" value="" />
				<c:if test="${address.floor gt 0}">
					<c:set var="floor" value="${address.floor}" />
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
									<c:param name="action" value="addressUpdate" />
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
						<td><input type="text" name="floor" value="${floor}"
							placeholder="${l.l("Этаж")}" title="${l.l("Этаж")}"  style="width: 100%" /></td>
					</tr>
					<tr class="in-pt05 in-pl05">
						<td colspan="2">
							<input type="text" name="comment" value="${address.comment}"
								placeholder="${l.l("Комментарий")}" title="${l.l("Комментарий")}" style="width: 100%" />
						</td>
					</tr>
				</table>
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

	<c:remove var="hideButtons"/>
</html:form>

<script>
	$(function () {
		$$.ui.inputFocus($("#${focusFieldUiid}"));
	});
</script>
