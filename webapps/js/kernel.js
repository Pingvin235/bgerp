/*
 * Global JS variable, storing all BGERP related data on Front End.
 * All the other functions and fields must be placed inside.
 */
const bgerp = {};
const $$ = bgerp;

// personal user settings
$$.pers = {};

// global key's state, pressed 'alt' etc.
$$.keys = {};
$$.keys.altPressed = function () {
	return !!$$.keys[18];
}

// suppress debug messages for areas
$$.debugAreas = {
	openUrl: 0,
	ajax: 0,
	shell: 0,
	'shell.login': 0,
	'shell.message': 0,
	buffer: 0,
	datepicker: 0,
	'process.hideLeftAreaOnScroll': 0,
	processQueue: 0,
	doOnClick: 0,
	uiMonthDaysSelect: 0,
	uiMonthSelect: 0,
	'ui.input': 0,
	'ui.layout': 0,
	'ui.select': 0,
	queueFilterDrag: 0,
	blow: 0,
	grpl: 0
};

/*
 * For debugging create a function:
 *  const debug = debug('areaName');
 * after use it:
 *  debug(a, b, c, d)
 */
$$.debug = function (debugArea) {
	return function () {
		const debugAreas = $$.debugAreas;
		if (!debugAreas.hasOwnProperty(debugArea) || debugAreas[debugArea]) {
			Array.prototype.unshift.call(arguments, "debug", debugArea);
			console.log.apply(console, arguments);
		}
	}
}

$$.error = function () {
	Array.prototype.unshift.call(arguments, "error", debugArea);
	console.log.apply(console, arguments);
}

$$.deprecated = "Deprecated";

// обработчики сообщений,пришедших от сервера
$$.eventProcessors = {};

// типы объектов ядра, сюда же добавляются плагины
$$.objectTypeTitles = {
	"process" : "Процесс",
	"customer" : "Контрагент"
};

$(function(){
	$("input.hasDatePicker").on('keyup',
		function(event)
		{
			if( (event.keyCode >= 48 && event.keyCode <= 57) || (event.keyCode >= 96 && event.keyCode <= 105 ) )
			{
				if( $( this ).val().length == 2 || $( this ).val().length ==5 )
				{
					$( this ).val( $( this ).val() + '.' );
				}
			}
			event.preventDefault();
		});

	/* Scroll to top floating button */
	$(window).scroll(function () {
		$(this).scrollTop() > 300 ? $('#scroll-to-top:hidden').fadeIn() : $('#scroll-to-top:visible').fadeOut();
	});
	$('#scroll-to-top').click(function () {
		$('html, body').animate({scrollTop: 0}, 600);
	});
});

function addEventProcessor( eventType, processor )
{
	var processors;
	if( !(processors = $$.eventProcessors[eventType]) )
	{
		processors = [];
		$$.eventProcessors[eventType] = processors;
	}
	processors.push( processor );
}

function processEvent( event )
{
	processors = $$.eventProcessors[event.className];
	if (processors) {
		for (var i = 0; i < processors.length; i++) {
			processors[i](event);
		}
	} else
		console.warn('Not found processor for', event);
}

function toPageId( formId, pageIndex, pageSize, pagePrefix )
{
	var form = document.getElementById( formId );
	toPage( form, pageIndex, pageSize, pagePrefix);
}

// form       - целевая форма, которая отправляется при изменении количества страниц
// pageIndex  - номер страницы
// pagePrefix - префикс, с которым передаётся параметр нужной страницы и в запросе
function toPage( form, pageIndex, pageSize, pagePrefix )
{
	var el = form.elements['page.pageIndex'];
	if( !el )
	{
		el = document.createElement( "input" );
		el.type = "hidden";
		el.name = "page.pageIndex";
	}
	el.value = pageIndex;
	form.appendChild(el);

	el = document.createElement( "input" );
	el.type = "hidden";
	el.name = "page.pageSize";
	el.value = pageSize;
	form.appendChild( el );
}

function enterPressed( e )
{
	var keycode;
	if( window.event )
	{
		keycode = window.event.keyCode;
	}
	else if( e )
	{
		keycode = e.which;
	}
	else
	{
		return false;
	}

	return keycode == 13;
}

