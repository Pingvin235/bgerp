/*
 * Standard UI elements.
 */
"use strict";

bgcrm.ui = new function() {
	const markChanged = ($element) => {
		const originalConfig = $element.val();
		$element.on("input", () => {
			$element.css("border", originalConfig !== $element.val() ? "1px solid red" : "");
		});
	};
	
	const comboSingleInit = ($comboDiv, onSelect) => {	
		var $drop = $comboDiv.find('ul.drop');
		
		var $hidden = $comboDiv.find( 'input[type=hidden]' );
		
		const updateCurrentTitle = function () {
			// по-умолчанию выбирается первый элемент
			var $currentLi = $drop.find( 'li:not(.filter):first' );		
			
			// если указано значение - то ищется оно	
			var currentValue = $hidden.val();
			if (currentValue) {
				$currentLi = $();
				
				// Наличие значения в hidden не гарантирует наличия соответствующего 
				// элемента <li>, поэтому берем если нашли сам элемент;
				const $foundLi = $drop.find( "li[value='" + currentValue + "']" );		
				if ($foundLi.length != 0) {
					$currentLi = $foundLi;
				}
			}
			
			var $currentTitle = $currentLi.find('span.title');
			if ($currentTitle.length == 0) {
				$currentTitle = $currentLi;
				$hidden.val($currentLi.attr('value'));
			}
			
			$drop.find('li').removeAttr('selected');
			$currentLi.attr('selected', '1');
			
			$comboDiv.find('.text-value').html($currentTitle.html());
		};
		
		$comboDiv.click(function () {
			$drop.show();
			
			$(document).one("click", function () {
				$drop.hide();
			});
				 
			return false;
		});
		
		// событие клика вешается через функцию on, чтобы событие срабатывало, если элемент добавился после  инициации динамически.
		$drop.on('click', 'li:not(.filter)', function () {
			$hidden.val( $(this).attr( "value" ) );
			updateCurrentTitle();
					
			if (onSelect) {
				onSelect(this);
			}
					
			$drop.hide();
				
			return false;
		});
		
		updateCurrentTitle();
	};

	const comboInputs = ($div) => {
		return $div.find("ul.drop li input");
	};

	const comboCheckUncheck = (object) => {
		const $parent = $(object).closest("ul");	
		if ($parent.find("input[type=checkbox]:checked").length == 0)
			$parent.find("input[type=checkbox]").prop("checked", true);
		else
			$parent.find("input[type=checkbox]").prop("checked", false);
	};

	const monthDaysSelectInit = ($div) => {
		var date = new Date();
		
		var $title = $div.find( "#month" );
		
		var $dayFrom = $div.find( "#dayFrom" );
		var $dayTo = $div.find( "#dayTo" );
		
		var $dateFromHidden = $div.find( "#dateFrom" );
		var $dateToHidden = $div.find( "#dateTo" );
		
		var dateFrom = $dateFromHidden.val();
		if (dateFrom) {
			var parts = dateFrom.split('.');
			date = new Date(parts[2], parts[1]-1, parts[0]);
		}
		
		date.setDate(1);
		
		const update = function() {
			$title.text( $.datepicker._defaults.monthNames[date.getMonth()] + " " + date.getFullYear() );
			
			let dayFrom = $dayFrom.val();
			if (!dayFrom)
				dayFrom = 1;
			
			let dayTo = $dayTo.val();
			if (!dayTo)
				dayTo = new Date(date.getFullYear(), date.getMonth() + 1, 0 ).getDate();
			
			$dateFromHidden.val(new Date( date.getFullYear(), date.getMonth(), dayFrom).format( "dd.mm.yyyy" ));
			$dateToHidden.val(new Date( date.getFullYear(), date.getMonth(), dayTo).format( "dd.mm.yyyy" ));
			
			bgcrm.debug('uiMonthDaysSelectInit', 'update: ', dayFrom, dayTo);
		};
		
		update();
			
		$div.find("#next").click(function () {
			var currentMonth = date.getMonth(); 
			if (currentMonth == 11) {
				date.setYear( date.getFullYear() + 1 );
				date.setMonth( 0 );
			} else
				date.setMonth( currentMonth + 1 );
			update();
		});
		
		$div.find("#prev").click(function () {
			var currentMonth = date.getMonth(); 
			if (currentMonth == 0) {
				date.setYear( date.getFullYear() - 1 );
				date.setMonth( 11 );
			} else
				date.setMonth( currentMonth - 1 );
			update();
		});
		
		$dayFrom.change(update);
		$dayTo.change(update);
	};

	const inputTextInit = ($input, onSelect) => {
		var $runIcon =  
			$("<span title='Искать'><img src='/images/arrow-right.png'/></span>")
			.css("position", "absolute")
			.css("cursor", "pointer")
			.hide();
		
		var $clearIcon =  
			$("<span title='Очистить'><img src='/images/cross.png'/></span>")
			.css("position", "absolute")
			.css("cursor", "pointer")
			.hide();	
		
		$input.parent().append($runIcon).append($clearIcon);
		
		var updateClear = function () {
		    var position = $input.offset();
			var show = $input.val().length > 0;
			$clearIcon
				.css("top", position.top + $input.height() / 2 + 3 /*TODO: replace "+ 3" to the calculated value!*/)
				.css("left", position.left + $input.width() - 20)
				.toggle(show);		
		};
		
		$input.on("mouseenter focusin", function () {
		    var position = $input.offset();
			$runIcon
				.css("top", position.top + $input.height() / 2 + 3 /*TODO: replace "+ 3" to the calculated value!*/)
				.css("left", position.left + $input.width())
				.show();
		});
		
		$input.on("focusout", function () {
			$runIcon.hide();
		});
		
		$input.on("propertychange change click keyup input paste", function () {
			updateClear();
		});
		
		$clearIcon.click(function () {
			$input.val("");
			$input.focus();
			updateClear();
		});
		
		if (onSelect)
			$runIcon.click(function () { onSelect.call($input[0]); });
	};
	
	const layout = ($selector) =>  {
		var debug = false;
		
		$selector.find(".layout-height-rest").each(function () {
			var height = $(this.parentNode).height();
			var restEl = this;
			
			if (debug) {
				console.debug( "Set height: ", $(this), "parent: ", $(this.parentNode), 'height = ' + height );
				console.debug( $(this.parentNode).find( ">*" ) );
			}
			
			$ (this.parentNode).children()./*find( ">*" ).*/each( function () {
				if (this.localName == 'script' || this == restEl ||
					$(this).hasClass( "layout-height-rest" ))
					return;
				
				height -= $(this).outerHeight( true );
				
				if (debug) {
					console.debug( $(this), $(this).outerHeight( true ), height );
				}
			})
			
			if (debug) {
				console.debug( "height => " + height );
			}
			
			$(this).css( "height", height + "px" );
		})
	};
	
	const showError = (errorMessage) => {
		$("#errorDialogMessage").html(errorMessage.replace(/\\n/g, "<br/>"));
		if (!$("#errorDialog").dialog("isOpen"))
			$("#errorDialog").dialog("open");
	};
	
	const tabsLoaded = ($tabs, event, callback) => {
		if ($tabs.data(event))
			callback();
		else
			$tabs.one(event, () => {
				$tabs.data(event, true);
				callback();
			});
	};
	
	// доступные функции
	this.markChanged = markChanged;
	this.comboSingleInit = comboSingleInit;
	this.comboInputs = comboInputs;
	this.comboCheckUncheck = comboCheckUncheck;
	this.monthDaysSelectInit = monthDaysSelectInit;
	this.inputTextInit = inputTextInit;
	this.layout = layout;
	this.showError = showError;
	this.tabsLoaded = tabsLoaded;
};


