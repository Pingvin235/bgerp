<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div id="customWorkTypesChoiseDialog${uiid}"
	title="Распределение видов работ" date="" team="" groupId="" userId=""
	style="text-align: center;">
	<c:set var="addCommentField" value="1" />
	<%@ include
		file="/WEB-INF/jspf/admin/work/shift/work_type_time_editor.jsp"%>
</div>

<div id="addGroupDialog${uiid}" userId="0"
	title="Добавление группы пользователю" style="text-align: center;">
	<div>
		<span>Пользователь: <b id="addGroupDialogUserTitle${uiid}"></b></span>
	</div>
	<div style="margin-top: 10px;">
		<a>Группа:&nbsp;</a> <select id="addGroupDialogSelect${uiid}"
			style="width: 180px;">
			<c:forEach var="item" items="${groupWithUsersMap}">
				<option value="${item.key}">${ctxUserGroupMap[item.key]}</option>
			</c:forEach>
		</select> <a>&nbsp;Период с: </a> <input id="addGroupDialogFromDate${uiid}"
			name="fromDate" type="text" />
		<c:set var="selector">input#addGroupDialogFromDate${uiid}</c:set>
		<c:set var="initialDate">0</c:set>
		<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>

		<a>по:</a> <input id="addGroupDialogToDate${uiid}" name="toDate"
			type="text" />
		<c:set var="selector">input#addGroupDialogToDate${uiid}</c:set>
		<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
	</div>
</div>

<div id="workTypeDiff${uiid}" title="Объединение бригад"
	style="text-align: center;">
	<div>
		<span>Вы собираетесь объединить две бригады с <b>разными</b>
			шаблонами видов работ. Выберите какой шаблон следует использовать.
		</span>
	</div>

	<div style="height: 100px; margin-top: 10px; position: relative;">
		<div id="workTypeDiffLeft${uiid}"
			style="float: left; width: 45%; height: 90%; margin-left: 2%;"></div>
		<div id="workTypeDiffRight${uiid}"
			style="float: right; width: 45%; height: 90%; margin-right: 2%;"></div>
		<div style="clear: both"></div>

		<input type="radio" style="float: left; margin-left: 22%;"
			name="workType" value="1"> <input type="radio"
			style="float: right; margin-right: 22%;" name="workType" value="2">
	</div>
</div>

<div id="shiftSetPeriodDialog${uiid}" title="Установка смен периодом">
	<div style="text-align: center;">
		<div>
			<span><b>Пользователь:&nbsp;</b></span> <span id="name">Иванов
				Иван Иванович</span>
		</div>
		<div style="padding-top: 5px;">
			<span class="ui-helper-hidden-accessible"><input type="text" /></span>
			<a><b>Период:</b> с </a> <input
				id="shiftSetPeriodDialogFromDate${uiid}" name="fromDate"
				class="fromDate" type="text" />
			<c:set var="selector">input#shiftSetPeriodDialogFromDate${uiid}</c:set>
			<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>

			<a>по</a> <input id="shiftSetPeriodDialogToDate${uiid}" name="toDate"
				type="text" class="toDate" />
			<c:set var="selector">input#shiftSetPeriodDialogToDate${uiid}</c:set>
			<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
		</div>
	</div>
	<div style="padding-top: 10px;">
		<span><b>Правила:</b></span> <input
			id="shiftPeriodAddRuleButton${uiid}" name="shiftPeriodAddRuleButton"
			type="button" value="Добавить" />
	</div>
	<div id="shiftPeriodRuleContainer${uiid}"
		style="border-top: 1px solid #000000; margin-top: 5px; padding-top: 5px; padding-left: 5px; min-height: 30px;">
	</div>
	<div style="padding-top: 5px;">
		<span><input id="shiftPeriodReplaceOld${uiid}" type="checkbox" />затереть
			существующий график</span>
	</div>
	<div id="shiftContainer${uiid}" style="display: none;"></div>
</div>

<div id="shiftPeriodNewRuleDialog${uiid}" title="Добавить правило">
	<div style="text-align: center;">
		<div style="display: inline-block;">
			<span>Смена:</span> <select id="newRuleshiftSelect${uiid}"
				name="rule" class="parametersSelect" style="width: 200px">
				<option value="0">Пустая смена</option>
				<c:forEach var="item" items="${avaiableShiftMap}">
					<option value="${item.key}">${item.value.title}
						(${item.key})</option>
				</c:forEach>
			</select>
		</div>
		<div style="display: inline-block; margin-top: 5px;">
			<span>Количество дней:</span> <input
				id="newRuleshiftDaysCount${uiid}" type="text" style="width: 50px" />
		</div>
	</div>
</div>

<div id="shiftGroupSortingDialog${uiid}"
	title="Сортировка пользователей">
	<div id="content">
		<ul class="sortable">
			<li class="ui-state-default"><span
				class="ui-icon ui-icon-arrowthick-2-n-s"></span>Item 1</li>
		</ul>
	</div>
</div>

<div id="contextMenu${uiid}"
	style="display: none; width: 200px; background-color: white; border: 1px solid black; padding: 5px; cursor: pointer;">
	Редактировать смену</div>

<div id="floatingShiftTimeSetDialog${uiid}"
	title="Установить время для динамического типа работы"></div>

<div id="dynamicShiftDialog${uiid}" style="text-align: center;">
	<span>Укажите точное время для смены:<br><b class="shiftTitle"></b></span> 
	<div class="content" style="display: inline-block;"></div>
</div>