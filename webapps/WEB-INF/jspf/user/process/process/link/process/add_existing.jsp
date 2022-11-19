<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Incoming variables:
	processType
	mode - 'linked' or 'link'
	linkFormUiid
--%>

<u:sc>
	<c:set var="action" value="${mode}ProcessAdd"/>

	<c:if test="${ctxUser.checkPerm('org.bgerp.action.ProcessLinkProcessAction:'.concat(action))}">
		<c:set var="uiid" value="${u:uiid()}"/>
		<div id="${uiid}">
			<html:form action="${form.httpRequestURI}" styleClass="pt1">
				<input type="hidden" name="action" value="${action}"/>
				<html:hidden property="id"/>

				<div id="addButton" class="in-table-cell">
					<div class="w100p">
						<ui:combo-single hiddenName="objectType" styleClass="w100p">
							<jsp:attribute name="valuesHtml">
								<li value="processLink">Link</li>
								<li value="processDepend">Depend</li>
								<li value="processMade">Made</li>
							</jsp:attribute>
						</ui:combo-single>
					</div>

					<div class="pl1" style="white-space: nowrap;">
						<c:url var="url" value="${form.httpRequestURI}">
							<c:param name="id" value="${form.id}"/>
							<c:param name="action" value="${mode}ProcessAvailable"/>
							<c:param name="returnUrl" value="${requestUrl}"/>
						</c:url>

						<c:set var="script">
							$('#${uiid} #addButton').hide();

							let url = '${url}&objectType=' + this.form.objectType.value;
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

				<div id="linkObjects" style="display: none;">
					<div id="linkTable">
						<%-- сюда сгенерируется таблица с процессами --%>
					</div>
					<div class="hint">${l.l('Для привязки доступны процессы, выбранные в буфер')}.</div>

					<div class="tableIndent mt1">
						<c:set var="script">
							$$.ajax.post(this).done(() => $$.ajax.load('${form.requestUrl}', $('#${linkFormUiid}').parent()));
						</c:set>

						<ui:button type="ok" onclick="${script}"/>
						<ui:button type="cancel" styleClass="ml1" onclick="$('#${uiid} #linkObjects').hide(); $('#${uiid} #addButton').show();"/>
					</div>
				</div>
			</html:form>
		</div>
	</c:if>
</u:sc>