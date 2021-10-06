<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<h1>Редактор</h1>

<html:form action="/user/plugin/bgbilling/proto/contractTariff" styleId="${uiid}" styleClass="in-table-cell">
	<html:hidden property="id"/>
	<html:hidden property="action"/>
	<html:hidden property="returnUrl"/>
	<html:hidden property="billingId"/>
	<html:hidden property="contractId"/>
	
	<c:set var="reload" value="openUrlToParent( formUrl( this.form ), $('#${uiid}') )"/>
	<c:set var="onclick">onclick="$(this).toggleClass( 'btn-blue btn-white' ); this.value = $(this).hasClass( 'btn-blue' ) ? 1 : 0; ${reload}"</c:set>

	<div style="white-space: nowrap;">
		<button type="button" class="${form.param.showUsed eq '1' ? 'btn-blue' : 'btn-white'}" 
			name="showUsed" value="${form.param.showUsed}" ${onclick}>Только используемые</button> 
			
		<button type="button" class="${form.param.useFilter eq '1' ? 'btn-blue' : 'btn-white'} ml1" 
			name="useFilter" value="${form.param.useFilter}" ${onclick}>Фильтр по договору</button>	
		
		<c:if test="${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[form.param.billingId].versionCompare( '5.2' ) ge 0}">
			<button type="button" class="${form.param.tariffGroupFilter eq '1' ? 'btn-blue' : 'btn-white'} ml1" 
				name="tariffGroupFilter" value="${form.param.tariffGroupFilter}" ${onclick}>Фильтр по группе тарифа</button>	
		</c:if>
	</div>	
	<div style="width: 100%;" class="pl1">
		<u:sc>
			<c:set var="list" value="${form.response.data.moduleList}"/>
			<c:set var="hiddenName" value="moduleId"/>
			<c:set var="value" value="${form.param.moduleId}"/>
			<c:set var="style" value="width: 100%;"/>
			<c:set var="placeholder" value="Фильтр по модулю"/>
			<c:set var="onSelect" value="${reload}"/>
			<%@ include file="/WEB-INF/jspf/select_single.jsp"%>	
		</u:sc>
	</div>			
</html:form>

<c:set var="contractTariff" value="${form.response.data.contractTariff}"/>

<html:form action="/user/plugin/bgbilling/proto/contractTariff" styleClass="mt1">
	<input type="hidden" name="action" value="updateContractTariff" />
	<html:hidden property="billingId"/>
	<html:hidden property="contractId"/>
	<html:hidden property="id"/>
	
	<div class="in-table-cell">
		<div style="width: 100%">
			<u:sc>
				<c:set var="list" value="${form.response.data.tariffList}"/>
				<c:set var="hiddenName" value="tariffPlanId"/>
				<c:set var="value" value="${contractTariff.tariffPlanId}"/>
				<c:set var="style" value="width: 100%;"/>
				<c:set var="placeholder" value="Тариф"/>
				<%@ include file="/WEB-INF/jspf/select_single.jsp"%>	
			</u:sc>
		</div>	
		
		<div style="white-space: nowrap;" class="pl1">
			${l.l('Позиция')}:
			<input type="text" style="text-align:center; width:50px" name="position" value="${contractTariff.position}"/>
			
			Период c
			<c:set var="editable" value="true"/>
			<input type="text" name="dateFrom" value="${tu.format( contractTariff.dateFrom, 'ymd' )}" id="${uiid}-dateFrom"/>	
			<c:set var="selector" value="#${uiid}-dateFrom"/>	
			<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
			по
			<c:set var="editable" value="true"/>
			<input type="text" name="dateTo" value="${tu.format( contractTariff.dateTo, 'ymd' )}" id="${uiid}-dateTo"/>	
			<c:set var="selector" value="#${uiid}-dateTo"/>	
			<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
		</div>
	</div>	
			
	<div class="mt1">		
		Комментарий: <br>
		<textarea name="comment" rows="10" style="width:100%; resize: vertical;">${contractTariff.comment}</textarea>
	</div>	

	<%@ include file="/WEB-INF/jspf/ok_cancel_in_form.jsp"%>	
</html:form>