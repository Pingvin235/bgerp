<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>


<html:form action="/user/process">
	<input type="hidden" name="action" value="processUpdate"/>
	<%@ include file="../tree/process_type_tree.jsp"%>
	
	
</html:form>