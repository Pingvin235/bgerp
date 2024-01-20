<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="url" value="${form.httpRequestURI}">
	<c:param name="action" value="serviceGet"/>
	<c:param name="contractId" value="${form.param.contractId}"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="moduleId" value="${form.param.moduleId}"/>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
</c:url>

<ui:button type="add" onclick="$$.ajax.load('${url}', $('#${uiid}').parent())"/>

<table id="${uiid}" class="data hl mt1">
	<tr>
		<td>Сервис</td>
		<td>Тип</td>
		<td>Период</td>
		<td>Статус</td>
		<td>Состояние</td>
		<td>ID</td>
		<td width="30">&nbsp;</td>
	</tr>
	<c:forEach var="item" items="${frd.list}">
		<tr data-tt-id="${item.id}" data-tt-parent-id="${item.parentId}" deviceId="${item.deviceId}">
			<td>${item.title}</td>
			<td>${item.typeTitle}</td>
			<td>${tu.formatPeriod(item.dateFrom, item.dateTo, 'ymd')}</td>
			<td>${item.statusTitle}</td>
			<td>${item.devStateTitle}</td>
			<td>${item.id}</td>

			<td nowrap="nowrap">
				<button type="button" class="menu btn-white btn-small icon" title="Управление состоянием"><i class="ti-more"></i></button>

				<c:url var="editUrl" value="${url}">
					<c:param name="id" value="${item.id}"/>
				</c:url>
				<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.load('${editUrl}', $('#${uiid}').parent())"/>

				<c:url var="deleteUrl" value="${form.httpRequestURI}">
					<c:param name="action" value="serviceDelete"/>
					<c:param name="contractId" value="${form.param.contractId}"/>
					<c:param name="billingId" value="${form.param.billingId}"/>
					<c:param name="moduleId" value="${form.param.moduleId}"/>
					<c:param name="id" value="${item.id}"/>
				</c:url>
				<ui:button type="del" styleClass="btn-small" onclick="$$.ajax.post('${deleteUrl}', {control: this}).done(() => $$.ajax.load('${form.requestUrl}', $('#${uiid}').parent()))"/>
			</td>
		</tr>
	</c:forEach>
</table>

<c:set var="uiidMenu" value="${u:uiid()}"/>
<ul id="${uiidMenu}"></ul>

<c:set var="uiidDialog" value="${u:uiid()}"/>
<div id="${uiidDialog}" style="display:none;" class="in-mt05"></div>

<script>
	$(function() {
		$("#${uiid}").treetable({
			expandable: true
		});

		var serviceId = 0, deviceId = 0;
		var $menu = $('#${uiidMenu}').menu().hide();

		<c:url var="url" value="plugin/bgbilling/proto/inet.do">
			<c:param name="contractId" value="${form.param.contractId}"/>
			<c:param name="billingId" value="${form.param.billingId}"/>
			<c:param name="moduleId" value="${form.param.moduleId}"/>
		</c:url>

		$('#${uiidMenu}').on("click", "li", function() {
			var $li = $(this);
			if ($li.attr("command")) {
				var command = "${url}&id=" + serviceId + '&';
				// вкл - выкл статуса
				if ($li.attr('confirm')) {
					if (confirm($li.attr('confirm')) && sendAJAXCommand(command + $li.attr("command"))) {
						$$.ajax.load('${form.requestUrl}', $('#${uiid}').parent());
					}
				}
				// вызов метода
				else {
					var result = sendAJAXCommand(command + $li.attr("command"));
					if (result) {
						var resp = result.data.response;
						if (resp.indexOf('telnet:') >= 0) {
							resp = resp.replace(':', ':///');
							resp = resp.replace(' ', ':');
							window.open(resp);
						} else {
							if (resp.indexOf('html') < 0) {
								<%-- есть табуляторы - текст преобразуется в табличку --%>
								if (resp.indexOf('\t') > 0) {
									<%-- странные workaroundы для DHCP snooping Maglan --%>
									resp = resp.replace('\t\t', '\t');
									resp = resp.replace('Interface\t', 'Interface\n');

									var table = "<table style='width: 100%;'>";
									resp.split("\n").forEach(function (line) {
										table += "<tr>";
										line.split("\t").forEach(function (cell) {
											table += "<td>" + cell + "</td>";
										});
										table += "</tr>";
									});
									table += "</table>";

									result.data.response = table;
								} else {
								   result.data.response = "<pre style='font-family: \"courier new\", courier, monospace;'>" + result.data.response + "</pre>";
								}
							}
							$('#${uiidDialog}').html(result.data.response);
							$('#${uiidDialog}').dialog({
								modal: true,
								width: 800,
								height: 600,
								draggable: true,
								<%-- resizable: true, --%>
								title: "Результат выполнения команды",
								position: { my: "center top", at: "center top+100px", of: window }
							});
						}
					}
				}
			}
		});

		$("#${uiid} tbody").on( "click", "button.menu", function() {
			var $tr = $(this).closest("tr");

			serviceId = $tr.attr("data-tt-id");
			deviceId = $tr.attr("deviceId");

			openUrlTo("${url}&action=serviceMenu&deviceId=" + deviceId, $("#${uiidMenu}"));

			$menu.menu("refresh");

			$menu.show().position({
				my: "right top",
				at: "right bottom",
				of: this
			});

			$(document).one( "click", function() {
				$menu.hide();
			});

			return false;
		});
	})
</script>