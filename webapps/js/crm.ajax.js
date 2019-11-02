// "use strict";

bgerp.ajax = new function() {
	const debug = bgerp.debug("ajax");
	
	/* Отправляет AJAX запрос и возвращает Promise. */
	const post = (url, options) => {
		if (!options) options = {}; 
		
		const separated = separatePostParams(url, options.toPostNames, false);
		
		return $.ajax({ 
			type: "POST",
			url: separated.url,
			data: separated.data,
		}).fail(function (jqXHR, textStatus, errorThrown) {
			onAJAXError(separated.url, jqXHR, textStatus, errorThrown);
		});		
	};
	
	/* 
	 * Default - send request and add result HTML on element. 
	 * vars.replace - replace element by HTML.
	 * vars.append  - append HTML into the element.
	 */
	const openUrlTo = (url, $selector, vars) => {
		debug("openUrlTo", url);
		
		if (url.tagName === 'FORM')
			url = formUrl(url);
		
		if (typeof $selector === 'string')
			$selector = $($selector);
		
		return post(url, vars).done(function (result) {
			if (vars && vars.replace)
				$selector.replaceWith(result);
			else if (vars && vars.append)
				$selector.append(result);
			else
				$selector.html(result);
		});
	}
	
	// TODO: Сделать функции.
	// openUrl, openUrlPos, openUrlTo, openUrlToAsync, openUrlToParent -> queryTo(url, $selector, options) 
	// 
	
	// доступные функции
	this.post = post;
	this.openUrlTo = openUrlTo;
	
	$$.openUrlTo = openUrlTo;
};

//загружает URL на какой-то последний видимый элемент, selectorStart - селектор элемента
function openUrl( url, selectorStart )
{
	openUrlPos( url, selectorStart, "last" );
}

//загружает URL на видимый элемент
//selectorStart - селектор
//pos - 'last' - последний видимый, отр. число - отступ от конца массива найденных элементов
function openUrlPos( url, selectorStart, pos )
{
	var result = getAJAXHtml( url );
	if( result )
	{
		if( pos == "last" )
	    {
	    	$( selectorStart + ':visible:last' ).html( result );
	    }
	    else if( pos < 0 )
	    {
	    	var $select = $( selectorStart + ":visible" );
	    	$select.eq( $select.length + pos - 1 ).html( result );
	    }
	}
	return result;
}

//загружает URL на элемент
//selector - селектор
function openUrlTo( url, $selector, vars )
{
	var result = undefined;
	if( vars )
	{		
		result = getAJAXHtml( url, vars.toPostNames );
	}
	else
	{
		result = getAJAXHtml( url );
	}
		
	if( result )
	{
		if( vars && vars.replace )
		{
			$selector.replaceWith( result );
		}
		else if( vars && vars.append )
		{
			$selector.append( result );
		}
		else
		{
			$selector.html( result );
		}
	}
	return result;
}

/*function openUrlToAsync(url, $selector, vars) {
	var time = window.performance.now();
	
	bgcrm.debug('openUrl', "openUrlToAsync", url);
	
	getAJAXHtmlAsync(url, vars ? vars.toPostNames : {}, function (result) {
		if (vars && vars.replace) {
			$selector.replaceWith(result);
		} else if (vars && vars.append) {
			$selector.append(result);
		} else {
			$selector.html( result );
		}
		
		if (vars && vars.then)
			vars.then();

		bgcrm.debug('openUrl', "openUrlToAsync", url, window.performance.now() - time);
	});
}*/

//загружает URL на предка элемента, фактически перетирая элемент
//selector - селектор
function openUrlToParent( url, $selector )
{
	// может быть так, что к данному моменту объекта уже нет 
	if( $selector.length > 0 )
	{
		var $parent = $($selector[0].parentNode);
		$parent.html("");
				
		var result = getAJAXHtml( url );
		if( result )
		{
			$parent.html( result );
		}
	}
}

