/*
 * Plugin DBA
 */
"use strict";

$$.dba = new function() {
	const debug = $$.debug("report");

	/**
	 * Loads stored query to editor.
	 * @param {*} button
	 */
	const queryHistoryGet = (button) => {
		const url = "/admin/plugin/dba/query/history.do?action=get&id=" + button.form.queryHistoryId.value;
		$$.ajax
			.post(url)
			.done(result => {
				// https://stackoverflow.com/questions/11581516/get-codemirror-instance
				const editor = button.form.querySelector('div.CodeMirror').CodeMirror;
				editor.getDoc().setValue(result.data.query);
			});
	}

	/**
	 * Removes stored query.
	 * @param {*} button
	 */
	const queryHistoryDel = (button) => {
		const url = "/admin/plugin/dba/query/history.do?action=del&id=" + button.form.queryHistoryId.value;
		$$.ajax
			.post(url)
			.done(() => $$.ajax.loadContent(button) );
	}

	// public functions
	this.queryHistoryGet = queryHistoryGet;
	this.queryHistoryDel = queryHistoryDel;
}
