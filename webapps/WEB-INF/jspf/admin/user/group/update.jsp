<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="/admin/user" styleClass="center1020">
	<input type="hidden" name="method" value="groupUpdate" />
	<html:hidden property="parentGroupId"/>

	<c:set var="group" value="${frd.group}" scope="page"/>

	<c:set var="uiidSample" value="${u:uiid()}"/>
	<c:set var="uiidTo" value="${u:uiid()}"/>

	<div class="in-inline-block in-pr1">
		<div style="width: 50%;">
			<h2>ID</h2>

			<div id="${uiidSample}">
				<input type="text" name="id" style="width: 100%" value="${group.id}" disabled="disabled"/>

				<h2>${l.l('Название')}</h2>
				<input type="text" name="title" style="width: 100%" value="${group.title}"/>

				<h2>${l.l('Comment')}</h2>
				<input type="text" name="comment" style="width: 100%" value="${group.comment}"/>
			</div>
		</div><%--
	--%><div style="width: 50%;">
			<h2>${l.l('Configuration')}</h2>
			<div id="${uiidTo}">
				<c:set var="taUiid" value="${u:uiid()}"/>
				<textarea name="groupConfig" id="${taUiid}" style="width:100%; height: 100%; resize: none;" wrap="off">${group.config}</textarea>
			</div>
		</div>
	</div>

	<u:sc>
		<c:set var="selectorSample" value="#${uiidSample}"/>
		<c:set var="selectorTo" value="#${uiidTo}"/>
		<%@ include file="/WEB-INF/jspf/same_height.jsp"%>
	</u:sc>

	<div class="in-inline-block in-pr1 in-va-top">
		<div style="width: 50%;">
			<h2>${l.l('Наборы прав')}</h2>

			<ui:select-mult list="${ctxUserPermsetList}" map="${ctxUserPermsetMap}" hiddenName="permset" availableIdSet="${u.toIntegerSet(perm.allowPermsetSet)}" values="${group.permsetIds}" moveOn="1"/>
		</div><%--
	--%><div style="width: 50%;">
			<h2>${l.l('Очереди процессов')}</h2>

			<ui:select-mult list="${ctxProcessQueueList}" map="${ctxProcessQueueMap}" hiddenName="queue" values="${group.queueIds}"/>
		</div>
	</div>

	<ui:form-ok-cancel styleClass="mt1"/>
</html:form>

<shell:state text="${l.l('Редактор')}" help="kernel/setup.html#user"/>

<script>
	$(function () {
		$$.ui.codeMirror('${taUiid}');
	})
</script>