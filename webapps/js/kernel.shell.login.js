/*
 * Login related logic.
 */
$$.shell.login = new function () {
	const debug = $$.debug("shell.login");

	/**
	 * @returns jQuery selector of '#loginErrorMessage' element.
	 */
	const $errorMessage = () => {
		return $("#loginErrorMessage");
	}

	/**
	 * Login request.
	 * @return a promise, for that can be added a done callback.
	 */
	const post = () => {
		const $msg = $errorMessage().text("");

		return $.ajax({
			url: "/login.do",
			method: "POST",
			dataType: "json",
			data: {
				j_username: $('input[name="j_username"]').val(),
				j_password: $('input[name="j_password"]').val(),
				responseType: "json"
			}
		}).fail((jqXHR, textStatus) => {
			// show detailed auth error or 'Connection Error', since 'textStatus' is always 'error'
			$msg.text(jqXHR.status == 401 ? jqXHR.responseText : "Connection Error");
		}).done((result) => {
			const title = result.data.title;
			if (title) {
				$("#head .right a.profile").text(title);
			}
		});
	}

	/**
	 * @returns jQuery selector of '#loginDialog' element.
	 */
	const $dialog = () => {
		return $("#loginDialog");
	}

	// Login dialog functions

	/**
	 * Inits login dialog.
	 * @param {*} options parameters for jQueryUI dialog() function.
	 */
	const init = (options) => {
		$dialog().dialog(options);

		$dialog().submit((e) => {
			$$.shell.login.post().done(() => {
				close(true);
			});
			// prevent auth form submit by browser
			e.preventDefault();
		});

		close();
	}

	/**
	 * Shows the dialog and stops $$.timer.
	 */
	const show = () => {
		const $dlg = $dialog();

		if (!$dlg.dialog("isOpen")) {
			$dlg.dialog("open");

			$dlg.dialog().on("dialogclose", () => {
				// continue pooling
				$$.timer.start();
			});

			$$.shell.message.close();
		}

		$$.timer.stop();
	}

	/**
	 * Closes the dialog, cleans error message and optionally starts $$.timer.
	 * @param {boolean} startTimer resume $$.timer.
	 */
	const close = (startTimer) => {
		$dialog().dialog("close");
		$errorMessage().text("");
		if (startTimer)
			$$.timer.start();
	}

	// public functions
	this.init = init;
	this.show = show;
	this.post = post;
}

