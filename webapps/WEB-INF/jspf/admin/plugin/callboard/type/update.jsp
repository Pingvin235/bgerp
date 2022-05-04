<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="/admin/plugin/callboard/work" styleId="${uiid}" styleClass="center1020">
	<input type="hidden" name="action" value="workTypeUpdate" />

	<c:set var="workType" value="${form.response.data.workType}"/>

	<div class="in-table-cell in-pr1 in-va-top">
		<div style="width: 50%;" id="left">
			<h2>ID</h2>
			<input type="text" name="id" style="width: 100%" value="${workType.id}" disabled="disabled"/>

			<h2>${l.l('Наименование')}</h2>
			<div id="sample"></div>
			<input type="text" name="title" style="width: 100%" value="${workType.title}"/>

			<h2>${l.l('Категория')}</h2>
			<u:sc>
				<c:set var="list" value="${allowOnlyCategories}"/>
				<c:set var="hiddenName" value="categoryId"/>
				<c:set var="value" value="${workType.category}"/>
				<c:set var="style" value="width: 100%;"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
			</u:sc>

			<h2>${l.l('Цвет')}</h2>
			<div class="controlset">
				<input type="text" name="color" value="${workType.color}" />
			</div>
			<div class="hint">Используется в плане работ.</div>


   			<h2>Учитывать в рабочих часах</h2>
			<u:sc>
				<c:set var="valuesHtml">
					<li value="0">${l.l('Да')}</li>
					<li value="1">${l.l('Нет')}</li>
				</c:set>
				<c:set var="hiddenName" value="nonWorkHours"/>
				<c:set var="value" value="${workType.nonWorkHours ? 1 : 0}"/>
				<c:set var="style" value="width: 100px;"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
			</u:sc>
			<div class="hint">Учитывать время данного типа работ в рабочих часах графика.</div>

   			<h2>Обозначение в табеле</h2>

			<input type="text" name="shortcuts" value="${u:toString( workType.shortcutList )}" style="width: 100%;"/>
			<div class="hint">Сокращения для табеля через запятую.</div>

			<%--
			<u:sc>
				<c:set var="valuesHtml">
					<li value="0">Не установлено</li>
					<c:forEach var="item" items="${shortcutMap}">
						<li value="${item.key}">${item.value.value} (${item.value.title})</li>
					</c:forEach>
				</c:set>
				<c:set var="hiddenName" value="shortcutId"/>
				<c:set var="value" value="${workType.shortcutId}"/>
				<c:set var="style" value="width: 100%;"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
			</u:sc>
			 --%>

   			<h2>${l.l('Комментарий')}</h2>
			<html:textarea property="comment" style="width: 100%; resize: none;" value="${workType.comment}"/>

		</div><%--
   	--%><div style="width: 50%;">
   			<h2>Шаг длительности (размер слота)</h2>
			<input type="text" name="timeSetStep" style="width: 100%" value="${workType.timeSetStep}"/>
			<div class="hint">Шаг в минутах, через который устанавливается время процессов. 0 - все процессы назначаются на начало периода типа работ.</div>

			<h2>Назначать время на</h2>
			<u:sc>
				<c:set var="valuesHtml">
					<li value="0">Начало интервала</li>
					<li value="1">Начало слота</li>
				</c:set>
				<c:set var="hiddenName" value="timeSetMode"/>
				<c:set var="value" value="${workType.timeSetMode}"/>
				<c:set var="style" value="width: 150px;"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
			</u:sc>

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

<c:set var="state" value="Редактор"/>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>
