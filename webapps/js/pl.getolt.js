/*
 * Plugin GetOLT
 */
'use strict';

$$.getolt = new function() {
	const debug = $$.debug('getolt');

	const REBOOT_COOLDOWN = 5 * 60 * 1000; // 5 minutes in ms

	const rebootTimers = {};
	let escapeListenerAdded = false;

	// Helper: find container from any child element (button click context)
	const containerOf = (el) => el.closest('.getolt-container');

	// Initialize reboot buttons within container
	const initRebootButtons = (ctx) => {
		const buttons = ctx.querySelectorAll('.getolt-btn-reboot');
		buttons.forEach(function(btn) {
			checkRebootCooldown(btn);
		});
	};

	// Check and apply cooldown for a reboot button
	const checkRebootCooldown = (btn) => {
		const key = btn.getAttribute('data-reboot-key');
		const storageKey = 'getolt_reboot_' + key;
		const lastReboot = localStorage.getItem(storageKey);

		if (lastReboot) {
			const lastTime = parseInt(lastReboot, 10);
			const now = Date.now();
			const elapsed = now - lastTime;
			const remaining = REBOOT_COOLDOWN - elapsed;

			if (remaining > 0) {
				setRebootCooldown(btn, remaining);
			} else {
				localStorage.removeItem(storageKey);
			}
		}
	};

	// Set button to cooldown state
	const setRebootCooldown = (btn, remainingMs) => {
		const key = btn.getAttribute('data-reboot-key');
		btn.disabled = true;
		btn.classList.add('getolt-cooldown');
		updateRebootTooltip(btn, remainingMs);

		if (rebootTimers[key]) {
			clearInterval(rebootTimers[key]);
		}

		rebootTimers[key] = setInterval(function() {
			remainingMs -= 1000;
			if (remainingMs <= 0) {
				clearInterval(rebootTimers[key]);
				delete rebootTimers[key];
				btn.disabled = false;
				btn.classList.remove('getolt-cooldown');
				btn.textContent = 'Перезагрузить ONU';
				btn.title = '';
				localStorage.removeItem('getolt_reboot_' + key);
			} else {
				updateRebootTooltip(btn, remainingMs);
			}
		}, 1000);
	};

	// Update button text and tooltip with remaining time
	const updateRebootTooltip = (btn, remainingMs) => {
		const mins = Math.floor(remainingMs / 60000);
		const secs = Math.floor((remainingMs % 60000) / 1000);
		const timeStr = mins + ':' + (secs < 10 ? '0' : '') + secs;
		btn.textContent = 'Ожидание ' + timeStr;
		btn.title = 'Повторная перезагрузка возможна через ' + mins + ' мин. ' + secs + ' сек.';
	};

	// Check for cyrillic letters that look like latin (A, B, C, E)
	const hasCyrillicLookalikes = (mac) => {
		return /[\u0410\u0412\u0421\u0415\u0430\u0432\u0441\u0435]/.test(mac);
	};

	// Validate MAC format
	const isValidMac = (mac) => {
		const clean = mac.replace(/[:\-.\s]/g, '');
		return /^[0-9A-Fa-f]{12}$/.test(clean);
	};

	// Normalize MAC to XX:XX:XX:XX:XX:XX format
	const normalizeMac = (mac) => {
		const clean = mac.replace(/[^0-9A-Fa-f]/g, '').toUpperCase();
		if (clean.length !== 12) return mac;
		return clean.match(/.{2}/g).join(':');
	};

	// Show activation status
	const showActivationStatus = (ctx, message, type) => {
		const status = ctx.querySelector('.getolt-activation-status');
		if (!status) return;
		status.textContent = message;
		status.className = 'getolt-activation-status getolt-active getolt-' + type;
	};

	// Hide activation status
	const hideActivationStatus = (ctx) => {
		const status = ctx.querySelector('.getolt-activation-status');
		if (!status) return;
		status.className = 'getolt-activation-status';
	};

	const showError = (btn, statusBar, message) => {
		btn.disabled = false;
		statusBar.style.display = 'flex';
		statusBar.className = 'getolt-refresh-status-bar getolt-active getolt-error';
		statusBar.querySelector('.getolt-spinner').style.display = 'none';
		statusBar.querySelector('.getolt-status-text').textContent = message;
		setTimeout(function() {
			statusBar.style.display = 'none';
			statusBar.className = 'getolt-refresh-status-bar';
			statusBar.querySelector('.getolt-spinner').style.display = '';
		}, 5000);
	};

	const reloadTab = (btn) => {
		const tabContent = $(btn).closest('.ui-tabs-panel');
		if (tabContent.length) {
			const tabs = tabContent.closest('.ui-tabs');
			const tabIndex = tabs.find('.ui-tabs-panel').index(tabContent);
			const tabLink = tabs.find('.ui-tabs-nav li').eq(tabIndex).find('a');
			if (tabLink.length && tabLink.attr('href')) {
				$$.ajax.load(tabLink.attr('href'), tabContent);
			}
		}
	};

	// Close service modal within container
	const closeServiceModalIn = (ctx) => {
		const modal = ctx.querySelector('.getolt-service-modal-overlay');
		if (modal) {
			modal.classList.remove('getolt-active');
		}
	};

	// Show service selection modal
	const showServiceModal = (ctx, services, mac) => {
		const modal = ctx.querySelector('.getolt-service-modal-overlay');
		const list = ctx.querySelector('.getolt-service-list');

		if (!modal || !list) return;

		list.innerHTML = '';
		services.forEach(function(svc) {
			const li = document.createElement('li');
			li.setAttribute('data-service-id', svc.serviceId);
			li.onclick = function() {
				closeServiceModalIn(ctx);
				doActivateOnu(ctx, svc.serviceId, mac);
			};

			const title = document.createElement('div');
			title.className = 'getolt-service-title';
			title.textContent = svc.displayTitle || svc.login || ('Услуга #' + svc.serviceId);
			li.appendChild(title);

			if (svc.mac) {
				const macDiv = document.createElement('div');
				macDiv.className = 'getolt-service-mac';
				macDiv.textContent = 'Текущий MAC: ' + svc.mac;
				li.appendChild(macDiv);
			}

			list.appendChild(li);
		});

		modal.classList.add('getolt-active');
	};

	// Close service modal (public, called from JSP onclick)
	this.closeServiceModal = (el) => {
		const ctx = containerOf(el);
		if (ctx) closeServiceModalIn(ctx);
	};

	// === Public methods (called from JSP onclick handlers) ===

	this.startOnuReboot = (btn) => {
		const oltId = btn.getAttribute('data-olt-id');
		const port = btn.getAttribute('data-port');
		const onuId = btn.getAttribute('data-onu-id');
		const mac = btn.getAttribute('data-mac') || '';
		const processId = btn.getAttribute('data-process-id') || '';
		const key = btn.getAttribute('data-reboot-key');

		if (!confirm('Вы уверены, что хотите перезагрузить ONU ' + port + ':' + onuId + '?')) {
			return;
		}

		btn.disabled = true;
		btn.textContent = 'Перезагрузка...';
		debug('Starting reboot for oltId', oltId, 'port', port, 'onuId', onuId, 'processId', processId);

		let url = '/user/plugin/getolt/getolt.do?method=rebootOnu&oltId=' + oltId + '&port=' + port + '&onuId=' + onuId;
		if (processId) url += '&processId=' + processId;
		if (mac) url += '&mac=' + encodeURIComponent(mac);
		$$.ajax.post(url, {})
			.done(function(response) {
				debug('Reboot response', response);
				const result = response.data && response.data.data ? response.data.data : response.data;
				debug('reboot result =', result);
				if (result && result.success) {
					localStorage.setItem('getolt_reboot_' + key, Date.now().toString());

					btn.textContent = 'Перезагружена!';
					btn.style.background = '#28a745';
					setTimeout(function() {
						btn.style.background = '';
						setRebootCooldown(btn, REBOOT_COOLDOWN);
					}, 2000);
				} else {
					const msg = (result && result.message) ? result.message : (response.message || 'Ошибка перезагрузки');
					debug('Reboot error', msg);

					if (msg.indexOf('Подождите') !== -1) {
						const match = msg.match(/(\d+)\s*мин\.\s*(\d+)\s*сек/);
						if (match) {
							const mins = parseInt(match[1], 10);
							const secs = parseInt(match[2], 10);
							const remainingMs = (mins * 60 + secs) * 1000;
							localStorage.setItem('getolt_reboot_' + key, (Date.now() - REBOOT_COOLDOWN + remainingMs).toString());
							setRebootCooldown(btn, remainingMs);
						} else {
							alert('Ошибка: ' + msg);
							btn.disabled = false;
							btn.textContent = 'Перезагрузить ONU';
						}
					} else {
						alert('Ошибка: ' + msg);
						btn.disabled = false;
						btn.textContent = 'Перезагрузить ONU';
					}
				}
			})
			.fail(function(xhr, status, error) {
				debug('Reboot AJAX fail', status, error);
				alert('Ошибка соединения с сервером');
				btn.disabled = false;
				btn.textContent = 'Перезагрузить ONU';
			});
	};

	this.startPortRefresh = (btn) => {
		const ctx = containerOf(btn);
		const oltIp = btn.getAttribute('data-olt-ip');
		const port = btn.getAttribute('data-port');
		const processId = btn.getAttribute('data-process-id') || '';
		const key = btn.getAttribute('data-refresh-key');
		const statusBar = ctx.querySelector('.getolt-refresh-status-bar[data-refresh-key="' + key + '"]');

		if (!statusBar) return;

		btn.disabled = true;
		statusBar.style.display = 'flex';
		statusBar.className = 'getolt-refresh-status-bar getolt-active';
		statusBar.querySelector('.getolt-status-text').textContent = 'Запуск обновления...';
		debug('Starting refresh for', oltIp, 'port', port, 'key', key, 'processId', processId);

		let url = '/user/plugin/getolt/getolt.do?method=refreshPort&oltIp=' + encodeURIComponent(oltIp) + '&portNumber=' + port;
		if (processId) url += '&processId=' + processId;
		$$.ajax.post(url, {})
			.done(function(response) {
				debug('Response', response);
				const result = response.data && response.data.data ? response.data.data : response.data;
				debug('result =', result);
				if (result && result.success) {
					statusBar.querySelector('.getolt-status-text').textContent = 'Обновление завершено!';
					statusBar.className = 'getolt-refresh-status-bar getolt-active getolt-success';
					statusBar.querySelector('.getolt-spinner').style.display = 'none';
					setTimeout(function() { reloadTab(btn); }, 1500);
				} else {
					const msg = (result && result.message) ? result.message : (response.message || 'Ошибка запуска');
					debug('Error', msg);
					showError(btn, statusBar, msg);
				}
			})
			.fail(function(xhr, status, error) {
				debug('AJAX fail', status, error);
				showError(btn, statusBar, 'Ошибка соединения с сервером');
			});
	};

	this.formatMacInput = (input, e) => {
		const ctx = containerOf(input);
		const cursorPos = input.selectionStart;
		const oldValue = input.value;

		if (hasCyrillicLookalikes(oldValue)) {
			input.classList.add('getolt-is-invalid');
			const status = ctx.querySelector('.getolt-activation-status');
			if (status) {
				status.textContent = 'Кириллица! Переключите раскладку на EN';
				status.className = 'getolt-activation-status getolt-active getolt-error';
			}
		} else {
			input.classList.remove('getolt-is-invalid');
			const status = ctx.querySelector('.getolt-activation-status');
			if (status && status.textContent.indexOf('Кириллица') !== -1) {
				status.className = 'getolt-activation-status';
			}
		}

		const hex = oldValue.replace(/[^0-9A-Fa-f]/g, '').toUpperCase().substring(0, 12);

		let formatted = '';
		for (let i = 0; i < hex.length; i++) {
			if (i > 0 && i % 2 === 0) {
				formatted += ':';
			}
			formatted += hex[i];
		}

		if (input.value !== formatted) {
			input.value = formatted;

			let newPos = cursorPos;
			const oldColons = (oldValue.substring(0, cursorPos).match(/:/g) || []).length;
			const newColons = (formatted.substring(0, Math.min(cursorPos, formatted.length)).match(/:/g) || []).length;
			newPos += (newColons - oldColons);

			if (formatted.length > oldValue.length && formatted[cursorPos] === ':') {
				newPos++;
			}

			input.setSelectionRange(newPos, newPos);
		}
	};

	this.handleMacPaste = (event, input) => {
		event.preventDefault();
		const pasted = (event.clipboardData || window.clipboardData).getData('text');
		const clean = pasted.replace(/[^0-9A-Fa-f]/g, '').toUpperCase().substring(0, 12);
		let formatted = '';
		for (let i = 0; i < clean.length; i++) {
			if (i > 0 && i % 2 === 0) {
				formatted += ':';
			}
			formatted += clean[i];
		}
		input.value = formatted;
	};

	// Private: ONU activation logic with explicit context
	const doActivateOnu = (ctx, serviceId, mac) => {
		const input = ctx.querySelector('.getolt-new-mac-input');
		const btn = ctx.querySelector('.getolt-btn-activate');

		if (!serviceId) {
			mac = input.value.trim();
		}

		if (hasCyrillicLookalikes(mac)) {
			input.classList.add('getolt-is-invalid');
			showActivationStatus(ctx, 'MAC содержит кириллицу. Переключите на английскую раскладку.', 'error');
			return;
		}

		if (!isValidMac(mac)) {
			input.classList.add('getolt-is-invalid');
			showActivationStatus(ctx, 'Невалидный формат MAC. Используйте XX:XX:XX:XX:XX:XX', 'error');
			return;
		}
		input.classList.remove('getolt-is-invalid');

		mac = normalizeMac(mac);

		const processId = input.getAttribute('data-process-id');
		if (!processId) {
			showActivationStatus(ctx, 'Не найден ID процесса', 'error');
			return;
		}

		btn.disabled = true;
		btn.textContent = 'Обработка...';
		showActivationStatus(ctx, 'Отправка запроса...', 'processing');

		let url = '/user/plugin/getolt/getolt.do?method=activateOnu&processId=' + processId +
				  '&newMac=' + encodeURIComponent(mac);
		if (serviceId) {
			url += '&serviceId=' + serviceId;
		}

		debug('Activating ONU', url);

		$$.ajax.post(url, {})
			.done(function(response) {
				debug('Activation response', response);
				const result = response.data && response.data.data ? response.data.data : response.data;

				if (result && result.success) {
					let msg = 'MAC обновлён: ' + (result.oldMac || '-') + ' -> ' + result.newMac;
					if (result.sessionDropped) {
						msg += '\nСессия сброшена.';
					}
					showActivationStatus(ctx, msg, 'success');
					input.value = '';
					closeServiceModalIn(ctx);

					setTimeout(function() {
						reloadTab(btn);
					}, 2000);

				} else if (result && result.status === 'MULTIPLE_SERVICES') {
					showServiceModal(ctx, result.services, mac);
					hideActivationStatus(ctx);

				} else {
					const errorMsg = (result && result.message) ? result.message : (response.message || 'Ошибка активации');
					showActivationStatus(ctx, errorMsg, 'error');
				}

				btn.disabled = false;
				btn.textContent = 'Активировать';
			})
			.fail(function(xhr, status, error) {
				debug('Activation AJAX fail', status, error);
				showActivationStatus(ctx, 'Ошибка соединения с сервером', 'error');
				btn.disabled = false;
				btn.textContent = 'Активировать';
			});
	};

	// Public: activate ONU from JSP button click
	this.activateOnu = (btn) => {
		const ctx = containerOf(btn);
		doActivateOnu(ctx);
	};

	// Initialize on page content load
	this.init = (uiid) => {
		const ctx = document.getElementById(uiid);
		initRebootButtons(ctx);

		// Close modal on overlay click
		ctx.addEventListener('click', function(e) {
			if (e.target.classList.contains('getolt-service-modal-overlay')) {
				closeServiceModalIn(ctx);
			}
		});

		// Close modal on Escape key (register once globally)
		if (!escapeListenerAdded) {
			document.addEventListener('keydown', function(e) {
				if (e.key === 'Escape') {
					const visibleModal = document.querySelector('.getolt-service-modal-overlay.getolt-active');
					if (visibleModal) {
						visibleModal.classList.remove('getolt-active');
					}
				}
			});
			escapeListenerAdded = true;
		}
	};
};
