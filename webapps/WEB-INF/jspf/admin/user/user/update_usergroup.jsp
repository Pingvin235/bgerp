<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Incoming variables:
	user - the User object
	userGroupList - list of UserGroup objects
	readOnly - boolean, do not show edit buttons
--%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:set var="reloadScript">$$.ajax.load('${form.requestUrl}', $('#addGroup${uiid}').parent());</c:set>

<html:form action="/admin/user" styleId="addGroup${uiid}" styleClass="in-mb1-all" style="display: none;">
	<input type="hidden" name="method" value="userAddGroup" />
	<html:hidden property="id" />

	<table>
		<tr>
			<td nowrap="nowrap">${l.l('Период с')}:</td>
			<td class="pl05">
				<ui:date-time name="fromDate" value="0"/>
				${l.l('по')}:
				<ui:date-time name="toDate"/>
			</td>
		</tr>
		<tr>
			<td class="pt05">${l.l('Group')}:</td>
			<td style="width: 100%" class="pl05 pt05">
				<ui:select-single name="group" list="${ctxUserGroupFullTitledList}" style="width: 100%;"/>
			</td>
		</tr>
	</table>

	<ui:button type="ok" onclick="$$.ajax.post(this).done(() => { ${reloadScript} })"/>
	<ui:button type="cancel" onclick="$('#addGroup${uiid}').hide(); $('#showGroup${uiid}').show();" styleClass="ml1"/>
</html:form>

<html:form action="/admin/user" styleId="showGroup${uiid}">
	<input type="hidden" name="method" value="userGroupList" />
	<html:hidden property="id" />

	<div class="in-mr1">
		<c:if test="${not readOnly and ctxUser.checkPerm('/admin/user:userAddGroup')}">
			<ui:button type="add" onclick="$('#showGroup${uiid}').hide(); $('#addGroup${uiid}').show();" styleClass="mr1"/>
		</c:if>

		<ui:date-time name="date" value="${form.param.date}" placeholder="${l.l('На дату')}"/>

		<ui:button type="run" onclick="$$.ajax.load(this.form, $('#showGroup${uiid}').parent())"/>
	</div>

	<table class="data mt1 hl">
		<c:set var="permDel" value="${not readOnly and ctxUser.checkPerm('/admin/user:userRemoveGroup')}"/>
		<c:set var="permClose" value="${not readOnly and ctxUser.checkPerm('/admin/user:userClosePeriodGroup')}"/>

		<tr>
			<c:if test="${permDel or permClose}">
				<td width="100">&nbsp;</td>
			</c:if>
			<td width="100">${l.l('Period')}</td>
			<td width="100%">${l.l('Group')}</td>
		</tr>

		<c:set var="list" value="${ctxUserGroupList}" />
		<c:set var="paramName" value="group" />
		<c:set var="values" value="${user.groupIds}" />
		<c:set var="moveOn" value="0"/>

		<c:forEach var="value" items="${userGroupList}">
			<c:forEach var="item" items="${list}">
				<c:if test="${item.id eq value.groupId}">
					<tr>
						<c:if test="${permDel or permClose}">
							<td nowrap="nowrap">
								<c:if test="${permDel}">
									<ui:button type="del" styleClass="btn-small"
										onclick="$$.ajax
											.post('/admin/user.do?method=userRemoveGroup&userId=${form.id}&groupId=${item.id}&dateFrom=${tu.format(value.dateFrom, 'ymd')}&dateTo=${tu.format(value.dateTo, 'ymd')}')
											.done(() => { $(this).parents('tr').first().remove(); })
										"/>
								</c:if>
								<c:if test="${permClose}">
									<button type="button" class="btn-white btn-small icon" title="${l.l('Закрыть период')}"
										onclick="$('#closeGroupId${uiid}').val(${item.id}); $('#dateFrom${uiid}').val('${tu.format(value.dateFrom, 'ymd')}'); $('#dateTo${uiid}').val('${tu.format(value.dateTo, 'ymd')}'); $('#showGroup${uiid}').hide(); $('#closeGroup${uiid}').show();"
									><i class="ti-control-skip-forward"></i></button>
								</c:if>
							</td>
						</c:if>
						<td nowrap="nowrap">${tu.format(value.dateFrom, 'ymd')} - ${tu.format(value.dateTo, 'ymd')}</td>
						<td>${item.titleWithPath}</td>
					</tr>
				</c:if>
			</c:forEach>
		</c:forEach>
	</table>
</html:form>

<html:form action="/admin/user" styleId="closeGroup${uiid}" style="display: none;" styleClass="in-inline-block">
	<input type="hidden" name="method" value="userClosePeriodGroup" />
	<input id="dateFrom${uiid}" type="hidden" name="dateFrom" value=""/>
	<input id="dateTo${uiid}" type="hidden" name="dateTo" value=""/>
	<input id="closeGroupId${uiid}" name="groupId" type="hidden" value="-1" />
	<input name="userId" type="hidden" value="${form.id}" />

	${l.l('Закрыть с даты')}:

	<ui:date-time name="date" value="0"/>

	<div class="in-ml1 ml1">
		<ui:button type="ok" onclick="if (!confirm('${l.l('Вы уверены, что хотите закрыть период группы?')}')) return; $$.ajax.post(this).done(() => { ${reloadScript} })"/>
		<ui:button type="cancel" onclick="$('#closeGroup${uiid}').hide(); $('#showGroup${uiid}').show();"/>
	</div>
</html:form>
