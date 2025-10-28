/*
 * Plugin Report
 */
"use strict";

$$.report = new function() {
	const debug = $$.debug("report");

	const more = (a) => {
		const $li = $(a).closest("li");
		const $form = $li.closest("form");

		// move a hidden editor to editor area
		const $editor = $li.find(".more-editor");
		const $editorContainer = $form.find(">.more-editor-container");
		if ($editor.length && $editorContainer.length) {
			$editor
				.attr("id", $li.attr("id"))
				.appendTo($editorContainer)
				.show();
		}

		$form.find(">.more").toggle();
	}

	const less = (button) => {
		const $form = $(button).closest("form");

		$form.find(">.more").toggle();

		const $editor = $form.find(">.more-editor-container>.more-editor");
		const $editorA = $form.find("li#" + $editor.attr("id") + ">a");
		if ($editor.length && $editorA.length) {
			// hide editor and move out
			$editor.hide().appendTo($editorA);
		}

		$form.find("button.out").click();
	}

	/**
	 * Generate a chart.
	 * @param {jQuery|Element} button - element, placed in report sending form.
	 * @param {*} index - 1 based index of chart.
	 */
	const chart = (button, index) => {
		const $form = $(button).closest("form");

		const $data = $(button).closest(".report").find(">.data");
		// TODO: configurable
		$data.height(600);
		// TODO: Show progress

		// To prevent error: "There is a chart instance already initialized on the dom."
		let chart = $data.data("echart");
		if (chart)
			chart.dispose();

		$data.empty();

		const url = $$.ajax.formUrl($form) + "&page.pageIndex=-1&chartIndex=" + index;
		$$.ajax.post(url).done((result) => {
			chart = echarts.init($data[0]);
			chart.setOption(result.data.chart);
			$data.data("echart", chart);
		});
	}

	// public functions
	this.more = more;
	this.less = less;
	this.chart = chart;
}
