<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="formUiid" value="${u:uiid()}"/>

<c:set var="selectorSample" value="${u:uiid()}"/>
<c:set var="selectorTo" value="${u:uiid()}"/>

<c:set var="typeChangedScript">
	var $form = $('#${formUiid}');

	var type = $form[0].type.value;
	var $listValues = $form.find('#listValues');

	if ($listValues.toggle(type === 'tree' || type == 'list' ||  type == 'listcount').is(':visible'))
	{
		$listValues.find('.hint').hide();
		$listValues.find('.' + type).show();
	}

	$('#${selectorTo}').css( "height", $('#${selectorSample}').height() + 'px' );
</c:set>

<%@ include file="/WEB-INF/jspf/admin/directory/directory.jsp"%>

<c:set var="parameter" value="${form.response.data.parameter}"/>

<html:form action="/admin/directory" styleClass="center1020" styleId="${formUiid}">
	<input type="hidden" name="action" value="parameterUpdate"/>
	<html:hidden property="directoryId"/>

	<div class="in-inline-block in-pr1 in-va-top">
		<div style="width: 50%;">
			<h2>ID</h2>
			<input type="text" name="id" value="${form.id}" disabled="disabled" style="width: 100%;"/>

			<div id="${selectorSample}">
				<h2>${l.l('Название')}</h2>
				<input type="text" name="title" style="width: 100%" value="${fn:escapeXml( parameter.title )}"/>

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

					<textarea name="listValues" rows="7" style="width: 100%; resize:none;" wrap="off">${parameter.valuesConfig}</textarea>
					<span class="hint list listcount">${l.l('hint.list.values')}</span>
					<span class="hint tree">${l.l('hint.tree.values')}</span>
				</div>

				<h2>${l.l('Порядок')}</h2>
				<input type="text" name="order" value="${parameter.order}" style="width: 100%;"/>

				<span class="hint">${l.l('hint.param.order')}</span>
			</div>
		</div><%--
	--%><div style="width: 50%;">
			<h2>${l.l('Комментарий')}</h2>
			<input type="text" name="comment" style="width: 100%" value="${fn:escapeXml( parameter.comment )}"/>

			<%--
			<h2>Скрипт</h2>
			<input type="text" name="script" style="width: 100%" value="${parameter.script}"/>
			--%>

			<h2>${l.l('Конфигурация')}</h2>
			<textarea id="${selectorTo}" name="config" rows="7" style="width: 100%; resize:none;" wrap="off">${parameter.config}</textarea>
		</div>
	</div>

	<div class="mt1">
		<ui:form-ok-cancel/>
	</div>
</html:form>

<script>
	${typeChangedScript}
	$$.ui.codeMirror('${selectorTo}');
</script>

<shell:state ltext="Редактор" help="kernel/setup.html#param"/>
