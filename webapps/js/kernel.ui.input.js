/*
 * Input text UI elements
 */
"use strict";

$$.ui.input = new function () {
	const debug = $$.debug('ui.input');

	// TODO: Move $$.ui.inputTextInit here

	// TODO: Move isNumberKey here

	this.decimal = new function () {
		const onkeydown = (event, digits) => {
			return isNumberKey(event, digits);
		}

		/**
		 * Paste only decimal digits and separator chars from clipboard
		 * @param {Event} event 'onpaste' event of 'text' input field
		 * @param {Number} digits amount of allowed digits after decimal separator
		 * @return {Boolean} always 'false' to prevent default event processing
		 */
		const onpaste = (event, digits) => {
			const target = event.target;

			const selectionStart = target.selectionStart;
			const selectionEnd = target.selectionEnd;

			const pasted = ((event.clipboardData && event.clipboardData.getData("Text")) || '').replace(',', '.');
			let paste = pasted;

			if (pasted) {
				paste = '';

				for (let i = 0; i < pasted.length; i++) {
					const c = pasted.charAt(i);
					if (('0' <= c && c <= '9') || (c == '.' && digits > 0))
						paste += c;
				}
			}

			const value = target.value;

			event.key = paste;

			if (isNumberKey(event, digits))
				target.value = value.substring(0, selectionStart) + paste + value.substring(selectionEnd);

			debug('onpaste', event, selectionStart, selectionEnd, pasted);

			// prevent default event processing
			return false;
		}

		// public functions
		this.onkeydown = onkeydown;
		this.onpaste = onpaste;
	}
}
