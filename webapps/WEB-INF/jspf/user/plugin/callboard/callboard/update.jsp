<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<shell:title text="График дежурств"/>

<c:set var="callboardList" value="${frd.callboardList}"/>

<c:set var="callboard" value="${frd.callboard}"/>
<c:set var="groupWithUsersMap" value="${frd.groupWithUsersMap}"/>
<c:set var="workTypeList" value="${frd.workTypeList}"/>
<c:set var="workShiftMap" value="${frd.workShiftMap}"/>
<c:set var="dateSet" value="${frd.dateSet}"/>
<c:set var="dateTypeMap" value="${frd.dateTypeMap}"/>
<c:set var="shiftMap" value="${frd.shiftMap}"/>
<c:set var="allowOnlyCategories" value="${frd.allowOnlyCategories}"/>
<c:set var="availableDays" value="${frd.availableDays}"/>

<c:set var="uiid" value="${u:uiid()}"/>
<c:set var="groupSelectUiid" value="${u:uiid()}"/>

<c:url var="changeOrderUrl" value="/user/plugin/callboard/work.do">
	<c:param name="method" value="callboardChangeOrder" />
	<c:param name="graphId" value="${form.param.graphId}" />
</c:url>

<c:set var="perm" value="${ctxUser.getPerm('org.bgerp.plugin.pln.callboard.action.WorkAction:callboardGet')}"/>
<c:set var="allowOnlyGroups" value="${u.toIntegerSet(perm['allowOnlyGroups'])}"/>

