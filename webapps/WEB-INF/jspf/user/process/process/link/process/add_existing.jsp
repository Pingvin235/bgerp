<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Incoming variables:
	processType
	mode - 'linked' or 'link'
	linkFormUiid
--%>

<c:if test="${processType.properties.configMap.get('show.tab.links.process.add.from.buffer', '1') ne '0'}">
	<u:sc>
		<c:set var="uiid" value="${u:uiid()}"/>
		<div id="${uiid}">
			<html:form action="${form.httpRequestURI}" styleId="addButton" styleClass="pt1">
				<input type="hidden" name="action" value="${mode}ProcessCreate"/>
				<html:hidden property="id"/>

				<div class="in-table-cell">
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
						<c:url var="url" value="/user/empty.do">
							<c:param name="id" value="${form.id}"/>
							<c:param name="returnUrl" value="${requestUrl}"/>
							<c:param name="forwardFile" value="/WEB-INF/jspf/user/process/process/link/process/buffer_process.jsp"/>
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

			<div id="linkObjects" style="display: none;">
				<div id="linkTable">
					<%-- сюда сгенерируется таблица с процессами --%>
				</div>
				<div class="hint">${l.l('Для привязки доступны процессы, выбранные в буфер')}.</div>

				<div class="tableIndent mt1">
					<c:set var="script">
						const deffs = [];

						const forms = $('#${uiid} #linkObjects form');
						for (var i = 0; i < forms.length; i++) {
							var form = forms[i];

							if (!form.check.checked)
								continue;

							deffs.push($$.ajax.post(form));
						}
						$.when.apply($, deffs).done(() => {
							$$.ajax.load('${form.requestUrl}', $('#${linkFormUiid}').parent());
						});
					</c:set>

					<ui:button type="ok" onclick="${script}"/>
					<ui:button type="cancel" styleClass="ml1" onclick="$('#${uiid} #linkObjects').hide(); $('#${uiid} #addButton').show();"/>
				</div>
			</div>
		</div>
	</u:sc>
</c:if>