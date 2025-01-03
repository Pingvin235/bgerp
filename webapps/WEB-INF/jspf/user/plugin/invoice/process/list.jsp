<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="${form.requestURI}">
	<html:hidden property="method"/>
	<html:hidden property="processId"/>

	<p:check action="org.bgerp.plugin.bil.invoice.action.InvoiceAction:create">
		<c:url var="url" value="${form.requestURI}">
			<c:param name="method" value="create"/>
			<c:param name="processId" value="${form.param.processId}"/>
			<c:param name="returnUrl" value="${form.requestUrl}"/>
		</c:url>
		<ui:button type="add" onclick="$$.ajax.load('${url}', $('#${uiid}').parent())"/>
	</p:check>

	<ui:page-control nextCommand="; $$.ajax.load(this.form, $('#${uiid}').parent())"/>
</html:form>

<c:set var="reloadScript">() => { $$.ajax.load('${form.requestUrl}', $('#${uiid}').parent()) }</c:set>

<%-- for payment date dialog --%>
<c:set var="hiddenUiid" value="${u:uiid()}"/>
<input type='hidden' id="${hiddenUiid}"/>

<table id="${uiid}" class="data mt05 hl">
	<tr>
		<td width="1em">&nbsp;</td>
		<td>ID</td>
		<td>${l.l('Period')}</td>
		<td>${l.l('Number')}</td>
		<td>${l.l('Сумма')}</td>
		<td>${l.l('Created')}</td>
		<td>${l.l('Paid')}</td>
	</tr>
	<c:forEach var="item" items="${frd.list}" >
		<tr>
			<td>
				<c:set var="menuUiid" value="${u:uiid()}"/>
				<ui:popup-menu id="${menuUiid}">
					<p:check action="org.bgerp.plugin.bil.invoice.action.InvoiceAction:get">
						<c:url var="url" value="${form.requestURI}">
							<c:param name="method" value="get"/>
							<c:param name="returnUrl" value="${form.requestUrl}"/>
							<c:param name="id" value="${item.id}"/>
						</c:url>
						<li><a href="#"
							onclick="$$.ajax.load('${url}', $('#${uiid}').parent()); return false;">
							<i class="ti-pencil"></i>
							${l.l('Редактировать')}</a>
						</li>
					</p:check>

					<c:choose>
						<c:when test="${not empty item.paymentDate}">
							<p:check action="org.bgerp.plugin.bil.invoice.action.InvoiceAction:unpaid">
								<c:url var="url" value="${form.requestURI}">
									<c:param name="method" value="unpaid"/>
									<c:param name="id" value="${item.id}"/>
								</c:url>
								<li><a href="#"
									onclick="$$.ajax.post('${url}').done(${reloadScript}); return false;">
									<i class="ti-eraser"></i>
									${l.l('Unpaid')}</a>
								</li>
							</p:check>
						</c:when>
						<c:otherwise>
							<p:check action="org.bgerp.plugin.bil.invoice.action.InvoiceAction:paid">
								<c:url var="url" value="${form.requestURI}">
									<c:param name="method" value="paid"/>
									<c:param name="id" value="${item.id}"/>
								</c:url>
								<li><a href="#"
									onclick="$$.invoice.paid('${hiddenUiid}', '${url}').done(${reloadScript}); return false;">
									<i class="ti-money"></i>
									${l.l('Paid')}</a>
								</li>
							</p:check>
						</c:otherwise>
					</c:choose>

					<p:check action="org.bgerp.plugin.bil.invoice.action.InvoiceAction:delete">
						<c:url var="url" value="${form.requestURI}">
							<c:param name="method" value="delete"/>
							<c:param name="id" value="${item.id}"/>
						</c:url>
						<li><a href="#"
							onclick="if ($$.confirm.del()) $$.ajax.post('${url}').done(${reloadScript}); return false;">
							<i class="ti-trash"></i>
							${l.l('Удалить')}</a>
						</li>
					</p:check>
				</ui:popup-menu>
				<ui:button type="more" styleClass="btn-small" onclick="$$.ui.menuInit($(this), $('#${menuUiid}'), 'left', true);"/>
			</td>
			<td>${item.id}</td>
			<td class="nowrap">${tu.format(item.dateFrom, 'ymd')} - ${tu.format(item.dateTo, 'ymd')}</td>
			<td>
				<c:choose>
					<c:when test="${ctxUser.checkPerm('org.bgerp.plugin.bil.invoice.action.InvoiceAction:doc')}">
						<c:url var="url" value="${form.requestURI}">
							<c:param name="method" value="doc"/>
							<c:param name="id" value="${item.id}"/>
						</c:url>
						<a target="_blank" href="${url}">${item.number}</a>
					</c:when>
					<c:otherwise>
						${item.number}
					</c:otherwise>
				</c:choose>
			</td>
			<td>${item.amount}</td>
			<td>${tu.format(item.createTime, 'ymdhm')}</td>
			<td>
				<c:if test="${not empty item.paymentDate}">
					${tu.format(item.paymentDate, 'ymd')}
					(<ui:user-link id="${item.paymentUserId}"/>)
				</c:if>
			</td>
		</tr>
	</c:forEach>
</table>
