<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div style="height: 100%; width: 100%; display: table-row;">
	<div style="vertical-align: top; display: table-cell; min-width: 25em; max-width: 25em;" class="in-w100p">
		<c:set var="allowedForms" value="${u.toSet(form.permission['allowedForms'])}" scope="request"/>
		<c:set var="defaultForm" value="${form.permission['defaultForm']}"/>

		<u:sc>
			<c:set var="onSelect" value="$$.search.onObjectSelect()"/>

			<ui:combo-single id="searchForm" hiddenName="searchMode" prefixText="${l.l('Искать')}:" onSelect="${onSelect}">
				<jsp:attribute name="valuesHtml">
					<c:if test="${empty allowedForms or allowedForms.contains('customer')}">
						<li value="customer">${l.l('Customer')}</li>
					</c:if>
					<c:if test="${empty allowedForms or allowedForms.contains('process')}">
						<li value="process">${l.l('Process')}</li>
					</c:if>

					<c:set var="mode" value="items" scope="request"/>
					<plugin:include endpoint="user.search.jsp"/>
				</jsp:attribute>
			</ui:combo-single>

			<script>
				$(function () {
					${onSelect}
				})
			</script>
		</u:sc>

		<div id="searchForms">
			<html:form action="${form.requestURI}" styleId="searchForm-customer" styleClass="searchForm in-mb1 mt1 in-w100p">
				<html:hidden property="method" value="customerSearch" />
				<html:hidden property="searchBy" />

				<ui:input-text
					name="title" placeholder="${l.l('Title')}" title="${l.l('Для поиска введите подстороку названия и нажмите Enter')}"
					onSelect="this.form.searchBy.value='title';
							  $$.ajax.load(this.form, '#searchResult')"/>

				<ui:input-text
					name="id" placeholder="ID" title="${l.l('Для поиска введите код контрагента и нажмите Enter')}"
					onSelect="this.form.searchBy.value='id';
							$$.ajax.load(this.form, '#searchResult')"/>

				<%@ include file="search_address_filter.jsp"%>

				<ui:combo-check paramName="textParam" prefixText="${l.l('Param text')}:" list="${customerParamTextList}" styleClass="w100p"/>

				<%@ include file="search_text_filter.jsp"%>

				<div>
					<button type="button" class="btn-white" onclick="$('#searchForm-customer').each (function(){this.reset(); });">${l.l('Очистить')}</button>
				</div>
			</html:form>

			<html:form action="${form.requestURI}" styleId="searchForm-process" styleClass="searchForm in-mb1 mt1 in-w100p">
				<html:hidden property="method" value="processSearch" />
				<html:hidden property="searchBy" />

				<ui:input-text name="id" placeholder="${l.l('Поиск процесса по ID')}"
							title="${l.l('Для поиска введите код процесса и нажмите Enter')}"
							onSelect="this.form.searchBy.value='id';
									  $$.ajax.load(this.form, $('#searchResult')); return false;" />

				<div style="display: flex;">
					<u:sc>
						<%@ include file="process_search_constants.jsp"%>
						<ui:combo-single hiddenName="mode" styleClass="w100p">
							<jsp:attribute name="valuesHtml">
								<li value="${MODE_USER_CREATED}">${l.l('Cозданные мной')}</li>
								<li value="${MODE_USER_CLOSED}">${l.l('Закрытые мной')}</li>
								<li value="${MODE_USER_STATUS_CHANGED}">${l.l('Статус изменён мной')}</li>
							</jsp:attribute>
						</ui:combo-single>
					</u:sc>
					<div class="pl05">
						<ui:button type="out" onclick="this.form.searchBy.value='userId'; $$.ajax.load(this.form, '#searchResult');"/>
					</div>
				</div>

				<div class="separator"/>

				<ui:combo-single hiddenName="open" prefixText="${l.l('process.closed')}:">
					<jsp:attribute name="valuesHtml">
						<li value="1">${l.l('Open')}</li>
						<li value="0">${l.l('Closed')}</li>
						<li value="">${l.l('All')}</li>
					</jsp:attribute>
				</ui:combo-single>

				<ui:combo-check paramName="textParam" prefixText="${l.l('Param text')}:" list="${processParamTextList}" styleClass="w100p"/>

				<%@ include file="search_text_filter.jsp"%>
			</html:form>

			<c:remove var="mode"/>
			<plugin:include endpoint="user.search.jsp"/>
		</div>
	</div>

	<c:if test="${not empty defaultForm}">
		<script>
			$(function () {
				$("#searchForm > ul.drop > li[value='${defaultForm}']").click();
			})
		</script>
	</c:if>

	<div id="searchResult" class="pl1" style="display: table-cell; width: 100%; vertical-align: top;">
		<%--  the search result --%>&#160;
	</div>
</div>

<script>
	$(function() {
		addAddressSearch("#searchForm-customer");
	})
</script>

<shell:title text="${l.l('Поиск')}"/>