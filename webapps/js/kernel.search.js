// "use strict";

$$.search = new function() {
	const debug = $$.debug("search");

	const onObjectSelect = () => {
		const value = $('#searchForm > input[type=hidden]').val();
		$('.searchForm').hide();
		$('#searchForm-' + value).show();
	}

	// public functions
	this.onObjectSelect = onObjectSelect;
}
