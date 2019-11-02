<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="requestUserId" value="${form.param.userId}" />
<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="createUrl" value="news.do">	
	<c:param name="action" value="newsEdit"></c:param>	
	<c:param name="requestUserId" value="${requestUserId}"></c:param>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
</c:url>

<div class="center1020">
	<c:choose>
		<c:when test="${empty requestUserId || requestUserId == form.userId}">
			<h2>${l.l('Свойства')}</h2>
			<c:import url="/user/profile.do?action=settings"/>
			
			<h2>${l.l('Параметры (сохраняются сразу)')}</h2>
			<c:import url="/user/profile.do?action=settings&subAction=parameters&requestUserId=${requestUserId}"/>
			
			<%-- значение по-умолчанию должно быть таким же как и при обращении к данной опции на чтение!!! --%>
			<h2>${l.l('Опции интерфейса')}</h2>
			
			<html:form action="/user/profile">
				<input type="hidden" name="action" value="updatePersonalization"/>
				
				<table class="data">
					<tr>
						<td>${l.l('Параметр')}</td>
						<td width="100%">${l.l('Значение')}</td>
					</tr>
					<%--
					<tr>
						<td nowrap="nowrap">
							Подсветка выбранной строки в очереди процессов
						</td>
						<td>	
							<u:sc>
								<c:set var="valuesHtml">
									<li value="1">Да</li>
									<li value="0">Нет</li>
								</c:set>
								<c:set var="key" value="iface.processQueue.rowHighlight"/>
								<c:set var="hiddenName" value="${key.replace( '.', '_' )}"/>
								<c:set var="value" value="${u:getFromPers( ctxUser, key, '1' )}"/>
								<c:set var="widthTextValue" value="50px"/>
								<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
							</u:sc>
						</td>
					</tr>
										 
					<tr>
						<td nowrap="nowrap">
							Открытие процесса по клику в произвольное место строки очереди
						</td>
						<td>
							<u:sc>
								<c:set var="valuesHtml">
									<li value="1">Да</li>
									<li value="0">Нет</li>
								</c:set>
								<c:set var="key" value="iface.processQueue.openOnClick"/>
								<c:set var="hiddenName" value="${key.replace( '.', '_' )}"/>
								<c:set var="value" value="${u:getFromPers( ctxUser, key, '1' )}"/>
								<c:set var="widthTextValue" value="50px"/>
								<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
							</u:sc>
						</td>
					</tr>
					--%>
					
					<tr>
						<td nowrap="nowrap">
							${l.l('Открытие буфера объектов по долгому нажатию ЛКМ')}
						</td>
						<td>
							<%--
							<u:sc>
								<c:set var="valuesHtml">
									<li value="1">Да</li>
									<li value="0">Нет</li>
								</c:set>
								<c:set var="key" value="iface.buffer.openOnLongPress"/>
								<c:set var="hiddenName" value="${key.replace( '.', '_' )}"/>
								<c:set var="value" value="${u:getFromPers( ctxUser, key, '1' )}"/>
								<c:set var="widthTextValue" value="50px"/>
								<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
							</u:sc>
							--%>
							<c:set var="key" value="iface.buffer.openOnLongPress"/>
							<ui:combo-single hiddenName="${key.replace( '.', '_' )}" value="${u:getFromPers(ctxUser, key, '1')}" widthTextValue="50px">
								<jsp:attribute name="valuesHtml">
									<li value="1">${l.l('Да')}</li>
									<li value="0">${l.l('Нет')}</li>
								</jsp:attribute>
							</ui:combo-single>
						</td>
					</tr>
					
					<tr>
						<td nowrap="nowrap">
							${l.l('Порядок объектов в буфере')}
						</td>
						<td>
							<%--
							<u:sc>
								<c:set var="valuesHtml">
									<li value="1">Последний объект сверху</li>
									<li value="2">Сохранять порядок объектов</li>
								</c:set>
								<c:set var="key" value="iface.buffer.behavior"/>
								<c:set var="hiddenName" value="${key.replace( '.', '_' )}"/>
								<c:set var="value" value="${u:getFromPers( ctxUser, key, '1' )}"/>
								<c:set var="widthTextValue" value="200px"/>
								<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
							</u:sc>
							--%>
							<c:set var="key" value="iface.buffer.behavior"/>
							<ui:combo-single hiddenName="${key.replace( '.', '_' )}" value="${u:getFromPers(ctxUser, key, '1')}" widthTextValue="200px">
								<jsp:attribute name="valuesHtml">
									<li value="1">${l.l('Последний объект сверху')}</li>
									<li value="2">${l.l('Сохранять порядок объектов')}</li>
								</jsp:attribute>
							</ui:combo-single>
						</td>
					</tr>
					
					<tr>
						<td nowrap="nowrap">
							${l.l('Максимальное число объектов в буфере')}
						</td>
						<td>
							<c:set var="key" value="iface.buffer.maxObjects"/>
							<c:set var="inputName" value="${key.replace( '.', '_' )}"/>
							<c:set var="value" value="${u:getFromPers( ctxUser, key, '15' )}"/>
								
							<input type="text" name="${inputName}" value="${value}" size="10"/>
						</td>
					</tr>
					
					<%--
					<tr>
						<td nowrap="nowrap">
							Правка описания процесса по клику мыши
						</td>
						<td>
							<u:sc>
								<c:set var="valuesHtml">
									<li value="1">Да</li>
									<li value="0">Нет</li>
								</c:set>
								<c:set var="key" value="iface.process.editDescriptionOnClick"/>
								<c:set var="hiddenName" value="${key.replace( '.', '_' )}"/>
								<c:set var="value" value="${u:getFromPers( ctxUser, key, '1' )}"/>
								<c:set var="widthTextValue" value="50px"/>
								<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
							</u:sc>
						</td>
					</tr>					
					
					<tr>
						<td nowrap="nowrap">
							Выбор строк таблицы на правку
						</td>
						<td>
							<u:sc>
								<c:set var="valuesHtml">
									<li value="1">Подсветка строки / клик</li>
									<li value="0">Кнопка со звёздочкой</li>
								</c:set>
								<c:set var="key" value="iface.table.rowHighlightClickEdit"/>
								<c:set var="hiddenName" value="${key.replace( '.', '_' )}"/>
								<c:set var="value" value="${u:getFromPers( ctxUser, key, '1' )}"/>
								<c:set var="widthTextValue" value="150px"/>
								<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
							</u:sc>
						</td>
					</tr>
					 --%>
					
					<c:set var="endpoint" value="user.profile.options.jsp"/>
					<%@ include file="/WEB-INF/jspf/plugin_include.jsp"%>
				</table>
				
				<c:set var="configTextUiid" value="${u:uiid()}"/>
				
				<div id="${configTextUiid}" style="display: none;">
					<h2>${l.l('Текст конфигурации опций')}</h2>
					
					<textarea style="width: 100%; height: 400px;">${ctxUser.personalizationMap.getDataString()}</textarea>
					<%--
					<button class="btn-grey mt1" type="button" 
						onclick="if (confirm('Перетереть все опции помимо выбранных?') && sendAJAXCommand(formUrl(this.form) + '&overwrite=1')) {alert('Сохранено, для применения изменений перегрузите интерфейс нажатием Ctrl+F5.')}">Сохранить опции перетерев прочую конфигурацию</button>
					--%>						
				</div> 	
					
				<button class="btn-grey mt1" type="button" 
					onclick="if( sendAJAXCommand( formUrl( this.form ) ) ){ alert( '${l.l('Сохранено, для применения изменений перегрузите интерфейс нажатием Ctrl+F5')}' ) }">${l.l('Сохранить опции')}</button>
				<button class="btn-white mt1 ml2" type="button"
					onclick="$('#${configTextUiid}').toggle()" title="${l.l('Показать текст конфигурации опций')}">${l.l('Текст')}</button>
			</html:form>
							
			<c:set var="state">
				<span>${l.l('Мой профиль')}</span>
			</c:set>
		</c:when>
		<c:otherwise>
			<div>
				<c:url var="url" value="/user/profile.do">
					<c:param name="action" value="settings"/>
					<c:param name="subAction" value="parameters"/>
					<c:param name="requestUserId" value="${requestUserId}"/>
				</c:url>
				<c:import url="${url}"/>
			</div>
			
			<button class="btn-grey mt1" type="button" onclick="openUrlContent( '${createUrl}' );">${l.l('Послать персональную новость')}</button>
					
			<c:set var="title">
				<span id='user_title_${requestUserId}' class='title'>${l.l('Профиль')}: ${ctxUserMap[u:int(requestUserId)].title}</span>
			</c:set>
			<c:set var="state"></c:set>
			
			<%-- 
				Убрано, т.к. при создание перс. новости в state выводится "Создание новости"	
			<script>
				$(function()
				{
					$('#title > .status:visible > .wrap > .left').attr( "style", "width: 100%; text-align: center;" );
				})
			</script> --%>
		</c:otherwise>
	</c:choose>
</div>

<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>

