<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="/admin/plugin/callboard/work" styleClass="center1020">
	<input type="hidden" name="method" value="shiftUpdate" />

	<c:set var="shift" value="${frd.shift}"/>
	<c:set var="workTypeList" value="${frd.workTypeList}"/>
	<c:set var="workTypeMap" value="${frd.workTypeMap}"/>

	<div class="in-inline-block in-pr1 in-va-top">
		<div style="width: 50%;">
			<h2>ID</h2>
			<input type="text" name="id" style="width: 100%" value="${shift.id}" disabled="disabled"/>

			<h2>Категория</h2>
			<ui:combo-single list="${allowOnlyCategories}" hiddenName="categoryId" value="${shift.category}" style="width: 100%;"/>

			<h2>${l.l('Title')}</h2>
			<html:text property="title" style="width: 100%" value="${shift.title}"/>


		</div><%--
	--%><div style="width: 50%;">
			<h2>Использовать цвет смены</h2>
			<ui:combo-single hiddenName="useOwnColor" value="${shift.useOwnColor ? 1 : 0}" style="width: 100%;">
				<jsp:attribute name="valuesHtml">
					<li value="1">Да</li>
					<li value="0">Нет</li>
				</jsp:attribute>
			</ui:combo-single>

			<h2>Символ смены (не больше 2 символов)</h2>
			<html:text property="symbol" style="width: 100%" value="${shift.symbol}"/>

			<h2>Цвет:</h2>
			<div class="controlset">
				<input type="text" name="color" id="shiftColor${uiid}" value="${shift.color}" />
			</div>
		</div>
	</div>

	<h2>Виды работ</h2>
	<%@ include file="work_type_time_editor.jsp"%>

	<ui:form-ok-cancel/>
</html:form>

<script>
	$('input#shiftColor${uiid}').colorPicker();
</script>

<shell:state text="${l.l('Редактор')}"/>