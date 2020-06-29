/*
 * Оболочка обычного пользовательского интерфейса (/user).
 */
$$.shell = new function () {
	const debug = $$.debug("shell");

	const pushHistoryState = function (href) {
		if (!history.state || history.state.href != href) {
			debug("pushHistoryState: ", href, "; current: ", history.state);
			history.pushState({href: href}, null, href);
		}
	}

	/**
	 * Follow the link with preventing default handling.
	 */
	const followLink = function (href, event) {
		debug("followLink: ", href, event);
		contentLoad(href);
		if (event)
			event.preventDefault();
	}

	const getCommandDiv = function (command, closable) {
		var $commandDiv = $("body > #content > div#" + command );

		if (closable) {
			$("body > #content > div[id!='" + command + "']").hide();
			$commandDiv.show();

			// процесс уже был открыт, если нет открытых с классом editorStopReload редакторов - то перезагрузка
			if(command.match( /process(\-*\d+)/ ) &&
			   $commandDiv.find( ".editorStopReload:visible" ).length == 0) {
				removeCommandDiv( command );
				$commandDiv = $();
			}
		}

		if ($commandDiv.length == 0) {
			$('body > #content').append(sprintf("<div id='%s'></div>", command));
			$commandDiv = $("body > #content > div#" + command);

			var divTemplate =
				"<div id='%s' class='status'>\
					<div class='wrap'>\
						<div class='left'>\
							<div class='title'>\
								<h1 class='title' title='Refresh'></h1>";
			if (closable) {
				divTemplate +=
					"<span class='icon-close' title='Close'></span>";
			}

			divTemplate += "\
							</div>\
						</div>\
						<div class='center'>\
							<h1 class='state'></h1>\
						</div>\
					</div>\
				</div>";

			$('#title #empty').after(sprintf(divTemplate, command));

			$commandDiv.isNew = true;
		}

		debug("getCommandDiv", $commandDiv.isNew, $commandDiv);

		$("body > #content > div[id!='" + command + "']").hide();
		$commandDiv.show();

		$("#title > div.status[id!='" + command + "']").hide();
		$("#title > div#" + command ).show();

		return $commandDiv;
	}

	const onCommandDivShow = function ($commandDiv) {
		// вызов onShow обработчика, если оснастка его повесила
		var onShow = $commandDiv.data('onShow');
		if (onShow) {
			debug("call onShow");
			onShow();
		}
	}

	const removeCommandDiv = function (command) {
		 $("body > #content > div#" + command ).remove();
		 $("#title > div#" + command ).remove();
		 $('#objectBuffer ul li[value=' + command +']').remove();
		 updateBufferCount();
	}

	const getBufferCount = function () {
		return $('#objectBuffer ul li:not([style*="display: none"])').length;
	}

	const updateBufferCount = function () {
		$("#objectBuffer .object-count").text(getBufferCount());
	}

	// next contentLoad starts only after previous is done
	let contentLoadDfd;

	const contentLoad = function (href, reopen, pinned) {
		debug("contentLoad", href, contentLoadDfd);
		
		const contentLoadCurrentDfd = contentLoadDfd;
		const contentLoadNewDfd = $.Deferred();
		$.when(contentLoadCurrentDfd).done(() => {
			contentLoadAsync(href, reopen, pinned, contentLoadNewDfd);
		});
		contentLoadDfd = contentLoadNewDfd;

		return contentLoadDfd.promise();
	}

	const contentLoadAsync = function (href, reopen, pinned, contentLoadDfd) {
		debug("contentLoadAsync: ", href, contentLoadDfd);

		// удаление протокола, хоста, порта, если есть
		// может прийти при восстановлении адресной строки
		let pos = href.indexOf('//');
		let command = pos >= 0 ? href.substring(href.indexOf('/', pos + 2)) : href;

		// дополнение /user/
		if (!command.startsWith("/user/"))
			command = "/user/" + command;

		pos = command.indexOf('#');
		const commandBeforeSharp = pos > 0 ? command.substring(0, pos) : command;
		const commandId = pos > 0 ? "&id=" + command.substring(pos + 1) : "";

		var item = menuItems[commandBeforeSharp];
		// открытие оснастки меню
		if (item) {
			// берём после префикса /user , для сохранения обратной совместимости
			const id = commandBeforeSharp.substring(6).replace(/\//g, "-");

			if (!item.allowed)
				alert("The menu item is now allowed.");
			else {
				let $taskButton = $('#taskPanel > div#' + id);

				if ($taskButton.length == 0) {
					$('#taskPanel')
						.append(sprintf("<div class='btn-blue btn-task-active' id='%s' title='%s'><span class='title'>%s</span>" +
							(pinned ? "" : "<span class='icon-close'><img src='/images/cross-white.png'/></span>") + "</div>",
							id, item.title, item.title));
					$taskButton = $('#taskPanel > div#' + id);

					var $commandDiv;

					$taskButton.data("href", href);

					$taskButton.click(function () {
						$commandDiv = getCommandDiv(id);

						if (typeof $$.closeObject == 'function')
							$$.closeObject();

						$("#taskPanel div[id!='" + id + "']")
							.removeClass( "btn-task-active btn-blue" ).addClass( "btn-white btn-task" )
							.find('img').attr('src','/images/cross.png');
						$("#taskPanel div#" + id)
							.removeClass( "btn-white btn-task" ).addClass( "btn-task-active btn-blue" )
							.find('img').attr('src','/images/cross-white.png');

						$(window).scrollTop($("#taskPanel div#" + id).attr('scroll'));

						pushHistoryState(href);

						onCommandDivShow($commandDiv);
					});

					$taskButton.click();

					$$.ajax.load(item.action + commandId, $commandDiv, { dfd: contentLoadDfd });

					$taskButton.find('span.icon-close').click( function () {
						// закрытие активной оснастки
						if ($taskButton.hasClass( "btn-task-active")) {
							// последняя неактивная кнопка становится активной
							var $inactiveButtons =  $("#taskPanel > div.btn-task");
							if ($inactiveButtons.length > 0)
								$inactiveButtons[$inactiveButtons.length - 1].click()
							else
								$("#title > #empty").show();
						}
						$taskButton.remove();
						removeCommandDiv(id);
					});
				}
				else {
					if (reopen) {
						const $commandDiv = getCommandDiv(id);
						$$.ajax.load(item.action + commandId, $commandDiv, { dfd: contentLoadDfd });

						const state = history.state;
						history.replaceState(state, null, href);

						$taskButton.data("href", href);
					} else {
						$('#taskPanel #' + id).click();
						contentLoadDfd.resolve();
					}
				}
			}
		}
		// открытие контрагентов и др. объектов
		else {
			var m = null;
			var url = null;

			var bgcolor = "";
			var objectId = 0;

			// открытие контрагента
			if ((m = href.match(/.*customer#(\d+)/)) != null) {
				url = "/user/customer.do?id=" + m[1];
				bgcolor = "#A1D0C9";
			}
			// открытие процесса
			else if ((m = href.match(/.*process#(\-*\d+)/)) != null) {
				url = "/user/process.do?id=" + (objectId = m[1]);
				if (objectId < 0)
					url += "&wizard=1";
				bgcolor = "#E6F7C0";
			}
			// профиль пользователя
			else if ((m = href.match(/.*profile#(\d+)/)) != null) {
				url = "/user/profile.do?action=getUserProfile&userId=" + m[1];
				bgcolor = "#C3A8D5";
			}
			// плагины
			else {
				const mapping = $$.shell.mapUrl(href)
				if (mapping) {
					url = mapping.url;
					bgcolor = mapping.bgcolor;
				}
			}

			const maxObjectsInBuffer = $$.pers['iface.buffer.maxObjects'] || 15;

			// 1 - последний добавляется сверху, нижние удаляются,
			// 2 - последний добавляется внизу либо на своё предшествующее место, первые удаляются
			const bufferBehavior = $$.pers['iface.buffer.behavior'] || 1;

			if (url) {
				// берём после префикса /user , для сохранения обратной совместимости
				const id = command.substring(6).replace("#", "-");

				$("#taskPanel div.btn-task-active").attr('scroll',$(window).scrollTop());
				$("#taskPanel div")
					.removeClass("btn-task-active btn-blue").addClass("btn-white btn-task")
					.find('img').attr('src','/images/cross.png');

				var $commandLi = $(sprintf("#objectBuffer ul>li[value='%s']", id));
				if ($commandLi.length) {
					if (bufferBehavior == 1)
						$commandLi.remove();
					else
						$commandLi.css( "display", "none" );
				}

				if (bufferBehavior == 1) {
					$('#objectBuffer ul>li:gt(' + maxObjectsInBuffer + ')').each(function () {
						removeCommandDiv($(this).attr("value"));
						$(this).remove();
					})
				} else {
					while (getBufferCount() > maxObjectsInBuffer) {
						var $li = $("#objectBuffer ul li:first");
						removeCommandDiv($li.attr("value"));
						$li.remove();
					}
				}

				var currentOpened = $("body > #content > div:visible").attr("id");

				var $commandDiv = getCommandDiv(id, true);

				// если это не повторное открытие того же объекта
				if ($commandDiv.attr( "id" ) != currentOpened) {
					pushHistoryState(command);

					onCommandDivShow($commandDiv);

					// если открыт уже какой-то объект - перемещение его в буфер
					if (typeof $$.closeObject == 'function')
						$$.closeObject();

					updateBufferCount();

					// функция перемещения текущего объекта в буфер
					$$.closeObject = function () {
						var liCode = sprintf("<li style='border-left: 8px solid %s;' value='%s'>%s</li>", bgcolor, id,
											 "<span class='icon-close'></span>" + $("#title #" + id + " h1.title").html());

						if (bufferBehavior == 1)
							$('#objectBuffer ul').prepend( liCode );
						else {
							var $li = $('#objectBuffer ul>li[value="' + id + '"]');
							if ($li.length)
								$li.replaceWith(liCode);
							else
								$('#objectBuffer ul').append(liCode);
						}

						var $commandLi = $('#objectBuffer ul>li[value="' + id + '"]');

						$commandLi.one("click", function (event) {
							contentLoad(href);
							$('#objectBuffer > ul').hide();
							return false;
						});

						$commandLi.find(".icon-close").one("click", function (event) {
							removeCommandDiv(id);
							$commandLi.remove();
							updateBufferCount();
							event.stopPropagation();
							return false;
						});

						updateBufferCount();

						$$.closeObject = null;
					}

					$(window).scrollTop(0);
				}

				if ($commandDiv.isNew) {
					$$.ajax.load(url, $commandDiv, { dfd: contentLoadDfd }).done(() => {
						$("#title > .status:visible > .wrap > .left > .title .icon-close")
							.one("click", function () {
								if (objectId < 0)
									alert("Объект не инициализирован до конца, его невозможно закрыть.");
								else {
									removeCommandDiv(id);

									$$.closeObject = null;
									window.history.back();
								}
							});

						$("#title > .status:visible > .wrap > .left > .title h1.title")
							.on("click", function () {
								$$.ajax.load(url, $commandDiv);
							});
					});
				} else
					contentLoadDfd.resolve();
			} else
				contentLoadDfd.resolve();
		}
	}

	const initBuffer = function () {
		window.addEventListener("popstate", function (e) {
			// при переходе по #UNDEF ссылкам e.state=null
			if (e.state) {
				debug("popstate: ", e.state);
				contentLoad(e.state.href);
			}
			else {
				//В Chrome выдаёт ошибку.
				//alert( 'Открыта некорректная ссылка, сообщите место её нахождения разработчикам!' );
			}
		}, false);

		// заглушка с пустой функцией стопа таймера
		let popupObjectBuffer = {
			stopTimer: function () {}
		};

		if ($$.pers["iface.buffer.openOnLongPress"] || 1 === 1)	{
			const debug = $$.debug("buffer");

			var $buffer = $("#objectBuffer");
			var $bufferDrop = $("#objectBuffer > ul.drop");

			popupObjectBuffer =  {
				startTimer: function (event) {
					// буфер уже отображается
					if ($bufferDrop.is(":visible"))
						return;

					debug('Start timer', event);

					window.clearTimeout(this.timer);

					this.timer = window.setTimeout(function () {
						popupObjectBuffer.showObjectBuffer(event);
					}, 500);
				},

				stopTimer: function () {
					window.clearTimeout(this.timer);
					debug('Stop timer', popupObjectBuffer);
				},

				showObjectBuffer: function (e) {
					debug('Show buffer', popupObjectBuffer);

					// отображать только непустой буфер
					if (getBufferCount() > 0) {
						$buffer.css("position", "static")

						$bufferDrop
							.css("position","absolute")
							.css("left", e.clientX)
							.css("top", e.clientY)
							.show();
					}

					var closeBuffer = function (e) {
						// неоднократно могут вызываться при очистке буфера
						if ($buffer.find(e.target).length <= 0) {
							debug('Hide buffer');

							$buffer.css("position", "relative");

							var position = $bufferDrop.position();
							var width = $bufferDrop.width();
							var height = $bufferDrop.height();

							$bufferDrop
								.css('display', 'none')
								.css('position', '' )
								.css("z-index", '')
								.css("left", '')
								.css("top", '');

							// эти загадочные манипуляции исправляют артефакты отрисовки в Хроме при сокрытии буфера
							$('<div></div>')
								.css('position', 'absolute')
								.css('top', position.top)
								.css('left', position.left)
								.width( width )
								.height( height )
								.appendTo( $('<body>') )
								.remove();
						}
						else {
							$(window).one("mousedown", function (e) {
								closeBuffer( e );
							});
						}
					};

					$(window).one("mousedown", function (e) {
						closeBuffer( e );
					});
				}
			}

			$(window).mousedown(function (e) {
				if (e.which == 1 && !$(e.target).prop("draggable")) {
					if (e.target.nodeName == 'A' ||
						e.target.nodeName == 'BUTTON' ||
						e.target.nodeName == 'INPUT' ||
						e.target.nodeName == 'TEXTAREA') {
						return;
					}
					popupObjectBuffer.startTimer(e);
				}
			});

			$(window).mouseup(function (e) {
				if (e.which == 1)
					popupObjectBuffer.stopTimer();
			});

			// препятствие открытию буфера при выделении текста
			$(window).mousemove(function (e) {
				// вместо вызова stopTimer, т.к. очень много отладки идёт
				window.clearTimeout(popupObjectBuffer.timer);
			});
		}
	}

	const stateFragment = (id) => {
		var state = history.state;
		if (id > 0 && state) {
			var pos = state.href.indexOf('#');
			state.href = (pos < 0 ? state.href : state.href.substring(0, pos)) + "#" + id;
			history.replaceState(state, null, state.href)
		}
	}

	const $content = function () {
		return $('#content > div:visible');
	}

	// public functions
	this.debug = debug;
	this.initBuffer = initBuffer;
	this.contentLoad = contentLoad;
	this.followLink = followLink;
	this.removeCommandDiv = removeCommandDiv;
	this.stateFragment = stateFragment;
	this.$content = $content;
}

function contentLoad (href) {
	console.warn($$.deprecated);
	$$.shell.contentLoad(href);
}