function truncValue( value, length )
{
	if( length < 4 )
	{
		return value;
	}
	if( value.length > length )
	{
		return value.substr( 0, length - 4 ) + "...";
	}
	return value;
}

function getCheckedValuesUrl( $selector, inputName )
{
	var values = "";
	var $checked = $selector.find( " input:checked[name='" + inputName + "']" );
	for( var i = 0; i < $checked.length; i++ )
	{
		values += "&" + inputName + "=" + $checked[i].value;
	}
	return values;
}

function getCheckedValues( $selector, inputName )
{
	var values = "";
	var $checked = $selector.find( " input:checked[name='" + inputName + "']" );
	for( var i = 0; i < $checked.length; i++ )
	{
		if( values.length != 0 )
		{
			values += ",";
		}
		values += $checked[i].value;
	}
	return values;
}

// the functions deleteLinksWithType is always used together with addLink
function deleteLinksWithType( objectType, objectId, linkedObjectType )
{
	const url = "/user/link.do?method=deleteLinksWithType&id=" + objectId + "&" + $$.ajax.requestParamsToUrl({ "objectType": objectType, "linkedObjectType": linkedObjectType });
	return $$.ajax.post(url);
}

function addLink( objectType, objectId, linkedObjectType, linkedObjectId, linkedObjectTitle, params )
{
	var requestParams = { "objectType" : objectType, "linkedObjectType" : linkedObjectType, "linkedObjectId" : linkedObjectId, "linkedObjectTitle" : linkedObjectTitle };
	if( params )
	{
		for( var i in params )
		{
			requestParams["c:" + i] = params[i];
		}
	}

	const url = "/user/link.do?method=addLink&id=" + objectId + "&" + $$.ajax.requestParamsToUrl(requestParams);
	return $$.ajax.post(url);
}

//фильтр по исполнителям, в реальном времени обновляет список пользователей
function checkFilter( executorMaskInput, listId )
{
	var mask = executorMaskInput.val();

	$("#" + listId + " tr").each( function()
	{
		var content = $(this).html().toLowerCase();
		content.indexOf( mask.toLowerCase() )==-1 ? $(this).hide() : $(this).show();
	});
}

function addParameter(uiid,count)
{
	addParameter(uiid, count, false);
}

function addParameter(uiid,count,firstOnly)
{
	var selected = $('#' + uiid + 'select').val();
	var lastVisible = $('#'+ uiid +'table').find('tbody tr:visible:last');
	$('#'+ uiid + 'row' + selected).css("display","");
	(lastVisible).after($('#'+ uiid + 'row' + selected));
	var checkbox = $('#'+ uiid + 'row' + selected).find('input[type=checkbox]').attr('checked', 'checked');
	if(typeof count != 'undefined' && count !="")
	{
		var selectedText = $('#' + uiid + 'select').find('option:selected').text();
		checkbox.val(selected+":"+count+":"+selectedText);

		if( firstOnly )
		{
			$('#'+ uiid + 'row' + selected).find('input[type=text]').first().val(count);
		}
		else
		{
			$('#'+ uiid + 'row' + selected).find('input[type=text]').val(count);
		}
	}
	$('#'+ uiid + 'select' + ' option:selected').remove();
}

function delParameter(uiid, itemId, text) {
	$('#' + uiid + 'row' + itemId).find('input[type=checkbox]').removeAttr('checked').val(itemId);
	$('#' + uiid + 'row' + itemId).css("display", "none");
	$('#' + uiid + 'select').append('<option value=' + itemId + '>' + text + '</option>');
}

//admin/process/type/check_list
function markOutTr(tr)
{
	$(tr).parent().children().css('background-color','transparent');
	$(tr).css('background-color','grey');
}

$$.encodeHtml = function (str) {
	var i = str.length,
		aRet = [];

	while (i--) {
		var iC = str[i].charCodeAt();
		if (iC < 65 || iC > 127 || (iC > 90 && iC < 97)) {
			aRet[i] = '&#' + iC + ';';
		}
		else {
			aRet[i] = str[i];
		}
	}

	return aRet.join('');
}

