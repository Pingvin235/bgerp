/*
 * Plugin Callboard
 */
"use strict";

$$.callboard = new function () {
	// $$.callboard.calendar
	this.calendar = new function () {
		/**
		 * Creates a datapicker for a year.
		 * @param {String} id the parent HTML element ID.
		 * @param {String} year the year.
		 * @param {Function} onCalendarDateSelect callback.
		 */
		const init = (id, year, onCalendarDateSelect) => {
			$(document.getElementById(id)).find('div.datepicker').datepicker({
				changeMonth: false,
				changeYear: false,
				dateFormat: "dd.mm.yy",
				minDate: new Date(year, 0, 1),
				maxDate: new Date(year, 11, 31),
				numberOfMonths: [3, 4],
				onSelect: function (date, inst) {
					inst.inline = false;
					onCalendarDateSelect(date);
				}
			});
		}
		// public functions
		this.init = init;
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
				const url = "/admin/plugin/callboard/work.do?method=userChangeGroup&" + $$.ajax.requestParamsToUrl({
					"graphId":$("#current-graphId").text(),
					"fromDate":$("#fromDate").val(),
					"toDate":$("#toDate").val(),
					"group":$("#selectGroupToAdd").val(),
					"userId":$("#userId").text()
				});

				$$.ajax.post(url).done(() =>
					$("#addGroupToUserPopup").dialog( "destroy" )
				);
			},
			"Cancel": function() {
				console.log(this);
				$("#addGroupToUserPopup").dialog( "destroy" );
			}
		},
	});
}
