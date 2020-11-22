<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${messageType.getClass().getName() eq 'ru.bgcrm.plugin.bgbilling.dao.MessageTypeHelpDesk'}">
	<c:set var="billingId" value="${messageType.billingId}"/>
	
	<div style="width: 100%;">
		 <div>
		 	#${message.id} HelpDesk ${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[billingId].title} (${message.systemId})
		 </div>
		 <div class="mt05">						 
			<c:choose>
				<c:when test="${message.direction eq 1}">
					${l.l('Создано')}: ${u:formatDate( message.fromTime, 'ymdhm' )}
					Прочитано: ${u:formatDate( message.toTime, 'ymdhm' )}
					<c:if test="${not empty message.toTime}">
						(<ui:user-link id="${message.userId}"/>)
					</c:if>	
				</c:when>
				<c:otherwise>
					${l.l('Создано')}: ${u:formatDate( message.fromTime, 'ymdhm' )} (<ui:user-link id="${message.userId}"/>)
					Прочитано: ${u:formatDate( message.toTime, 'ymdhm' )}
				</c:otherwise>
			</c:choose>
		</div>
	</div>
	
	<c:if test="${empty message.toTime and message.direction eq 1}">
		<div>
			<c:url var="url" value="/user/plugin/bgbilling/proto/helpdesk.do">
				<c:param name="action" value="markMessageRead"/>
				<c:param name="messageId" value="${message.id}"/>
			</c:url>
		
			<button class="btn-white" type="button" onclick="if( sendAJAXCommand( '${url}' ) ){ openUrlToParent( '${form.requestUrl}', $('#${editorContainerUiid}') ) }">Прочитано</button>
		</div>		
	</c:if>	
</c:if>