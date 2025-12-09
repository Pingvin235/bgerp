// "use strict";

$$.demo = new function () {
	const dateSelectDialog = (element) => {
		$(element).datepicker("dialog", "", (value) => {
			console.log("date selected", value);
		}, { dateFormat: "dd.mm.yy", showButtonPanel: true });
	};

	const dateSelectDialogWithButtons = (element) => {
		$(element).datepicker("dialog", "", (value) => {
			console.log("date selected", value);
		}, { dateFormat: "dd.mm.yy", showButtonPanel: true });
	};

	// public functions
	this.dateSelectDialog = dateSelectDialog;
	this.dateSelectDialogWithButtons = dateSelectDialogWithButtons;
}