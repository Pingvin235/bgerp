<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:forEach var="type" items="${config.typeMap.values()}">
	<c:if test="${type.getClass().simpleName eq 'MessageTypeCall'}">
		<u:sc>
			<c:set var="reg" value="${type.getRegistrationByUser(form.userId)}"/>

			<c:set var="uiid" value="${u:uiid()}"/>
			<div class="mb1" id="${uiid}">
				<c:choose>
					<c:when test="${not empty reg}">
						<span class="tt">${type.title}&nbsp;${l.l('number')}&nbsp;<b>${reg.number}</b></span>
						<c:set var="url" value="/user/message/call.do?typeId=${type.id}&method=numberFree"/>
						<button
							type="button" class="btn-grey ml1"
							onclick="$$.ajax.post('${url}').done(() => { $$.ajax.load('${form.requestUrl}', $('#${uiid}').parent()) })" >${l.l('Освободить')}</button>

						<p:check action="/user/message/call:testCall">
							<form action="/user/message/call.do" onsubmit="this.out.click(); return false;" class="mt1 in-mr1">
								<input type="submit" hidden/>
								<input type="hidden" name="method" value="testCall"/>
								<input type="hidden" name="typeId" value="${type.id}"/>

								<ui:combo-single name="direction" value="1" prefixText="Direction:" widthTextValue="4em"
									onSelect="const isOut = this.value == 2;
									$(this.form.processId).toggle(isOut); $(this.form.customerId).toggle(isOut);">
									<jsp:attribute name="valuesHtml">
										<li value="1">IN</li>
										<li value="2">OUT</li>
									</jsp:attribute>
								</ui:combo-single>

								<input type="text" name="number" placeholder="${l.l('Number')}" size="10"/>

								<ui:input-decimal name="processId" digits="0" placeholder="Process ID" style="display: none;"/>
								<ui:input-decimal name="customerId" digits="0" placeholder="Customer ID" style="display: none;"/>

								<button type="button" name="out" class="btn-grey" onclick="$$.ajax.post(this).done(() => { alert('OK') })" class="ml2">TEST</button>
							</form>
						</p:check>
					</c:when>
					<c:otherwise>
						<form action="/user/message/call.do" style="display: inline-block;">
							<input type="hidden" name="method" value="numberRegister"/>
							<input type="hidden" name="typeId" value="${type.id}"/>

							<input type="text" name="number" placeholder="${type.title}, ${l.l('number')}" class="" value="${type.getUserOfferedNumber(ctxUser.id)}"/>

							<c:set var="code">
								$$.ajax
									.post(this)
									.done((result) => {
										const user = result.data.regUser;
										if (user && !confirm('${l.l('Number occupied by user: ')}' + user.title + ',\n' + '${l.l('register anyway?')}'))
											return;

										$$.ajax
											.post($$.ajax.formUrl(this.form) + '&check=0')
											.done(() => $$.ajax.load('${form.requestUrl}', $('#${uiid}').parent()));
									})
							</c:set>

							<button type="button" class="btn-grey ml1" onclick="${code}">${l.l('Занять')}</button>
						</form>
					</c:otherwise>
				</c:choose>
			</div>
		</u:sc>
	</c:if>
</c:forEach>