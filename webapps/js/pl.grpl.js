/*
 * Plugin Group Plan
 */
'use strict';

$$.grpl = new function() {
	const debug = $$.debug('grpl');

	const ATTR_COLUMN_ID = 'bg-column-id';
	const ATTR_DATE = 'bg-date';

	const menuInit = (tableUiid, menuUiid, boardId, requestURI, returnUrl) => {
		const $table = $(document.getElementById(tableUiid));
		const $menu = $(document.getElementById(menuUiid)).menu();

		$table.on('click', (e) => {
			debug('click', e);

			// process link
			if (e.target.nodeName === 'A')
				return;

			const td = () => {
				const target = e.target;
				if (target.nodeName === 'TD')
					return target;
				if (target.nodeName === 'DIV' && target.classList.contains('grpl-board-group'))
					return $(target).closest('td');
			}

			const $td = $(td());
			if (!$td.length || $td.hasClass('grpl-past'))
				return;

			const columnId = $td.attr(ATTR_COLUMN_ID);
			const date = $td.closest('tr').attr(ATTR_DATE);

			// non-header cells
			if (columnId && date && !$td.find('.grpl-board-process').length) {
				$$.ajax
					.load(requestURI + '?' + $$.ajax.requestParamsToUrl({
						method: 'menu',
						id: boardId,
						columnId: columnId,
						date: date,
						returnUrl: returnUrl,
						returnChildUiid: tableUiid,
					}), $menu)
					.done(() => {
						$menu.menu("refresh");

						$menu.show().position({
							my: 'left top',
							at: 'left bottom',
							of: e
						});

						$(document).one('click', () => {
							$menu.hide();
						});
					});
			}
		});
	}

	const menuClick = (requestURI, boardId, date, columnId, groupId, returnUrl, returnChildUiid) => {
		$$.ajax
			.post(requestURI + '?' + $$.ajax.requestParamsToUrl({
				method: 'cellGroup',
				id: boardId,
				date: date,
				columnId: columnId,
				groupId: groupId
			}))
			.done(() => {
				$$.ajax.load(returnUrl, $(document.getElementById(returnChildUiid).parentElement));
			});
	}

	const ATTR_DURATION = 'bg-duration';
	const ATTR_PROCESS_ID = 'bg-process-id';

	const CLASS_DROP_ALLOWED = 'grpl-board-drop-allowed';

	const dragInit = (tableUiid, dialogUiid, boardId, requestURI, returnUrl) => {
		const $table = $(document.getElementById(tableUiid));

		let dragId;

		$table.find('.grpl-board-process')
			.attr('draggable', true)
			.on('dragstart', function (event) {
				event = event.originalEvent;

				debug('drag', this, event);

				dragId = event.target.id;
			});

		const removeAllowed = function () {
			$(this).removeClass(CLASS_DROP_ALLOWED);
		};

		$table.find('.grpl-board-process-placement')
			.on('dragover', function (event) {
				event = event.originalEvent;
				const el = document.getElementById(dragId);

				debug('dragover', this, event, el);

				const $el = $(el);
				const $target = $(this);

				if (parseInt($target.attr(ATTR_DURATION)) >= parseInt($el.attr(ATTR_DURATION)) &&
					$target.closest('td').attr(ATTR_COLUMN_ID) === $el.closest('td').attr(ATTR_COLUMN_ID)) {
					$target.addClass(CLASS_DROP_ALLOWED);
					event.preventDefault();
				}
			})
			.on('dragleave', removeAllowed)
			.on('mouseleave', removeAllowed)
			.on('drop', function (event) {
				event = event.originalEvent;
				const el = document.getElementById(dragId);

				debug('drop', this, event, el);

				const $target = $(event.target);

				if ($target.hasClass(CLASS_DROP_ALLOWED)) {
					const $targetTd = $target.closest('td');
					const dialog = document.getElementById(dialogUiid);

					$$.ajax
						.load(requestURI + '?' + $$.ajax.requestParamsToUrl({
							method: 'dialog',
							id: boardId,
							columnId: $targetTd.attr(ATTR_COLUMN_ID),
							processId: $(el).attr(ATTR_PROCESS_ID),
							date: $targetTd.closest('tr').attr(ATTR_DATE),
							time: $target.attr('bg-time'),
							duration: $target.attr(ATTR_DURATION),
							returnUrl: returnUrl
						}), dialog)
						.done(() => {
							const $dialog = $(dialog);
							$dialog.find('.ok').click(function () {
								$$.ajax.post(this).done(() => {
									$$.ajax.load(returnUrl, document.getElementById(tableUiid).parentElement);
									$dialog.dialog('close');
								})
							})
							$dialog.dialog().dialog('open');
						});
				}
			});
	}

	// public functions
	this.menuInit = menuInit;
	this.menuClick = menuClick;
	this.dragInit = dragInit;
}
