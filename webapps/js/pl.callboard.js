
addEventProcessor( 'ru.bgcrm.event.DynamicShiftUserEvent', processShiftUserEvent );

function processShiftUserEvent( e )
{
	if( e.dynamicSettingsSet.length == 0 )
	{
		return;
	}

	var point = e.dynamicSettingsSet[0];

	if( $( '#dynamicShiftDialog' ).length == 0 )
	{
		var options = "";

		for( var item in point.availableTimeSet ) {
			options+="<option>"+point.availableTimeSet[item]+"</option>";
		}

		$( "body" ).append( "<div id='dynamicShiftDialog' style='text-align: center;'><span>Укажите точное время для смены:<br><b>"+ point.title +"</b></span> " +
							"<div style='display: inline-block;'><select>" + options + "</select></div>" +
							"</div>" );

		$( '#dynamicShiftDialog' ).dialog({
			modal: true,
			draggable: false,
			resizable: false,
			width: 300,
			closeOnEscape: false,
			autoOpen: false,
		    title: "Назначение времени",

			open:function()
			{
				$("#dynamicShiftDialog").parents(".ui-dialog:first").find(".ui-dialog-titlebar-close").remove();
			},

		    close: function()
		    {
		    	$('#dynamicShiftDialog').remove();
		    },

		    buttons:
	    	{
		    	'Установить' : function()
		    	{
		    		var url = '/admin/plugin/callboard/work.do?action=setDynamicShiftTime&workShiftId='+point.workShift.id+'&userId='+point.workShift.userId+'&graphId='+
		    				   point.workShift.graphId+'&groupId='+point.workShift.groupId+'&workTypeId='+point.workShift.workTypeTime[0].workTypeId+
		    				   '&selectedTime='+$(this).find('select option:selected').val();

		    		if( sendAJAXCommand( url ) )
	    			{
	    				$( this ).dialog( 'close' );
	    			}
		    	}
	    	},
			open: function()
			{
				$("#dynamicShiftDialog").keypress(function(e)
				{
					if (e.keyCode == $.ui.keyCode.ENTER)
					{
						$(this).parent().find(".ui-dialog-buttonpane button:first").click();
					}
				});
			}
		});
	}

	if( !$("#dynamicShiftDialog").dialog( "isOpen" ) )
	{
		$("#dynamicShiftDialog").dialog( "open" ).parent().find( ".ui-dialog-titlebar-close" ).hide();
	}
}

function addGroupToUser(userTitle, userId)
{
	$("#userTitle").text( userTitle );
	$("#userId").text( userId );
	$("#fromDate").datepicker();
	$("#toDate").datepicker();
	$("#toDate").val("");

	$("#fromDate").val( $.datepicker.formatDate('dd.mm.yy', new Date()) );

	$("#todayDate").val( date );

	$("#addGroupToUserPopup").dialog({
		autoOpen: true,
		height: 220,
		width: 350,
		modal: true,
		buttons: {
			"Ok": function()
			{
				sendAJAXCommandWithParams("/admin/plugin/callboard/work.do?action=userChangeGroup",
						{
							"graphId":$("#current-graphId").text(),
							"fromDate":$("#fromDate").val(),
							"toDate":$("#toDate").val(),
							"group":$("#selectGroupToAdd").val(),
							"userId":$("#userId").text()
						});
				$("#addGroupToUserPopup").dialog( "destroy" );
			},
			"Cancel": function() {
				console.log(this);
				$("#addGroupToUserPopup").dialog( "destroy" );
			}
		},
	});
}
