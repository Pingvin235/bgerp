// "use strict";

// global key's state, pressed 'alt' etc.
$$.keys = new function () {
	window.onkeydown = (e) => {
		$$.keys[e.keyCode] = 1;
		setTimeout(function () {
			delete $$.keys[e.keyCode]
		}, 4000);
	};

	window.onkeyup = (e) => {
		delete $$.keys[e.keyCode];
	};

	// public functions
	this.altPressed = () => {
		return !!$$.keys[18];
	}
}

function enterPressed(e) {
	return e.which == 13;
}

/**
 * Validates text inputs for entering numeric values only, possible with decimal separator.
 * @param {Event} e 'onkeydown' or another event with 'key' property of 'text' input
 * @param {Number} digits amount of digits after dot, if not defined than 2
 * @return {Boolean} is input change allowed
 */
function isNumberKey(e, digits) {
	if (digits === undefined)
		digits = 2;

	if (e.ctrlKey || ['Backspace', 'ArrowLeft', 'ArrowRight', 'Home', 'End', 'Delete'].includes(e.key))
		return true;

	const target = e.target;
	const value = target.value;

	const valueCandidate = value.substring(0, target.selectionStart) + e.key + value.substring(target.selectionEnd);

	let result = !isNaN(valueCandidate);
	if (result && digits > 0) {
		const pos = valueCandidate.indexOf('.');
		result = pos < 0 || valueCandidate.length - pos - 2 < digits;
	}

	return result;
}


