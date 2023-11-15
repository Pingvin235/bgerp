<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:sc>
	<%-- the var is used in included item.jsp --%>
	<c:set var="filter" value="${filterFromList}"/>
	<c:set var="type" value="${filterFromList.parameter.type}"/>
	<c:set var="title" value="${not empty filter.title ? filter.title : filter.parameter.title}"/>

	<c:choose>
		<c:when test="${type == 'address'}">
			<c:set var="cityIds" value="${filter.configMap['cityIds']}"/>
			<c:set var="fields" value="${filter.configMap['fields']}"/>
			<c:set var="paramName" value="param${filter.parameter.id}value"/>
			<c:set var="code">
				<c:set var="uiid" value="${u:uiid()}"/>
				<c:set var="buttonId" value="${u:uiid()}"/>
				<input type="button" id="${buttonId}" class="btn-white" onclick="$('#${uiid}').toggle();" value="${title}"/>

				<div id="${uiid}" style="display:none;position:absolute;background-color:#ffffff;border: 1px solid #aaaaaa;border-radius:5px;padding:5;">
					<input type="hidden" name="param${filter.parameter.id}valueCityId" value="${savedParamsFilters.get('param'.concat(filter.parameter.id).concat('valueCityId') )}">
					<input type="hidden" name="param${filter.parameter.id}valueStreetId" value="${savedParamsFilters.get('param'.concat(filter.parameter.id).concat('valueStreetId') )}">
					<input type="hidden" name="param${filter.parameter.id}valueHouseId" value="${savedParamsFilters.get('param'.concat(filter.parameter.id).concat('valueHouseId') )}">
					<input type="hidden" name="param${filter.parameter.id}valueQuarterId" value="${savedParamsFilters.get('param'.concat(filter.parameter.id).concat('valueQuarterId') )}">

					<c:set var="cityFilterId" value="${u:uiid()}"/>
					<c:set var="quarterFilterId" value="${u:uiid()}"/>
					<c:set var="streetFilterId" value="${u:uiid()}"/>
					<c:set var="houseFilterId" value="${u:uiid()}"/>
					<c:set var="flatFilterId" value="${u:uiid()}"/>

					<script>
						addCustomCitySearch( '#${cityFilterId}', '#${uiid} > input[name=param${filter.parameter.id}valueCityId]' );
						addCustomQuarterSearch( '#${quarterFilterId}', '#${uiid} > input[name=param${filter.parameter.id}valueQuarterId]', '${cityIds}' );
						addCustomStreetSearch( '#${streetFilterId}', '#${uiid} > input[name=param${filter.parameter.id}valueStreetId]' );
						addCustomHouseSearch( '#${houseFilterId}', '#${uiid} > input[name=param${filter.parameter.id}valueStreetId]', '#${uiid} > input[name=param${filter.parameter.id}valueHouseId]' );
					</script>

					<table>
						<c:if test="${empty fields}">
							<c:set var="fields" value="city;street;house"/>
						</c:if>

						<%-- город, улица и дом - обязательные поля --%>
						<c:if test="${!fields.contains('house')}">
							<c:set var="fields" value="house;${fields}"/>
						</c:if>
						<c:if test="${!fields.contains('street')}">
							<c:set var="fields" value="street;${fields}"/>
						</c:if>
						<c:if test="${!fields.contains('city')}">
							<c:set var="fields" value="city;${fields}"/>
						</c:if>

						<c:forTokens var="show" delims=";" items="${fields}">
							<c:if test="${show.contains('city')}">
								<tr><td>${l.l('Город')}:</td><td><input id="${cityFilterId}" value="${savedParamsFilters.get('param'.concat(filter.parameter.id).concat('valueCity') )}" name="param${filter.parameter.id}valueCity" type="text" onkeyup="$('#${uiid} > input[name=param${filter.parameter.id}valueCityId]').val('')"/></td></tr>
							</c:if>
							<c:if test="${show.contains('quarter')}">
								<tr><td>${l.l('Квартал')}:</td><td><input id="${quarterFilterId}" value="${savedParamsFilters.get('param'.concat(filter.parameter.id).concat('valueQuarter') )}" name="param${filter.parameter.id}valueQuarter" type="text" onkeyup="$('#${uiid} > input[name=param${filter.parameter.id}valueQuarterId]').val('')"/></td></tr>
							</c:if>
							<c:if test="${show.contains('street')}">
								<tr><td>${l.l('Улица')}:</td><td><input id="${streetFilterId}" value="${savedParamsFilters.get('param'.concat(filter.parameter.id).concat('valueStreet') )}" name="param${filter.parameter.id}valueStreet" type="text" onkeyup="$('#${uiid} > input[name=param${filter.parameter.id}valueStreetId]').val('')"/></td></tr>
							</c:if>
							<c:if test="${show.contains('house')}">
								<tr><td>${l.l('Дом')}:</td><td><input id="${houseFilterId}" value="${savedParamsFilters.get('param'.concat(filter.parameter.id).concat('valueHouse') )}" name="param${filter.parameter.id}valueHouse" type="text" onkeyup="$('#${uiid} > input[name=param${filter.parameter.id}valueHouseId]').val('')" /></td></tr>
							</c:if>
							<c:if test="${show.contains('flat')}">
								<tr><td>${l.l('Квартира')}:</td><td><input id="${flatFilterId}" value="${savedParamsFilters.get('param'.concat(filter.parameter.id).concat('valueFlat') )}" name="param${filter.parameter.id}valueFlat" type="text" onkeyup="$('#${uiid} > input[name=param${filter.parameter.id}valueFlat]').val('')" /></td></tr>
							</c:if>
						</c:forTokens>

						<tr><td colspan="2" align="center" class="in-table-cell pt1">
								<div>
									<input type="button" class="btn-white" value="${l.l('Очистить')}"
											onclick="
												$('#${cityFilterId}').val('').keyup();
												$('#${streetFilterId}').val('').keyup();
												$('#${quarterFilterId}').val('').keyup();
												$('#${houseFilterId}').val('').keyup();
												$('#${flatFilterId}').val('').keyup();
												$('#${uiid} #applyButton').click();
											"/>
								</div>
								<div class="w100p pl1">
									<input id="applyButton" type="button" class="btn-grey w100p" value="${l.l('Применить')}" onclick="
											$$.process.queue.filter.param.addressApply('${uiid}', '${title.replace("'", "\\'")}',
												'${cityFilterId}', '${streetFilterId}', '${quarterFilterId}', '${houseFilterId}', '${flatFilterId}',
												'${buttonId}')"
									/>
								</div>
							</td></tr>
					</table>
				</div>
			</c:set>
			<%@ include file="../item.jsp"%>
		</c:when>

		<c:when test="${type.startsWith('date')}">
			<c:set var="code">
				${title}
				<c:choose>
					<c:when test="${not empty savedParamsFilters.get( 'dateTimeParam'.concat(filter.parameter.id).concat('From') ) }">
						${l.l('с')} <input type="text" value="${savedParamsFilters.get('dateTimeParam'.concat(filter.parameter.id).concat('From')) }" name="dateTimeParam${filter.parameter.id}From" />
					</c:when>
					<c:otherwise>
						${l.l('с')} <input type="text" name="dateTimeParam${filter.parameter.id}From" />
					</c:otherwise>
				</c:choose>
				<c:choose>
					<c:when test="${not empty savedParamsFilters.get( 'dateTimeParam'.concat(filter.parameter.id).concat('To') ) }">
						${l.l('по')} <input type="text" value="${savedParamsFilters.get('dateTimeParam'.concat(filter.parameter.id).concat('To')) }" name="dateTimeParam${filter.parameter.id}To" />
					</c:when>
					<c:otherwise>
						${l.l('по')} <input type="text" name="dateTimeParam${filter.parameter.id}To" />
					</c:otherwise>
				</c:choose>
			</c:set>

			<u:sc>
				<c:set var="type" value="ymd" />
				<c:set var="selector">${selectorForm} input[name='dateTimeParam${filter.parameter.id}From']</c:set>
				<c:set var="editable" value="1"/>
				<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
				<c:set var="selector">${selectorForm} input[name='dateTimeParam${filter.parameter.id}To']</c:set>
				<c:set var="editable" value="1"/>
				<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
			</u:sc>

			<%@ include file="../item.jsp"%>
		</c:when>

		<c:when test="${type == 'list' or type == 'listcount'}">
			<c:set var="code">
				<u:sc>
					<c:set var="paramName" value="param${filter.parameter.id}value"/>
					<c:set var="values" value="${savedParamsFilters.getSelectedValues(paramName)}"/>

					<ui:combo-check paramName="${paramName}"
						list="${filter.parameter.listParamValues}" available="${filter.availableValues}"
						values="${empty values ? filter.defaultValues : values}"
						showFilter="${true}" prefixText="${title}:" widthTextValue="12em"/>
				</u:sc>
			</c:set>

			<%@ include file="../item.jsp"%>
		</c:when>

		<c:when test="${type eq 'money'}">
			<c:set var="code">
				<input type="checkbox" name="param${filter.parameter.id}empty"/>
				<span>${l.l('Undefined')}&nbsp;${l.l('or')}</span>
				<input type="text" name="param${filter.parameter.id}From" size="3" onkeydown="return isNumberKey(event)" placeholder="${l.l('range.from')}"/>
				<input type="text" name="param${filter.parameter.id}To" size="3" onkeydown="return isNumberKey(event)" placeholder="${l.l('range.to')}"/>
			</c:set>

			<%@ include file="../item.jsp"%>
		</c:when>

		<c:when test="${type == 'text' || type == 'blob'}">
			<c:set var="code">
				<input type="text" name="param${filter.parameter.id}value" placeholder="${title}" size="20" onkeypress="if( enterPressed( event ) ){ ${sendCommand} }"/>
			</c:set>

			<%@ include file="../item.jsp"%>
		</c:when>

		<c:otherwise>
			<c:set var="code">
				Unsupported filter for param type '${type}'
			</c:set>

			<%@ include file="../item.jsp"%>
		</c:otherwise>
	</c:choose>
</u:sc>