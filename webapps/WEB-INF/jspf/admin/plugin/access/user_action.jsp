<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<p:check action="/user/plugin/access/credential:get">
	<button class="btn-white btn-small icon"
		onclick="$$.access.get(this, ${user.id})"
		title="${l.l('Копировать логин с паролем в буфер обмена')}"><i class="ti-clipboard"></i></button>
</p:check>
