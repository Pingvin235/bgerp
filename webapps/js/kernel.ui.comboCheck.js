/*
 * combo-check tag support.
 */
"use strict";

$$.ui.comboCheck = new function () {
	const init = ($comboDiv, onChange) => {
		const $drop = $comboDiv.find('ul.drop');

		const doOnChange = (input) => {
			if (onChange) {
				// to make 'this' equals to the checkbox input
				input.onChange = onChange;
				input.onChange();
			}
		}

		$comboDiv.find("ul.drop").on("click", "li input", function (event) {
			updateCurrentTitle($comboDiv);
			doOnChange(this);
			event.stopPropagation();
		});

		$comboDiv.find("ul.drop").on("click", "li", function () {
			const input = $(this).find("input")[0];
			input.checked = !(input.checked);
			updateCurrentTitle($comboDiv);
			doOnChange(input);
			return false;
		});

		$$.ui.dropOnClick($comboDiv, $drop);

		$comboDiv.find("div.icon").click(function (event) {
			$comboDiv.find("ul.drop li input").each(function () {
				this.checked = false;
			});
			updateCurrentTitle($comboDiv);

			event.stopPropagation();
		});

		updateCurrentTitle($comboDiv);
	}

	const updateCurrentTitle = ($comboDiv) =>  {
		let checkedCount = 0;
		let titles = "";

		$comboDiv.find("ul.drop li input[type=checkbox]").each(function () {
			if (this.checked) {
				checkedCount++;
				const title = $(this).next().text();
				if (titles.length > 0) {
					titles += ", ";
				}
				titles += title;
			}
		});

		$comboDiv.find('.text-value').text("[" + checkedCount + "] " + titles);
	}

	const uncheck = (object) => {
		const $parent = $(object).closest("ul");
		if ($parent.find("input[type=checkbox]:checked").length === 0)
			$parent.find("input[type=checkbox]").prop("checked", true);
		else
			$parent.find("input[type=checkbox]").prop("checked", false);
	}

	// public functions
	this.init = init;
	this.uncheck = uncheck;
}