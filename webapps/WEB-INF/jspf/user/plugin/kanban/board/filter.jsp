<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="submitScript">$$.ajax.loadContent(this.form);</c:set>

<form action="/user/plugin/kanban/board.do" id="kanbanFilterForm" class="in-inline-block">
	<input type="hidden" name="queueId" value="${queue.id}"/>
	<%-- required by the shared $$.process.queue.filter.showSelected() - it writes the
	     currently-toggled filter IDs here, it does not read/restore them from this value here --%>
	<input type="hidden" name="selectedFilters" value=""/>

	<c:set var="filters" scope="request" value=""/>

	<c:set var="selectedFiltersStr" value="${ctxUser.pers['kanbanSelectedFilters'.concat(queue.id)]}"/>
	<c:set var="selectedFilters" value="${u.toIntegerSet(selectedFiltersStr)}"/>

	<c:set var="valuesHtml">
		<c:forEach var="filterFromList" items="${queue.filterList.filterList}">
			<c:if test="${filterFromList.type == 'type' and types.size() gt 1}">
				<c:set var="filter" value="${filterFromList}"/>
				<c:set var="title" value="${l.l('Type')}"/>
				<c:set var="code">
					<ui:combo-single name="typeId" value="${selectedTypeId}" prefixText="${l.l('Type')}:" widthTextValue="16em"
						showFilter="true" onChange="${submitScript}">
						<jsp:attribute name="valuesHtml">
							<c:forEach var="t" items="${types}">
								<li value="${t.id}">${t.title}</li>
							</c:forEach>
						</jsp:attribute>
					</ui:combo-single>
				</c:set>
				<%@ include file="/WEB-INF/jspf/user/process/queue/filter/item.jsp"%>
			</c:if>

			<c:if test="${filterFromList.type == 'openClose'}">
				<c:set var="filter" value="${filterFromList}"/>
				<c:set var="title" value="${l.l('Closed')}"/>
				<c:set var="value" value="${not empty form.param.openClose ? form.param.openClose : filterFromList.defaultValue}"/>
				<c:set var="code">
					<ui:combo-single value="${value}" name="openClose" prefixText="${l.l('Closed')}:" widthTextValue="60px" onChange="${submitScript}">
						<jsp:attribute name="valuesHtml">
							<li value="none">${l.l('All')}</li>
							<li value="open">${l.l('No')}</li>
							<li value="close">${l.l('Yes')}</li>
						</jsp:attribute>
					</ui:combo-single>
				</c:set>
				<%@ include file="/WEB-INF/jspf/user/process/queue/filter/item.jsp"%>
			</c:if>

			<c:if test="${filterFromList.type == 'status' and selectedTypeId gt 0}">
				<c:set var="filter" value="${filterFromList}"/>
				<c:set var="title" value="${l.l('Status')}"/>
				<c:set var="code">
					<ui:combo-check name="status" list="${columns}" map="${ctxProcessStatusMap}"
						values="${form.getParamValues('status')}"
						prefixText="${l.l('Status')}:" widthTextValue="8em"/>
				</c:set>
				<%@ include file="/WEB-INF/jspf/user/process/queue/filter/item.jsp"%>
			</c:if>

			<c:if test="${filterFromList.getClass().simpleName eq 'FilterParam'}">
				<c:set var="filter" value="${filterFromList}"/>
				<c:set var="paramType" value="${filter.parameter.type}"/>
				<c:set var="title" value="${not empty filter.title ? filter.title : filter.parameter.title}"/>

				<c:if test="${paramType == 'list' or paramType == 'listcount'}">
					<c:set var="code">
						<ui:combo-check name="param${filter.parameter.id}value" list="${filter.parameter.listParamValues}"
							values="${form.getParamValues('param'.concat(filter.parameter.id).concat('value'))}"
							showFilter="true" prefixText="${title}:" widthTextValue="10em"/>
					</c:set>
					<%@ include file="/WEB-INF/jspf/user/process/queue/filter/item.jsp"%>
				</c:if>
				<c:if test="${paramType == 'text' or paramType == 'blob'}">
					<c:set var="code">
						<input type="text" name="param${filter.parameter.id}value" value="${form.param['param'.concat(filter.parameter.id).concat('value')]}"
							placeholder="${title}" size="16" onkeypress="if ($$.keys.enterPressed(event)) { ${submitScript} return false; }"/>
					</c:set>
					<%@ include file="/WEB-INF/jspf/user/process/queue/filter/item.jsp"%>
				</c:if>
			</c:if>

			<c:if test="${filterFromList.type == 'close_date'}">
				<c:set var="filter" value="${filterFromList}"/>
				<c:set var="title" value="${l.l('Дата закрытия')}"/>
				<c:set var="code">
					${l.l('Закрыт с')}&nbsp;<ui:date-time name="dateCloseFrom" value="${form.param.dateCloseFrom}"/>
					${l.l('по')}&nbsp;<ui:date-time name="dateCloseTo" value="${form.param.dateCloseTo}"/>
				</c:set>
				<%@ include file="/WEB-INF/jspf/user/process/queue/filter/item.jsp"%>
			</c:if>

			<c:if test="${filterFromList.type == 'create_date'}">
				<c:set var="filter" value="${filterFromList}"/>
				<c:set var="title" value="${l.l('Дата создания')}"/>
				<c:set var="code">
					${l.l('Создан с')}&nbsp;<ui:date-time name="dateCreateFrom" value="${form.param.dateCreateFrom}"/>
					${l.l('по')}&nbsp;<ui:date-time name="dateCreateTo" value="${form.param.dateCreateTo}"/>
				</c:set>
				<%@ include file="/WEB-INF/jspf/user/process/queue/filter/item.jsp"%>
			</c:if>

			<c:if test="${filterFromList.type == 'description'}">
				<c:set var="filter" value="${filterFromList}"/>
				<c:set var="title" value="${l.l('Description')}"/>
				<c:set var="code">
					<input type="text" name="description" value="${form.param.description}" placeholder="${title}" size="20"
						onkeypress="if ($$.keys.enterPressed(event)) { ${submitScript} return false; }"/>
				</c:set>
				<%@ include file="/WEB-INF/jspf/user/process/queue/filter/item.jsp"%>
			</c:if>
		</c:forEach>
	</c:set>

	<c:set var="selectorId" value="${u:uiid()}"/>
	<c:set var="onChange" value="$$.process.queue.filter.showSelected('${selectorId}', '#kanbanFilterForm')"/>
	<ui:combo-check id="${selectorId}" valuesHtml="${valuesHtml}" onChange="${onChange}" prefixText="${l.l('Фильтры')}:" styleClass="mr1 filtersSelect" widthTextValue="5em"/>
	<script style="display: none;">
		$(function () { ${onChange} })
	</script>

	<%-- the "filters" variable is concatenated in process/queue/filter/item.jsp --%>
	${filters}

	<script style="display: none;">
		$(function () { processQueueMarkFilledFilters($('#kanbanFilterForm')); })
	</script>

	<c:if test="${queue.sortSet.comboCount gt 0}">
		<c:forEach begin="1" end="${queue.sortSet.comboCount}" step="1" varStatus="status">
			<c:set var="value" value="0"/>
			<c:forEach var="mode" items="${queue.sortSet.modeList}" varStatus="statusItem">
				<c:if test="${queue.sortSet.defaultSortValues[status.count] eq statusItem.count}">
					<c:set var="value" value="${mode.orderExpression}"/>
				</c:if>
			</c:forEach>
			<c:if test="${not empty form.param.sort}">
				<c:set var="value" value="${form.param.sort}"/>
			</c:if>

			<ui:combo-single value="${value}" name="sort" prefixText="${l.l('Сорт.')}:" widthTextValue="60px" onChange="${submitScript}">
				<jsp:attribute name="valuesHtml">
					<li value="0">- ${l.l('нет')} -</li>
					<c:forEach var="mode" items="${queue.sortSet.modeList}">
						<li value="${mode.orderExpression}">${mode.title}</li>
					</c:forEach>
				</jsp:attribute>
			</ui:combo-single>
		</c:forEach>
	</c:if>

	<ui:button type="out" title="${l.l('Apply')}" onclick="${submitScript}" styleClass="ml1"/>
</form>
