// "use strict";

$$.param = new function () {
	const debug = $$.debug("param");

	const dirChanged = (form, item, $hidden) => {
		form.action.value = $(item).attr("action");
		form.directoryId.value = $hidden.val();
		$$.ajax.loadContent(form);
	}

	// public functions
	this.dirChanged = dirChanged;

	// $$.param.listcount
	this.listcount = new function () {
		/**
		 * Adds a new value in listcount editor.
		 * @param {jQuery} $table values table selector.
		 * @param {Array} errors array with two alerted errors.
		 * @returns
		 */
		const addValue = ($table, errors) => {
			const form = $table.closest("form")[0];

			const itemId = form.newItemId.value;
			const itemTitle = form.newItemTitle.value;
			const itemCount = form.newItemCount.value;

			if (!itemId) {
				alert(errors[0]);
				return;
			}

			if (!itemCount) {
				alert(errors[1]);
				return;
			}

			$$.ajax
				.post(
					"/user/parameter.do?action=parameterListcountAddValue&itemId=" + itemId +
					"&itemTitle=" + encodeURIComponent(itemTitle) + "&itemCount=" + encodeURIComponent(itemCount),
					{ html: true }
				).done(result => {
					$table.find("tr:last-child").before(result);
				});
		}

		// public functions
		this.addValue = addValue;
	}

	// $$.param.phone
	this.phone = new function () {
		/**
		 * Adds a new empty value to phone parameter editor.
		 * @param {HTMLButtonElement} button add button that was clicked.
		 */
		const addValue = (button) => {
			const $tr = $(
				"<tr>" +
					"<td><input type='text' name='phone' class='w100p'/></td>" +
					"<td><input type='text' name='comment' class='w100p'/></td>" +
					"<td><button class='btn-white btn-small icon' onclick='$$.param.phone.delValue(this)'><i class='ti-trash'></i></button></td>" +
				"</tr>"
			);
			$(button).closest('table').find('tr:last').after($tr);
			$tr.find("input[name='phone']").focus();
		}
		/**
		 * Deletes a value in phone parameter editor.
		 * @param {HTMLButtonElement} button deletion button.
		 */
		const delValue = (button) => {
			$(button).closest('tr').remove()
		}

		// public functions
		this.addValue = addValue;
		this.delValue = delValue;
	}
}
