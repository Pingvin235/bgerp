<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%-- required for recognition of messages, belong to the process --%>
${form.setParam('processId', form.id.toString())}
<%@ include file="/WEB-INF/jspf/user/message/process/list/list.jsp"%>
