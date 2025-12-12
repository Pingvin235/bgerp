<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:sc>
	<c:set var="uiid" value="${u:uiid()}"/>
	<div style="background-color: ${process.priorityColor}; padding-bottom: 0;" class="box">
		<div id="${uiid}" class="pb05">
			<ui:when type="user">
				<c:set var="cloneAllowed" value="${ctxUser.checkPerm('/user/process:processClone')}"/>
				<c:set var="mergeAllowed" value="${ctxUser.checkPerm('/user/process:processMerge')}"/>
				<c:set var="deleteAllowed" value="${ctxUser.checkPerm('/user/process:processDelete')}"/>

				<c:if test="${cloneAllowed || mergeAllowed || deleteAllowed}">
					<c:set var="uiidMenu" value="${u:uiid()}"/>
					<ui:popup-menu id="${uiidMenu}">
						<c:if test="${cloneAllowed}">
							<c:url var="url" value="/user/process.do">
								<c:param name="method" value="processClone"/>
								<c:param name="id" value="${process.id}"/>
							</c:url>
							<li>
								<a href="#" onclick="if (confirm('${l.l('Clone process')}?'))
									$$.ajax.post('${url}').done((result) => {
										$$.process.open(result.data.process.id);
									}); return false;">
									<i class="ti-layers"></i>
									${l.l('Clone process')}
								</a>
							</li>
						</c:if>

						<c:if test="${mergeAllowed}">
							<c:url var="url" value="/user/process.do">
								<c:param name="method" value="processMergeEdit"/>
								<c:param name="returnUrl" value="${requestUrl}"/>
								<c:param name="returnChildUiid" value="${tableId}"/>
								<c:param name="id" value="${process.id}"/>
							</c:url>
							<li>
								<a href="#" onclick="$$.ajax.load('${url}', $('#${uiid}')); return false;">
									<i class="ti-shift-right"></i>
									${l.l('Слить в существующий')}
								</a>
							</li>
						</c:if>

						<c:if test="${deleteAllowed}">
							<c:url var="url" value="/user/process.do">
								<c:param name="method" value="processDelete"/>
								<c:param name="id" value="${process.id}"/>
							</c:url>
							<li>
								<a href="#" onclick="if (confirm('${l.l('Удалить процесс')}?')) $$.ajax.post('${url}').done(() => { ${returnBreakCommand} }); return false;">
									<i class="ti-trash"></i>
									${l.l('Удалить процесс')}
								</a>
							</li>
						</c:if>
					</ui:popup-menu>

					<c:set var="uiidMenuLink" value="${u:uiid()}"/>
					[<a href="#" id="${uiidMenuLink}">...</a>]
					<script>
						$(() => {
							$$.ui.menuInit($("#${uiidMenuLink}"), $("#${uiidMenu}"), "left");
						})
					</script>
				</c:if>
			</ui:when>

			<%@ include file="process_header_type.jsp"%>

			<ui:when type="user">
				<u:sc>
					<c:set var="processTmp" value="${process}" scope="page"/>
					<c:remove var="process" scope="request"/>
					(<ui:process-link id="${process.id}"/>)
					<c:set var="process" value="${processTmp}" scope="request"/>
				</u:sc>

				<p:check action="/user/process:processPriorityUpdate">
					<c:if test="${processType.properties.configMap['hidePriority'] ne 1}">
						<c:url var="url" value="/user/process.do">
							<c:param name="method" value="processPriorityEdit"/>
							<c:param name="returnUrl" value="${requestUrl}"/>
							<c:param name="returnChildUiid" value="${tableId}"/>
							<c:param name="id" value="${process.id}"/>
							<c:param name="priority" value="${process.priority}"/>
						</c:url>
						[<a href="#" onclick="$$.ajax.load('${url}', $('#${uiid}')); return false;">${l.l('priority')}</a>]
					</c:if>
				</p:check>

				<p:check action="/user/process:unionLog">
					<c:url var="logUrl" value="/user/process.do">
						<c:param name="method" value="unionLog"></c:param>
						<c:param name="id" value="${form.id}"></c:param>
						<c:param name="type" value="process"></c:param>
						<c:param name="objectType" value="${form.param.objectType}"></c:param>
						<c:param name="returnUrl" value="${requestUrl}"></c:param>
					</c:url>
					<c:if test="${not empty processType}">
						[<a href="#" onclick="$$.ajax.load('${logUrl}', $('#${tableId}').parent()); return false;">${l.l('log')}</a>]
					</c:if>
				</p:check>
			</ui:when>
		</div>

		<div class="pb05">
			${l.l('Created')}: ${tu.format( process.createTime, 'ymdhms' )}
			<c:if test="${process.createUserId gt 0}">
				(<ui:user-link id="${process.createUserId}"/>)
			</c:if>
		</div>
		<div class="pb05">
			<%@ include file="process_status_current.jsp"%>
		</div>
		<c:if test="${process.closeUserId gt 0}">
			<div class="pb05">Закрыт: ${tu.format(process.closeTime, 'ymdhms')} (<ui:user-link id="${process.closeUserId}"/>)</div>
		</c:if>
	</div>
</u:sc>