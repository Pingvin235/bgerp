<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<input type="hidden" name="lastModifyUserId" value="${lastModifyObject.lastModify.userId}"/>
<input type="hidden" name="lastModifyTime" value="${tu.format( lastModifyObject.lastModify.time, 'yyyy-MM-dd HH:mm:ss' )}"/>