function datetimepickerValueChanged( dayColorList, inst )
{
	if( !dayColorList )
	{
		return;
	}

	for( var i=0; i<dayColorList.length; i++ )
	{
		if( dayColorList[i].color != "" )
		{
			$( '#ui-datepicker-calendar-day-'+dayColorList[i].monthDay ).children().css( "background",dayColorList[i].color );
		}
		if( dayColorList[i].comment != "" )
		{
			$( '#ui-datepicker-calendar-day-'+dayColorList[i].monthDay ).attr( 'title',dayColorList[i].comment );
		}
	}

	//Селектор для выбора времени
	var timeSelector = $( "div#ui-timepicker-div-" + inst.id ).find( "select.ui-timepicker-timeselector" );

	if( $( timeSelector ) != 'undefined' )
	{
		$( timeSelector ).empty();

		if( dayColorList[inst.selectedDay-1] != 'undefined' && dayColorList[inst.selectedDay-1].timeList.length > 0 )
		{
			var availableTime = dayColorList[inst.selectedDay-1].timeList.toString().split( "," );
			var appendTimeStr = '';

			availableTime.forEach( function( value )
			{
				appendTimeStr+='<option>'+value+'</option>';
			});

			$( timeSelector ).append( appendTimeStr );
		}
	}
}

function datetimepickerOnChanging(year, month, inst, url)
{
	if(!url)
	{
		return;
	}

	datetimepickerValueChanged($$.ajax.post(url + $$.ajax.requestParamsToUrl({ 'newDate': '01.' + month + "." + year })).data.dayColorList, inst);
}

function openedObjectList( params )
{
	var result = [];

	var includeTypes = undefined;
	var excludeTypes = undefined;
	var selectedValues = undefined;

	if( params )
	{
		includeTypes = params['typesInclude'];
		excludeTypes = params['typesExclude'];
		selectedValues = params['selected'];
	}

	$("#objectBuffer ul li").each( function()
	{
		var data = {};

		var value = $(this).attr( "value" );
		var pos = value.lastIndexOf( "-" );
		if( pos <= 0 )
		{
			console.error( "Incorrect value: " + value );
			return;
		}

		data.id = value.substr( pos + 1 );
		data.title = $(this).find( ".title" ).text();
		if( !data.title )
		{
			data.title = $(this).text();
		}
		data.objectType = value.substr( 0, pos );
		data.objectTypeTitle = $$.objectTypeTitles[data.objectType];

		if( includeTypes && $.inArray( data.objectType, includeTypes ) < 0 )
		{
			return;
		}

		if( excludeTypes && $.inArray( data.objectType, excludeTypes ) >= 0 )
		{
			return;
		}
		// в некоторых сущностях (договора биллинга) в id поле таба запоминается два значения через тире: billingId-contractId
		// а в базе у них представление такое, что billingId уже добавляется через двоеточие к типу: contract:billingId
		// поэтому здесь всё приводится к единой строке type-id, contract-billingId-id через тире, такое же ожидается в selectedValues
		var fullKey = data.objectType + "-" + data.id;
		if( selectedValues && $.inArray( fullKey, selectedValues ) >= 0 )
		{
			return;
		}

		result.push( data );
	})

	return result;
}

function showPopupMessage( title, message )
{
	var $messageDiv = $( "<div>" + message +"</div>" );

	$( "body" ).append( $messageDiv );

	$( $messageDiv ).dialog({
		autoOpen: false,
		show: "slide",
		hide: "explode",
		resizable: false,
		position: { my: "center top", at: "center top+100px", of: window },
		title: title,
		close: function(event, ui)
		{
			$messageDiv.remove();
		}
	});

	$messageDiv.dialog( "open" );
}

//обработка событий
addEventProcessor('ru.bgcrm.event.client.MessageOpenEvent', processClientEvents);
addEventProcessor('ru.bgcrm.event.client.UrlOpenEvent', processClientEvents);

function processClientEvents(event) {
	if (event.className == 'ru.bgcrm.event.client.MessageOpenEvent') {
		$$.shell.contentLoad("/user/message/queue").done(() => {
			$$.ajax.loadContent('/user/message.do?typeId=' + event.typeId + '&messageId=' + event.systemId + '&returnUrl=' + encodeURIComponent('/user/message.do?method=messageList'));
		});
	}
	else if (event.className == 'ru.bgcrm.event.client.UrlOpenEvent') {
		$$.shell.contentLoad(event.url);
	}
}

