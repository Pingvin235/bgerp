/*
 * Date and time picker.
 */
"use strict";

$$.ui.datetime = new function () {
	/**
	 * Inits text input for picking date and time.
	 * @param {*} inputSelector selector of the input.
	 * @param {*} type date or time BGERP style format type.
	 * @param {*} format date or time Java style format.
	 */
	const init = (inputSelector, type, format) => {
		format = format
			.replaceAll('yyyy', 'y')
			.replaceAll('yy', 'y')
			.replaceAll('MM', 'm')
			.replaceAll('dd', 'd')
			.replaceAll('HH', 'h')
			.replaceAll('mm', 's')
			.replaceAll('ss', 's');

		if (type.startsWith('ymd'))
			$(inputSelector).inputmask(format, { "placeholder": "_" });

		$(inputSelector).keydown(function (e) {
			const input = $(inputSelector)[0];

			// Обработка таба и шифта
			if (!e.shiftKey && e.keyCode === 9) {
				const start = this.selectionStart;
				switch (start) {
					case 0:
					case 1:
					case 2:
						{
							setFocusAndRangeForDate(input, 3, 5, e, true);
							break;
						}
					case 3:
					case 4:
					case 5:
						{
							setFocusAndRangeForDate(input, 6, 10, e, true);
							break;
						}
					case 6:
					case 7:
					case 8:
					case 9:
					case 10:
						{
							if (type == 'ymd') {
								setFocusAndRangeForDate(input, 0, 2, e, false);
								break;
							}
							else {
								setFocusAndRangeForDate(input, 11, 13, e, true);
								break;
							}
						}
					case 11:
					case 12:
					case 13:
						{
							if (type == 'ymdh') {
								setFocusAndRangeForDate(input, 0, 2, e, false);
								break;
							}
							else {
								setFocusAndRangeForDate(input, 14, 16, e, true);
								break;
							}
						}
					case 14:
					case 15:
						{
							if (type == 'ymdhm') {
								setFocusAndRangeForDate(input, 0, 2, e, false);
								break;
							}
							else {
								setFocusAndRangeForDate(input, 17, 19, e, true);
								break;
							}
						}
				}
			}
			// Стрелка вверх, идет движение взад
			else if (e.keyCode === 38) {
				const start = this.selectionStart;
				switch (start) {
					case 0:
					case 1:
					case 2:
						{
							if (type == 'ymd') {
								setFocusAndRangeForDate(input, 6, 10, e, true);
								break;
							}
							if (type == 'ymdh') {
								setFocusAndRangeForDate(input, 11, 13, e, true);
								break;
							}
							if (type == 'ymdhm') {
								setFocusAndRangeForDate(input, 14, 16, e, true);
								break;
							}
							if (type == 'ymdhms') {
								setFocusAndRangeForDate(input, 17, 19, e, true);
								break;
							}
						}
					case 3:
					case 4:
					case 5:
						{
							setFocusAndRangeForDate(input, 0, 2, e, true);
							break;
						}
					case 6:
					case 7:
					case 8:
					case 9:
					case 10:
						{
							setFocusAndRangeForDate(input, 3, 5, e, true);
							break;
						}
					case 11:
					case 12:
					case 13:
						{
							setFocusAndRangeForDate(input, 6, 10, e, true);
							break;
						}
					case 14:
					case 15:
					case 16:
						{
							setFocusAndRangeForDate(input, 11, 13, e, true);
							break;
						}
					case 17:
					case 18:
					case 19:
						{
							setFocusAndRangeForDate(input, 14, 16, e, true);
						}
				}
			}
			// Обработка ввода цифр (в том числе и NumPad)
			else if ((48 <= e.keyCode && e.keyCode <= 57) || (96 <= e.keyCode && e.keyCode <= 105)) {
				const start = this.selectionStart;
				switch (start) {
					case 1:
						{
							setFocusAndRangeForDate(input, 3, 5, e, false);
							break;
						}
					case 4:
						{
							setFocusAndRangeForDate(input, 6, 10, e, false);
							break;
						}
					case 9:
						{
							if (type != 'ymd') {
								setFocusAndRangeForDate(input, 11, 13, e, false);
								break;
							}
						}
					case 12:
						{
							if (type != 'ymdh') {
								setFocusAndRangeForDate(input, 14, 16, e, false);
								break;
							}
						}
					case 15:
						{
							if (type != 'ymdhm') {
								setFocusAndRangeForDate(input, 17, 19, e, false);
								break;
							}
						}
				}
			}
			// Cтрелка влево
			else if (e.keyCode === 37) {
				const start = this.selectionStart;
				switch (start) {
					case 6:
						{
							setFocusAndRangeForDate(input, 3, 5, e, true);
							break;
						}
					case 3:
						{
							setFocusAndRangeForDate(input, 0, 2, e, true);
							break;
						}
					case 11:
						{
							if (type != 'ymd') {
								setFocusAndRangeForDate(input, 6, 10, e, true);
								break;
							}
						}
					case 14:
						{
							setFocusAndRangeForDate(input, 11, 13, e, true);
							break;
						}
					case 17:
						{
							setFocusAndRangeForDate(input, 14, 16, e, true);
							break;
						}
					case 20:
						{
							setFocusAndRangeForDate(input, 17, 19, e, true);
							break;
						}
					case 0:
						{
							if (type == 'ymd') {
								setFocusAndRangeForDate(input, 6, 10, e, true);
								break;
							}
							else if (type == 'ymdh') {
								setFocusAndRangeForDate(input, 11, 13, e, true);
								break;
							}
							else if (type == 'ymdhm') {
								setFocusAndRangeForDate(input, 14, 16, e, true);
								break;
							}
							else if (type == 'ymdhms') {
								setFocusAndRangeForDate(input, 17, 19, e, true);
								break;
							}
						}
				}
			}
			// Стрелка вниз, идет движение вперед
			else if (e.keyCode === 40) {
				const start = this.selectionStart;
				switch (start) {
					case 0:
					case 1:
					case 2:
						{
							setFocusAndRangeForDate(input, 3, 5, e, true);
							break;
						}
					case 3:
					case 4:
					case 5:
						{
							setFocusAndRangeForDate(input, 6, 10, e, true);
							break;
						}
					case 6:
					case 7:
					case 8:
					case 9:
					case 10:
						{
							if (type == 'ymd') {
								setFocusAndRangeForDate(input, 0, 2, e, true);
								break;
							}
							else {
								setFocusAndRangeForDate(input, 11, 13, e, true);
								break;
							}
						}
					case 11:
					case 12:
					case 13:
						{
							if (type == 'ymdh') {
								setFocusAndRangeForDate(input, 0, 2, e, true);
								break;
							}
							else {
								setFocusAndRangeForDate(input, 14, 16, e, true);
								break;
							}

						}
					case 14:
					case 15:
						{
							if (type == 'ymdhm') {
								setFocusAndRangeForDate(input, 0, 2, e, true);
								break;
							}
							else {
								setFocusAndRangeForDate(input, 17, 19, e, true);
								break;
							}
						}
					case 17:
					case 18:
					case 19:
					case 20:
						{
							setFocusAndRangeForDate(input, 0, 2, e, true);
							break;
						}
				}
			}
			// cтрелка вправо
			else if (e.keyCode === 39) {
				const start = this.selectionStart;
				switch (start) {
					case 2:
						{
							setFocusAndRangeForDate(input, 3, 5, e, true);
							break;
						}
					case 5:
						{
							setFocusAndRangeForDate(input, 6, 10, e, true);
							break;
						}
					case 10:
						{
							if (type == 'ymd') {
								setFocusAndRangeForDate(input, 0, 2, e, true);
								break;
							}
							else {
								setFocusAndRangeForDate(input, 11, 13, e, true);
								break;
							}
						}
					case 13:
						{
							if (type == 'ymdh') {
								setFocusAndRangeForDate(input, 0, 2, e, true);
								break;
							}
							else {
								setFocusAndRangeForDate(input, 14, 16, e, true);
								break;
							}
						}
					case 16:
						{
							if (type == 'ymdhm') {
								setFocusAndRangeForDate(input, 0, 2, e, true);
								break;
							}
							else {
								setFocusAndRangeForDate(input, 17, 19, e, true);
								break;
							}
						}
					case 19:
						{
							setFocusAndRangeForDate(input, 0, 2, e, true);
							break;
						}
				}
			}
		});
	}

	const setFocusAndRangeForDate = function (input, from, end, event, preventDefault) {
		if (preventDefault)
			event.preventDefault();

		setTimeout(function () {
			input.setSelectionRange(from, end);
		}, 50);
	}

	// public functions
	this.init = init;
}

