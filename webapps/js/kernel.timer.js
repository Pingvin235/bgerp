// "use strict";

$$.timer = new function() {
	const debug = $$.debug("timer");

	let run = true;

	const start = () => {
		run = true;
	}

	const stop = () => {
		run = false;
	}

	const timer = () => {
		let urlArray = generateUrlForFilterCounter(),
			url = "/user/pool.do",
			callback = function () {
				window.setTimeout(timer, 5000);
			};

		if (urlArray.length > 0) {
			url += "?processCounterUrls=" + encodeURIComponent(urlArray);
		}

		if (run)
			$$.ajax
				.post(url)
				.always(callback);
		else
			callback();
	}

	// public functions
	this.init = timer;
	this.start = start;
	this.stop = stop;
}
