/*
 * Combo UI elements
 */
"use strict";

$$.ui.combo = new function () {

	// $$.ui.combo.check
	this.check = new function () {
		const init = ($comboDiv, onChange) => {
			const $drop = $comboDiv.find('ul.drop');

			const doOnChange = (input) => {
				if (onChange) {
					// to make 'this' equals to the checkbox input
					input.onChange = onChange;
					input.onChange();
				}
			}

			$comboDiv.find("ul.drop").on("click", "li input[type=checkbox]", function (event) {
				updateCurrentTitle($comboDiv);
				doOnChange(this);
				event.stopPropagation();
			});

			$comboDiv.find("ul.drop").on("click", "li", function () {
				const input = this.querySelector("input[type=checkbox]");
				if (input) {
					input.checked = !(input.checked);
					updateCurrentTitle($comboDiv);
					doOnChange(input);
					return false;
				}
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

		/**
		 * Values search
		 * @param {HTMLInputElement} input text field with search substring
		 */
		const filter = (input) => {
			const mask = input.value.toLowerCase();
			$(input).closest('ul').find('li:gt(0)').each(function () {
				var content = $(this).text().toLowerCase();
				$(this).toggle(content.indexOf(mask) >= 0);
			});
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
		this.filter = filter;
		this.uncheck = uncheck;
	}

	// $$.ui.combo.single
	this.single = new function () {

		const init = ($comboDiv, onChange) => {
			const $drop = $comboDiv.find('ul.drop');
			const $hidden = $comboDiv.find('input[type=hidden]');

			$$.ui.dropOnClick($comboDiv, $drop);

			$drop.on('click', 'li:not(.filter)', function () {
				$hidden.val($(this).attr("value"));
				updateCurrentTitle($comboDiv, $drop, $hidden);

				if (onChange) {
					// to make 'this' equals to the hidden input
					$hidden[0].onSelect = onChange;
					$hidden[0].onSelect(this);
				}

				$drop.hide();

				return false;
			});

			updateCurrentTitle($comboDiv, $drop, $hidden);
		}

		const updateCurrentTitle = ($comboDiv, $drop, $hidden) => {
			// by default the first item is selected
			let $currentLi = $drop.find('li:not(.filter):first');

			const currentValue = $hidden.val();

			const $foundLi = $drop.find("li[value='" + currentValue + "']");
			if ($foundLi.length !== 0) {
				$currentLi = $foundLi;
			}

			let $currentTitle = $currentLi.find('span.title');
			if ($currentTitle.length === 0) {
				$currentTitle = $currentLi;
				$hidden.val($currentLi.attr('value'));
			}

			$drop.find('li').removeAttr('selected');
			$currentLi.attr('selected', '1');

			$comboDiv.find('.text-value').html($currentTitle.html());
		};

		/**
		 * Executes filtering in combo-single element
		 * @param {*} input text input element
		 */
		const filter = (input) => {
			const $input = $(input);
			const mask = $input.val().toLowerCase();
			$(input.parentNode.parentNode).find('li:gt(0)').each(function () {
				const content = $(this).text().toLowerCase();
				$(this).toggle(content.indexOf(mask) >= 0);
			});
		}

		// public functions
		this.init = init;
		this.filter = filter;
	}
}