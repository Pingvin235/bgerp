<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<table width="100%" class="hdata">
	<tr class="header">
		<td colspan="7" nowrap="nowrap">
		Cессии логина ${form.param.login}
		<button style="border:none; background:transparent; cursor: pointer; text-decoration:underline;" 
			onclick="$('#${form.param.uiid}sessionErrorList').empty();">[закрыть]</button> 
		</br>
		Активные 
		</td>
	</tr>
	
	<tr class="header">
		<td>Начало</td>
		<td>Окончание</td>
		<td>Время</td>
		<td>IP адрес</td>		
		<td>С номера / На номер</td>
		<td>RADIUS</td>
	</tr>	
	
	<c:forEach var="dialUpSession" items="${form.response.data.activeSessionList}">
        <c:set var="activeSessionStyle" value=""/>
        <c:if test="${dialUpSession.active eq 'true'}">
            <c:set var="activeSessionStyle" value="style='background: #ACEAA9;'"/>
        </c:if>

        <tr align="center" ${activeSessionStyle}>
       		<fmt:formatDate value="${dialUpSession['sessionStart']}" var="sessionStart" pattern="dd.MM.yyyy HH:mm:ss"/>
    		<td>${sessionStart}</td>
    		<fmt:formatDate value="${dialUpSession['sessionStop']}" var="sessionStop" pattern="dd.MM.yyyy HH:mm:ss"/>
    		<td>${sessionStop}</td>
    		<td>${dialUpSession['sessionTime']}</td>
    		<td>${dialUpSession['ipAddress'].getHostAddress()}</td>
    		<td>${dialUpSession['fromNumber']} / ${dialUpSession['toNumber']}</td>
    	
    		<c:url var="radiusLogUrl" value="plugin/bgbilling/proto/dialup.do">
    			<c:param name="action" value="radiusLog" />
    			<c:param name="splitter" value="</br>" />
    			<c:param name="moduleId" value="${form.param.moduleId}" />
    			<c:param name="billingId" value="${form.param.billingId}" />
    			<c:param name="sessionStart" value="${dialUpSession['sessionStart']}" />
    			<c:param name="sessionId" value="${dialUpSession['radiusLogId']}" />
    			<c:param name="contractId" value="${form.param.contractId}" />
    		</c:url> 							
    		<td nowrap="nowrap">									
    			<input type="button"  value="Show Log" onclick="if($('#${form.param.contractId}-${dialUpSession['radiusLogId']}-radiusLog').children().size()<=0) {openUrlTo('${radiusLogUrl}', $('#${form.param.contractId}-${dialUpSession['radiusLogId']}-radiusLog') );} else {$('#${form.param.contractId}-${dialUpSession['radiusLogId']}-radiusLog').toggle();} " />
    			<c:if test="${dialUpSession.active eq 'true' }">
    				<c:url var="terminateSessionUrl" value="plugin/bgbilling/proto/dialup.do">
    					<c:param name="action" value="terminateSession" />
    					<c:param name="moduleId" value="${form.param.moduleId}" />
    					<c:param name="billingId" value="${form.param.billingId}" />
    					<c:param name="recordId" value="${dialUpSession['id']}" />
    				</c:url>
    				<p:check action="ru.bgcrm.plugin.bgbilling.proto.struts.action.DialUpAction:terminateSession">
    					<input type="button" value="Term" onclick="openUrl('${terminateSessionUrl}'); this.parentNode.innerHTML = 'запрос отправлен';" />
    				</p:check>
    			</c:if>
    		</td>
		</tr>
		<tr style="border-spacing:0px;">
			<td colspan="6" style="padding: 0em;">
				<div id="${form.param.contractId}-${dialUpSession['radiusLogId']}-radiusLog">
				</div>	
			</td>
		</tr>
	</c:forEach>
	
	<tr class="header">
		<td colspan="6" nowrap="nowrap">За последние ${form.param.sessionDays} суток.

			<form action="/user/plugin/bgbilling/proto/dialup.do">
				<input type="hidden" name="action" value="sessionList"/>
				<input type="hidden" name="billingId" value="${form.param.billingId}"/>
				<input type="hidden" name="contractId" value="${form.param.contractId}"/>
				<input type="hidden" name="moduleId" value="${form.param.moduleId}"/>
				<input type="hidden" name="loginId" value="${form.param.loginId}"/>
				<input type="hidden" name="login" value="${form.param.login}"/>
				<input type="hidden" name="uiid" value="${form.param.uiid}"/>
				
				Показать сессии за:
				
				<select name="sessionDays" onchange="openUrlTo(formUrl( this.form), $('#${form.param.uiid}sessionErrorList') );scrollToElementById('${form.param.uiid}sessionErrorList');">
				  <option disabled selected style='display:none;'>...</option>
				  <option>1</option>
				  <option>7</option>
				  <option>14</option>
				  <option>21</option>
				  <option>28</option>
				</select>
				
				суток.
			</form>							
		</td>
	</tr>
	<tr class="header">
		<td>Начало</td>
		<td>Окончание</td>
		<td>Время</td>
		<td>IP адрес</td>		
		<td>С номера / На номер</td>
		<td>RADIUS</td>
	</tr>
	
	<c:forEach var="dialUpSession" items="${form.response.data.sessionList}">
		<tr align="center">
			<fmt:formatDate value="${dialUpSession['sessionStart']}" var="sessionStart" pattern="dd.MM.yyyy HH:mm:ss"/>
			<td>${sessionStart}</td>
			<fmt:formatDate value="${dialUpSession['sessionStop']}" var="sessionStop" pattern="dd.MM.yyyy HH:mm:ss"/>
			<td>${sessionStop}</td>
			<td>${dialUpSession['sessionTime']}</td>
			<td>${dialUpSession['ipAddress'].getHostAddress()}</td>
			<td>${dialUpSession['fromNumber']} / ${dialUpSession['toNumber']}</td>
		
			<c:url var="radiusLogUrl" value="/user/plugin/bgbilling/proto/dialup.do">
				<c:param name="action" value="radiusLog" />
				<c:param name="splitter" value="</br>" />
				<c:param name="moduleId" value="${form.param.moduleId}" />
				<c:param name="billingId" value="${form.param.billingId}" />
				<c:param name="sessionStart" value="${dialUpSession['sessionStart']}" />
				<c:param name="sessionId" value="${dialUpSession['radiusLogId']}" />
				<c:param name="contractId" value="${form.param.contractId}" />
			</c:url> 							
			<td>									
				<input type="button" style="width: 100%" value="Show Log" onclick="if($('#${form.param.contractId}-${dialUpSession['radiusLogId']}-radiusLog').children().size()<=0) {openUrlTo('${radiusLogUrl}', $('#${form.param.contractId}-${dialUpSession['radiusLogId']}-radiusLog') );} else {$('#${form.param.contractId}-${dialUpSession['radiusLogId']}-radiusLog').toggle();} " />
			</td>
		</tr>
		<tr style="border-spacing:0px;">
			<td colspan="6" style="padding: 0em;">
				<div id="${form.param.contractId}-${dialUpSession['radiusLogId']}-radiusLog">
				</div>	
			</td>
		</tr>
	</c:forEach>
		
</table>
