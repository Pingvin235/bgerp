<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>


<ui:combo-single name="resourceId" value="${form.param.resourceId}" list="${frd.resources}" prefixText="Диапазон:"
	styleClass="w100p mb1" />

<select name="freeNumber" multiple style="width: 100%; height: 20em;">
	<c:forEach var="item" items="${frd.free}">
		<option value="${item}">${item}</option>
	</c:forEach>
</select>