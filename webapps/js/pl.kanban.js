/*
 * Plugin Kanban
 */
"use strict";

$$.kanban = new function() {
	const debug = $$.debug("kanban");

	const ATTR_PROCESS_ID = "bg-process-id";
	const ATTR_ALLOWED = "bg-allowed";
	const ATTR_STATUS_ID = "bg-status-id";

	const CLASS_DRAG_OVER = "kanban-drag-over";

	const updateCount = ($column) => {
		$column.find("> .kanban-column-head .kanban-count").text("[" + $column.find(".kanban-card").length + "]");
	}

	const isAllowed = ($card, statusId) => {
		const allowed = ($card.attr(ATTR_ALLOWED) || "").split(",").map((s) => s.trim());
		return allowed.indexOf(String(statusId)) >= 0;
	}

	// moves the card DOM node optimistically, reverting on a failed status change
	const move = ($card, $column) => {
		const processId = $card.attr(ATTR_PROCESS_ID);
		const statusId = $column.attr(ATTR_STATUS_ID);

		const $fromColumn = $card.closest(".kanban-column");
		if ($fromColumn.attr(ATTR_STATUS_ID) === statusId)
			return;

		const $fromBody = $card.parent();

		$column.find("> .kanban-column-body").append($card);
		updateCount($fromColumn);
		updateCount($column);

		debug("move", processId, "->", statusId);

		// reuses the existing process status-change endpoint as-is: permission check,
		// transition matrix, required-parameter validation, history and events all apply
		// exactly as they would from the standard status dropdown
		$$.ajax
			.post("/user/process.do?method=processStatusUpdate&id=" + processId + "&statusId=" + statusId)
			.fail(() => {
				$fromBody.append($card);
				updateCount($fromColumn);
				updateCount($column);
			});
	}

	const openPreview = (processId) => {
		let $dialog = $("#kanban-preview-dialog");
		if ($dialog.length === 0)
			$dialog = $("<div id=\"kanban-preview-dialog\"></div>").appendTo(document.body);

		const $title = $$.shell.$title();
		const savedTitle = $title.html();
		const $state = $$.shell.$state();
		const $savedState = $state.children().detach();

		$$.ajax.load("/user/process.do?id=" + processId, $dialog).done(() => {
			$dialog.dialog({
				modal: true,
				width: Math.min($(window).width() * 0.9, 1200),
				height: Math.round($(window).height() * 0.85),
				close: () => {
					$dialog.empty();
					$title.html(savedTitle);
					$state.html("");
					$state.append($savedState);
				}
			});
		});
	}

	const initBoard = ($board) => {
		let $cardDrag = null;

		$board.find(".kanban-card[draggable=true]").each(function () {
			const $card = $(this);

			$card.on("dragstart", function () {
				$cardDrag = $card;
				this.style.opacity = "0.4";
			});

			$card.on("dragend", function () {
				this.style.opacity = "";
				$board.find("." + CLASS_DRAG_OVER).removeClass(CLASS_DRAG_OVER);
				$cardDrag = null;
			});
		});

		$board.find(".kanban-column").each(function () {
			const $column = $(this);

			$column.on("dragover", (e) => {
				if ($cardDrag && isAllowed($cardDrag, $column.attr(ATTR_STATUS_ID))) {
					e.preventDefault();
					$column.addClass(CLASS_DRAG_OVER);
				}
			});

			$column.on("dragleave", () => $column.removeClass(CLASS_DRAG_OVER));

			$column.on("drop", (e) => {
				e.preventDefault();
				$column.removeClass(CLASS_DRAG_OVER);
				// re-checked here, not just relied on from dragover: drop can fire without a
				// permitting dragover outside of a genuine mouse-driven drag session
				if ($cardDrag && isAllowed($cardDrag, $column.attr(ATTR_STATUS_ID)))
					move($cardDrag, $column);
			});
		});
	}

	// public functions
	this.initBoard = initBoard;
	this.openPreview = openPreview;
}
