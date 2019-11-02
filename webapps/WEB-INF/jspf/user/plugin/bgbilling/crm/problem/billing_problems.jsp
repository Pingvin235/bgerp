<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
<c:set var="dbInfo" value="${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager}"/>

<%-- фиксируем форму в pageContext а то реквест после импортов меняется --%> 
<c:set var="form" value="${form}"/>

<c:set var="showOnly" value="${not empty form.param.showOnly}"/>

<div id="${uiid}">
	<div id="table" style="width: 100%;">
		<c:if test="${not showOnly}">
			<form action="plugin/bgbilling/proto/billingCrm.do">
				<input type="hidden" name="action" value="getRegisterProblem"/>
				<input type="hidden" name="problemId" value="new"/> 
				<input type="hidden" name="processId" value="${form.param.processId}"/> 
				<input type="hidden" name="description" value="${form.param.description}"/> 
				<div style="display:table-cell; width: 100%;" class="tableIndent">
					<select name="billingId" style="width: 100%;">
						<option value="-1">-- выберите биллинг --</option>
						<c:forEach items="${dbInfo.dbInfoList}" var="db">
							<option value="${db.id}">${db.title}</option>
						</c:forEach>
					</select>
				</div>
				<div style="display:table-cell;" class="tableIndent">	
					<input type="button" onclick="bgbilling_editProblem(formUrl( this.form ), $(this.form).find('option:selected').val());" value="Создать"/>
				</div>
			</form>
		</c:if>
		<div>
			<table style="width: 100%;" id="${uiid}table_data" class="data">
				<tr>
					<c:set var="columnCount" value="9"/>
				
					<c:if test="${not showOnly}">
						<td width="30">&#160;</td>
						<c:set var="columnCount" value="${columnCount - 1}"/>
					</c:if>
					<td>Биллинг</td>
					<td>ID</td>
					<td>Группа</td>
					<td>Статус</td>
					<td>Статус(время)</td>
					<td>Статус(польз.)</td>
					<td>Длительность</td>				
				</tr>

				<c:forEach items="${form.response.data.list}" var="item" varStatus="status">
					<c:url var="problemInfoUrl" value="plugin/bgbilling/proto/billingCrm.do">
						<c:param name="action" value="registerProblemListItem" />
						<c:param name="billingId" value="${fn:substringAfter( item.linkedObjectType, ':' ) }" /> 
						<c:param name="problemId" value="${item.linkedObjectId }" />
						<c:param name="showOnly" value="${showOnly}" />
						<c:param name="processId" value="${form.param.processId}" />
					</c:url>
					<script>
						$('#${uiid}table_data > tbody').append( getAJAXHtml( '${problemInfoUrl}' ));
					</script>
				</c:forEach>
			</table>
		</div>	
	</div>
	<div id="editor">
	</div>
</div>