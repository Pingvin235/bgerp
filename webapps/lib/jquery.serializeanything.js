(function($) {
	$.fn.serializeAnything = function( excludeParams ) {
		const toReturn = [];

		$.each($(this).find(':input'), function () {
			if (excludeParams && excludeParams.indexOf(this.name) >= 0) {
				return;
			}
			if (this.name && (this.checked || /button|select|textarea/i.test(this.nodeName) || /text|hidden|password/i.test(this.type))) {
				const val = $(this).val();
				toReturn.push(encodeURIComponent(this.name) + "=" + encodeURIComponent(val));
			}
		});

		return toReturn.join("&").replace(/%20/g, "+");
	}

})(jQuery);