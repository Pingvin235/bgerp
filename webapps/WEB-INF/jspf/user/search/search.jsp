<%@page import="ru.bgcrm.dao.process.ProcessDAO"%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="ru.bgcrm.model.IdTitle"%>
<%@ page import="java.util.List"%>

<script>
	$(function() {
		$(".searchForm").hide();
		$("#searchForm-customer").show();

		addAddressSearch("#searchForm-customer");
	})
</script>

<div style="height: 100%; width: 100%; display: table-row;">
	<div style="vertical-align: top; display: table-cell; min-width: 300px;" class="in-w100p">
		<c:set var="allowedForms" value="${u:toSet( form.permission['allowedForms'] )}" scope="request"/>
		<c:set var="defaultForm" value="${form.permission['defaultForm']}"/>
	
		<u:sc>
			<c:set var="id" value="searchForm"/>
			<c:set var="valuesHtml">
				<c:if test="${empty allowedForms or allowedForms.contains( 'customer' ) }">
					<li value="customer">Контрагент</li>
				</c:if>
				<c:if test="${empty allowedForms or allowedForms.contains( 'process' ) }">
					<li value="process">Процесс</li>
				</c:if>	
				
				<c:set var="endpoint" value="user.search.jsp"/>
				<c:set var="mode" value="items" scope="request"/>
				<%@ include file="/WEB-INF/jspf/plugin_include.jsp"%>				
			</c:set>
			<c:set var="hiddenName" value="searchMode"/>
			<c:set var="prefixText" value="Искать:"/>
			<c:set var="onSelect">
				var value = $('#searchForm > input[type=hidden]').val();
				$('.searchForm').hide(); $('#searchForm-' + value).show();
			</c:set>
			<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
		</u:sc>
		
		<%--	
		<table style="width: 250;">
			<tr>
				<td nowrap="nowrap">Объект поиска:</td>
				<td width="100%;">
					<select id="searchMode" onchange="$('.searchForm').hide(); $('#searchForm-' + this.value).show();"	style="width: 100%;">
						<option value="customer">Контрагент</option>
					</select>
				</td>
			</tr>
		</table>
		 --%>

		<div id="searchForms">
			<html:form action="/user/search" method="GET"
				styleId="searchForm-customer" styleClass="searchForm in-mb1 mt1 in-w100p">
				<html:hidden property="action" value="customerSearch" />
				<html:hidden property="searchBy" />

				
				<c:if test="${setup['sphinx.enable'] eq '1'}">
					<c:set var="uiid" value="${u:uiid()}" /> 
					<input type="text" name="searchString" placeholder="Мультипоиск"
							   	onkeypress="if( enterPressed( event ) ){ 
										      this.form.elements['searchBy'].value='any'; 
							   			      openUrl( formUrl( this.form ), '#searchResult' ) }" 
								title="Для поиска введите подстроку названия и нажмите Enter"/>						
				</c:if>
					
							
				<c:set var="minStringLength" value="${setup['searchCustomerTitleMinSubstringLength']}"/>
				<c:if test="${empty minStringLength}">
					<c:set var="minStringLength" value="4"/>
				</c:if>
				 
				<%--
				<input type="text" name="title" placeholder="Наименование"
						   	onkeypress="if( enterPressed( event ) ){ 
									      if( this.value.length < ${minStringLength} ){ alert( 'Строка поиска должна быть ${minStringLength} и более симоволов!' ); return; }
									      this.form.elements['searchBy'].value='title'; 
						   			      openUrl( formUrl( this.form ), '#searchResult' ) }" 
							title="Для поиска введите подстороку названия и нажмите Enter"/>
				--%>
				
				<ui:input-text 
				    name="title" placeholder="Наименование" title="Для поиска введите подстороку названия и нажмите Enter"
				    onSelect="if (this.value.length < ${minStringLength}) {alert('Строка поиска должна быть ${minStringLength} и более симоволов!' ); return;}
                              this.form.elements['searchBy'].value='title'; 
                              openUrl(formUrl(this.form), '#searchResult')"/>

				<%@ include file="search_address_filter.jsp"%>
				<ui:input-text 
				    name="id" placeholder="ID" title="Для поиска введите код контрагента и нажмите Enter"
					onSelect="this.form.elements['searchBy'].value='id'; 
						      openUrl( formUrl( this.form ), '#searchResult' )" />
				<%--
				<tr>
					<td colspan="2">
							Группы:<br/>	
							 
							<c:set var="uiid" value="${u:uiid()}" /> 
							
							<div style="height: 350px; overflow: auto;">
								<c:set var="list" value="${ctxCustomerGroupList}" />
								<c:set var="paramName" value="groupId" />
								<%@ include file="/WEB-INF/jspf/check_list.jsp"%>									
							</div>
							
							<div style="width: 100%; text-align: right;">
								<input type="button" 
										onclick="this.form.elements['searchBy'].value='group'; openUrl( formUrl( this.form ), '#searchResult' )" 
										value="Найти по группам"/>
							</div>
							
							<script>
								addCustomerSearch($('#${uiid}'));
							</script>
					</td>
				</tr>
				--%>
				
				<div>
					<button type="button" class="btn-white" onclick="$('#searchForm-customer').each (function(){this.reset(); });">Очистить</button>
				</div>
			</html:form>
			
			<html:form action="/user/search" method="GET"
				styleId="searchForm-process" styleClass="searchForm in-mb1 mt1 in-w100p">
				<html:hidden property="action" value="processSearch" />
				<html:hidden property="searchBy" />
				<ui:input-text name="id" placeholder="Поиск процесса по ID"
							title="Для поиска введите код процесса и нажмите Enter"
							onSelect="this.form.elements['searchBy'].value='id'; 
						   			  openUrl( formUrl( this.form ), '#searchResult' ); return false;" />		
				<div style="display: flex;">
					<u:sc>
						<%@ include file="process_search_constants.jsp"%>						
						<ui:combo-single hiddenName="mode" style="width: 100%;">
							<jsp:attribute name="valuesHtml">
								<li value="${MODE_USER_CREATED}">Cозданные мной</li>
								<li value="${MODE_USER_CLOSED}">Закрытые мной</li>
								<li value="${MODE_USER_STATUS_CHANGED}">Статус изменён мной</li>
							</jsp:attribute>
						</ui:combo-single>
					</u:sc>
					<div class="pl05">
						<button type="button" class="btn-white btn-slim" style="white-space: nowrap;"
							onclick="this.form.elements['searchBy'].value='userId'; $$.ajax.load(this.form, '#searchResult');"
							title="Вывести">
								<%-- &#x25B6; Unicode стрелки вправо, но слишком чёрная --%>
								&nbsp;<img src="/images/arrow-right.png"> 
						</button>					
					</div>				 
				</div>				
			</html:form>
			
			<c:set var="endpoint" value="user.search.jsp"/>
			<c:remove var="mode"/>
			<%@ include file="/WEB-INF/jspf/plugin_include.jsp"%>
		</div>
	</div> 
	
	<c:if test="${not empty defaultForm}">
		<script>
			$(function()
			{
				$("#searchForm > ul.drop > li[value='${defaultForm}']").click();
			})
		</script>
	</c:if>	
	
    <div id="searchResult"
		class="pl1"
		style="display: table-cell; width: 100%; vertical-align: top;">
		<%--  сюда вставляются DIV ки --%>
		&#160;
	</div>
</div>

<c:set var="title" value="Поиск"/>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>