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

	/**
	 * Checks if subject field is empty. Shows confirmation message if it is.
	 *
	 * @param {HTMLFormElement} form form element.
	 * @param {string} message confirmation message.
	 * @return {boolean} subject is filled out or confirmed to be empty.
	 */
	const checkSubject = (form, message) => {
		let subject = form.subject;
		if (!subject)
			return true;

		subject = subject.value;

		return subject || confirm(message);
	}

	/**
	 * Checks if attachment exists. Show confirmation message if it isn't and a message text points to existence of it.
	 *
	 * @param {HTMLFormElement} form form element.
	 * @param {string} message confirmation message.
	 * @return {boolean} attachment exists or no text markers points to existence of it, or it is confirmed to not be presented.
	 */
	const checkAttach = (form, message) => {
		if (form.fileId || form.tmpFileId || form.announcedFileId)
			return true;

		let text = form.text;
		if (!text)
			return true;

		text = text.value.toLowerCase();

		let contains = false;
		for (const marker of ['attach', 'anhang', 'влож']) {
			if (contains = text.includes(marker))
				break;
		}

		return !contains || confirm(message);
	}

	// public functions
	this.editorTypeChanged = editorTypeChanged;
	this.subjectTableInit = subjectTableInit;
	this.checkSubject = checkSubject;
	this.checkAttach = checkAttach;
}

