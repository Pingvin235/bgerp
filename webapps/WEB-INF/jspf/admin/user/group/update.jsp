<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="/admin/user" styleClass="center1020">
	<input type="hidden" name="action" value="groupUpdate" />
	<html:hidden property="parentGroupId"/>

	<c:set var="group" value="${form.response.data.group}" scope="page"/>

	<c:set var="uiidSample" value="${u:uiid()}"/>
	<c:set var="uiidTo" value="${u:uiid()}"/>

	<div class="in-inline-block in-pr1">
		<div style="width: 50%;">
			<h2>ID</h2>

			<div id="${uiidSample}">
				<input type="text" name="id" style="width: 100%" value="${group.id}" disabled="disabled"/>

				<h2>${l.l('Название')}</h2>
				<input type="text" name="title" style="width: 100%" value="${group.title}"/>

				<h2>${l.l('Комментарий')}</h2>
				<input type="text" name="comment" style="width: 100%" value="${group.comment}"/>

				<h2>${l.l('Скрытая')}</h2>

				<u:sc>
					<c:set var="valuesHtml">
						<li value="0">${l.l('No')}</li>
						<li value="1">${l.l('Yes')}</li>
					</c:set>
					<c:set var="hiddenName" value="archive"/>
					<c:set var="value" value="${group.archive}"/>
					<c:set var="style" value="width: 100px;"/>
					<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
				</u:sc>
			</div>
		</div><%--
	--%><div style="width: 50%;">
			<h2>${l.l('Конфигурация')}</h2>
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

			<u:sc>
				<c:set var="list" value="${ctxUserPermsetList}"/>
				<c:set var="map" value="${ctxUserPermsetMap}"/>
				<c:set var="hiddenName" value="permset" />
				<c:set var="available" value="${u.toIntegerSet(perm['allowPermsetSet'])}"/>
				<c:set var="values" value="${group.permsetIds}" />
				<c:set var="moveOn" value="1"/>
				<%@ include file="/WEB-INF/jspf/select_mult.jsp"%>
			</u:sc>
		</div><%--
	--%><div style="width: 50%;">
			<h2>${l.l('Очереди процессов')}</h2>

			<u:sc>
				<c:set var="list" value="${ctxProcessQueueList}"/>
				<c:set var="map" value="${ctxProcessQueueMap}"/>
				<c:set var="hiddenName" value="queue"/>
				<c:set var="values" value="${group.queueIds}"/>
				<%@ include file="/WEB-INF/jspf/select_mult.jsp"%>
			</u:sc>
		</div>
	</div>

	<ui:form-ok-cancel styleClass="mt1"/>
</html:form>

<shell:state ltext="Редактор" help="kernel/setup.html#user"/>

<script>
	$$.ui.codeMirror('${taUiid}');
</script>