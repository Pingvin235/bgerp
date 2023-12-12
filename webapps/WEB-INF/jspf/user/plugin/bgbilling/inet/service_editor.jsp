<%--suppress XmlPathReference --%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:set var="service" value="${form.response.data.service}"/>
<c:set var="typeList" value="${form.response.data.typeList}"/>
<c:set var="objectList" value="${form.response.data.objectList}"/>

<c:set var="contractId" value="${form.param.contractId}"/>
<c:set var="billingId" value="${form.param.billingId}"/>
<c:set var="moduleId" value="${form.param.moduleId}"/>

<h1>Редактор сервиса:  ${service.title}</h1>

<html:form action="/user/plugin/bgbilling/proto/inet.do" styleId="${uiid}">
	<input type="hidden" name="action" value="serviceUpdate" />
	<html:hidden property="contractId"/>
	<html:hidden property="billingId"/>
	<html:hidden property="moduleId"/>
	<html:hidden property="id"/>

	<input type="hidden" name="deviceTypeIds"/>
	<input type="hidden" name="deviceGroupIds"/>

	<table style="width:100%">
		<tr class="in-pl1">
			<td width="100%">
				<c:set var="typeSelectUiid" value="${u:uiid()}"/>

				<c:set var="onSelectCode">
					$$.bgbilling.inet.serviceTypeChanged('${typeSelectUiid}');
				</c:set>

				<h2>Тип:</h2>

				<ui:combo-single id="${typeSelectUiid}" hiddenName="typeId" value="${service.typeId}" onSelect="${onSelectCode}" style="width: 100%;">
					<jsp:attribute name="valuesHtml">
						<c:forEach var="item" items="${typeList}">
							<li value="${item.id}"
								sessionCountLimit=${!item.sessionCountLimitLock ? '1' : '0'}
								login=${item.needLogin ? '1' : '0'}
								device=${item.needDevice ? '1' : '0'}
								interface=${item.needInterface ? '1' : '0'}
								vlan=${item.needVlan ? '1' : '0'}
								mac_address=${item.needMacAddress ? '1' : '0'}
								address_panel=${item.addressDescriptor.addressPanel ? '1' : '0'}
								address_dash=${item.addressDescriptor.addressDash ? '1' : '0'}
								net_slash=${item.addressDescriptor.netSlash ? '1' : '0'}
								addr_to=${item.addressDescriptor.addrTo ? '1' : '0'}
								mask=${item.addressDescriptor.mask ? '1' : '0'}
								object_panel=${item.needContractObject ? '1' : '0'}
								deviceTypeIds="${u.toString(item.deviceTypeIds)}"
								deviceGroupIds="${u.toString(item.deviceGroupIds)}"
							>${item.title }</li>
						</c:forEach>
					</jsp:attribute>
				</ui:combo-single>

				<script>
					$(function () {
						${onSelectCode}
					})
				</script>
			</td>
			<td nowrap="nowrap">
				<h2>Период</h2>
				c
				<c:set var="editable" value="true"/>
				<input type="text" name="dateFrom" value="${tu.format( service.dateFrom, 'ymd' ) }" id="${uiid}-dateFrom"/>
				<c:set var="selector" value="#${uiid}-dateFrom"/>
				<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
				по
				<c:set var="editable" value="true"/>
				<input type="text" name="dateTo" value="${tu.format( service.dateTo, 'ymd' ) }" id="${uiid}-dateTo" />
				<c:set var="selector" value="#${uiid}-dateTo"/>
				<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
			</td>
			<td nowrap="nowrap">
				<h2>Статус</h2>
				<ui:combo-single hiddenName="status" value="${service.status}" widthTextValue="150px">
					<jsp:attribute name="valuesHtml">
						<li value="0">Открыт</li>
						<li value="1">Закрыт</li>
						<li value="2">Заблокирован</li>
					</jsp:attribute>
				</ui:combo-single>
			</td>
			<td nowrap="nowrap" id="sessionCountLimit">
				<h2>Кол.-во сессий</h2>
				<ui:combo-single hiddenName="sessions" value="${service.sessionCountLimit}" widthTextValue="100px">
					<jsp:attribute name="valuesHtml">
						<li value="0">Неограниченно</li>
						<c:forEach var="item" begin="1" end="10">
							<li value="${item}">${item}</li>
						</c:forEach>
					</jsp:attribute>
				</ui:combo-single>
			</td>
		</tr>
	</table>
	<div id="login" class="in-inline-block in-pr1">
			<div style="width: 50%;">
				<h2>Логин</h2>

				<div class="in-table-cell in-pr1">
					<div style="width: 100%;">
						<input type="text" name="login" style="width: 100%;" value="${service.login}"/>
					</div>
					<div style="white-space: nowrap;">
						<input type="checkbox" name="generateLogin" value="1"/>&#160;&#160;авто
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
						<input type="checkbox" name="generatePassword" value="1"/>&#160;&#160;авто
					</div>
				</div>
			</div>
		</div>

	<div id="device" class="in-table-cell in-pr1">
		<div style="width: 100%;">
			<h2>Устройство</h2>

			<div class="in-table-cell in-pr1">
				<div style="width: 100%;">
					<input type="hidden" id="${uiid}-deviceId" class="deviceId" name="deviceId" value="${service.deviceId}"/>
					<input type="text" disabled="disabled" class="deviceTitle" name="deviceTitle" style="width: 100%;" value="${service.deviceTitle}"/>
				</div>
				<div style="white-space: nowrap;">
					<button type="button" class="btn-white" onclick="$$.bgbilling.inet.devices(this.form)">&lt;&lt;&lt;</button>
				</div>
			</div>
		</div><%--
	--%><div id="interface" style="min-width: 350px;">
			<h2>Интерфейс</h2>

			<div class="in-table-cell in-pr1">
				<div style="width: 100%;">
					<input type="hidden" id="${uiid}-ifaceId" name="ifaceId" value="${service.ifaceId}"/>
					<input type="text" id="${uiid}-ifaceTitle" name="ifaceTitle" disabled="disabled" style="width: 100%;" value="${service.interfaceTitle}"/>
				</div>
				<div style="white-space: nowrap;">
					<button type="button" class="btn-white" onclick="$$.bgbilling.inet.ifaces(this.form)">&lt;&lt;&lt;</button>
				</div>
			</div>

		</div><%--
	--%><div id="vlan" style="min-width: 250px;">
			<h2>VLAN</h2>
			<div class="in-table-cell in-pr1">
				<div style="width: 100%;">
					<input type="text" style="width: 100%;"  id="${uiid}-vlan" name="vlan" value="${service.vlan}"/>
				</div>
				<div style="white-space: nowrap;">
					<button type="button" class="btn-white" onclick="$$.bgbilling.inet.vlans(this.form)">&lt;&lt;&lt;</button>
				</div>
			</div>
		</div>
	</div>
	<div id ="${uiid}-deviceEdit" class="in-table-cell in-pr1 deviceEdit"></div>

	<div id="mac_address" class="in-pr1">
		<h2>MAC адрес</h2>

		<div style="width: 100%;">
			<input type="text" style="width: 100%;" name="macAddress" value="${service.macAddressStr}"/>
		</div>
	</div>


	<div id="address_panel">
		<h2>Адрес</h2>

		<div class="in-table-cell in-pr1">
			<div id="addr_from">
				<input type="text" name="addrFrom" style="width: 100%;" value="${service.addrFromStr}"/>
			</div>
			<div id="address_dash">
				-
			</div>
			<div id="net_slash">
				/
			</div>
			<div id="addr_to">
				<input type="text" name="addrTo" style="width: 100%;" value="${service.addrToStr}"/>
			</div>
			<div id="mask">
				<input type="text" name="mask" style="width: 100%;" value="${service.mask}"/>
			</div>
		</div>
	</div>

	<div id="object_panel" class="in-table-cell in-pr1">
		<div style="width: 100%;">
			<h2>Обьект</h2>
			<ui:combo-single
					hiddenName="contractObjectId" value="${service.contractObjectId}" prefixText="Объект:" widthTextValue="120px">
				<jsp:attribute name="valuesHtml">
					<c:forEach var="object" items="${objectList}" >
						<li value="${object.id}">${object.title}</li>
					</c:forEach>
				</jsp:attribute>
			</ui:combo-single>
		</div>
	</div>

	<div>
		<h2>Комментарий</h2>
		<textarea style="width: 100%; height: 200px; resize: none;" name="comment">${service.comment}</textarea>
	</div>

	<div class="mt1 mb1">
		<c:set var="returnCommand" value="$$.ajax.load('${form.returnUrl}', $('#${uiid}').parent())"/>
		<button class="btn-grey" type="button" onclick="$$.ajax.post(this).done(() => ${returnCommand})">OK</button>
		<button class="btn-white ml1" type="button" onclick="${returnCommand}">Отмена</button>
	</div>
</html:form>