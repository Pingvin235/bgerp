<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div style="display: table; width: 100%;">
	<div class="in-table-cell">
		<div style="vertical-align: top; width: 30px;" id="typeSelectContainer">
			<h2>Тип</h2>
			<%-- сюда будет помещён выпадающий список выбора типа --%>
		</div>
		<div id="address" class="pl1" style="width: 100%; vertical-align: top;">
			<h2>Код темы форума</h2>
			<input type="text" name="systemId" style="width: 100%;" placeholder="" value="${message.systemId}"/>
		</div>
	</div>

	<div class="mt1 mb1">
		<button class="btn-grey" type="button" onclick="if( sendAJAXCommand( formUrl( this.form ), ['text'] ) ){ $$.ajax.load('${form.returnUrl}', $('#${form.returnChildUiid}').parent()) }">OK</button>
		<button class="btn-grey ml1" type="button" onclick="$('#${form.returnChildUiid}').empty();">${l.l('Отмена')}</button>
	</div>
</div>