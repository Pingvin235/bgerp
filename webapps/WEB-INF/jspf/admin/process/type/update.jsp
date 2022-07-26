<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="type" value="${form.response.data.type}"/>

<html:form action="/admin/process" styleClass="center500">
	<input type="hidden" name="action" value="typeUpdate"/>
	<input type="hidden" name="update" value="true"/>
	<c:choose>
		<c:when test="${not empty type}">
			<input type="hidden" name="parentTypeId" value="${type.parentId}"/>
		</c:when>
		<c:otherwise>
			<html:hidden property="parentTypeId"/>
		</c:otherwise>
	</c:choose>

	<h2>ID</h2>
	<input type="text" name="id" value="${form.param['id']}" disabled="disabled" style="width: 100%;"/>

	<h2>${l.l('Название')}</h2>
	<html:text property="title" style="width: 100%" value="${type.title}"/>

	<h2>${l.l('Наследовать свойства от предка')}</h2>
	<u:sc>
		<c:set var="valuesHtml">
			<li value="0">${l.l('No')}</li>
			<li value="1">${l.l('Yes')}</li>
		</c:set>
		<c:set var="hiddenName" value="useParent"/>
		<c:if test="${type.useParentProperties}">
			<c:set var="value" value="1"/>
		</c:if>
		<c:set var="style" value="width: 100px;"/>
		<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
	</u:sc>
	<div>
		<ui:form-ok-cancel styleClass="mt1"/>
	</div>
</html:form>

<shell:state ltext="Редактор типа" help="kernel/process/index.html#type"/>
