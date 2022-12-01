/*
 * Standard UI elements.
 */
"use strict";

$$.ui = new function () {
	// sub namespace selectMult
	this.selectMult = new function () {
		const liUp = function (el) {
			var currentLi = el.parentNode;
			var $prev = $(currentLi).prev();
			if ($prev.length > 0) {
				$(currentLi).insertBefore($prev);
			}
		}

		const liDown = function (el) {
			var currentLi = el.parentNode;
			var $next = $(currentLi).next();
			if ($next.length > 0) {
				$(currentLi).insertAfter($next);
			}
		}

		// public functions
		this.liUp = liUp;
		this.liDown = liDown;
	}

	const comboSingleInit = ($comboDiv, onSelect) => {
		const $drop = $comboDiv.find('ul.drop');
		const $hidden = $comboDiv.find('input[type=hidden]');

		const updateCurrentTitle = function () {
			// по-умолчанию выбирается первый элемент
			let $currentLi = $drop.find('li:not(.filter):first');

			// если указано значение - то ищется оно
			const currentValue = $hidden.val();

			// Наличие значения в hidden не гарантирует наличия соответствующего
			// элемента <li>, поэтому берем если нашли сам элемент;
			const $foundLi = $drop.find("li[value='" + currentValue + "']");
			if ($foundLi.length !== 0) {
				$currentLi = $foundLi;
			}

			let $currentTitle = $currentLi.find('span.title');
			if ($currentTitle.length === 0) {
				$currentTitle = $currentLi;
				$hidden.val($currentLi.attr('value'));
			}

			$drop.find('li').removeAttr('selected');
			$currentLi.attr('selected', '1');

			$comboDiv.find('.text-value').html($currentTitle.html());
		};

		dropOnClick($comboDiv, $drop);

		// событие клика вешается через функцию on, чтобы событие срабатывало, если элемент добавился после  инициации динамически.
		$drop.on('click', 'li:not(.filter)', function () {
			$hidden.val($(this).attr("value"));
			updateCurrentTitle();

			if (onSelect) {
				// to make this equals hidden input
				$hidden[0].onSelect = onSelect;
				$hidden[0].onSelect(this);
			}

			$drop.hide();

			return false;
		});

		updateCurrentTitle();
	}

	/**
	 * Executes filtering in combo-single element.
	 * @param {*} input text input element.
	 */
	const comboSingleFilter = (input) => {
		const $input = $(input);
		const mask = $input.val().toLowerCase();
		$(input.parentNode.parentNode).find('li:gt(0)').each(function () {
			const content = $(this).text().toLowerCase();
			$(this).toggle(content.indexOf(mask) >= 0);
		});
	}

	const comboInputs = ($div) => {
		return $div.find("ul.drop li input");
	}

	const comboCheckUncheck = (object) => {
		const $parent = $(object).closest("ul");
		if ($parent.find("input[type=checkbox]:checked").length === 0)
			$parent.find("input[type=checkbox]").prop("checked", true);
		else
			$parent.find("input[type=checkbox]").prop("checked", false);
	}

	const comboPermTreeCheckInit = ($comboDiv) => {
		const $drop = $comboDiv.find(">.drop");
		$$.ui.dropOnClick($comboDiv, $drop);

		const updateText = function () {
			// timeout allows to process first tree logic of selection children/parents
			setTimeout(function () {
				let checkedCount = 0;

				$drop.find("li input[type=checkbox]").each(function () {
					if (this.checked)
						checkedCount++;
				});

				$comboDiv.find(">.text-value").text("[" + checkedCount + "]");
			});
		};

		updateText();

		// clean cross
		$comboDiv.find(">.icon").click(function (event) {
			$comboDiv.find("li input[type=checkbox]").each(function () {
				this.checked = false;
			});
			updateText();
			event.stopPropagation();
		});

		$drop.find("li input[type=checkbox]").click(function () {
			updateText();
		});
	}

	// close all visible drop-downs
	const dropsHide = () => {
		$(document).find(".drop:visible").hide();
	}

	const dropOnClick = ($comboDiv, $drop) => {
		$comboDiv.click(function () {
			if ($drop.is(":hidden")) {
				dropShow($drop);
				return false;
			}
		});
	}

	const dropShow = ($drop) => {
		dropsHide();
		menusHide();

		$drop.show();

		const handler = function (e) {
			// click outside drop, not in check tree inside
			if (e && e.target && !$drop[0].contains(e.target))
				$drop.hide();
			else
				$(document).one("click", handler);
		}

		handler();
	}

	/**
	 * Init popup menu.
	 * @param {*} $launcher jQuery selector of start button.
	 * @param {*} $menu jQuery selector to <ul> of menu.
	 * @param {*} align 'left' or 'right'.
	 * @param {*} show when true - show menu immediately, otherwise
	 * will add click event
	 */
	const menuInit = ($launcher, $menu, align, show) => {
		$menu.menu({
			icons: {
				submenu: "ti-angle-right"
			}
		}).hide();

		// empty menu items
		$menu.find("a:not([onclick])").click(function (event) {
			return false;
		});

		if (show) {
			showMenu();
		} else {
			$launcher.click(showMenu);
		}

		function showMenu() {
			menusHide();

			$$.ui.dropsHide();

			$menu.show().position({
				my: align + " top",
				at: align + " bottom",
				of: $launcher
			});

			// to do not process the first click, called the menu
			setTimeout(function () {
				$(document).one("click", function () {
					$menu.hide();
				});
			}, 0);

			return false;
		}
	}

	// close all visible menus
	const menusHide = () => {
		$(document).find("ul.ui-menu:visible[role=menu]").menu().hide();
	}

	const monthDaysSelectInit = ($div) => {
		const debug = $$.debug('uiMonthDaysSelect');

		const $dayFrom = $div.find("#dayFrom");
		const $dayTo = $div.find("#dayTo");

		const $dateFromHidden = $div.find("#dateFrom");
		const $dateToHidden = $div.find("#dateTo");

		const date = monthDateFrom($dateFromHidden);

		const update = function () {
			const $title = $div.find("#month");

			// TODO: Use global month names.
			$title.text($.datepicker._defaults.monthNames[date.getMonth()] + " " + date.getFullYear());

			let dayFrom = $dayFrom.val();
			if (!dayFrom)
				dayFrom = 1;

			let dayTo = $dayTo.val();
			if (!dayTo)
				dayTo = new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate();

			$dateFromHidden.val(new Date(date.getFullYear(), date.getMonth(), dayFrom).format("dd.mm.yyyy"));
			$dateToHidden.val(new Date(date.getFullYear(), date.getMonth(), dayTo).format("dd.mm.yyyy"));

			debug('update: ', dayFrom, dayTo);
		};

		update();

		monthChangeHandlers($div, date, update);

		$dayFrom.change(update);
		$dayTo.change(update);
	}

	const monthSelectInit = ($div) => {
		const debug = $$.debug('uiMonthSelect');

		const $dateFromHidden = $div.find("#dateFrom");

		const date = monthDateFrom($dateFromHidden);

		const update = function () {
			const $title = $div.find("#month");

			// TODO: Use global month names.
			$title.text($.datepicker._defaults.monthNames[date.getMonth()] + " " + date.getFullYear());

			$dateFromHidden.val(new Date(date.getFullYear(), date.getMonth(), 1).format("dd.mm.yyyy"));

			debug('update: ', $dateFromHidden.val());
		};

		update();

		monthChangeHandlers($div, date, update);
	}

	const monthDateFrom = ($dateFromHidden) => {
		let date = new Date();

		const dateFrom = $dateFromHidden.val();
		if (dateFrom) {
			var parts = dateFrom.split('.');
			date = new Date(parts[2], parts[1] - 1, parts[0]);
		}

		date.setDate(1);

		return date;
	}

	const monthChangeHandlers = ($div, date, update) => {
		$div.find("#next").click(function () {
			monthPlus(date);
			update();
		});

		$div.find("#prev").click(function () {
			monthMinus(date);
			update();
		});
	}

	const monthPlus = (date) => {
		const currentMonth = date.getMonth();
		if (currentMonth === 11) {
			date.setYear(date.getFullYear() + 1);
			date.setMonth(0);
		} else
			date.setMonth(currentMonth + 1);
	}

	const monthMinus = (date) => {
		const currentMonth = date.getMonth();
		if (currentMonth == 0) {
			date.setYear(date.getFullYear() - 1);
			date.setMonth(11);
		} else
			date.setMonth(currentMonth - 1);
	}

	const inputTextInit = ($input) => {
		var $clearIcon =
			$("<span class='ti-close'></span>")
				.css("position", "absolute")
				.css("cursor", "pointer")
				.hide();

		$input.parent().append($clearIcon);

		var updateClear = function () {
			var position = $input.offset();
			var show = $input.val().length > 0;
			$clearIcon
				.css("top", position.top + $input.height() / 2 + 2 /*TODO: calculate "+ 2" em based */)
				.css("left", position.left + $input.width() - 6 /*TODO: calculate "- 6" em based */)
				.toggle(show);
		};

		$input.on("propertychange change click keyup input paste", function () {
			updateClear();
		});

		$clearIcon.click(function () {
			$input.val("");
			$input.focus();
			updateClear();
		});
	}

	/**
	 * TagBox init function.
	 * @param {jQuery} $target selector for input type='text'.
	 * @param {String} values comma separated list of initial values.
	 * @param {Boolean} focus set focus on.
	 * @param {String} url URL for loading values using AJAX.
	 * @param {Boolean} preload send AJAX request upfront.
	 */
	const tagBoxInit = function ($target, values, focus, url, preload) {
		let autocomplete = [];

		if (Array.isArray(values)) {
			autocomplete = values;
		} else if (typeof values === 'string') {
			autocomplete = values.split(',');
		}

		$target.tagator({
			autocomplete: autocomplete,
			useDimmer: false,
			showAllOptionsOnFocus: !!focus,
			height: 'auto'
		});

		if (url) {
			const $tagatorInput = $('#tagator_' + $target.attr('id')).find('input.tagator_input');

			const tagatorInit = () => {
				$$.ajax
					.post(url)
					.done((response) => {
						$target.tagator('autocomplete', response.data.values);
					});
			}

			if (preload)
				tagatorInit();
			else
				$tagatorInput.one('focus', () => tagatorInit());
		}
	}

	/**
	 * Calculates layout process for a DOM element.
	 * Processed classes:
	 * 'layout-height-rest' - set an element' height as the rest of available parent' height;
	 * 'layout-width-rest' - set an element' width as the rest of available parent' width.
	 * @param {jQuery} $selector the processed element' selector.
	 */
	const layout = ($selector) => {
		const debug = $$.debug("ui.layout");

		$selector.find(".layout-height-rest").each(function () {
			let height = $(this.parentNode).height();
			const restEl = this;

			debug("Set height: ", $(this), "parent: ", $(this.parentNode), 'height = ' + height);

			$(this.parentNode).children().each(function () {
				if (this.localName == 'script' || this == restEl ||
					$(this).hasClass("layout-height-rest"))
					return;

				height -= $(this).outerHeight(true);

				debug($(this), $(this).outerHeight(true), height);
			})

			debug("height => ", height);

			$(this).css("height", height + "px");
		});

		$selector.find(".layout-width-rest").each(function () {
			let width = $(this.parentNode).width();
			const restEl = this;

			debug("Set width: ", $(this), "parent: ", $(this.parentNode), 'width = ' + width);

			$(this.parentNode).children().each(function () {
				if (this.localName == 'script' || this == restEl ||
					$(this).hasClass("layout-width-rest"))
					return;

				width -= $(this).outerWidth(true);

				debug($(this), $(this).outerWidth(true), width);
			})

			debug("width => ", width);

			$(this).css("width", width + "px");
		});
	}

	const tabsLoaded = ($tabs, event, callback) => {
		if ($tabs.data(event))
			callback();
		else
			$tabs.one(event, () => {
				$tabs.data(event, true);
				callback();
			});
	}

	const inputFocus = ($selector) => {
		// иначе в FF не работает
		setTimeout(function () {
			// mouseover() мыши нужно для datepicker а, чтобы отобразился редактор
			$selector.mouseover().focus();
		}, 0);
	}

	/**
	 * Code mirror highlight for textarea.
	 * @param {*} id textarea CSS ID.
	 * @param {*} mode optional mode, if not defined when used 'properties'.
	 */
	const codeMirror = (id, mode) => {
		const ta = document.getElementById(id);
		const editor = CodeMirror.fromTextArea(ta, {
			mode: mode ? mode : "properties",
			lineNumbers: true,
			styleActiveLine: true,
			matchBrackets: true
		});

		const $ta = $(ta);
		const originalConfig = editor.getValue();
		editor.on('change', () => {
			$ta.next('.CodeMirror').toggleClass('CodeMirror-changed', editor.getValue() !== originalConfig);
			$ta.val(editor.getValue());
		});

		return editor;
	}

	/**
	 * Table rows highlighter. Preserving and restoring original row background color.
	 * Can highlight multiple rows together.
	 * @param {*} $table table selector.
	 * @param {*} rows how many rows to highlight, if not defined - 1.
	 */
	const tableRowHl = ($table, rows) => {
		if (!rows) rows = 1;

		const attrBgColor = 'bgcolor';
		const attrBgColorOrig = 'bgcolor-orig';
		const classHl = 'hl';

		const getFirstTr = ($tr) => $($tr.parent().children().get($tr.index() - $tr.index() % rows));

		$table.find('> tbody > tr:gt(' + (rows - 1) + ')').each(function () {
			const $tr = $(this);
			$tr.mouseover(function () {
				let $ftr = getFirstTr($tr);

				const bgcolor = $ftr.attr(attrBgColor) || 'white';

				if (!$ftr.attr(attrBgColorOrig)) {
					$ftr.attr(attrBgColorOrig, bgcolor);
					for (var i = 0; i < rows; i++) {
						$ftr.addClass(classHl);
						$ftr = $ftr.next();
					}
				}
			});

			$tr.mouseleave(function () {
				let $ftr = getFirstTr($tr);

				const bgcolorOrig = $ftr.attr(attrBgColorOrig);
				if (bgcolorOrig) {
					for (var i = 0; i < rows; i++) {
						$ftr.removeClass(classHl);
						$ftr.removeAttr(attrBgColorOrig);
						$ftr = $ftr.next();
					}
				}
			});
		});
	}

	/**
	 * Set listener for paste event for attachment.
	 * @param mainFormId
	 * @param uploadFormId
	 */
	const setPasteUploadListener = function (mainFormId, uploadFormId) {
		const mainForm = document.getElementById(mainFormId);
		if (mainForm) {
			mainForm.addEventListener('paste', function (e) {
				if (e && e.clipboardData && e.clipboardData.files && e.clipboardData.files.length) {
					const form = $('#' + uploadFormId);
					if (form) {
						const fileInput = form.find('>input[type="file"]')[0];
						if (fileInput) {
							fileInput.files = e.clipboardData.files;
							form.submit();
						}
					}
				}
			});
		}
	}

	/**
	 * Add file to upload list.
	 * @param {*} form
	 * @param {*} uploadFormId
	 */
	const uploadAdd = function (form, uploadFormId) {
		const uploadList = form.querySelector(".upload-list");

		const input = uploadList.parentNode.querySelector("input[type='hidden'][name='addFileId']");
		const id = input.value;

		// upload
		if (id == 0) {
			$$.ajax.triggerUpload(uploadFormId);
		}
		// already uploaded or announce
		else{
			const paramName = id > 0 ? "fileId" : "announcedFileId";

			const li = form.querySelector("li[value='" + id + "']");

			const $div = $("<div>" +
					"<input type='hidden' name='" + paramName + "' value='" + id + "'/>" +
					"<button type='button' class='btn-white btn-small icon mr1' onclick=''><span class='ti-trash'></span></button>" +
					li.textContent +
				"</div>");
			uploadList.append($div[0]);

			$(li).hide();
			$(form.querySelector("li[value='0']")).click();

			$div.find("button").click(() => {
				$div.remove();
				$(li).show();
			});
		}
	}

	// public functions
	this.comboSingleInit = comboSingleInit;
	this.comboSingleFilter = comboSingleFilter;
	this.comboInputs = comboInputs;
	this.comboCheckUncheck = comboCheckUncheck;
	this.comboPermTreeCheckInit = comboPermTreeCheckInit;
	this.dropsHide = dropsHide;
	this.dropOnClick = dropOnClick;
	this.dropShow = dropShow;
	this.menuInit = menuInit;
	this.monthDaysSelectInit = monthDaysSelectInit;
	this.monthSelectInit = monthSelectInit;
	this.inputTextInit = inputTextInit;
	this.tagBoxInit = tagBoxInit;
	this.layout = layout;
	this.tabsLoaded = tabsLoaded;
	this.inputFocus = inputFocus;
	this.codeMirror = codeMirror;
	this.tableRowHl = tableRowHl;
	this.setPasteUploadListener = setPasteUploadListener;
	this.uploadAdd = uploadAdd;
}

