// "use strict";

$$.ajax = new function() {
	const debug = $$.debug("ajax");
	
	/**
	 * Sends AJAX response and returns a promise.
	 * url - string, or form, or $(form)
	 * options.toPostNames - array of names of POST body parameters
	 * options.html = true - treat result as HTML 
	 * By default the promise is processed by checkResponse() function.
	 */
	const post = (url, options) => {
		debug("post", url);

		options = options || {};

		const separated = separatePostParams(url, options.toPostNames, !options.html);
		
		const def = $.Deferred();

		$.ajax({ 
			type: "POST",
			url: separated.url,
			data: separated.data,
		}).fail(function (jqXHR, textStatus, errorThrown) {
			onAJAXError(separated.url, jqXHR, textStatus, errorThrown);
			def.reject();
		}).done((data) => {
			if (!options.html) {
				if (checkResponse(data))
					def.resolve(data);
				else
					def.reject();
			}
			else
				def.resolve(data);
		});

		return def.promise();
	}
	
	/* 
	 * Default - send request and set result HTML on element. 
	 * options.replace - replace element by HTML.
	 * options.append  - append HTML into the element.
	 */
	const load = (url, $selector, options) => {
		debug("load", url, $selector);

		options = options || {};
		options.html = true;
		
		if (typeof $selector === 'string')
			$selector = $($selector);
		
		// erasing of existing value, speeds up load process significantly in some cases
		// the reason is not clear, was found in callboard, probably because of removing of onLoad listeners
		if (!options || (!options.replace && !options.append))
			$selector.html("");
		
		return post(url, options).done(function (result) {
			if (options.replace)
				$selector.replaceWith(result);
			else if (options.append)
				$selector.append(result);
			else
				$selector.html(result);
		});
	}

	/** 
	 * Перенос в POST часть запроса определённых в массиве toPostNames параметров запроса 
	 * либо начинающихся с благославенного префикса data
	 */
	const separatePostParams = function (url, toPostNames, json) {
		url = formUrl(url);

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
		}
		
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

		if (json) {
			if (url.indexOf( "?" ) > 0)
				url += "&responseType=json";
			else
				url += "?responseType=json";
		}
		
		return {"url" : url, "data" : data};
	}

	/**
	 * Checks AJAX response.
	 * @param {*} data param and values
	 */ 
	const checkResponse = function (data) {
		var result = false;

		//TODO: Убрать поддержку статуса 'message', отнести его к ошибкам.
		if (data.status == 'ok' || data.status == 'message') {
			result = data;
			// обработка событий на обновления в интерфейсе
			for (var i = 0; i < data.eventList.length; i++) {
				if (data.eventList[i] != null) {
					processEvent(data.eventList[i]);
				}
			}

			if (data.message) {
				alert(data.message);
			}
		}
		else {
			var message = undefined;

			// старый формат
			if (data.error) {
				message = data.error;
			}
			// новый формат
			else {
				message = data.message;
			}

			alert("Ошибка: " + message);

			// обработка событий на обновления в интерфейсе
			for (var i = 0; i < data.eventList.length; i++) {
				processEvent(data.eventList[i]);
			}
		}

		return result;
	}

	/**
	 * Builds URL string from form.
	 * @param {*} param string with ready URL or form's selector or form itself
	 * @param {*} excludeParams skipping params
	 */
	const formUrl = function (param, excludeParams) {
		if (typeof param === 'string')
			return param;

		let forms = param; 

		if (forms instanceof HTMLFormElement) {
			forms = [forms];
		}

		var commonUrl = "";

		for (var k = 0; k < forms.length; k++) {
			var form = forms[k];

			var param = $(form).attr('action');
			var params = $(form).serializeAnything(excludeParams);
			if (params.length > 0) {
				if (commonUrl.indexOf('?') > 0 || param.indexOf('?') > 0) {
					param += "&" + params;
				}
				else {
					param += "?" + params;
				}
			}

			// удаление параметров page.
			for (var i = 0; i < form.length; i++) {
				var el = form.elements[i];
				if (el.name == 'page.pageIndex') {
					el.value = 1;
				}
				else if (el.name.indexOf("page.") == 0) {
					form.removeChild(el);
					i--;
				}
			}

			if (commonUrl.length > 0) {
				commonUrl += "&";
			}
			commonUrl += param;
		}

		return commonUrl;
	}

	/**
	 * Builds URL from key-value pairs.
	 * @param {*} requestParams key value param pairs
	 * @param {*} subParam NO IDEA
	 */
	const requestParamsToUrl = function (requestParams, subParam) {
		let url = "";
		for (const k in requestParams) {
			url += "&";
			if (subParam) {
				url += subParam + "(";
			}
			url += encodeURIComponent(k);
			if (subParam) {
				url += ")";
			}
			url += "=" + encodeURIComponent(requestParams[k]); 
		}
		return url;
	}

	// доступные функции
	this.post = post;
	this.load = load;
	this.checkResponse = checkResponse;
	this.formUrl = formUrl;
	this.requestParamsToUrl = requestParamsToUrl;
	// временно доступные
	this.separatePostParamsInt = separatePostParams;
}