/**
 * Validates text inputs for entering numeric values only, possible with decimal separator.
 * @param {Event} event 'onkeydown' or another event with 'key' property of 'text' input
 * @param {Number} digits amount of digits after dot, if not defined than 2
 * @return {Boolean} is input change allowed
 */
function isNumberKey(event, digits) {
	if (digits === undefined)
		digits = 2;

	if (event.ctrlKey || ['Backspace', 'ArrowLeft', 'ArrowRight', 'Home', 'End', 'Delete'].includes(event.key))
		return true;

	const target = event.target;
	const value = target.value;

	const valueCandidate = value.substring(0, target.selectionStart) + event.key + value.substring(target.selectionEnd);

	let result = !isNaN(valueCandidate);
	if (result && digits > 0) {
		const pos = valueCandidate.indexOf('.');
		result = pos < 0 || valueCandidate.length - pos - 2 < digits;
	}

	return result;
}

function updateLastModify( object, $uiid )
{
	var lastModify = object.lastModify;
	if( lastModify )
	{
		$uiid.find("input[name='lastModifyUserId']").val( lastModify.userId );
		$uiid.find("input[name='lastModifyTime']").val( lastModify.time );
	}
}

// выполнение действия по нажатию, при этом не обрабатывается выделение текста + ряд проверок по буферу и т.п.
$$.doOnClick = ($selector, filter, callback) => {
	const getSelected = () => {
		if (window.getSelection)
			return window.getSelection();
		else if (document.getSelection)
			return document.getSelection();
		else if (document.selection)
			return document.selection.createRange().text;

		return '';
	}

	// запоминание выделенного текста, т.к. на onclick он будет уже пуст
	let dontOpenProcess = false;

	// нажатие с открытым буфером либо выделенным текстом
	$selector.on('mousedown', filter, function (event) {
		dontOpenProcess = getSelected().toString() || $("#objectBuffer > ul.drop").is(":visible");
	});

	const timerHolder = {};

	$selector.on('click', filter, function (event) {
		if (event.target.nodeName == 'A' ||
			event.target.nodeName == 'BUTTON' ||
			event.target.nodeName == 'INPUT' ||
			// если клик очищал выделение либо убирал буфер
			dontOpenProcess ||
			// при клике (отпускании мыши выделен текст либо открыт буфер)
			getSelected().toString() ||
			$("#objectBuffer > ul.drop").is(":visible")) {
			return;
		}

		const $clicked = $(this);

		window.clearTimeout(timerHolder.timer);

		timerHolder.timer = window.setTimeout(function () {
			$$.debug('doOnClick', "open");
			callback($clicked);
		}, 300);

		$$.debug('doOnClick', "start timeout", timerHolder.timer, event);
	})

	// двойной клик используется для выделения текста
	$selector.on('dblclick', filter, function (event) {
		$$.debug('doOnClick', "clear timeout", timerHolder.timer);
		window.clearTimeout(timerHolder.timer);
	})
}

/**
 * Highlights file
 * @param {*} id
 * @param {*} url
 */
$$.hlFile = (id, url) => {
	$$.ajax.post(url).done((result) => {
		const classes = result.data.classes;
		if (classes)
			document.getElementById(id).classList.add(classes);
	})
}

$(document).delegate('textarea.tabsupport', 'keydown', function(e) {
	  var keyCode = e.keyCode || e.which;

	  if (keyCode == 9) {
		e.preventDefault();
		var start = $(this).get(0).selectionStart;
		var end = $(this).get(0).selectionEnd;

		// set textarea value to: text before caret + tab + text after caret
		$(this).val($(this).val().substring(0, start)
					+ "\t"
					+ $(this).val().substring(end));

		// put caret at right position again
		$(this).get(0).selectionStart =
		$(this).get(0).selectionEnd = start + 1;
	  }
});

window.onkeydown = function(e) {
	$$.keys[e.keyCode] = 1;
	setTimeout( function() {
		delete $$.keys[e.keyCode]
	}, 4000);
};

window.onkeyup = function(e) {
	delete $$.keys[e.keyCode];
};
