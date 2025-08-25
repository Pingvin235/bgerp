// "use strict";

$$.license = new function() {
	const debug = $$.debug("lic");

	// events
	const processEvent = (event) => {
		const message = event.message;
		if (!message)
			return;

		let text = message.message;
		if (event.linkShown)
			text = "<a href='#' onclick=\"$$.shell.followLink('/user/admin/license', event)\">" + text + "</a>";

		$$.shell.message.show(message.title, text);
	}

	$$.event.addProcessor('org.bgerp.event.client.LicenseEvent', processEvent);
}