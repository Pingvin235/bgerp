// "use strict";

$$.param = new function() {
	const debug = $$.debug("param");

	const dirChanged = (form, item, $hidden) => {
		form.action.value = $(item).attr("action");
		form.directoryId.value = $hidden.val();
		$$.ajax.loadContent(form);
	}

	const listcount = new function () {
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

	// public functions
	this.dirChanged = dirChanged;
	// public objects
	this.listcount = listcount;
}
