<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="/admin/plugin/callboard/work" styleId="${uiid}" styleClass="center1020">
	<input type="hidden" name="method" value="workTypeUpdate" />

	<c:set var="workType" value="${frd.workType}"/>

	<div class="in-table-cell in-pr1 in-va-top">
		<div style="width: 50%;" id="left">
			<h2>ID</h2>
			<input type="text" name="id" style="width: 100%" value="${workType.id}" disabled="disabled"/>

			<h2>${l.l('Title')}</h2>
			<div id="sample"></div>
			<input type="text" name="title" style="width: 100%" value="${workType.title}"/>

			<h2>${l.l('Категория')}</h2>
			<ui:combo-single list="${allowOnlyCategories}" hiddenName="categoryId" value="${workType.category}" style="width: 100%;"/>

			<h2>${l.l('Цвет')}</h2>
			<div class="controlset">
				<input type="text" name="color" value="${workType.color}" />
			</div>
			<div class="hint">Используется в плане работ.</div>


			<h2>Учитывать в рабочих часах</h2>
			<ui:combo-single hiddenName="nonWorkHours" value="${workType.nonWorkHours ? 1 : 0}" style="width: 100px;">
				<jsp:attribute name="valuesHtml">
					<li value="0">${l.l('Yes')}</li>
					<li value="1">${l.l('No')}</li>
				</jsp:attribute>
			</ui:combo-single>

			<div class="hint">Учитывать время данного типа работ в рабочих часах графика.</div>

			<h2>Обозначение в табеле</h2>

			<input type="text" name="shortcuts" value="${u.toString( workType.shortcutList )}" style="width: 100%;"/>
			<div class="hint">Сокращения для табеля через запятую.</div>


			<h2>${l.l('Комментарий')}</h2>
			<html:textarea property="comment" style="width: 100%; resize: none;" value="${workType.comment}"/>

		</div><%--
	--%><div style="width: 50%;">
			<h2>Шаг длительности (размер слота)</h2>
			<input type="text" name="timeSetStep" style="width: 100%" value="${workType.timeSetStep}"/>
			<div class="hint">Шаг в минутах, через который устанавливается время процессов. 0 - все процессы назначаются на начало периода типа работ.</div>

			<h2>Назначать время на</h2>
			<ui:combo-single hiddenName="timeSetMode" value="${workType.timeSetMode}" style="width: 150px;">
				<jsp:attribute name="valuesHtml">
					<li value="0">Начало интервала</li>
					<li value="1">Начало слота</li>
				</jsp:attribute>
			</ui:combo-single>

			<h2>Конфигурация длительности</h2>
			<textarea name="ruleConfig" style="width: 100%; resize: none;" wrap="off" class="layout-height-rest">${workType.ruleConfig}</textarea>
		</div>
	</div>

	<ui:form-ok-cancel styleClass="mt1"/>

	<script>
		$(function()
		{
			$('#${uiid} input[name=color]').colorPicker();
		})
	</script>

	<%@ include file="/WEB-INF/jspf/layout_process.jsp"%>
</html:form>

<shell:state text="${l.l('Редактор')}"/>