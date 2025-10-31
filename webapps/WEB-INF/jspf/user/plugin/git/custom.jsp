<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="action" value="/admin/plugin/git/custom"/>
<p:check action="${action}:null">
	<c:url var="url" value="${action}.do">
		<c:param name="returnUrl" value="${form.requestUrl}"/>
	</c:url>
	<script>
		$$.ajax.post('${url}', { html: true }).done((result) => {
			document.getElementById('custom-src-header').innerHTML += result;
		});
	</script>
</p:check>