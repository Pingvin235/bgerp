<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${empty tableId}">
	<c:set var="tableId" value="${u:uiid()}"/>
</c:if>	

<c:if test="${not empty onlyData}">
	<c:set var="hideTr">style="display: none;"</c:set>
</c:if>

<c:url var="paramLogUrl" value="../user/parameter.do">
	<c:param name="action" value="parameterLog"></c:param>
	<c:param name="id" value="${id}"></c:param>
	<c:param name="objectType" value="${form.param.objectType}"></c:param>
	<c:param name="returnUrl" value="${form.requestUrl}"></c:param>
</c:url> 

<c:if test="${not empty form.param.header}">
	<div class="mt1 mb05">
		<h2 style="display: inline;">${form.param.header}</h2> [<a href="#UNDEF" onclick="openUrlToParent( '${paramLogUrl}', $('#${tableId}') ); return false;">${l.l('лог изменений')}</a>]
	</div>
</c:if>

<table style="width: 100%;" id="${tableId}" class="data">
	<tr ${hideTr}>
		<td width="30">ID</td>
		<td>${l.l('Название')}</td>
		<td width="100%">${l.l('Значение')}</td>
	</tr>
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
				
		<%-- флаг readonly выставлен для параметра в конфигурации типа процесса --%>
		<c:if test="${not empty form.param['processTypeId']}">
			<c:set var="processTypeConfig" value="${ctxProcessTypeMap[u:int( form.param['processTypeId'] )].properties.configMap}"/>
			<c:if test="${u:contains( u:toIntegerSet( processTypeConfig['readonlyParamIds'] ), parameter.id )}">
				<c:set var="readonly" value="true"/>
			</c:if>
		</c:if>
		
		<c:if test="${form.param.globalReadOnly == '1'}">
			<c:set var="readonly" value="true"/>
		</c:if>
		
		<c:set var="hide" value=""/>
		<c:if test="${parameter.configMap.hide == '1'}">
			<c:set var="hide" value="style='display:none'"/>
		</c:if>
		
		<c:set var="viewDivId" value="${u:uiid()}"/>
		<c:set var="editDivId" value="${u:uiid()}"/>
		<c:set var="editColspan" value="2"/>
		
		<c:set var="startEdit">{  $('#${viewDivId}').hide(); $('#${editDivId}').parent().show(); }; return false;</c:set>
		
		<tr ${hide} id="${viewDivId}" title="${parameter.comment}">
			<c:if test="${empty onlyData}">
				<td>${parameter.id}</td>
				<c:set var="editColspan" value="3"/>
			</c:if>
			
			<td nowrap="nowrap">${parameter.title}</td>
			<td width="100%" style="padding: 2px;">
				<c:choose>
					<c:when test="${'file' eq parameter.type}">
						<c:set var="showForOwnerOnly"
							value="${parameter.configMap.showForOwnerOnly}" />
						<c:set var="showForOwnerGroupOnly"
							value="${parameter.configMap.showForOwnerGroupOnly}" />

						<c:if test="${parameter.configMap.showVersions}">
							<input type="button" value="Только последние/все версии" 
								onclick="$('div[overrided=1]').toggle(); $('div[version]').each( function() { var padding=parseInt($(this).css('padding-left'))>0?0:20*($(this).attr('version')-1);  $(this).css('padding-left',padding); } );" />
						</c:if>
						
						<c:forEach var="file" items="${item.value}" varStatus="status">
							<c:set var="value" value="${file.value}" />

							<c:if test="${(ctxUser.id eq value.user.id) or (showForOwnerOnly ne '1' ) }">
								<c:if test="${( not empty u:intersection(ctxUserMap[u:int(value.user.id)].groupIds, ctxUser.groupIds) ) or (showForOwnerGroupOnly ne '1' ) }">

									<c:set var="version" value="${value.version}" />
									<c:set var="position" value="${fn:substring(file.key,0, fn:length(file.key) -  fn:length(version.toString()) )}" />
									<c:set var="editFormId" value="${u:uiid()}" />

									<c:if test="${not status.last and item.value[u:concat(position,version+1 )] ne null and parameter.configMap.showVersions}">
										<c:set var="args" value="style='text-decoration:line-through'" />
										<c:set var="overrided" value="1" />
									</c:if>

									<div version="${value.version}" overrided="${overrided}">

										<c:if test="${not readonly and overrided ne '1'}">
											<html:form action="/user/parameter" styleId="${editFormId}"
												style="display: inline;">
												<input type="hidden" name="action" value="parameterUpdate" />
												<html:hidden property="objectType" />
												<input type="hidden" name="id" value="${id}" />
												<input type="hidden" name="paramId" value="${parameter.id}" />
												<input type="hidden" name="position" value="${position}" />
												<input type="hidden" name="version" value="${version}" />

												<c:set var="deleteCommand" value="formUrl( this.form )" />
												<c:set var="deleteAjaxCommandAfter">openUrlToParent( '${form.requestUrl}', $('#${tableId}') )</c:set>
												<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
											</html:form>
										</c:if>

										<c:url var="url" value="../user/file.do">
											<c:param name="id" value="${value.id}" />
											<c:param name="title" value="${value.title}" />
											<c:param name="secret" value="${value.secret}" />
										</c:url>
										<a href="${url}" ${args} target="_blank" class="preview">${value.title}</a>

										<%-- 
										<c:if test="${parameter.configMap.showFullInfo}">
											<c:if test="${ value.version > 0 }">
												</br>
												<span ${args}> <b>ВЕРСИЯ:</b> ${value.version}
												</span>
											</c:if>
											</br>

											<c:if test="${parameter.configMap.showOwner eq 1}">
												<span ${args}> <b>Владелец:</b> <c:set var="userId"
														value="${value.user.id}" /> <%@ include
														file="/WEB-INF/jspf/user_link.jsp"%>
												</span>
											</c:if>

											<c:if test="${parameter.configMap.showUploadDate eq 1}">
												</br>
												<span ${args}> <fmt:formatDate value="${value.time}"
														var="uploadDate" pattern="dd.MM.yyyy HH:mm:ss" /> <b>Дата
														изменения:</b> ${uploadDate} <c:remove var="uploadDate" />
												</span>
											</c:if>

											<c:if
												test="${parameter.configMap.showComment eq 1 and value.comment.length()>0}">
												</br>
												<span ${args}> <b>Комментарий:</b> ${value.comment}
												</span>
											</c:if>
										</c:if>
										--%>
										</br>
									</div>
									<c:remove var="args" />
									<c:remove var="overrided" />
								</c:if>
							</c:if>
						</c:forEach>
						<script>
							$(function (){
								$('#${viewDivId} .preview').preview();
							});
						</script>	 

						<c:if test="${(not empty multiple or empty item.value) and not readonly}">
							<c:url var="uploadUrl" value="../user/parameter.do">
								<c:param name="action" value="parameterUpdate" />
								<c:param name="id" value="${id}" />
								<c:param name="paramId" value="${parameter.id}" />
							</c:url>
							
							<c:set var="uploadFormId" value="${u:uiid()}" />

							<div style="white-space:nowrap">
								<form id="${uploadFormId}" action="../user/parameter.do" method="POST" enctype="multipart/form-data" name="form">
									<input type="hidden" name="action" value="parameterUpdate" />
									<input type="hidden" name="responseType" value="json" />
									<input type="hidden" name="id" value="${id}" /> 
									<input type="hidden" name="paramId" value="${parameter.id}" /> 
									<c:choose>
										<c:when test="${parameter.configMap.needComment}">
											<input type="file" name="file" style="visibility: block" />
											Комментарий<input type="text" name="comment" value="" />
											<input type="button" class="btn-white btn-small" value="Выгрузить" onclick="$(this.form).submit();" />
										</c:when>
										<c:otherwise>
											<input type="button" class="btn-white btn-small" value="+" onclick="$(this.form).find('input[name=file]').click();"/>
											<input type="file" name="file" onchange="$(this.form).submit();" style="visibility:hidden"/>
										</c:otherwise>
									</c:choose>
								</form>
							</div>

							<script>
								$(function()
								{
									$('#${uploadFormId}').iframePostForm
									({
										json : true,
										post : function()
										{
											var filePath = $('#${uploadFormId} input[type=file]').val();
											var fileName = filePath.substr(filePath.lastIndexOf('\\') + 1);
											
											if( fileName.length == 0 )
											{
												alert( "Не выбран файл!" );
												return false;
											}
											if($('#${uploadFormId} input[name=comment]').size > 0)
											{
												if($('#${uploadFormId} input[name=comment]').val().length == 0 && $('a:contains(' + fileName + ')').length != 0 )
												{
													alert( "Отсутствует комментарий" );
													return false;
												}
											}
										},
										complete : function( response )
										{
											openUrlToParent( '${form.requestUrl}', $('#${tableId}') )
											//openProcess('${id}');
										}
									});									
								});
								$('div[overrided=1]').toggle();								
							</script>
						</c:if>
					</c:when>
					
					<c:when test="${'email' eq parameter.type}">
						<c:url var="getUrl" value="../user/parameter.do">
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
										
										<c:set var="deleteCommand" value="formUrl( this.form )"/>
										<c:set var="deleteAjaxCommandAfter">openUrlToParent( '${form.requestUrl}', $('#${tableId}') )</c:set>		
										<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
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
													
										<a href="#UNDEF" onclick="if( openUrlTo( formUrl( $('#${editFormId}')[0] ), $('#${editDivId}') ) ) ${startEdit}">
											${value}
										</a>
									</html:form>
								</c:when>
								<c:otherwise>${value}</c:otherwise>
							</c:choose>		
							<br/>
						</c:forEach>
						
						<c:if test="${(not empty multiple or empty item.value) and not readonly}">
							<%-- добавить --%>
							<html:form action="/user/parameter" style="display: inline;">
								<input type="hidden" name="action" value="parameterGet"/>
								<html:hidden property="objectType"/>
								<input type="hidden" name="id" value="${id}"/>
								<input type="hidden" name="position" value="-1"/>									
								<input type="hidden" name="returnUrl" value="${form.requestUrl}"/>
								<input type="hidden" name="tableId" value="${tableId}"/>
								<input type="hidden" name="paramId" value="${parameter.id}"/>
									
								<c:set var="addCommand">if( openUrlTo( formUrl( this.form ), $('#${editDivId}') ) ) ${startEdit}</c:set>
								<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
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
										
										<c:set var="deleteCommand" value="formUrl( this.form )"/>
										<c:set var="deleteAjaxCommandAfter">openUrlToParent( '${form.requestUrl}', $('#${tableId}') )</c:set>		
										<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>							
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
													
										<a href="#UNDEF" onclick="if( openUrlTo( formUrl( $('#${editFormId}')[0] ), $('#${editDivId}') ) ) ${startEdit}">
											${value.value}
										</a>
									</html:form>
								</c:when>
								<c:otherwise>${value.value}</c:otherwise>
							</c:choose>
							
							<p:check action="ru.bgcrm.struts.action.DirectoryAddressAction:addressGet">
								<c:url var="url" value="directory/address.do">
									<c:param name="action" value="addressGet"/>
									<c:param name="addressHouseId" value="${value.houseId}"/>
									<c:param name="hideLeftPanel" value="1"/>
									<c:param name="returnUrl" value="${form.requestUrl}"/>
								</c:url>
								[<a href="#UNDEF" onclick="$$.ajax.load('${url}', $('#${tableId}').parent()); return false;">дом</a>]
								<%-- openUrlContent('?action=addressGet&returnUrl=%2fuser%2fdirectory%2faddress.do%3fselectTab%3dstreet%26addressItemId%3d3139%26searchMode%3dhouse&selectTab=street&addressCountryTitle=%d0%a0%d0%be%d1%81%d1%81%d0%b8%d1%8f&addressCityTitle=%d0%b3.+%d0%a3%d1%84%d0%b0&addressItemTitle=!%d0%a1%d0%a2%d0%a0%d0%9e%d0%98%d0%a2&addressCityId=1&addressItemId=3139&addressHouseId=29556  --%>
							</p:check>
							
							<br/>
						</c:forEach>
						
						<c:if test="${(not empty multiple or empty item.value) and not readonly}">
							<%-- добавить --%>
							<c:set var="editFormId" value="${u:uiid()}"/>
							
							<html:form action="/user/parameter" styleId="${editFormId}"  style="display: inline;">
								<input type="hidden" name="action" value="parameterGet"/>
								<html:hidden property="objectType"/>
								<input type="hidden" name="id" value="${id}"/>
								<input type="hidden" name="position" value="0"/>									
								<input type="hidden" name="returnUrl" value="${form.requestUrl}"/>
								<input type="hidden" name="tableId" value="${tableId}"/>
								<input type="hidden" name="paramId" value="${parameter.id}"/>
									
								<c:set var="addCommand">if( openUrlTo( formUrl( $('#${editFormId}')[0] ), $('#${editDivId}') ) ) ${startEdit}</c:set>
								<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
							</html:form>
						</c:if>	
					</c:when>
					
					<%-- список, дерево, телефон - редактор нужно вызвать --%>
					<c:when test="${fn:contains( 'email, text, blob, date, datetime, list, phone, tree, listcount', parameter.type ) and empty editorType}">
						<c:set var="editFormId" value="${u:uiid()}"/>
						<c:set var="valueTitle" value="${item.valueTitle}"/>
						
						<c:if test="${fn:contains( 'date, datetime', parameter.type) }">
							<c:set var="type" value="${u:maskEmpty(parameter.configMap.type, 'ymd')}"/>
							<c:set var="valueTitle" value="${u:formatDate(item.value, type )}"/>
						</c:if>

						<c:if test="${parameter.type eq 'blob'}">
							<c:set var="valueTitle" value="<pre>${item.valueTitle}</pre>"/>
						</c:if>

						<c:set var="goToLink">
							<c:if test="${parameter.configMap.showAsLink eq '1' and not empty item.value}">
								[<a target="_blank" href="${item.value}">перейти</a>]
							</c:if>
						</c:set>	
						
						<c:choose>
							<c:when test="${not readonly}">
								<html:form action="/user/parameter" styleId="1"><input type="hidden" value="1"/></html:form>
								
								<html:form action="/user/parameter" styleId="${editFormId}">
									<html:hidden property="objectType"/>
									<input type="hidden" name="id" value="${id}"/>
									<input type="hidden" name="action" value="parameterGet"/>
									<input type="hidden" name="returnUrl" value="${form.requestUrl}"/>
									<input type="hidden" name="tableId" value="${tableId}"/>
									<input type="hidden" name="paramId" value="${parameter.id}"/>
												
									<a href="#UNDEF" onclick="if( openUrlTo( formUrl( $('#${editFormId}')[0] ), $('#${editDivId}') ) ) ${startEdit}">
										${valueTitle}
										<c:if test="${empty item.valueTitle}">${l.l('не указан')}</c:if>
										${goToLink}
									</a>
									<%--
									<a href="#UNDEF" onclick="bgcrm.buffer.storeParam( ${id}, ${parameter.id}, '${parameter.type}' )" title="Скопировать в буфер"><b>[B]</b></a>
									 --%>
								</html:form>
							</c:when>
							
							<c:otherwise>
								${valueTitle}
								<c:if test="${empty item.valueTitle}">${l.l('не указан')}</c:if>
								${goToLink}
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
