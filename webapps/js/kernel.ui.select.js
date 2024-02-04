/*
 * Select UI element.
 */
"use strict";

$$.ui.select = new function () {
	// $$.ui.select.single
	this.single = new function () {
		/**
		 * Initializes a single select.
		 * @param {String} id container elements' ID.
		 * @param {Array} source source array with id and value fields.
		 * @param {String} value a current value.
		 * @param {Function} filter optional source filter function.
		 * @param {Function} onSelect option on select handler function.
		 */
		const init = (id, source, value, filter, onSelect) => {
			const selectDiv = document.getElementById(id);

			const inputText = selectDiv.querySelector("input[type='text']");
			const inputHidden = selectDiv.querySelector("input[type='hidden']");
			const icon = selectDiv.querySelector(".icon");

			if (inputText.hasAttribute("disabled"))
				icon.classList.add("disabled");
			else
				icon.addEventListener("click", (event) => {
					$(inputText).autocomplete("search", "");

					$(document).one("click", () => {
						$(selectDiv).find("ul").hide();
					});

					event.stopPropagation();
				});

			if (value) {
				const valueItem = source.find((item) => item.id === value);
				if (valueItem)
					inputText.value = valueItem.value;
			}

			$(inputText).autocomplete({
				minLength: 0,
				source: (request, response) => {
					let filteredSource = $.ui.autocomplete.filter(source, request.term);
					if (filter)
						filteredSource = filter(id, filteredSource);
					response(filteredSource);
				},
				select: (event, ui) => {
					inputHidden.value = ui.item.id;
					inputText.value = ui.item.value;

					if (onSelect) {
						inputHidden.$$onSelect = onSelect;
						// to make 'this' pointing to 'inputHidden'
						return inputHidden.$$onSelect($(inputHidden), $(inputText));
					}
				},
				appendTo: "#" + id,
				open: () => {
					// cleanup calculated widths and positions
					$(inputText).autocomplete("widget")
						.css("width", "")
						.css("top", "")
						.css("left", "");
				}
			});

			// cleanup hidden on input cleanup
			inputText.addEventListener("keyup", (event) => {
				if (!event.value)
					inputHidden.value = "";
			});

			$(selectDiv).find("ul").removeClass("ui-autocomplete ui-front");
		}

		// public functions
		this.init = init;
	}

	// $$.ui.select.mult
	this.mult = new function () {
		const liUp = (el) => {
			const currentLi = el.parentNode;
			const $prev = $(currentLi).prev();
			if ($prev.length > 0) {
				$(currentLi).insertBefore($prev);
			}
		}

		const liDown = (el) => {
			const currentLi = el.parentNode;
			const $next = $(currentLi).next();
			if ($next.length > 0) {
				$(currentLi).insertAfter($next);
			}
		}

		const liDel = (el) => {
			$(el.parentNode).remove();
		}

		/**
		 * Handles a selected item in the related select-single.
		 * @param {jQuery} $hidden hidden input of the related select-single.
		 * @param {jQuery} $input text input of the related select-single.
		 * @param {string} uiid ID of the top DIV element.
		 * @param {string} upDownIcons HTML fragment with up and down icons.
		 * @returns false
		 */
		const onSelect = ($hidden, $input, uiid, upDownIcons) => {
			const id = $hidden.val();
			if (!id) {
				console.error('A value is not set');
				return;
			}

			const title = $input.val();
			const hiddenName = $hidden.attr('name');

			$(document.getElementById(uiid).querySelector('ul.drop-list')).append(
				"<li>" +
					"<span class='delete ti-close' onclick='$$.ui.select.mult.liDel(this)'></span>" +
					upDownIcons +
					"<span class='title'>" + title + "</span>" +
					"<input type='hidden' name='" + hiddenName + "' value='" + id + "'/>" +
				"</li>"
			);

			$input.val('');
			$hidden.val('');

			// otherwise text appears in $input again
			return false;
		}

		/**
		 * Filters drop-down values for the related select-single element.
		 * @param {string} id ID of the related select-single top DIV element.
		 * @param {Array} filteredSource all the values.
		 * @returns a filtered array.
		 */
		const filter = (id, filteredSource) => {
			const dropList = document.getElementById(id).closest('.select-mult').querySelector('.drop-list');

			const values = new Set();
			dropList.querySelectorAll('input[type=hidden]').forEach(hidden =>
				values.add(hidden.value)
			);

			return filteredSource.filter(item => !values.has(item.id));
		}

		// public functions
		this.liUp = liUp;
		this.liDown = liDown;
		this.liDel = liDel;
		this.onSelect = onSelect;
		this.filter = filter;
	}
}