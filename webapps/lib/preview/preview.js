(function($) {
	"use strict";
	$.fn.preview = function(options) {
		const c = $.extend({
			xOffset : 10,
			yOffset : 10
		}, options);
		
		const position = function ($preview, e) {
			const width = $preview.outerWidth();
			const height = $preview.outerHeight();
			
			const wWidth = $(window).width();
			const wHeight = $(window).height();
			
			const displayOptions = {};
			if (e.pageY < wHeight / 2)
				displayOptions.top = (e.pageY + c.yOffset) + "px";
			else
				displayOptions.top = (e.pageY - height - c.yOffset) + "px";
			
			if (e.pageX < wWidth / 2)
				displayOptions.left =  e.pageX + c.xOffset + "px";
			else
				displayOptions.left =  (e.pageX - width- - c.xOffset) + "px";
			
			$preview.css(displayOptions);
		};
		
		return this.each(function() {
			const $a = $(this);
			const text = $a.text().toLowerCase();
			if (text && 
				(text.endsWith(".jpg") || text.endsWith(".jpeg") || text.endsWith(".gif") || text.endsWith(".png"))) {
				$a.hover(
					function(e) {
						$("body").append("<div id='preview'><img style='max-width: 100%; max-height: 100%;' src='" + this.href + "' alt='Preview'/></div>");
						const $preview = $("#preview");
						position($preview, e);
						$preview.fadeIn();
					}, 
					function() {
						$("#preview").remove();
					});
				$a.mousemove(function(e) {
					position($("#preview"), e);
				});
			}
		})
	}
})(jQuery);