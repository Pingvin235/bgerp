<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${board.lastIndex ge 0}">
	<c:set var="lastIndex" value="${board.lastIndex}"/>
	<c:set var="queues" value="${board.queues}"/>
	<c:set var="columns" value="${board.executors.size()}"/>

	<c:forEach var="index" begin="0" end="${lastIndex}">
		<c:set var="item" value="${queues[u:int(0)][index]}"/>
		<c:if test="${empty undistributed and not empty item and empty item.children and empty item.parent.process}">
			<c:set var="undistributed" value="1"/>
			<tr><td colspan="${columns}" bg-id="0"><b>${l.l('NO GROUP')}</b></td></tr>
		</c:if>
		<tr>
			<c:choose>
				<%-- common task --%>
				<c:when test="${not empty item}">
					<c:set var="colspan" value="${columns}"/>
					<%@ include file="item.jsp"%>
				</c:when>
				<%-- per executor --%>
				<c:otherwise>
					<c:forEach var="ex" items="${board.executors}">
						<c:set var="item" value="${queues[ex.id][index]}"/>
						<c:choose>
							<c:when test="${not empty item}">
								<c:set var="colspan" value="1"/>
								<%@ include file="item.jsp"%>
							</c:when>
							<c:otherwise><td>&nbsp;</td></c:otherwise>
						</c:choose>
					</c:forEach>
				</c:otherwise>
			</c:choose>
		</tr>
	</c:forEach>
</c:if>
