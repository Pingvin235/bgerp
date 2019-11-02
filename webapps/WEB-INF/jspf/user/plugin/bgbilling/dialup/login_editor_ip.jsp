<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="enable"><p:check action="ru.bgcrm.plugin.bgbilling.proto.struts.action.DialUpAction:updateStaticIP">enable</p:check></c:set>	

<html:form action="/user/plugin/bgbilling/proto/dialup.do" styleClass="${enable}">
	<input type="hidden" name="action" value="updateStaticIP" />
	<html:hidden property="contractId"/>
	<html:hidden property="billingId"/>
	<html:hidden property="moduleId"/>
	<html:hidden property="id"/>
	
	<c:set var="uiidTable" value="${u:uiid()}"/>
	<c:set var="uiidEditor" value="${u:uiid()}"/>
	
	<div class="mt1 mb05">
		<c:set var="script">
			$('#${uiidEditor} input[name=ip]').val('');
			$('#${uiidEditor} input[name=dateFrom]').val('');
			$('#${uiidEditor} input[name=dateTo]').val('');
			$('#${uiidEditor}').attr( 'editingRow', -1 );
			$('#${uiidEditor}').show();
			return false;
		</c:set>
	
		<h2 style="display: inline;">IP адреса</h2>
		<c:if test="${not empty enable}">
			[<a href="#UNDEF" onclick="${script}">+</a>]
		</c:if>	
	</div>
	
	<table class="data" style="width: 100%;" id="${uiidTable}">
		<tr>
			<c:if test="${not empty enable}">
				<td width="30"></td>
			</c:if>	
			<td width="100%">Реалм</td>
			<td>IP</td>
			<td>Период</td>
		</tr>
		<c:forEach var="item" items="${form.response.data.ipAddressList}" >
			<tr>
				<c:if test="${not empty enable}">
					<td nowrap="nowrap">
						<button type="button" class="btn-white btn-small edit-button">*</button>
						<button type="button" class="btn-white btn-small remove-button">X</button>
					</td>
				</c:if>	
				<td>
					<input type="hidden" name="address" value="${item.address}:${item.realm}:${u:formatDate( item.dateFrom, 'ymd' )}-${u:formatDate( item.dateTo, 'ymd' )}"/>
					${item.realm}
				</td>
				<td>${item.address}</td>
				<td nowrap="nowrap">${u:formatPeriod( item.dateFrom, item.dateTo, 'ymd' )}</td>
			</tr>
		</c:forEach>
	</table>
	
	<c:if test="${not empty enable}">
		<div style="display: none;" id="${uiidEditor}" >
			<div class="mt1">
				<input type="text" size="6" name="ip" placelohder="IP"/>
				
				с 
				<c:set var="editable" value="true"/>
				<input type="text" name="dateFrom" id="${uiidEditor}-dateFrom"/>	
				<c:set var="selector" value="#${uiidEditor}-dateFrom"/>	
				<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
				
				по
				<c:set var="editable" value="true"/>
				<input type="text" name="dateTo" id="${uiidEditor}-dateTo"/>	
				<c:set var="selector" value="#${uiidEditor}-dateTo"/>	
				<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
				
				<div class="pl05" style="display: inline;">
					<u:sc>
						<c:set var="list" value="${form.response.data.realmList}"/>
						<c:set var="hiddenName" value="realm"/>
						<c:set var="value" value="${radiusInfo.realmGroup}"/>
						<c:set var="prefixText" value="REALM: "/>
						<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
					</u:sc>
				</div>
			</div>	
			
			<c:set var="uiidOk" value="${u:uiid()}"/>
			<div class="mt1">
				<button type="button" class="btn-grey" id="${uiidOk}">ОK</button>
				<button type="button" class="btn-grey ml1" onclick="$('#${uiidEditor}').hide();">Отмена</button>
			</div>
		</div>
	</c:if>	
	
	<script>
		$(function()
		{
			var onSave = function()
			{
				var ip = $('#${uiidEditor} input[name=ip]').val();
				var dateFrom = $('#${uiidEditor} input[name=dateFrom]').val();
				var dateTo = $('#${uiidEditor} input[name=dateTo]').val();
				var realm = $('#${uiidEditor} input[name=realm]').val();
				
				var row = 
					'<tr>\
						<td nowrap="nowrap">\
							<button type="button" class="btn-white btn-small edit-button">*</button>\
							<button type="button" class="btn-white btn-small remove-button">X</button>\
						</td>\
						<td>\
							<input type="hidden" name="address" value="' + ip + ':' + realm + ':' + dateFrom + '-' + dateTo + '"/>' + realm + '</td>\
						<td>' + ip + '</td>\
						<td nowrap="nowrap">' + dateFrom + '-' + dateTo + '</td>\
					</tr>';
				
				var editingRow = $('#${uiidEditor}').attr( 'editingRow' );
				if( editingRow < 0 )
				{
					$('#${uiidTable} tbody').append( row );
				}
				else
				{
					$('#${uiidTable} tbody tr:eq(' + editingRow + ')').replaceWith( row );	
				}
				
				console.log( editingRow );
				
				$('#${uiidEditor}').hide();
			};
			
			$('#${uiidOk}').click( onSave );
			
			$('#${uiidTable}').on( 'click', '.edit-button', function( event ) 
			{
				var $tds = $(event.target).closest('tr').find("td");
				var realm = $($tds[1]).text();
				var ip = $($tds[2]).text(); 
				var period = $($tds[3]).text();
				
				$('#${uiidEditor} input[name=ip]').val( ip );
				$('#${uiidEditor} input[name=dateFrom]').val( period.substring( 0, period.indexOf( '-' ) ) );
				$('#${uiidEditor} input[name=dateTo]').val( period.substring( period.indexOf( '-' ) + 1 ) );
				$('#${uiidEditor} input[name=realm]').val( realm );
				$('#${uiidEditor} div.text-value').text( realm );
				$('#${uiidEditor}').show();
				
				$('#${uiidEditor}').attr( 'editingRow', $(event.target).closest('tr').index() );
			});
			
			$('#${uiidTable}').on( 'click', '.remove-button', function( event ) 
			{
				$(event.target).closest('tr').remove();
			});				
		})
	</script>			
</html:form>