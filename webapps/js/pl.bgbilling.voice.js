/*
 * Plugin BGBilling module Voice
 */
"use strict";

$$.bgbilling.voice = new function () {
	const accountTypeChanged = (id, types) => {
		const input = document.getElementById(id).querySelector("input[type=hidden]");

		const type = types.find((item) => item.id == input.value);
		if (type) {
			const fields = [
				'needDevice',
				'needLogin',
				'checkPassword',
				'needNumber'
			];

			const form = input.form;

			for (const key of fields) {
				$(form.querySelector("#" + key)).toggle(type[key]);
			};

			$(form.querySelector('table.data')).show();
		}
	}

	const devices = (a) => {
		const form = a.closest('form');

		const request = "/user/plugin/bgbilling/proto/voice.do?method=devices" +
			"&billingId=" + form.billingId.value +
			"&moduleId=" + form.moduleId.value +
			"&deviceId=" + form.deviceId.value;

		const $device = $(form.querySelector('#needDevice'));
		const $editor = $(form.querySelector('#deviceEditor'));

		$$.ajax.load(request, $editor.find('td')).done(() => {
			$device.toggle();
			$editor.toggle();
		});
	}

	const setDevice = (form) => {
		form.deviceId.value = form.deviceIdSelect.value;
		form.deviceId.parentNode.querySelector('a').textContent = form.deviceTitleSelect.value;
	}

	const categories = (button) => {
		const form = button.closest('form');

		const request = "/user/plugin/bgbilling/proto/voice.do?method=categories" +
			"&billingId=" + form.billingId.value +
			"&moduleId=" + form.moduleId.value;

		const $phone = $(form.querySelector('#needPhone'));
		const $editor = $(form.querySelector('#phoneEditor'));

		$$.ajax.load(request, $editor.find('td'), { control: button }).done(() => {
			$phone.toggle();
			$editor.toggle();
		});
	}

	const category = (input) => {
		const form = input.closest('form');

		const request = "/user/plugin/bgbilling/proto/voice.do?method=category" +
			"&billingId=" + form.billingId.value +
			"&moduleId=" + form.moduleId.value +
			"&categoryId=" + form.categoryId.value;

		$$.ajax.load(request, form.querySelector('#category'));
	}

	// public functions
	this.accountTypeChanged = accountTypeChanged;
	this.devices = devices;
	this.setDevice = setDevice;
	this.categories = categories;
	this.category = category;
}
