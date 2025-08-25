// "use strict";

$$.event = new function () {
	const eventProcessors = {};

	const addProcessor = (eventType, processor) => {
		let processors;
		if (!(processors = eventProcessors[eventType])) {
			processors = [];
			eventProcessors[eventType] = processors;
		}
		processors.push(processor);
	}

	const process = (event) => {
		const processors = eventProcessors[event.className];
		if (processors) {
			for (let i = 0; i < processors.length; i++) {
				processors[i](event);
			}
		} else
			console.warn('Not found processor for', event);
	}

	// events
	const processEvent = (event) => {
		if (event.className == 'ru.bgcrm.event.client.MessageOpenEvent') {
			$$.shell.contentLoad("/user/message/queue").done(() => {
				$$.ajax.loadContent('/user/message.do?typeId=' + event.typeId + '&messageId=' + event.systemId + '&returnUrl=' + encodeURIComponent('/user/message.do?method=messageList'));
			});
		}
		else if (event.className == 'ru.bgcrm.event.client.UrlOpenEvent') {
			$$.shell.contentLoad(event.url);
		}
	}

	addProcessor('ru.bgcrm.event.client.MessageOpenEvent', processEvent);
	addProcessor('ru.bgcrm.event.client.UrlOpenEvent', processEvent);

	// public functions
	this.addProcessor = addProcessor;
	this.process = process;
}

