<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
<html:form styleId="${uiid}" action="user/process/link">
	<html:hidden property="action"/>
	<html:hidden property="id"/>
	<html:hidden property="linkedReferenceName"/>
	<html:hidden property="linkReferenceName"/>
	<html:hidden property="ifaceId"/>
	<html:hidden property="ifaceState"/>

	<ui:combo-single hiddenName="open" value="${form.param.open}" onSelect="$$.ajax.load($('#${uiid}'), $('#${uiid}').parent())"
		prefixText="${l.l('Открыт')}:" styleClass="mr1" widthTextValue="50px">
		<jsp:attribute name="valuesHtml">
			<li value="">${l.l('Все')}</li>
			<li value="1">${l.l('Да')}</li>
			<li value="0">${l.l('Нет')}</li>
		</jsp:attribute>
	</ui:combo-single>
</html:form>

<c:if test="${processType.properties.configMap.getSok('1', false, 'show.tab.links.process.up', 'processShowProcessLinks.Linked') ne '0' and not empty form.response.data.linkedProcessList}">
	<h2>${l.l('Процесс привязан к')}:</h2>

	<c:set var="list" value="${form.response.data.linkedProcessList}"/>
	<c:set var="mode" value="linked"/>
	<%@ include file="process_link_table.jsp"%>
</c:if>

<c:if test="${processType.properties.configMap.getSok('1', false, 'show.tab.links.process.down', 'processShowProcessLinks.Links') ne '0'}">
	<c:if test="${(processType.properties.configMap.getSok('1', false, 'show.tab.links.process.add.from.buffer', 'processCreateLinkModeSelect') ne '0') and
		ctxUser.checkPerm('ru.bgcrm.struts.action.ProcessAction:linkProcessCreate')}">
		<c:set var="uiid" value="${u:uiid()}"/>
		<div id="${uiid}">
			<html:form action="/user/process/link" styleId="addButton" styleClass="pt1">
				<input type="hidden" name="action" value="linkProcessCreate"/>
				<input type="hidden" name="id" value="${form.id}"/>

				<div class="in-table-cell">
					<div style="width: 100%;">
						<u:sc>
							<c:remove var="list"/>
							<c:set var="valuesHtml">
								<li value="processLink">${l.l('Ссылается')}</li>
								<li value="processDepend">${l.l('Зависит')}</li>
								<li value="processMade">${l.l('Породил')}</li>
							</c:set>
							<c:set var="hiddenName" value="objectType"/>
							<c:set var="style" value="width: 100%;"/>
							<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
						</u:sc>
					</div>

					<div class="pl1" style="white-space: nowrap;">
						<c:url var="url" value="/user/empty.do">
							<c:param name="id" value="${form.id}"/>
							<c:param name="returnUrl" value="${requestUrl}"/>
							<c:param name="forwardFile" value="/WEB-INF/jspf/user/process/process/process_link_buffer_process.jsp"/>
						</c:url>

						<c:set var="script">
							$('#${uiid} #addButton').hide();

							let url = '${url}&linkType=' + this.form.objectType.value;
							const processes = openedObjectList({typesInclude: ['process']});
							for (const i in processes) {
								const process = processes[i];
								url += '&process=' + encodeURIComponent(process.id + ':' + process.title);
							}

							$$.ajax.load(url, $('#${uiid} #linkTable'));

							$('#${uiid} #linkObjects').show();
						</c:set>

						<ui:button type="add" onclick="${script}"/>
					</div>
				</div>
			</html:form>

			<%@ include file="process_link_exist_link.jsp"%>
		</div>
	</c:if>

	<%-- preconfigured process types, processCreateLink https://bgerp.org/doc/3.0/manual/kernel/process/#linked-process --%>
	<c:set var="requestUrl" value="${form.requestUrl}"/>
	<%@ include file="process_link_create_and_link.jsp"%>

	<c:if test="${not empty form.response.data.list}">
		<c:set var="uiid" value="${u:uiid()}"/>
		<html:form action="/user/process/link" styleId="${uiid}">
			<div style="display: inline-block;" class="tt bold mt05 mb05">${l.l('К процессу привязаны')}:</div>

			<input type="hidden" name="action" value="linkProcessList"/>
			<input type="hidden" name="id" value="${form.id}"/>

			<ui:page-control nextCommand="; $$.ajax.load(this.form, $('#${uiid}').parent())"/>
		</html:form>

		<c:set var="list" value="${form.response.data.list}"/>
		<c:set var="mode" value="link"/>
		<%@ include file="process_link_table.jsp"%>
	</c:if>
</c:if>