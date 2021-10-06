<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${empty tableId}">
	<c:set var="tableId" value="${u:uiid()}"/>
</c:if>

<c:set var="showTr">${empty onlyData}</c:set>
<c:set var="showId" value="${form.param.showId ne '0'}"/>

<c:url var="paramLogUrl" value="/user/parameter.do">
	<c:param name="action" value="parameterLog"></c:param>
	<c:param name="id" value="${id}"></c:param>
	<c:param name="objectType" value="${form.param.objectType}"></c:param>
	<c:param name="returnUrl" value="${form.requestUrl}"></c:param>
</c:url>

<c:if test="${not empty form.param.header}">
	<div class="mt1 mb05">
		<h2 style="display: inline;">${form.param.header}</h2> [<a href="#" onclick="$$.ajax.load('${paramLogUrl}', $('#${tableId}').parent()); return false;">${l.l('лог изменений')}</a>]
	</div>
</c:if>

<table id="${tableId}" class="data">
	<c:if test="${showTr}">
		<tr>
			<c:if test="${showId}">
				<td width="30">ID</td>
			</c:if>
			<td>${l.l('Название')}</td>
			<td width="100%">${l.l('Значение')}</td>
		</tr>
	</c:if>
	<c:forEach var="item" items="${list}">
		<c:set var="parameter" value="${item.parameter}"/>

		<%-- списковый параметр выбором в виде радиокнопок --%>
		<c:set var="editorTypeParameterName">param.${parameter.id}.editor</c:set>
		<c:set var="editorType">${paramsConfig[editorTypeParameterName]}</c:set>

		<c:set var="radioSelect" value="${(parameter.type eq 'list') and (editorType eq 'radio')}"/>

		<c:if test="${radioSelect}">
			<c:set var="radioSelectNotChoosed">не опр.</c:set>

			<c:set var="paramName">param.${parameter.id}.editorRadioNotChoosedText</c:set>
			<c:if test="${not empty paramsConfig[paramName] }">
				<c:set var="radioSelectNotChoosed" value="${paramsConfig[paramName]}"/>
			</c:if>
		</c:if>

		<%-- TODO: В дальнейшем добавить проверку пермишена на изменение параметра. --%>
		<c:set var="readonly" value="${parameter.configMap.readonly eq 1}"/>

		<c:set var="multiple" value="${parameter.configMap.multiple}" />

		<%-- флаг readonly выставлен для параметра в конфигурации типа процесса
		<c:if test="${not empty form.param['processTypeId']}">
			<c:set var="processTypeConfig" value="${ctxProcessTypeMap[u:int( form.param['processTypeId'] )].properties.configMap}"/>
			<c:if test="${u.toIntegerSet(processTypeConfig['readonlyParamIds']).contains(parameter.id)}">
				<c:set var="readonly" value="true"/>
			</c:if>
		</c:if>
		--%>

		<c:set var="hide" value=""/>
		<c:if test="${parameter.configMap.hide == '1'}">
			<c:set var="hide" value="style='display:none'"/>
		</c:if>

		<c:set var="viewDivId" value="${u:uiid()}"/>
		<c:set var="editDivId" value="${u:uiid()}"/>
		<c:set var="editColspan" value="2"/>

		<c:set var="startEdit">{  $('#${viewDivId}').hide(); $('#${editDivId}').parent().show(); }; return false;</c:set>

		<tr ${hide} id="${viewDivId}" title="${parameter.comment}">
			<c:if test="${showTr and showId}">
				<td>${parameter.id}</td>
				<c:set var="editColspan" value="3"/>
			</c:if>

			<td nowrap="nowrap">${parameter.title}</td>
			<td width="100%">
				<c:choose>
					<c:when test="${'file' eq parameter.type}">
						<c:forEach var="file" items="${item.value}" varStatus="status">
							<c:set var="value" value="${file.value}" />

							<div>
								<c:if test="${not readonly}">
									<html:form action="/user/parameter" styleId="${editFormId}"
										style="display: inline;">
										<input type="hidden" name="action" value="parameterUpdate" />
										<html:hidden property="objectType" />
										<input type="hidden" name="id" value="${id}" />
										<input type="hidden" name="paramId" value="${parameter.id}" />
										<input type="hidden" name="position" value="${file.key}" />

										<ui:button type="del" styleClass="btn-small" onclick="
											$$.ajax.post(this.form, {control: this}).done(() => {
												$$.ajax.load('${form.requestUrl}', $('#${tableId}').parent());
											})"/>
									</html:form>
								</c:if>

								<c:url var="url" value="/user/file.do">
									<c:param name="id" value="${value.id}" />
									<c:param name="title" value="${value.title}" />
									<c:param name="secret" value="${value.secret}" />
								</c:url>
								<a href="${url}" ${args} target="_blank" class="preview">${value.title}</a>
							</div>
						</c:forEach>
						<script>
							$(function (){
								$('#${viewDivId} .preview').preview();
							});
						</script>

						<c:if test="${(not empty multiple or empty item.value) and not readonly}">
							<c:url var="uploadUrl" value="/user/parameter.do">
								<c:param name="action" value="parameterUpdate" />
								<c:param name="id" value="${id}" />
								<c:param name="paramId" value="${parameter.id}" />
							</c:url>

							<c:set var="uploadFormId" value="${u:uiid()}" />

							<div style="white-space:nowrap">
								<form id="${uploadFormId}" action="/user/parameter.do" method="POST" enctype="multipart/form-data" name="form">
									<input type="hidden" name="action" value="parameterUpdate" />
									<input type="hidden" name="responseType" value="json" />
									<input type="hidden" name="id" value="${id}" />
									<input type="hidden" name="paramId" value="${parameter.id}" />

									<ui:button type="add" styleClass="btn-small" onclick="$$.ajax.triggerUpload('${uploadFormId}');"/>
									<input type="file" name="file" style="visibility:hidden;"/>
								</form>
							</div>

							<script>
								$$.ajax.upload('${uploadFormId}', 'param-file-upload', function () {
									$$.ajax.load('${form.requestUrl}', $('#${tableId}').parent());
								});
							</script>
						</c:if>
					</c:when>

					<c:when test="${'email' eq parameter.type}">
						<c:url var="getUrl" value="/user/parameter.do">
							<c:param name="action" value="parameterGet"/>
							<c:param name="id" value="${id}"/>
							<c:param name="paramId" value="${parameter.id}"/>
						</c:url>

						<c:forEach var="email" items="${item.value}">
							<c:set var="position" value="${email.key}"/>
							<c:set var="value" value="${email.value}"/>

							<c:choose>
								<c:when test="${not readonly}">
									<html:form action="/user/parameter" style="display:inline;">
										<input type="hidden" name="action" value="parameterUpdate"/>
										<html:hidden property="objectType"/>
										<input type="hidden" name="id" value="${id}"/>
										<input type="hidden" name="paramId" value="${parameter.id}"/>
										<input type="hidden" name="position" value="${position}"/>

										<ui:button type="del" styleClass="btn-small"
											onclick="$$.ajax.post(this.form).done(() => { $$.ajax.load('${form.requestUrl}', $('#${tableId}').parent()) })"/>
									</html:form>

									<c:set var="editFormId" value="${u:uiid()}"/>
									<html:form action="/user/parameter" styleId="${editFormId}" style="display: inline;">
										<input type="hidden" name="action" value="parameterGet"/>
										<html:hidden property="objectType"/>
										<input type="hidden" name="id" value="${id}"/>
										<input type="hidden" name="paramId" value="${parameter.id}"/>
										<input type="hidden" name="position" value="${position}"/>
										<input type="hidden" name="returnUrl" value="${form.requestUrl}"/>
										<input type="hidden" name="tableId" value="${tableId}"/>

										<a href="#" onclick="$$.ajax.load($('#${editFormId}'), $('#${editDivId}')).done(() => { ${startEdit} }); return false;">
											${value}
										</a>
									</html:form>
								</c:when>
								<c:otherwise>${value}</c:otherwise>
							</c:choose>
							<br/>
						</c:forEach>

						<c:if test="${(not empty multiple or empty item.value) and not readonly}">
							<%-- adding new --%>
							<html:form action="/user/parameter" style="display: inline;">
								<input type="hidden" name="action" value="parameterGet"/>
								<html:hidden property="objectType"/>
								<input type="hidden" name="id" value="${id}"/>
								<input type="hidden" name="position" value="-1"/>
								<input type="hidden" name="returnUrl" value="${form.requestUrl}"/>
								<input type="hidden" name="tableId" value="${tableId}"/>
								<input type="hidden" name="paramId" value="${parameter.id}"/>

								<ui:button type="add" styleClass="btn-small"
									onclick="$$.ajax.load(this.form, $('#${editDivId}')).done(() => { ${startEdit} })"/>
							</html:form>
						</c:if>
					</c:when>

					<c:when test="${'address' eq parameter.type}">
						<c:forEach var="addr" items="${item.value}" varStatus="status">
							<c:set var="position" value="${addr.key}"/>
							<c:set var="value" value="${addr.value}"/>

							<c:choose>
								<c:when test="${not readonly}">
									<html:form action="/user/parameter" style="display: inline;">
										<input type="hidden" name="action" value="parameterUpdate"/>
										<html:hidden property="objectType"/>
										<input type="hidden" name="id" value="${id}"/>
										<input type="hidden" name="paramId" value="${parameter.id}"/>
										<input type="hidden" name="position" value="${position}"/>

										<ui:button type="del" styleClass="btn-small" onclick="
											$$.ajax.post(this.form).done(() => {
												$$.ajax.load('${form.requestUrl}', $('#${tableId}').parent());
											})
										"/>
									</html:form>

									<c:set var="editFormId" value="${u:uiid()}"/>
									<html:form action="/user/parameter" styleId="${editFormId}" style="display: inline;">
										<input type="hidden" name="action" value="parameterGet"/>
										<html:hidden property="objectType"/>
										<input type="hidden" name="id" value="${id}"/>
										<input type="hidden" name="paramId" value="${parameter.id}"/>
										<input type="hidden" name="position" value="${position}"/>
										<input type="hidden" name="returnUrl" value="${form.requestUrl}"/>
										<input type="hidden" name="tableId" value="${tableId}"/>

										<a href="#" onclick="$$.ajax.load($('#${editFormId}'), $('#${editDivId}')).done(() => { ${startEdit} }); return false;">
											${value.value}
										</a>
									</html:form>
								</c:when>
								<c:otherwise>${value.value}</c:otherwise>
							</c:choose>

							<p:check action="ru.bgcrm.struts.action.DirectoryAddressAction:addressGet">
								<c:url var="url" value="/user/directory/address.do">
									<c:param name="action" value="addressGet"/>
									<c:param name="addressHouseId" value="${value.houseId}"/>
									<c:param name="hideLeftPanel" value="1"/>
									<c:param name="returnUrl" value="${form.requestUrl}"/>
								</c:url>
								<div style="display: inline;">[<a href="#" onclick="$$.ajax.load('${url}', $('#${tableId}').parent()); return false;">${l.l('дом')}</a>]</div>
							</p:check>
						</c:forEach>

						<c:if test="${(not empty multiple or empty item.value) and not readonly}">
							<%-- adding --%>
							<c:set var="editFormId" value="${u:uiid()}"/>

							<html:form action="/user/parameter" styleId="${editFormId}"  style="display: inline;">
								<input type="hidden" name="action" value="parameterGet"/>
								<html:hidden property="objectType"/>
								<input type="hidden" name="id" value="${id}"/>
								<input type="hidden" name="position" value="0"/>
								<input type="hidden" name="returnUrl" value="${form.requestUrl}"/>
								<input type="hidden" name="tableId" value="${tableId}"/>
								<input type="hidden" name="paramId" value="${parameter.id}"/>

								<ui:button type="add" styleClass="btn-small" onclick="
									$$.ajax.load($('#${editFormId}'), $('#${editDivId}')).done(() => {
										${startEdit}
									})
								"/>
							</html:form>
						</c:if>
					</c:when>

					<%-- список, дерево, телефон - редактор нужно вызвать --%>
					<c:when test="${fn:contains( 'email, text, blob, date, datetime, list, phone, tree, listcount', parameter.type ) and empty editorType}">
						<c:set var="editFormId" value="${u:uiid()}"/>
						<c:set var="valueTitle" value="${item.valueTitle}"/>

						<c:if test="${fn:contains( 'date, datetime', parameter.type) }">
							<c:set var="type" value="${u:maskEmpty(parameter.configMap.type, 'ymd')}"/>
							<c:set var="valueTitle" value="${tu.format(item.value, type )}"/>
						</c:if>

						<c:if test="${parameter.type eq 'blob'}">
							<c:set var="valueTitle" value="<pre>${item.valueTitle}</pre>"/>
						</c:if>

						<c:set var="showAsLink" value="${parameter.configMap.showAsLink eq '1' and not empty item.value}"/>
						<c:choose>
							<c:when test="${readonly}">
								<c:if test="${showAsLink}"><a target="_blank" href="${item.value}"></c:if>
								${valueTitle}
								<c:if test="${showAsLink}"></a></c:if>
							</c:when>
							<c:otherwise>
								<html:form action="/user/parameter" styleId="1"><input type="hidden" value="1"/></html:form>

								<html:form action="/user/parameter" styleId="${editFormId}">
									<html:hidden property="objectType"/>
									<input type="hidden" name="id" value="${id}"/>
									<input type="hidden" name="action" value="parameterGet"/>
									<input type="hidden" name="returnUrl" value="${form.requestUrl}"/>
									<input type="hidden" name="tableId" value="${tableId}"/>
									<input type="hidden" name="paramId" value="${parameter.id}"/>

									<a href="#" onclick="if( openUrlTo( formUrl( $('#${editFormId}')[0] ), $('#${editDivId}') ) ) ${startEdit}">
										${valueTitle}
										<c:if test="${empty item.valueTitle}">${l.l('не указан')}</c:if>
										<c:if test="${showAsLink}">
											[<a target="_blank" href="${item.value}">перейти</a>]
										</c:if>
									</a>
								</html:form>
							</c:otherwise>
						</c:choose>
					</c:when>

					<%-- редактор сразу здесь --%>
					<c:otherwise>
						<c:set var="editFormId" value="${u:uiid()}"/>

						<c:if test="${parameter.configMap['encrypt'] eq 'encrypted'}">
							<c:set var="confirmEncryptedParam" value="if( !confirm( 'Вы действительно хотите записать значение \n'+ this.value + '\n в параметр \n'+'${parameter.title}' ) ) { return false; }"/>
						</c:if>

						<c:choose>
							<c:when test="${parameter.configMap['onErrorChangeParamsReload'] eq '1'}">
								<c:set var="saveCommand">${confirmEncryptedParam} sendAJAXCommand( formUrl( $('#${editFormId}')[0] ), ['value'] ); openUrlToParent( '${form.requestUrl}', $('#${tableId}') );</c:set>
							</c:when>
							<c:otherwise>
								<c:set var="saveCommand">${confirmEncryptedParam} if( sendAJAXCommand( formUrl( $('#${editFormId}')[0] ), ['value'] ) ){ openUrlToParent( '${form.requestUrl}', $('#${tableId}') ); return true; }</c:set>
							</c:otherwise>
						</c:choose>

						<c:set var="saveOn" value="${u:maskEmpty(parameter.configMap.saveOn, 'editor')}"/>
						<%-- для параметров типа date, datetime --%>
						<c:set var="editable" value="${parameter.configMap.editable}"/>

						<c:set var="onBlur" value=""/>
						<c:set var="onEnter" value=""/>
						<c:choose>
							<c:when test="${saveOn eq 'focusLost'}">
								<c:set var="onBlur">onBlur="if( $(this).attr( 'changed' ) == '1' ){ ${saveCommand} }"</c:set>
							</c:when>
							<c:when test="${saveOn eq 'enter'}">
								<c:set var="onEnter">onkeypress=" if( enterPressed( event ) ){ ${saveCommand} }" </c:set>
							</c:when>
							<c:when test="${saveOn eq 'editor'}">

							</c:when>
						</c:choose>

						<html:form action="/user/parameter" styleId="${editFormId}" style="width: 100%; text-align: left;" onsubmit="return false;">
							<input type="hidden" name="id" value="${id}"/>
							<html:hidden property="action" value="parameterUpdate"/>
							<html:hidden property="paramId" value="${parameter.id}"/>

							<%-- для параметров date, datetime --%>
							<c:set var="selector">#${editFormId} input[name='value']</c:set>

							<c:set var="changeAttrs">
								${onEnter}
								onchange="$(this).attr( 'changed', '1')" ${onBlur}
							</c:set>

							<c:choose>
								<%-- Выбор редактора спискового типа параметра --%>
								<c:when test="${parameter.type eq 'list'}">
									<c:choose>
										<%-- radio button редактор --%>
										<c:when test="${radioSelect}">
											<input type="radio" name="value" value="-1" ${u:checkedFromBool( empty item.value )} onclick="${saveCommand}"/>
											${radioSelectNotChoosed}
											<c:forEach var="valueItem" items="${parameter.listParamValues}">
												<c:if test="${fn:startsWith(valueItem.title, '@')==false}">
													<input type="radio" name="value" value="${valueItem.id}" ${u:checkedFromCollection( item.value, valueItem )} onclick="${saveCommand}"/>
													${valueItem.title}
												</c:if>
											</c:forEach>
										</c:when>

										<%-- select редактор --%>
										<c:when test="${editorType eq 'select'}">
											<c:set var="editorEmptyTextParameterName">param.${parameter.id}.editorEmptyText</c:set>
											<c:set var="editorEmptyText">${paramsConfig[editorEmptyTextParameterName]}</c:set>

											<c:if test="${empty editorEmptyText}">
												<c:set var="editorEmptyText">Выберите значение</c:set>
											</c:if>

											<select name="value" style="width: 100%; margin: 0px;" onchange="${saveCommand}">
												<option value="-1">${editorEmptyText}</option>

												<c:forEach var="valueItem" items="${parameter.listParamValues}">
													<c:set var="isSelected" value=""/>
													<c:if test="${not empty u:checkedFromCollection( item.value, valueItem )}">
														<c:set var="isSelected" value="selected"/>
													</c:if>
													<c:if test="${not fn:startsWith( valueItem.title, '@' )}">
														<option value="${valueItem.id}" ${isSelected}>${valueItem.title}</option>
													</c:if>
												</c:forEach>
											</select>
										</c:when>
									</c:choose>
								</c:when>
							</c:choose>
						</html:form>
						<c:if test="${parameter.type eq 'blob' and not readonly}">
							<div style="width: 100%; text-align: right;">
								<input type="button" value="Сохранить" onclick="${saveCommand}"/>
							</div>
						</c:if>
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
		<tr style="display: none;">
			<%-- сюда динамически загружается редактор --%>
			<td colspan="${editColspan}" id="${editDivId}"></td>
		</tr>
	</c:forEach>
</table>
