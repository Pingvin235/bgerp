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
			
			<h2>Название</h2>
			<html:text property="title" style="width: 100%" value="${config.title}"/>
				
			<c:if test="${empty perm['activeAllow'] or perm['activeAllow'] eq '1'}">
				<h2>Активный</h2>
				<u:sc>
					<c:set var="valuesHtml">
						<li value="0">Нет</li>
						<li value="1">Да</li>									
					</c:set>
					<c:set var="hiddenName" value="active"/>
					<c:set var="value" value="${config.active ? 1 : 0}"/>
					<c:set var="style" value="width: 100px;"/>
					<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
				</u:sc>
			</c:if>	
		</div><%-- 
	--%><div style="width: 70%;" class="pl1">
			<h2>Конфигурация</h2>
			<c:set var="taUiid" value="${u:uiid()}"/>
			<textarea id="${taUiid}" name="data" style="width: 100%; resize: vertical;" wrap="off" rows="40">${config.data}</textarea>
		</div>
	</div>		
			
	<div class="mt1">
		<c:set var="toPostNames" value="['data']"/>
		<c:set var="saveCommand" value="sendAJAXCommand(formUrl(this.form), ${toPostNames})"/>
		
		<%@ include file="/WEB-INF/jspf/send_and_cancel_form.jsp"%>
		
		<c:set var="saveScript">
			var result = ${saveCommand}; 
			if (result)
				openUrlToParent('${editUrl}', $('#${formUiid}'));
		</c:set>		
		<button type="button" class="btn-grey" onclick="${saveScript}" style="float: right;" title="Сохранить без выхода из редактора">Сохранить</button>
	</div>
</html:form>

<shell:state text="${l.l('Редактор')}" help="http://www.bgcrm.ru/doc/3.0/manual/kernel/setup.html#config"/>

<script>
	$(function () {
		$$.ui.markChanged($('#${taUiid}'));
	});
</script>