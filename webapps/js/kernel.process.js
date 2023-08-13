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
	 * Hides left area in process card on scrolling down to make right full-width.
	 * @param {*} $leftDiv left DIV in process card, must contain sub-div with class='wrap'.
	 * @param {*} topTolerance how many pixels left area should be scrolled up out of visible area to be hidden, the option prevents flickering.
	 */
	const hideLeftAreaOnScroll = ($leftDiv, topTolerance) => {
		const debug = $$.debug("process.hideLeftAreaOnScroll");

		const $wrap = $leftDiv.find(".wrap");

		$(window).scroll(function() {
			const wrapBottomOffset = $wrap.offset().top + $wrap.height();
			const scrollTop = document.documentElement.scrollTop;

			debug("wrapBottomOffset:", wrapBottomOffset, "topTolerance: ", topTolerance, "scrollTop:", scrollTop);

			if ($leftDiv.is(":visible")) {
				if (wrapBottomOffset + topTolerance < scrollTop) {
					debug("hide()");
					$leftDiv.hide();
				}
			} else if (scrollTop < wrapBottomOffset) {
				debug("show()");
				$leftDiv.show();
			}
		});
	}

	// available functions
	this.open = open;
	this.hideLeftAreaOnScroll = hideLeftAreaOnScroll;

	// sub namespace link
	this.link = new function() {
		const showForm = (uiid, id) => {
			$(`#${uiid} #linkEditor > form`).hide();
			$(`#${uiid} #linkEditor > form#${id}`).show();
		}

		/**
		 * Sends checked request forms for adding links.
		 * @param {*} uiid parent element with forms.
		 * @param {*} requestUrl URL to load after adding to parent of uiid.
		 */
		const add = (uiid, requestUrl) => {
			const deferreds = [];

			const forms = $('#' + uiid + ' form:visible');
			for (var i = 0; i < forms.length; i++) {
				const form = forms[i];
				if (form.check && form.check.checked)
					deferreds.push($$.ajax.post(form));
			}

			$.when.apply($, deferreds).done(() => { $$.ajax.load(requestUrl, $('#' + uiid).parent()) });
		}

		const customerRoleChanged = ($hidden) => {
			$hidden.closest('tr').find('form')[0].linkedObjectType.value = $hidden.val();
		}

		// available functions
		this.showForm = showForm;
		this.add = add;
		this.customerRoleChanged = customerRoleChanged;
	}

	// sub namespace queue
	this.queue = new function() {
		const debug = $$.debug("processQueue");

		const changed = (savedSetId) => {
			const queueId = $("#processQueueSelect > input[type=hidden]").val();

			let filterLoadDfd;
			let typeTreeLoadDfd;

			// поиск и создание нужного фильтра и кнопок
			if ($("#processQueueFilter > div#" + queueId).length == 0) {
				filterLoadDfd = $$.ajax.load("/user/process/queue.do?action=queueGet&id=" + queueId, $('#processQueueFilter'), {append : true});
			}

			if (savedSetId == undefined) {
				// дерево типов для создания
				typeTreeLoadDfd = $$.ajax.load("/user/process/queue.do?action=typeTree&queueId=" + queueId, $("#processQueueCreateProcess > #typeTree"));
			}

			// workaround to make waiting, see logic in $$.ajax.load
			const dfd = $.Deferred();
			const $loader = $("<div class='loader'></div>");
			$('#processQueueShow').append($loader);
			$loader.data("dfd", {dfd: dfd, url: "queue.changed.filter"});

			return $.when(filterLoadDfd, typeTreeLoadDfd).done(() => {
				$("#processQueueFilter > div[id!=" + queueId + "]").hide();

				// отображение нужного фильтра, нужных кнопок
				var $filter = $("#processQueueFilter").find("div#" + queueId);

				$filter.show()

				if (savedSetId >= 0)
				{
					$filter.find("*[id=savedFilters]").toggle(savedSetId > 0);
					$filter.find("*[id='" + queueId + "-0']").toggle(savedSetId == 0);

					$filter.find("form").attr("active", "");
					$filter.find("form[id='" + queueId + "-" + savedSetId + "']").attr("active", "1");
				}

				if (savedSetId > 0) {
					$filter.find("#savedFilters div[draggable=true]").each(function() {
						if ($(this).attr("id") == savedSetId)
						{
							if ( $(".combo.dropFilterArea").find("div[id="+savedSetId+"]").length == 0 )
							{
								$(".combo.dropFilterArea").find("div.text-value").html("");
							}
							$(this).removeClass("btn-white").addClass("btn-blue");

						} else {
							$(this).removeClass("btn-blue").addClass("btn-white");
						}
					})
				}

				const url = $$.ajax.formUrl($filter.find("form[active=1]"));
				if (url) {
					$$.ajax
						.load(url, $('#processQueueData'))
						.done(() => {
							$$.shell.stateFragment(queueId)
							dfd.resolve();
						});
				} else
					dfd.resolve();
			});
		}

		const showSelected = (id) => {
			if (id) {
				$(".btn-panel input[type=hidden]").each(function () {
					if ($(this).val() == id) {
						$(this).parent().removeClass("btn-white");
						$(this).parent().addClass("btn-blue");
					}
					else {
						$(this).parent().removeClass("btn-blue");
						$(this).parent().addClass("btn-white");
					}
				});

				if (id != $('#processQueueSelect').find('input[type=hidden]').val()) {
					$('#processQueueSelect').find('input[type=hidden]').val(id);
				}

				if ($(".btn-panel.btn-blue").length < 1) {
					$("#processQueueSelect .drop li").each(function () {
						if ($(this).val() == id) {
							$("#processQueueSelect > .text-value").html($(this).html());
							$('#processQueueSelect').find('.text-value div.icon-add').remove();
							return;
						}
					});
				}
			}

			$$.process.queue.changed();
		}

		const updateSelected = (id) => {
			$$.ajax
				.post("/user/process/queue.do?action=queueSavedPanelSet" + $$.ajax.requestParamsToUrl({command: "updateSelected",  queueId: id}))
				.done(() => {
					$(".btn-panel input[type=hidden]").each(function () {
						if ($(this).val() == id) {
							$("#processQueueSelect").find(".text-value").empty();
						}
					});
				});
		}

		// available functions
		this.debug = debug;
		this.changed = changed;
		this.showSelected = showSelected;
		this.updateSelected = updateSelected;
	}
};

