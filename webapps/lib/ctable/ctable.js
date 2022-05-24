/**
 *  Взято здесь:
 *  http://stackoverflow.com/questions/20479148/big-table-navigation-how-to-control-the-headers
 *  http://jsfiddle.net/rCuPf/7/
 *
 *  Код поправлен до работоспособного состояния, при этом урезался непонятный функционал с поддержкой сложных заголовков c colspan,
 *  но возможно частично он остался.
 *
 *  FIXME: Фактически код завязан на ОДНУ только закрепляемую строку. Если понадобится другое - прроверить.
 */
$.fn.cTable = function (o) {
	const $container = o.container;

	$container.addClass('relativeContainer');

	const debug = false;

	const time = window.performance.now();
	const timeStart = time;

	if (debug) {
		console.log("cTable", this);
	}

	// расчёт высоты и ширины ячейки, закреплённой в левом верхнем углу
	const tr1c1Height = this.find(">tbody>tr:first").outerHeight(true);
	let tr1c1Width = 0;
	for (let i = 0; i < o.fCols; i++) {
		tr1c1Width += this.find(">tbody>tr:first>td:eq(" + i + ")").outerWidth(true);
	}

	//Update below template as how you have it in orig table
	const origTableTmpl = this[0].cloneNode(false).outerHTML;

	if (debug) {
		console.log("ctable03", window.performance.now() - time);
		time = window.performance.now();
	}

	// левая верхняя таблица из одной ячейки - перенос в неё закреплённых столбцов
	const $tr1c1 = $(origTableTmpl).append("<tbody/>");
	const $row1 = this.find('tr').slice(0, o.fRows);
	const $rowClone = $($row1[0].cloneNode(false));
	$row1.find('td').slice(0, o.fCols).appendTo($rowClone);
	$tr1c1.append($rowClone);

	$tr1c1.wrap('<div class="fixedTB"/>');
	$tr1c1.parent().prependTo($container);

	$tr1c1.height(tr1c1Height);

	if (debug) {
		console.log("ctable05", window.performance.now() - time);
		time = window.performance.now();
	}

	// create a table with just c1
	// таблица, закреплённая слева
	const $c1 = $(origTableTmpl).append("<tbody/>");
	for (let i = o.fRows; i < this[0].rows.length; i++) {
		const row = this[0].rows[i];

		const $rowClone = $(row.cloneNode(false));
		$(row).find(">td").slice(0, o.fCols).appendTo($rowClone);

		$c1.append($rowClone);
	}

	if (debug) {
		console.log("ctable0", window.performance.now() - time);
		time = window.performance.now();
	}

	const $fixedTB = $tr1c1.closest('.fixedTB');

	$c1.wrap('<div class="leftSBWrapper" />')
	$c1.parent().wrap('<div class="leftContainer" />');
	$c1.closest('.leftContainer').insertAfter($fixedTB);

	const $leftSBWrapper = $c1.closest('.leftSBWrapper');
	const $leftContainer = $c1.closest('.leftContainer');

	// первая строка основной таблицы - выделение
	//create table with just row 1 without col 1
	const $r1 = $(origTableTmpl).append("<tbody/>");
	this.find('>tbody>tr').slice(0, o.fRows).appendTo($r1);

	if (debug) {
		console.log("ctable1", window.performance.now() - time);
		time = window.performance.now();
	}

	$r1.wrap('<div class="topSBWrapper" />');
	$r1.parent().wrap('<div class="rightContainer" />')

	const $topSBWrapper = $r1.closest('.topSBWrapper');
	const $rightContainer = $r1.closest('.rightContainer').appendTo($container);

	// остаток основной таблицы
	this.wrap('<div class="SBWrapper"/>')
	this.parent().appendTo($rightContainer);

	if (debug) {
		console.log("ctable2", window.performance.now() - time);
		time = window.performance.now();
	}

	const $SBWrapper = this.closest('.SBWrapper');

	if (debug) {
		console.log("ctable5", window.performance.now() - time);
		time = window.performance.now();
	}

	$fixedTB.css('width', tr1c1Width);
	$leftContainer.css({ 'top': tr1c1Height, 'width': tr1c1Width });
	$rightContainer.css('left', tr1c1Width);

	if (debug) {
		console.log("ctable6", window.performance.now() - time);
		time = window.performance.now();
	}

	// асинхронный вызов, чтобы таблица была видна быстрее
	setTimeout(function () {
		const setSize = function () {
			if (debug) {
				console.log("setSize", $container);
			}

			if ($container.is(":visible")) {
				const time = window.performance.now();

				const rtw = $container.width() - tr1c1Width;
				$rightContainer.css({ 'max-width': rtw, 'height': $container.height() });
				$SBWrapper.css({ 'height': $container.height() - tr1c1Height });

				if (debug) {
					console.log("setSize", $container, "time:", window.performance.now() - time);
				}
			}
		};

		setSize();

		$SBWrapper.scroll(function () {
			if (debug) {
				console.log("cTable scroll");
			}

			$leftSBWrapper.css('top', ($(this).scrollTop() * -1));
			$topSBWrapper.css('left', ($(this).scrollLeft() * -1));
		});

		$(window).resize(function () {
			setSize();
		});
	}, 20);

	if (debug) {
		console.log("fullTime", window.performance.now() - timeStart);
	}
}