<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
<c:set var="info" value="${form.response.data.info}"/>

<div style="display: table; height: 100%;" id="${uiid}" class="in-table-cell in-va-top">
	<div style="min-width: 350px;">
		<h2>Статус</h2>
		
		<html:form action="/user/plugin/bgbilling/proto/ipn" style="width: 100%;">
			<input type="hidden" name="action" value="gateStatusUpdate"/>
			<html:hidden property="moduleId"/>
			<html:hidden property="contractId"/>
			<html:hidden property="billingId"/>
	
			<u:sc>
				<c:set var="valuesHtml">
					<li value="0">${l.l('Открыт')}</li>
					<li value="1">${l.l('Закрыт')}</li>
					<li value="2">${l.l('Заблокирован')}</li>
					<li value="3">${l.l('Жесткая блокировка')}</li>
					<li value="4">${l.l('Удален')}</li>
				</c:set>
				<c:set var="hiddenName" value="status"/>
				<c:set var="value" value="${info.statusId}"/>
				<c:set var="style" value="width: 100%;"/>
				<c:set var="onSelect" value="if( sendAJAXCommand( formUrl( $('#${uiid}').find('form')[0] ) ) ){ openUrlToParent( '${form.requestUrl}', $('#${uiid}') ) }"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
			</u:sc>
		</html:form> 
		
		<h2>Шлюзы</h2>
		
		<c:url var="baseEditUrl" value="plugin/bgbilling/proto/ipn.do">
			<c:param name="action" value="gateRuleEdit"/>
			<c:param name="moduleId" value="${form.param.moduleId}"/>
			<c:param name="contractId" value="${form.param.contractId}"/>
			<c:param name="billingId" value="${form.param.billingId}"/>
			<c:param name="returnUrl" value="${form.requestUrl}"/>
		</c:url>
		
		<c:url var="baseDeleteUrl" value="plugin/bgbilling/proto/ipn.do">
			<c:param name="action" value="gateRuleDelete"/>
			<c:param name="moduleId" value="${form.param.moduleId}"/>
			<c:param name="contractId" value="${form.param.contractId}"/>
			<c:param name="billingId" value="${form.param.billingId}"/>			
		</c:url>
		
		<button class="btn-green mb1" onclick="openUrlToParent( '${baseEditUrl}', $('#${uiid}') )">+</button>
		
		<div class="layout-height-rest" style="overflow: auto;">
			<table class="data" style="width: 100%;">
				<tr>
					<td>&nbsp;</td>
					<td>Шлюз</td>
				</tr>
				<c:forEach var="item" items="${info.gateList}">
					<tr>
						<td nowrap="nowrap">
							<u:sc>
								<c:url var="url" value="${baseEditUrl}">
									<c:param name="id" value="${item.id}"/>
									<c:param name="gateId" value="${item.gateId}"/>
									<c:param name="gateTypeId" value="${item.typeId}"/>								 
								</c:url>
								<c:set var="editCommand" value="openUrlToParent('${url}',$('#${uiid}'))"/>
							
								<c:url var="deleteAjaxUrl" value="${baseDeleteUrl}">
									<c:param name="action" value="gateRuleDelete"/>
									<c:param name="id" value="${item.id}"/>
								</c:url>
								<c:set var="deleteAjaxCommandAfter" value="openUrlToParent('${form.requestUrl}',$('#${uiid}'))"/>
								<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
							</u:sc>
						</td>
						<td width="100%">
							${item.title}
						</td>
					</tr>	
				</c:forEach>
			</table>
		</div>		
		
		<%--
		<select class="layout-height-rest" multiple="multiple" style="width: 100%;">
			<c:forEach var="item" items="${info.gateList}">
				<option id="${item.id}" gateId="${item.gateId}" gateTypeId="${item.typeId}">${item.title}</option>
			</c:forEach>		
		</select>
		<div class="hint">Для правки шлюза - двойной клик.</div>
		
		<script>
			$(function()
			{
				$('#${uiid} select').on( 'dblclick', 'option', function()
				{
					var id = $(this).attr('id');
					var gateId = $(this).attr('gateId');
					var gateTypeId = $(this).attr('gateTypeId');
					
					var url = "${baseEditUrl}&id=" + id + "&gateId=" + gateId + "&gateTypeId=" + gateTypeId;
					openUrlToParent( url, $('#${uiid}') );
				})		
			})
		</script>
		--%>
	</div>
	<div style="width: 100%; overflow: auto;" class="pl1">
		<h2>Статистика состояния шлюза</h2>
		<table style="width: 100%;" class="data">
			<tr>
				<td nowrap="nowrap">Дата и время</td>
				<td>Действие</td>
				<td>Исполнитель</td>
				<td>Комментарий</td>
			</tr>
			<c:forEach var="item" items="${info.statusLog}">
				<tr> 
					<td nowrap="nowrap">${u:formatDate( item.time, 'ymdhms' )}</td>
					<td>${item.statusTitle}</td>
					<td>${item.user}</td>
					<td>${item.comment}</td>
				</tr>
			</c:forEach>
		</table>
	</div>
</div>

<%@ include file="/WEB-INF/jspf/layout_process.jsp"%>