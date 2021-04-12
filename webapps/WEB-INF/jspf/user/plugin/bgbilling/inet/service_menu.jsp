<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<li confirm="Вы уверены, что хотите включить сервис на устройстве?" command="action=serviceStateModify&state=1"><a href="#" onclick="return false;">Включить (отладка)</a></li>
<li confirm="Вы уверены, что хотите отключить сервис на устройстве?" command="action=serviceStateModify&state=0"><a href="#" onclick="return false;">Отключить (отладка)</a></li>
<%-- 
<li confirm="Вы уверены, что хотите синхронизировать сервис на устройстве?" command="action=serviceStateModify&state=1"><a href="#" onclick="return false;">Синхронизировать (отладка)</a></li>
--%>
<c:forEach var="item" items="${form.response.data.deviceMethods}">
   <li command="action=serviceDeviceManage&deviceId=${form.param.deviceId}&operation=${item.method}"><a>${item.title}</a></li>
</c:forEach>