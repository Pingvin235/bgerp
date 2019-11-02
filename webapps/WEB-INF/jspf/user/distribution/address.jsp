<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<script>
	//openUrlTo( "/user/distribution.do?action=userList" , $("#users"), "" );
	//openUrlTo( "/user/distribution.do?action=undistributedHouses", $("#undistributedHouses"), "" );
	//openUrlTo( "/user/distribution.do?action=userHouses", $("#userHouses"), "" );
	
	listDistrTypes();
	//listDistr();
	listUndistHouses();
	listUsers();
	
	
	
	
	function listDistrTypes()
	{
		openUrlTo( "/user/distribution.do?action=distrTypeList", $("#distrType"), "" );
	}
	
	/*function listDistr()
	{
		openUrlTo( "/user/distribution.do?action=distrList", $("#distr"), "" );
	}*/
	
	function listUsers()
	{
		//var userMask = $("[name=userMask]").val();
		var distrId = $("#distr input[name=distrId]").val();
		openUrlTo( "/user/distribution.do?action=userList&distrId=" + distrId , $("#users"), "" );  //&userMask=" + userMask + "
	}
	
	function addHouseToUser()
	{
		var userId = $("#users input[name=userId]").val();
		var distrId = $("#distr input[name=distrId]").val();
		var hid= ""; 
		
		$("#undistributedHouses option:selected").each( function() {
			hid += "&hid=" + $(this).val();
		});
		
		if( userId == undefined || hid == undefined)
		{
			alert("Не выбран пользователь или дом!"); 
			return;
		}
		
		sendAJAXCommand( "/user/distribution.do?action=attachHouseToUser&userId=" + userId + "&distrId=" + distrId + "" + hid );
		listUserHouses();
		listUndistHouses();
	}
	
	function removeHouseFromUser()
	{
		var userId = $("#users input[name=userId]").val();
		var distrId = $("#distr input[name=distrId]").val();
		var hid = "";
		
		$("#userHouses option:selected").each( function() {
			hid += "&hid=" + $(this).val();
		});
		
		if( userId == undefined || hid == undefined)
		{
			alert( "Не выбран пользователь или дом!" );
			return;
		}
		
		sendAJAXCommand( "/user/distribution.do?action=removeHouseFromUser&userId=" + userId + "&distrId=" + distrId + "" + hid );
		listUserHouses();
		listUndistHouses();
	}
	
	function listUserHouses()
	{
		var userId = $("#users input[name=userId]").val();
		var distrId = $("#distr input[name=distrId]").val();
		var quarter = $("[name=userQuarter]").val();
		var street = $("[name=userStreet]").val();
		var house = $("[name=userHouse]").val();
		var userTitle = $("#users input[name=userId]");
		//$("#userTitle").html("Дома пользователя: <b>" + userTitle + "</b>" );
		openUrlTo( "/user/distribution.do?action=userHouses&userId=" + userId + "&distrId=" + distrId + "&quarterMask=" + quarter + "&streetMask=" + street + "&houseMask=" + house, $("#userHouses"), "" );
	}
	
	function listUndistHouses()
	{
		var userId = $("#users input[name=userId]").val();
		var distrId = $("#distr input[name=distrId]").val();
		var quarter = $("[name=undistQuarter]").val();
		var street = $("[name=undistStreet]").val();
		var house = $("[name=undistHouse]").val();
		openUrlTo( "/user/distribution.do?action=undistributedHouses&userId=" + userId + "&distrId=" + distrId + "&quarterMask=" + quarter + "&streetMask=" + street + "&houseMask=" + house, $("#undistributedHouses"), "" );
	}
	
	function resetUndistFilter()
	{
		$("[name=undistQuarter]").val("");
		$("[name=undistStreet]").val("");
		$("[name=undistHouse]").val("");
		listUndistHouses();
	}
	
	function resetUserHouseFilter()
	{
		$("[name=userQuarter]").val("");
		$("[name=userStreet]").val("");
		$("[name=userHouse]").val("");
		listUserHouses();
	}
	
	function resetUserFilter()
	{
		$("[name=userMask]").val("");
		listUsers();
	}
	
	function listAll()
	{
		listUsers();
		//listUserHouses();
		listUndistHouses();
	}
</script>

<div>
	<html:form method="GET" action="/user/distribution">
		<table style="width:100%;">
			<tr>
				<td>
					<div>
						<u:sc>
							<c:set var="id" value="distr"/>
							
							<c:set var="valuesHtml">
								<li value="-1">не выбрано распределение</li>
								<c:forEach var="item" items="${ctxSetup.getSetup().subIndexed( 'addressDistribution.' ).entrySet()}">
									<li value="${item.key}">${item.value.title}</li>
								</c:forEach>
							</c:set>
							<c:set var="hiddenName" value="distrId"/>
							<c:set var="prefixText" value="План распределения:"/>
							<c:set var="onSelect">
								listAll();
							</c:set>
							<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
						</u:sc>
					</div>
				</td>
				<td></td>
				<td style="align:center">
					<div id ="users">
						<!-- список пользователей -->
					</div>
				</td>
			</tr>
			<tr style="height:50px;">
				<td>
					<div style="float:left;">
						Квартал: <input name="userQuarter" type="text" size="10"  onkeypress="if( enterPressed( event ) ) { listUserHouses(); }"/>
						Улица: <input name="userStreet" type="text" size="25" onkeypress="if( enterPressed( event ) ) { listUserHouses(); }"/>
						Дом: <input name="userHouse" type="text" size="5" onkeypress="if( enterPressed( event ) ) { listUserHouses(); }"/>
					</div>
					<div style="float:right;text-align:right;">
						<input type="button" class="btn-white" value="Фильтр" onclick="listUserHouses()"/>
						<input type="button" class="btn-white"  value="Сброс" onclick="resetUserHouseFilter()" />
					</div>
				</td>
				<td></td>
				<td>
					<div style="float:left;">
						Квартал: <input name="undistQuarter" type="text" size="10" onkeypress="if( enterPressed( event ) ) { listUndistHouses(); }"/>
						Улица: <input name="undistStreet" type="text" size="25" onkeypress="if( enterPressed( event ) ) { listUndistHouses(); }"/>
						Дом: <input name="undistHouse" type="text" size="5" onkeypress="if( enterPressed( event ) ) { listUndistHouses(); }"/>
					</div>
					<div style="float:right;text-align:right;">
						<input type="button" class="btn-white"  value="Фильтр" onclick="listUndistHouses()" />
						<input type="button" class="btn-white"  value="Сброс" onclick="resetUndistFilter()" />
					</div>
				</td>
			</tr>
			<tr>
				<td>
					<div id="userHouses">
						<!-- Дома пользователя -->
					</div>
				</td>
				<td style="text-align:center;">
					<html:form action="/user/distribution">
					<input type="button" class="btn-white"  value="&lt;&lt;" onclick="addHouseToUser()"/><br/>
					</html:form>
					
					<html:form action="/user/distribution" method="GET">
					<input type="button" class="btn-white"  value="&gt;&gt;" onclick="removeHouseFromUser()"/>
					</html:form>
				</td>
				<td>
					<div id="undistributedHouses">
						<!--Нераспределённые дома -->
					</div>
					
				</td>
			</tr>
			<!-- <tr>
				<td colspan="3">
					<input type="hidden" name="distrId"/>
					Улица: <input type="text"/>
					Дом: <input type="text"/>
				</td>
			</tr> -->
		</table>
	</html:form>
</div>
