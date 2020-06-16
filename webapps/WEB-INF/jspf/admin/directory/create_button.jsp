<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:sc>
	<c:url var="url" value="/admin/directory.do">
		<c:param name="returnUrl" value="${form.requestUrl}"/>
		<c:param name="directoryId" value="${form.param.directoryId}"/>
		<c:param name="id" value="-1"/>
	</c:url>
	<button 
		type="button" class="btn-green" 
		onclick="$$.ajax.load('${url}&action=' + this.form.action.value.replace('List', 'Get'), $$.shell.$content())">+</button>
</u:sc>
