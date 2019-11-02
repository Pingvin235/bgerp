<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="type" value="${form.response.data.type}"/>

<html:form action="admin/process" styleClass="center500">
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
	
	<h2>Название</h2>
	<html:text property="title" style="width: 100%" value="${type.title}"/>
		
	<h2>Наследовать свойства от предка</h2>
	<u:sc>
		<c:set var="valuesHtml">
			<li value="0">Нет</li>
			<li value="1">Да</li>			
		</c:set>
		<c:set var="hiddenName" value="useParent"/>
		<c:if test="${type.useParentProperties}">
			<c:set var="value" value="1"/>
		</c:if>	
		<c:set var="style" value="width: 100px;"/>
		<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
	</u:sc>
	
	<div class="mt1">
		<%@ include file="/WEB-INF/jspf/send_and_cancel_form.jsp"%>
	</div>
</html:form>

<c:set var="state" value="Редактор типа"/>
<c:set var="help" value="http://www.bgcrm.ru/doc/3.0/manual/kernel/process/index.html#type"/>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>