// replace to bgerp.ajax.openUrlTo
function openUrlToParentAsync( url, $selector )
{
	// может быть так, что к данному моменту объекта уже нет 
	if( $selector.length > 0 )
	{
		var time = window.performance.now();
		
		bgcrm.debug( 'openUrl', "openUrlToParentAsync", url );
        		
		var $parent = $($selector[0].parentNode)
		
		/* По неведомой причине, если очистить предварительно элемент, то не выскакивают предупреждения о слишком долгом выполнении скрипта в FF,
		 * в случае загрузки на на контейнер $parent содержимого второй раз.
		 * Возможно, причина в том, что при затирании старого содержимого долго удаляются различные слушатели с элементов и выполнение 
		 * onLoad страницы становится слишком долгим.
		 * Выяснено при оптимизации графика дежурств. Проблема возникала, если в $parent уже был загружен график и нажимали "Вывести" повторно.
		 * Асинхронным вызов сделан для пущей правильности, помогало и с синхронным вариантом. 
		 * Ускорение времени от предварительной очистки если есть, то немного, а окошко выскакивать перестало.
		 */		 
		$parent.html( "" );
		
		getAJAXHtmlAsync( url, {}, function( response )
		{
			$parent.html( response );
			
			bgcrm.debug( 'openUrl', "openUrlToParentAsync", url, window.performance.now() - time );
		});
	}
}


//отправка AJAX с результатом HTML страница
function getAJAXHtml( url, toPostNames )
{
	var result = false;
	
	var separated = separatePostParams( url, toPostNames, false );
	
	$.ajax({ 
		type: "POST",
		url: separated.url,
		data: separated.data,
		async: false,
		success: function( response ) 
		{
			result = response;
		},
		error: function( jqXHR, textStatus, errorThrown )
		{
			onAJAXError( separated.url, jqXHR, textStatus, errorThrown );
		}
	});
	
	return result;
}
	
function getAJAXHtmlAsync( url, toPostNames, success )
{
	var result = false;
	
	var separated = separatePostParams( url, toPostNames, false );	
	
	$.ajax({ 
		type: "POST",
		url: separated.url,
		data: separated.data,
		success: success,
		error: function( jqXHR, textStatus, errorThrown )
		{
			onAJAXError( separated.url, jqXHR, textStatus, errorThrown );
		}
	});
	
	return result;
}

function sendAJAXCommandAsync( url, toPostNames, callback, control, timeout, callbackError )
{
	lock( control );
	
	var separated = separatePostParams( url, toPostNames, true );
		
	$.ajax({
		type: "POST",
		async: true,
		url: separated.url,
		data: separated.data,
		dataType: "json",
		success: function( data )
		{
			var result = checkAJAXCommandResult( data );
			if (callback) {
				callback( result );
			}
			unlock( control, timeout );
		},
		error: function( jqXHR, textStatus, errorThrown ) 
		{
			onAJAXError( separated.url, jqXHR, textStatus, errorThrown );
			if (callbackError) {
				callbackError();
			}
			unlock( control, timeout );
		}	
	});
}

//отправка AJAX команды c JSON ответом определённого формата
function sendAJAXCommand( url, toPostNames )
{
	var result = false;
	
	var separated = separatePostParams( url, toPostNames, true );
		
	$.ajax({
		type: "POST",
		async: false,
		url: separated.url,
		data: separated.data,
		dataType: "json",
		success: function( data )
		{
			result = checkAJAXCommandResult( data );		
		},
		error: function( jqXHR, textStatus, errorThrown )
		{
			onAJAXError( separated.url, jqXHR, textStatus, errorThrown );
		}
	});
	
	return result;
}

//аналог предыдущей функции, за исключением, что для URL можно указывать параметры из хэша
function sendAJAXCommandWithParams( url, requestParams )
{
	return sendAJAXCommand( url + requestParamsToUrl( requestParams ) );
}

