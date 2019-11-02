<%@ tag body-content="scriptless" pageEncoding="UTF-8" description="Ссылка на документацию"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="url" description="Конечная часть ссылки"%>
<a href="https://docs.bitel.ru/pages/viewpage.action?pageId=${url}" target="_brank"><jsp:doBody/></a>
