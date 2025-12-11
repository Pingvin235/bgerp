<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div style="display: table; width: 100%;">
	<div class="in-table-cell">
		<div style="vertical-align: top; width: 30px;" id="typeSelectContainer">
			<h2>${l.l('Type')}</h2>
			<%-- here will be placed type selection --%>
		</div>
		<div class="pl1 w100p">
			<c:choose>
				<c:when test="${ctxUser.checkPerm('/user/plugin/email/email:recipients') and
					ctxUser.pers.get('iface.email.message.tag-box.disable') ne '1'}">

					<c:set var="to" value="${not empty message ? message.to : ''}"/>
					<c:set var="addresses" value="${u:newInstance1('org.bgerp.util.mail.Addresses', to)}"/>

					<h2>${l.l('Получатель')}</h2>
					<ui:tag-box name="to" style="width: 100%;"
						showOptions="1"
						value="${addresses.serializeTo()}"
						url="/user/plugin/email/email.do?method=recipients&processId=${form.param.processId}"
						preload="true"
						title="${l.l('email.recipients.input.hint')}"
					/>

					<h2>${l.l('Recipient (Copy)')}</h2>
					<ui:tag-box name="toCc" style="width: 100%;"
						showOptions="1"
						value="${addresses.serializeCc()}"
						url="/user/plugin/email/email.do?method=recipients&processId=${form.param.processId}"
						preload="true"
						title="${l.l('email.recipients.input.hint')}"
					/>
				</c:when>
				<c:otherwise>
					<h2>${l.l('Получатель')}</h2>
					<input type="text" name="to" style="width: 100%;"
						placeholder="addr1@domain.com, addr2@domain.com, CC: copy1@domain.com, copy2.domain.com"
						value="${message.to}"/>
				</c:otherwise>
			</c:choose>
		</div>
	</div>
	<div class="in-table-cell pt1">
		<input type="hidden" name="updateTags" value="1"/>

		<c:set var="tagConfig" value="${ctxSetup.getConfig('org.bgerp.model.msg.config.TagConfig')}"/>
		<c:set var="messageTagIds" value="${frd.messageTagIds}"/>

		<div style="width: 30px; vertical-align: top;">
			<h2>${l.l('Вложить историю')}</h2>
			<ui:combo-single name="tagId" value="${tagConfig.getSelectedHistoryTag(messageTagIds)}" widthTextValue="10em">
				<jsp:attribute name="valuesHtml">
					<li value="0">-- ${l.l('нет')} --</li>
					<li value="<%=org.bgerp.model.msg.config.TagConfig.Tag.TAG_HISTORY_WITH_ADDRESS_ID%>">${l.l('Переписка с данным адресом')}</li>
					<li value="<%=org.bgerp.model.msg.config.TagConfig.Tag.TAG_HISTORY_ALL_ID%>">${l.l('Все сообщения')}</li>
				</jsp:attribute>
			</ui:combo-single>
		</div>
		<div class="pl1" style="width: 100%;">
			<h2>${l.l('Tags')}</h2>
			<ui:select-mult list="${tagConfig.tagList}" values="${messageTagIds}" name="tagId" style="width: 100%;"/>
		</div>
	</div>

	<%@ include file="/WEB-INF/jspf/user/message/process/edit/text.jsp"%>
	<div>
		<%@ include file="/WEB-INF/jspf/user/message/process/edit/upload_list.jsp"%>
	</div>

	<%@ include file="/WEB-INF/jspf/user/message/process/edit/ok_cancel.jsp"%>
</div>
