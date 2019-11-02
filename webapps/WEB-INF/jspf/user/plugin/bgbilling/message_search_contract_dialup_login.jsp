<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<input type="text" placeholder="Логин либо алиас" title="Подстрока, минимум 3 символа" name="login" 
		size="10" class="mr1" value="${form.param.login}" style="width: 100%;" ${searchOnEnter}/>

