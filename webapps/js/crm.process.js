/*
 * Процессы и очереди.
 */
$$.process = new function() {
	const open = (id) => {
		$$.shell.contentLoad("process#" + id);
	};
	
	// доступные функции
	this.open = open;
};

function processQueueChanged(savedSetId) {
	var queueId = $("#processQueueSelect > input[type=hidden]").val();
	
	// поиск и создание нужного фильтра и кнопок
	if ($("#processQueueFilter > div#" + queueId).length == 0) {
		openUrlTo("process.do?action=queueGet&id=" + queueId,
				$('#processQueueFilter'), {
					append : true
				});
	}
	
	if (savedSetId == undefined) {
		// дерево типов для создания
		openUrlTo("process.do?action=typeTree&queueId=" + queueId,
				$("#processQueueCreateProcess > #typeTree"));
	}
	
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

	$(function() {
		var url = formUrl($filter.find("form[active=1]"));
		if( url )
		{
			openUrlTo(url, $('#processQueueData'));
			addProcessQueueIdToUrl( queueId );		
		}
	});
}

function processQueueClearHiddenFilters($form) {
	uiComboInputs($form.find('.filtersSelect')).each( function() {
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
	uiComboInputs($form.find('.filtersSelect')).each( function() {
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

//помещение в URL #<код очереди>
function addProcessQueueIdToUrl( queueId )
{
	var state = history.state;
	if (state)
	{
		var pos = state.href.indexOf( '#' );
		state.href = ( pos < 0 ? state.href : state.href.substring( 0, pos ) ) + "#" + queueId;
		history.replaceState( state, null, state.href )
	}
}

function processQueueFilterSetSelect(queueId) {
	$('#processQueueFilter').find('div#' + queueId).remove();
	// $('#processQueueButtons').find('span#' + queueId ).remove();
	processQueueChanged();
}

function updateExecutors($groups, $executors, paramNameGroup,
		paramNameExecutor, showEmpty, roleId, savedExecutors) {
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
			openUrlTo(url, $executors);
			
			// отмечаем сохраненные значения с прошлой выборки
			if ( savedExecutors )
			{
				savedExecutors = savedExecutors.split(",");
				savedExecutors.forEach(function(executorId, i , arr)
				{
					$executors.find("input[value="+executorId+"][name=" + paramNameExecutor + "]").prop('checked', true);
				});
			}
				
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

	openUrlTo("process.do?action=processCreateGroups&typeId=" + nodeId, $(el)
			.closest("#typeTree").parent().find("#groupSelect"));
	openUrlTo("process.do?action=processRequest&typeId=" + nodeId, $(el)
			.closest("#typeTree").parent().find("#additionalParamsSelect"));
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
	var html = '<table class="data" style="width: 100%;">\
			<tr>\
				<td>&nbsp;</td>\
				<td>Тип</td>\
				<td width="100%">Наименование</td>\
			</tr>';

	var objects = []; 
		
	if( !(additionalLinksForAdd) )
	{
		objects = openedObjectList({
			"typesExclude" : [ 'process', 'workarea' ],
			"selected" : selectedValues
		});
	}
	else 
	{
		objects = additionalLinksForAdd;
	}

	for( var d in objects ) 
	{
		var data = objects[d];

		var objectType = data.objectType;
		var objectId = data.id;

		/* contract_ds-333
		 * bgbilling-commonContract-333
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
					<form action="link.do">\
						<input type="hidden" name="action" value="addLink"/>\
						<input type="hidden" name="objectType" value="process"/>\
						<input type="hidden" name="id" value="' + processId + '"/>\
						<input type="hidden" name="linkedObjectId" value="' + objectId + '"/>\
						<input type="hidden" name="linkedObjectTitle" value="' + encodeHtml(data.title) + '"/>\
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

	/*
	 * html += '<tr><td colspan="3" align="center">Ручная привязка</td></tr>\
	 * <tr>\ <td colspan="3">\ <form action="link.do">\ <input type="checkbox"
	 * name="check"/>\ <input type="hidden" name="action" value="addLink"/>\
	 * <input type="hidden" name="objectType" value="process"/>\ <input
	 * type="hidden" name="id" value="' + processId + '"/>\ Тип <input
	 * name="linkedObjectType"/>\ ID <input name="linkedObjectId"/>\ Заголовок
	 * <input name="linkedObjectTitle"/>\ </form>\ </td>\ </tr>';
	 */

	html += '</table>';

	$uiid.html(html);
}

function processesToLinkTable($uiid, processId, linkType) {
	var html = '<table class="data mt1" style="width: 100%;">\
			<tr>\
				<td>&nbsp;</td>\
				<td width="100%">Наименование</td>\
			</tr>';

	var processes = openedObjectList({
		"typesInclude" : [ 'process' ]
	});
	for ( var i in processes) {
		var process = processes[i];

		// привязка самого к себе запрещена
		if (process.id == processId) {
			continue;
		}

		html += '<tr>\
				<td>\
					<form action="link.do" class="mt1">\
						<input type="hidden" name="action" value="addLink"/>\
						<input type="hidden" name="objectType" value="process"/>\
						<input type="hidden" name="id" value="'
				+ processId
				+ '"/>\
						<input type="hidden" name="linkedObjectType" value="'
				+ linkType
				+ '"/>\
						<input type="hidden" name="linkedObjectId" value="'
				+ process.id
				+ '"/>\
						<input type="hidden" name="linkedObjectTitle" value=""/>\
						<input type="checkbox" name="check"/>\
					</form>\
				</td>\
				<td>'
				+ process.title + '</td>\
			</tr>';
	}

	$uiid.html(html);
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
		
		sendAJAXCommandWithParams("/user/process.do?action=queueSavedPanelSet", {command: "add", queueId: id, queueTitle: title});
	}

	$( '#processQueueSelect' ).before(	"<div onclick=$('#processQueueSelect').find('input[type=hidden]').val("+id+");processQueueChanged();updateSelectedQueue("+id+"); class='btn-white btn-panel'>" +
			"<input type='hidden' value="+id+" />" +
			"<span class='icon' style='margin-right:5px;'>" +
			"<img src='/images/cross.png'></span>" +
			"<span title='"+title+"' class='title'>" + title + "</span>" +
		"</div>"  );
	
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
	sendAJAXCommandWithParams("/user/process.do?action=queueSavedPanelSet", {command: "delete", queueId: id});
	
	$("#processQueueSelect").find(".drop").append("<li value="+id+" onclick='updateSelectedQueue("+id+");showSelectedQueue("+id+");'><div style='display: inline;'>"+ title +
			"</div><div class='icon-add'></div></li>");
	
	$("#processQueueSelect .icon-add").click(function(event){
		event.stopPropagation();
		addToPanelScript(id,title,true);
		$(this).parent().remove();	
	});	
}

function updateSelectedQueue(id)
{	
	sendAJAXCommandWithParams( "/user/process.do?action=queueSavedPanelSet", { 
				command: "updateSelected", 
				queueId: id
	});
	
	$(".btn-panel input[type=hidden]").each(function()
	{
		if ( $(this).val() == id )
		{
			$("#processQueueSelect").find(".text-value").empty();
		}
	});
}

function showSelectedQueue(id)
{
	// очередь может быть сохранена, как последняя открытая, а потом удалена
	if ($("#processQueueSelect li[value=" + id + "]").length === 0)
		return;
	
	if (id) {
		$(".btn-panel input[type=hidden]").each( function ()
		{
			if( $(this).val() == id )
			{
				$(this).parent().removeClass("btn-white");
				$(this).parent().addClass("btn-blue");
			}
			else
			{
				$(this).parent().removeClass("btn-blue");
				$(this).parent().addClass("btn-white");
			}
		});
		
		if( id != $('#processQueueSelect').find('input[type=hidden]').val() )
		{
			$('#processQueueSelect').find('input[type=hidden]').val(id);
		}
		
		if( $(".btn-panel.btn-blue").length < 1 )
		{
			$("#processQueueSelect .drop li").each(function()
			{
				if ( $(this).val() == id )
				{
					$("#processQueueSelect > .text-value").html( $(this).html() );
					$('#processQueueSelect').find('.text-value div.icon-add').remove();
					return;
				}
			});			
		}
	}
	
	processQueueChanged();
}

/*function showCommonFilters()
{
	var queueId = $("#processQueueSelect > input[type=hidden]").val();
	
	// отображение нужного фильтра, нужных кнопок
	var $filter = $("#processQueueFilter").find("div#" + queueId);
	$filter.find("*[id=savedCommonFilters]").toggle(true);
	$filter.find("*[id=savedFilters]").toggle(false);
	$filter.find("*[id='" + queueId + "-0']").toggle(false);

	$filter.find("#savedCommonFilters button").each(function() 
	{
		$(this).addClass("btn-white");
	});
}*/

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
	
	sendAJAXCommandWithParams("process.do?",{"action":"queueSavedFilterSet","command":"addCommon","url":url,"queueId":queueId,"title":title})

	location.reload();
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

	sendAJAXCommandWithParams("process.do?",{"action":"queueSavedFilterSet","command":"importCommon","id":id,"queueId":queueId,"title":title});

	location.reload();
}

function deleteFilterFromCommons()
{
	if ( confirm("Вы уверены что хотите удалить общий фильтр?") )
	{
		var queueId = $("#processQueueSelect > input[type=hidden]").val();
		var id = $("#commonFiltersPanel input[type=hidden]").val();
		var title = $("#commonFiltersPanel .text-value").html();

		sendAJAXCommandWithParams("process.do?", {"action":"queueSavedFilterSet", "command":"deleteCommon", "id":id, "queueId":queueId, "title":title});

		location.reload();
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
	var allDraggableButtons = $("#savedFilters div[draggable=true]").size();
	var rareDraggableButtons = $("div.dropFilterArea ul div").size();
	  
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
	bgcrm.debug('queueFilterDrag', "Drop to: ", xPosition);
	
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
	
	bgcrm.debug('queueFilterDrag', "Put before: ", $putBefore);
	
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
	sendAJAXCommandAsync("process.do?action=queueSavedFilterSet&command=updateFiltersOrder&queueId=" + queueId + order);
}

function setFilterStatusRare( filterId, value )
{
	var queueId = $("#processQueueSelect > input[type=hidden]").val();
	sendAJAXCommandWithParams("process.do?",{"action":"queueSavedFilterSet","command":"setRareStatus","filterId":filterId, "rare":value, "queueId":queueId});
}

//Drag&Drop filters --- end ----


function updateSelectedFilterAndOpen( queueId, filterId )
{
	bgerp.shell.followLink("processQueue", "");
	sendAJAXCommandWithParams("/user/process.do?action=queueSavedFilterSet", {"command":"select", "id":filterId, "queueId":queueId});
	updateSelectedQueue( queueId );
	showSelectedQueue( queueId );
}


// обработка клиентских событий - должна быть в конце
// все новые функции добавлять перед этим комментарием!!!
addEventProcessor('ru.bgcrm.event.client.ProcessChangedEvent', processProcessClientEvents);
addEventProcessor('ru.bgcrm.event.client.ProcessOpenEvent', processProcessClientEvents);
addEventProcessor('ru.bgcrm.event.client.ProcessCloseEvent', processProcessClientEvents);
addEventProcessor('ru.bgcrm.event.client.ProcessCurrentQueueRefreshEvent', processProcessClientEvents);
addEventProcessor('ru.bgcrm.event.client.TemporaryObjectEvent', processProcessClientEvents);

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
			
			bgcrm.closeObject = null;
			window.history.back();
			
			break;
		}
		case 'ru.bgcrm.event.client.ProcessCurrentQueueRefreshEvent':
		{
			$("#content > #processQueue #processQueueData button[name='pageControlRefreshButton']").click();
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