//загружает URL на какой-то последний видимый элемент, selectorStart - селектор элемента
function openUrl( url, selectorStart )
{
	console.warn($$.deprecated);

	openUrlPos( url, selectorStart, "last" );
}

//загружает URL на видимый элемент
//selectorStart - селектор
//pos - 'last' - последний видимый, отр. число - отступ от конца массива найденных элементов
function openUrlPos( url, selectorStart, pos )
{
	console.warn($$.deprecated);

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
	console.warn($$.deprecated);

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

//загружает URL на предка элемента, фактически перетирая элемент
//selector - селектор
function openUrlToParent( url, $selector )
{
	console.warn($$.deprecated);

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

// replace to $$.ajax.load
function openUrlToParentAsync( url, $selector )
{
	console.warn($$.deprecated);

	// может быть так, что к данному моменту объекта уже нет 
	if( $selector.length > 0 )
	{
		var time = window.performance.now();
		
		$$.debug( 'openUrl', "openUrlToParentAsync", url );
				
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
			
			$$.debug( 'openUrl', "openUrlToParentAsync", url, window.performance.now() - time );
		});
	}
}


//отправка AJAX с результатом HTML страница
function getAJAXHtml( url, toPostNames )
{
	console.warn($$.deprecated);

	var result = false;
	
	var separated = $$.ajax.separatePostParamsInt(url, toPostNames);
	
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
	console.warn($$.deprecated);

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
	console.warn($$.deprecated);

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
			var result = $$.ajax.checkResponse( data );
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
	console.warn($$.deprecated);

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
			result = $$.ajax.checkResponse( data );		
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
	console.warn($$.deprecated);

	return sendAJAXCommand( url + requestParamsToUrl( requestParams ) );
}

//перенос в POST часть запроса определённых в массиве toPostNames параметров запроса либо начинающихся
//с благославенного имени data
function separatePostParams(url, toPostNames, json) {
	console.warn($$.deprecated);

	return $$.ajax.separatePostParamsInt(url, toPostNames, json);
}

// move to $$.ui
function onAJAXError( url, jqXHR, textStatus, errorThrown )
{
	if( jqXHR.status == 401 )
	{
		showLoginPopup( jqXHR.responseText );
	}
	else if( jqXHR.status == 500 )
	{
		console.error("AJAX error, URL: ", url);
		showErrorDialog( jqXHR.responseText );
	}
	else
	{
		alert( "При открытии адреса " + url + " произошла ошибка: " + errorThrown );
	}
}

function checkAJAXCommandResult( data )
{
	console.warn($$.deprecated);

	return $$.ajax.checkResponse(data);
}

function requestParamsToUrl( requestParams, subParam )
{
	console.warn($$.deprecated);

	return $$.ajax.requestParamsToUrl(requestParams, subParam);
}

//генерирует URL строку на основании введённых в форму параметров
function formUrl( forms, excludeParams )
{
	console.warn($$.deprecated);

	return $$.ajax.formUrl(forms, excludeParams);
}

function openUrlContent( url )
{
	console.warn($$.deprecated);

	$$.ajax.load(url, $$.shell.$content());
}

