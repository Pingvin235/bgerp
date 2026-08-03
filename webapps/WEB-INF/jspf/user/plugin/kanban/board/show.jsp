<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="queues" value="${frd.queues}"/>

<c:choose>
	<c:when test="${empty queues}">
		${l.l("The user doesn't have allowed queues")}
	</c:when>
	<c:otherwise>
		<c:set var="queue" value="${frd.queue}"/>

		<c:set var="queueSelectUiid" value="${u:uiid()}"/>
		<ui:combo-single id="${queueSelectUiid}"
			value="${queue.id}"
			widthTextValue="18em"
			prefixText="${l.l('Очередь')}:"
			list="${queues}"
			onSelect="$$.ajax.loadContent('/user/plugin/kanban/board.do?queueId=' + this.value, this)"/>

		<shell:state moveSelector="#${queueSelectUiid}"/>

		<c:choose>
			<c:when test="${empty queue}">
				${l.l('No access to the queue')}
			</c:when>
			<c:otherwise>
				<c:set var="types" value="${frd.types}"/>
				<c:set var="selectedTypeId" value="${form.param.typeId}"/>
				<c:set var="columns" value="${frd.columns}"/>
				<c:set var="cardsByStatus" value="${frd.cardsByStatus}"/>

				<div class="mt1 mb1"><%@ include file="filter.jsp"%></div>

				<c:choose>
					<c:when test="${selectedTypeId le 0}">
						${l.l('Select a process type')}
					</c:when>
					<c:otherwise>
						<c:set var="uiid" value="${u:uiid()}"/>
						<c:set var="canDrag" value="${ctxUser.checkPerm('/user/process:processStatusUpdate')}"/>
						<c:set var="previewEnabled" value="${frd.previewEnabled}"/>

						<div id="${uiid}" class="kanban-board">
							<c:forEach var="status" items="${columns}" varStatus="colLoop">
								<c:set var="processes" value="${cardsByStatus[status.id]}"/>
								<div class="kanban-column" bg-status-id="${status.id}">
									<div class="kanban-column-head" style="background-color: ${kanbanConfig.getColor(queue.id, status.id, colLoop.index)}">
										${status.title} <span class="kanban-count">[${processes.size()}]</span>
									</div>
									<div class="kanban-column-body">
										<c:forEach var="process" items="${processes}">
											<%@ include file="card.jsp"%>
										</c:forEach>
									</div>
								</div>
							</c:forEach>
						</div>

						<script>
							$(function () {
								$$.kanban.initBoard($('#${uiid}'));
							});
						</script>
					</c:otherwise>
				</c:choose>
			</c:otherwise>
		</c:choose>
	</c:otherwise>
</c:choose>

<shell:title text="${l.l('Kanban')}${not empty queue ? ': '.concat(queue.title) : ''}"/>
