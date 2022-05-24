<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="workTypeMap" value="${form.response.data.workTypeMap}"/>
<c:set var="shiftList" value="${form.response.data.shiftList}"/>
<c:set var="minimalVersion" value="${form.response.data.minimalVersion}"/>

<c:set var="uiid" value="${u:uiid()}"/>

<div id="${uiid}" class="wrap box" style="display: none;">
	<div class="shift ml05" shiftId="0">
		<div class="text">Пустая смена</div>
		<div class="color">&nbsp;</div>
	</div>
	
	<c:forEach var="shift" items="${shiftList}">
		<div class="shift" shiftId="${shift.id}">
			<div class="text">${shift.title}</div>
			<div class="color" style="background-color: ${shift.color};"><%-- 
			--%><c:if test="${not empty shift.symbol}"><%--
				--%><span class="symbol">${shift.symbol}</span><%--
			--%></c:if><%--
			--%>&nbsp;<%--
		--%></div>
		</div>
	</c:forEach>		
</div>

<script>
	$(function()
	{		
		var $shiftArea = $('#${uiid}').closest('#shiftArea');
		
		var width = $shiftArea.width();
		$('#${uiid}').css( 'width', width ).css( 'max-width', width ).show();
		
		$('#${uiid} .shift').click( function()
		{
			$('#${uiid} .shift').removeClass( 'selected' );
			$(this).addClass( 'selected' );
		});
		
		$(window).resize( function()
		{
			$('#${uiid}').hide();
			
			width = $shiftArea.width();
			$('#${uiid}').css( 'width', width ).css( 'max-width', width ).show();
		});
	})
</script>
						
	
<%--	
	<tr>
		<td>Пустая смена</td>
		<c:forEach var="shift" items="${shiftList}" >
			<td>${shift.title}</td>	
		</c:forEach>
	</tr>						
	<tr>	
		<td class="shiftOne shiftPanelOne" shiftId="0" shiftTitle="Пустая смена" style="text-align: center; height: 50px;">
			<div style="height: 50px; position: relative; z-index: 9999; border-style: solid; border-width: 3px; border-color: transparent;"></div>							
		</td>
				
		<c:forEach var="shift" items="${shiftList}" >
			<td class="shiftOne shiftPanelOne" shiftId="${shift.id}" shiftTitle="${shift.title}" style="height: 50px;" >
				<div style="height: 50px; width: 100px; display: inline-block; position: relative; z-index: 9999; border-style: solid; border-width: 3px; border-color: transparent;">
										
					<c:set var="color" value="${shift.color}"/>					
					<c:set var="symbolExists" value="0"/>				
					
					<c:forEach var="workType" items="${shift.workTypeTimeList}">
						<c:if test="${shift.useOwnColor == false}"><c:set var="color" value="${workTypeMap[workType.workTypeId].workTypeConfig.color}"/></c:if>									
						<div nonWorkingHours="${workTypeMap[workType.workTypeId].isNonWorkHours ? 1 : 0}" workTypeId="${workType.workTypeId}" timeFrom="${workType.minutesFrom}" timeTo="${workType.minutesTo}" style="width: 100%; display: table; text-align: center; background: ${color}; height: ${100 / shift.workTypeCount}%">
							<c:if test="${fn:length( shift.symbol ) > 0 && symbolExists == 0}">
								<p class="shiftSymbol ${minimalVersion == 0 ? "shiftSymbolFont" : "shiftSymbolFontMinimal" }">${shift.symbol}</p>
								<c:set var="symbolExists" value="1"/>
							</c:if>							
							<div style="display: table-cell; vertical-align: middle;"></div>
						</div>
					</c:forEach>
				</div>								
			</td>
		</c:forEach>
	</tr>
--%>