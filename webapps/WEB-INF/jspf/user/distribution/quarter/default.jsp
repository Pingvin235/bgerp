<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
<c:set var="distributionMap" value="${form.response.data.distributionMap}" scope="page" />

<div>

	<table style="width:100%;">
		<tr>
			<%--<td colspan="4">				
				
				<select id="quarterDistr${uiid}" onchange="listGroups();">					
					<c:forEach var="item" items="${distributionMap}" >
						<option value="${item.key}">${item.value}</option>
					</c:forEach>
				</select>
			</td> --%>
		</tr>
		
		<tr>
			<td>
				<u:sc>
					<c:set var="id" value="quarterDistr${uiid}"/>
					<c:set var="valuesHtml">
						
						<c:forEach var="item" items="${distributionMap}">
							<li value="${item.key}">${item.value}</li>
						</c:forEach>
					</c:set>
					<c:set var="hiddenName" value="planId"/>
					<c:set var="prefixText" value="План распределения:"/>
					<c:set var="onSelect">
						listGroups();
					</c:set>
					<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
				</u:sc>
			</td>
			<td></td>
			<td>
				<div id="groups${uiid}">
				</div>
			</td>
		</tr>
		
		
		<tr>
		
		<td>
			<div id="groupTitle${uiid}"><b>Кварталы группы:</b></div>
			<div>
				<select id="groupQuarters${uiid}" class="select" multiple="multiple" size="27" style="width:100%;height:100%;">					
				</select>
			</div>
		</td>
		<td style="text-align:center;">			
			<input type="button" class="btn-white" value="&lt;&lt;" onclick="addQuarterToGroup()"/><br/>			
			<input type="button" class="btn-white" value="&gt;&gt;" onclick="removeQuarterFromGroup()"/>			
		</td>
		<td>
			<div><b>Нераспределенные кварталы:</b></div>
			<div>
				<select id="undistributedQuarters${uiid}" class="select" multiple="multiple" size="27" style="width:100%;height:100%;">					
				</select>
			</div>
		</td>
	</tr>
	</table>	
</div>

<script>

	function listGroups()
	{		
		var distrId = $("#quarterDistr${uiid} input[name=planId]").val();
		openUrlTo( "/user/distribution.do?action=groupList&distrId=" + distrId, $("#groups${uiid}"), "" );
	}
	
	function listUndistGroups()
	{		
		var distrId = $("#quarterDistr${uiid} input[name=planId]").val();
		
		openUrlTo( "/user/distribution.do?action=undistributedGroups&distrId=" + distrId, $("#undistributedQuarters${uiid}"), "" );
	}
	
	function listGroupQuarters()
	{
		var groupId = $("#groups${uiid} input[name=groupId]").val();
		var distrId = $("#quarterDistr${uiid} input[name=planId]").val();
		
		openUrlTo( "/user/distribution.do?action=groupQuarters&groupId=" + groupId + "&distrId=" + distrId, $("#groupQuarters${uiid}"), "" );
	}
	
	function addQuarterToGroup()
	{
		var groupId = $("#groups${uiid} input[name=groupId]").val();
		var distrId = $("#quarterDistr${uiid} input[name=planId]").val();
		var quarter = ""; 
		
		$("#undistributedQuarters${uiid} option:selected").each( function() {
			quarter += "&quarter=" + $(this).val();
		});
		
		if( groupId == undefined || distrId == undefined)
		{
			alert("Не выбрана группа или квартал!"); 
			return;
		}
		
		if( sendAJAXCommand( "/user/distribution.do?action=attachQuarterToGroup&groupId=" + groupId + "&distrId=" + distrId + "" + quarter ) )
		{
			listGroupQuarters();
			listUndistGroups();
		}		
	}
	
	function removeQuarterFromGroup()
	{
		var groupId = $("#groups${uiid} input[name=groupId]").val();
		var distrId = $("#quarterDistr${uiid} input[name=planId]").val();
		var quarter = ""; 
		
		$("#groupQuarters${uiid} option:selected").each( function() {
			quarter += "&quarter=" + $(this).val();
		});
		
		if( groupId == undefined || distrId == undefined)
		{
			alert("Не выбрана группа или квартал!"); 
			return;
		}
		
		if( sendAJAXCommand( "/user/distribution.do?action=removeQuarterFromGroup&groupId=" + groupId + "&distrId=" + distrId + "" + quarter ) )
		{
			listGroupQuarters();
			listUndistGroups();
		}		
	}
	
	listGroups();	
	listUndistGroups();
	
</script>


