<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<button class="btn-grey" style="width: 100%;" onclick="$$.ajax.post('/login.do?method=logout').done(() => window.location.href = '/usermob'); return false;">${l.l('Выйти')}</button>