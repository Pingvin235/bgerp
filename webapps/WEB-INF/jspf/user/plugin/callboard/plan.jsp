<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="title" value="${l.l('План работ')}"/>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>

<c:set var="callboardList" value="${form.response.data.callboardList}"/>

<c:set var="callboard" value="${form.response.data.callboard}"/>
<c:set var="date" value="${form.response.data.date}"/>
<c:set var="groupDataMap" value="${form.response.data.groupDataMap}"/>

<c:set var="workTypeMap" value="${form.response.data.workTypeMap}"/>

<c:set var="uiid" value="${u:uiid()}"/>
<c:set var="groupSelectUiid" value="${u:uiid()}"/>

<div id="${uiid}">
	<div id="groupFilters" style="display: none;">
		<c:set var="values" value="${form.getSelectedValues('groupId')}"/>

		<c:forEach var="clb" items="${callboardList}"><%--
		--%><u:sc><%--
			--%><c:set var="list" value="${ctxUserGroupList}"/><%--
			--%><c:set var="prefixText" value="Группы:"/><%--
			--%><c:set var="id" value="${clb.id}-${uiid}"/><%--
			--%><c:set var="paramName" value="groupId"/><%--
			--%><c:set var="showFilter" value="1"/><%--
			--%><c:set var="available" value="${ctxUserGroupMap[clb.groupId].childSet}"/><%--
			--%><c:set var="widthTextValue" value="100px"/><%--
			--%><%@ include file="/WEB-INF/jspf/combo_check.jsp"%><%--
		--%></u:sc><%--
	--%></c:forEach>
	</div>

	<form id="${groupSelectUiid}" action="/user/plugin/callboard/work.do"  class="in-table-cell in-pr05" style="display: inline-block;">
		<input type="hidden" name="action" value="planGet" />

		<c:set var="onSelectGroupScript">
			var selectedCallboard = $('#${groupSelectUiid} #callboardSelect-${uiid} li[selected]').attr('value');

			$('#${groupSelectUiid} #groupFilter > div').appendTo( $('#${uiid} #groupFilters') );
			$('#${uiid} #groupFilters > div[id^=' + selectedCallboard + ']').appendTo( $('#${groupSelectUiid} #groupFilter') );
		</c:set>

		<div>
			<u:sc>
				<c:set var="list" value="${callboardList}"/>
				<c:set var="hiddenName" value="graphId"/>
				<c:set var="prefixText" value="План:"/>
				<c:set var="value" value="${form.param.graphId}"/>
				<c:set var="widthTextValue" value="100px"/>
				<c:set var="id" value="callboardSelect-${uiid}"/>
				<c:set var="onSelect" value="${onSelectGroupScript}"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
			</u:sc>
		</div>

		<div id="groupFilter">
			<%-- сюда переносится фильтр по группам --%>
		</div>

		<div>
			 <input id="date${uiid}" name="date" type="text" placeholder="Дата" value="${form.param.date}"/>
			 <c:set var="selector">#date${uiid}</c:set>
			 <c:set var="editable" value="1"/>
			 <%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
		 </div>

		<div>
			<button type="button" class="btn-grey ml05" onclick="openUrlToParent( formUrl( this.form ), $('#${uiid}') )" title="${l.l('Вывести')}">=&gt;</button>
		</div>
	</form>

	<script>
		$(function()
		{
			${onSelectGroupScript}

			var $state = $('#title > .status:visible > .wrap > .center');
			$state.html( "" );

			$('#${groupSelectUiid}').appendTo( $state );

			var $tableDiv = $('#${uiid} div.plan');

			if( $tableDiv.length > 0 )
			{
				var top = $tableDiv.position().top;

				/* TODO: отступы как-то взять из констант может, особенно нижний */
				var $container = $("<div style='position: absolute; top: " + top + "px; left: 2em; bottom: 20px; right: 2em;'></div>");
				$tableDiv.append( $container );

				$tableDiv.find( '>table' ).cTable({
					container: $container,
					fCols: 1,
					fRows: 1
				});

				var clickFunction = function()
				{
					var mode = $('#${uiid} #mode input[name=mode]').val();

					var column = $(this).index();

					var $row = $(this).closest("table").closest( "tr" );
					var row = $row.index();

					var dayMinuteFrom = $(this).attr( "dayMinuteFrom" );

					// параметры смены
					var $groupRow = $tableDiv.find( ".leftSBWrapper table tr:nth-child(" + (row + 1) + ")" );
					var groupId = $groupRow.attr( "groupId" );
					var userId = $groupRow.attr( "userId" ).replace( /([\[\]])/mg, "" );
					var team = $groupRow.attr( "team" );

					var urlEnd =
						"&graphId=${form.param.graphId}" +
						"&date=${form.param.date}" +
						"&groupId=" + groupId +
						"&userId=" + userId +
						"&team=" + team +
						"&dayMinuteFrom=" + dayMinuteFrom;

					// установка смен
					if( mode == 'lock' )
					{
						if( !$(this).hasClass( "lock" ) )
						{
							var result = sendAJAXCommand( "/user/plugin/callboard/work.do?action=processTimeLock" + urlEnd );
							if( result )
							{
								$(this).toggleClass( "lock" );
								$(this).html( "Б" );
							}
						}
					}
					else if( mode == 'unlock' )
					{
						if( $(this).hasClass( "lock" ) )
						{
							var result = sendAJAXCommand( "/user/plugin/callboard/work.do?action=processTimeUnlock" + urlEnd );
							if( result )
							{
								$(this).toggleClass( "lock" );
								$(this).html( "" );
							}
						}
					}
				};

				// нажатие либо вхождение с нажатой мышью
				$tableDiv.find( "table.workTypeTime td" )
					.mousedown( clickFunction )
					.mouseenter( function( event ){ event.preventDefault(); if( event.buttons == 1 ){ clickFunction.call( this ) } } );
			}
		})
	</script>

	<c:if test="${not empty date}">
		<c:set var="timeList" value="${callboard.planConfig.getDateTimes( date )}"/>

		<div id="mode" class="mb1">
			<u:sc>
				<c:set var="valuesHtml">
					<li value="view">Просмотр</li>
					<li value="lock">Блокировка</li>
					<li value="unlock">Разблокировка</li>
				</c:set>
				<c:set var="hiddenName" value="mode"/>
				<c:set var="widthTextValue" value="150px"/>
				<c:set var="prefixText" value="Режим:"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
			</u:sc>
		</div>

		<div class="plan">
			<table class="hdata plan">
				<tr class="header">
					<td class="shiftUser"></td><%--
				--%><c:forEach var="time" items="${timeList}"><%--
					--%><td class="timeHead"><fmt:formatDate value="${time}" pattern="HH:mm"/></td><%--
				--%></c:forEach>
				</tr>

				<c:forEach var="pair" items="${groupDataMap}">
					<c:choose>
						<c:when test="${pair.key eq 0}">
							<c:set var="group">/ ${ctxUserGroupMap[callboard.groupId]} /</c:set>
						</c:when>
						<c:otherwise>
							<c:set var="group" value="${ctxUserGroupMap[pair.key].title}"/>
						</c:otherwise>
					</c:choose>

					<tr class="groupHeader">
						<td class="shiftUser"><b>${group}</b></td>
						<c:forEach items="${timeList}">
							<td>&nbsp;</td>
						</c:forEach>
						<%-- <td colspan="${timeList.size()}">&nbsp;</td> --%>
					</tr>

					<c:forEach var="shift" items="${pair.value}"><%--
					--%><tr class="user" groupId="${pair.key}" userId="${shift.userIds}" team="${shift.team}"><%--
						--%><c:set var="title" value="${u:objectTitleList( ctxUserList, shift.userIds )}"/><%--

						--%><td class="shiftUser" title="${title}"><%--
							--%><c:if test="${shift.team gt 0}">[${shift.team}] </c:if>${title}<%--
						--%></td><%--

						--%><c:set var="cellRanges" value="${shift.getCellRanges( callboard.planConfig )}"/><%--

						--%><c:choose><%--
							--%><c:when test="${empty cellRanges}"><%--
								--%><c:forEach items="${timeList}"><%--
									--%><td class="cell">&nbsp;</td><%--
								--%></c:forEach><%--
							--%></c:when><%--
							--%><c:otherwise><%--
								--%><c:forEach var="cellRange" items="${cellRanges}"><%--
									--%><c:set var="typeTime" value="${cellRange.workTypeTime}"/><%--
									--%><c:set var="count" value="${cellRange.cells}"/><%--
									--%><c:set var="workType" value="${cellRange.workType}"/><%--

									--%><c:remove var="background"/><%--
									--%><c:if test="${not empty typeTime}"><%--
										--%><c:set var="background">style="background-color: ${workType.color}"</c:set><%--
									--%></c:if><%--
									--%><td colspan="${count}" class="cell" title="${workType.title}" ${background}><%--
										--%><table style="width: 100%; table-layout: fixed;" class="workTypeTime"><tr><%--
											--%><c:forEach var="slotRange" items="${cellRange.getSlotRanges()}"><%--
												--%><c:set var="dayMinuteFrom"><%--
													--%>dayMinuteFrom='${typeTime.dayMinuteFrom + slotRange.slotFrom * cellRange.workType.timeSetStep}'<%--
												--%></c:set><%--
												--%><c:set var="lock"><%--
													--%><c:if test="${slotRange.task.lock}"><%--
														--%>class='lock'<%--
													--%></c:if><%--
												--%></c:set><%--
												--%><c:remove var="titleTag"/><%--
												--%><c:set var="ref" value="${slotRange.task.reference}"/><%--
												--%><c:if test="${not empty ref}"><%--
													--%><c:set var="titleTag">title='${ref}'</c:set><%--
												--%></c:if><%--

												--%><c:remove var="onClick"/><%--
												--%><c:if test="${slotRange.task.processId gt 0}"><%--
													--%><c:set var="onClick">onClick='openProcess( ${slotRange.task.processId} );'</c:set><%--
												--%></c:if><%--

												--%><td colspan="${slotRange.slotCount}" ${titleTag} ${dayMinuteFrom} ${lock} ${onClick}>${slotRange.task.reference}<%--
													--%><c:if test="${slotRange.task.lock}">Б</c:if><%--
												--%></td><%--
											--%></c:forEach><%--
										--%></tr></table><%--
									--%></td><%--
								--%></c:forEach><%--
							--%></c:otherwise><%--
						--%></c:choose><%--
					--%></tr>
					</c:forEach>
				</c:forEach>
			</table>
		</div>
	</c:if>
</div>
