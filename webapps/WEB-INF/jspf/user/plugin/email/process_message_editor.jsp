<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div style="display: table; width: 100%;">
	<div class="in-table-cell">
		<div style="vertical-align: top; width: 30px;" id="typeSelectContainer">
			<h2>${l.l('Тип')}</h2>
			<%-- here will be placed type selection --%>
		</div>
		<div class="pl1 w100p">
			<h2>${l.l('Получатель')}</h2>
			<c:choose>
				<c:when test="${ctxUser.checkPerm('org.bgerp.plugin.msg.email.action.EMailAction:recipients') and
					ctxUser.personalizationMap.get('iface.email.message.tag-box.disable') ne '1'}">
					<ui:tag-box inputName="to" style="width: 100%;"
						showOptions="1"
						value="${message.to}"
						url="/user/plugin/email/email.do?action=recipients&processId=${form.param.processId}"
						preload="true"/>
				</c:when>
				<c:otherwise>
					<input type="text" name="to" style="width: 100%;"
						placeholder="EMail: addr1@domain.com, addr2@domain.com; CC: copy1@domain.com, copy2.domain.com"
						value="${message.to}"/>
				</c:otherwise>
			</c:choose>
			<div class="hint">${l.l('Используйте запятую либо Enter для разделения значений')}</div>
		</div>
	</div>
	<div class="in-table-cell pt1">
		<input type="hidden" name="updateTags" value="1"/>

		<c:set var="tagConfig" value="${ctxSetup.getConfig('ru.bgcrm.model.message.TagConfig')}"/>
		<c:set var="messageTagIds" value="${form.response.data.messageTagIds}"/>

		<div style="width: 30px; vertical-align: top;">
			<h2>${l.l('Вложить историю')}</h2>
			<ui:combo-single hiddenName="tagId" value="${tagConfig.getSelectedHistoryTag(messageTagIds)}" widthTextValue="120px">
				<jsp:attribute name="valuesHtml">
					<li value="0">-- ${l.l('нет')} --</li>
					<li value="<%=ru.bgcrm.model.message.TagConfig.Tag.TAG_HISTORY_WITH_ADDRESS_ID%>">${l.l('Переписка с данным адресом')}</li>
					<li value="<%=ru.bgcrm.model.message.TagConfig.Tag.TAG_HISTORY_ALL_ID%>">${l.l('Все сообщения')}</li>
				</jsp:attribute>
			</ui:combo-single>
		</div>
		<div class="pl1" style="width: 100%;">
			<h2>${l.l('Теги')}</h2>
			<ui:select-mult list="${tagConfig.tagList}" values="${messageTagIds}" hiddenName="tagId" style="width: 100%;"/>
		</div>
	</div>

	<%@ include file="/WEB-INF/jspf/user/message/process_message_edit_text.jsp"%>
	<div>
		<%@ include file="/WEB-INF/jspf/user/message/process_message_edit_upload_list.jsp"%>
	</div>

	<%@ include file="/WEB-INF/jspf/user/message/process_message_edit_ok_cancel.jsp"%>
</div>