function uiComboSingleInit ($comboDiv, onSelect) {
	console.warn($$.deprecated);
	bgcrm.ui.comboSingleInit($comboDiv, onSelect);
}

function uiComboInputs ($div) {
	console.warn($$.deprecated);
	return bgcrm.ui.comboInputs($div);
}

function uiComboCheckUncheck (object) {
	console.warn($$.deprecated);
	bgcrm.ui.comboCheckUncheck(object);
}

function uiMonthDaysSelectInit ($div) {
	console.warn($$.deprecated);
	bgcrm.ui.monthDaysSelectInit($div);
}

function uiInputTextInit ($input, onSelect) {
	console.warn($$.deprecated);
	bgcrm.ui.inputTextInit($input, onSelect);
}

function layoutProcess ($selector) {
	console.warn($$.deprecated);
	bgcrm.ui.layout($selector);
}

function showErrorDialog (errorMessage) {
	console.warn($$.deprecated);
	bgcrm.ui.showError(errorMessage);
}

function optionTag( id, title, selected )
{
	var tag = "<option value='" + id + "'";
	if( selected )
	{
		tag += " selected='1'";
	}
	tag += ">" + title + "</option>";
	return tag;
}

function scrollToElementById(id)
{
	$("html:not(:animated), body:not(:animated)").animate({scrollTop: $("#"+id).position().top});
}

//admin/process/type/check_list
function normalizeDivHeight(uiid)
{
	$('#'+uiid +'tableDiv').css("height",$('#'+uiid +'tableDiv').parent().height()-30);
}

function moveBeforePrevVisible(element)
{
	temp_pointer = $(element);
	do
	{
		temp_pointer = temp_pointer.prev();
	} while (temp_pointer.is(':hidden') || temp_pointer.is('table'));
	temp_pointer.before($(element));
}

function moveAfterNextVisible(element)
{
	temp_pointer = $(element);
	do
	{
		temp_pointer = temp_pointer.next();
	} while (temp_pointer.is(':hidden') || temp_pointer.is('table'));
	temp_pointer.after($(element));
}

