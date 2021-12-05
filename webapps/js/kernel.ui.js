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
		var $drop = $comboDiv.find('ul.drop');
		var $hidden = $comboDiv.find('input[type=hidden]');

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
				onSelect(this);
			}

			$drop.hide();

			return false;
		});

		updateCurrentTitle();
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

	// close all visible drop-downs
	const dropsHide = () => {
		$(document).find("ul.drop:visible").hide();
	}

	const dropOnClick = ($comboDiv, $drop) => {
		$comboDiv.click(function () {
			dropShow($drop);
			return false;
		});
	}

	const dropShow = ($drop) => {
		dropsHide();
		menusHide();

		$drop.show();

		$(document).one("click", function () {
			$drop.hide();
		});
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
		const date = new Date();

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
	 */
	const tagBoxInit = function ($target, values, focus, url) {
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
			$tagatorInput.one('focus', () => {
				$$.ajax
					.post(url)
					.done((response) => {
						$target.tagator('autocomplete', response.data.list);
						$target.tagator('refresh');
						$tagatorInput.click();
					});
			});
		}
	}

	const layout = ($selector) => {
		var debug = false;

		$selector.find(".layout-height-rest").each(function () {
			var height = $(this.parentNode).height();
			var restEl = this;

			if (debug) {
				console.debug("Set height: ", $(this), "parent: ", $(this.parentNode), 'height = ' + height);
				console.debug($(this.parentNode).find(">*"));
			}

			$(this.parentNode).children()./*find( ">*" ).*/each(function () {
				if (this.localName == 'script' || this == restEl ||
					$(this).hasClass("layout-height-rest"))
					return;

				height -= $(this).outerHeight(true);

				if (debug) {
					console.debug($(this), $(this).outerHeight(true), height);
				}
			})

			if (debug) {
				console.debug("height => " + height);
			}

			$(this).css("height", height + "px");
		})
	}

	const showError = (errorMessage) => {
		$("#errorDialogMessage").html(errorMessage.replace(/\\n/g, "<br/>"));
		if (!$("#errorDialog").dialog("isOpen"))
			$("#errorDialog").dialog("open");
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

	const codeMirror = (id) => {
		const ta = document.getElementById(id);
		const editor = CodeMirror.fromTextArea(ta, {
			mode: "properties",
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
	this.comboInputs = comboInputs;
	this.comboCheckUncheck = comboCheckUncheck;
	this.dropsHide = dropsHide;
	this.dropOnClick = dropOnClick;
	this.dropShow = dropShow;
	this.menuInit = menuInit;
	this.monthDaysSelectInit = monthDaysSelectInit;
	this.monthSelectInit = monthSelectInit;
	this.inputTextInit = inputTextInit;
	this.tagBoxInit = tagBoxInit;
	this.layout = layout;
	this.showError = showError;
	this.tabsLoaded = tabsLoaded;
	this.inputFocus = inputFocus;
	this.codeMirror = codeMirror;
	this.tableRowHl = tableRowHl;
	this.setPasteUploadListener = setPasteUploadListener;
	this.uploadAdd = uploadAdd;
}


function uiComboSingleInit($comboDiv, onSelect) {
	console.warn($$.deprecated);
	$$.ui.comboSingleInit($comboDiv, onSelect);
}

function uiComboInputs($div) {
	console.warn($$.deprecated);
	return $$.ui.comboInputs($div);
}

function uiComboCheckUncheck(object) {
	console.warn($$.deprecated);
	$$.ui.comboCheckUncheck(object);
}

function uiInputTextInit($input, onSelect) {
	console.warn($$.deprecated);
	$$.ui.inputTextInit($input, onSelect);
}

function layoutProcess($selector) {
	console.warn($$.deprecated);
	$$.ui.layout($selector);
}

function showErrorDialog(errorMessage) {
	console.warn($$.deprecated);
	$$.ui.showError(errorMessage);
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


