<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="enable"><p:check action="ru.bgcrm.plugin.bgbilling.proto.struts.action.DialUpAction:updateLoginRadiusInfo">enable</p:check></c:set>
 	
<c:set var="radiusInfo" value="${form.response.data.radiusInfo}"/>

<html:form action="/user/plugin/bgbilling/proto/dialup.do" styleClass="${enable}" style="vertical-align: top;">
	<input type="hidden" name="action" value="updateLoginRadiusInfo" />
	<html:hidden property="contractId"/>
	<html:hidden property="billingId"/>
	<html:hidden property="moduleId"/>
	<html:hidden property="id"/>
	
	<h2>RADIUS</h2>
	
	<table style="width: 100%;">
		<tr>
			<td width="50%">
				<u:sc>
					<c:set var="valuesHtml">
						<li value="0">Глобальные + локальные</li>
						<li value="1">Только локальные</li>
					</c:set>
					<c:set var="hiddenName" value="attributeMode"/>
					<c:set var="value" value="${radiusInfo.attributeMode }"/>
					<c:set var="style" value="width: 100%;"/>
					<c:set var="prefixText" value="Присв. атрибуты:"/>
					<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
				</u:sc>
			</td>
			<td width="50%" class="pl1">
				<u:sc>
					<c:set var="list" value="${form.response.data.realmGroupList}"/>
					<c:set var="hiddenName" value="realmGroup"/>
					<c:set var="value" value="${radiusInfo.realmGroup}"/>
					<c:set var="style" value="width: 100%;"/>
					<c:set var="prefixText" value="Гр. реалмов"/>
					<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
				</u:sc>
			</td>
		</tr>	
	</table>
	<div>
		<h2>Наборы атрибутов</h2>
					
		<c:set var="uiidTableAttrSet" value="${u:uiid()}"/>
					
		<table class="data" style="width: 100%;" id="${uiidTableAttrSet}">
			<tr>
				<c:if test="${not empty enable}">
					<td width="20"></td>
				</c:if>	
				<td width="100%">Набор</td>
				<td>Реалм</td>
			</tr>
			<c:forEach var="item" items="${radiusInfo.attrSetList}">
				<tr>
					<c:if test="${not empty enable}">
						<td>
							<input type="hidden" name="attrSet" value="${item.id}:${item.realm}"/>
							<button type="button" class="btn-white btn-small button-remove">X</button>
						</td>
					</c:if>	
					<td>${item.title}</td>
					<td>${item.realm}</td>
				</tr>
			</c:forEach>
		</table>
		
		<c:if test="${not empty enable}">
			<div class="mt1 in-table-cell in-pr1">
				<c:set var="uiidAttrSet" value="${u:uiid()}"/>
				<c:set var="uiidAttrSetRealm" value="${u:uiid()}"/>
			
				<div style="width: 50%;">
					<u:sc>
						<c:set var="list" value="${form.response.data.attrSetList}"/>
						<c:set var="hiddenName" value="attrSetId"/>
						<c:set var="style" value="width: 100%;"/>
						<c:set var="id" value="${uiidAttrSet}"/>
						<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
					</u:sc>
				</div>
				<div style="width: 50%;">
					<u:sc>
						<c:set var="list" value="${form.response.data.realmList}"/>
						<c:set var="hiddenName" value="realm"/>
						<c:set var="style" value="width: 100%;"/>
						<c:set var="id" value="${uiidAttrSetRealm}"/>
						<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
					</u:sc>
				</div>
				<div>
					<c:set var="uiidButtonAddSet" value="${u:uiid()}"/>
					<button type="button" class="btn-grey" id="${uiidButtonAddSet}">+</button>						
				</div>
			</div>
		</c:if>	
		
		<h2>Атрибуты</h2>
		
		<c:set var="uiidTableAttr" value="${u:uiid()}"/>
		
		<table class="data" style="width: 100%;" id="${uiidTableAttr}">
			<tr>
				<c:if test="${not empty enable}">
					<td width="30"></td>
				</c:if>	
				<td>Атрибут</td>
				<td width="100%">Значение</td>
				<td>Реалм</td>
			</tr>
			<c:forEach var="item" items="${radiusInfo.attrList}">
				<tr>
					<c:if test="${not empty enable}">
						<td>
							<input type="hidden" name="attribute" value="${item.name}:${item.value}:${item.realm}"/>
							<button type="button" class="btn-white btn-small button-remove">X</button>
						</td>
					</c:if>	
					<td nowrap="nowrap">${item.name}</td>
					<td>${item.value}</td>
					<td>${item.realm}</td>												
				</tr>
			</c:forEach>	
		</table>
		
		<c:if test="${not empty enable}">
			<div class="mt1 in-table-cell in-pr1">
				<c:set var="uiidAttr" value="${u:uiid()}"/>
				<c:set var="uiidAttrValue" value="${u:uiid()}"/>
				<c:set var="uiidAttrRealm" value="${u:uiid()}"/>
			
				<div style="width: 30%;">
					<u:sc>
						<c:set var="valuesHtml">
							<c:forEach var="item" items="${form.response.data.attrTypeList}">
								<li value="${item}">${item}</li>
							</c:forEach>			
						</c:set>
						<c:set var="hiddenName" value="attr"/>
						<c:set var="style" value="width: 100%;"/>
						<c:set var="id" value="${uiidAttr}"/>
						<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
					</u:sc>
				</div>
				<div style="width: 30%;">
					<input type="text" style="width: 100%" placeholder="Значение" id="${uiidAttrValue}"/>
				</div>
				<div style="width: 30%;">
					<u:sc>
						<c:set var="list" value="${form.response.data.realmList}"/>
						<c:set var="hiddenName" value="realm"/>
						<c:set var="style" value="width: 100%;"/>
						<c:set var="id" value="${uiidAttrRealm}"/>
						<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
					</u:sc>
				</div>
				<div>
					<c:set var="uiidButtonAddAttr" value="${u:uiid()}"/>
					<button type="button" class="btn-grey" id="${uiidButtonAddAttr}">+</button>						
				</div>
			</div>
		</c:if>	
		
		<script>
			$(function()
			{
				var removeFunction = function( event )
				{
					$(event.target).closest('tr').remove();	
				};
				
				$( "#${uiidTableAttrSet}" ).on( "click", "button.button-remove", removeFunction );						
				$( "#${uiidTableAttr}" ).on( "click", "button.button-remove", removeFunction );
				
				var onClick = function()
				{
					var setId = $('#${uiidAttrSet} input[name=attrSetId]').val();
					var setTitle = $('#${uiidAttrSet} div.text-value').text();
					var realm = $('#${uiidAttrSetRealm} input[name=realm]').val();
					
					if( setId > 0 )
					{
						$( "#${uiidTableAttrSet} tbody").append( 
							"<tr><td>" +
							"<input name='attrSet' type='hidden' value='" + setId + ":" + realm + "'/>" +
							"<button type='button' class='btn-white btn-small button-remove'>X</button>" +
							"</td><td>" + setTitle + "</td><td>" + realm + "</td></tr>" );
					}
				};
				
				$('#${uiidButtonAddSet}').click( onClick );
				
				onClick = function()
				{
					var attribute = $('#${uiidAttr} input[name=attr]').val();
					var value = $('#${uiidAttrValue}').val();
					var realm = $('#${uiidAttrRealm} input[name=realm]').val();
										
					if( attribute && value && realm )
					{
						$( "#${uiidTableAttr} tbody").append( 
							"<tr><td>" +
							"<input name='attribute' type='hidden' value='" + attribute + ":" + value + ":" + realm + "'/>" +
							"<button type='button' class='btn-white btn-small button-remove'>X</button>" +
							"</td><td nowrap='nowrap'>" + attribute + "</td><td>" + value + "</td><td>" + realm + "</td></tr>" );
					}
				};
				
				$('#${uiidButtonAddAttr}').click( onClick );
			})
		</script>
	</div>
</html:form>