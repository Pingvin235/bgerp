<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
<c:set var="info" value="${form.response.data.info}"/>

<html:form action="/user/plugin/bgbilling/proto/ipn" style="width: 100%; height: 100%;" styleId="${uiid}">
	<input type="hidden" name="action" value="gateRuleEdit"/>
	<html:hidden property="moduleId"/>
	<html:hidden property="contractId"/>
	<html:hidden property="billingId"/>
	<html:hidden property="gateId"/>
	<html:hidden property="gateTypeId"/>
	<html:hidden property="returnUrl"/>
	
	<h1>Выберите шлюз</h1>
	
	<div style="background-color: #ffffff; cursor: pointer; overflow: auto;" class="layout-height-rest">
		<c:forEach var="node" items="${form.response.data.gateList}">
			<c:set var="node" value="${node}" scope="request"/>
			<c:set var="level" value="0" scope="request"/>
			
			<jsp:include page="gate_select_item.jsp"/>
		</c:forEach>
	</div>	
	
	<div class="mt1">
		<button type="button" class="btn-grey" onclick="openUrlToParent( formUrl( this.form ), $('#${uiid}') )">ОК</button>
		<button type="button" class="btn-grey ml1" onclick="openUrlToParent( '${form.returnUrl}', $('#${uiid}') )">${l.l('Отмена')}</button>
	</div>
	
	<%@ include file="/WEB-INF/jspf/layout_process.jsp"%>		
</html:form>