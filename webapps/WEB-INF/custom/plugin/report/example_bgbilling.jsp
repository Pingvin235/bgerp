<%@ page import="java.util.Enumeration"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="center1020">
	<%-- 
	    Используется БД, настороенная для биллинга с ID test.
	    Пример настройки в конфигурации:
	    bgbilling:server.2.id=test
		..
		bgbilling:server.2.db.driver=com.mysql.jdbc.Driver
		bgbilling:server.2.db.url=jdbc:mysql://X.X.X.X/bgbilling_test?useUnicode=true&characterEncoding=UTF-8&connectionCollation=utf8_unicode_ci&allowUrlInLocalInfile=true&zeroDateTimeBehavior=convertToNull&jdbcCompliantTruncation=false&elideSetAutoCommits=true&cachePrepStmts=true&useCursorFetch=true&queryTimeoutKillsConnection=true
		bgbilling:server.2.db.user=root
		bgbilling:server.2.db.pswd=
	--%>
	<c:set var="dbInfo" value="${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap['test']}"/>
	
	<%-- в случае, если Slave база не настроена - будет использована обычная --%>
	<sql:query var="result" dataSource="${dbInfo.connectionPool.dataSource}">
		SELECT id, title, comment FROM contract LIMIT 100
	</sql:query>
		
	<table style="width: 100%;" class="data mt1">
		<tr>
			<td>ID</td>
			<td>${l.l('Номер')}</td>
			<td>${l.l('Комментарий')}</td>
		</tr>	

		<c:forEach var="row" items="${result.rowsByIndex}">
			<tr>
				<td>${row[0]}</td>
				<td>${row[1]}</td>
				<td>${row[2]}</td>
			</tr>			
		</c:forEach>
	</table>
</div>