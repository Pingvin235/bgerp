/*
 * Plugin Report
 */
"use strict";

$$.report = new function() {
	const debug = $$.debug("report");

	/**
	 * Show report or chart area.
	 * @param {*} area 
	 */
	const show = (area) => {
		
	}

	/* const moreMenuInit = ($button, $ul) => {
		$ul.insertBefore($ul.closest(''))
		$$.ui.menuInit($button, $ul, "right");
	} */

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
		
		/* const $pageControl = $form.find(".page").parent();
		$pageControl.hide(); */

		$form.find(">.more").toggle();
	}

	const less = (button) => {
		const $form = $(button).closest("form");
		$form.find(">.more").toggle();

		// move editor to a hidden area
		const $editor = $form.find(">.more-editor-container>.more-editor");
		const $editorA = $form.find("li#" + $editor.attr("id") + ">a");
		if ($editor.length && $editorA.length) {
			$editor
				.hide()
				.appendTo($editorA);
		}
	}

	const chart = (button, type) => {
		const $form = $(button).closest("form");

		const url = $$.ajax.formUrl($form) + "&page.pageIndex=-1";
		$$.ajax.post(url).done((response) => {
			console.log(response);
		});

		const $data = $(button).closest(".report").find(">.data");

		$data.empty();

		var w = 500;
		var h = 100;

		var barPadding = 1;

		var dataset = [5, 10, 13, 19, 21, 25, 22, 18, 15, 13,
			11, 12, 15, 20, 18, 17, 16, 18, 23, 25
		];

		//Create SVG element
		var svg = d3.select($data[0])
			.append("svg")
			.attr("width", w)
			.attr("height", h);

		//Add bars to the generated svg element
		svg.selectAll("rect")
			.data(dataset)
			.enter()
			.append("rect")
			.attr("x", function (d, i) {
				return i * (w / dataset.length);
			})
			.attr("y", function (d) {
				return h - (d * 4);
			})

			//setting dynamic width for each bar with bar padding acting as the space between bars
			.attr("width", w / dataset.length - barPadding)

			//multiplying data value with 4 to increase the height of the bars 
			.attr("height", function (d) {
				return d * 4;
			})

			//apply dynamic colors
			.attr("fill", function (d) {
				return "rgb(5, 2, " + (d * 10) + ")";
			});

		//set the label
		svg.selectAll("text")
			.data(dataset)
			.enter()
			.append("text")
			.text(function (d) {
				return d;
			})
			.attr("text-anchor", "middle")
			.attr("x", function (d, i) {
				return i * (w / dataset.length) + (w / dataset.length - barPadding) / 2;
			})
			.attr("y", function (d) {
				return h - (d * 4) + 14;
			})
			/* .attr("font-family", "sans-serif")
			.attr("font-size", "11px") */
			.attr("fill", "white");

		/* const $dataTable = $report.find(">table.data");
		$dataTable.hide(); */

		// https://github.com/d3/d3/wiki/Tutorials

		// http://bl.ocks.org/anonymous/64f9c70bd629810bee67392f6224d414 Horizontal Bar Chart with Labels and Tooltips
		// https://jsfiddle.net/nordible/pmyu26ss/ Bar chart with label using D3.js
		/* d3.select($area)
			.selectAll("p")
			.data([4, 8, 15, 16, 23, 42])
			.enter().append("p")
				.text(function(d) { return "I’m number " + d + "!"; }); */
	}

	// public functions
	//this.moreMenuInit = moreMenuInit;
	this.more = more;
	this.less = less;
	this.chart = chart;
}
