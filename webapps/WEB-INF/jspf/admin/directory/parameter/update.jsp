<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="formUiid" value="${u:uiid()}"/>

<c:set var="heightSampleUiid" value="${u:uiid()}"/>
<c:set var="heightToUiid" value="${u:uiid()}"/>

<c:set var="typeChangedScript">
	$$.param.editorTypeChanged('${formUiid}', '${heightSampleUiid}', '${heightToUiid}');
</c:set>

<%@ include file="/WEB-INF/jspf/admin/directory/directory.jsp"%>

<c:set var="parameter" value="${frd.parameter}"/>

<html:form action="/admin/directory" styleClass="center1020" styleId="${formUiid}">
	<input type="hidden" name="action" value="parameterUpdate"/>
	<html:hidden property="directoryId"/>

	<div class="in-inline-block in-pr1 in-va-top">
		<div style="width: 50%;">
			<h2>ID</h2>
			<input type="text" name="id" value="${form.id}" disabled="disabled" style="width: 100%;"/>

			<div id="${heightSampleUiid}">
				<h2>${l.l('Название')}</h2>
				<input type="text" name="title" style="width: 100%" value="${u.escapeXml( parameter.title )}"/>

				<h2>${l.l('Type')}</h2>
				<c:choose>
					<c:when test="${form.id ne '-1'}">
						<input type="text" name="type" value="${parameter.type}" disabled="disabled" style="width: 100%;"/>
					</c:when>
					<c:otherwise>
						<ui:combo-single hiddenName="type" list="<%=ru.bgcrm.model.param.Parameter.TYPES%>" styleClass="w100p" onSelect="${typeChangedScript}"/>
					</c:otherwise>
				</c:choose>

				<div id="listValues">
					<h2>${l.l('Значения')}</h2>

					<textarea name="listValues" rows="30" style="width: 100%; resize:none;" wrap="off">${parameter.valuesConfig}</textarea>
					<span class="hint list listcount">${l.l('hint.list.values')}</span>
					<span class="hint tree treecount">${l.l('hint.tree.values')}</span>
				</div>

				<h2>${l.l('Порядок')}</h2>
				<input type="text" name="order" value="${parameter.order}" style="width: 100%;"/>

				<span class="hint">${l.l('hint.param.order')}</span>
			</div>
		</div><%--
	--%><div style="width: 50%;">
			<h2>${l.l('Комментарий')}</h2>
			<input type="text" name="comment" style="width: 100%" value="${u.escapeXml( parameter.comment )}"/>

			<h2>${l.l('Configuration')}</h2>
			<textarea id="${heightToUiid}" name="config" rows="7" style="width: 100%; resize:none;" wrap="off">${parameter.config}</textarea>
		</div>
	</div>

	<div class="mt1">
		<ui:form-ok-cancel/>
	</div>
</html:form>

<script>
	${typeChangedScript}
</script>

<shell:state text="${l.l('Редактор')}" help="kernel/setup.html#param"/>
