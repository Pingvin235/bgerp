<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%-- UI functions with localized texts --%>

$$.confirm = {
	del: () => {
		return confirm('${l.l('Вы уверены, что хотите удалить?')}');
	}
}