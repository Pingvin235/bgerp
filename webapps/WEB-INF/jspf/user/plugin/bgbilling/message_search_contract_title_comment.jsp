<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<input type="text" placeholder="Номер" title="Подстрока с номером, минимум 3 символа" name="title" 
		size="10" class="mr1" value="${form.param.title}" ${searchOnEnter}/>
<input type="text" placeholder="Комментарий" title="Подстрока с комментарием, минимум 3 символа" name="comment" 
		size="10" value="${form.param.comment}" ${searchOnEnter}/>