<div id="${uiid}">
	<div id="groupFilters" style="display: none;">
		<c:set var="values" value="${form.getParamValues('groupId')}"/>

		<c:forEach var="clb" items="${callboardList}">
			<ui:combo-check id="${clb.id}-${uiid}" name="groupId"
				list="${ctxUserGroupList}"
				available="${empty allowOnlyGroups ? ctxUserGroupMap[clb.groupId].childSet : cu.intersection(ctxUserGroupMap[clb.groupId].childSet, allowOnlyGroups)}"
				showFilter="1" prefixText="${l.l('Groups')}:" widthTextValue="7em"/>
		</c:forEach>
	</div>

	<form id="${groupSelectUiid}" action="/user/plugin/callboard/work.do"  class="in-table-cell in-pr05" style="display: inline-block;">
		<input type="hidden" name="method" value="callboardGet"/>

		<c:set var="onSelectGroupScript">
			var selectedCallboard = $('#${groupSelectUiid} #callboardSelect-${uiid} li[selected]').attr('value');

			$('#${groupSelectUiid} #groupFilter > div').appendTo( $('#${uiid} #groupFilters') );
			$('#${uiid} #groupFilters > div[id^=' + selectedCallboard + ']').appendTo( $('#${groupSelectUiid} #groupFilter') );
		</c:set>

		<div>
			<ui:combo-single list="${callboardList}" hiddenName="graphId" prefixText="График:" value="${form.param.graphId}" widthTextValue="100px"
				id="callboardSelect-${uiid}" onSelect="${onSelectGroupScript}"/>
		</div>

		<div id="groupFilter">
			<%-- сюда переносится фильтр по группам --%>
		</div>

		<div>
			<ui:date-time name="fromDate" value="${u.maskEmpty(form.param.fromDate, 'first')}" placeholder="${l.l('Date from')}"/>
		</div>

		<div>
			<ui:date-time name="toDate" value="${u.maskEmpty(form.param.toDate, 'last')}" placeholder="${l.l('Date to')}"/>
		</div>
		<div>
			<ui:button type="out" styleClass="ml05" onclick="$$.ajax.load(this.form, $('#${uiid}').parent(), {control: this})"/>
		</div>
	</form>

	<script>
		$(function()
		{
			${onSelectGroupScript}

			var $state = $$.shell.$state();
			$state.html( "" );

			$('#${groupSelectUiid}').appendTo( $state );

			var $callboard = $('#${uiid} div.callboard');

			if( $callboard.length > 0 )
			{
				var top = $callboard.position().top;

				/* TODO: отступы как-то взять из констант может, особенно нижний */
				var $container = $("<div style='position: absolute; top: " + top + "px; left: 2em; bottom: 20px; right: 2em;'></div>");
				$callboard.append( $container );

				$callboard.find( '>table' ).cTable({
					container: $container,
					fCols: 2,
					fRows: 1
				});

				// основная таблица с данными
				var table = $callboard.find( ".SBWrapper table" )[0];
				var tableRowCount = table.rows.length;
				var tableColumnCount = 0;
				if( tableRowCount > 0 )
				{
					tableColumnCount = table.rows[0].cells.length;
				}

				var updateShiftCounts = function( sumCell )
				{
					var shiftCount = 0;

					var column = $(sumCell).index();
					var $row = $(sumCell).closest( "tr" );

					for( var i = $row.index() + 1; i < tableRowCount; i++ )
					{
						var td = table.rows[i].cells[column];
						if( $(td).hasClass( "sum" ) )
						{
							break;
						}
						if( $(td).attr( "minutes" ) > 0 )
						{
							shiftCount++;
						}
					}

					$(sumCell).text( shiftCount );
				};

				$(table).find( "tr.groupHeader td" ).each( function()
				{
					updateShiftCounts( this );
				});

				var updateHourSum = function( sumCell )
				{
					var minuteSum = 0;

					var row = $(sumCell).closest( "tr" ).index();
					var cells = table.rows[row].cells;

					for( var i = 0; i < tableColumnCount; i++ )
					{
						var $td = $(cells[i]);

						var minutes = $td.attr( "minutes" );
						var prevMinutes = $td.attr( "prevMinutes" );

						minuteSum += (isNaN( minutes ) ? 0 : parseInt( minutes )) + (isNaN( prevMinutes ) ? 0 : parseInt( prevMinutes ));
					}

					$(sumCell).text( minuteSum / 60 );
				};

				$callboard.find( ".leftSBWrapper table tr.userShiftLine td:nth-child(2)" ).each( function()
				{
					updateHourSum( this );
				});

				var clickFunction = function()
				{
					var mode = $('#${uiid} #editor #controls input[name=mode]').val();

					var column = $(this).index();

					var $row = $(this).closest( "tr" );
					var row = $row.index();

					// запрос на установку смены
					var $groupRow = $callboard.find( ".leftSBWrapper table tr:nth-child(" + (row + 1) + ")" );
					var groupId = $groupRow.attr( "groupId" );
					var userId = $groupRow.attr( "userId" );
					var date = $callboard.find( ".topSBWrapper table tr td:nth-child(" + (column + 1) + ")" ).attr( "date" );

					var $shiftDiv = $();

					var shiftId = null;
					var team = null;

					// установка смен
					if( mode == 0 )
					{
						var $shift = $('#${uiid} #shiftArea .shift.selected');

						if( $shift.length == 0 )
						{
							alert( "Выберите смену для установки!" );
							return;
						}

						shiftId = $shift.attr("shiftId");
						// текущая бригада - сохранение
						team = $(this).find(".team").text();

						$shiftDiv = $shift.find('div.color');
					}
					// установка бригад
					else
					{
						$shiftDiv = $(this);
						shiftId = $(this).attr('shiftId');

						//shiftId = $(this).attr("shiftId");
						team = $('#${uiid} #editor #controls input[name=team]').val();

						// поиск смены по номеру бригады в тот же день
						$row.closest("table").find("td:nth-child(" + (column + 1) + ")").each( function()
						{
							var $cell = $(this);

							// пропуск самой ячейки
							if( $cell.closest( "tr" ).index() == row )
							{
								return;
							}

							var cellTeam = $cell.find(".team").text();
							if( cellTeam == team )
							{
								shiftId = $cell.attr("shiftId");
								$shiftDiv = $cell;
								return;
							}
						});

						if( team > 0 && !(shiftId > 0) )
						{
							alert( "Не установлена смена и не найдена смена с такой же бригадой!" );
							return;
						}
					}

					var url =
						"/user/plugin/callboard/work.do?method=callboardUpdateShift&graphId=${form.param.graphId}" +
						"&groupId=" + groupId +
						"&userId=" + userId +
						"&date=" + date +
						"&shiftId=" + shiftId +
						"&team=" + team;

					if( column == $row[0].cells.length - 1 )
					{
						url += "&lastDate=1";
					}

					$$.ajax.post(url).done((result) => {
						$shiftDiv = $shiftDiv.clone();
						$shiftDiv.find(".team").remove();

						$(this).css( "background-color", $shiftDiv.css( "background-color" ) ).html( $shiftDiv.html() ).append( "<span class='team'>" + team + "</span>" );

						$(this).attr( "shiftId", shiftId );
						$(this).attr( "minutes", result.data.minutes );

						// сумма часов (горизонталь)
						$groupRow.find("td:nth-child(2)" ).each( function()
						{
							updateHourSum( this );
						});

						// сумма дней со сменами смен (вертикаль)
						var table = $row.closest( "table" )[0];

						for( var i = $row.index(); i >= 0; i-- )
						{
							var td = table.rows[i].cells[column];
							if( $(td).hasClass( "sum" ) )
							{
								updateShiftCounts( td );
								break;
							}
						}
					})
				};

				// нажатие либо вхождение с нажатой мышью
				$callboard.find( "td.dayShift" )
					.mousedown( clickFunction )
					.mouseenter( function( event ){ event.preventDefault(); if( event.buttons == 1 ){ clickFunction.call( this ) } } );
			}

		})
		$("td.shiftUser > b").on("contextmenu", function(event){
			$(event.target).parent().append( $("#contextMenu") );
			$("#contextMenu").show();

			$(window).one("mousedown", function(e){
				if( e.buttons == 1 && $("#contextMenu").find( e.target ).length == 0 )
				{
					$("#contextMenu").hide();
				}
			});
			event.preventDefault();
		});

		function showSortMenu(object)
		{
			$(object).parents().each(function(){
				if ( $(this).attr("class") == "shiftUser" )
				{
					$("#sortMenu > ul").empty();
					$(this).parent().nextAll().each( function(){

						if ( $(this).attr("class") == "userShiftLine" )
						{
							var userId = $(this).find(".shiftUser").parent().attr("userId");
							var groupId = $(this).find(".shiftUser").parent().attr("groupId");
							$("#sortMenu > ul").append("<li draggable='true' userId=" + userId +" groupId="+ groupId +">" + $(this).find(".shiftUser").text() + "</li>");
						}
						else
						{
							return false;
						}
					});
				}
			});
			$("#contextMenu").hide();
			$("#sortMenu").dialog({
				height: 300,
				width: 300,
				modal: true,
				buttons: [ { text: "Отсортировать", click: function()
							{
								var groupId = $("#sortMenu > ul > li:first").attr("groupId");
								var url = '${changeOrderUrl}&groupId=' + groupId;
								var order = '&order=';
								var position = 1;

								$("#sortMenu > ul > li").each(function(){

									order+=$(this).attr("userId")+':'+position+';';
									position++;
								});
								url+=order;

								$$.ajax.post(url).done(() => {
									$$.ajax.load($("form[action='/user/plugin/callboard/work.do']"), $('#${uiid}').parent());
								})

								$( this ).dialog( 'destroy' );
							} },
							{ text: "Отмена", click: function(){ $(this).dialog("destroy"); } }
				],
				open: function (event, ui) {
					$(this).closest('.ui-dialog').find('.ui-dialog-titlebar-close').remove();
					$(this).closest(".ui-dialog")
						.find(".ui-button")
						.removeAttr("class")
						.addClass("btn-white")
				}
			});
			$("#sortMenu > ul").sortable();
		}

		$("div.rightContainer .userShiftLine").on("mouseover", function(event)
		{
			if ( $(event.target).attr("class").indexOf("dayShift") > -1 )
			{
				var userId = $(event.target).parent().attr("userId");
				var groupId = $(event.target).parent().attr("groupId");
				var $userRow = $("tr.userShiftLine[userId="+userId+"][groupId="+groupId+"] > td.shiftUser.Header");
				var indexTd = $(event.target).parent().find("td").index( event.target );
				var $dateCel = $( $("div.rightContainer tr.header td").get( indexTd ) );

				$(event.target).addClass( "selected-cel" );
				$userRow.addClass( "selected-cel" );
				$dateCel.addClass( "selected-cel" );

				$(event.target).one("mouseout", function(e)
				{
					$(event.target).removeClass( "selected-cel" );
					$userRow.removeClass( "selected-cel" );
					$dateCel.removeClass( "selected-cel" );
				});
			}
		});

		function hideShifts()
		{
			var graphId = "${form.param.graphId}";
			var hideEmptyShifts = "FALSE";
			var hideEmptyGroups = "FALSE";

			$("#hideMenu input").each( function()
			{
				if ( $(this).val() == "shifts" && $(this).prop("checked") )
				{
					$("div.rightContainer tr.userShiftLine").each(function()
					{
						if( $(this).find("td[shiftid]").length == 0 )
						{
							var userid = $(this).attr("userid");
							var groupid = $(this).attr("groupid");
							$(this).remove();
							$("div.leftContainer tr.userShiftLine[userid="+userid+"][groupid="+groupid+"]").remove();
						}
					});
					hideEmptyShifts = "TRUE";
				}
				else if( $(this).val() == "groups" && $(this).prop("checked") )
				{
					$("div.leftContainer tr.groupHeader").each(function()
					{
						if( $(this).next().attr("class") == "groupHeader" || $(this).next().length == 0 )
						{
							var index = $("div.leftContainer tr.groupHeader").index( $(this) );
							$(this).remove();
							$( $("div.rightContainer tr.groupHeader").get(index) ).remove();
						}
					});
					hideEmptyGroups = "TRUE";
				}
			});

			const url = "/user/plugin/callboard/work.do?method=callboardUpdateFilters&" + $$.ajax.requestParamsToUrl({ "hideEmptyShifts": hideEmptyShifts, "hideEmptyGroups": hideEmptyGroups, "graphId": graphId });
			$$.ajax.post(url);
		}

	</script>
	<c:if test="${not empty dateSet}">

		<div id="contextMenu" class="contextMenu combo" style="display: none;">
			<ul class="drop">
				<li>
					<span><a onclick="showSortMenu($(this))">Изменить порядок сортировки</a></span>
				</li>
			</ul>
		</div>

		<div id="sortMenu" class="combo" style="display: none;">
			<ul class="drop"></ul>
		</div>

		<div id="editor" class="in-table-cell mb1">
			<div id="controls">
				<div class="in-table-cell mb1">
					<div id="hideMenu">
						<ui:combo-check onChange="hideShifts()" prefixText="Скрыть">
							<jsp:attribute name="valuesHtml">
								<li>
									<input value="shifts" type="checkbox" <c:if test="${callboard.hideEmptyShifts}">checked</c:if> /><span> Пустые смены</span>
								</li>
								<li>
									<input value="groups" type="checkbox" <c:if test="${callboard.hideEmptyGroups}">checked</c:if> /><span> Пустые группы</span>
								</li>
							</jsp:attribute>
						</ui:combo-check>
					</div>

					<div class="pl05">
						<ui:combo-single hiddenName="mode" widthTextValue="50px" onSelect="
							$('#${uiid} #categories').toggle( this.value == 0 );
							$('#${uiid} #shiftArea').toggle( this.value == 0 );
							$('#${uiid} #teams').toggle( this.value == 1 );
						">
							<jsp:attribute name="valuesHtml">
								<li value="0">Смены</li>
								<li value="1">Бригады</li>
							</jsp:attribute>
						</ui:combo-single>
					</div>
				</div>
				<div id="categories">
					<ui:select-single list="${allowOnlyCategories}" hiddenName="categoryId"
						onSelect="var url = '/user/plugin/callboard/work.do?method=callboardAvailableShift&categoryId=' + this.value;
								$$.ajax.load( url, $('#${uiid} #shiftArea') );"
						style="width: 100%;" placeholder="Категория смен"/>
				</div>
				<div id="teams" style="display: none;">
					<input name="team" style="width: 100%;" placeholder="Бригада"/>
				</div>
			</div>
			<div id="shiftArea" class="pl1">
			</div>
		</div>

		<u:sc>
			<c:set var="selectorSample" value="#${uiid} #editor #controls"/>
			<c:set var="selectorTo" value="#${uiid} #editor #shiftArea"/>
			<%@ include file="/WEB-INF/jspf/same_height.jsp"%>
		</u:sc>

		<div id="addGroupToUserPopup" style="display: none;">
			<div id="current-graphId" style="display: none;">${form.param.graphId}</div>
			<div>Пользователь: <span id="userId" style="display: none;"></span><span id="userTitle"></span></div>
			<div>Выберите группу:
				<select id="selectGroupToAdd">
					<c:forEach var="group" items="${groupWithUsersMap}">
						<option value="${group.key}">${ctxUserGroupMap[group.key]}</option>
					</c:forEach>
				</select>
			</div>
			<div>Период: с <input type=text id="fromDate"/> по <input type=text  id="toDate" /></div>
		</div>

		<div class="callboard">
			<table class="hdata minimal">
				<tr class="header">
					<td class="shiftUser">
						<p:check action="org.bgerp.plugin.pln.callboard.action.WorkAction:callboardGetTabel">
							<c:if test="${not empty callboard.tabelConfig}">
								<a href="/user/plugin/callboard/work.do?method=callboardGetTabel&graphId=${form.param.graphId}&fromDate=${form.param.fromDate}&toDate=${form.param.toDate}">Табель</a>
							</c:if>
						</p:check>
					</td>
					<td><b>Час.</b></td>
					<c:forEach var="date" items="${dateSet}" varStatus="status">
						<c:choose>
							<c:when test="${not empty dateTypeMap}">
								<c:set var="dayType" value="${dateTypeMap[date].first}"/>

								<c:choose>
									<c:when test="${not empty dayType}">
										<td class="shiftDateHeader" date="${tu.format( date, 'ymd')}" style="color: ${dayType.color};" title="${dayType.title} ${tu.format( date, 'ymd' )}">
											<b>${tu.format( date, 'dd' )}<br/>${tu.getShortDateName( date )}</b>
										</td>
									</c:when>
									<c:otherwise>
										<td class="shiftDateHeader" date="${tu.format( date, 'ymd')}" title="??? ${tu.format( date, 'ymd' )}">
											<b>${tu.format( date, 'dd' )}<br/>${tu.getShortDateName( date )}</b>
										</td>
									</c:otherwise>
								</c:choose>
							</c:when>
							<c:otherwise>
								<td class="shiftDateHeader" date="${tu.format( date, 'ymd')}"><b>${tu.format( date, 'dd' )}<br/>${tu.getShortDateName( date )}</b></td>
							</c:otherwise>
						</c:choose>
					</c:forEach>
				</tr>

				<c:set var="fromDate" value="${form.getParamDate( 'fromDate' )}"/>

				<c:forEach var="item" items="${groupWithUsersMap}">
					<%-- подсчёт количества сотрудников, входящих в группу --%>
					<c:set var="userCount" value="${item.value.size()}"/>
					<c:choose>
						<c:when test="${userCount >0}"><c:set var="userCount" value="${userCount}"/></c:when>
						<c:otherwise><c:set var="userCount" value="0"/></c:otherwise>
					</c:choose>

					<tr class="groupHeader">
						<td class="shiftUser" groupId="${item.key}">
							<b>
								<c:choose>
									<c:when test="${item.key gt 0}">
										${ctxUserGroupMap[item.key]} (${userCount})
									</c:when>
									<c:otherwise>
										/ ${ctxUserGroupMap[callboard.groupId]} (${userCount}) /
									</c:otherwise>
								</c:choose>
							</b>
						</td>
						<td>&nbsp;</td>
						<c:forEach begin="1" end="${dateSet.size()}">
							<td class="sum">0</td>
						</c:forEach>
					</tr>
					<c:forEach var="user" items="${item.value}">
						<tr class="userShiftLine" groupId="${item.key}" userId="${user}"><%--
						--%><c:set var="userTitle" value="${ctxUserMap[user].title}"/><%--
						--%><td class="shiftUser header" onclick="addGroupToUser('${userTitle}','${user}')" title="${userTitle}">${userTitle}</td><%--

						--%><td class="sum">0</td><%--

						--%><c:set var="groupShifts" value="${workShiftMap[item.key]}"/><%--

						--%><c:set var="prevMinutes" value="0"/><%--

							подсчёт переходящих из дня предшествующего периоду количества минут на первый день периода
						--%><c:forEach var="workShiftOne" items="${groupShifts}"><%--
							--%><c:if test="${workShiftOne.userId == user and workShiftOne.date == prevDate}"><%--
								--%><c:set var="prevMinutes" value="${workShiftOne.getWorkMinutesInDay( prevDate, fromDate )}"/><%--
							--%></c:if><%--
						--%></c:forEach><%--

						--%><c:forEach var="date" items="${dateSet}" varStatus="status"><%--
							--%><c:set var="dayShiftTitle" value=""/><%--
							--%><c:set var="dayShiftTitleCounter" value="0"/><%--

							--%><%-- тормозит !!!!
								 Формирование всплывающей информации о распорядке дня
								<c:forEach var="workShiftOne" items="${workShiftMap[item.key]}">

									<c:if test="${workShiftOne.userId == user and workShiftOne.date == date}">
										<c:forEach var="workTypeTime" items="${workShiftOne.workTypeTime}" >

											<c:set var="dayShiftTitleCounter" value="${dayShiftTitleCounter+1}"/>

											<c:if test="${dayShiftTitle.length() >0}">
												<c:set var="dayShiftTitle" value="${dayShiftTitle}<br />"/>
											</c:if>

											<c:set var="dayShiftTitle" value="${dayShiftTitle}${dayShiftTitleCounter}) ${workTypeMap[workTypeTime.workTypeId].title} (c ${workTypeTime.formatedTimeFrom} до ${workTypeTime.formatedTimeTo})" />

											<c:if test="${workTypeTime.comment.length() > 0}">
												<c:set var="dayShiftTitle" value="${dayShiftTitle}: ${workTypeTime.comment}"/>
											</c:if>

										</c:forEach>
									</c:if>
								</c:forEach>
								--%><%--

							--%><c:remove var="color"/><%--
							--%><c:remove var="symbol"/><%--
							--%><c:remove var="shiftId"/><%--
							--%><c:remove var="team"/><%--
							--%><c:set var="minutes" value="0"/><%--

							--%><c:forEach var="workShiftOne" items="${groupShifts}"><%--
								--%><c:if test="${workShiftOne.userId == user and workShiftOne.date == date}"><%--
									--%><c:set var="shift" value="${shiftMap[workShiftOne.shiftId]}"/><%--

									--%><%-- если для рабочей смены установлен шаблон смены, проверить, нужно ли использовать его цвет --%><%--
									--%><c:if test="${shift.useOwnColor == true}"><%--
										--%><c:set var="color" value="${shift.color}"/><%--
										--%><c:set var="shiftId" value="${workShiftOne.shiftId}"/><%--
									--%></c:if><%--

									--%><c:set var="minutes" value="${workShiftOne.getWorkMinutesInDay( date, status.last ? date : null )}"/><%--

									--%><%-- TODO: подумать про расцветку "флагом" если не указан цвет смены --%><%--
									--%><c:set var="symbol" value="${shift.symbol}"/><%--
									--%><c:set var="team" value="${workShiftOne.team}"/><%--
								--%></c:if><%--
							--%></c:forEach><%--

							--%><%-- прорисовка смен --%>
							<c:choose>
								<c:when test="${item.key <= 0}">
									<c:set var="userGroupId" value="${callboard.groupId}"/>
								</c:when>
								<c:otherwise>
									<c:set var="userGroupId" value="${item.key}"/>
								</c:otherwise>
							</c:choose>

							<c:choose>
								<c:when test="${not empty availableDays.get( userGroupId ).get( user ) and availableDays.get( userGroupId ).get( user ).contains( date ) and empty shiftId}">
									<td class="dayShift available-day" style="background-color: ${color};" minutes="${minutes}" ${status.first ? "prevMinutes='".concat( prevMinutes ).concat( "'") : ""} ${shiftId gt 0 ? "shiftId='".concat( shiftId ).concat("'") : ""}>
								</c:when>
								<c:when test="${empty color}">
									<td class="dayShift nonavailable-day" style="background-color: ${color};" minutes="${minutes}" ${status.first ? "prevMinutes='".concat( prevMinutes ).concat( "'") : ""} ${shiftId gt 0 ? "shiftId='".concat( shiftId ).concat("'") : ""}>
								</c:when>
								<c:otherwise>
									<td class="dayShift" style="background-color: ${color};" minutes="${minutes}" ${status.first ? "prevMinutes='".concat( prevMinutes ).concat( "'") : ""} ${shiftId gt 0 ? "shiftId='".concat( shiftId ).concat("'") : ""}>
								</c:otherwise>
							</c:choose>
								<c:if test="${not empty symbol}"><%--
									--%><span class="symbol">${symbol}</span><%--
								--%></c:if><%--
								--%>&nbsp;<%--
								--%><span class="team"><%--
									--%><c:if test="${team > 0}"><%--
										--%>${team}<%--
									--%></c:if><%--
								--%></span><%--
							--%></td><%--
						--%></c:forEach>
						</tr>
					</c:forEach>
				</c:forEach>
			</table>
		</div>

		<%--
		<div class="callboardControlGroup">
			<input type="checkbox" onclick="showEmptyGroups( !$( this ).attr( 'checked' ) );" checked="checked"/>Скрыть пустые группы
		</div>
		--%>
	</c:if>
</div>
