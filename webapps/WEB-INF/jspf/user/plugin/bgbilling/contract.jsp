<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="contract" value="${frd.contract}" scope="request"/>

<c:if test="${not empty contract}">
	<c:set var="billingId" value="${form.param['billingId']}"/>
	<c:set var="contractId" value="${form.id}"/>

	<c:set var="contractTitle" value="${contract.title}"/>
	<c:set var="contractComment" value="${contract.comment}"/>

	<c:set var="showTdId" value="${u:uiid()}"/>
	<c:set var="editTdId" value="${u:uiid()}"/>

	<c:set var="tabsUiid" value="${u:uiid()}"/>

	<script>
		$(function()
		{
			$("#${editTdId}").hide();

			const $tabs = $("#${tabsUiid}").tabs( {spinner: '' , refreshButton : true} );

			<%-- параметры и отчёты - ниже в DIV ке --%>
			<c:url var="url" value="/user/plugin/bgbilling/proto/contract.do">
				<c:param name="method" value="contractCards"/>
				<c:param name="billingId" value="${billingId}"/>
				<c:param name="contractId" value="${contractId}"/>
			</c:url>
			$tabs.tabs( "add", "${url}", "Карточки" );

			<c:url var="url" value="/user/process/link.do">
				<c:param name="method" value="linkedProcessList"/>
				<c:param name="objectType" value="contract:${billingId}"/>
				<c:param name="objectTitle" value="${contractTitle}"/>
				<c:param name="id" value="${contractId}"/>
			</c:url>
			$tabs.tabs( "add", "${url}", "Процессы", "style='margin-left: 1em;'" );

			<c:set var="plugin" value="${ctxPluginManager.pluginMap['document']}"/>
			<c:if test="${not empty plugin}">
				<c:url var="url" value="plugin/document/document.do">
					<c:param name="scope" value="bgbilling-contract"/>
					<c:param name="objectType" value="contract:${billingId}"/>
					<c:param name="objectTitle" value="${contractTitle}"/>
					<c:param name="objectId" value="${contractId}"/>
				</c:url>
				$tabs.tabs( 'add', "${url}", "Документы" );
			</c:if>
		})
	</script>

	<c:set var="customer" value="${frd.customer}"/>

	<%-- означает, что договор не в буфере открытых, т.к. открыт где-то вложенно --%>
	<c:if test="${form.param.inBuffer ne 0}">
		<u:sc>
			<c:set var="title">
				<span class='title'>${contractTitle}</span> <%--
			--%><span class='comment'>(${u.escapeXml( contractComment )})</span>
			</c:set>
			<shell:title text="${title}"/>
			<shell:state/>
		</u:sc>
	</c:if>

	<c:set var="uiid" value="${u:uiid()}"/>

	<div class="in-table-cell nowrap in-pr1 mb05" id="${uiid}">
		<div>ID: <b><a title="Открыть договор отдельно" href="#" onclick="$$.bgbilling.contract.open('${billingId}', ${contractId}); return false;">${contractId}</a></b></div>

		<c:set var="customerSelectUiid" value="${u:uiid()}"/>

		<div style="width: 100%;" id="${showTdId}">
			Контрагент:
			<span><c:choose>
				<c:when test="${not empty customer}">
					<a href="#" onclick="$$.customer.open(${customer.id}); return false;">${customer.title}</a>
					<c:set var="customerId" value="${customer.id}"/>
				</c:when>
				<c:otherwise>
					не установлен
					<c:set var="customerId" value="0"/>
				</c:otherwise>
			</c:choose></span>

			<ui:button type="edit" styleClass="btn-small mr1"
					onclick="$('#${showTdId}').hide(); $('#${editTdId}').show();
							 buildOpenedCustomerList( $('#${editTdId}'), { id : '${customer.id}', title : '${u.escapeXml( customer.title )}' } );
							 $$.ui.comboSingleInit( $('#${customerSelectUiid}') )"
					title="Изменить контрагента"/>

			<c:choose>
				<c:when test="${not empty contract.title}">
					<c:set var="contractTabId" value="bgbilling-contractTabs-${billingId}-${contractId}"/>

					<c:url var="url" value="${form.requestURI}">
						<c:param name="method" value="copyCustomerParamToContract"/>
						<c:param name="contractId" value="${contractId}"/>
						<c:param name="contractTitle" value="${contract.title}"/>
						<c:param name="billingId" value="${billingId}"/>
						<c:param name="customerId" value="${customerId}"/>
					</c:url>

					<button type="button" class="btn-white btn-small mr1" onclick="if (confirm('Скопировать параметры контрагента в договор?')) $$.ajax.post('${url}').done(() => $$.bgbilling.contract.open('${billingId}', ${contractId}))"
							title="Скопировать параметры контрагента в договор">
						Скопировать параметры
					</button>

					<c:url var="url" value="${form.requestURI}">
						<c:param name="method" value="createCustomerFromContract"/>
						<c:param name="customerId" value="${customer.id}"/>
						<c:param name="contractId" value="${contractId}"/>
						<c:param name="billingId" value="${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[billingId].id}"/>
					</c:url>

					<button type="button" class="btn-grey btn-small" onclick="$$.ajax.post('${url}', {control: this}).done(() => $$.bgbilling.contract.open('${billingId}', ${contractId}))"
							title="Импорт в контрагента">Импорт</button>
				</c:when>
				<c:otherwise>
					<b>Договор не найден в БД биллинга, отвяжите его от контрагента!</b>
				</c:otherwise>
			</c:choose>
		</div>

		<div style="width: 100%;" id="${editTdId}">
			<ui:combo-single id="${customerSelectUiid}" hiddenName="customerId" widthTextValue="15em" prefixText="Контрагент:"/>

			<c:set var="changeCustomerScript">
				bgbilling_changeContractCustomer($('#${editTdId}'), $('#${showTdId}').find('span'), '${billingId}', ${contractId}, '${contractTitle}').done(() => {
					const newCustomerId = $('#${editTdId} input[name=customerId]').val();
					const dependView = bgcrm.pers['iface.bgbilling.contractOpenMode'] != 2;

					<%--- исходя из того, что того же контрагента он выбрать не сможет --%>
					if (dependView) {
						<c:choose>
							<c:when test="${not empty customer}">
								const $tabs = $('#${editTdId}').closest('div.ui-tabs');
								const active = $tabs.tabs('option', 'active');
								$tabs.tabs('remove', active);
							</c:when>
							<c:otherwise>
								$$.closeObject = null;
								$$.shell.removeCommandDiv('contract_${billingId}-${contractId}');
								$$.bgbilling.contract.open('${billingId}', ${contractId});
							</c:otherwise>
						</c:choose>
					} else {
						<%-- для обновления обозначения в буфере --%>
						$$.closeObject = null;
						$$.shell.removeCommandDiv('contract_${billingId}-${contractId}');
						$$.bgbilling.contract.open('${billingId}', ${contractId});
					}
				});
			</c:set>
			<button type="button" class="btn-grey ml1" onclick="${changeCustomerScript}">OK</button>
			<button type="button" class="btn-white ml05"
				onclick="$('#${editTdId}').hide(); $('#${showTdId}').show();">Отмена</button>
		</div>

		<c:if test="${not empty contract.title}">
			<c:url var="url" value="/user/plugin/bgbilling/proto/contract.do">
				<c:param name="billingId" value="${billingId}"/>
				<c:param name="contractId" value="${contractId}"/>
			</c:url>
			<div><u:sc>
				<c:set var="value" value="${tu.format(contract.dateTo, 'dd.MM.yyyy')}"/>
				Период: <b>${tu.format(contract.dateFrom, 'ymd')} - <a href="#" onclick="$$.bgbilling.contract.dateToUpdate(this, '${value}', '${url}').done(() => {
					$$.ajax.loadContent('${form.requestUrl}');
				}); return false;">${empty value ? '...' : value}</a></b>
			</u:sc></div>
			<div>
				Биллинг: <b>${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[billingId].title}</b>
			</div>
			<div>
				<c:url var="openUrl" value="${url}">
					<c:param name="method" value="bgbillingOpenContract" />
				</c:url>

				<button type="button" class="btn-white btn-small" onclick="$$.ajax.post('${openUrl}')">Открыть в биллинге</button>
			</div>
		</c:if>
	</div>

	<c:if test="${not empty contract.title}">
		<div id="${tabsUiid}">
			<ul><%--
			--%><li><a href='#params'>Параметры</a></li><%--
			--%><li><a href='#reports'>Отчёты</a></li><%--
		--%></ul>
			<div id="params">
				<%@ include file="contract_billing_data.jsp"%>
			</div>
			<div id="reports">
				<%@ include file="contract_reports.jsp"%>
			</div>
		</div>
	</c:if>
</c:if>