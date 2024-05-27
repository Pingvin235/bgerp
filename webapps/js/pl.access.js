/*
 * Plugin Access
 */
"use strict";

$$.access = new function() {
	const debug = $$.debug("access");

	const get = (button, userId) => {
		const url = "/user/plugin/access/credential.do?method=get&id=" + userId;
		$$.ajax.post(url, {control: button}).done(result => {
			navigator.clipboard.writeText(result.data.text);
		});
	}

	// public functions
	this.get = get;
}
