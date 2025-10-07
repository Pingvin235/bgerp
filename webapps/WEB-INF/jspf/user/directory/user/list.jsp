<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="paramName" value="executor"/>
<c:if test="${not empty form.param.paramName}">
	<c:set var="paramName" value="${form.param.paramName}"/>
</c:if>

<li>
	<input type="checkbox" name="${paramName}" value="empty" ${u:checkedFromCollection(form.getParamValues('executor'), 'empty')}/>
	<span>** ${l.l("Undefined")} **</span>
</li>
<li>
	<input type="checkbox" name="${paramName}" value="current" ${u:checkedFromCollection(form.getParamValues('executor'), 'current')}/>
	<span>${l.l("** Я **")}</span>
</li>
<c:forEach var="item" items="${frd.list}">
	<li>
		<input type="checkbox" name="${paramName}" value="${item.id}" ${u:checkedFromCollection(form.getParamValues('executor'), item.id)}/>
		<span>${item.title}</span>
	</li>
</c:forEach>
