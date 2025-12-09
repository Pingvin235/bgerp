/*
 * Plugin Invoice
 */
"use strict";

$$.invoice = new function() {
	const debug = $$.debug("invoice");

	/**
	 * Add invoice position
	 * @param {*} button the triggering button
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
	 * Request payment date and make invoice paid
	 * @param {HTMLElement} element any HTML element
	 * @param {*} url URL for AJAX call
	 * @return promise
	 */
	const paid = (element, url) => {
		const dfd = $.Deferred();

		$(element).datepicker("dialog", "", (value) => {
			$$.ajax
				.post(url + "&date=" + value)
				.done(() => dfd.resolve());
		}, { dateFormat: "dd.mm.yy" });

		return dfd.promise();
	}

	// public functions
	this.addPosition = addPosition;
	this.paid = paid;
}
