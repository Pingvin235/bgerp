/*
 * Select UI elements.
 */
"use strict";

$$.ui.select = new function () {
	const single = new function () {
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
						filteredSource = filter(filteredSource);
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

	// public objects
	this.single = single;
}