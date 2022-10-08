<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div id="loginErrorMessage" style="color: #FF0000;"></div>
<input name="j_username" type="text" placeholder="${l.l('Логин')}" class="w100p"/>
<input name="j_password" type="password" placeholder="${l.l('Пароль')}" class="w100p"/>
<button type="submit" class="mt05 btn-grey w100p">${l.l('Войти')}</button>
