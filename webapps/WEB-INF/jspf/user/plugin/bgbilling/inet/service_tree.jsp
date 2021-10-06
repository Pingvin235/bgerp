<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="url" value="plugin/bgbilling/proto/inet.do">
	<c:param name="action" value="serviceGet"/>
	<c:param name="contractId" value="${form.param.contractId}"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="moduleId" value="${form.param.moduleId}"/>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
</c:url>
<button type="button" class="btn-green mb1" title="Добавить сервис" onclick="openUrlToParent('${url}', $('#${uiid}') )">+</button>

<table id="${uiid}" class="hdata" style="width: 100%;">
	<thead>
		<tr>
			<td>Сервис</td>
			<td>Тип</td>
			<td>Период</td>
			<td>Статус</td>
			<td>Состояние</td>
			<td>ID</td>
			<td width="30">&nbsp;</td>
		</tr>
	</thead>
	<tbody>
	   <c:forEach var="item" items="${form.response.data.list}">
		  <tr data-tt-id="${item.id}" data-tt-parent-id="${item.parentId}" deviceId="${item.deviceId}">
		    <td>${item.title}</td>
		    <td>${item.typeTitle}</td>
		    <td>${tu.formatPeriod( item.dateFrom, item.dateTo, 'ymd' )}</td>
		    <td>${item.statusTitle}</td>
		    <td>${item.devStateTitle}</td>
		    <td>${item.id}</td>
		    
		    <c:url var="eUrl" value="${url}">
				<c:param name="id" value="${item.id}"/>
			</c:url>
			<c:set var="editCommand" value="openUrlToParent('${eUrl}', $('#${uiid}') )"/>
			
			<c:url var="deleteAjaxUrl" value="plugin/bgbilling/proto/inet.do">
				<c:param name="action" value="serviceDelete"/>
				<c:param name="contractId" value="${form.param.contractId}"/>
				<c:param name="billingId" value="${form.param.billingId}"/>
				<c:param name="moduleId" value="${form.param.moduleId}"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>
			<c:set var="deleteAjaxCommandAfter" value="openUrlToParent('${form.requestUrl}',$('#${uiid}'))"/>
		    
		    <td nowrap="nowrap">
		    	<button type="button" class="menu btn-white btn-small" onclick="" title="Управление состоянием">S</button>
		    	<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
		    </td>
		  </tr>
	   </c:forEach>
	</tbody> 
</table>

<c:set var="uiidMenu" value="${u:uiid()}"/>
<ul id="${uiidMenu}">
</ul>	

<c:set var="uiidDialog" value="${u:uiid()}"/>
<div id="${uiidDialog}" style="display:none;" class="in-mt05">
</div>

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
						openUrlToParent('${form.requestUrl}', $('#${uiid}') );
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