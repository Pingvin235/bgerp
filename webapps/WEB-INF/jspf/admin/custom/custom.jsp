<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h2 id="custom-src-header">SRC</h2>

<plugin:include endpoint="admin.custom"/>

<ui:files files="<%=org.bgerp.action.admin.CustomAction.CUSTOM_SRC%>" maxCount="20"/>

<html:form action="/admin/custom" onsubmit="return false;" style="display: inline-block;" styleClass="mt1">
	<input type="hidden" name="method" value="compile"/>
	<button class="btn-grey" type="button" onclick="$$.ajax.loadContent(this);">${l.l('Compile Java code')}</button>
</html:form>

<c:set var="result" value="${frd.result}"/>
<c:if test="${not empty result}">
	<div class="mt1">
		<b>${l.l('Compilation result')}:</b><br/>
		${u:htmlEncode(result.logString)}
	</div>
</c:if>

<h2>JAR</h2>
<ui:files files="<%=org.bgerp.action.admin.CustomAction.CUSTOM_JAR%>" requestUrl="${form.requestUrl}" maxCount="1"/>
<div class="mt1">
	<%@ include file="../app/app_restart.jsp"%>
</div>

<shell:title text="${l.l('Custom')}"/>
<shell:state help="kernel/extension.html#custom"/>
