<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="queue" value="${form.response.data.queue}"/>

<c:set var="formAction" value="/user/process.do"/>

<c:set var="key" value="queueCurrentSavedFilterSet.${queue.id}"/>
<c:set var="currentSavedFilterSetId" value="${u:int( ctxUser.personalizationMap[key] )}"/>

<c:set var="config" value="${u:getConfig( ctxUser.personalizationMap, 'ru.bgcrm.model.process.queue.config.SavedFiltersConfig' )}"/>
<c:set var="currentSavedFilterSet" value="${config.savedFilterSetMap[u:int( currentSavedFilterSetId )]}"/>

<c:choose>
	<c:when test="${currentSavedFilterSetId gt 0 and not empty currentSavedFilterSet}">
		 <c:set var="hideWhenSavedFilter">style="display: none;"</c:set>
	</c:when>
	<c:otherwise>
		<c:set var="hideWhenFullFilter">style="display: none;"</c:set>
	</c:otherwise>
</c:choose>

<div id="${queue.id}" style="display:none;" class="in-inline-block" ${hideForm}>
	<c:set var="selectorForm">#processQueueFilter form[id=${queue.id}-0]</c:set>
	<c:set var="sendCommand">openUrlTo( formUrl( $('${selectorForm}') ), $('#processQueueData') );</c:set>

	<c:set var="uiidMoreButton" value="${u:uiid()}"/>
	<c:set var="uiidMoreMenu" value="${u:uiid()}"/>
	<c:set var="saveFilterFormUiid" value="${u:uiid()}"/>
	<c:set var="savedFiltersFormsDivUiid" value="${u:uiid()}"/>

	<c:set var="addActionFormUiid" value="${u:uiid()}"/>

	<jsp:useBean id="curdate" class="java.util.Date"/>

	<button class="btn-white combo mr1 mb1" id="${uiidMoreButton}">
		<div class="text-value">${l.l('Ещё')}</div>
		<div class="icon"><img src="/images/arrow-down.png"/></div>
		<script>
			$(function () {
				$$.ui.menuInit($("#${uiidMoreButton}"), $("#${uiidMoreMenu}"), "left");
			})
		</script>
	</button>

	<div style="height: 0px; max-height: 0px; width: 0px; max-width: 0px;">
		<ul style="display: none; z-index: 2000;" id="${uiidMoreMenu}">
			<c:if test="${queue.configMap['allowCreateProcess'] ne 0}">
				<li draggable="true"><a onclick="$('#processQueueShow').hide(); $('#processQueueCreateProcess').show();">Создать процесс</a></li>
			</c:if>

			<c:if test="${not empty queue.getMediaColumnList('print') or not empty queue.configMap['allowPrint']}">
				<c:set var="script">
					var savedSetId = $('#processQueueFilter > #${queue.id}').find( '#savedFilters:visible .btn-blue' ).attr( 'id' );
					if( !savedSetId )
					{
						savedSetId = 0;
					}

					window.location.href = formUrl( $('#processQueueFilter form#' + ${queue.id} + '-' + savedSetId )) +'&print=1&processIds=' + getCheckedProcessIds();
				</c:set>
				<li><a onclick="${script}">Печать</a></li>
			</c:if>

			<%-- обращение к плагину, не совсем корректно, в перспективе лучше сделать точку расширения --%>
			<c:set var="printConfig" value="${u:getConfig( queue.configMap, 'ru.bgcrm.plugin.report.model.PrintQueueConfig' )}"/>
			<c:if test="${not empty printConfig and not empty printConfig.printTypes}">
				<c:forEach var="item" items="${printConfig.printTypes}">
					<c:set var="script">
						var savedSetId = $('#processQueueFilter > #${queue.id}').find( '#savedFilters:visible .btn-blue' ).attr( 'id' );
						if( !savedSetId )
						{
							savedSetId = 0;
						}

						window.location.href = formUrl( $('#processQueueFilter form#' + ${queue.id} + '-' + savedSetId )) +'&print=1&printTypeId=${item.id}&processIds=' + getCheckedProcessIds();
					</c:set>
					<li><a onclick="${script}">${item.title}</a></li>
				</c:forEach>
			</c:if>

			<li draggable="true" id="savedFilters" ${hideWhenFullFilter}><a onclick="$$.process.queue.changed(0);">Фильтр - вернуться в полный</a></li>
			<c:set var="getSavedSetId">
				var savedSetId = $('#processQueueFilter > #${queue.id}').find( '#savedFilters div.btn-blue' ).attr( 'id' ) ;
				if( !savedSetId )
				{
					alert( 'Фильтр не выбран!' );return;
				}
			</c:set>
			<li draggable="true" id="savedFilters" ${hideWhenFullFilter}>
				<a onclick="if( !confirm( 'Удалить сохранённый фильтр?' ) ){ return; }
					${getSavedSetId}
					$$.ajax.post('/user/process/queue.do?action=queueSavedFilterSet&queueId=${queue.id}&id=' + savedSetId + '&command=delete').done(() => {
						processQueueFilterSetSelect(${queue.id})
					})">Фильтр - удалить</a>
			</li>
			<li draggable="true" id="savedFilters" ${hideWhenFullFilter}>
				<a onclick="
					${getSavedSetId}
					$$.ajax.post('/user/process/queue.do?action=queueSavedFilterSet&queueId=${queue.id}&id=' + savedSetId + '&command=toFullFilter').done(() => {
						$('#processQueueFilter > div#${queue.id}').remove();
						$$.process.queue.changed(0);
					})">Фильтр - извлечь в полный</a>
			</li>
			<li draggable="true" id="savedFilters" ${hideWhenFullFilter}><a onclick="addCounterToPanel();">Фильтр - счетчик на панель</a></li>
			<li draggable="true" id="savedFilters" ${hideWhenFullFilter}><a onclick="delCounterFromPanel();">Фильтр - счетчик убрать с панели</a></li>

			<li draggable="true" id="${queue.id}-0" ${hideWhenSavedFilter}><a onclick="$$.process.queue.changed(1);">Фильтр - сохранённые</a></li>
			<li draggable="true" id="${queue.id}-0" ${hideWhenSavedFilter}><a onclick="$('#${saveFilterFormUiid}').css('display','');">Фильтр - сохранить</a></li>
			<li draggable="true" id="${queue.id}-0" ${hideWhenSavedFilter}>
				<a onclick="if( !confirm( 'Сбросить полный фильтр?' ) ){ return; }
					$$.ajax.post('/user/process/queue.do?action=queueSavedFilterSet&queueId=${queue.id}&id=0&command=toFullFilter').done(() => {
						$('#processQueueFilter > div#${queue.id}').remove();
						$$.process.queue.changed(0);
					})">Фильтр - сброс</a>
			</li>

			<c:forEach var="processor" items="${queue.processorMap.values()}">
				<li>
					<c:set var="script">
						<c:choose>
							<c:when test="${not empty processor.page}">
								$('div[id*=${addActionFormUiid}]').hide();
								$('#${addActionFormUiid}-${processor.id}').show().trigger('show');
							</c:when>
							<c:otherwise>
								$('#${addActionFormUiid}-${processor.id} #okButton').click();
							</c:otherwise>
						</c:choose>
					</c:set>
					<a onClick="${script}; return false;">${processor.title}</a>
				</li>
			</c:forEach>
			<c:if test="${not empty queue.getMediaColumnList('xls')}">
				<c:set var="xls">
						var savedSetId = $('#processQueueFilter > #${queue.id}').find( '#savedFilters:visible .btn-blue' ).attr( 'id' );
						if( !savedSetId )
						{
							savedSetId = 0;
						}
						window.location.href = formUrl( $('#processQueueFilter form#' + ${queue.id} + '-' + savedSetId )) +'&xls=1';
				</c:set>
				<li><a onclick="${xls}">Выгрузка в Excel</a></li>
			</c:if>
		</ul>
	</div>

	<%-- панель для кнопок из Ещё --%>
	<div id="dropMoreArea" style="display: inline-block;">

		<script>
			$("#dropMoreArea").attr("title", "Вы можете перетащить сюда кнопки из Ещё");

			$( "#processQueueFilter > div > div > ul > li[draggable=true]").each(function()
			{
				$(this).on('dragstart', moreHandleDragStart);
				$(this).on('dragend', filterHandleDragEnd);
			});
			$("#dropMoreArea").on('dragover', filterHandleDragOver);
			$("#dropMoreArea").on('dragdrop', moreHandleDrop);
			$("#dropMoreArea").on('dragenter', filterHandleDragEnter);
			$("#dropMoreArea").on('dragleave', filterHandleDragLeave);
		</script>
	</div>

	<%-- доп. действия --%>
	<div style="display: inline-block;">
		<c:forEach var="processor" items="${queue.processorMap.values()}">
			<form action="/user/process/queue.do" id="${addActionFormUiid}-${processor.id}" style="display: none;" class="in-mb1-all">
				<input type="hidden" name="action" value="processCustomClassInvoke"/>
				<input type="hidden" name="queueId" value="${queue.id}"/>
				<input type="hidden" name="processorId" value="${processor.id}"/>
				<input type="hidden" name="processIds"/>

				<c:if test="${not empty processor.page}">
					<c:set var="processor" value="${processor}" scope="request"/>
					<jsp:include page="${processor.page}"/>
				</c:if>

				<c:set var="doScript">
					var processIds = getCheckedProcessIds();
					var debug = '';

					if( !processIds )
					{
						alert( 'Выберите процессы!' );
						return;
					}

					// Alt нажат
					if (bgcrm.keys.altPressed())
					{
						debug = '&debug=true';
					}

					this.form.processIds.value = processIds;

					if( '${processor.responseType}' === 'file' )
					{
						const w = window.open( formUrl(this.form) + '&responseType=stream' + debug, 'Print', 'menubar=1, scrollbars=1, height=800, width=800');
						<c:if test="${processor.configMap.openPrintDialog eq '1'}">
							w.addEventListener('load', () => {
								w.focus();
								w.print();
							}, false);
						</c:if>
					}
					else
					{
						$$.ajax.post($$.ajax.formUrl(this.form) + debug).done(() => { ${sendCommand} })
					}

					$(this.form).hide();
				</c:set>

				<button id="okButton" type="button" class="btn-grey" onclick="${doScript}">ОК</button>
				<button type="button" class="btn-grey ml05 mr1" onclick="$(this.form).hide();">Отмена</button>
			</form>
		</c:forEach>
	</div>

	<%-- сохранение фильтра --%>
	<form action="/user/process/queue.do" style="display: none;" id="${saveFilterFormUiid}" class="mb1 mr1">
		Название фильтра:
		<input type="hidden" name="action" value="queueSavedFilterSet"/>
		<input type="hidden" name="queueId" value="${queue.id}"/>
		<input type="hidden" name="command" value="add"/>
		<input type="hidden" name="url"/>
		<input type="text" name="title" size="20"/>

		<button type="button" class="btn-grey"
			onclick="if( this.form.title.value == '' ){ alert( 'Введите название!'); return; }
					this.form.url.value = $$.ajax.formUrl($('#processQueueFilter').find('form#${queue.id}-0'), ['page.pageIndex', 'savedFilterSetId']);
					$$.ajax.post(this.form).done(() => { processQueueFilterSetSelect(${queue.id}) })">OK</button>
		<button type="button" class="btn-grey" onclick="$(this.form).hide()">Отмена</button>
	</form>

	<%-- сохранённые фильтры --%>
	<div id="savedFilters" ${hideWhenFullFilter} class="in-mb1-all mr1 in-mr05 dropFilterArea">
		<c:forEach var="saved" items="${config.queueSavedFilterSetsMap[queue.id]}">
			<%-- old URLs start from process.do, after some intermediate version /user/process.do --%>
			<form 
				action="${saved.url.replace('/user/process.do', '/user/process/queue.do').replace('process.do', '/user/process/queue.do')}" 
				id="${queue.id}-${saved.id}" ${currentSavedFilterSetId eq saved.id ? "active='1'" : ""} style="display: none;">
				<input type="hidden" name="savedFilterSetId" value="${saved.id}"/>
			</form>
				<div draggable="true" id="${saved.id}" class="${currentSavedFilterSetId eq saved.id ? 'btn-blue' : 'btn-white'}" onclick="$$.process.queue.changed(${saved.id})">${saved.title}</div>
				<c:if test="${saved.rare}">
					<script>
						moveFilterToRare( $("div.combo.dropFilterArea"), ${saved.id}, true );
					</script>
				</c:if>
		</c:forEach>

		<u:sc>
			<c:set var="styleClass" value="dropFilterArea"/>
			<c:set var="style" value="display: inline-block"/>
			<c:set var="prefixText" value="${l.l('Фильтр')}:"/>
			<c:set var="widthTextValue" value="40px"/>
			<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
		</u:sc>

		<script>
			$("div.combo.dropFilterArea").attr("title", "Вы можете перетащить сюда редко используемые фильтры");

			$("div[draggable=true]").each(function(){
				$(this).on('dragstart', filterHandleDragStart);
				$(this).on('dragend', filterHandleDragEnd);
			});
			$(".dropFilterArea").on('dragover', filterHandleDragOver);
			$(".dropFilterArea").on('drop', filterHandleDrop);
			$(".dropFilterArea").on('dragenter', filterHandleDragEnter);
			$(".dropFilterArea").on('dragleave', filterHandleDragLeave);
		</script>

		<button class="btn-white" onclick="showCommonFiltersMenu()" title="Общие фильтры">*</button>

		<div id="commonFiltersPanel" class="box" style="display:none;">
			<button onclick="importFilterFromCommons()" title="Импорт себе общего фильтра" class="btn-white"><</button>
			<button onclick="exportFilterToCommons()" title="Экспорт своего фильтра в общие" class="btn-white">></button>
			<button onclick="deleteFilterFromCommons()" title="Удалить общий фильтр" class="btn-white">X</button>
			<u:sc>
				<c:set var="hiddenName" value="currentCommonFilter"/>
				<c:set var="prefixText" value="Общий фильтр:"/>
				<c:set var="list" value="${commonConfig.queueSavedCommonFilterSetsMap[queue.id]}"/>
				<c:set var="style" value="display: inline-block;"/>
				<c:set var="widthTextValue" value="100px"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
			</u:sc>
		</div>
	</div>

	<%-- Set используется для установки статусов отображаемым фильтрам --%>
	<c:set var="selectedFiltersStr" value="${ctxUser.personalizationMap['queueSelectedFilters'.concat(queue.id)]}"/>
	<c:set var="selectedFilters" value="${u:toIntegerSet( selectedFiltersStr )}"/>

	<%-- полный фильтр --%>
	<form action="/user/process/queue.do" id="${queue.id}-0" ${currentSavedFilterSetId le 0 ? "active='1'" : "style='display: none;'"} class="in-inline-block in-mr05 in-mb1-all">
		<input type="hidden" name="savedFilterSetId" value="0"/>
		<input type="hidden" name="selectedFilters" value="${selectedFiltersStr}"/>
		<input type="hidden" name="action" value="queueShow"/>
		<input type="hidden" name="pageableId" value="queue"/>

		<input type="hidden" name="id" value="${queue.id}"/>

		<u:sc export="filters">
			<c:set var="valuesHtml">
				<c:forEach var="filterFromList" items="${queue.filterList.filterList}">
					<c:if test="${filterFromList.type == 'code'}">
						<c:set var="filter" value="${filterFromList}"/>
						<c:set var="title" value="Код процесса"/>
						<c:set var="code">
							<c:choose>
								<c:when test="${not empty savedParamsFilters.get( 'code' ) }">
									<input type="text" value="${savedParamsFilters.get( 'code' )}" name="code" placeholder="Код" size="6" style="text-align: center;" onkeypress="if( enterPressed( event ) ){ ${sendCommand}; return false; }"/>
								</c:when>
								<c:otherwise>
									<input type="text" name="code" placeholder="Код" size="6" style="text-align: center;" onkeypress="if( enterPressed( event ) ){ ${sendCommand}; return false; }"/>
								</c:otherwise>
							</c:choose>
						</c:set>
						<%@ include file="filter_item.jsp"%>
					</c:if>

					<c:if test="${filterFromList.type == 'type'}">
						<c:set var="filter" value="${filterFromList}"/>

						<c:set var="title" value="Типы"/>
						<c:set var="code">
							<u:sc>
								<c:set var="prefixText" value="Тип:"/>
								<c:set var="paramName" value="type"/>
								<c:set var="list" value="${form.response.data.typeList}"/>
								<c:set var="map" value="${ctxProcessTypeMap}"/>
								<c:set var="showFilter" value="1"/>
								<c:set var="values" value="${filter.defaultValues}"/>
								<c:set var="available" value="${filter.availableValues}"/>
								<c:set var="widthTextValue" value="100px"/>
								<c:if test="${not empty savedParamsFilters.getSelectedValues( paramName ) }">
									<c:set var="values" value="${savedParamsFilters.getSelectedValues( paramName ) }"/>
								</c:if>
								<%@ include file="/WEB-INF/jspf/combo_check.jsp"%>
							</u:sc>
					 	</c:set>
						<%@ include file="filter_item.jsp"%>
					</c:if>

					<c:set var="statusFilterId" value="${u:uiid()}"/>

					<c:if test="${filterFromList.type == 'openClose'}">
						<c:set var="filter" value="${filterFromList}"/>
						<c:set var="title" value="Открыт"/>
						<c:set var="code">
							<u:sc>
								<span class="dontResetOnHideFilter"></span>

								<c:set var="valuesHtml">
									<li value="none">Все</li>
									<li value="open">Нет</li>
									<li value="close">Да</li>
								</c:set>

								<c:set var="hiddenName" value="openClose"/>
								<c:set var="value" value="${filter.defaultValue}"/>
								<c:if test="${not empty savedParamsFilters.get( hiddenName ) }">
									<c:set var="value" value="${savedParamsFilters.get( hiddenName ) }"/>
								</c:if>
								<c:set var="prefixText" value="Закрыт:"/>
								<c:set var="widthTextValue" value="40px"/>
								<c:set var="onSelect" value="${sendCommand}"/>
								<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
							</u:sc>
						</c:set>

						<c:set var="id" value="${statusFilterId}"/>
						<%@ include file="filter_item.jsp"%>
						<c:remove var="id"/>
					</c:if>

					<c:if test="${filterFromList.type == 'status'}">
						<c:set var="filter" value="${filterFromList}"/>
						<c:set var="title" value="Статусы"/>
						<c:set var="code">
							<u:sc>
								<c:set var="paramName" value="status"/>
								<c:set var="list" value="${ctxProcessStatusList}"/>
								<c:set var="map" value="${ctxProcessStatusMap}"/>
								<c:set var="values" value="${filter.defaultValues}"/>

								<c:if test="${not empty savedParamsFilters.getSelectedValues( paramName ) }">
									<c:set var="values" value="${savedParamsFilters.getSelectedValues( paramName ) }"/>
								</c:if>

								<c:set var="available" value="${filter.availableValues}"/>

								<c:set var="prefixText" value="Статус:"/>
								<c:set var="widthTextValue" value="100px"/>
								<%@ include file="/WEB-INF/jspf/combo_check.jsp"%>
							</u:sc>
						</c:set>

						<c:set var="id" value="${statusFilterId}"/>
						<%@ include file="filter_item.jsp"%>
						<c:remove var="id"/>
					</c:if>

					<c:remove var="statusFilterId"/>

					<c:if test="${filterFromList.type == 'description'}">
						<c:set var="filter" value="${filterFromList}"/>
						<c:set var="title" value="Описание"/>
						<c:set var="code">
							<c:choose>
								<c:when test="${not empty savedParamsFilters.get( 'description' ) }">
									<input type="text" value="${savedParamsFilters.get( 'description' )}" name="description" placeholder="Описание" size="20" style="text-align: center;" onkeypress="if( enterPressed( event ) ){ ${sendCommand}; return false; }"/>
								</c:when>
								<c:otherwise>
									<input type="text" name="description" placeholder="Описание" size="20" style="text-align: center;" onkeypress="if( enterPressed( event ) ){ ${sendCommand}; return false; }"/>
								</c:otherwise>
							</c:choose>
						</c:set>
						<%@ include file="filter_item.jsp"%>
					</c:if>

					<c:if test="${filterFromList.type == 'linkedCustomer:title'}">
						<c:set var="filter" value="${filterFromList}"/>

						<c:set var="title" value="Контрагент"/>
						<c:set var="code">
							<input type="text" value="${ savedParamsFilters.get( 'linkedCustomer:title' ) }" name="linkedCustomer:title" placeholder="Контрагент" size="20" onkeypress="if( enterPressed( event ) ){ ${sendCommand} }"/>
						</c:set>
						<%@ include file="filter_item.jsp"%>
					</c:if>

					<c:if test="${filterFromList.type == 'message:systemId'}">
						<c:set var="filter" value="${filterFromList}"/>

						<c:set var="title" value="${filter.title}"/>
						<c:set var="code">
							<input type="text" value="${savedParamsFilters.get('message:systemId')}" name="message:systemId" placeholder="${filter.title}" size="6" style="text-align: center;" onkeypress="if( enterPressed( event ) ){ ${sendCommand}; return false; }"/>
						</c:set>
						<%@ include file="filter_item.jsp"%>
					</c:if>

					<c:if test="${fn:startsWith(filterFromList.type, 'linkedCustomer:param:')}">
						<c:set var="filter" value="${filterFromList}"/>
						<c:set var="title" value="${not empty filter.title ? filter.title : filter.parameter.title}"/>
						<c:set var="code">
							<u:sc>
								<c:set var="id" value="${u:uiid()}"/>
								<c:set var="paramName" value="param${filter.parameter.id}value"/>
								<c:set var="list" value="${filter.parameter.listParamValues}"/>
								<c:set var="values" value="${filter.defaultValues}"/>
								<c:if test="${not empty savedParamsFilters.getSelectedValues( paramName ) }">
									<c:set var="values" value="${savedParamsFilters.getSelectedValues( paramName ) }"/>
								</c:if>
								<c:set var="available" value="${filter.availableValues}"/>
								<c:set var="showFilter" value="1"/>
								<c:set var="prefixText" value="${title}:"/>
								<c:set var="widthTextValue" value="150px"/>
								<%@ include file="/WEB-INF/jspf/combo_check.jsp"%>
							</u:sc>
						</c:set>
						<%@ include file="filter_item.jsp"%>
					</c:if>

					<c:if test="${filterFromList.type == 'create_date'}">
						<c:set var="filter" value="${filterFromList}"/>

						<c:set var="dateFrom" value="${filter.configMap['defaultValueFrom']}"/>
						<c:if test="${ dateFrom eq 'currentDate' }">
							<fmt:formatDate value="${curdate}" var="dateFrom"/>
						</c:if>
						<c:if test="${not empty savedParamsFilters.get('dateCreateFrom')}">
							<c:set var="dateFrom" value="${savedParamsFilters.get('dateCreateFrom')}"/>
						</c:if>

						<c:set var="dateTo" value="${filter.configMap['defaultValueTo']}"/>
						<c:if test="${ dateTo eq 'currentDate' }">
							<fmt:formatDate value="${curdate}" var="dateFrom"/>
						</c:if>
						<c:if test="${not empty savedParamsFilters.get('dateCreateTo')}">
							<c:set var="dateTo" value="${savedParamsFilters.get('dateCreateTo')}"/>
						</c:if>

						<c:set var="title" value="Дата создания"/>
						<c:set var="code">
							Создан с: <input type="text" name="dateCreateFrom" value="${dateFrom}"/>
							по: <input type="text" name="dateCreateTo" value="${dateTo}"/>
						</c:set>

						<u:sc>
							<c:set var="type" value="ymd"/>

							<c:set var="selector">${selectorForm} input[name='dateCreateFrom']</c:set>
							<c:set var="editable" value="1"/>
							<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>

							<c:set var="selector">${selectorForm} input[name='dateCreateTo']</c:set>
							<c:set var="editable" value="1"/>
							<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
						</u:sc>

						<%@ include file="filter_item.jsp"%>
					</c:if>

					<c:if test="${filterFromList.type == 'close_date' }">
						<c:set var="filter" value="${filterFromList}"/>

						<c:set var="dateFrom" value="${filter.configMap['defaultValueFrom']}"/>
						<c:if test="${ dateFrom eq 'currentDate' }">
							<fmt:formatDate value="${curdate}" var="dateFrom"/>
						</c:if>

						<c:if test="${not empty savedParamsFilters.get('dateCloseFrom')}">
							<c:set var="dateFrom" value="${savedParamsFilters.get('dateCloseFrom')}"/>
						</c:if>

						<c:set var="dateTo" value="${filter.configMap['defaultValueTo']}"/>
						<c:if test="${ dateTo eq 'currentDate' }">
							<fmt:formatDate value="${curdate}" var="dateTo"/>
						</c:if>

						<c:if test="${not empty savedParamsFilters.get('dateCloseTo')}">
							<c:set var="dateTo" value="${savedParamsFilters.get('dateCloseTo')}"/>
						</c:if>

						<c:set var="title" value="Дата закрытия"/>
						<c:set var="code">
							Закрыт с:
							<input type="text" name="dateCloseFrom" value="${dateFrom}"/>
							по <input type="text" name="dateCloseTo" value="${dateTo}"/>
						</c:set>

						<u:sc>
							<c:set var="type" value="ymd"/>

							<c:set var="selector">${selectorForm} input[name='dateCloseFrom']</c:set>
							<c:set var="editable" value="1"/>
							<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>

							<c:set var="selector">${selectorForm} input[name='dateCloseTo']</c:set>
							<c:set var="editable" value="1"/>
							<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
						</u:sc>

						<%@ include file="filter_item.jsp"%>
					</c:if>

					<c:if test="${filterFromList.type == 'status_date' }">
						<c:set var="filter" value="${filterFromList}"/>

						<c:set var="title" value="Дата статуса"/>
						<c:set var="code">
							<u:sc>
								<c:set var="hiddenName" value="dateStatusStatus"/>

								<c:if test="${not empty savedParamsFilters.get( hiddenName ) }">
									<c:set var="value" value="${savedParamsFilters.get( hiddenName ) }"/>
								</c:if>

								<c:set var="list" value="${ctxProcessStatusList}"/>
								<c:set var="map" value="${ctxProcessStatusMap}"/>
								<c:set var="values" value="${filter.defaultValues}"/>
								<c:set var="available" value="${filter.availableValues}"/>

								<c:set var="prefixText" value="Дата стат.:"/>
								<c:set var="widthTextValue" value="100px"/>
								<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
							</u:sc>
							<c:choose>
								<c:when test="${not empty savedParamsFilters.get( 'dateStatusFrom' ) }">
									с <input type="text" value="${savedParamsFilters.get( 'dateStatusFrom' )}" name="dateStatusFrom"/>
								</c:when>
								<c:otherwise>
									с <input type="text" name="dateStatusFrom"/>
								</c:otherwise>
							</c:choose>
							<c:choose>
								<c:when test="${not empty savedParamsFilters.get( 'dateStatusTo' ) }">
									по <input type="text" value="${savedParamsFilters.get( 'dateStatusTo' )}" name="dateStatusFrom"/>
								</c:when>
								<c:otherwise>
									по <input type="text" name="dateStatusTo"/>
								</c:otherwise>
							</c:choose>
						</c:set>

						<u:sc>
							<c:set var="type" value="ymd"/>

							<c:set var="selector">${selectorForm} input[name='dateStatusFrom']</c:set>
							<c:set var="editable" value="1"/>
							<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>

							<c:set var="selector">${selectorForm} input[name='dateStatusTo']</c:set>
							<c:set var="editable" value="1"/>
							<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
						</u:sc>

						<%@ include file="filter_item.jsp"%>
					</c:if>

					<c:if test="${filterFromList.type == 'groups'}">
						<c:set var="roleId" value=""/>
						<c:set var="groupListId" value="${u:uiid()}"/>
						<c:set var="executorListId" value="${u:uiid()}"/>
						<c:set var="groupParamName" value="group"/>
						<c:set var="executorParamName" value="executor"/>

						<c:set var="filter" value="${filterFromList}"/>
						<%@ include file="filter_groups.jsp"%>
					</c:if>

					<c:if test="${filterFromList.type == 'executors'}">
						<c:set var="filter" value="${filterFromList}"/>

						<%@ include file="filter_executors.jsp"%>
					</c:if>

					<c:if test="${filterFromList.type.startsWith('date')}">
						<%@ include file="filter_display.jsp"%>
						 <div style="width:${leftFilterColumnWidth}px;">
							<c:set var="type" value="ymd" />
							<b>
								<c:choose>
									<c:when test="${not empty filter.title}">${filter.title}</c:when>
									<c:otherwise>${filter.parameter.title}:</c:otherwise>
								</c:choose>
							</b><br/>
							с <input type="text" name="dateTimeParam${filter.parameter.id}From" />
							<c:set var="selector">${selectorForm} input[name='dateTimeParam${filter.parameter.id}From']</c:set>
							<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>

							по <input type="text" name="dateTimeParam${filter.parameter.id}To" />
							<c:set var="selector">${selectorForm} input[name='dateTimeParam${filter.parameter.id}To']</c:set>
							<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
						</div>
					</c:if>

					<c:if test="${filterFromList.getClass().getName() eq 'ru.bgcrm.model.process.queue.FilterParam'}">
						<c:choose>
							<c:when test="${filterFromList.parameter.type.startsWith( 'date' ) }">
								<c:set var="filter" value="${filterFromList}" />
								<c:set var="title" value="${not empty filter.title ? filter.title : filter.parameter.title}"/>

								<c:set var="code">
									${title}
									<c:choose>
										<c:when test="${not empty savedParamsFilters.get( 'dateTimeParam'.concat(filter.parameter.id).concat('From') ) }">
											с <input type="text" value="${savedParamsFilters.get('dateTimeParam'.concat(filter.parameter.id).concat('From')) }" name="dateTimeParam${filter.parameter.id}From" />
										</c:when>
										<c:otherwise>
											с <input type="text" name="dateTimeParam${filter.parameter.id}From" />
										</c:otherwise>
									</c:choose>
									<c:choose>
										<c:when test="${not empty savedParamsFilters.get( 'dateTimeParam'.concat(filter.parameter.id).concat('To') ) }">
											по <input type="text" value="${savedParamsFilters.get('dateTimeParam'.concat(filter.parameter.id).concat('To')) }" name="dateTimeParam${filter.parameter.id}To" />
										</c:when>
										<c:otherwise>
											по <input type="text" name="dateTimeParam${filter.parameter.id}To" />
										</c:otherwise>
									</c:choose>
								</c:set>

								<u:sc>
									<c:set var="type" value="ymd" />
									<c:set var="selector">${selectorForm} input[name='dateTimeParam${filter.parameter.id}From']</c:set>
									<c:set var="editable" value="1"/>
									<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
									<c:set var="selector">${selectorForm} input[name='dateTimeParam${filter.parameter.id}To']</c:set>
									<c:set var="editable" value="1"/>
									<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
								</u:sc>

								<%@ include file="filter_item.jsp"%>
							</c:when>

							<c:when test="${filterFromList.parameter.type == 'list' or filterFromList.parameter.type == 'listcount'}">
								<c:set var="filter" value="${filterFromList}"/>
									<c:set var="title" value="${not empty filter.title ? filter.title : filter.parameter.title}"/>

									<c:set var="code">
										<u:sc>
											<c:set var="id" value="${u:uiid()}"/>
											<c:set var="paramName" value="param${filter.parameter.id}value"/>
											<c:set var="list" value="${filter.parameter.listParamValues}"/>
											<c:set var="values" value="${filter.defaultValues}"/>

											<c:if test="${not empty savedParamsFilters.getSelectedValues( paramName ) }">
												<c:set var="values" value="${savedParamsFilters.getSelectedValues( paramName ) }"/>
											</c:if>

											<c:set var="available" value="${filter.availableValues}"/>
											<c:set var="showFilter" value="1"/>
											<c:set var="prefixText" value="${title}:"/>
											<c:set var="widthTextValue" value="150px"/>
											<%@ include file="/WEB-INF/jspf/combo_check.jsp"%>
										</u:sc>
									</c:set>

									<%@ include file="filter_item.jsp"%>
							</c:when>

							<c:when test="${filterFromList.parameter.type == 'address'}">
								<c:set var="filter" value="${filterFromList}"/>
								<c:set var="title" value="${not empty filter.title ? filter.title : filter.parameter.title}"/>
								<c:set var="cityIds" value="${filter.configMap['cityIds']}"/>
								<c:set var="fields" value="${filter.configMap['fields']}"/>
								<c:set var="paramName" value="param${filter.parameter.id}value"/>
								<c:set var="code">
									<c:set var="uiid" value="${u:uiid()}"/>
									<c:set var="buttonId" value="${u:uiid()}"/>
									<input type="button" id="${buttonId}" class="btn-white" onclick="$('#${uiid}').toggle();" value="${title}"/>

									<div id="${uiid}" style="display:none;position:absolute;background-color:#ffffff;border: 1px solid #aaaaaa;border-radius:5px;padding:5;">
										<input type="hidden" name="param${filter.parameter.id}valueCityId" value="${savedParamsFilters.get('param'.concat(filter.parameter.id).concat('valueCityId') )}">
										<input type="hidden" name="param${filter.parameter.id}valueStreetId" value="${savedParamsFilters.get('param'.concat(filter.parameter.id).concat('valueStreetId') )}">
										<input type="hidden" name="param${filter.parameter.id}valueHouseId" value="${savedParamsFilters.get('param'.concat(filter.parameter.id).concat('valueHouseId') )}">
										<input type="hidden" name="param${filter.parameter.id}valueQuarterId" value="${savedParamsFilters.get('param'.concat(filter.parameter.id).concat('valueQuarterId') )}">

										<c:set var="cityFilterId" value="${u:uiid()}"/>
										<c:set var="quarterFilterId" value="${u:uiid()}"/>
										<c:set var="streetFilterId" value="${u:uiid()}"/>
										<c:set var="houseFilterId" value="${u:uiid()}"/>
										<c:set var="flatFilterId" value="${u:uiid()}"/>

										<script>
											addCustomCitySearch( '#${cityFilterId}', '#${uiid} > input[name=param${filter.parameter.id}valueCityId]' );
											addCustomQuarterSearch( '#${quarterFilterId}', '#${uiid} > input[name=param${filter.parameter.id}valueQuarterId]', '${cityIds}' );
											addCustomStreetSearch( '#${streetFilterId}', '#${uiid} > input[name=param${filter.parameter.id}valueStreetId]' );
											addCustomHouseSearch( '#${houseFilterId}', '#${uiid} > input[name=param${filter.parameter.id}valueStreetId]', '#${uiid} > input[name=param${filter.parameter.id}valueHouseId]' );
										</script>

										<table>
											<c:if test="${empty fields}">
												<c:set var="fields" value="city;street;house"/>
											</c:if>

											<%-- город, улица и дом - обязательные поля --%>
											<c:if test="${!fn:contains(fields, 'house')}">
												<c:set var="fields" value="house;${fields}"/>
											</c:if>
											<c:if test="${!fn:contains(fields, 'street')}">
												<c:set var="fields" value="street;${fields}"/>
											</c:if>
											<c:if test="${!fn:contains(fields, 'city')}">
												<c:set var="fields" value="city;${fields}"/>
											</c:if>

											<c:forTokens var="show" delims=";" items="${fields}">
												<c:if test="${fn:contains(show, 'city')}">
													<tr><td>Город:</td><td><input id="${cityFilterId}" value="${savedParamsFilters.get('param'.concat(filter.parameter.id).concat('valueCity') )}" name="param${filter.parameter.id}valueCity" type="text" onkeyup="$('#${uiid} > input[name=param${filter.parameter.id}valueCityId]').val('')"/></td></tr>
												</c:if>
												<c:if test="${fn:contains(show, 'quarter')}">
													<tr><td>Квартал:</td><td><input id="${quarterFilterId}" value="${savedParamsFilters.get('param'.concat(filter.parameter.id).concat('valueQuarter') )}" name="param${filter.parameter.id}valueQuarter" type="text" onkeyup="$('#${uiid} > input[name=param${filter.parameter.id}valueQuarterId]').val('')"/></td></tr>
												</c:if>
												<c:if test="${fn:contains(show, 'street')}">
													<tr><td>${l.l('Улица')}:</td><td><input id="${streetFilterId}" value="${savedParamsFilters.get('param'.concat(filter.parameter.id).concat('valueStreet') )}" name="param${filter.parameter.id}valueStreet" type="text" onkeyup="$('#${uiid} > input[name=param${filter.parameter.id}valueStreetId]').val('')"/></td></tr>
												</c:if>
												<c:if test="${fn:contains(show, 'house')}">
													<tr><td>Дом:</td><td><input id="${houseFilterId}" value="${savedParamsFilters.get('param'.concat(filter.parameter.id).concat('valueHouse') )}" name="param${filter.parameter.id}valueHouse" type="text" onkeyup="$('#${uiid} > input[name=param${filter.parameter.id}valueHouseId]').val('')" /></td></tr>
												</c:if>
												<c:if test="${fn:contains(show, 'flat')}">
													<tr><td>${l.l('Квартира')}:</td><td><input id="${flatFilterId}" value="${savedParamsFilters.get('param'.concat(filter.parameter.id).concat('valueFlat') )}" name="param${filter.parameter.id}valueFlat" type="text" onkeyup="$('#${uiid} > input[name=param${filter.parameter.id}valueFlat]').val('')" /></td></tr>
												</c:if>
											</c:forTokens>

											<tr><td colspan="2" align="center" class="in-table-cell pt1">
												 <div>
													 <input type="button" class="btn-white" value="${l.l('Очистить')}"
															 onclick="
																 $('#${cityFilterId}').val('').keyup();
																 $('#${streetFilterId}').val('').keyup();
																 $('#${quarterFilterId}').val('').keyup();
																 $('#${houseFilterId}').val('').keyup();
																 $('#${flatFilterId}').val('').keyup();
																 $('#${uiid} #applyButton').click();
															 "/>
												 </div>
												 <div class="w100p pl1">
													 <input id="applyButton" type="button" class="btn-grey w100p" value="Применить"
															 onclick="$('#${uiid}').hide();
																	  var title = '${title}';
																	  [$('#${cityFilterId}').val(), $('#${streetFilterId}').val(), $('#${quarterFilterId}').val(),
																	   $('#${houseFilterId}').val(), $('#${flatFilterId}').val()].forEach(function (token) {
																		 if (token) {
																			 title += ', ' + token;
																		 }
																	  })
	 																  $('#${buttonId}').val(title);"/>
 												 </div>
											 </td></tr>
										</table>
									</div>
								</c:set>
								<%@ include file="filter_item.jsp"%>
							</c:when>

							<c:when test="${filterFromList.parameter.type == 'text' || filterFromList.parameter.type == 'blob'}">
								<c:set var="filter" value="${filterFromList}" />
								<c:set var="title" value="${not empty filter.title ? filter.title : filter.parameter.title}"/>

								<c:set var="code">
									<input type="text" name="param${filter.parameter.id}value" placeholder="${title}" size="20" onkeypress="if( enterPressed( event ) ){ ${sendCommand} }"/>
								</c:set>

								<%@ include file="filter_item.jsp"%>
							</c:when>
						</c:choose>
					</c:if>

					<c:if test="${filterFromList.type == 'grex'}">
						<c:set var="filterGrEx" value="${filterFromList}"/>
						<c:set var="roleId" value="${filterGrEx.roleId}"/>

						<c:set var="groupListId" value="${u:uiid()}"/>
						<c:set var="executorListId" value="${u:uiid()}"/>
						<c:set var="groupParamName" value="group${roleId}"/>
						<c:set var="executorParamName" value="executor${roleId}"/>

						<c:set var="filter" value="${filterGrEx.groupsFilter}"/>
						<%@ include file="filter_groups.jsp"%>

						<c:set var="filter" value="${filterGrEx.executorsFilter}"/>
						<%@ include file="filter_executors.jsp"%>
					</c:if>

					<c:if test="${filterFromList.getClass().getName() eq 'ru.bgcrm.model.process.queue.FilterLinkObject'}">
						<c:set var="filter" value="${filterFromList}"/>
						<c:set var="title" value="${not empty filterFromList.title ? filterFromList.title : 'Код привязки'}"/>

						<c:set var="code">
							<input type="text" name="${filterFromList.paramName}" placeholder="${title}" size="6" style="text-align: center;" onkeypress="if( enterPressed( event ) ){ ${sendCommand}; return false; }"/>
						</c:set>
						<%@ include file="filter_item.jsp"%>
					</c:if>

				</c:forEach>
			</c:set>

			<c:set var="id" value="${u:uiid()}"/>
			<c:set var="hiddenName" value="param"/>
			<c:set var="prefixText" value="${l.l('Фильтры')}:"/>
			<c:set var="styleClass" value="mr1 filtersSelect"/>
			<c:set var="widthTextValue" value="50px"/>
			<c:set var="onChange">
				var selectedFilters = {};
				var selectedFilterIds = "";

				$$.ui.comboInputs($("#${id}")).each( function() {
					if( this.checked )
					{
						selectedFilters[$(this).attr('id')] = 1;
						if( selectedFilterIds.length > 0 )
						{
							selectedFilterIds += ",";
						}
						selectedFilterIds += $(this).attr('value');
					}
				});

				$('${selectorForm}')[0].selectedFilters.value = selectedFilterIds;

				// отображение выбранных фильтров
				$('${selectorForm}').find('.filter-item').each( function()
				{
					$(this).toggle(selectedFilters[$(this).attr( 'id' )] !== undefined);
				});

				processQueueClearHiddenFilters($('${selectorForm}'));
				processQueueMarkFilledFilters($('${selectorForm}'));
			</c:set>
			<%@ include file="/WEB-INF/jspf/combo_check.jsp"%>
		</u:sc>

		${filters}

		<script style="display: none;">
			$(function()
			{
				processQueueMarkFilledFilters($('${selectorForm}'));
			})
		</script>

		<c:if test="${queue.sortSet.comboCount gt 0}">
			<div>
				${l.l('Сорт.')}:
				<c:forEach begin="1" end="${queue.sortSet.comboCount}" step="1" varStatus="status">
					<c:set var="value" value="0"/>
					<c:forEach var="mode" items="${queue.sortSet.modeList}" varStatus="statusItem">
						<c:if test="${queue.sortSet.defaultSortValues[status.count] eq statusItem.count}">
							<c:set var="value" value="${mode.orderExpression}"/>
						</c:if>
					</c:forEach>

					<ui:combo-single value="${value}" hiddenName="sort" widthTextValue="50px">
						<jsp:attribute name="valuesHtml">
							<li value="0">- нет -</li>
							<c:forEach var="mode" items="${queue.sortSet.modeList}" varStatus="statusItem">
								<li value="${mode.orderExpression}">${mode.title}</li>
							</c:forEach>
						</jsp:attribute>
					</ui:combo-single>
				</c:forEach>
			</div>
		</c:if>

		<button type="button" title="Применить" class="btn-grey" onclick="var $form = $('${selectorForm}'); processQueueMarkFilledFilters($form); openUrlTo( formUrl( $form ), $('#processQueueData') );">=&gt;</button>
	</form>
</div>
