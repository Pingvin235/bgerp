// "use strict";

$$.message = new function() {
	const debug = $$.debug("message");

	const editorTypeChanged = (editorUiid, typeComboUiid) => {
		const typeId = $('#' + typeComboUiid).find('input[name=typeId]').val();
		const $selectedTypeLi = $('#' + typeComboUiid + ' ul.drop li[value="' + typeId + '"]');

		const editor = $selectedTypeLi.attr('editor');

		const $activeEditor = editor ? $('form[id="'+ editorUiid+ '-' + editor + '"]') : $('#' + editorUiid);

		$activeEditor.parent().find('>form').hide();

		$activeEditor.show();

		$('#' + typeComboUiid).detach().appendTo($activeEditor.find('#typeSelectContainer'));
	}

	// public functions
	this.editorTypeChanged = editorTypeChanged;
}

