<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${empty tableId}">
	<c:set var="tableId" value="${u:uiid()}"/>
</c:if>

<c:set var="showTr">${empty onlyData}</c:set>
<%-- parameter IDs are hidden in Open interface --%>
<c:set var="showId" value="${form.param.showId ne '0'}"/>

<c:url var="paramLogUrl" value="/user/parameter.do">
	<c:param name="method" value="parameterLog"></c:param>
	<c:param name="id" value="${id}"></c:param>
	<c:param name="objectType" value="${form.param.objectType}"></c:param>
	<c:param name="returnUrl" value="${form.requestUrl}"></c:param>
</c:url>

<c:if test="${not empty form.param.header}">
	<div class="mt1 mb05">
		<h2>${form.param.header}
			<span class="normal"> [<a href="#" onclick="$$.ajax.load('${paramLogUrl}', $('#${tableId}').parent()); return false;">${l.l('log')}</a>]</span>
		</h2>
	</div>
</c:if>

<table id="${tableId}" class="hdata">
	<c:if test="${showTr}">
		<tr class="header">
			<td>${l.l('Название')}</td>
			<td width="100%">${l.l('Value')}</td>
		</tr>
	</c:if>
	<c:forEach var="item" items="${list}">
		<c:set var="parameter" value="${item.parameter}"/>

		<%-- TODO: Check permission for parameter update. --%>
		<c:set var="readonly" value="${parameter.readonly or form.param.readOnly eq '1'}"/>

		<c:set var="multiple" value="${parameter.configMap.multiple eq '1'}" />

		<c:set var="style" value="${parameter.configMap.style}"/>

		<%-- флаг readonly выставлен для параметра в конфигурации типа процесса --%>
		<c:if test="${not empty form.param['processTypeId']}">
			<c:set var="processTypeConfig" value="${ctxProcessTypeMap[u:int(form.param['processTypeId'])].properties.configMap}"/>
			<c:if test="${u.toIntegerSet(processTypeConfig['readonlyParamIds']).contains(parameter.id)}">
				<c:set var="readonly" value="true"/>
			</c:if>
		</c:if>

		<c:set var="viewDivId" value="${u:uiid()}"/>
		<c:set var="editDivId" value="${u:uiid()}"/>

		<c:set var="startEdit">{  $('#${viewDivId}').hide(); $('#${editDivId}').parent().show(); }; return false;</c:set>

		<tr ${hide} id="${viewDivId}" style="${style}">
			<td width="50%" title="${showId ? ui.idAndComment(parameter) : ""}">${parameter.title}</td>
			<td width="50%">
				<c:choose>
					<c:when test="${'address' eq parameter.type}">
						<c:forEach var="addr" items="${item.value}" varStatus="status">
							<c:set var="position" value="${addr.key}"/>
							<c:set var="value" value="${addr.value}"/>

							<c:choose>
								<c:when test="${not readonly}">
									<html:form action="/user/parameter" style="display: inline;">
										<input type="hidden" name="method" value="parameterUpdate"/>
										<html:hidden property="objectType"/>
										<input type="hidden" name="id" value="${id}"/>
										<input type="hidden" name="paramId" value="${parameter.id}"/>
										<input type="hidden" name="position" value="${position}"/>

										<ui:button type="del" styleClass="btn-small" onclick="
											$$.ajax.post(this).done(() => {
												$$.ajax.load('${form.requestUrl}', $('#${tableId}').parent());
											})
										"/>
									</html:form>

									<c:set var="editFormId" value="${u:uiid()}"/>
									<html:form action="/user/parameter" styleId="${editFormId}" style="display: inline;">
										<input type="hidden" name="method" value="parameterGet"/>
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
									<c:param name="method" value="addressGet"/>
									<c:param name="addressHouseId" value="${value.houseId}"/>
									<c:param name="hideLeftPanel" value="1"/>
									<c:param name="returnUrl" value="${form.requestUrl}"/>
								</c:url>
								<div style="display: inline;">[<a href="#" onclick="$$.ajax.load('${url}', $('#${tableId}').parent()); return false;">${l.l('house')}</a>]</div>
							</p:check>
						</c:forEach>

						<%-- adding --%>
						<c:if test="${(multiple or empty item.value) and not readonly}">
							<c:set var="editFormId" value="${u:uiid()}"/>

							<html:form action="/user/parameter" styleId="${editFormId}">
								<input type="hidden" name="method" value="parameterGet"/>
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

					<c:when test="${'file' eq parameter.type}">
						<c:forEach var="file" items="${item.value}" varStatus="status">
							<c:set var="value" value="${file.value}"/>

							<div>
								<c:if test="${not readonly}">
									<html:form action="/user/parameter" styleId="${editFormId}" style="display: inline;">
										<input type="hidden" name="method" value="parameterUpdate" />
										<html:hidden property="objectType" />
										<input type="hidden" name="id" value="${id}" />
										<input type="hidden" name="paramId" value="${parameter.id}" />
										<input type="hidden" name="position" value="${file.key}" />

										<ui:button type="del" styleClass="btn-small" onclick="
											$$.ajax.post(this).done(() => {
												$$.ajax.load('${form.requestUrl}', $('#${tableId}').parent());
											})"/>
									</html:form>
								</c:if>

								<ui:file-link file="${value}"/>
							</div>
						</c:forEach>
						<script>
							$(function () {
								$('#${viewDivId} .preview').preview();
							});
						</script>

						<%-- adding --%>
						<c:if test="${(multiple or empty item.value) and not readonly}">
							<c:url var="uploadUrl" value="/user/parameter.do">
								<c:param name="method" value="parameterUpdate" />
								<c:param name="id" value="${id}" />
								<c:param name="paramId" value="${parameter.id}" />
							</c:url>

							<div style="white-space: nowrap;">
								<form action="/user/parameter.do" method="POST" enctype="multipart/form-data">
									<input type="hidden" name="method" value="parameterUpdate" />
									<input type="hidden" name="responseType" value="json" />
									<input type="hidden" name="id" value="${id}" />
									<input type="hidden" name="paramId" value="${parameter.id}" />

									<ui:button type="add" styleClass="btn-small" onclick="$$.ajax.fileUpload(this.form).done(() => {
										$$.ajax.load('${form.requestUrl}', $('#${tableId}').parent());
									});"/>
									<input type="file" name="file" ${multiple and empty item.value ? "multiple='true'" : ""} style="visibility: hidden; width: 1px; height: 0;"/>
								</form>
							</div>
						</c:if>
					</c:when>

					<%-- editor has to be called --%>
					<c:when test="${'blob, date, datetime, email, list, listcount, money, phone, text, tree, treecount'.contains(parameter.type)}">
						<c:set var="editFormId" value="${u:uiid()}"/>
						<c:set var="valueTitle" value="${item.valueTitle}"/>

						<c:choose>
							<c:when test="${parameter.type eq 'blob' and not empty valueTitle}">
								<c:set var="valueTitle" value="<pre>${u.escapeXml(valueTitle)}</pre>"/>
							</c:when>
							<c:when test="${'date, datetime'.contains(parameter.type)}">
								<c:set var="type" value="${u.maskEmpty(parameter.configMap.type, 'ymd')}"/>
								<c:set var="valueTitle" value="${tu.format(item.value, type )}"/>
							</c:when>
							<c:when test="${parameter.type eq 'email'}">
								<c:set var="valueTitle" value="${u.escapeXml(valueTitle)}"/>
							</c:when>
						</c:choose>

						<c:set var="showAsLink" value="${parameter.type eq 'text' and not empty parameter.showAsLink and not empty item.value}"/>
						<c:choose>
							<c:when test="${readonly}">
								<c:if test="${showAsLink}"><a target="_blank" href="${item.value}"></c:if>
								${valueTitle}
								<c:if test="${showAsLink}"></a></c:if>
							</c:when>
							<c:otherwise>
								<html:form action="/user/parameter" styleId="1"><input type="hidden" value="1"/></html:form>

								<u:sc>
									<c:set var="paramLinkUiid" value="${u:uiid()}"/>

									<html:form action="/user/parameter" styleId="${editFormId}">
										<html:hidden property="objectType"/>
										<input type="hidden" name="id" value="${id}"/>
										<input type="hidden" name="method" value="parameterGet"/>
										<input type="hidden" name="returnUrl" value="${form.requestUrl}"/>
										<input type="hidden" name="tableId" value="${tableId}"/>
										<input type="hidden" name="paramId" value="${parameter.id}"/>

										<a id="${paramLinkUiid}" href="#" onclick="$$.ajax.load($('#${editFormId}'), $('#${editDivId}')).done(() => { ${startEdit} }); return false;">
											<c:choose>
												<c:when test="${empty valueTitle}">${l.l('undefined')}</c:when>
												<c:otherwise>${valueTitle}</c:otherwise>
											</c:choose>
											<c:if test="${showAsLink}">
												[<a target="_blank" href="${item.value}">${l.l('link.open')}</a>]
											</c:if>
										</a>
									</html:form>

									<c:set var="paramValue" value="${item}" scope="request"/>
									<c:set var="menuItems">
										<plugin:include endpoint="<%=ru.bgcrm.plugin.Endpoint.USER_PARAM_MENU_ITEMS%>"/>
									</c:set>

									<c:if test="${not empty menuItems}">
										<c:set var="uiid" value="${u:uiid()}"/>
										<ui:popup-menu id="${uiid}">${menuItems}</ui:popup-menu>

										<script>$$.param.menuInit('${paramLinkUiid}', '${uiid}');</script>
									</c:if>
								</u:sc>
							</c:otherwise>
						</c:choose>
					</c:when>
				</c:choose>
			</td>
		</tr>
		<tr style="display: none;">
			<%-- here is loaded editor --%>
			<td colspan="2" id="${editDivId}"></td>
		</tr>
	</c:forEach>
</table>
