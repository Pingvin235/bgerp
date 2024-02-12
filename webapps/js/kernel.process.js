// "use strict";
/*
 * Processes and process queues.
 */
$$.process = new function() {
	/**
	 * Opens process card.
	 * @param {*} id process ID.
	 */
	const open = (id) => {
		$$.shell.contentLoad("process#" + id);
	}

	/**
	 * Removes process card e.g. after deletion of it.
	 * @param {Number} processId the process ID.
	 */
	const remove = (processId) => {
		$$.closeObject = null;
		$$.shell.removeCommandDiv('process-' + processId);
	}

	/**
	 * Hides left area in process card on scrolling down to make right full-width.
	 * @param {*} $leftDiv left DIV in process card, must contain sub-div with class='wrap'.
	 * @param {*} topTolerance how many pixels left area should be scrolled up out of visible area to be hidden, the option prevents flickering.
	 */
	const hideLeftAreaOnScroll = ($leftDiv, topTolerance) => {
		const debug = $$.debug("process.hideLeftAreaOnScroll");

		const $wrap = $leftDiv.find(".wrap");
		let wrapBottomOffset = $wrap.offset().top + $wrap.height();

		$(window).scroll(function() {
			const scrollTop = document.documentElement.scrollTop;
			if ($leftDiv.is(":visible")) {
				// $wrap.height() can be increased when params editing
				wrapBottomOffset = Math.max(wrapBottomOffset, $wrap.offset().top + $wrap.height());
				if (wrapBottomOffset + topTolerance < scrollTop) {
					debug("hide()", "wrapBottomOffset:", wrapBottomOffset, "topTolerance: ", topTolerance, "scrollTop:", scrollTop);
					$leftDiv.hide();
				}
			} else if (scrollTop < wrapBottomOffset) {
				debug("show()", "wrapBottomOffset:", wrapBottomOffset, "scrollTop:", scrollTop);
				$leftDiv.show();
			}
		});
	}

	/**
	 * Opens process description editor.
	 * @param {*} id CSS id for process description container.
	 */
	const descriptionEdit = (id) => {
		$('#' + id + ' #show').hide();
		$('#' + id + ' #editorAdd').hide();
		$('#' + id + ' #editorChange').show();
		const $ta = $('#' + id + ' #editorChange textarea');
		$ta.focus();
		$ta[0].setSelectionRange($ta[0].value.length, $ta[0].value.length);
	}

	// public functions
	this.open = open;
	this.remove = remove;
	this.hideLeftAreaOnScroll = hideLeftAreaOnScroll;
	this.descriptionEdit = descriptionEdit;
};

function updateExecutors($groups, $executors, paramNameGroup, paramNameExecutor, savedExecutors) {
	if ($executors.length > 0) {
		if (paramNameGroup == undefined) {
			paramNameGroup = "group";
		}
		if (paramNameExecutor == undefined) {
			paramNameExecutor = "executor";
		}

		var groupValues = getCheckedValuesUrl($groups, paramNameGroup);
		var executorValues = getCheckedValuesUrl($executors, paramNameExecutor);

		groupValues = groupValues.replace(new RegExp(
			"&" + paramNameGroup + "=", 'g'), "&group=");

		var url = "/user/directory/user.do?action=userList&page.pageIndex=-1"
			+ groupValues + executorValues;

		if (paramNameExecutor) {
			url += "&paramName=" + paramNameExecutor;
		}

		var $data = $executors.find("data");
		if ($data.length > 0) {
			$executors = $data;
		}

		if (groupValues.length > 0) {
			$$.ajax.load(url, $executors).done(() => {
				// отмечаем сохраненные значения с прошлой выборки
				if (savedExecutors) {
					savedExecutors = savedExecutors.split(",");
					savedExecutors.forEach(function(executorId, i , arr) {
						$executors.find("input[value="+executorId+"][name=" + paramNameExecutor + "]").prop('checked', true);
					});
				}
			});
		} else {
			$executors.html("");
		}
	}
}

