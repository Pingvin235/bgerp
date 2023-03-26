<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${l.lang ne 'en'}">
	<script src="/lib/jquery-ui-1.12.1/i18n/jquery.ui.datepicker-${l.lang}.js"></script>
	<script src="/lib/timepicker-1.3/i18n/jquery.ui.timepicker-${l.lang}.js"></script>
</c:if>