function layoutProcess($selector) {
	console.warn($$.deprecated);
	$$.ui.layout($selector);
}

function tableRowHl($table, rows) {
	console.warn($$.deprecated);
	$$.ui.tableRowHl($table, rows);
}

function optionTag(id, title, selected) {
	var tag = "<option value='" + id + "'";
	if (selected) {
		tag += " selected='1'";
	}
	tag += ">" + title + "</option>";
	return tag;
}

function scrollToElementById(id) {
	$("html:not(:animated), body:not(:animated)").animate({scrollTop: $("#" + id).position().top});
}

//admin/process/type/check_list
function normalizeDivHeight(uiid) {
	$('#' + uiid + 'tableDiv').css("height", $('#' + uiid + 'tableDiv').parent().height() - 30);
}

function moveBeforePrevVisible(element) {
	var temp_pointer = $(element);
	do {
		temp_pointer = temp_pointer.prev();
	} while (temp_pointer.is(':hidden') || temp_pointer.is('table'));
	temp_pointer.before($(element));
}

function moveAfterNextVisible(element) {
	var temp_pointer = $(element);
	do {
		temp_pointer = temp_pointer.next();
	} while (temp_pointer.is(':hidden') || temp_pointer.is('table'));
	temp_pointer.after($(element));
}


