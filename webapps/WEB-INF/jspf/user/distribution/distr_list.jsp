<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<option value="-1" checked="true">&lt;не выбрано распределение&gt;</option>

<c:set var="distributions" value="${form.response.data.distrbution}"/>
<c:forEach var="distr" items="${distributions.entrySet()}">
	<option value="${distr.key}">${distr.value.title}</option>
</c:forEach>
