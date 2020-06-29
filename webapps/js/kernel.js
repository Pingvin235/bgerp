/*
 * Глобальная переменная JS, в которой хранятся все переменные BGERP.
 * Все остальные функции подключаются в неё.
 */
const bgerp = {};
const bgcrm = bgerp;
const $$ = bgerp;

// персональные настройки пользователя
$$.pers = {};

// глобальное состояние клави, зажатые alt и т.п.
$$.keys = {};
$$.keys.altPressed = function () {
	return !!$$.keys[18];
}

$$.debugAreas = {
	openUrl: 0,
	ajax: 0,
	shell: 0,
	buffer: 0,
	datepicker: 0,
	processQueue: 0,
	doOnClick: 0,
	uiMonthDaysSelectInit: 0,
	queueFilterDrag: 0,
	blow: 0
};

/*
 * Для отладки создать новый объект:
 *  const debug = debug('areaName');
 * далее такой вызов:
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

// система блокировки ресурсов при правке, указанные ниже функции lock/unlock помесить в объект ui или что-то подобное
$$.lock = {};

$$.lock.add = function( name ){
	return sendAJAXCommand( '/user/lock.do?action=add&lockId=' + name )	;
};

$$.lock.free = function( name ){
	return sendAJAXCommand( '/user/lock.do?action=free&lockId=' + name );
};

// блокировка кнопок, вроде пока не используется, разобраться и перенести в bgcrm.ui
function lock( obj )
{
	$(obj).attr( "disabled", true );
}

function unlock( obj, timeout )
{
	if( timeout )
	{
		setTimeout( function()
		{
			$(obj).removeAttr( "disabled" );
		}, timeout );
	}
	else
	{
		$(obj).removeAttr( "disabled" );
	}
}

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
	if( processors )
	{
		for( var i = 0; i < processors.length; i++ )
		{
			processors[i]( event );
		}
	}
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

// линковка
function addLink( objectType, objectId, linkedObjectType, linkedObjectId, linkedObjectTitle, params )
{
	var url = "/user/link.do?action=addLink&id=" + objectId;
	var requestParams = { "objectType" : objectType, "linkedObjectType" : linkedObjectType, "linkedObjectId" : linkedObjectId, "linkedObjectTitle" : linkedObjectTitle };
	if( params )
	{
		for( var i in params )
		{
			requestParams["c:" + i] = params[i];
		}
	}
	return sendAJAXCommandWithParams( url, requestParams );
}

function deleteLink( objectType, objectId, linkedObjectType, linkedObjectId )
{
	var url = "/user/link.do?action=deleteLink&id=" + objectId;
	return sendAJAXCommandWithParams( url, { "objectType" : objectType, "linkedObjectType" : linkedObjectType, "linkedObjectId" : linkedObjectId });
}

function deleteLinksWithType( objectType, objectId, linkedObjectType )
{
	var url = "/user/link.do?action=deleteLinksWithType&id=" + objectId;
	return sendAJAXCommandWithParams( url, { "objectType" : objectType, "linkedObjectType" : linkedObjectType } );
}

function deleteLinksTo( objectType, linkedObjectType, linkedObjectId )
{
	var url = "/user/link.do?action=deleteLinksTo";
	return sendAJAXCommandWithParams( url, { "objectType" : objectType, "linkedObjectType" : linkedObjectType, "linkedObjectId" : linkedObjectId });
}


// буфер обмена параметров (в данный момент нигде не работает)
// возможно, стоит сделать вместо него перенос параметров с объекта на объект

// buffer
$$.buffer = {};
$$.buffer.objects = [];

$$.buffer.storeParam = function( id, paramId, parameterType )
{
	var url = "/user/parameter.do?action=parameterGet";
	var object = {};
	object.type = parameterType;
	object.id = id;
	object.paramId = paramId;
	object.value = sendAJAXCommandWithParams( url, { "id" : id, "paramId" : paramId } ).data;

	// TODO: проверять есть ли уже параметр с таким id:paramId и добавлять только в том случае, если он есть

	$$.buffer.objects.push( object );
}

$$.buffer.fillWithStoredObjects = function( select, type )
{
	select.empty();

	switch( type )
	{
		case 'phone':
			{
				var title = "";
				for( var i=0; i<$$.buffer.objects.length; i++ )
				{
					var obj = $$.buffer.objects[i].value;
					title += "+" + obj.parts1[0] + " (" + obj.parts1[1] + ") " + obj.parts1[2] + "; ";
				}
				select.append("<option value=''>" + title + "</option>");
			}
			break;
	}
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

function delParameter(uiid,itemId,text)
{
	$('#'+ uiid + 'row' + itemId).find('input[type=checkbox]').removeAttr('checked').val(itemId);
	$('#'+uiid+'row'+itemId).css("display","none");///
	$('#'+ uiid + 'select').append('<option value='+itemId+'>'+text+'</option>');
}
//admin/process/type/check_list
function markOutTr(tr)
{
	$(tr).parent().children().css('background-color','transparent');
	$(tr).css('background-color','grey');
}

function timer()
{
	var urlArray = generateUrlForFilterCounter(),
		url = "/user/pool.do",
		callback = function()
		{
			window.setTimeout( timer, 5000 );
		};

	if ( urlArray.length > 0 )
	{
		url += "&processCounterUrls=" + encodeURIComponent(urlArray);
	}

	$$.ajax
		.post(url, {toPostNames: ["processCounterUrls"]})
		.always(callback);
}

function encodeHtml( str )
{
	var i = str.length,
	aRet = [];

	while( i-- )
	{
		var iC = str[i].charCodeAt();
		if( iC < 65 || iC > 127 || (iC>90 && iC<97) )
		{
			aRet[i] = '&#'+iC+';';
		}
		else
		{
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

	datetimepickerValueChanged(sendAJAXCommandWithParams( url,{'newDate':'01.'+month+"."+year} ).data.dayColorList, inst);
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

function showLoginPopup()
{
	if( !$("#loginForm").dialog( "isOpen" ) )
	{
		$("#loginForm").dialog( "open" );
	}
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
addEventProcessor( 'ru.bgcrm.event.client.NewsInfoEvent', processClientEvents );
addEventProcessor( 'ru.bgcrm.event.client.MessageOpenEvent', processClientEvents );
addEventProcessor( 'ru.bgcrm.event.client.LockEvent', processClientEvents );
addEventProcessor( 'ru.bgcrm.event.client.UrlOpenEvent', processClientEvents );

function processClientEvents( event )
{
	var messagesCount = 0;

	var $messagesLink = $('#messagesLink');
	var $messagesMenu = $("#messagesMenu");

	$messagesMenu.html( "" );

	if( event.className == 'ru.bgcrm.event.client.NewsInfoEvent' )
	{
		messagesCount = event.newsCount + event.messagesCount;

		// новости
		var itemCode = "<li><a href='/user/news' onclick='$$.shell.followLink(this.href, event)'>Новостей: <span style='font-weight: bold;";
		if( event.blinkNews )
			itemCode += "color: orange;"
		itemCode += "'>" + event.newsCount + "</span></a></li>";

		$messagesMenu.append( itemCode );

		// сообщения
		if (event.messagesCount > 0) {
			itemCode = "<li><a href='/user/message/queue' onclick='$$.shell.followLink(this.href, event)'>Сообщений необр.: <span style='font-weight: bold;";
			if (event.blinkMessages)
				itemCode += "color: orange;";
			itemCode += "'>" + event.messagesCount + "</span></a></li>";

			$messagesMenu.append( itemCode );
		}

		//проверка всплывающих новостей
		if (event.popupNews) {
			event.popupNews.forEach(function (id) {
				showPopupMessage( "Последние новости", getAJAXHtml( "/user/news.do?action=newsGet&newsId=" + id ) );
			});
		}

		if (event.blinkNews || event.blinkMessages) {
			// переменная называется blinkMessages, но мигать может и из-за новостей новых
			if (!$$.blinkMessages) {
				$$.blinkMessages = setInterval(function() {
					if ($messagesLink.attr('style')) {
						$messagesLink.attr('style', '');
					} else {
						$messagesLink.css('color', 'orange');
					}
				}, 500)
			}
		} else {
			$messagesLink.attr('style', '');
			if ($$.blinkMessages) {
				clearInterval($$.blinkMessages);
				$$.blinkMessages = undefined;
			}
		}
	}
	else if (event.className == 'ru.bgcrm.event.client.MessageOpenEvent') {
		$$.shell.contentLoad("/user/message/queue").done(() => {
			$$.ajax.load('/user/message.do?id=' + event.id + '&returnUrl=' + encodeURIComponent('/user/message.do?action=messageList'), $$.shell.$content());
		});
	}
	else if (event.className == 'ru.bgcrm.event.client.LockEvent') {
		var lockId = event.lock.id;

		if ($('#lock-' + lockId).length == 0) {
			console.log("Free lock: " + event.lock.id);
			$$.lock.free(event.lock.id);
		}
	}
	else if (event.className == 'ru.bgcrm.event.client.UrlOpenEvent') {
		$$.shell.contentLoad(event.url);
	}

	$messagesMenu.menu( "refresh" );

	$messagesLink.html( messagesCount );
}

function isNumberKey(event)
{
	var charCode = event.which ? event.which : event.keyCode;
	if (charCode !=190 && charCode !=110 && charCode != 46 && charCode > 31 && (charCode < 48 || charCode > 57) &&
		(event.keyCode < 96 || event.keyCode > 105 )&& (event.keyCode < 37 || event.keyCode > 40 ))
	{
		return false;
	}
	return true;
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

function RGBMix( colorHex1, colorHex2 )
{
	var color1 = Color( colorHex1 );
	var color2 = Color( colorHex2 );

	var r = (color1.red() + color2.red()) / 2;
	var g = (color1.green() + color2.green()) / 2;
	var b = (color1.blue() + color2.blue()) / 2;

	return Color().rgb([r, g, b]).hexString();
}

function getSelected ()
{
	var t = '';
	if( window.getSelection )
	{
		t = window.getSelection();
	}
	else if( document.getSelection )
	{
		t = document.getSelection();
	}
	else if( document.selection )
	{
		t = document.selection.createRange().text;
	}
	return t;
}

// выполнение действия по нажатию, при этом не обрабатывается выделение текста +
// ряд проверок по буферу и т.п.
function doOnClick( $selector, filter, callback )
{
	// запоминание выделенного текста, т.к. на onclick он будет уже пуст
	var dontOpenProcess = false;

	// нажатие с открытым буфером либо выделенным текстом
	$selector.on( 'mousedown', filter, function( event )
	{
		dontOpenProcess = getSelected().toString() || $("#objectBuffer > ul.drop").is(":visible");
	});

	var timerHolder = {};

	$selector.on( 'click', filter, function( event )
	{
		if( event.target.nodeName == 'A' ||
			event.target.nodeName == 'BUTTON' ||
			event.target.nodeName == 'INPUT' ||
			// если клик очищал выделение либо убирал буфер
			dontOpenProcess ||
			// при клике (отпускании мыши выделен текст либо открыт буфер)
			getSelected().toString() ||
			$("#objectBuffer > ul.drop").is(":visible") )
		{
			return;
		}

		var $clicked = $(this);

		window.clearTimeout( timerHolder.timer );

		timerHolder.timer = window.setTimeout( function()
		{
			$$.debug( 'doOnClick', "open" );

			callback( $clicked );
		}, 300 );

		$$.debug( 'doOnClick', "start timeout", timerHolder.timer, event );
	})

	// двойной клик используется для выделения текста
	$selector.on( 'dblclick', filter, function( event )
	{
		$$.debug( 'doOnClick', "clear timeout", timerHolder.timer );

		window.clearTimeout( timerHolder.timer );
	})
}

function tableRowHl($table, rows) {
	if (!rows) rows = 1;

	var getFirstTr = function ($tr) {
		return $($tr.parent().children().get($tr.index() - $tr.index() % rows));
	};

	$table.find('> tbody > tr:gt(' + (rows - 1) + ')' ).each( function () {
		var $tr = $(this);
		$tr.mouseover( function () {
			var $ftr = getFirstTr($tr);

			var bgcolor = $ftr.attr( 'bgcolor' );
			if( !bgcolor ) {
				bgcolor = 'white';
			}

			if( !$ftr.attr( 'bgcolor-orig' ) ) {
				$ftr.attr( 'bgcolor-orig', bgcolor );
				for (var i = 0; i < rows; i++) {
					$ftr.attr( 'bgcolor', '#A9F5F2' );
					$ftr = $ftr.next();
				}
			}
		});
		$tr.mouseleave( function() {
			var $ftr = getFirstTr($tr);

			var bgcolorOrig = $ftr.attr( 'bgcolor-orig' );
			if( bgcolorOrig ) {
				for (var i = 0; i < rows; i++) {
					$ftr.attr( 'bgcolor', bgcolorOrig );
					$ftr.attr( 'bgcolor-orig', '' );
					$ftr = $ftr.next();
				}
			}
		});
	});
}

// проверка видимости элемента после скроллинга
$$.isElementInView = function (element, offset) {
	var pageTop = $(window).scrollTop();
	var pageBottom = pageTop + $(window).height();
	var elementTop = $(element).offset().top;
	var elementBottom = elementTop + $(element).height() + offset;

	return (elementTop <= pageBottom) && (elementBottom >= pageTop);
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