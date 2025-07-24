// "use strict";

$$.process.queue.filter = new function () {
	/**
	 * Shows selected filters.
	 * @param {*} id ID of filter selector.
	 * @param {*} selectorForm CSS selector of queue show form.
	 */
	const showSelected = (id, selectorForm) => {
		const selectedFilters = {};
		let selectedFilterIds = "";

		$$.ui.comboInputs($(document.getElementById(id))).each(function () {
			if (this.checked) {
				selectedFilters[$(this).attr('id')] = 1;

				if (selectedFilterIds.length)
					selectedFilterIds += ",";

				selectedFilterIds += $(this).attr('value');
			}
		});

		const $form = $(selectorForm);

		$form[0].selectedFilters.value = selectedFilterIds;
		$form.find('.filter-item').each(function () {
			$(this).toggle(selectedFilters[$(this).attr('id')] !== undefined);
		});

		clearHiddenFilters($form);
		processQueueMarkFilledFilters($form);
	}

	const clearHiddenFilters = ($form) => {
		$$.ui.comboInputs($form.find('.filtersSelect')).each( function() {
			var id = $(this).attr('id');
			var $filterItem = $('#' + id + '.filter-item');

			// очистка скрытых фильтров
			if (!this.checked &&
				$filterItem.find( '.dontResetOnHideFilter' ).length == 0 ) {
				/* пока простейший сброс хотя бы текстовых фильтров и фильтров по дате, с combo_check и т.п. ещё разобраться */
				$filterItem.find('input[type=text]').val('');
				$filterItem.find('input[type=hidden]').val('');
			}
		});
	}

	const savedSetId = (queueId) => {
		const result = $('#processQueueFilter > #' + queueId).find( '#savedFilters:visible .btn-blue' ).attr( 'id' );
		return result ? result : 0;
	}

	// public functions
	this.showSelected = showSelected;
	this.savedSetId = savedSetId;

	// $$.process.queue.filter.param
	this.param = new function () {
		const addressApply = (uiid, title, cityFilterId, streetFilterId, quarterFilterId, houseFilterId, flatFilterId, buttonId) => {
			$(document.getElementById(uiid)).hide();

			[$(document.getElementById(cityFilterId)).val(), $(document.getElementById(streetFilterId)).val(), $(document.getElementById(quarterFilterId)).val(),
				$(document.getElementById(houseFilterId)).val(), $(document.getElementById(flatFilterId)).val()].forEach(function (token) {
					if (token)
						title += ', ' + token;
				})
			$(document.getElementById(buttonId)).val(title);
		}

		// public functions
		this.addressApply = addressApply;
	}
}
