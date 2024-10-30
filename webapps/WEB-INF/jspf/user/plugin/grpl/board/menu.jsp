<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="cell" value="${frd.cell}"/>
<c:set var="board" value="${cell.row.board}"/>
<c:set var="date" value="${tu.format(cell.row.date, 'ymd')}"/>

<c:choose>
	<c:when test="${empty cell.slots}">
		<c:forEach var="group" items="${cell.freeGroups}">
			<li><a href="#"
					onclick="$$.grpl.menuClick('${form.requestURI}', ${board.id}, '${date}', ${cell.columnId}, ${group.id}, '${form.returnUrl}', '${form.returnChildUiid}'); return false;">${group.title}</a>
			</li>
		</c:forEach>
		<li><a href="#" onclick="if ($$.confirm.del()) {
			$$.grpl.menuClick('${form.requestURI}', ${board.id}, '${date}', ${cell.columnId}, 0, '${form.returnUrl}', '${form.returnChildUiid}')
		}; return false;"><i class="ti-eraser"></i> ${l.l('Clear Group')}</a></li>
	</c:when>
	<c:otherwise>
		processes
	</c:otherwise>
</c:choose>
