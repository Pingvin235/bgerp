<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="user/plugin/invoice/invoice">
	<html:hidden property="action"/>
	<html:hidden property="processId"/>

	<p:check action="org.bgerp.plugin.bil.billing.invoice.action.InvoiceAction:create">
		<c:url var="url" value="/user/plugin/invoice/invoice.do">
			<c:param name="action" value="create"/>
			<c:param name="processId" value="${form.param.processId}"/>
			<c:param name="returnUrl" value="${form.requestUrl}"/>
		</c:url>
		<ui:button type="add" onclick="$$.ajax.load('${url}', $('#${uiid}').parent())"/>
	</p:check>

	<ui:page-control/>
</html:form>

<table id="${uiid}" class="data mt05">
	<tr>
		<td width="1em">&nbsp;</td>
		<td>${l.l('Месяц')}</td>
		<td>${l.l('Номер')}</td>
		<td>${l.l('Сумма')}</td>
		<td>${l.l('Создан')}</td>
		<td>${l.l('Оплачен')}</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}" >
		<tr>
			<td>
				<c:set var="menuUiid" value="${u:uiid()}"/>
				<ui:popup-menu id="${menuUiid}">
					<p:check action="org.bgerp.plugin.bil.billing.invoice.action.InvoiceAction:delete">
						<c:url var="url" value="/user/plugin/invoice/invoice.do">
							<c:param name="action" value="delete"/>
							<c:param name="processId" value="${form.param.processId}"/>
							<c:param name="id" value="${item.id}"/>
						</c:url>
						<li><a href="#"
							onclick="if ($$.confirm.del()) $$.ajax.post('${url}').done(() => { $$.ajax.load('${form.requestUrl}', $('#${uiid}').parent()) })">
							<i class="ti-trash"></i>
							${l.l('Удалить')}</a>
						</li>
					</p:check>
				</ui:popup-menu>
				<ui:button type="more" styleClass="btn-small" onclick="$$.ui.menuInit($(this), $('#${menuUiid}'), 'left', true);"/>
			</td>
			<td>${tu.format(item.fromDate, 'yyyy.MM')}</td>
			<td>
				<c:choose>
					<c:when test="${p:get(ctxUser.id, 'org.bgerp.plugin.bil.billing.invoice.action.InvoiceAction:doc') ne null}">
						<c:url var="url" value="/user/plugin/invoice/invoice.do">
							<c:param name="action" value="doc"/>
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
			<td>${tu.format(item.createdTime, 'ymdhm')}</td>
			<td>${tu.format(item.paymentDate, 'ymd')}</td>
		</tr>
	</c:forEach>
</table>
