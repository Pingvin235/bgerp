/*
 * Message dialog.
 */
$$.shell.message = new function () {
	const debug = $$.debug("shell.message");

	const $dialog = () => {
		return $("#messageDialog");
	}

	/**
	 * Inits message dialog.
	 * @param {*} options parameters for jQueryUI dialog() function.
	 */
	const init = (options) => {
		debug("init");
		$dialog().dialog(options);
	}

	/**
	 * Shows HTML message dialog.
	 * @param {*} title title.
	 * @param {*} message HTML message, '\n' replaced automatically to '<br/>'
	 */
	const show = (title, message) => {
		const $dlg = $dialog();
		if (!$dlg.dialog("isOpen")) {
			$("#messageDialogMessage").html(message.replace(/\\n/g, "<br/>"));
			$dlg.dialog("option", "title", title);

			$dlg.dialog("open");
		}
	}

	/**
	 * Closes message dialog.
	 */
	const close = () => {
		$dialog().dialog("close");
	}

	// public functions
	this.init = init;
	this.show = show;
	this.close = close;
}
