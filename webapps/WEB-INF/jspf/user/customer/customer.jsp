<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="customer" value="${form.response.data.customer}" scope="request"/>

<c:if test="${empty tableId}">
	<c:set var="tableId" value="${u:uiid()}"/>
</c:if>

<c:if test="${not empty customer}">
	<u:sc>
		<c:set var="title">
			<span class='title' id='customer_title_${customer.id}'></span>
		</c:set>
		<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>
		<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>
	</u:sc>

	<script>
		$(function () {
			const $selector = $("div#customer-${customer.id} > #customerViewTabs");

			const $tabs = $selector.tabs({refreshButton: true});

			<c:url var="url" value="parameter.do">
				<c:param name="action" value="parameterList"/>
				<c:param name="id" value="${customer.id}"/>
				<c:param name="objectType" value="customer"/>
				<c:param name="parameterGroup" value="${customer.paramGroupId}"/>
			</c:url>

			$tabs.tabs("add", "${url}", "${l.l('Параметры')}");

			<c:url var="url" value="/user/process/link.do">
				<c:param name="action" value="linkedProcessList"/>
				<c:param name="objectType" value="customer"/>
				<c:param name="objectTitle" value="${customer.title}"/>
				<c:param name="id" value="${customer.id}"/>
			</c:url>

			$tabs.tabs("add", "${url}", "${l.l('Процессы')}");

			<c:set var="endpoint" value="user.customer.tabs.jsp"/>
			<%@ include file="/WEB-INF/jspf/plugin_include.jsp"%>

			customerChangeTitle(${customer.id}, "${u:quotEscape(customer.title)}");

			// обновление вкладки "Процессы", если она открыта
			var $contentDiv = $('#content > #customer-${customer.id}');

			$contentDiv.data('onShow', function() {
				const processesTabPos = 1;
				if ($tabs.tabs("option", "active" ) === processesTabPos) {
					$selector.find("ul > li").eq(processesTabPos).data("loaded", false);
					$tabs.tabs("load", processesTabPos);
				}
			});
		})
	</script>

	<c:set var="uiid" value="${u:uiid()}"/>

	<div id="${uiid}" class="in-table-cell in-nowrap">
		<div style="width: 100%;">ID: <b>${customer.id}</b> <span id="customer_title_${customer.id}"><%-- название подставляется скриптом --%></span></div>

		<%-- TODO: Перенести кнопку в плагин.
			 Временно заблокирована из-за ошибок в импорте контрагентов.
		<td width="5%">
			<form action="/user/plugin/bgbilling/contract.do">
				<input type="hidden" name="action" value="copyCustomerParamCascade"/>
				<input type="hidden" name="customerId" value="${customer.id}"/>
				<input type="button" value="Скопировать параметры в договора" onclick="if( confirm('Скопировать параметры?') ) { sendAJAXCommand( formUrl( this.form) ); }"/>
			</form>
		</td>


		<td width="5%" class="box" nowrap="nowrap" align="center">
			Группы: ${u:objectTitleList( ctxCustomerGroupList, customer.groupIds )}
		</td>

		--%>

		<div>
			Создан: <b><fmt:formatDate value="${customer.createdDate}" pattern="dd.MM.yyyy"/></b>
		</div>

		<div class="in-inline-block">
			<c:url var="url" value="customer.do">
				<c:param name="id" value="${customer.id}"/>
				<c:param name="action" value="customerGet"/>
			</c:url>
			<c:set var="editCommand">openUrlToParent('${url}', $('#${uiid}') )</c:set>

			<c:url var="deleteAjaxUrl" value="customer.do">
				<c:param name="action" value="customerDelete"/>
				<c:param name="id" value="${customer.id}"/>
			</c:url>

			<c:set var="deleteAjaxCommandAfter">
				bgcrm.closeObject = null;
				removeCommandDiv( 'customer-${customer.id }' );
				window.history.back();
			</c:set>

			<c:url var="entityLogCommand" value="../user/parameter.do">
				<c:param name="action" value="entityLog"></c:param>
				<c:param name="id" value="${form.id}"></c:param>
				<c:param name="type" value="customer"></c:param>
				<c:param name="returnUrl" value="${form.requestUrl}"></c:param>
			</c:url>

			<button class="btn-white btn-small ml1 mr1" onclick="openUrlToParent( '${entityLogCommand}',  $('#${uiid}') );">${l.l('Лог изменений')}</button>

			<p:check action="ru.bgcrm.struts.action.CustomerAction:customerMerge">
				<c:set var="uiid" value="${u:uiid()}"/>
				<div>
					<c:set var="mergeButtonUiid" value="${u:uiid()}"/>
					<c:set var="mergeFormUiid" value="${u:uiid()}"/>
					<c:set var="mergeCustomersUiid" value="${u:uiid()}"/>

					<c:set var="script">
						$('#${mergeButtonUiid}').toggle();
						$('#${mergeFormUiid}').toggle();

						var customerList = openedObjectList( {'selected': ['customer-${customer.id}'], 'typesInclude' : ['customer'] } );

						var html = '<li value=\'-1\'>-- выберите контрагента --</li>';
						$.each(customerList, function() {
							html += '<li value=\'' + this.id + '\'>' + this.title + '</li>';
						});

						$('#${mergeCustomersUiid} ul.drop' ).html( html );
						uiComboSingleInit( $('#${mergeCustomersUiid}') );
					</c:set>
					<button class="btn-white btn-small" onclick="${script}" id="${mergeButtonUiid}">Слияние</button>

					<div style="display:none" id="${mergeFormUiid}">
						<form action="customer.do">
							<input type="hidden" name="action" value="customerMerge"/>
							<input type="hidden" name="customerId" value="${customer.id}"/>

							Слить с:
							<ui:combo-single hiddenName="mergingCustomerId" id="${mergeCustomersUiid}"/>

							<button type="button" class="btn-grey ml1"
								onclick="
								if (!(this.form.mergingCustomerId.value > 0)) {
										alert('Выберите контрагента');
										return false;
								}
								if( confirm( 'Вы уверены, что хотите объединить?' ) && sendAJAXCommand( formUrl(this.form)))
								{
									alert('Слияние успешно');
									removeCommandDiv( 'customer-'.concat(this.form.mergingCustomerId.value) );
									openUrlContent( '${form.requestUrl}' );
								}">ОК</button>
							<button type="button" class="btn-grey ml05 mr1" onclick="$('#${mergeButtonUiid}').toggle(); $('#${mergeFormUiid}').toggle();">Отмена</button>
						</form>
					</div>
				</div>
			</p:check>

			<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
		</div>
	</div>

	<div id="customerViewTabs">
		<ul></ul>
	</div>
</c:if>