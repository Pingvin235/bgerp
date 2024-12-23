<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Incoming variables:
	mob - optional boolean if called from 'usermob' iface
--%>

<%-- generation table rows using JEXL --%>
<c:set var="rowExpression" value="${queue.configMap.getConfig('ru.bgcrm.model.process.config.RowExpressionConfig')}"/>

<c:set var="tableUiid" value="${u:uiid()}"/>

<table class="data" id="${tableUiid}">
	<jsp:useBean id="headData" class="java.util.HashMap"/>

	<c:set var="checkAll">
		$(this).closest('table').find('input[name=processId]').each(
			function() {
				$(this).prop('checked', !$(this).prop('checked'));
			}
		);
	</c:set>

	<c:set var="showCheckColumn" value="${not empty queue.getProcessors(ctxIface) or not empty queue.configMap.checkColumn}"/>

	<c:set target="${headData}" property="checkAllLink">
		<a href="#" onclick="${checkAll}; return false;">✓</a>
	</c:set>

	<c:if test="${not empty rowExpression}">
		<c:set var="headExpressionHtml" value="${rowExpression.getHead( 'html', headData )}"/>
	</c:if>

	<c:set var="aggregateValues" value="${frd.aggregateValues}"/>

	<c:choose>
		<c:when test="${not empty headExpressionHtml}">
			${headExpressionHtml}
		</c:when>
		<c:otherwise>
			<tr>
				<c:if test="${showCheckColumn}">
					<td width="20">${headData["checkAllLink"]}</td>
				</c:if>
				<c:forEach var="mediaColumn" items="${columnList}" varStatus="status">
					<c:set var="column" value="${mediaColumn.column}"/>

					<c:if test="${not empty column.title and column.value ne 'priority'}">
						<td>${column.title}
							<c:if test="${not empty aggregateValues and not empty aggregateValues.get(status.index)}">
								<br/>[ ${aggregateValues.get(status.index)} ]
							</c:if>
						</td>
					</c:if>
				</c:forEach>
			</tr>
		</c:otherwise>
	</c:choose>

	<c:forEach var="row" items="${frd.list}">
		<jsp:useBean id="rowData" class="java.util.HashMap"/>

		<c:set target="${rowData}" property="urgColor" value="${''}"/>

		<c:forEach begin="1" var="col" items="${row}" varStatus="status">
			<c:set var="column" value="${columnList[status.index - 1].column}"/>

			<c:if test="${column.value eq 'priority'}">
				<c:set var="priority" value="${col}"/>
				<%@ include file="/WEB-INF/jspf/process_color.jsp"%>
				<c:set var="bgcolor" value="bgcolor='${color}'"/>
				<c:set target="${rowData}" property="urgColor" value="${color}"/>
			</c:if>
		</c:forEach>

		<c:set target="${rowData}" property="process" value="${row[0][0]}"/>
		<c:set target="${rowData}" property="linkedProcess" value="${row[0][1]}"/>

		<%-- decoding html column values --%>
		<c:forEach begin="1" var="col" items="${row}" varStatus="status">
			<c:set var="mediaColumn" value="${columnList[status.index - 1]}"/>
			<c:set var="column" value="${mediaColumn.column}"/>

			<%-- process the main or a linked --%>
			<c:set var="process" value="${mediaColumn.getProcess(row[0])}"/>
			<%-- cell html value from a plugin --%>
			<c:set var="cellHtml" value="${column.cellHtml(process, col)}"/>

			<c:set target="${rowData}" property="col${column.id}">
				<c:choose>
					<c:when test="${not empty cellHtml}">
						${cellHtml}
					</c:when>

					<c:when test="${column.value eq 'N'}">
						${status_from.count}
					</c:when>

					<c:when test="${column.value eq 'id' and not mob}">
						<ui:process-link id="${process.id}"/>
					</c:when>

					<c:when test="${column.value.startsWith('linkCustomerLink') or
									column.value.startsWith('linkedCustomerLink')}">
						<c:forEach var="customer" items="${col.split('$')}" varStatus="status">
							<c:set var="customerId" value="${customer.split(':')[0]}"/>
							<c:set var="customerTitle" value="${customer.split(':')[1]}"/>
							<c:choose>
								<c:when test="${mob}">${customerTitle}</c:when>
								<c:otherwise><a href="#" onclick="$$.customer.open(${customerId}); return false;">${customerTitle}</a></c:otherwise>
							</c:choose>
							<c:if test="${not status.last}">,</c:if>
						</c:forEach>
					</c:when>

					<c:when test="${column.value.startsWith('linkedObject:process')}">
						<c:forEach var="processId" items="${col.split(',' )}">
							<c:choose>
								<c:when test="${mob}">${processId}</c:when>
								<c:otherwise><ui:process-link id="${processId}"/></c:otherwise>
							</c:choose>
						</c:forEach>
					</c:when>

					<%-- TODO: Код этот должен быть по-правильному в плагине BGBilling --%>
					<c:when test="${column.value.startsWith('linkObject:contract' ) or
									column.value.startsWith('linkedObject:contract' )}">
						<c:forEach var="contractInfo" items="${col.split(',' )}">
							<c:set var="info" value="${contractInfo.split(':' )}"/>
							<c:choose>
								<c:when test="${mob}">${info[2]}</c:when>
								<c:otherwise><a href="#" onclick="$$.bgbilling.contract.open( '${info[0]}', '${info[1]}' ); return false;">${info[2]}</a></c:otherwise>
							</c:choose>
						</c:forEach>
					</c:when>

					<c:when test="${column.value eq 'actions'}">
						<c:set var="actionShowMode" value="${queue.configMap.actionShowMode}"/>

						<c:forEach var="action" items="${queue.actionList}">
							<c:if test="${action.statusIds.contains(process.statusId)}">
								<c:url var="url" value="/user/process.do">
									<c:param name="id" value="${process.id}"/>
									<c:param name="method" value="processDoCommands"/>
									<c:param name="commands" value="${action.commands}"/>
								</c:url>

								<c:choose>
									<c:when test="${actionShowMode eq 'buttons'}">
										<button class="btn-white btn-small" onclick="$$.ajax.post('${url}');" title="${action.title}" style="${action.style}">${action.shortcut}</button>
									</c:when>
									<c:otherwise>
										<a href="#" onclick="$$.ajax.post('${url}'); return false;" style="${action.style}">${action.title}</a><br/>
									</c:otherwise>
								</c:choose>
							</c:if>
						</c:forEach>
					</c:when>

					<c:when test="${column.value.startsWith('status')}">
						${col.replace('; ','</br>')}
					</c:when>

					<c:when test="${column.value.startsWith('linkProcessList') or column.value.startsWith('linkedProcessList')}">
						<div style="display: table">
							<c:forEach var="lp" items="${col}">
								<div style="display: table-row" class="in-table-cell in-pb05 in-pl05">
									<div><ui:process-link id="${lp.id}"/></div>
									<div>${lp.typeTitle}</div>
									<div><b>${lp.statusTitle}</b></div>
								</div>
							</c:forEach>
						</div>
					</c:when>

					<c:otherwise>
						<c:set var="title" value=""/>
						<c:if test="${0 lt column.titleIfMore and column.titleIfMore lt col.toString().length()}">
							<c:set var="title">title="${u.escapeXml( col )}"</c:set>
							<c:set var="col">${col.substring(0, column.titleIfMore)}...</c:set>
						</c:if>

						<c:if test="${column.formatToHtml}">
							<c:set var="col" value="${u:htmlEncode(col)}"/>
						</c:if>

						<span ${title}>
							<c:choose>
								<c:when test="${0 lt column.cutIfMore}">
									<c:set var="maxLength" value="${column.cutIfMore}"/>
									<c:if test="${maxLength gt 0}">
										<ui:short-text text="${col}" maxLength="${maxLength}"/>
									</c:if>
								</c:when>
								<c:when test="${not empty column.showAsLink and not empty col}">
									<a href="${col}" target="_blank">
										<c:choose>
											<c:when test="${column.showAsLink eq 'linkUrl'}">${col}</c:when>
											<c:otherwise>${column.showAsLink}</c:otherwise>
										</c:choose>
									</a>
								</c:when>
								<c:when test="${column.value eq 'descriptionLink'}">
									<ui:process-link process="${process}" text="${col}"/>
								</c:when>
								<c:otherwise>${col}</c:otherwise>
							</c:choose>
						</span>
					</c:otherwise>
				</c:choose>
			</c:set>
		</c:forEach>

		<c:remove var="rowExpressionHtml"/>
		<c:if test="${not empty rowExpression}">
			<c:set var="rowExpressionHtml" value="${rowExpression.getRow('html', rowData)}"/>
		</c:if>

		<c:set var="process" value="${row[0][0]}"/>

		<c:choose>
			<c:when test="${not empty rowExpressionHtml}">
				${rowExpressionHtml}
			</c:when>
			<c:otherwise>
				<c:set var="openProcessId">
					<ui:when type="open">
						<c:if test="${ctxSetup.getConfig('org.bgerp.action.open.ProcessAction$Config').isOpen(process, form)}">${process.id}</c:if>
					</ui:when>
				</c:set>

				<tr ${bgcolor} processId="${process.id}" openProcessId="${openProcessId}">
					<c:set var="onceFlag" value="0"/>
					<c:forEach begin="1" var="col" items="${row}" varStatus="status">
						<c:set var="mediaColumn" value="${columnList[status.index - 1]}"/>
						<c:set var="column" value="${mediaColumn.column}"/>

						<c:set var="bgcolor" value=""/>
						<c:set var="style" value=""/>
						<c:set var="show" value="${column.value ne 'priority'}"/>

						<c:if test="${not empty column.style}">
							<c:set var="style">style="${column.style}"</c:set>
						</c:if>

						<c:if test="${showCheckColumn and onceFlag ne '1'}">
							<c:set var="onceFlag" value="1"/>
							<td align="center"><input type="checkbox" name="processId" value="${process.id}"/></td>
						</c:if>
						<c:if test="${show}">
							<td ${style}>${rowData['col'.concat(column.id)]}</td>
						</c:if>
					</c:forEach>
				</tr>
			</c:otherwise>
		</c:choose>
	</c:forEach>
</table>
