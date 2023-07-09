/*
 * Plugin Invoice
 */
"use strict";

$$.invoice = new function() {
	const debug = $$.debug("invoice");

	/**
	 * Adds invoice position.
	 * @param {*} button triggering button.
	 */
	const addPosition = (button) => {
		const $tr = $(button).closest("tr");

		const $li = $tr.find("ul li[selected]");
		if (!$li.length)
			return;

		$tr.before(
			"<tr>\
				<td><input type='text' name='pos_id' value='" + $li.attr("value") + "' disabled='true' size='10'/></td>\
				<td><input type='text' name='pos_title' value='" + $li.text() + "' style='width: 100%;'/></td>\
				<td><input type='text' name='pos_quantity' value='" + $tr.find("input[name='add_quantity']").val() + "' size='2'/></td>\
				<td><input type='text' name='pos_unit' value='" + $tr.find("input[name='add_unit']").val() + "' size='2'/></td>\
				<td><input type='text' name='pos_amount' value='" + $tr.find("input[name='add_amount']").val() + "' onkeydown='return isNumberKey(event)' size='5'/></td>\
				<td><button onclick=\"$(this).closest('tr').remove()\" class='btn-white icon'><i class='ti-trash'></i></button></td>\
			</tr>");
	}

	/**
	 * Request payment date and make invoice paid.
	 * @param {*} hiddenId hidden input ID.
	 * @param {*} url URL for AJAX call.
	 * @return promise.
	 */
	const paid = (hiddenId, url) => {
		const dfd = $.Deferred();

		$(document.getElementById(hiddenId)).datepicker("dialog", "", (unused, inst) => {
			const date = new Date(inst.selectedYear, inst.selectedMonth, inst.selectedDay);
			$$.ajax
				.post(url + "&date=" + date.toLocaleDateString('de-DE'))
				.done(() => dfd.resolve());
		});

		return dfd.promise();
	}

	// public functions
	this.addPosition = addPosition;
	this.paid = paid;
}
