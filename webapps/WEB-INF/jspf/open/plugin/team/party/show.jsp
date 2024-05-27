<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:url var="urlParty" value="/open/plugin/team/party.do"/>

<c:set var="uiid" value="${u:uiid()}"/>
<div id="${uiid}" class="center1020">
	<c:set var="party" value="${frd.party}"/>

	<c:choose>
		<c:when test="${empty party}">
			<h1>Create a Party</h1>
			<form action="${urlParty}">
				<input type="hidden" name="method" value="update"/>
				<input type="text" name="title" size="20" placeholder="Party title"/>
				<ui:button type="add" styleClass="ml1" onclick="
					$$.ajax.post(this).done((result) => {
						window.location = '/open/party/' + result.data.party.secret.toLowerCase();
					})
				"/>
			</form>
		</c:when>
		<c:otherwise>
			<c:url var="urlParty" value="${urlParty}">
				<c:param name="secret" value="${party.secret}"/>
			</c:url>

			<c:set var="members" value="${frd.members}"/>
			<c:set var="balance" value="${frd.balance}"/>

			<h1>${party.title}</h1>

			<form action="${urlParty}">
				<input type="hidden" name="method" value="paymentUpdate"/>
				<table class="data">
					<tr>
						<td width="50%">Who</td>
						<td width="50%">Description</td>
						<td>Amount</td>
						<td>&nbsp;</td>
					</tr>
					<c:forEach var="member" items="${members}">
						<c:forEach var="payment" items="${member.payments}">
							<tr>
								<td>${member.title}</td>
								<td>${payment.description}</td>
								<td>${payment.amount}</td>
								<td>
									<c:url var="url" value="${urlParty}">
										<c:param name="method" value="paymentDelete"/>
										<c:param name="id" value="${payment.id}"/>
									</c:url>
									<ui:button type="del" styleClass="btn-small" onclick="
										$$.ajax.post('${url}').done(() => {
											$$.ajax.load('${urlParty}', $('#${uiid}').parent());
										})
									"/>
								</td>
							</tr>
						</c:forEach>
					</c:forEach>
					<tr>
						<td><input type="text" name="member" class="w100p"/></td>
						<td><input type="text" name="description" class="w100p"/></td>
						<td><input type="text" name="amount" onkeydown="return isNumberKey(event)" class="w100p"/></td>
						<td>
							<ui:button type="add" onclick="
								$$.ajax.post(this).done(() => {
									$$.ajax.load('${urlParty}', $('#${uiid}').parent());
								})
							"/>
						</td>
					</tr>
				</table>
			</form>

			<h1>Payments</h1>

			<c:forEach var="memberFrom" items="${members}">
				<c:remove var="title"/>
				<c:forEach var="memberTo" items="${members}">
					<c:set var="amount" value="${balance.get(memberFrom.id, memberTo.id)}"/>
					<c:if test="${not empty amount}">
						<c:if test="${empty title}">
							<h2>${memberFrom.title}</h2>
							<c:set var="title" value="1"/>
						</c:if>

						<div>${amount} to ${memberTo.title}</div>
					</c:if>
				</c:forEach>
			</c:forEach>
		</c:otherwise>
	</c:choose>
</div>