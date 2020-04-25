<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${processType.properties.configMap.getSok('1', false, 'show.tab.links.process.up', 'processShowProcessLinks.Linked') ne '0' and not empty form.response.data.linkedProcessList}">
	<h2>${l.l('Процесс привязан к')}:</h2>
	
	<c:set var="list" value="${form.response.data.linkedProcessList}"/>
	<c:set var="mode" value="linked"/>
	<%@ include file="process_link_table.jsp"%>
</c:if>

<c:if test="${processType.properties.configMap.getSok('1', false, 'show.tab.links.process.down', 'processShowProcessLinks.Links') ne '0'}">
	<c:if test="${processType.properties.configMap.getSok('1', false, 'show.tab.links.process.add.from.buffer', 'processCreateLinkModeSelect') ne '0'}">
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
								<li value="processLink">Ссылается</li>
								<li value="processDepend">Зависит</li>
								<li value="processMade">Породил</li>
							</c:set>
							<c:set var="hiddenName" value="objectType"/>
							<c:set var="style" value="width: 100%;"/>
							<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
						</u:sc>	
					</div>
					
					<div class="pl1" style="white-space: nowrap;">
						<c:set var="script">
							$('#${uiid} #addButton').hide();
							var objectType = this.form.objectType;
							processesToLinkTable( $('#${uiid} #linkTable'), ${form.id}, objectType.value );
							$('#${uiid} #linkObjects').show();
						</c:set>
					
						<button type="button" class="btn-green" onclick="${script}" ${style}>+</button>
					</div>
				</div>	
			</html:form>
			
			<%@ include file="process_link_exist_link.jsp"%>
		</div>
	</c:if>	
	
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