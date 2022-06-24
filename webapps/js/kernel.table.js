// "use strict";

$$.table = new function() {
	/**
	 * Manages checkboxes in table: select / deselect.
	 * @param {jQuery} $table table selector.
	 * @param {jQuery} $selectedCounter count element selector.
	 * @param {String} mode 'init' - init listeners, 'all' - select all, 'nothing' - deselect all, 'invert' - invert selection.
	 */
	const select = ($table, $selectedCounter, mode) => {
		const $checkboxes = $table.find('input[type="checkbox"]');

		const updateSelectedCounter = () => $selectedCounter.text($checkboxes.filter(':checked').length);

		if (mode === 'init') {
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
				updateSelectedCounter();
			});

			updateSelectedCounter();
		} else if (mode === 'all') {
			$checkboxes.prop('checked', true);
			updateSelectedCounter();
		} else if (mode === 'nothing') {
			$checkboxes.prop('checked', false);
			updateSelectedCounter();
		} else if (mode === 'invert') {
			$checkboxes.each(function(index, elem) {
				$(elem).prop('checked', !$(elem).prop('checked'));
			});
			updateSelectedCounter();
		}
	}

	// public functions
	this.select = select;
}

