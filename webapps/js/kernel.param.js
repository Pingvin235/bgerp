// "use strict";

$$.param = new function () {
	const debug = $$.debug("param");

	const dirChanged = (form, item, hidden) => {
		form.method.value = $(item).attr("action");
		form.directoryId.value = hidden.value;
		$$.ajax.loadContent(form);
	}

	/**
	 * Handles changes of type in parameter editor. Depending of the chosen type is enabled list values area, and adjusted configuration editor height.
	 * @param {*} formId ID of the editor form.
	 * @param {*} heightSampleId ID of a sample DOM element with dynamic height.
	 * @param {*} heightToId ID of the config textarea, which height must be the same as for the sample element.
	 */
	const editorTypeChanged = (formId, heightSampleId, heightToId) => {
		// without the timeout type choosing combo single doesn't close after selection
		setTimeout(() => {
			const $form = $(document.getElementById(formId));

			const type = $form[0].type.value;
			const $listValues = $form.find('#listValues');

			if ($listValues.toggle(type == 'list' || type == 'listcount' || type === 'tree' || type === 'treecount').is(':visible')) {
				$listValues.find('.hint').hide();
				$listValues.find('.' + type).show();
			}

			const height = $(document.getElementById(heightSampleId)).css('height');
			const textarea = document.getElementById(heightToId);
			$(textarea).css('height', height);

			const nextSibling = textarea.nextSibling;
			if (nextSibling && nextSibling.classList && nextSibling.classList.contains('CodeMirror'))
				nextSibling.remove();

			$$.ui.codeMirror(heightToId);
		}, 0);
	}

	// public functions
	this.dirChanged = dirChanged;
	this.editorTypeChanged = editorTypeChanged;

	const addValueCount = ($table, errors, action, multiple) => {
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

		return $$.ajax
			.post(
				"/user/parameter.do?method=" + action + "&multiple=" + multiple + "&itemId=" + itemId +
				"&itemTitle=" + encodeURIComponent(itemTitle) + "&itemCount=" + encodeURIComponent(itemCount),
				{ html: true }
			).done(result => {
				$table.find("tr:last-child").before(result);
			});
	}

	// $$.param.listcount
	this.listcount = new function () {
		/**
		 * Adds a new value in listcount editor.
		 * @param {jQuery} $table values table selector.
		 * @param {Array} errors array with two alerted errors.
		 * @returns
		 */
		const addValue = ($table, errors) => {
			addValueCount($table, errors, "parameterListCountAddValue");
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
					"<td><button type='button' class='btn-white btn-small icon' onclick='$$.param.phone.delValue(this)'><i class='ti-trash'></i></button></td>" +
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

	this.email  = new function () {
		/**
		 * Adds a new empty value to email parameter editor.
		 * @param {HTMLButtonElement} button add button that was clicked.
		 */
		const addValue = (button) => {
			const $tr = $(
				"<tr>" +
				"<td><input type='text' name='address' class='w100p'/></td>" +
				"<td><input type='text' name='name' class='w100p'/></td>" +
				"<td><button type='button' class='btn-white btn-small icon' onclick='$$.param.email.delValue(this)'><i class='ti-trash'></i></button></td>" +
				"</tr>"
			);
			$(button).closest('table').find('tr:last').after($tr);
			$tr.find("input[name='address']").focus();
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

	// $$.param.treecount
	this.treecount = new function () {
		const treeOpen = (a) => {
			const $a = $(a);
			$a.hide();
			$a.closest('td').find('>div').show();
		}

		/**
		 * Handles value tree closing.
		 * @param {*} button the close button.
		 * @param {*} titleInputName the name of the hidden input with the selected item title.
		 */
		const treeClose = (button, titleInputName) => {
			const $button = $(button);
			const $div = $button.closest('div');
			$div.hide();
			const title = $div.find('input[name=' + titleInputName + ']').val();

			const $a = $button.closest('td').find('>a');
			$a.text(title);
			$a.show();
		}

		/**
		 * Adds a new value in treecount editor.
		 * @param {Boolean} multiple multiple values supported.
		 * @param {jQuery} $table values table selector.
		 * @param {Array} errors array with two alerted errors.
		 * @returns AJAX promise.
		 */
		const addValue = (multiple, $table, errors) => {
			return addValueCount($table, errors, "parameterTreeCountAddValue", multiple);
		}

		const editorToggle = (multiple, element) => {
			const table = element.tagName === 'TABLE' ? element : element.closest('table');
			const editorRow = table.rows[table.rows.length - 1];
			$(editorRow).toggle(multiple || table.rows.length === 2);
		}

		// public functions
		this.treeOpen = treeOpen;
		this.treeClose = treeClose;
		this.addValue = addValue;
		this.editorToggle = editorToggle;
	}
}
