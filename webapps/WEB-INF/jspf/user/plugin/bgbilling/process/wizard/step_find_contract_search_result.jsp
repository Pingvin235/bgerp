<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<option value="-1">---</option>

<c:if test="${not empty result}">
	<c:forEach items="${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoList}" var="db">
		<c:set var="dbResult" value="${result[db.id]}"/>
		
		<c:if test="${not empty dbResult}">
			<c:set var="status">
				<x:out select="$dbResult/data/@status"/>
			</c:set>	
		
			<c:choose>
				<c:when test="${status eq 'ok'}">
					<c:set var="itemCount">
						<x:out select="$dbResult/data/contracts/@recordCount"/>
					</c:set>
					
					<x:set var="items" select="$dbResult/data/contracts/item"/>
					
					<c:set var="truncItemCount">
						<x:out select="count($items)"/>
					</c:set>
					
					<%-- может быть при поиске по ID, там возвращается одна запись только --%>
					<c:if test="${empty itemCount}">
						<c:set var="itemCount" value="${truncItemCount}"/>
					</c:if>
											
					<c:if test="${itemCount gt 0}">
							<x:forEach select="$items" var="item">
								<c:set var="id">
									<x:out select="$item/@id"/>
								</c:set>
									
								<option value="${id}"><x:out select="$item/@title"/></option>
								
							</x:forEach>
					</c:if>	
				</c:when>
				<c:otherwise>
					<c:set var="error">
						<x:out select="$dbResult/data/text()"/>
					</c:set>
					<%@include file="/WEB-INF/jspf/error_div.jsp"%>
				</c:otherwise>
			</c:choose>				
		</c:if>
	</c:forEach>
</c:if>	