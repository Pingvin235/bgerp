<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="mb1">
	<%-- <select id="allWorkTypeSelect${uiid}" style="display: none;">
		<c:forEach var="item" items="${workTypeMap}">
			<option color="${item.value.workTypeConfig.color}" value="${item.value.id}" isDynamic="${item.value.type == 2 ? 1 : 0}">${item.value.title} (${item.value.id})</option>
		</c:forEach>
	</select> --%>

	<div class="in-table-cell" style="white-space: nowrap;">
		<div>Работы:</div>

		<div style="width: 100%;" class="pl05">
			<ui:select-single id="workTypeSelect${uiid}" list="${workTypeList}" hiddenName="param" style="width: 100%;"/>
		</div>

		<%-- <select id="workTypeSelect${uiid}" onchange="onSelect();" class="parametersSelect" style="min-width:200px;max-width:200px; width:200px">
			<c:forEach var="item" items="${workTypeList}">
				<option color="${item.workTypeConfig.color}" value="${item.id}" isDynamic="${item.type == 2 ? 1 : 0}">${item.title} (${item.id})</option>
			</c:forEach>
		</select> --%>

		<div class="pl05">
			с:
			<input id="timeFromInput${uiid}" type="text" placeholder="Выберите время" readonly="readonly"/>
			по:
			<input id="timeToInput${uiid}" type="text" placeholder="Выберите время" readonly="readonly"/>
		</div>

		<div>
			<button id="workTypeAdd${uiid}" type="button" class="btn-grey ml1">Добавить</button>
		</div>
	</div>

	<div id="workTypeList${uiid}" style="text-align: left;" class="ml05 mt05 in-mb05">
		<c:if test="${not empty shift}">
			<c:forEach var="item" items="${shift.workTypeTimeList}">
				<div>
					<input type="checkbox" name="rule" value="${item.workTypeId}:${item.dayMinuteFrom}:${item.dayMinuteTo}:${item.isDynamic ? 1 : 0}" checked="checked" hidden="hidden"/>
					<ui:button type="del" onclick="$(this).parent().remove();"/>
					<a class="pl05">${workTypeMap[item.workTypeId].title} (${item.workTypeId}) (с ${item.formatedTimeFrom} по ${item.formatedTimeTo})${item.isDynamic ? " <b style='color: red;'>(динамический)</b>" : ""}</a>
				</div>
			</c:forEach>
		</c:if>
	</div>
</div>

<script>
	var onSelect = function( e )
	{
		var selected = $( 'select#workTypeSelect${uiid} option:selected' );

		if( $( selected ).attr( 'isDynamic' ) === '1' )
		{
			$( '#dynMsg${uiid}' ).removeClass( 'invisible' );
		}
		else
		{
			$( '#dynMsg${uiid}' ).addClass( 'invisible' );
		}
	};

	var clearFields = function()
	{
		$( 'div#workTypeList${uiid}' ).empty();
		$( 'input#timeFromInput${uiid}' ).val( '' );
		$( 'input#timeToInput${uiid}' ).val( '' );
		$( 'select#workTypeSelect${uiid} option:first').attr( 'selected', 'selected' );
	};

	var getFullMinutesFromTime = function( time )
	{
		if( time.indexOf( ":" ) > -1 )
		{
			var minutes = time.substr( 0, time.indexOf( ":" ) ) * 60;
			minutes+= parseInt( time.substr( time.indexOf( ":" ) + 1 ) );

			return minutes;
		}
	};

	$( 'select#workTypeSelect${uiid}' ).combobox({
		selected : 	onSelect
	});

	$( 'input#timeFromInput${uiid}, input#timeToInput${uiid}' ).timepicker({
		hourMin: 0,
		hourMax: 23
	});

	$( '#workTypeAdd${uiid}' ).on( 'click', function()
	{
		var selected = $( '#workTypeSelect${uiid} input[type=hidden]' );

		if( !(selected && selected.val() > 0) )
		{
			alert( 'Выберите тип работы' );
			return;
		}

		var text = $( '#workTypeSelect${uiid} input[type=text]' ).val();
		var timeFrom = $( 'input#timeFromInput${uiid}' );
		var timeTo = $( 'input#timeToInput${uiid}' );

		if( $( timeFrom ).val().length == 0 || $( timeTo ).val().length == 0 )
		{
			alert( 'Необходимо выбрать период времени' );
			return;
		}

		var minFrom = getFullMinutesFromTime( $( timeFrom ).val() );
		var minTo = getFullMinutesFromTime( $( timeTo ).val() );
		var timeOccupied = false;

		$( 'input.workTypeRule${uiid}' ).each( function()
		{
			ruleMinFrom = parseInt( $(this).attr("minFrom") );
			ruleMinTo = parseInt( $(this).attr("minTo") );

			if( ( minFrom > ruleMinFrom && minFrom < ruleMinTo ) || ( minTo > ruleMinFrom && minTo < ruleMinTo ) || ( ruleMinFrom > minFrom && ruleMinFrom < minTo ) || ( ruleMinTo > minFrom && ruleMinTo < minTo ) )
			{
				timeOccupied = true;
			}
		});

		if( timeOccupied )
		{
			alert( 'Выбранный промежуток времени пересекается с одним или несколькими уже добавленными правилами' );
			return;
		}

		var append = '<div class="userWorkType${uiid} userWorkType" workTypeId="'+$(selected).val()+'" color="'+$( selected ).attr('color')+'" timeFrom="'+minFrom+'" timeTo="'+minTo+'">';
		append+='<input type="checkbox" class="workTypeRule${uiid}" name="rule" value="'+$(selected).val()+':'+minFrom+':'+minTo+':'+ ( $(selected).attr( 'isDynamic' ) == 1 ? 1 : 0 ) +'" minFrom="'+minFrom+'" minTo="'+minTo+'" checked="checked" hidden="hidden"/>';
		append+='<button type="button" onclick="$(this).parent().remove();" title="Удалить" class="btn-white btn-small">X</button>';
		append+=' <a class="pl05">'+text + ' (' + $(selected).val() + ') ' +' (с '+$(timeFrom).val()+' по '+$(timeTo).val()+')' + ( $(selected).attr( 'isDynamic' ) === '1' ? '<b style="color: red;"> (динамический)</b>' : '') + '</a>';

		<c:if test="${not empty addCommentField}">
			append+='<input class="comment" type="text"/>';
		</c:if>

		append+='</div>';

		$('#workTypeList${uiid}').append( append );
	});
</script>