<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<shell:title text="${l.l('Очереди процессов')}"/>

<div id="processQueueShow" >
	<table style="width: 100%;"><tr>
		<td nowrap="nowrap">
			<c:choose>
				<c:when test="${not empty frd.list}">
					<c:set var="config" value="${ctxUser.personalizationMap.getConfig('ru.bgcrm.model.process.queue.config.SavedPanelConfig')}"/>
					<c:set var="savedPanelMap" value="${config.savedPanelSet}"/>
					<c:set var="valuesHtml">
						<c:forEach items="${frd.list}" var="item">
							<c:if test="${not savedPanelMap.contains(item.id)}">
								<li value="${item.id}">
									<div>${item.title}</div><div class="icon-add" onclick="event.stopPropagation(); addToPanelScript('${item.id}','${item.title}',true);"></div>
								</li>
							</c:if>
						</c:forEach>
					</c:set>

					<c:set var="id" value="processQueueSelect"/>

					<ui:combo-single id="${id}"
						value="${ctxUser.personalizationMap['queueLastSelected']}"
						onSelect="$$.process.queue.updateSelected(this.value); $$.process.queue.showSelected(this.value);"
						widthTextValue="18em"
						prefixText="${l.l('Очередь')}:"
						valuesHtml="${valuesHtml}"/>

					<c:set var="queueId" value="${form.id}"/>
					<c:if test="${not (queueId gt 0)}">
						<c:set var="queueId" value="${ctxUser.personalizationMap['queueLastSelected']}"/>
					</c:if>

					<script>
						$(function () {
							$('#${id}').appendTo($$.shell.$state());

							<c:forEach items="${frd.list}" var="item">
								<c:if test="${savedPanelMap.contains(item.id)}">
									addToPanelScript('${item.id}', '${item.title}');
								</c:if>
							</c:forEach>

							$$.process.queue.showSelected(${queueId});
						})
					</script>
				</c:when>
				<c:otherwise>
					${l.l("The user doesn't have allowed queues")}
				</c:otherwise>
			</c:choose>
		</td>
	</table>

	<div id="processQueueFilter" class="tableIndent">
		<%-- сюда динамически грузится фильтр --%>
	</div>

	<div id="processQueueData">
		<%-- сюда динамически грузится вывод очереди --%>
	</div>
</div>


<html:form action="/user/process" styleId="processQueueCreateProcess" style="display: none; width: 50%;" styleClass="center1020">
	<input type="hidden" name="action" value="processCreate"/>

	<div id="typeTree">
		<%-- сюда динамически грузится дерево типов для очереди --%>
	</div>
	<div id="groupSelect">
		<%-- сюда динамически грузятся группы решения --%>
	</div>
	<div id="constPart" class="mt05">
		<b>${l.l('Description')}:</b><br/>
		<textarea name="description" rows="10" style="width: 100%;"></textarea>

		<c:set var="returnToShow">$('#processQueueCreateProcess').hide(); $('#processQueueShow').show();</c:set>
		<c:set var="saveCommand">
			var result = sendAJAXCommand( formUrl( this.form ) ); if( result ){ ${returnToShow} $$.process.open( result.data.process.id ); };
		</c:set>

		<div class="mt1 in-mr1">
			<button class="btn-grey" type="button" onclick="${saveCommand}">OK</button>
			<button class="btn-white" type="button" onclick="${returnToShow}">${l.l('Отмена')}</button>
		</div>
	</div>
</html:form>