/*
 * Plugin Group Plan
 */
'use strict';

$$.grpl = new function() {
	const debug = $$.debug('grpl');

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
			if (!$td.length)
				return;

			const $tr = $td.closest('tr');

			// non-header cells
			if ($td.attr('bg-column-id') && !$td.find('.grpl-board-process').length) {
				$$.ajax
					.load(requestURI + '?' + $$.ajax.requestParamsToUrl({
						method: 'menu',
						id: boardId,
						date: $tr.attr('bg-date'),
						columnId: $td.attr('bg-column-id'),
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

			// return false;
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

	// public functions
	this.menuInit = menuInit;
	this.menuClick = menuClick;
}
