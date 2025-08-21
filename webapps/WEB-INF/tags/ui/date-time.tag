<%@ tag body-content="empty" pageEncoding="UTF-8" description="Date input with optional time"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="id" description="id of input, auto generated if not explicitly specified"%>
<%@ attribute name="paramName" description="input's name"%>
<%@ attribute name="value" description="current value in dd.MM.yyyy format or '0' - current date, 'first' - first day of the month, 'last' - last day of the month"%>
<%@ attribute name="type" description="specified 'date' type, ymdhms or shorter; parameter is altered if value was not defined"%>
<%@ attribute name="selector" description="jQuery selector of input element (deprecated)"%>
<%@ attribute name="styleClass" description="CSS-classes for input"%>
<%@ attribute name="placeholder" description="placeholder for input"%>
<%@ attribute name="saveCommand" description="command used to save the value upon closure"%>
<%@ attribute name="parameter" type="java.lang.Object" description="provides access to the configuration when editing object's parameter"%>

<c:if test="${empty selector and not empty paramName}">
	<c:choose>
		<c:when test="${not empty id}">
			<c:set var="uiid" value="${id}"/>
		</c:when>
		<c:otherwise>
			<c:set var="uiid" value="${u:uiid()}"/>
		</c:otherwise>
	</c:choose>
	<c:set var="selector" value="#${uiid}"/>
	<input type="text" name="${paramName}" id="${uiid}" class="${styleClass}" placeholder="${placeholder}" value="${value}"/><%--
--%></c:if>

<%-- type: ymd, ymdh, ymdhm, ymdhms --%>
<c:if test="${empty type and not empty parameter}">
	<c:set var="type" value="${parameter.configMap.type}"/>
</c:if>
<c:if test="${empty type}">
	<c:set var="type" value="ymd"/>
</c:if>

<c:set var="timeFormat" value=""/>

<c:set var="dateFormat" value="${tu.getTypeFormat( type )}"/>
<c:if test="${dateFormat.contains(' ')}">
	<c:set var="dateFormat" value="${su.substringBefore(dateFormat, ' ')}"/>
	<c:set var="timeFormat" value="${su.substringAfter(tu.getTypeFormat(type), ' ')}"/>
</c:if>

<c:set var="dateFormat" value="${dateFormat.replace('yyyy', 'yy')}"/>
<c:set var="dateFormat" value="${dateFormat.replace('MM', 'mm')}"/>

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
	<%-- if attribute is not removed, then the field won't react on getting focus, etc --%>
	$("${selector}").removeAttr( "readonly" );

	$("${selector}").datetimepicker({
		"dateFormat" : "${dateFormat}",
		"showHour" : ${type.startsWith('ymdh')},
		"showMinute" : ${type.startsWith('ymdhm')},
		"showSecond" : ${type.startsWith('ymdhms')},
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

		<c:if test="${not empty saveCommand}">
			, onClose: function() { ${saveCommand} }
		</c:if>

		<c:forEach var="item" items="${parameter.configMap}">
			,"${item.key}" : "${item.value}"
		</c:forEach>
	});

	$$.ui.datetime.init('${selector}', '${type}', '${tu.getTypeFormat(type)}');

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