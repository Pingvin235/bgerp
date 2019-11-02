<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>


ПРИВЕТ
<html:form action="/dynTest">
	<html:text property="param1"/>
	<%-- <html:text property="param2"/>  --%>
</html:form>

${form.action}
url ${form.requestUrl}