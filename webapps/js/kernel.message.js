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

	/**
	 * Init subjects list table for message processing.
	 *
	 * @param {*} tableId table ID.
	 * @param {*} editorId editor DIV element ID.
	 * @param {*} selectedId optional ID for counter of selected elements.
	 */
	const subjectTableInit = (tableId, editorId, selectedId) => {
		const $dataTable = $('#' + tableId);

		const callback = function ($row) {
			const openUrl = $row.attr('openUrl');
			if (openUrl) {
				$$.ajax.load(openUrl, $('#' + editorId));
				$dataTable.find('tr').removeClass('hl');
				$row.addClass('hl');
			} else {
				alert('Not found attribute openUrl!');
			}
		};
		doOnClick($dataTable, 'tr:gt(0)', callback);

		if (selectedId)
			$$.table.select($dataTable, $('#' + selectedId), 'init');
	}

	// public functions
	this.editorTypeChanged = editorTypeChanged;
	this.subjectTableInit = subjectTableInit;
}

