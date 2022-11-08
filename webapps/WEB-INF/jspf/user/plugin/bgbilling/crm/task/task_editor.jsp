<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${ not empty form.response.data.task}">
	<c:set var="task" value="${form.response.data.task}" />
</c:if>

<script>
     $(function() {
         var $taskEditorTabs = $( "#taskEditorTabs-${form.param.billingId}-${form.param.contractId}-Tabs" ).tabs( {spinner: '', cache:true} );

          <c:url var="url" value="empty.do">
            <c:param name="forwardFile" value="/WEB-INF/jspf/user/plugin/bgbilling/crm/task/task_comment.jsp"/>
	      	<c:param name="billingId" value="${form.param.billingId}"/>
		  	<c:param name="contractId" value="${form.param.contractId}"/>
		  	<c:param name="taskComment" value="${task.getComment()}"/>
		  </c:url>
	      $taskEditorTabs.tabs( "add", "${url}", "Комментарий" );

	      <c:url var="url" value="empty.do">
	      	<c:param name="forwardFile" value="/WEB-INF/jspf/user/plugin/bgbilling/crm/task/task_resolution.jsp"/>
	      	<c:param name="billingId" value="${form.param.billingId}"/>
		  	<c:param name="contractId" value="${form.param.contractId}"/>
		  	<c:param name="taskResolution" value="${task.getResolution()}"/>
		  </c:url>
		  $taskEditorTabs.tabs( "add", "${url}", "Резолюция" );

	      <c:url var="url" value="empty.do">
	      	<c:param name="forwardFile" value="/WEB-INF/jspf/user/plugin/bgbilling/crm/task/task_log.jsp"/>
	      	<c:param name="billingId" value="${form.param.billingId}"/>
		  	<c:param name="contractId" value="${form.param.contractId}"/>
		  	<c:param name="logOpen" value="${task.getLog().getOpen()}"/>
		  	<c:param name="logClose" value="${task.getLog().getClose()}"/>
		  	<c:param name="logAccept" value="${task.getLog().getAccept()}"/>
		  	<c:param name="logLastModify" value="${task.getLog().getLastModify()}"/>
		  </c:url>
	      $taskEditorTabs.tabs( "add", "${url}", "Лог" );

     });
