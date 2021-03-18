<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="result" value="${stepData.contracts}"/>

<c:if test="${not empty result}">
	<table class="data" width="100%">
	<tr>
		<td>ID</td>
		<td>${l.l('Договор')}</td>
		<td>${l.l('Комментарий')}</td>
		<td>${l.l('Дата заключения')}</td>
		<td>${l.l('Баланс')}</td>
		<td>${l.l('Тариф')}</td>
		<td>${l.l('Статус')}</td>
	</tr>
		<c:forEach var="db" items="${result}" >
			<c:set var="dbResult" value="${db.value}"/>
			
			<c:if test="${not empty dbResult}">
				<c:set var="status">
					<x:out select="$dbResult/data/@status"/>
				</c:set>	
			
				<c:choose>
					<c:when test="${status eq 'ok'}">
						
						<x:set var="items" select="$dbResult/data/contracts/item"/>
						
						<x:forEach var="item" select="$items">
							<tr>
								<td><x:out select="$item/@id"/></td>
								<td><x:out select="$item/@contractTitle"/></td>
								<td><x:out select="$item/@comment"/></td>
								<td><x:out select="$item/@conclusionDate"/></td>
								<td><x:out select="$item/@balance"/></td>
								<td><x:out select="$item/@tariff"/></td>
								<td><x:out select="$item/@status"/> <x:out select="$item/@groups"/></td>
							</tr>
						</x:forEach>
						
					</c:when>
					<c:otherwise>
						<c:set var="error">
							<x:out select="$dbResult/data/text()"/>
						</c:set>
						<h2>Биллинг: ${db.title}</h2>
						<%@include file="/WEB-INF/jspf/error_div.jsp"%>
					</c:otherwise>
				</c:choose>				
			</c:if>
		</c:forEach>
		</table>
</c:if>