// "use strict";

$$.demo = new function () {
	const dateSelectDialog = (element) => {
		$(element).datepicker("dialog", "", (value) => {
			console.log("date selected", value);
		});
	};

	const dateSelectDialogWithButtons = (element) => {
		$(element).datepicker("dialog", "", (value) => {
			console.log("date selected", value);
		}, { showButtonPanel: true });
	};

	// public functions
	this.dateSelectDialog = dateSelectDialog;
	this.dateSelectDialogWithButtons = dateSelectDialogWithButtons;
}