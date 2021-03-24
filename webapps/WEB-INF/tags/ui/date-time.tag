<%@ tag body-content="empty" pageEncoding="UTF-8" description="Date input with optional time"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="paramName" description="имя инпута"%>
<%@ attribute name="value" description="текущее значение dd.MM.yyyy, '0' - текущая дата, 'first' - первый день месяца, 'last' - последний"%>
<%@ attribute name="type" description="указанный тип даты, ymdhms либо короче, если не указан - то это правка параметра"%>
<%@ attribute name="selector" description="параметр $ функции выборки элемента"%>
<%@ attribute name="editable" type="java.lang.Boolean"  description="можно править"%>
<%@ attribute name="styleClass" description="CSS классы для input а"%>
<%@ attribute name="placeholder" description="placeholder для input а"%>
<%@ attribute name="saveCommand" description="команда для сохранения значения по закрытию"%>
<%@ attribute name="parameter" type="java.lang.Object" description="доступ к конфигурации при правке параметра объекта"%>

<c:if test="${empty selector and not empty paramName}">
	<c:set var="uiid" value="${u:uiid()}"/>
	<c:set var="selector" value="#${uiid}"/>
	<input type="text" name="${paramName}" id="${uiid}" class="${styleClass}" placeholder="${placeholder}" value="${value}"/>
</c:if>


<%-- type: ymd, ymdh, ymdhm, ymdhms --%>
<c:if test="${empty type and not empty parameter}">
	<c:set var="type" value="${parameter.configMap.type}"/>
</c:if>
<c:if test="${empty type}">
	<c:set var="type" value="ymd"/>
</c:if>

<c:set var="timeFormat" value=""/>

<c:set var="dateFormat" value="${u:getDateTypeFormat( type )}"/>
<c:if test="${fn:contains( dateFormat, ' ' ) }">
	<c:set var="dateFormat" value="${fn:substringBefore( dateFormat, ' ')}"/>
	<c:set var="timeFormat" value="${fn:substringAfter( u:getDateTypeFormat(type), ' ' )}"/>
</c:if>

<c:set var="dateFormat" value="${fn:replace(dateFormat, 'yyyy', 'yy')}"/>
<c:set var="dateFormat" value="${fn:replace(dateFormat, 'MM', 'mm')}"/>

<c:set var="size" value="8"/>
<c:if test="${type eq 'ymdh'}">
	<c:set var="size" value="10"/>
</c:if>
<c:if test="${type eq 'ymdhm'}">
	<c:set var="size" value="13"/>
</c:if>
<c:if test="${type eq 'ymdhms'}">
	<c:set var="size" value="16"/>
</c:if>

<script style="display: none;">
	<%-- если атрибут не удалить - поле не отрабатывает получение фокуса и т.п.
		 кое-где раньше стояло  --%>
	$("${selector}").removeAttr( "readonly" );

	$("${selector}").datetimepicker({
		"dateFormat" : "${dateFormat}",
		"showHour" : ${fn:startsWith( type, 'ymdh') },
		"showMinute" : ${fn:startsWith( type, 'ymdhm') },
		"showSecond" : ${fn:startsWith( type, 'ymdhms') },
		"timeFormat" : "${timeFormat}",
		"stepMinute" : 5

		<c:if test="${type ne 'ymd'}">
			, "showTime" : true
		</c:if>

		<c:if test="${parameter.configMap['showTimeSelector'] == 1}">
			, showTimeSelector : true
		</c:if>

		<c:if test="${type eq 'ymd'}">
			, "showTimepicker" : false
			, "onSelect": function() {  $("${selector}").datepicker( "setNowIfEmptySaveAndHide" ); }
		</c:if>

		<c:if test="${not empty getDateUrl}">
			, "onChangeMonthYear" : function(year, month, inst) { if(year != undefined && month != undefined) { datetimepickerOnChanging(year, month, inst, '${getDateUrl}') } }
			, "afterShow": function(input, inst){ datetimepickerOnChanging(inst.selectedYear, inst.selectedMonth+1, inst, '${getDateUrl}') }
		</c:if>

		<c:if test="${not empty saveCommand}">
			, onClose: function() { ${saveCommand} }
		</c:if>

		<c:forEach var="item" items="${parameter.configMap}">
			,"${item.key}" : "${item.value}"
		</c:forEach>
	});

	<%-- TODO: Вынести функции в JS файлы --%>
	<%@ include file="/WEB-INF/jspf/datetimepicker_inputmask.jsp"%>

	<%-- убран нередактируемый с клавиатуры режим
	<c:choose>
		<c:when test="${not empty parameter.configMap['editable'] or not empty editable}">
			<%@ include file="datetimepicker_inputmask.jsp"%>
		</c:when>
		<c:otherwise>
			$("${selector}").attr( "readonly", "true" );
		</c:otherwise>
	</c:choose>
	--%>

	<c:if test="${value eq '0' or value eq 'last' or value eq 'first'}">
		var date = new Date();
		<c:if test="${value eq '0'}">
			$("${selector}").datepicker('setDate', date);
		</c:if>
		<c:if test="${value eq 'last'}">
			$("${selector}").datepicker('setDate', new Date(date.getFullYear(), date.getMonth() + 1, 0));
		</c:if>
		<c:if test="${value eq 'first'}">
			$("${selector}").datepicker('setDate', new Date(date.getFullYear(), date.getMonth(), 1));
		</c:if>
	</c:if>

	$("${selector}").attr( "size", "${size}" );
	$("${selector}").css( "text-align", "center" );
</script>

<c:set var="type" value=""/>
<c:set var="editable" value=""/>
<c:set var="value" value=""/>