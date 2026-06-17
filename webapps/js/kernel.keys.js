// "use strict";

/**
 * Plus key event handling functions
 */
$$.keys = new function () {
	/** Global pressed key codes */
	const pressed = [];

	window.onkeydown = (e) => {
		pressed[e.keyCode] = 1;
		setTimeout(function () {
			delete pressed[e.keyCode]
		}, 4000);
	};

	window.onkeyup = (e) => {
		delete pressed[e.keyCode];
	};

	/**
	 * @returns {Boolean} is 'Alt' button pressed down globally
	 */
	const altPressed = () => {
		return !!pressed[18];
	}

	/**
	 * Check if 'Enter' was pressed in a keypress or keydown event
	 * @param {Event} e the event
	 * @returns {Boolean} is the key was pressed in the event
	 */
	const enterPressed = (e) => {
		return e.which == 13;
	}

	/**
	 * Validates text inputs for entering numeric values only, possible with decimal separator.
	 * @param {Event} e 'onkeydown' or another event with 'key' property of 'text' input
	 * @param {Number} digits amount of digits after dot, if not defined then 2
	 * @return {Boolean} is input change allowed
	 */
	function numericPressed(e, digits) {
		if (!(digits >= 0))
			digits = 2;

		if (e.ctrlKey || ['Backspace', 'ArrowLeft', 'ArrowRight', 'Home', 'End', 'Delete'].includes(e.key))
			return true;

		const target = e.target;
		const value = target.value;

		const valueCandidate = value.substring(0, target.selectionStart) + e.key + value.substring(target.selectionEnd);

		let result = !isNaN(valueCandidate);
		if (result) {
			const pos = valueCandidate.indexOf('.');
			if (pos > 0)
				result = digits > 0 && valueCandidate.length - pos - 2 < digits;
		}

		return result;
	}

	// public functions
	this.altPressed = altPressed;
	this.enterPressed = enterPressed;
	this.numericPressed = numericPressed;
}

function enterPressed(e) {
	console.warn($$.deprecated);
	return $$.keys.enterPressed(e);
}