function processQueueChanged(savedSetId) {
	console.warn($$.deprecated);
	$$.process.queue.changed(savedSetId);
}

function processQueueClearHiddenFilters($form) {
	$$.ui.comboInputs($form.find('.filtersSelect')).each( function() {
		var id = $(this).attr('id');
		var $filterItem = $('#' + id + '.filter-item');

		// очистка скрытых фильтров
		if (!this.checked &&
			$filterItem.find( '.dontResetOnHideFilter' ).length == 0 ) {
			/* пока простейший сброс хотя бы текстовых фильтров и фильтров по дате, с combo_check и т.п. ещё разобраться */
			$filterItem.find('input[type=text]').val('');
			$filterItem.find('input[type=hidden]').val('');
		}
	});
}

function processQueueMarkFilledFilters($form) {
	$$.ui.comboInputs($form.find('.filtersSelect')).each( function() {
		var id = $(this).attr('id');
		var $filterItem = $('#' + id + '.filter-item');

		// отметка жирным заполненных фильтров
		var $span = $(this).parent().find("span");
		$span.css("font-weight", "");

		if ($filterItem.find('input[type=text]').val() ||
			$filterItem.find('input[type=hidden]').val() ||
			$filterItem.find('input[type=checkbox]:checked').length) {
			$span.css("font-weight", "bold");
		}
	});
}

function processQueueFilterSetSelect(queueId) {
	$('#processQueueFilter').find('div#' + queueId).remove();
	$$.process.queue.changed();
}

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
	/* $$.ajax.load("/user/process.do?action=processRequest&typeId=" + nodeId,
			$(el).closest("#typeTree").parent().find("#additionalParamsSelect")); */
}