//перенос в POST часть запроса определённых в массиве toPostNames параметров запроса либо начинающихся
//с благославенного имени data
function separatePostParams(url, toPostNames, json) {
	let data = "";
	
	let dataStartPos = 0;
	
	// перемещает параметр в тело POST запроса
	const move = function () {
		let dataEndPos = url.indexOf( "&", dataStartPos + 1 );
		if (dataEndPos <= 0)
			dataEndPos = url.length;
		
		var length = dataEndPos - dataStartPos;
		
		data += url.substr(dataStartPos, length);
		url = url.substr(0, dataStartPos) + url.substr(dataEndPos, url.length);
	};
	
	// все переменные, могущие содержать большой объём данных должны начинаться с data
	// перенос их в тело запроса
	while ((dataStartPos = url.indexOf("&data")) > 0) 
		move();
	
	// все переменные, имя которых есть в toPostNames тоже переносим в post запрос
	if (toPostNames) {
		for (index in toPostNames) {
			dataStartPos = 0;			
			while ((dataStartPos = url.indexOf("&" + toPostNames[index] + "=")) > 0) 
				move();
		}
	}
	
	// странный параметр, убрать
	if (json) {
		if (url.indexOf( "?" ) > 0)
			url += "&responseType=json";
		else
			url += "?responseType=json";
	}
	
	return {"url" : url, "data" : data};
}

function onAJAXError( url, jqXHR, textStatus, errorThrown )
{
	if( jqXHR.status == 401 )
	{
		showLoginPopup( jqXHR.responseText );
	}
	else if( jqXHR.status == 500 )
	{
		showErrorDialog( jqXHR.responseText );
	}	
	else
	{
		alert( "При открытии адреса " + url + " произошла ошибка: " + errorThrown );
	}
}

function checkAJAXCommandResult( data )
{
	var result = false;
	
	//TODO: Убрать поддержку статуса 'message', отнести его к ошибкам.
	if( data.status == 'ok' || data.status == 'message' )
	{
		result = data;
		// обработка событий на обновления в интерфейсе
		for( var i = 0; i < data.eventList.length; i++ ) 
		{
			if( data.eventList[i] != null)
			{
				processEvent( data.eventList[i] );	
			}
		}
		
		if( data.message )
		{
			alert( data.message );
		}
	}
	else
	{
		var message = undefined;
		
		// старый формат
		if( data.error )
		{
			message = data.error;
		}
		// новый формат
		else				
		{
			message = data.message;
		}
		
		alert( "Ошибка: " + message );
		
		// обработка событий на обновления в интерфейсе
		for( var i = 0; i < data.eventList.length; i++ ) 
		{
		    processEvent( data.eventList[i] );
		}
	}
	
	return result;
}

function requestParamsToUrl( requestParams, subParam )
{
	var url = "";
	for( var k in requestParams )
	{
		url += "&";
		if( subParam )
		{
			url += subParam + "(";
		}
		url += encodeURIComponent( k );
		if( subParam )
		{
			url += ")";
		}
		url += "=" + encodeURIComponent( requestParams[k] ); 
	}
	return url;
}

//генерирует URL строку на основании введённых в форму параметров
function formUrl( forms, excludeParams )
{
	if( forms instanceof HTMLFormElement )
	{
		forms = [ forms ];
	}
	
	var commonUrl = "";
	
	for( var k = 0; k < forms.length; k++ )
	{
		var form = forms[k];
		
		var url = $(form).attr('action');
		var params =  $(form).serializeAnything( excludeParams );
		if( params.length > 0 )
		{
			if( commonUrl.indexOf( '?' )  > 0 || url.indexOf( '?' ) > 0 )
			{
				url += "&" + params;
			}
			else
			{
				url += "?" + params;
			}
		}
		
		// удаление параметров page.
		for( var i = 0; i < form.length; i++ )
		{
			var el = form.elements[i];
			if( el.name == 'page.pageIndex' )
			{
				el.value = 1;
			}
			else if( el.name.indexOf( "page." ) == 0 )
			{
				form.removeChild( el );
				i--;
			}
		}
		
		if( commonUrl.length > 0 )
		{
			commonUrl += "&";
		}
		commonUrl += url;
	}
		
	return commonUrl;
}

function openUrlContent( url )
{
	openUrlTo( url, bgerp.shell.$content());
}

//загружает URL на последнюю открытую вкладку
function openTabUrl( url ) 
{
	console.warn( "Use openUrlContent instead openTabUrl!" );
	openUrlContent( url );
}

//загружает URL не на последнюю открытую вкладку а с неким отрицательным отступом в иерархии вкладок
function openTabUrlPos( url, pos ) 
{
	console.error( "Function openTabUrlPos is incorrect!" );
	openUrlPos( url, ".ui-tabs-panel", pos );
}

