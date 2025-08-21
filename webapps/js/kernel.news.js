// "use strict";

$$.news = new function () {
	const showPopupMessage = (title, message) => {
		const $messageDiv = $("<div>" + message + "</div>");

		$("body").append($messageDiv);

		$($messageDiv).dialog({
			autoOpen: false,
			show: "slide",
			hide: "explode",
			resizable: false,
			position: { my: "center top", at: "center top+100px", of: window },
			title: title,
			close: () => {
				$messageDiv.remove();
			}
		});

		$messageDiv.dialog("open");
	}

	const processEvent = (event) => {
		const messagesCount = event.newsCount + event.messagesCount + (event.versionUpdateNeeded ? 1 : 0);
		const message = event.message;

		const $messagesLink = $('#messagesLink');
		const $messagesMenu = $('#messagesMenu');

		$messagesMenu.html("");

		// news
		let itemCode = "<li><a href='/user/news' onclick='$$.shell.followLink(this.href, event)'>" + message['News'] + ": <span style='font-weight: bold;";
		if (event.blinkNews)
			itemCode += "color: orange;"
		itemCode += "'>" + event.newsCount + "</span></a></li>";

		$messagesMenu.append(itemCode);

		// messages
		if (event.messagesCount > 0) {
			itemCode = "<li><a href='/user/message/queue' onclick='$$.shell.followLink(this.href, event)'>" + message['Unprocessed messages'] + ": <span style='font-weight: bold;";
			if (event.blinkMessages)
				itemCode += "color: orange;";
			itemCode += "'>" + event.messagesCount + "</span></a></li>";

			$messagesMenu.append(itemCode);
		}

		// popup news
		if (event.popupNews) {
			event.popupNews.forEach(function (id) {
				$$.ajax
					.post('/user/news.do?method=newsGet&newsId=' + id, {html: true})
					.done(result => {
						showPopupMessage(message['News'], result);
					});
			});
		}

		// app version update needed
		if (event.versionUpdateNeeded) {
			itemCode = "<li>";
			if (event.versionLink)
				itemCode += "<a href='/user/admin/app/status' onclick='$$.shell.followLink(this.href, event)'>";
			itemCode += message['App update is needed'] + ": <span style='font-weight: bold; color: orange;'>1</span>";
			if (event.versionLink)
				itemCode += "</a>";
			itemCode += "</li>";

			$messagesMenu.append(itemCode);
		}

		if (event.blinkNews || event.blinkMessages || event.versionUpdateNeeded) {
			// переменная называется blinkMessages, но мигать может и из-за новостей новых
			if (!$$.blinkMessages) {
				$$.blinkMessages = setInterval(function() {
					if ($messagesLink.attr('style')) {
						$messagesLink.attr('style', '');
					} else {
						$messagesLink.css('color', 'orange');
					}
				}, 500)
			}
		} else {
			$messagesLink.attr('style', '');
			if ($$.blinkMessages) {
				clearInterval($$.blinkMessages);
				$$.blinkMessages = undefined;
			}
		}

		$messagesMenu.menu("refresh");

		$messagesLink.html(messagesCount);
	}

	addEventProcessor('org.bgerp.event.client.NewsInfoEvent', processEvent);
}