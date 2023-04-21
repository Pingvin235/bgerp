<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:choose>
	<c:when test="${l.lang eq 'en'}">
		<script>
			$.datepicker.setDefaults({
				firstDay: 1
			});
		</script>
	</c:when>
	<%-- only 'ru' is supported for now --%>
	<c:otherwise>
		<script src="/lib/jquery-ui-1.12.1/i18n/jquery.ui.datepicker-${l.lang}.js"></script>
		<script src="/lib/timepicker-1.3/i18n/jquery.ui.timepicker-${l.lang}.js"></script>
	</c:otherwise>
</c:choose>
