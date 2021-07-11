<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:url var="editUrl" value="/admin/config.do">
	<c:param name="action" value="get"/>
	<c:param name="id" value="${form.id}"/>
	<c:param name="returnUrl" value="${form.returnUrl}"/>
</c:url>

<c:set var="formUiid" value="${u:uiid()}"/>

<html:form action="admin/config" styleId="${formUiid}">
	<html:hidden property="action" value="update"/>

	<c:set var="config" value="${form.response.data.config}"/>
	<c:set var="lastModifyUiid" value="${u:uiid()}"/>
	<div id="${lastModifyUiid}">
		<c:set var="lastModifyObject" value="${config}"/>
		<%@ include file="/WEB-INF/jspf/last_modify_hiddens.jsp"%>
	</div>

	<c:set var="perm" value="${p:get(form.user.id, 'ru.bgcrm.struts.action.admin.ConfigAction:update')}" />

	<div class="in-inline-block in-va-top">
		<div style="width: 30%;">
			<h2>ID</h2>
			<input type="text" name="id" style="width: 100%" value="${config.id}" disabled="disabled"/>

			<h2>${l.l('Название')}</h2>
			<html:text property="title" style="width: 100%" value="${config.title}"/>

			<c:if test="${empty perm['activeAllow'] or perm['activeAllow'] eq '1'}">
				<h2>${l.l('Активный')}</h2>
				<ui:combo-single hiddenName="active" value="${config.active ? 1 : 0}" style="width: 100px;">
					<jsp:attribute name="valuesHtml">
						<li value="0">${l.l('Нет')}</li>
						<li value="1">${l.l('Да')}</li>
					</jsp:attribute>
				</ui:combo-single>
			</c:if>
		</div><%--
	--%><div style="width: 70%;" class="pl1">
			<h2>${l.l('Конфигурация')}</h2>
			<c:set var="taUiid" value="${u:uiid()}"/>
			<textarea id="${taUiid}" name="data" style="width: 100%; resize: vertical;" wrap="off" rows="40">${config.data}</textarea>
		</div>
	</div>

	<div class="mt1">
		<c:set var="toPostNames" value="['data']"/>
		<ui:form-ok-cancel toPostNames="${toPostNames}"/>
		<button type="button" class="btn-grey"
			onclick="$$.ajax.post(this.form, {toPostNames: ${toPostNames}}).done(() => {
				$$.ajax.load('${editUrl}', $('#${formUiid}').parent());
			})"
		style="float: right;" title="${l.l('Сохранить без выхода из редактора')}">${l.l('Сохранить')}</button>
	</div>
</html:form>

<shell:state text="${l.l('Редактор')}" help="kernel/setup.html#config"/>

<script>
	$(function () {
		$$.ui.codeMirror('${taUiid}');
	});
</script>