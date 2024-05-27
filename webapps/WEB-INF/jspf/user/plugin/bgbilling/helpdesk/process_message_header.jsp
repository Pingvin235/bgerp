<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${messageType.getClass().getName() eq 'ru.bgcrm.plugin.bgbilling.dao.MessageTypeHelpDesk'}">
	<c:set var="billingId" value="${messageType.billingId}"/>

	<div style="width: 100%;">
		<div>
			<%@ include file="/WEB-INF/jspf/user/message/message_direction.jsp"%>
			#${message.id} HelpDesk ${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[billingId].title} (${message.systemId})
		</div>
		<div class="mt05">
			<c:choose>
				<c:when test="${message.incoming}">
					Создано: ${tu.format(message.fromTime, 'ymdhm')}
					<c:if test="${not empty message.toTime}">
						&nbsp;Прочитано: ${tu.format(message.toTime, 'ymdhm')}
						(<ui:user-link id="${message.userId}"/>)
					</c:if>
				</c:when>
				<c:otherwise>
					Создано: ${tu.format(message.fromTime, 'ymdhm')} (<ui:user-link id="${message.userId}"/>)
					<c:if test="${not empty message.toTime}">
						&nbsp;Прочитано: ${tu.format(message.toTime, 'ymdhm')}
					</c:if>
				</c:otherwise>
			</c:choose>
		</div>
	</div>

	<c:if test="${message.unread}">
		<div>
			<c:url var="url" value="/user/plugin/bgbilling/proto/helpdesk.do">
				<c:param name="method" value="markMessageRead"/>
				<c:param name="messageId" value="${message.id}"/>
			</c:url>

			<button class="btn-white" type="button"
				onclick="$$.ajax.post('${url}').done(() => { $$.ajax.load('${form.requestUrl}', $('#${editorContainerUiid}').parent()) })">Прочитано</button>
		</div>
	</c:if>
</c:if>