// can be used only in custom code
function openProcess(id) {
	console.warn($$.deprecated);
	$$.process.open(id);
}

function openProcessTypeTreeNode(el, nodeId) {
	$(el.parentNode).find("#" + nodeId + "_childs").toggle();
}

function processTypeTreeNodeSelected(el, nodeId) {
	var parent = el.parentNode;
	while (parent.tagName != 'FORM') {
		parent = parent.parentNode;
	}
	$(parent).find("input[name='typeId']").attr("value", nodeId);

	$(parent).find("span").css("font-weight", "").css("color", "");
	$(el).css("font-weight", "bold").css("color", "blue");

	$$.ajax.load("/user/process.do?action=processCreateGroups&typeId=" + nodeId,
			$(el).closest("#typeTree").parent().find("#groupSelect"));
}

function objectsToLinkTable($uiid, processId, customerLinkRoles, selectedValues, additionalLinksForAdd) {
	var objects = [];

	if (!(additionalLinksForAdd)) {
		objects = openedObjectList({
			"typesExclude": ['process'],
			"selected": selectedValues
		});
	}
	else {
		objects = additionalLinksForAdd;
	}

	var html = '';

	for (const d in objects) {
		var data = objects[d];

		var objectType = data.objectType;
		var objectId = data.id;

		/* contract_ds-333
		 * customer-333
		 * process-444
		 */

		// для объектов типа contract_ds
		var pos = objectType.lastIndexOf('_');
		if (pos > 0) {
			objectType = objectType.substring(0, pos) + ":" + objectType.substring(pos + 1);
		}

		html += '<tr>\
				<td>\
					<form action="/user/link.do">\
						<input type="hidden" name="action" value="addLink"/>\
						<input type="hidden" name="objectType" value="process"/>\
						<input type="hidden" name="id" value="' + processId + '"/>\
						<input type="hidden" name="linkedObjectId" value="' + objectId + '"/>\
						<input type="hidden" name="linkedObjectTitle" value="' + $$.encodeHtml(data.title) + '"/>\
						<input type="hidden" name="linkedObjectType" value="';
		if (objectType == 'customer') {
			html += customerLinkRoles[0][0] + '"/>';
		}
		else {
			html += objectType + '"/>';
		}
		html += '<input type="checkbox" name="check"/>\
					</form>\
				</td>\
				<td nowrap="nowrap">';
		if (objectType == 'customer') {
			html += '<select name="linkedObjectType" onChange="$(this).closest(\'tr\').find(\'form\')[0].linkedObjectType.value = this.options[selectedIndex].value">';
			$.each(customerLinkRoles, function () {
				html += '<option value="' + this[0] + '">' + this[1] + '</option>';
			});
			html += '</select>';
		}
		else {
			html += data.objectTypeTitle;
		}
		html += '</td>\
				<td>' + data.title + '</td>\
			</tr>';
	}

	const show = objects.length > 0;

	$uiid.toggle(show);
	if (show) {
		const $table = $uiid.find('table');
		$table.find('tr:gt(0)').remove();
		$table.append(html);
	}
}

// processing client events

$(() => {
	const processProcessClientEvents = (event) => {
		$$.process.open(event.id);
	};

	addEventProcessor('ru.bgcrm.event.client.ProcessChangedEvent', processProcessClientEvents);
	addEventProcessor('ru.bgcrm.event.client.ProcessOpenEvent', processProcessClientEvents);
})

addEventProcessor('ru.bgcrm.event.client.ProcessCloseEvent', (event) => {
	removeCommandDiv( "process-" + event.id );
	$$.closeObject = null;
	window.history.back();
});

addEventProcessor('ru.bgcrm.event.client.TemporaryObjectEvent', (event) => {
	$.each(event.processIds, function () {
		if ($('#content #process-'.concat(this)).length == 0)
			openProcess(this);
	})
});
