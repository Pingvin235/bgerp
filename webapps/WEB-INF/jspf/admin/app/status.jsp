<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="center1020">
	<h2>${l.l('Status')}</h2>
	<pre>${frd.statusApp}</pre>

	<c:set var="traceUiid" value="${u:uiid()}"/>

	<h2>${l.l('DB')} <span class="normal"> [<a href="#" onclick="$('#${traceUiid}').toggle(); $(this).toggleClass('bold'); return false;">trace</a>]</span></h2>
	<pre>${frd.statusDb}</pre>

	<div id="${traceUiid}" style="display: none;">
		<h2>${l.l('DB Trace')}</h2>
		<pre class="box cmd" style="overflow-x: auto;">${frd.dbTrace}</pre>
	</div>

	<p:check action="/admin/app:restart">
		<h2>${l.l('Restart')}</h2>
		<html:form action="/admin/app">
			<input type="hidden" name="method" value="restart"/>
			<input type="hidden" name="confirmText" value="${l.l('Perform restart?')}"/>
			<%@ include file="run_restart_button.jsp"%>
		</html:form>
	</p:check>

	<h2>${l.l('Logs')}</h2>

	<c:set var="uiid" value="${u:uiid()}"/>
	<div id="${uiid}">
		<ul>
			<li><a href="#tabs-1">${l.l('Application')}</a></li><%--
		--%><li><a href="#tabs-2">${l.l('Access')}</a></li>
		</ul>
		<div id="tabs-1"><ui:files files="<%=org.bgerp.action.admin.AppAction.LOG_APP%>" requestUrl="${form.requestUrl}"/></div>
		<div id="tabs-2"><ui:files files="<%=org.bgerp.action.admin.AppAction.LOG_ACCESS%>" requestUrl="${form.requestUrl}" maxCount="10"/></div>
	</div>

	<script>
		$("#${uiid}").tabs();
	</script>

	<div class="in-table-cell">
		<p:check action="/admin/app:update">
			<div class="pr1">
				<h2>${l.l('Update')}</h2>
				<html:form action="/admin/app">
					<input type="hidden" name="method" value="update"/>
					<ui:combo-single hiddenName="force" widthTextValue="3em" prefixText="${l.l('Force')}:" styleClass="mr05">
						<jsp:attribute name="valuesHtml">
							<li value="0">${l.l('No')}</li>
							<li value="1">${l.l('Yes')}</li>
						</jsp:attribute>
					</ui:combo-single>
					<input type="hidden" name="confirmText" value="${l.l('Perform update?')}"/>
					<%@ include file="run_update_restart_button.jsp"%>
				</html:form>
			</div>
		</p:check>

		<c:set var="error" value="${frd.error}"/>

		<p:check action="/admin/app:updateToChange">
			<div>
				<h2>${l.l('Update on change')}</h2>
				<html:form action="/admin/app">
					<input type="hidden" name="method" value="updateToChange"/>
					<ui:combo-single hiddenName="changeId" widthTextValue="18em" prefixText="ID:" styleClass="mr05">
						<jsp:attribute name="valuesHtml">
							<c:forEach var="item" items="${frd.changes}">
								<li value="${item.id}">${item.title}</li>
							</c:forEach>
						</jsp:attribute>
					</ui:combo-single>
					<c:choose>
						<c:when test="${empty error}">
							<input type="hidden" name="confirmText" value="${l.l('Update on the change?')}"/>
							<%@ include file="run_update_restart_button.jsp"%>
						</c:when>
						<c:otherwise>
							<b>${l.l('Is not allowed because of:')}&nbsp;${error}</b>
						</c:otherwise>
					</c:choose>
				</html:form>
			</div>
		</p:check>
	</div>

	<c:set var="uiid" value="${u:uiid()}"/>
	<div id="${uiid}" class="mt1">
		<ul>
			<li><a href="#tabs-1">${l.l('Logs')}</a></li><%--
		--%><li><a href="#tabs-2">${l.l('Files')}</a></li>
		</ul>
		<div id="tabs-1"><ui:files files="<%=org.bgerp.action.admin.AppAction.LOG_UPDATE%>" requestUrl="${form.requestUrl}" maxCount="20"/></div>
		<div id="tabs-2"><ui:files files="<%=org.bgerp.action.admin.AppAction.UPDATE_ZIP%>" requestUrl="${form.requestUrl}" maxCount="20"/></div>
	</div>

	<script>
		$("#${uiid}").tabs();
	</script>
</div>

<shell:title text="${l.l('App Status')}"/>
<shell:state error="${error}" help="kernel/install.html#app-status"/>