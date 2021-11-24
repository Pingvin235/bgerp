<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div>
	<b>&lt;ui:tag-box&gt;</b><br/>
	Lazy loading from AJAX request<br>
	<ui:tag-box showOptions="1" value="mail1@domain.com,mail2@domain.com" url="/user/test.do?action=enumValues"/>
</div>

<%@ include file="/WEB-INF/jspf/test.jsp"%>

<shell:title ltext="Тест title"/>
<shell:state ltext="Тест state"/>