</script>
<div>
    <h2>Редактор</h2>
	<form action="plugin/bgbilling/proto/billingCrm.do"
		id="${form.param.billingId}-${form.param.contractId}-createTaskForm">

		<table class="box" width="100%">
			<tr>
				<td valign="top" width="100%">
					<table class="box" width="100%">
						<tr>
							<td colspan="4"><b>${l.l('Договор')}:</b></td>
						</tr>
						<tr>
							<td valign="bottom"><input type="text" style="width: 100%" name="contractTitle" value="${task.getContract()}" disabled /></td>
							<td width="100%" colspan="3" valign="bottom" align="center">
								Адрес:
								<div id="${form.param.billingId}-${form.param.contractId}-contractAddressList">
									<c:set var="apid" value="0"/>
									<c:if test="${task.getAddressPId() > 0}">
										<c:set var="apid" value="${task.getAddressPId()}"/>
									</c:if>
									<script>
										bgbilling_getContractAddress('${form.param.contractId}','${form.param.billingId}',${apid});
									</script>
								</div>
							</td>
						</tr>
						<tr>
							<td colspan="4"><b>Исходные данные:</b></td>
						</tr>
						<tr>
							<td align="center" nowrap="nowrap">Id проблемы</td>
							<td align="center" nowrap="nowrap">Тип задачи</td>
							<td align="center" nowrap="nowrap">Срок выполнения</td>
							<td align="center" nowrap="nowrap">Дата выполнения</td>
						</tr>
						<tr>
							<td>
								<input type="text" style="width: 100%; text-align:center" name="taskId" value="${task.getId()}" disabled />
							</td>
							<td width="100%">
								<div id="${form.param.billingId}-${form.param.contractId}-taskTypeList">
									<c:set var="type" value="0"/>
									<c:if test="${task.getTypeId() > 0 }">
										<c:set var="type" value="${task.getTypeId()}"/>
									</c:if>
									<script>
										bgbilling_getTaskTypes('${form.param.contractId}','${form.param.billingId}',${type});
									</script>
								</div>
							</td>
							<td>
								 <c:set var="uiid" value="${u:uiid()}" />
								 <input	type="text" style="width: 100%" name="targetDate" value="${task.getTargetDateTime()}" id="${uiid}" />
								 <c:set	var="selector" value="#${uiid}" />
								 <c:set var="type" value="ymdhm" />
								 <%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
							</td>
							<td>
								<c:set var="uiid" value="${u:uiid()}" />
								<input type="text" style="width: 100%" name="executeDate" value="${task.getExecuteDate()}" id="${uiid}" />
								<c:set var="selector" value="#${uiid}" />
								<c:set var="type" value="ymd" />
								<%@ include	file="/WEB-INF/jspf/datetimepicker.jsp"%>
							</td>
						</tr>
						<tr>
							<td colspan="4">
								<div
									id="taskEditorTabs-${form.param.billingId}-${form.param.contractId}-Tabs">
									<ul></ul>
								</div>
							</td>
						</tr>
					</table>
				</td>

				<td valign="top">
					<table class="box" width="500px">
						<tr>
							<td colspan="2"><b>Исполнение:</b></td>
						</tr>
						<tr>
							<td width="30px">Статус:</td>
							<td>
								<div class="tableIndent">
									<select name="statusId" style="width: 100%">
										<option value="0">Открыта</option>
										<option value="1">Принята</option>
										<option value="2">Закрыта</option>
									</select>
									<script>
										bgbilling_updateTaskStatus(${task.statusCode});
									</script>
								</div>
							</td>
						</tr>
						<tr>
							<td width="30px">Группа:</td>
							<td>
								<div id="${form.param.billingId}-${form.param.contractId}-registerGroupList">
									<c:set var="group" value="0"/>
									<c:if test="${ not empty task.getGroupId()}">
										<c:set var="group" value="${task.getGroupId()}"/>
									</c:if>
									<script>
										bgbilling_getRegistredGroups('${form.param.contractId}','${form.param.billingId}',${group});
									</script>
								</div>
							</td>
						</tr>
						<tr>
							<td nowrap="nowrap" align="center" colspan="2">Исполнители:
								<div>
									<div id="${form.param.billingId}-${form.param.contractId}-registerExecutorList"	class="box"	style="overflow: auto; width: inherit; height: 280px;">
										<c:set var="executors" value="0"/>
										<c:if test="${ task.getExecutors().length() > 0}">
											<c:set var="executors" value="[${task.getExecutors() }]"/>
										</c:if>
										<script>
											bgbilling_getRegistredExecutors('${form.param.billingId}-${form.param.contractId}-registerExecutorList','${form.param.billingId}',$('select[name=registerGroupId]:visible').children(':selected').val(),${executors});
										</script>
									</div>
									</br>
									<input type="button" value="Все" onclick="$('input[name=executor]:visible').attr('checked',true);" />
									<input type="button" value="Сброс" onclick="$('input[name=executor]:visible').attr('checked',false)" />
									<input type="button" value="Инвертировать" onclick="$('input[name=executor]:visible').each( function() { $(this).attr('checked', !$(this).attr('checked')); });" />
								</div>
							</td>
						</tr>
					</table>
				</td>
			</tr>
		</table>

		<input type="hidden" name="action" value="updateRegisterTask" />
		<input type="hidden" name="billingId" value="${form.param.billingId }" />
		<input type="hidden" name="contractId" value="${form.param.contractId }" />
		<input type="hidden" name="taskComment" value="${ task.getComment() }" />
		<input type="hidden" name="taskResolution" value="${ task.getResolution() }" />

		<c:set var="crmTabs" value="crmTabs-${form.param.billingId}-${form.param.contractId}-Tabs"/>
		<input type="button" value="OK" onClick="if( sendAJAXCommand( formUrl( this.form ) ) ){ $$.ajax.load('${form.param.returnUrl}',$(this.form).parent());}" />
		<input type="button" value="Отмена"	onClick="$$.ajax.load('${form.param.returnUrl}',$(this.form).parent())" />
	</form>
</div>

