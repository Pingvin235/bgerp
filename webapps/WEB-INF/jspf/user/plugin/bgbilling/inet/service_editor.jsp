<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:set var="service" value="${form.response.data.service}"/>
<c:set var="typeList" value="${form.response.data.typeList}"/>

<h1>Редактор сервиса</h1>

<html:form action="/user/plugin/bgbilling/proto/inet.do" styleId="${uiid}">
	<input type="hidden" name="action" value="serviceUpdate" />
	<html:hidden property="contractId"/>
	<html:hidden property="billingId"/>
	<html:hidden property="moduleId"/>
	<html:hidden property="id"/>
	
	<table style="width:100%">
		<tr class="in-pl1">
			<td width="100%">
				<c:set var="typeSelectUiid" value="${u:uiid()}"/>
			
				<c:set var="onSelectCode">
					var $typeLi = $('#${typeSelectUiid} li[selected]');
					$.each( $typeLi[0].attributes, function( index, attr ) 
					{
						$('#${uiid} #' + attr.name).toggle( attr.value == '1' );
            		});
				</c:set>
			
				<h2>Тип:</h2>
				<u:sc>
					<c:set var="valuesHtml">
						<c:forEach var="item" items="${typeList}">
							<li value="${item.id}"
								sessionCountLimit=${!item.sessionCountLimitLock ? '1' : '0'}
								login=${item.needLogin ? '1' : '0'}
								device=${item.needDevice ? '1' : '0'}
								interface=${item.needInterface ? '1' : '0'}
								vlan=${item.needVlan ? '1' : '0'}
								macAddress=${item.needMacAddress ? '1' : '0'}
								>${item.title }</li>	
						</c:forEach>
					</c:set>
					
					<c:set var="id" value="${typeSelectUiid}"/>
					<c:set var="hiddenName" value="typeId"/>
					<c:set var="value" value="${service.typeId }"/>
					<c:set var="style" value="width: 100%;"/>
					<c:set var="onSelect" value="${onSelectCode}"/>
					<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
				</u:sc>
				
				<script>
					$(function()
					{
						${onSelectCode}
					})
				</script>
			</td>
			<td nowrap="nowrap">
				<h2>Период</h2>
				c
		   		<c:set var="editable" value="true"/>
				<input type="text" name="dateFrom" value="${u:formatDate( service.dateFrom, 'ymd' ) }" id="${uiid}-dateFrom"/>	
				<c:set var="selector" value="#${uiid}-dateFrom"/>	
				<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
				по
				<c:set var="editable" value="true"/>
				<input type="text" name="dateTo" value="${u:formatDate( service.dateTo, 'ymd' ) }" id="${uiid}-dateTo" />
				<c:set var="selector" value="#${uiid}-dateTo"/>	
				<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
			</td>
			<td nowrap="nowrap">
				<h2>Статус</h2>
			
				<u:sc>
					<c:set var="valuesHtml">
						<li value="0">Открыт</li>
						<li value="1">Закрыт</li>
						<li value="2">Заблокирован</li>
					</c:set>
					<c:set var="hiddenName" value="status"/>
					<c:set var="value" value="${service.status}"/>
					<c:set var="widthTextValue" value="150px"/>
					<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
				</u:sc>
			</td>			
			<td nowrap="nowrap" id="sessionCountLimit">
				<h2>Кол.-во сессий</h2>
			
				<u:sc>
					<c:set var="valuesHtml">
						<li value="0">Неограниченно</li>
						<c:forEach var="item" begin="1" end="10">
							<li value="${item}">${item}</li>			
						</c:forEach>
					</c:set>
					<c:set var="hiddenName" value="sessions"/>
					<c:set var="value" value="${service.sessionCountLimit}"/>
					<c:set var="widthTextValue" value="100px"/>
					<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
				</u:sc>			
			</td>				
		</tr>
	</table>
		
	<div id="login" class="in-inline-block in-pr1">
		<div style="width: 50%;">
			<h2>Логин</h2>
			
			<div class="in-table-cell in-pr1">
				<div style="width: 100%;">
					<input type="text" name="login" style="width: 100%;" value="${session.login}"/>					
				</div>
				<div style="white-space: nowrap;">
					<input type="checkbox" name="loginAuto" value="1"/>&#160;&#160;авто
				</div>
			</div>	
		</div><%-- 
	--%><div style="width: 50%;">
			<h2>Пароль</h2>
		
			<div class="in-table-cell in-pr1">
				<div style="width: 100%;">
					<input type="password" name="pswd" style="width: 100%;" value="*******"/>					
				</div>
				<div style="white-space: nowrap;">
					<input type="checkbox" name="pswdAuto" value="1"/>&#160;&#160;авто
				</div>
			</div>
		</div>
	</div>
	
	<div id="device" class="in-table-cell in-pr1">
		<div style="width: 100%;">
			<h2>Устройство</h2>
			
			<div class="in-table-cell in-pr1">
				<div style="width: 100%;">
					<input type="hidden" name="deviceId" value="${service.deviceId}"/>
					<input type="text" disabled="disabled" style="width: 100%;" value="${service.deviceTitle}"/>
				</div>
				<div style="white-space: nowrap;">
					<button type="button" class="btn-white" onclick="">&lt;&lt;&lt;</button>
				</div>
			</div>
		</div><%--
	--%><div id="interface" style="min-width: 150px;">
			<h2>Интерфейс</h2>
			
			<div class="in-table-cell in-pr1">
				<div style="width: 100%;">
					<input type="hidden" name="ifaceId" value="${service.ifaceId}"/>
					<input type="text" disabled="disabled" style="width: 100%;" value="${service.interfaceTitle}"/>
				</div>
				<div style="white-space: nowrap;">
					<button type="button" class="btn-white" onclick="">&lt;&lt;&lt;</button>
				</div>
			</div>
		</div><%--
	--%><div id="vlan" style="min-width: 150px;">
			<h2>VLAN</h2>
			
			<div class="in-table-cell in-pr1">
				<div style="width: 100%;">
					<input type="text" style="width: 100%;" name="vlan" value="${service.vlan}"/>
				</div>
				<div style="white-space: nowrap;">
					<button type="button" class="btn-white" onclick="">&lt;&lt;&lt;</button>
				</div>
			</div>
		</div>
	</div>
	
	<div id="macAddress" class="in-pr1">
		<h2>MAC адрес</h2>
		
		<div style="width: 100%;">
			<input type="text" style="width: 100%;" name="macAddress" value="${service.macAddressStr}"/>
		</div>
	</div>
	
	<div>
		<h2>Комментарий</h2>
		<textarea style="width: 100%; height: 200px; resize: none;" name="comment">${service.comment}</textarea>
	</div>
	
	<div class="mt1 mb1">
		<button class="btn-grey" type="button" onclick="if (sendAJAXCommand(formUrl(this.form))){openUrlToParent('${form.returnUrl}', $('#${uiid}'))}">OK</button>
		<button class="btn-grey ml1" type="button" onclick="openUrlToParent( '${form.returnUrl}', $('#${uiid}') )">Отмена</button>
	</div>	
</html:form>