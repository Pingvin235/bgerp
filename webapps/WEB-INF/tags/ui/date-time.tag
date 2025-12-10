<%@ tag body-content="empty" pageEncoding="UTF-8" description="Date input with optional time"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="id" description="input's id, auto generated if not explicitly specified"%>
<%@ attribute name="paramName" description="input's name"%>
<%@ attribute name="selector" description="jQuery selector of an existing input text element"%>
<%@ attribute name="type" description="specified 'date' type, ymdhms or shorter; parameter is altered if value was not defined"%>
<%@ attribute name="parameter" type="java.lang.Object" description="provides access to the configuration when editing object's parameter"%>
<%@ attribute name="value" description="current value in defined format or '0' - current date, 'first' - first day of the month, 'last' - last day of the month"%>
<%@ attribute name="styleClass" description="CSS-classes for input"%>
<%@ attribute name="placeholder" description="placeholder for input"%>
<%@ attribute name="saveCommand" description="command used to save the value upon closure"%>

<%-- type: ymd, ymdh, ymdhm, ymdhms --%>
<c:if test="${empty type and not empty parameter}">
	<c:set var="type" value="${parameter.configMap.type}"/>
</c:if>
<c:if test="${empty type}">
	<c:set var="type" value="ymd"/>
</c:if>

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
	<c:choose>
		<c:when test="${type eq 'ymd' and ctxUser.pers['iface.input.date'] eq 'native'}">
			<c:set var="nativeInput" value="1"/>
			<input type="date" name="${paramName}" value="${value}" class="${styleClass}" placeholder="${placeholder}"/>
		</c:when>
		<c:otherwise>
			<input type="text" name="${paramName}" id="${uiid}" value="${value}" class="${styleClass}" placeholder="${placeholder}"/>
		</c:otherwise>
	</c:choose>
</c:if>

<c:if test="${empty nativeInput}">
	<script style="display: none;">
		$(() => {
			const $input = $("${selector}");
			$input.datetimepicker({
				"timeFormat" : "${su.substringAfter(tu.getTypeFormat(type), ' ')}",
				"showHour" : ${type.startsWith('ymdh')},
				"showMinute" : ${type.startsWith('ymdhm')},
				"showSecond" : ${type.startsWith('ymdhms')},
				"stepMinute" : 5,

				onClose: function () {
					${saveCommand}
				}

				<c:if test="${type eq 'ymd'}">
					, onSelect: function () {
						$input.datepicker("hide");
					}
					, "showTimepicker" : false
				</c:if>

				<c:forEach var="item" items="${parameter.configMap}">
					,"${item.key}" : "${item.value}"
				</c:forEach>
			});

			$$.ui.datetime.init('${selector}', '${type}', '${tu.getTypeFormat(type)}', '${value}');
		})
	</script>
</c:if>
