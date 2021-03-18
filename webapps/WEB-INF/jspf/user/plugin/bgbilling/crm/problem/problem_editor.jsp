<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%-- фиксируем форму в pageContext а то реквест после импортов меняется --%> 
<c:set var="form" value="${form}"/>
<c:set var="problem" value="${form.response.data.problem}"/>

<c:set var="dbInfo" value="${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager}"/>
<c:set var="billing" value="${dbInfo.dbInfoMap[form.param.billingId]}"/>
<c:set var="problemStatusList" value="${billing.setup['crm.problem.status.list'] }"/>

<c:if test="${empty problemStatusList}">
	<c:set var="problemStatusList" value="0:открыта;1:принята;2:закрыта"/>
</c:if>

<html:form action="/user/plugin/bgbilling/proto/billingCrm" styleId="form">
	<input type="hidden" name="action" value="updateRegisterProblem"/>
	<input type="hidden" name="problemId" value="${problem.getId()}"/>
	<input type="hidden" name="billingId" value="${form.param.billingId}"/>

	<table style="width: 100%">
		<tr>
			<td width="200px">
				<div style="display: table-cell;">
					<b>Статус:</b>
				</div>
				
				<div style="display: table-cell; width: 50%;">
					<html:select property="statusId" value="${problem.getStatusCode()}">
						<c:forTokens var="token" items="${problemStatusList }" delims=";">
							<html:option value="${fn:substringBefore( token, ':' )}">${fn:substringAfter( token, ':' )}</html:option>							
						</c:forTokens>				
					</html:select>
				</div>
				
				<div style="display: table-cell;">	
					<b>Срочность:</b>
				</div>
				
				<div style="display: table-cell; width: 50%;">	
					<html:select property="urgency" value="${problem.getUrgency()}">
						<c:forTokens items="-2,-1,0,1,2" delims="," var="item">
							<html:option value="${item}">${item}</html:option>
						</c:forTokens>
					</html:select>
				</div>	
			</td>			
			
			<c:if test="${not empty form.param.description}">
				<c:set var="comment" value="${form.param.description}"/>
			</c:if>
			
			<td rowspan="3" style="height: 100%; width: 100%;">
				<table class="nopad" style="width: 100%; height: 400;">
					<tr><td><b>Комментарий:</b></td></tr>
					<tr height="100%"><td><textarea name="problemComment" style="width: 100%; height: 100%;">${comment}</textarea></td></tr>
				</table>
			</td>	
		</tr>
		<tr>
			<td>
				<div style="display: table-cell;"><b>Группа:</b></div>
				<div id="${form.param.billingId}-${problem.getId()}-registerGroupList" style="display: table-cell; width: 100%;">
					<c:set var="group" value="0"/>
					<c:if test="${ not empty problem.getGroup()}">
						<c:set var="group" value="${problem.getGroup()}"/>
					</c:if>
					<script>
						bgbilling_getRegistredGroups('${problem.getId()}','${form.param.billingId}','${group}');
					</script>
				</div>
			</td>
		</tr>
		<tr>
			<td>		
				<b>Исполнители:</b><br/>
				<div id="${form.param.billingId}-${problem.getId()}-registerExecutorList" style="overflow: auto; height: 300px;" >
					<c:set var="executors" value="0"/>
					<c:if test="${ not empty problem.getExecutors()}">
						<c:set var="executors" value="[${problem.getExecutors()}]"/>
					</c:if>
					<script>
						bgbilling_getRegistredExecutors('${form.param.billingId}-${problem.getId()}-registerExecutorList','${form.param.billingId}',$('select[name=registerGroupId]:visible').children(':selected').val(),${executors});
					</script>
				</div>
			</td>
		</tr>
		<tr>
			<td colspan="2" align="right">
				<input type="button" value="OK" onclick="bgbilling_registerProblemUpdate( formUrl( this.form ), '${form.param.processId}', '${form.param.billingId}', '${problem.getId()}' )"/>
				<input type="button" value="Отмена" onclick="refreshCurrentSelectedTab();"/>
			</td>
		</tr>
	</table>
</html:form>