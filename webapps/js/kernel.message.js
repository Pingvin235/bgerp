// "use strict";

$$.message = new function() {
	const debug = $$.debug("message");

	/**
	 * On change drop-down selector of editor.
	 * 
	 * @param {*} editorId editor ID or prefix for plugin editors.
	 * @param {*} typeComboId ID of drop-down with message types.
	 * @param {*} uploadFormId ID of file upload form.
	 */
	const editorTypeChanged = (editorId, typeComboId, uploadFormId) => {
		const typeId = $('#' + typeComboId).find('input[name=typeId]').val();
		const $selectedTypeLi = $('#' + typeComboId + ' ul.drop li[value="' + typeId + '"]');

		const editor = $selectedTypeLi.attr('editor');

		const $activeEditor = editor ? $('form[id="'+ editorId + '-' + editor + '"]') : $('#' + editorId);

		$activeEditor.parent().find('>form').hide();

		$activeEditor.show();

		$('#' + typeComboId).detach().appendTo($activeEditor.find('#typeSelectContainer'));

		const dataKey = "pasteListener";
		if (!$activeEditor.data(dataKey)) {
			$$.ui.setPasteUploadListener($activeEditor.attr("id"), uploadFormId);
			$activeEditor.data(dataKey, true);
		}
	}

	// public functions
	this.editorTypeChanged = editorTypeChanged;
}

