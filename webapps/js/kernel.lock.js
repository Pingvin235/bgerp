// "use strict";

$$.lock = new function () {
	const debug = $$.debug("lock");

	this.add = (name) => {
		return $$.ajax.post('/user/lock.do?action=add&lockId=' + name);
	}

	this.free = (name) => {
		return $$.ajax.post('/user/lock.do?action=free&lockId=' + name);
	}

	const processEvent = (event) => {
		const lockId = event.lock.id;
		if ($('#lock-' + lockId).length == 0) {
			debug("Free lock: ", event.lock.id);
			$$.lock.free(event.lock.id);
		}
	}

	addEventProcessor('ru.bgcrm.event.client.LockEvent', processEvent);
}