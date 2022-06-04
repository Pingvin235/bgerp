// "use strict";

$$.table = new function() {
	/**
	 * Manages checkboxes in table: select / deselect.
	 * @param {jQuery} $table table selector.
	 * @param {jQuery} $selected count element selector.
	 * @param {String} mode 'init' - init listeners, 'all' - select all, 'nothing' - deselect all, 'invert' - invert selection.
	 */
	const select = ($table, $selected, mode) => {
		const $checkboxes = $table.find('input[type="checkbox"]');

		if (mode === 'init') {
			$checkboxes.change(function () {
				$selected.text($checkboxes.filter(':checked').length);
			});

			let lastChecked = null;

			/* selection with Shift */
			$checkboxes.click(function (e) {
				if (!lastChecked) {
					lastChecked = e.target;
					return;
				}

				if (e.shiftKey) {
					const start = $checkboxes.index(this);
					const end = $checkboxes.index(lastChecked);
					$checkboxes.slice(Math.min(start, end), Math.max(start, end) + 1).prop('checked', e.target.checked);
				}

				lastChecked = e.target;
			});
		} else if (mode === 'all') {
			$checkboxes.prop('checked', true);
		} else if (mode === 'nothing') {
			$checkboxes.prop('checked', false);
		} else if (mode === 'invert') {
			$checkboxes.each(function(index, elem) {
				$(elem).prop('checked', !$(elem).prop('checked'));
			});
		}
	}

	// public functions
	this.select = select;
}

