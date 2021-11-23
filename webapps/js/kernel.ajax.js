// "use strict";

$$.ajax = new function () {
	const debug = $$.debug("ajax");

	const trim100 = (value) => {
		if (typeof value !== "string")
			return value;
		return value.length > 100 ? value.substr(0, 100) + "..." : value;
	}

	/**
	 * Sends AJAX response and returns a promise.
	 * url - string, or form, or $(form)
	 * options.toPostNames - array of names of POST body parameters
	 * options.html = true - treat result as HTML
	 * options.control - button to add there progress spinner
	 * options.failAlert = false - do now show alert on failing promise
	 * By default the promise is processed by checkResponse() function.
	 */
	const post = (url, options) => {
		debug("post", trim100(url));

		options = options || {};

		const separated = separatePostParams(url, options.toPostNames, !options.html);

		const form = getForm(url);
		if (form) {
			$(form).find("input").removeClass("error");
		}

		// handling process spinner
		let requestDone = () => {};
		if (options.control) {
			options.control.disabled = true;

			const $control = $(options.control);
			$control.prepend("<span class='progress'><i class='progress-icon ti-reload'></i></span>");
			const $progress = $control.find(">.progress");

			requestDone = () => {
				options.control.disabled = false;
				$progress.remove();
			}
		}

		const def = $.Deferred();

		$.ajax({
			type: "POST",
			url: separated.url,
			data: separated.data,
		}).fail(function (jqXHR, textStatus, errorThrown) {
			requestDone();
			if (options.failAlert !== false)
				onAJAXError(separated.url, jqXHR, textStatus, errorThrown);
			def.reject();
		}).done((data) => {
			requestDone();
			if (!options.html) {
				if (checkResponse(data, form))
					def.resolve(data);
				else
					def.reject();
			} else
				def.resolve(data);
		});

		return def.promise();
	}

	let loadCnt = 0;
	let loadDfd;

	const getLoadDfd = () => {
		return loadDfd ? loadDfd : {
			resolve: () => {}
		}
	}

	/*
	 * Default - send request and set result HTML on element.
	 * options.dfd - deferred, being resolved after all onLoad JS on chained loads are done.
	 * options.replace - replace element by HTML, deprecated.
	 * options.append  - append HTML into the element, deprecated.
	 * options.control - will be passed to 'post' function.
	 */
	const load = (url, $selector, options) => {
		debug("load", trim100(url), $selector);

		options = options || {};
		options.html = true;

		if (typeof $selector === 'string')
			$selector = $($selector);

		// erasing of existing value, speeds up load process significantly in some cases
		// the reason is not clear, was found in callboard, probably because of removing of onLoad listeners
		// !!! the erasing was disabled because of problems with generation URL from form elements, which already were gone

		// parameter runs cascaded load
		let dfd = options.dfd;
		if (dfd) {
			loadDfd = {
				key: "dfd",

				/** Wrapping object, contains Deffered + URL for debug. */
				create: function (dfd, url) {
					const result = {
						dfd: dfd,
						url: trim100(url)
					};
					debug("Create wrap", result);
					return result;
				},

				resolve: function ($selector) {
					const dfd = $selector.data(loadDfd.key);

					const wait = [];
					$selector.find(".loader").each(function () {
						const subDfd = $(this).data(loadDfd.key);
						if (subDfd)
							wait.push(subDfd);
					})

					if (wait) {
						debug("Resolve when", dfd.url, wait);
						$.when.apply($, wait.map(el => el.dfd)).done(() => {
							debug("Resolve", dfd.url, wait);
							dfd.dfd.resolve();
						});
					} else {
						debug("Resolve", dfd.url);
						dfd.dfd.resolve();
					}
				}
			}

			dfd.done(() => {
				debug("Set loadDfd null", url);
				loadDfd = null;
			});
		}

		if (!loadDfd) {
			$selector.toggleClass("ajax-loading");
			return post(url, options).done((result) => {
				if (options.replace) {
					$selector.replaceWith(result);
				} else if (options.append) {
					$selector.append(result);
				} else {
					$selector.html(result);
				}
			}).always(() => {
				$selector.toggleClass("ajax-loading");
			});
		} else {
			const existingDfd = $selector.data(loadDfd.key);
			if (existingDfd) {
				if (existingDfd.state() === 'resolved') {
					debug("Existing resolved dfd", existingDfd, url);
					$selector.removeData(loadDfd.key);
				} else {
					console.error("Existing not resolved dfd", existingDfd, url);
					return existingDfd;
				}
			}

			if (!dfd)
				dfd = $.Deferred();

			$selector
				.addClass("loader")
				.toggleClass("ajax-loading")
				.data(loadDfd.key, loadDfd.create(dfd, url));

			post(url, options).done((result) => {
				debug("Done", trim100(url));

				const id = "load-" + (loadCnt++);
				const afterLoadScript =
					`<script id="${id}"> {
						const $selector = $("#${id}").parent();
						$(() => {
							$$.ajax.loadDfd().resolve($selector);
						});
					} </script>`;

				if (options.append) {
					$selector.append(result + afterLoadScript);
				} else {
					$selector.html(result + afterLoadScript);
				}
			}).always(() => {
				$selector.toggleClass("ajax-loading");
			});

			return dfd;
		}
	}

	/**
	 * Calls load with $selector $$.shell.$content()
	 * @param {*} url URL to be loaded.
	 * @param {*} obj DOM element, placed in the loaded area.
	 */
	const loadContent = (url, obj) => {
		return load(url, $$.shell.$content(obj));
	}

	/**
	 * Moves to POST part HTTP request parameters. Separated param names have to be placed in toPostNames or start from prefix 'data'.
	 * @param {*} url initial URL.
	 * @param {*} toPostNames array with POST params.
	 * @param {*} json object with properties url and data.
	 */
	const separatePostParams = function (url, toPostNames, json) {
		url = formUrl(url);

		let data = "";

		let dataStartPos = 0;

		// перемещает параметр в тело POST запроса
		const move = function () {
			let dataEndPos = url.indexOf("&", dataStartPos + 1);
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
			if (url.indexOf("?") > 0)
				url += "&responseType=json";
			else
				url += "?responseType=json";
		}

		return {"url": url, "data": data};
	}

	/**
	 * Checks AJAX response.
	 * @param {*} data param and values
	 * @param {*} form optional form object, to mark incorrect fields there
	 */
	const checkResponse = function (data, form) {
		var result = false;

		if (data.status == 'ok') {
			result = data;

			processClientEvents(data);

			if (data.message) {
				alert(data.message);
			}
		} else {
			const message = data.message;

			if (form) {
				const paramName = data.data && data.data.paramName;
				if (paramName) {
					const $input = $(form).find("input[name='" + paramName + "']");
					$input.addClass("error");
					$input[0].scrollIntoView();
				}
			}

			alert("Error: " + message);

			processClientEvents(data);
		}

		return result;
	}

	const processClientEvents = function (data) {
		for (var i = 0; i < data.eventList.length; i++) {
			if (data.eventList[i] != null) {
				processEvent(data.eventList[i]);
			}
		}
	}

	/**
	 * Get HTMLFormElement object if it has passed.
	 * @param {*} obj - array of forms, or a single form
	 * @return HTMLFormElement.
	 */
	const getForm = function (obj) {
		if (obj instanceof Array || obj instanceof jQuery)
			return obj[0];
		else if (obj instanceof HTMLFormElement)
			return obj;
		return null;
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
				} else {
					param += "?" + params;
				}
			}

			// удаление параметров page.
			for (var i = 0; i < form.length; i++) {
				var el = form.elements[i];
				if (el.name == 'page.pageIndex') {
					el.value = 1;
				} else if (el.name.indexOf("page.") == 0) {
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

	/**
	 * File upload.
	 * @param {*} formId hidden form's CSS ID.
	 * @param {*} iframeId hidden iframe's CSS ID.
	 * @param {*} complete callback function on upload is done.
	 */
	const upload = function (formId, iframeId, complete) {
		const $form = $('#' + formId);
		$form.iframePostForm({
			json: true,
			iframeID: iframeId,
			post: function () {
				if (!$form.find('input[type=file]').val()) {
					alert("Missing file!");
					return false;
				}
			},
			complete: function (response) {
				complete(response);
			}
		});
	}

	/**
	 * Manage all necessary events and listeners to make file upload.
	 * Should be added as CLICK EVENT on the triggering HTML element
	 *
	 * @param formId - ID of the form element
	 */
	const triggerUpload = function (formId) {
		const form = document.getElementById(formId);
		const inputFile = form.querySelectorAll('input[name=file]')[0];

		if (inputFile) {
			const onChange = function () {
				if (typeof form.requestSubmit === 'function') {
					form.requestSubmit();
				} else {
					form.submit();
				}
				form.reset();
				inputFile.onchange = function () {};
			};
			inputFile.onchange = onChange;
			inputFile.click();
		}
	}

	// public functions
	this.debug = debug;
	this.post = post;
	this.load = load;
	this.loadDfd = getLoadDfd;
	this.loadContent = loadContent;
	this.checkResponse = checkResponse;
	this.formUrl = formUrl;
	this.requestParamsToUrl = requestParamsToUrl;
	this.upload = upload;
	this.triggerUpload = triggerUpload;
	// deprecated
	this.separatePostParamsInt = separatePostParams;
}

//загружает URL на какой-то последний видимый элемент, selectorStart - селектор элемента
function openUrl(url, selectorStart) {
	console.warn($$.deprecated);

	openUrlPos(url, selectorStart, "last");
}

//загружает URL на видимый элемент
//selectorStart - селектор
//pos - 'last' - последний видимый, отр. число - отступ от конца массива найденных элементов
function openUrlPos(url, selectorStart, pos) {
	console.warn($$.deprecated);

	var result = getAJAXHtml(url);
	if (result) {
		if (pos == "last") {
			$(selectorStart + ':visible:last').html(result);
		} else if (pos < 0) {
			var $select = $(selectorStart + ":visible");
			$select.eq($select.length + pos - 1).html(result);
		}
	}
	return result;
}

//загружает URL на элемент
//selector - селектор
function openUrlTo(url, $selector, vars) {
	console.warn($$.deprecated);

	var result = undefined;
	if (vars) {
		result = getAJAXHtml(url, vars.toPostNames);
	} else {
		result = getAJAXHtml(url);
	}

	if (result) {
		if (vars && vars.replace) {
			$selector.replaceWith(result);
		} else if (vars && vars.append) {
			$selector.append(result);
		} else {
			$selector.html(result);
		}
	}
	return result;
}

//загружает URL на предка элемента, фактически перетирая элемент
//selector - селектор
function openUrlToParent(url, $selector) {
	console.warn($$.deprecated);

	// может быть так, что к данному моменту объекта уже нет
	if ($selector.length > 0) {
		var $parent = $($selector[0].parentNode);
		$parent.html("");

		var result = getAJAXHtml(url);
		if (result) {
			$parent.html(result);
		}
	}
}

// replace to $$.ajax.load
function openUrlToParentAsync(url, $selector) {
	console.warn($$.deprecated);

	// может быть так, что к данному моменту объекта уже нет
	if ($selector.length > 0) {
		var time = window.performance.now();

		$$.debug('openUrl', "openUrlToParentAsync", url);

		var $parent = $($selector[0].parentNode)

		/* По неведомой причине, если очистить предварительно элемент, то не выскакивают предупреждения о слишком долгом выполнении скрипта в FF,
		 * в случае загрузки на на контейнер $parent содержимого второй раз.
		 * Возможно, причина в том, что при затирании старого содержимого долго удаляются различные слушатели с элементов и выполнение
		 * onLoad страницы становится слишком долгим.
		 * Выяснено при оптимизации графика дежурств. Проблема возникала, если в $parent уже был загружен график и нажимали "Вывести" повторно.
		 * Асинхронным вызов сделан для пущей правильности, помогало и с синхронным вариантом.
		 * Ускорение времени от предварительной очистки если есть, то немного, а окошко выскакивать перестало.
		 */
		$parent.html("");

		getAJAXHtmlAsync(url, {}, function (response) {
			$parent.html(response);

			$$.debug('openUrl', "openUrlToParentAsync", url, window.performance.now() - time);
		});
	}
}


//отправка AJAX с результатом HTML страница
function getAJAXHtml(url, toPostNames) {
	console.warn($$.deprecated);

	var result = false;

	var separated = $$.ajax.separatePostParamsInt(url, toPostNames);

	$.ajax({
		type: "POST",
		url: separated.url,
		data: separated.data,
		async: false,
		success: function (response) {
			result = response;
		},
		error: function (jqXHR, textStatus, errorThrown) {
			onAJAXError(separated.url, jqXHR, textStatus, errorThrown);
		}
	});

	return result;
}

function getAJAXHtmlAsync(url, toPostNames, success) {
	console.warn($$.deprecated);

	var result = false;

	var separated = separatePostParams(url, toPostNames, false);

	$.ajax({
		type: "POST",
		url: separated.url,
		data: separated.data,
		success: success,
		error: function (jqXHR, textStatus, errorThrown) {
			onAJAXError(separated.url, jqXHR, textStatus, errorThrown);
		}
	});

	return result;
}

function sendAJAXCommandAsync(url, toPostNames, callback, control, timeout, callbackError) {
	console.warn($$.deprecated);

	lock(control);

	var separated = separatePostParams(url, toPostNames, true);

	$.ajax({
		type: "POST",
		async: true,
		url: separated.url,
		data: separated.data,
		dataType: "json",
		success: function (data) {
			var result = $$.ajax.checkResponse(data);
			if (callback) {
				callback(result);
			}
			unlock(control, timeout);
		},
		error: function (jqXHR, textStatus, errorThrown) {
			onAJAXError(separated.url, jqXHR, textStatus, errorThrown);
			if (callbackError) {
				callbackError();
			}
			unlock(control, timeout);
		}
	});
}

//отправка AJAX команды c JSON ответом определённого формата
function sendAJAXCommand(url, toPostNames) {
	console.warn($$.deprecated);

	var result = false;

	var separated = separatePostParams(url, toPostNames, true);

	$.ajax({
		type: "POST",
		async: false,
		url: separated.url,
		data: separated.data,
		dataType: "json",
		success: function (data) {
			result = $$.ajax.checkResponse(data);
		},
		error: function (jqXHR, textStatus, errorThrown) {
			onAJAXError(separated.url, jqXHR, textStatus, errorThrown);
		}
	});

	return result;
}

//аналог предыдущей функции, за исключением, что для URL можно указывать параметры из хэша
function sendAJAXCommandWithParams(url, requestParams) {
	console.warn($$.deprecated);

	return sendAJAXCommand(url + requestParamsToUrl(requestParams));
}

//перенос в POST часть запроса определённых в массиве toPostNames параметров запроса либо начинающихся
//с благославенного имени data
function separatePostParams(url, toPostNames, json) {
	console.warn($$.deprecated);

	return $$.ajax.separatePostParamsInt(url, toPostNames, json);
}

// move to $$.ui
function onAJAXError(url, jqXHR, textStatus, errorThrown) {
	if (jqXHR.status == 401) {
		showLoginPopup(jqXHR.responseText);
	} else if (jqXHR.status == 500) {
		console.error("AJAX error, URL: ", url);
		showErrorDialog(jqXHR.responseText);
	} else {
		alert("URL: " + url + ", error: " + errorThrown);
	}
}

function checkAJAXCommandResult(data) {
	console.warn($$.deprecated);

	return $$.ajax.checkResponse(data);
}

function requestParamsToUrl(requestParams, subParam) {
	console.warn($$.deprecated);

	return $$.ajax.requestParamsToUrl(requestParams, subParam);
}

//генерирует URL строку на основании введённых в форму параметров
function formUrl(forms, excludeParams) {
	console.warn($$.deprecated);

	return $$.ajax.formUrl(forms, excludeParams);
}

function openUrlContent(url) {
	console.warn($$.deprecated);

	$$.ajax.load(url, $$.shell.$content());
}
