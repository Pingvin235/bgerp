<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:url var="editUrl" value="/admin/config.do">
	<c:param name="method" value="get"/>
	<c:param name="id" value="${form.id}"/>
	<c:param name="returnUrl" value="${form.returnUrl}"/>
</c:url>

<c:set var="formUiid" value="${u:uiid()}"/>

<html:form action="/admin/config" styleId="${formUiid}">
	<html:hidden property="method" value="update"/>

	<c:set var="config" value="${frd.config}"/>
	<c:set var="lastModifyUiid" value="${u:uiid()}"/>
	<div id="${lastModifyUiid}">
		<c:set var="lastModifyObject" value="${config}"/>
		<%@ include file="/WEB-INF/jspf/last_modify_hiddens.jsp"%>
	</div>

	<div class="in-inline-block in-va-top">
		<div style="width: 30%;">
			<h2>ID</h2>
			<input type="text" name="id" style="width: 100%" value="${config.id}" disabled="disabled"/>

			<h2>${l.l('Название')}</h2>
			<html:text property="title" style="width: 100%" value="${config.title}"/>

			<c:if test="${config.parentId le 0 and ctxUser.getPerm('/admin/config:update')['activeAllow'] ne '0'}">
				<h2>${l.l('Активный')}</h2>
				<ui:combo-single hiddenName="active" value="${config.active ? 1 : 0}" style="width: 100px;">
					<jsp:attribute name="valuesHtml">
						<li value="0">${l.l('No')}</li>
						<li value="1">${l.l('Yes')}</li>
					</jsp:attribute>
				</ui:combo-single>
			</c:if>
		</div><%--
	--%><div style="width: 70%;" class="pl1">
			<h2>${l.l('Configuration')}</h2>
			<c:set var="taUiid" value="${u:uiid()}"/>
			<textarea id="${taUiid}" name="data" style="width: 100%;" rows="40">${config.data}</textarea>
		</div>
	</div>

	<div class="mt1">
		<c:set var="loadReturn" value="$$.ajax.load('${form.returnUrl}', $(this.form).parent())"/>
		<c:set var="updateAllowed" value="${ctxUser.checkPerm('/admin/config:update')}"/>

		<c:if test="${updateAllowed}">
			<ui:button type="ok" styleClass="mr1" onclick="$$.ajax.post(this).done(() => {${loadReturn}})"/>
		</c:if>

		<ui:button type="cancel" onclick="${loadReturn}"/>

		<c:if test="${updateAllowed}">
			<span style="float: right;">
				<button type="button" class="btn-grey mr1"
					onclick="$$.ajax.post(this).done(() => $$.ajax.load('${editUrl}', $('#${formUiid}').parent()))"
					title="${l.l('Save without leaving editor')}">${l.l('Save')}</button>
				<button type="button" class="btn-grey"
					onclick="$$.ajax.load('${editUrl}', $('#${formUiid}').parent());">${l.l('Restore')}</button>
			</span>
		</c:if>
	</div>
</html:form>

<shell:state text="${l.l('Редактор')}" help="kernel/setup.html#config"/>

<script>
	$$.ui.codeMirror('${taUiid}');
</script>