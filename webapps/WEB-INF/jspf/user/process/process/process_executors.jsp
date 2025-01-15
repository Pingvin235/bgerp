<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:sc>
	<div>
		<c:set var="uiid" value="${u:uiid()}"/>
		<div class="mt1 mb05" id="${uiid}">
			<h2>${l.l('Группы / исполнители')}
				<span class="normal">
					<p:check action="ru.bgcrm.struts.action.ProcessAction:processGroupsUpdate">
						<c:url var="url" value="/user/process.do">
							<c:param name="method" value="processGroupsEdit"/>
							<c:param name="id" value="${process.id}"/>
							<c:param name="returnUrl" value="${requestUrl}"/>
							<c:param name="returnChildUiid" value="${tableId}"/>
						</c:url>
						<c:if test="${not empty processType}">
							[<a href="#" onclick="$$.ajax.load('${url}', $('#${uiid}').parent()); return false;">${l.l('группы')}</a>]
						</c:if>
					</p:check>

					<p:check action="ru.bgcrm.struts.action.ProcessAction:processExecutorsUpdate">
						<c:url var="url" value="/user/process.do">
							<c:param name="method" value="processExecutorsEdit"/>
							<c:param name="id" value="${process.id}"/>
							<c:param name="returnUrl" value="${requestUrl}"/>
							<c:param name="returnChildUiid" value="${tableId}"/>
						</c:url>
						<c:if test="${not empty processType}">
							[<a href="#" onclick="$$.ajax.load('${url}', $('#${uiid}').parent()); return false;">${l.l('исполнители')}</a>]
						</c:if>
					</p:check>

					<c:if test="${ctxUser.checkPerm('ru.bgcrm.struts.action.ProcessAction:processExecutorsSwap') and process.getGroups().size() eq 2}">
						<c:url var="url" value="/user/process.do">
							<c:param name="id" value="${process.id}"/>
							<c:param name="method" value="processExecutorsSwap"/>
						</c:url>
						[<a href="#" onclick="$$.ajax.post('${url}').done(() => { $$.ajax.load('${requestUrl}', $('#${tableId}').parent()) }); return false;">eswap</a>]
					</c:if>
				</span>
			</h2>
		</div>

		<c:remove var="emptyExecutors"/>
		<div class="pl1 in-mb05">
			<c:forEach var="role" items="${ctxUserGroupRoleList}">
				<c:forEach var="group" items="${ctxUserGroupList}" varStatus="status">
					<c:remove var="groupIs"/>
					<c:forEach  var="processGroup" items="${process.groups}">
						<c:if test="${processGroup.groupId eq group.id and processGroup.roleId eq role.id}">
							<c:set var="groupIs" value="1"/>
						</c:if>
					</c:forEach>

					<c:if test="${not empty groupIs}">
						<div>
							<c:choose>
								<c:when test="${role.id eq '0'}"><b title="ID: ${group.id}">${ctxUserGroupMap[group.id]}: </b></c:when>
								<c:otherwise><b title="ID: ${group.id}">${ctxUserGroupMap[group.id]} (<span title="ID: ${role.id}">${ctxUserGroupRoleMap[role.id].title}</span>): </b></c:otherwise>
							</c:choose>

							<%-- в списке есть текущий пользователь --%>
							<c:set var="currentPresented" value="false"/>

							<c:set var="executors" value=""/>
							<c:forEach var="executor" items="${process.executors}" varStatus="status">
								<c:if test="${group.id == executor.groupId and role.id == executor.roleId}">
									<c:set var="executors"><%--
									--%>${executors}<%--
									--%><c:if test="${not empty executors}">, </c:if><%--
									--%><ui:user-link id="${executor.userId}"/><%--
								--%></c:set>
									<c:set var="currentPresented" value="${currentPresented or executor.userId eq ctxUser.id}"/>
								</c:if>
								<c:if test="${executor.groupId eq 0}">
									<c:set var="emptyExecutors" value="1" />
								</c:if>
							</c:forEach>

							${executors}

							<c:choose>
								<c:when test="${currentPresented}">
									<%-- полный список исполнителей без текущего в данной группе и роли --%>
									<c:url var="url" value="/user/process.do">
										<c:param name="id" value="${process.id}"/>
										<c:param name="method" value="processExecutorsUpdate"/>
										<c:param name="group">${group.id}:${role.id}</c:param>
										<c:forEach var="executor" items="${process.executors}">
											<c:if test="${group.id eq executor.groupId and role.id eq executor.roleId and executor.userId ne ctxUser.id}">
												<c:param name="executor">${executor.userId}:${executor.groupId}:${executor.roleId}</c:param>
											</c:if>
										</c:forEach>
									</c:url>
									[<a href="#"
										title="${l.l('Удалить меня из исполнителей')}"
										onclick="if (confirm('${l.l('Удалить вас из исполнителей')}?')) $$.ajax.post('${url}').done(() => $$.ajax.load('${requestUrl}', $('#${tableId}').parent())); return false;">${l.l('-Я')}</a>]
								</c:when>
								<c:otherwise>
									<%-- текущий пользователь есть в этой группе --%>
									<c:if test="${ctxUser.groupIds.contains(group.id)}">
										<%-- полный список исполнителей в данной группе и роли --%>
										<c:url var="url" value="/user/process.do">
											<c:param name="id" value="${process.id}"/>
											<c:param name="method" value="processExecutorsUpdate"/>
											<c:param name="group">${group.id}:${role.id}</c:param>
											<c:forEach var="executor" items="${process.executors}">
												<c:if test="${group.id eq executor.groupId and role.id eq executor.roleId}">
													<c:param name="executor">${executor.userId}:${executor.groupId}:${executor.roleId}</c:param>
												</c:if>
											</c:forEach>
											<c:param name="executor">${ctxUser.id}:${group.id}:${role.id}</c:param>
										</c:url>
										[<a href="#"
											title="${l.l('Добавить меня в исполнители')}"
											onclick="if (confirm('${l.l('Добавить вас в исполнители')}?')) $$.ajax.post('${url}').done(() => $$.ajax.load('${requestUrl}', $('#${tableId}').parent())); return false;">${l.l('+Я')}</a>]
										</c:if>
								</c:otherwise>
							</c:choose>
						</div>
					 </c:if>
				</c:forEach>
			</c:forEach>

			<c:if test="${not empty emptyExecutors}">
				<c:set var="executors" value=""/>
				<b>${l.l('ИСПОЛНИТЕЛИ БЕЗ ГРУПП')}:</b>
				<c:forEach var="executor" items="${process.executors}" varStatus="status">
					<c:if test="${executor.groupId == 0 and executor.roleId == 0}">
						<c:if test="${not empty executors}">
							<c:set var="executors" value="${executors}, "/>
						</c:if>
						<c:set var="executors" value="${executors}${ctxUserMap[executor.userId]}"/>
					</c:if>
				</c:forEach>

				${executors}
				<br/>
			</c:if>
		</div>
	</div>
</u:sc>