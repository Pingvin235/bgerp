<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="btnStyle" value="btn-white"/>
<c:if test="${action eq form.method}">
	<c:set var="btnStyle" value="btn-blue"/>
</c:if>
<button type="button" class="${btnStyle}" onclick="$('#${balanceForm} input[name=action]').val('${action}'); ${sendForm}">${title}</button>