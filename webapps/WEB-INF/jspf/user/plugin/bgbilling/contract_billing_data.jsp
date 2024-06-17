<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="contractTreeId" value="bgbilling-${contract.billingId}-${contract.id}-tree"/>

<div class="in-table-cell bgbilling-contractTree" id="${contractTreeId}">
	<div style="min-width: 350px;">
		<div class="box" style="overflow: auto; height: 650px;">
			<table id="treeTable">
				<u:sc>
					<c:set var="title" value="Параметры договора"/>
					<c:set var="value" value=""/>
					<c:set var="url" value="contract.do?method=parameterList"/>
					<%@ include file="contract/tree_item.jsp"%>
				</u:sc>
				<u:sc>
					<c:set var="title" value="Объекты договора"/>
					<c:set var="value">${contract.objects.first} / ${contract.objects.second}</c:set>
					<c:set var="url" value="contract.do?method=contractObjectList"/>
					<%@ include file="contract/tree_item.jsp"%>
				</u:sc>

				<u:sc>
					<c:set var="title" value="Иерархия договоров"/>
					<c:set var="value">
						<c:choose>
							<c:when test="${contract.hierarchy eq 'super'}">
								супер: ${contract.hierarchyDep} / ${contract.hierarchyIndep}
							</c:when>
							<c:when test="${contract.hierarchy eq 'depend_sub'}">
								з.суб
							</c:when>
							<c:when test="${contract.hierarchy eq 'independ_sub'}">
								н.суб
							</c:when>
						</c:choose>
					</c:set>
					<c:set var="url" value="contract.do?method=contractSubcontractList"/>
					<%@ include file="contract/tree_item.jsp"%>
				</u:sc>

				<u:sc>
					<c:set var="title" value="Статус"/>
					<c:set var="value" value="${contract.status}"/>
					<c:set var="valueId" value="status"/>
					<c:set var="url" value="contract.do?method=status"/>
					<%@ include file="contract/tree_item.jsp"%>
				</u:sc>

				 <u:sc>
					<c:set var="title" value="Лимит"/>
					<c:set var="value" value="${contract.balanceLimit}"/>
					<c:set var="valueId" value="limit"/>
					<c:set var="url" value="contract.do?method=limit"/>
					<%@ include file="contract/tree_item.jsp"%>
				</u:sc>

				<u:sc>
					<c:set var="title" value="Режим"/>
					<c:set var="value">
						<c:choose>
							<c:when test="${contract.mode eq 1}">Дебет</c:when>
							<c:otherwise>Кредит</c:otherwise>
						</c:choose>
					</c:set>
					<c:set var="valueId" value="mode"/>
					<c:set var="url" value="contract.do?method=modeLog"/>
					<%@ include file="contract/tree_item.jsp"%>
				</u:sc>

				<u:sc>
					<c:set var="title" value="Лицо"/>
					<c:set var="value">
						<c:choose>
							<c:when test="${contract.face eq 0}">Физическое</c:when>
							<c:otherwise>Юридическое</c:otherwise>
						</c:choose>
					</c:set>
					<c:set var="valueId" value="face"/>
					<c:set var="url" value="contract.do?method=faceLog"/>
					<%@ include file="contract/tree_item.jsp"%>
				</u:sc>

				<c:url var="urlBalancePeriod" value="balance.do?method=balance">
					<c:if test="${not empty contract.balanceDate}">
						<c:param name="dateFrom" value="${tu.format(contract.balanceDate, 'dd.MM.yyyy')}"/>
						<c:param name="dateTo" value="${tu.format(tu.getEndMonth(contract.balanceDate), 'dd.MM.yyyy')}"/>
					</c:if>
				</c:url>

				<u:sc>
					<c:set var="locale" value="${u:newInstance1('java.util.Locale', 'ru')}"/>
					<c:set var="format" value="${u:newInstance2('java.text.SimpleDateFormat', 'LLLL Y', locale)}"/>
					<c:set var="title">
						Баланс (<span id="balanceMonth">${not empty contract.balanceDate ? format.format(contract.balanceDate) : ''}</span>)
					</c:set>
					<c:set var="value" value=""/>
					<c:url var="url" value="${urlBalancePeriod}">
						<c:param name="method" value="balance"/>
					</c:url>
					<c:set var="rowClass" value="balance"/>
					<%@ include file="contract/tree_item.jsp"%>
				</u:sc>

				 <u:sc>
					<c:set var="title" value="Входящий остаток"/>
					<c:set var="value" value="${contract.balanceIn}"/>
					<c:set var="valueId" value="balanceIn"/>
					<c:url var="url" value="${urlBalancePeriod}">
						<c:param name="method" value="balance"/>
					</c:url>
					<c:set var="rowClass" value="balance"/>
					<c:set var="level" value="2"/>
					<%@ include file="contract/tree_item.jsp"%>
				</u:sc>

				<u:sc>
					<c:set var="title" value="Приход"/>
					<c:set var="value" value="${contract.balancePayment}"/>
					<c:set var="valueId" value="balancePayment"/>
					<c:url var="url" value="${urlBalancePeriod}">
						<c:param name="method" value="paymentList"/>
					</c:url>
					<c:set var="rowClass" value="balance"/>
					<c:set var="level" value="2"/>
					<%@ include file="contract/tree_item.jsp"%>
				</u:sc>

				<u:sc>
					<c:set var="title" value="Наработка"/>
					<c:set var="value" value="${contract.balanceAccount}"/>
					<c:set var="valueId" value="balanceAccount"/>
					<c:url var="url" value="${urlBalancePeriod}">
						<c:param name="method" value="accountList"/>
					</c:url>
					<c:set var="rowClass" value="balance"/>
					<c:set var="level" value="2"/>
					<%@ include file="contract/tree_item.jsp"%>
				</u:sc>

				<u:sc>
					<c:set var="title" value="Расход"/>
					<c:set var="value" value="${contract.balanceCharge}"/>
					<c:set var="valueId" value="balanceCharge"/>
					<c:url var="url" value="${urlBalancePeriod}">
						<c:param name="method" value="chargeList"/>
					</c:url>
					<c:set var="rowClass" value="balance"/>
					<c:set var="level" value="2"/>
					<%@ include file="contract/tree_item.jsp"%>
				</u:sc>

				 <u:sc>
					<c:set var="title" value="Исходящий остаток"/>
					<c:set var="value" value="${contract.balanceOut}"/>
					<c:set var="valueId" value="balanceOut"/>
					<c:url var="url" value="${urlBalancePeriod}">
						<c:param name="method" value="balance"/>
					</c:url>
					<c:set var="rowClass" value="balance"/>
					<c:set var="level" value="2"/>
					<%@ include file="contract/tree_item.jsp"%>
				</u:sc>

				 <u:sc>
					<c:set var="title" value="Тарифные планы"/>
					<c:set var="value" value=""/>
					<c:set var="url" value="contractTariff.do?method=tariff"/>
					<%@ include file="contract/tree_item.jsp"%>
				</u:sc>

				<c:forEach var="item" items="${contract.tariffList}">
					<u:sc>
						<c:set var="title" value="${item.title}"/>
						<c:set var="value" value=""/>
						<c:set var="url" value=""/>
						<c:set var="level" value="2"/>
						<%@ include file="contract/tree_item.jsp"%>
					</u:sc>
				</c:forEach>

				<u:sc>
					<c:set var="title" value="Модули"/>
					<c:set var="value" value=""/>
					<c:set var="url" value="contract.do?method=moduleList"/>
					<c:set var="rowId" value="modules"/>
					<%@ include file="contract/tree_item.jsp"%>
				</u:sc>

				<c:forEach var="item" items="${contract.moduleList}">
					<u:sc>
						<c:set var="title" value="${item.title}"/>
						<c:set var="value" value="${item.status}"/>
						<c:set var="url">
							<c:choose>
								<c:when test="${item.clientPackage eq 'bitel.billing.module.services.npay' or item.clientPackage eq 'ru.bitel.bgbilling.modules.npay.client'}">npay.do?method=serviceList&moduleId=${item.moduleId}</c:when>
								<c:when test="${item.clientPackage eq 'ru.bitel.bgbilling.modules.rscm.client'}">rscm.do?method=serviceList&moduleId=${item.moduleId}</c:when>
								<c:when test="${item.clientPackage eq 'ru.bitel.bgbilling.modules.inet.api.client' or item.clientPackage eq 'ru.bitel.bgbilling.modules.inet.client'}">inet.do?method=serviceTree&moduleId=${item.moduleId}</c:when>
								<c:when test="${item.clientPackage eq 'bitel.billing.module.services.card' or item.clientPackage eq 'ru.bitel.bgbilling.modules.card.client'}">card.do?method=contractInfo&moduleId=${item.moduleId}</c:when>
								<c:when test="${item.clientPackage eq 'bitel.billing.module.services.bill' or item.clientPackage eq 'ru.bitel.bgbilling.modules.bill.client'}">/user/empty.do?forwardFile=/WEB-INF/jspf/user/plugin/bgbilling/bill/contract_info.jsp&moduleId=${item.moduleId}</c:when>
								<c:when test="${item.clientPackage eq 'ru.bitel.bgbilling.modules.cerbercrypt.client'}">/user/empty.do?forwardFile=/WEB-INF/jspf/user/plugin/bgbilling/cerbercrypt/contract_info.jsp&moduleId=${item.moduleId}</c:when>
								<c:otherwise>dev</c:otherwise>
							</c:choose>
						</c:set>
						<c:set var="rowClass" value="module"/>
						<c:set var="level" value="2"/>
						<%@ include file="contract/tree_item.jsp"%>
					</u:sc>
				</c:forEach>

				<u:sc>
					<c:set var="title" value="Web"/>
					<c:set var="value" value=""/>
					<c:set var="url" value="dev"/>
					<%@ include file="contract/tree_item.jsp"%>
				</u:sc>

				<u:sc>
					<c:set var="title" value="Группы"/>
					<c:set var="value" value=""/>
					<c:set var="url" value="contract.do?method=groupList"/>
					<c:set var="level" value=""/>
					<%@ include file="contract/tree_item.jsp"%>
				</u:sc>

				 <tr>
					 <td colspan="2">
							<div id="groups" class="enum">
								<c:forEach var="item" items="${contract.groupList}" varStatus="status">
									<div>${item.title}<c:if test="${not status.last}">, </c:if></div>
								</c:forEach>
							</div>
					 </td>
				 </tr>

				 <u:sc>
					<c:set var="title" value="Скрипт поведения"/>
					<c:set var="value" value=""/>
					<c:set var="url" value="contract.do?method=scriptList"/>
					<c:set var="level" value=""/>
					<%@ include file="contract/tree_item.jsp"%>
				 </u:sc>

				  <tr>
					 <td colspan="2">
							<div id="scripts" class="enum">
								<c:forEach var="item" items="${contract.scriptList}" varStatus="status">
									<div>${item.title}<c:if test="${not status.last}">, </c:if></div>
								</c:forEach>
							</div>
					 </td>
				  </tr>

				  <%--
				  <c:forEach var="item" items="${contract.scriptList}">
					<u:sc>
						<c:set var="title" value="${item.title}"/>
						<c:set var="url" value=""/>
						<c:set var="level" value="2"/>
						<%@ include file="contract/tree_item.jsp"%>
					</u:sc>
				 </c:forEach>
				  --%>

				 <u:sc>
					<c:set var="title" value="Доп. действия"/>
					<c:set var="value" value=""/>
					<c:set var="url" value="contract.do?method=additionalActionList"/>
					<%@ include file="contract/tree_item.jsp"%>
				</u:sc>

				<u:sc>
					<c:set var="title" value="Примечания"/>
					<c:set var="value" value="${contract.comments}"/>
					<c:set var="url" value="contract.do?method=memoList"/>
					<%@ include file="contract/tree_item.jsp"%>
				</u:sc>

				<%--
				<u:sc>
					<c:set var="title" value=""/>
					<c:set var="value" value=""/>
					<c:set var="url" value=""/>
					<%@ include file="contract/tree_item.jsp"%>
				</u:sc>
				--%>

				<%--
				<c:set var="node" value="${contractInfoTree}" scope="request"/>
				<c:set var="level" value="0" scope="request"/>
				<jsp:include page="contract/parameter_tree.jsp"/>
				--%>
			</table>
		</div>
	</div>
	<div style="width: 100%;" class="pl1">
		<div id="content" class="box p05" style="overflow: auto; height: 650px;">
		</div>
	</div>
</div>

<script>
	$("#${contractTreeId} #treeTable").children().children(':first').click();
</script>