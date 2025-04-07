(function($) {
	"use strict";
	$.fn.preview = function (options) {
		const c = $.extend({
			xOffset: 10,
			yOffset: 10
		}, options);

		const position = ($preview, e) => {
			// for image - dependent preview orientation and size

			// const img = $preview.find('img')[0];
			// const iWidth = img.width;
			// const iHeight = img.height;

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
				displayOptions.left = e.pageX + c.xOffset + "px";
			else
				displayOptions.left = (e.pageX - width - c.xOffset) + "px";

			$preview.css(displayOptions);
		};

		return this.each(() => {
			const $a = $(this);
			const text = $a.text().toLowerCase();
			if (text &&
				(text.endsWith(".jpg") || text.endsWith(".jpeg") || text.endsWith(".gif") || text.endsWith(".png"))) {
				$a.hover(
					function (e) {
						const $preview = $("<div id='preview'><img style='max-width: 100%; max-height: 100%;' src='" + this.href + "' alt='Preview'/></div>");
						$("body").append($preview);
						$preview.find('img').on('load', () => {
							position($preview, e);
							$preview.fadeIn();
						});
					},
					() => {
						$("#preview").remove();
					});
				$a.mousemove((e) => {
					position($("#preview"), e);
				});
			}
		})
	}
})(jQuery);