/*
 * Plugin Blow
 */
"use strict";

$$.blow = new function() {
	const debug = $$.debug("blow");

	const ATTR_BG_ID = "bg-id";
	const ATTR_BG_PARENT_ID = "bg-parent-id";
	const ATTR_BG_TYPE_ID = "bg-type-id";

	const CLASS_BORDER_TOP = "group-border-t";
	const CLASS_BORDER_LEFT = "group-border-l";
	const CLASS_BORDER_BOTTOM = "group-border-b";
	const CLASS_BORDER_RIGHT = "group-border-r";

	const CLASS_SELECTED = "selected";
	const CLASS_SELECTED_FILTER = "selected";

	const selectItem = ($td, $cells) => {
		const itemId = $td.attr(ATTR_BG_ID);
		const parentId = $td.attr(ATTR_BG_PARENT_ID);

		if (itemId) {
			$td.addClass(CLASS_SELECTED);
			$cells.filter("[" + ATTR_BG_PARENT_ID + "=" + itemId + "]").each(function () {
				selectItem($(this), $cells);
			});
		}
	}

	const initTable = ($table, $menu) => {
		const $cells = $table.find("> tbody > tr:gt(0) > td");

		$cells.each(function () {
			const $td = $(this);

			$td.mouseover(function () {
				debug("mouseover", $td);
				selectItem($td, $cells);
			});

			$td.mouseleave(function () {
				debug("mouseleave", $td);
				$cells.removeClass(CLASS_SELECTED);
			});

			if ($menu)
				$$.blow.drag.init($td);

			groupBorder($cells, $td);
		});

		if ($menu)
			initRcMenu($table, $menu);
	}

	const initRcMenu = ($table, $menu) => {
		const menu = $menu.menu();

		// context of process for popup menu show
		let $td;
		let $tdCut;

		$menu.find("#create").on("click", () => {
			let url = null;

			if ($td.attr(ATTR_BG_PARENT_ID) > 0)
				url = "/user/process/link/process.do?action=linkProcessCreate&objectType=processMade&typeId=" + $td.attr(ATTR_BG_TYPE_ID) + "&id=" + $td.attr(ATTR_BG_PARENT_ID);
			else
				url = "/user/process.do?action=processCreate&typeId=" + $td.attr(ATTR_BG_TYPE_ID);

			$$.ajax
				.post(url)
				.done((resp) => {
					$$.process.open(resp.data.process.id);
				});
		});

		const $cut = $menu.find("#cut");
		$cut.on("click", () => {
			$table.find("td").removeClass("cut");
			$td.addClass("cut");
			$paste.show();
			$tdCut = $td;
		});

		const $paste = $menu.find("#paste");
		$paste.on("click", () => {
			move($tdCut, $td);
		});

		const $merge = $menu.find("#merge");
		$merge.on("click", () => {
			merge($tdCut, $td);
		});

		const $free = $menu.find("#free");
		$free.on("click", () => {
			move($td, $());
		});

		$table.on("contextmenu", "td", function (e) {
			debug("contextmenu", e);
			$td = $(e.target);
			if ($td.attr(ATTR_BG_TYPE_ID)) {
				menu.show().position({
					my: "left top",
					at: "left bottom",
					of: e
				});

				$(document).one("click", () => {
					menu.hide();
				});

				$cut.toggle($td.attr(ATTR_BG_PARENT_ID) !== '');
				$paste.toggle(!!$tdCut && $tdCut.attr(ATTR_BG_ID) > 0);
				$free.toggle($td.attr(ATTR_BG_PARENT_ID) > 0);
				$merge.toggle(!!$tdCut && $tdCut.attr(ATTR_BG_ID) > 0);
			}
			return false;
		});
	}

	const groupBorder = ($cells, $td) => {
		const itemId = $td.attr(ATTR_BG_ID);
		const $children = $cells.filter("[" + ATTR_BG_PARENT_ID + "=" + itemId + "]");

		if (itemId == 0)
			$td.addClass(CLASS_BORDER_TOP);
		else if (itemId > 0) {
			// group of cells
			if ($children.length > 0) {
				const $root = $td;

				debug($root, $children);

				$.merge($root, $children).each((i, td) => {
					$td = $(td);

					const $tr = $td.closest("tr");

					if ($tr.is(":nth-child(2)") || $td[0] === $root[0])
						$td.addClass(CLASS_BORDER_TOP);

					if ($tr.is(":last-child"))
						$td.addClass(CLASS_BORDER_BOTTOM);

					if ($td.is(":first-child") || $td.prev().attr(ATTR_BG_PARENT_ID) != itemId)
						$td.addClass(CLASS_BORDER_LEFT);

					if ($td.is(":last-child") || $td.next().attr(ATTR_BG_PARENT_ID) != itemId)
						$td.addClass(CLASS_BORDER_RIGHT);
				});
			}
			// standalone cell
			else if ($td.attr(ATTR_BG_PARENT_ID) == 0) {
				const $tr = $td.closest("tr");
				if ($tr.index() > 1 && getCellInSameColumn($tr.prev(), $td).attr(ATTR_BG_PARENT_ID) > 0)
					$td.addClass(CLASS_BORDER_TOP);
			}
		}
		// empty cell
		else if (!itemId) {
			if (!$td.is(":first-child") && isGroupMember($td.prev()))
				$td.addClass(CLASS_BORDER_LEFT);

			if (!$td.is(":last-child") && isGroupMember($td.next()))
				$td.addClass(CLASS_BORDER_RIGHT);

			const $tr = $td.closest("tr");

			if (!$tr.is(":nth-child(2)") && neighborCellNeedBorder($tr.prev(), $td))
				$td.addClass(CLASS_BORDER_TOP);

			if (!$tr.is(":last-child") && neighborCellNeedBorder($tr.next(), $td))
				$td.addClass(CLASS_BORDER_BOTTOM);
		}
	}

	const neighborCellNeedBorder = ($tr, $td) => {
		return isGroupMember(getCellInSameColumn($tr, $td));
	}

	const getCellInSameColumn = ($tr, $td) => {
		const index = $td.index();
		const $children = $tr.children();
		debug("getCellInSameColumn", $tr, $children);
		return  $children.length === 1 ?  $children.first() : $($children.get(index));
	}

	const isGroupMember = ($td) => {
		return $td.attr(ATTR_BG_PARENT_ID) === "" || $td.attr(ATTR_BG_PARENT_ID) > 0;
	}

	const toggleFilterHighlight = ($table, $button) => {
		const $cells = $table.find("td.filter-" + $button.attr(ATTR_BG_ID));
		if ($button.toggleClass(CLASS_SELECTED_FILTER).hasClass(CLASS_SELECTED_FILTER)) {
			$cells.css("background-color", $button.css("color"));
			if ($cells.length > 0)
				$cells[0].scrollIntoView();
		}
		else
			$cells.css("background-color", "");

	}

	const move = ($td, $tdTo) => {
		const targetProcessId = $tdTo.attr(ATTR_BG_ID);
		const targetParentProcessId = $tdTo.attr(ATTR_BG_PARENT_ID);

		$$.ajax
			.post("/user/plugin/blow/board.do?action=move&processId=" + $td.attr(ATTR_BG_ID) + "&fromParentProcessId=" + $td.attr(ATTR_BG_PARENT_ID) +
					"&parentProcessId=" + (targetParentProcessId > 0 ? targetParentProcessId : targetProcessId))
			.done(() => $$.shell.contentLoad("/user/blow/board"));
	}

	const merge = ($td, $tdTo) => {
		const targetProcessId = $tdTo.attr(ATTR_BG_ID);
		$$.ajax
			.post("/user/process.do?action=processMerge&id=" + $td.attr(ATTR_BG_ID) + "&processId=" + targetProcessId)
			.done(() => $$.shell.contentLoad("/user/blow/board"));
	}

	const search = (form) => {
		$$.ajax.post(form, { html: true }).done((result) => {
			$(form).append(result);
			const filterPos = $(form.filter).position();
			const $drop = $(form).find(".drop");

			$$.ui.dropShow($drop);
			$(document).one("click", function () {
				$drop.parent().remove();
			});
		});
		return false;
	}

	// drag & drop
	this.drag = new function () {
		let $tdDrag;
		let $cells;

		const dragStart = function (e) {
			this.style.opacity = '0.4';
			$tdDrag = $(this);
			$cells = $(this).closest("table").find("td");
		}

		const dragEnd = function (e) {
			this.style.opacity = '';
			$cells.removeClass(CLASS_SELECTED);
		}

		const dragOver = (e) => {
			debug("drag over", e);

			$cells.removeClass(CLASS_SELECTED);

			const $td = $(e.target);
			if ($td.prop("tagName") === "TD") {
				const targetProcessId = $td.attr(ATTR_BG_ID);
				const targetParentProcessId = $td.attr(ATTR_BG_PARENT_ID);

				let $root = null;
				if (targetParentProcessId > 0)
					$root = $cells.filter("td[" + ATTR_BG_ID + "=" + targetParentProcessId + "]");
				else if (targetParentProcessId === "")
					$root = $td;

				if ($root)
					selectItem($root, $cells);
			}
			e.preventDefault();
		}

		const dragDrop = function (e) {
			debug("drag drop", e);

			e.preventDefault();

			move($tdDrag, $(this));

			return false;
		}

		const initDD = ($td) => {
			$td.on('dragstart', dragStart);
			$td.on('dragend', dragEnd);
			$td.on('dragover', dragOver);
			$td.on('drop', dragDrop);
		}

		this.init = initDD;
	}

	// public functions
	this.initTable = initTable;
	this.toggleFilterHighlight = toggleFilterHighlight;
	this.search = search;
}