function statusChangeEditor(selector, selectedStatus, currentStatus,
		allowedStatus, processId, paramId) {
	selectedStatus = parseInt(selectedStatus);

	if (selectedStatus == currentStatus) {
		$(selector + " div[type=editor]").hide();
	} else {
		$(selector + " div#editor").show();
		if ((allowedStatus == null || allowedStatus.isEmpty() || allowedStatus
				.indexOf(selectedStatus) >= 0)
				&& paramId > 0) {
			var url = "/user/parameter.do?action=parameterGet&hideButtons=1&id="
					+ processId + "&paramId=" + paramId;
			openUrlTo(url, $(selector + " div#editorCat #editorCatEditor"));
			$(selector + " div#editorCat").show();
		}
	}
}

function objectsToLinkTable($uiid, processId, customerLinkRoles, selectedValues, additionalLinksForAdd)
{
	var objects = [];

	if( !(additionalLinksForAdd) )
	{
		objects = openedObjectList({
			"typesExclude" : ['process'],
			"selected" : selectedValues
		});
	}
	else
	{
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
		if( pos > 0 )
		{
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
		if (objectType == 'customer')
		{
			html += customerLinkRoles[0][0] + '"/>';
		}
		else
		{
			html += objectType + '"/>';
		}
		html += '<input type="checkbox" name="check"/>\
					</form>\
				</td>\
				<td nowrap="nowrap">';
		if( objectType == 'customer' )
		{
			html += '<select name="linkedObjectType" onChange="$(this).closest(\'tr\').find(\'form\')[0].linkedObjectType.value = this.options[selectedIndex].value">';
			$.each( customerLinkRoles, function()
			{
				html += '<option value="' + this[0] + '">' + this[1] + '</option>';
			});
			html += '</select>';
		}
		else
		{
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

function setListItemsClick(e) {
	$(e).find('tr td:nth-child(2)').on('click', function() {
		$(this).parent().find('input').trigger('click');
	});
}

function addToPanelScript(id, title, isNew)
{
	var checkedQueue = $('#processQueueSelect').find('input[type=hidden]').val();

	if ( isNew )
	{
		$("#processQueueSelect").find("li[value="+id+"]").remove();

		$$.ajax.post("/user/process/queue.do?action=queueSavedPanelSet" + $$.ajax.requestParamsToUrl({command: "add", queueId: id, queueTitle: title}));
	}

	$( '#processQueueSelect' ).before(	"<div onclick=$('#processQueueSelect').find('input[type=hidden]').val("+id+");$$.process.queue.changed();updateSelectedQueue("+id+"); class='btn-white btn-panel'>" +
			"<input type='hidden' value="+id+" />" +
			"<span class='icon ti-close mr05'></span>" +
			"<span title='" + title + "' class='title'>" + title + "</span>" +
		"</div>");

	$(".btn-panel").find("input[value="+id+"]").parent().find("span.icon").click(function(event){
		event.stopPropagation();
		removeFromPanel(id,title);
		$(this).parent().remove();
	})

	//Если добавляем на панель очередь выбранную в выпадающем списке, то подсвечиваем ее после добавления.
	if ( id == checkedQueue )
	{
		$(".btn-panel").find("input[value="+id+"]").parent().removeClass("btn-white");
		$(".btn-panel").find("input[value="+id+"]").parent().addClass("btn-blue");
	}

	$(".btn-panel").on("click", function(event){
		$(".btn-panel").removeClass("btn-blue");
		$(".btn-panel").addClass("btn-white");
		$(this).removeClass("btn-white");
		$(this).addClass("btn-blue");
	});
}

function removeFromPanel(id, title)
{
	$$.ajax
		.post("/user/process/queue.do?action=queueSavedPanelSet" + $$.ajax.requestParamsToUrl({command: "delete", queueId: id}))
		.done(() => {
			$("#processQueueSelect").find(".drop").append("<li value="+id+" onclick='updateSelectedQueue("+id+");showSelectedQueue("+id+");'><div style='display: inline;'>"+ title +
			"</div><div class='icon-add'></div></li>");

			$("#processQueueSelect .icon-add").click(function(event){
				event.stopPropagation();
				addToPanelScript(id,title,true);
				$(this).parent().remove();
			});
		});
}

function updateSelectedQueue(id) {
	console.warn($$.deprecated);
	$$.process.queue.updateSelected(id);
}

function showSelectedQueue(id) {
	console.warn($$.deprecated);
	$$.process.queue.showSelected(id);
}

function exportFilterToCommons()
{
	var queueId = $("#processQueueSelect > input[type=hidden]").val();
	var buttonId = $("#savedFilters .btn-blue").attr("id");
	var url = $("#savedFilters form[id='"+queueId+"-"+buttonId+"']").attr("action");
	var title = $("#savedFilters .btn-blue").html();

	if (!buttonId) {
		alert("Не выбран сохранённый фильтр!");
		return;
	}

	$$.ajax
		.post("/user/process/queue.do?" + $$.ajax.requestParamsToUrl({"action":"queueSavedFilterSet","command":"addCommon","url":url,"queueId":queueId,"title":title}))
		.done(() => {
			location.reload();
		})
}

function importFilterFromCommons()
{
	var queueId = $("#processQueueSelect > input[type=hidden]").val();
	var id = $("#commonFiltersPanel input[type=hidden]").val();
	var title = $("#commonFiltersPanel .text-value").html();

	if (!id) {
		alert("Не выбран общий фильтр для импорта!");
		return;
	}

	$$.ajax
		.post("/user/process/queue.do?" + $$.ajax.requestParamsToUrl({"action":"queueSavedFilterSet","command":"importCommon","id":id,"queueId":queueId,"title":title}))
		.done(() => {
			location.reload();
		});
}

function deleteFilterFromCommons()
{
	if ( confirm("Вы уверены что хотите удалить общий фильтр?") )
	{
		var queueId = $("#processQueueSelect > input[type=hidden]").val();
		var id = $("#commonFiltersPanel input[type=hidden]").val();
		var title = $("#commonFiltersPanel .text-value").html();

		$$.ajax
			.post("/user/process/queue.do?" + $$.ajax.requestParamsToUrl({"action":"queueSavedFilterSet", "command":"deleteCommon", "id":id, "queueId":queueId, "title":title}))
			.done(() => {
				location.reload();
			});
	}
}

function showCommonFiltersMenu()
{
	if ( $("#commonFiltersPanel.box").css("display") == "none" )
	{
		$("#commonFiltersPanel.box").show();
		$("#commonFiltersPanel.box").css("display", "inline-block");
	}
	else
	{
		$("#commonFiltersPanel.box").hide();
	}
}

function getCheckedProcessIds()
{
	var processIds='';

	$( '#processQueueData input[name=processId]:checked' ).each(function()
	{
		if(processIds != '') processIds+=',';
		processIds += $(this).val();
	});

	return processIds;
}

// Drag&Drop filters------ start ----------
// TODO: Вынести в пространство имён и переписать короче, как в pl.blow.js
function filterHandleDragStart(e)
{
	this.style.opacity = '0.4';

	e.originalEvent.dataTransfer.effectAllowed = 'move';
	e.originalEvent.dataTransfer.setData('text/html', $(this).attr("id"));
}

function moreHandleDragStart(e)
{
	  this.style.opacity = '0.4';

	  e.originalEvent.dataTransfer.effectAllowed = 'move';
	  e.originalEvent.dataTransfer.setData('text/html', $(this) );
	  console.log("get from more");
}

function filterHandleDragOver(e)
{
	if (e.preventDefault)
	{
		e.preventDefault();
	}

	e.originalEvent.dataTransfer.dropEffect = 'move';
	return false;
}

function filterHandleDragEnter(e)
{
	this.classList.add('over');
}

function filterHandleDragLeave(e)
{
	this.classList.remove('over');
}

function filterHandleDragEnd(e)
{
	this.style.opacity = '';
}

function filterHandleDrop(e)
{
	if (e.preventDefault) e.preventDefault();
	if (e.stopPropagation) e.stopPropagation();

	var filterButtonId = e.originalEvent.dataTransfer.getData('text/html');
	var allDraggableButtons = $("#savedFilters div[draggable=true]").length;
	var rareDraggableButtons = $("div.dropFilterArea ul div").length;

	if ( $(this).attr("id") == "savedFilters" )
	{
		moveFilterToMain( this, filterButtonId, e );
	}
	else if ( ( allDraggableButtons - rareDraggableButtons ) == 1  )//Если на панели осталсz один фильтр и его пытаются перенести в редко используемые
	{
		alert("Вы не можете сделать все фильтры редко используемыми.");
	}
	else
	{
		moveFilterToRare( this, filterButtonId );
	}
	return false;
}

function moreHandleDrop(e)
{
	if (e.stopPropagation)
	{
		e.stopPropagation();
	}

	var moreButton = e.originalEvent.dataTransfer.getData('text/html');
	$("#dropMoreArea").append( $(moreButton) );

	return false;
}

function moveFilterToRare( container, filterButtonId, firstLoad )
{
	if ( firstLoad == undefined )
	{
		firstLoad = false;
	}

	var filterButton = $("div[id="+filterButtonId+"][draggable=true]");

	if ( $(filterButton).attr("class") == "btn-blue" )
	{
		$(".dropFilterArea > .text-value").html( $(filterButton).html() );
	}

	$(filterButton).attr("style", "display: block");

	$(filterButton).click(function(event)
	{
		$(".dropFilterArea > .drop").hide();
		$(".dropFilterArea > .text-value").html( $(filterButton).html() );
		event.stopPropagation();
	});

	$(container).find("ul[class=drop]").prepend( $(filterButton) );

	if( !firstLoad )
	{
		setFilterStatusRare( filterButtonId, true );
	}
}

function moveFilterToMain( container, filterButtonId, e )
{
	var xPosition = e.originalEvent.clientX;
	$$.debug('queueFilterDrag', "Drop to: ", xPosition);

	var filterButton = $("div[id="+filterButtonId+"][draggable=true]");

	var maxXPos = 0;
	var $putBefore = undefined;

	$(container).find(">div.btn-white,>div.btn-blue").each(function(){
		var x = $(this).position().left;
		if( x < xPosition) {
			maxXPos = x;
			$putBefore = $(this);
		}
	});

	$$.debug('queueFilterDrag', "Put before: ", $putBefore);

	if ($putBefore) {
		$putBefore.before($(filterButton).attr("style", "display: inline-block;").off("click"));
	} else {
		$(container).prepend($(filterButton).attr("style", "display: inline-block;").off("click"));
	}

	if ( $(filterButton).attr("class") == "btn-blue" )
	{
		$(".dropFilterArea .text-value").html("");
	}
	setFilterStatusRare( filterButtonId, false );

	updateSavedFiltersOrder(container);
}

function updateSavedFiltersOrder(container) {
	var order = "";
	$(container).find(">div.btn-white,>div.btn-blue").each(function(){
		var id = $(this).attr("id");
		if (id) {
			order += "&setId=" + id;
		}
	});

	var queueId = $("#processQueueSelect > input[type=hidden]").val();
	$$.ajax.post("/user/process/queue.do?action=queueSavedFilterSet&command=updateFiltersOrder&queueId=" + queueId + order);
}

function setFilterStatusRare( filterId, value )
{
	var queueId = $("#processQueueSelect > input[type=hidden]").val();
	$$.ajax.post("/user/process/queue.do?" + $$.ajax.requestParamsToUrl({"action":"queueSavedFilterSet","command":"setRareStatus","filterId":filterId, "rare":value, "queueId":queueId}));
}

//Drag&Drop filters --- end ----


function updateSelectedFilterAndOpen( queueId, filterId )
{
	$$.shell.followLink("/user/process/queue", "");
	$$.ajax
		.post("/user/process/queue.do?action=queueSavedFilterSet" + $$.ajax.requestParamsToUrl({"command":"select", "id":filterId, "queueId":queueId}))
		.done(() => {
			updateSelectedQueue(queueId);
			$$.process.queue.showSelected(queueId);
		});
}

// counters

function generateUrlForFilterCounter() {
	var queueId = $("#processQueueSelect > input[type=hidden]").val();

	var excludeRareFilters = [];
	$(".dropFilterArea .drop div[draggable='true']").each(function () {
		excludeRareFilters.push($(this).attr("id"))
	});

	var urlArray = [];
	$("#processQueueFilter").find("form[id^=" + queueId + "-][action!='/user/process/queue.do']").each(function () {
		var buttonId = $(this).attr("id").split("-")[1];
		for (var i = 0; i < excludeRareFilters.length; i++) {
			if (buttonId == excludeRareFilters[i]) {
				return;
			}
		}
		urlArray.push(buttonId + ":" + queueId + ":" + $$.ajax.formUrl(this));
	});

	$("#filterCounterPanel a").each(function () {
		var filterButtonId = $(this).attr("id").split("-")[2];
		var filterQueueId = $(this).attr("queue");
		var filterUrl = $(this).attr("url");
		var concatedUrl = filterButtonId + ":" + filterQueueId + ":" + filterUrl;

		if ($.inArray(concatedUrl, urlArray) == -1 && filterButtonId != undefined && filterQueueId != undefined && filterUrl != undefined) {
			urlArray.push(concatedUrl);
		}
	});

	return urlArray;
}

function processFilterCounterEvents(event)
{
	var queueId = $("#processQueueSelect > input[type=hidden]").val();

	var filters = event["filters"];
	$("#filterCounterPanel").empty();
	for ( var key in filters )
	{
		addCounterToPanel( key, filters[key]["queueId"], filters[key]["title"], filters[key]["queueName"], filters[key]["color"], filters[key]["url"], true );
	}

	var count = event["count"];

	for( var queueId in count )
	{
		for( var btnId in count[queueId] )
		{
			$("#savedFilters > div[draggable=true]").each( function()
			{
				if ( count[queueId][btnId] == -1 )
				{
					count[queueId][btnId] = "X";
				}

				var buttonId = $(this).attr("id");

				if ( buttonId == btnId )
				{
					var textValue = $(this).html();
					if ( textValue.split("] ").length > 1 )
					{
						$(this).html( "[" + count[queueId][btnId] + "] " + textValue.split("] ")[1] );
					}
					else
					{
						$(this).html( "[" + count[queueId][btnId] + "] " + textValue );
					}
				}
			});
			$("#panelFilterCounter-" + queueId + "-" + btnId ).html( count[queueId][btnId] );
		}
	};
}

function addCounterToPanel( buttonId, queueId, buttonName, queueName, color, url, addOnStart )
{
	var filterQueueName = queueName;
	var filterButtonName = buttonName;
	var filterButtonCount = "X";
	var filterButtonId = buttonId;
	var filterColor = color;

	if ( !addOnStart )
	{
		filterQueueName = $("#processQueueSelect > .text-value > div").html() + ":";
		queueId = $("#processQueueSelect > input[type=hidden]").val();
		filterButtonId = $("#savedFilters:visible .btn-blue").attr("id");

		if ( filterButtonId == undefined )
		{
			alert("Вы не выбрали ни одного фильтра");
			return;
		}

		if ( $("#savedFilters .btn-blue").html().indexOf("]") > -1 )
		{
			filterButtonName = $("#savedFilters:visible .btn-blue").html().split("] ")[1];
		}
		else
		{
			filterButtonName = $("#savedFilters:visible .btn-blue").html();
		}

		if ( filterButtonCount == undefined )
		{
			filterButtonCount = "X";
		}

		if( url == undefined )
		{
			url = $("#"+queueId+"-"+filterButtonId).attr("action");
			if ( url == undefined ){alert("Ошибка добавления, попробуйте еще раз."); return;}
		}
		$("#colorPickerModal > div.colorPicker-picker").css("background-color", "#005589");
		$("#colorPickerModal > div.colorPicker-picker").css("display", "inline-block");
		$("#colorPickerModal").show();

		$( "#colorPickerModal" ).dialog({
			modal: true,
			buttons:
			{
				Ok: function()
				{
					filterColor = $("#colorPickerModal > div.colorPicker-picker").css("background-color");
					if ( filterColor == undefined ){ filterColor = "" };
					$("#colorPickerModal").hide();
					$( this ).dialog( "close" );
					$("#filterCounterPanel").prepend("<a style='margin-left: 4px; color:"+filterColor+";' id='panelFilterCounter-" + queueId + "-" + filterButtonId + "' queue="+queueId+" url="+url+" title='"+ filterQueueName + " " + filterButtonName +"' onclick='updateSelectedFilterAndOpen("+ queueId +","+ filterButtonId+ ")' href='#'>"+filterButtonCount+" </a>");
					$$.ajax.post("/user/process/queue.do?action=queueSavedFilterSet" + $$.ajax.requestParamsToUrl({"command":"setStatusCounterOnPanel", "filterId":filterButtonId, "queueId":queueId, "color":filterColor, "statusCounterOnPanel":true, "title": filterButtonName, "queueName": filterQueueName}));
				},
				Cancel: function()
				{
					filterColor = "";
					$("#colorPickerModal").hide();
					$( this ).dialog( "close" );
					$("#filterCounterPanel").prepend("<a style='margin-left: 4px; color:"+filterColor+";' id='panelFilterCounter-" + queueId + "-" + filterButtonId + "' queue="+queueId+" url="+url+" title='"+ filterQueueName + " " + filterButtonName +"' onclick='updateSelectedFilterAndOpen("+ queueId +","+ filterButtonId+ ")' href='#'>"+filterButtonCount+" </a>");
					$$.ajax.post("/user/process/queue.do?action=queueSavedFilterSet" + $$.ajax.requestParamsToUrl({"command":"setStatusCounterOnPanel", "filterId":filterButtonId, "queueId":queueId, "color":filterColor, "statusCounterOnPanel":true, "title": filterButtonName, "queueName": filterQueueName}));
				}
			}
		});
	}
	else
	{
		if ( filterColor == undefined ){ filterColor = "" };
		$("#filterCounterPanel").append("<a style='margin-left: 4px; color:"+filterColor+";' id='panelFilterCounter-" + queueId + "-" + filterButtonId + "' queue="+queueId+" url="+url+" title='"+ filterQueueName + " " + filterButtonName +"' onclick='updateSelectedFilterAndOpen("+ queueId +","+ filterButtonId+ ")' href=''>"+filterButtonCount+" </a>");
	}

	$("#filterCounterPanel > a").each(function()
			{
				$(this).click(function(event)
						{event.preventDefault();});
			});
}

function delCounterFromPanel()
{
	var queueId = $("#processQueueSelect > input[type=hidden]").val();
	var filterButtonId = $("#savedFilters:visible .btn-blue").attr("id");

	if ( filterButtonId == undefined )
	{
		alert("Вы не выбрали ни одного фильтра");
		return;
	}

	if( !confirm( 'Удалить счетчик фильтра с панели?' ) )
	{
		return;
	}

	$("a#panelFilterCounter-" + queueId + "-" + filterButtonId ).remove();

	$$.ajax.post("/user/process/queue.do?action=queueSavedFilterSet" + $$.ajax.requestParamsToUrl({"command":"setStatusCounterOnPanel", "filterId":filterButtonId, "queueId":queueId, "statusCounterOnPanel":false}));
}

// обработка клиентских событий - должна быть в конце
// все новые функции добавлять перед этим комментарием!!!
addEventProcessor('ru.bgcrm.event.client.ProcessChangedEvent', processProcessClientEvents);
addEventProcessor('ru.bgcrm.event.client.ProcessOpenEvent', processProcessClientEvents);
addEventProcessor('ru.bgcrm.event.client.ProcessCloseEvent', processProcessClientEvents);
addEventProcessor('ru.bgcrm.event.client.ProcessCurrentQueueRefreshEvent', processProcessClientEvents);
addEventProcessor('ru.bgcrm.event.client.TemporaryObjectEvent', processProcessClientEvents);
addEventProcessor('ru.bgcrm.event.client.FilterCounterEvent', processFilterCounterEvents);

function processProcessClientEvents( event )
{
	switch( event.className )
	{
		case 'ru.bgcrm.event.client.ProcessChangedEvent':
		case 'ru.bgcrm.event.client.ProcessOpenEvent':
		{
			openProcess(event.id);
			break;
		}
		case 'ru.bgcrm.event.client.ProcessCloseEvent':
		{
			// FIXME: Было с табами так.
			// removeClosableTab("processTabs", event.id);

			removeCommandDiv( "process-" + event.id );

			$$.closeObject = null;
			window.history.back();

			break;
		}
		case 'ru.bgcrm.event.client.ProcessCurrentQueueRefreshEvent':
		{
			$("#content > #process-queue #processQueueData button[name='pageControlRefreshButton']").first().click();
			break;
		}
		case 'ru.bgcrm.event.client.TemporaryObjectEvent':
		{
			$.each( event.processIds, function()
			{
				if( $('#content #process-'.concat( this )).length == 0 )
				{
					openProcess(this);
				}
			})
		}
	}
}