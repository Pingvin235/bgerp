/*
 * Message dialog.
 */
$$.shell.message = new function () {
	const debug = $$.debug("shell.message");

	const $dialog = () => {
		return $("#message-dialog");
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
	 * @param {*} message HTML message
	 */
	const show = (title, message) => {
		const $dlg = $dialog();
		if (!$dlg.dialog("isOpen")) {
			$("#message-dialog-message").html(message);
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
