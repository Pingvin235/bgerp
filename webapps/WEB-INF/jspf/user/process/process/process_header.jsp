<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:sc>
	<c:set var="priority" value="${process.priority}"/>
	<%@ include file="/WEB-INF/jspf/process_color.jsp"%>

	<c:set var="uiid" value="${u:uiid()}"/>
	<div style="background-color: ${color}" class="pl05 pr05 box">
		<div id="${uiid}" class="pt05 pb05">
			<ui:when type="user">
				<p:check action="ru.bgcrm.struts.action.ProcessAction:processDelete">
					<c:set var="uiidDelMenu" value="${u:uiid()}"/>
					
					<div style="max-height: 0px;">
						<ul id="${uiidDelMenu}" style="display: none;">
							<c:url var="url" value="/user/process.do">
								<c:param name="action" value="processDelete"/>
								<c:param name="id" value="${process.id}"/>
							</c:url>
							<li><a href="#" 
								onclick="if (confirm('${l.l('Удалить процесс')}?')) $$.ajax.post('${url}').done(() => { ${returnBreakCommand} }); return false;">${l.l('Удалить процесс')}</a>
							</li>
							
							<c:url var="url" value="/user/empty.do">
								<c:param name="returnUrl" value="${requestUrl}"/>
								<c:param name="returnChildUiid" value="${tableId}"/>
								<c:param name="id" value="${process.id}"/>
								<c:param name="forwardFile" value="/WEB-INF/jspf/user/process/process/editor_merge.jsp"/>
							</c:url>
							<li><a href="#" 
								onclick="$$.ajax.load('${url}', $('#${uiid}')); return false;">${l.l('Слить в существующий')}</a>
							</li>
						</ul>
					</div>

					<c:set var="uiidDelMenuLink" value="${u:uiid()}"/>
					[<a href="#" id="${uiidDelMenuLink}"> X </a>]
					<script>
						$(() => {
							$$.ui.menuInit($("#${uiidDelMenuLink}"), $("#${uiidDelMenu}"), "left");
						})
					</script>
				</p:check>
			</ui:when>	

			<b>
				<u:sc>
					<c:set var="processType" value="${ctxProcessTypeMap[process.typeId]}"/>
					<c:choose>
						<c:when test="${not empty processType}">
							<c:forEach var="item" items="${processType.path}" varStatus="status">
								<c:if test="${status.index ne 0}"> -> </c:if>
									${item.title}
							</c:forEach>
						</c:when>
						<c:otherwise>
							${l.l('Данный тип процесса был удален')} (${process.typeId})
						</c:otherwise>
					</c:choose>
				</u:sc>
			</b>
			(<ui:process-link id="${process.id}"/>)

			<ui:when type="user">
				<p:check action="ru.bgcrm.struts.action.ProcessAction:processPriorityUpdate">
					<c:if test="${processType.properties.configMap['hidePriority'] ne 1}">
						<c:url var="url" value="/user/empty.do">
							<c:param name="returnUrl" value="${requestUrl}"/>
							<c:param name="returnChildUiid" value="${tableId}"/>
							<c:param name="id" value="${process.id}"/>
							<c:param name="forwardFile" value="/WEB-INF/jspf/user/process/process/editor_priority.jsp"/>
							<c:param name="priority" value="${process.priority}"/>
						</c:url>
						[<a href="#" onclick="$$.ajax.load('${url}', $('#${uiid}')); return false;">${l.l('приоритет')}</a>]
					</c:if>
				</p:check>

				<p:check action="ru.bgcrm.struts.action.ProcessAction:processTypeUpdate">
					<c:url var="url" value="/user/process.do">
						<c:param name="action" value="processTypeEdit"/>
						<c:param name="id" value="${process.id}"/>
						<c:param name="typeId" value="${process.typeId}" />
						<c:param name="returnUrl" value="${requestUrl}"/>
						<c:param name="returnChildUiid" value="${tableId}"/>
						<c:param name="forward" value="processTypeChange"/>
					</c:url>
					[<a href="#" onclick="$$.ajax.load('${url}', $('#${uiid}').parent()); return false;">${l.l('изменить тип')}</a>]
				</p:check>

				<p:check action="ru.bgcrm.struts.action.ProcessAction:unionLog">
					<c:url var="logUrl" value="/user/process.do">
						<c:param name="action" value="unionLog"></c:param>
						<c:param name="id" value="${form.id}"></c:param>
						<c:param name="type" value="process"></c:param>
						<c:param name="objectType" value="${form.param.objectType}"></c:param>
						<c:param name="returnUrl" value="${requestUrl}"></c:param>
					</c:url>
					<c:if test="${not empty processType}">
						[<a href="#" onclick="$$.ajax.load('${logUrl}', $('#${tableId}').parent()); return false;">${l.l('лог изменений')}</a>]
					</c:if>
				</p:check>
			</ui:when>	
		</div>

		<div class="pb05">
			${l.l('Создан')}: ${u:formatDate( process.createTime, 'ymdhms' )}
			<c:if test="${process.createUserId gt 0}">
				(<ui:user-link id="${process.createUserId}"/>)
			</c:if>
		</div>
		<div class="pb05">
			<%@ include file="process_status_current.jsp"%>
		</div>
		<c:if test="${process.closeUserId gt 0}">
			<div class="pb05">Закрыт: ${u:formatDate( process.closeTime, 'ymdhms' )} (<ui:user-link id="${process.closeUserId}"/>)</div>
		</c:if>
	</div>
</u:sc>