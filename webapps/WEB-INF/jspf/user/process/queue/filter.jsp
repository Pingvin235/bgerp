<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="queue" value="${frd.queue}"/>

<c:set var="formAction" value="/user/process.do"/>

<c:set var="key" value="queueCurrentSavedFilterSet.${queue.id}"/>
<c:set var="currentSavedFilterSetId" value="${u:int( ctxUser.personalizationMap[key] )}"/>

<c:set var="config" value="${ctxUser.personalizationMap.getConfig('ru.bgcrm.model.process.queue.config.SavedFiltersConfig')}"/>
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
	<c:set var="sendCommand">$('${selectorForm}').find('button.out').click();</c:set>

	<c:set var="uiidMoreButton" value="${u:uiid()}"/>
	<c:set var="uiidMoreMenu" value="${u:uiid()}"/>
	<c:set var="saveFilterFormUiid" value="${u:uiid()}"/>
	<c:set var="savedFiltersFormsDivUiid" value="${u:uiid()}"/>

	<c:set var="addActionFormUiid" value="${u:uiid()}"/>

	<jsp:useBean id="curdate" class="java.util.Date"/>

	<button class="btn-white combo mr1 mb1" id="${uiidMoreButton}">
		<div class="text-value">${l.l('Ещё')}</div>
		<div class="icon ti-angle-down"></div>
		<script>
			$(function () {
				$$.ui.menuInit($("#${uiidMoreButton}"), $("#${uiidMoreMenu}"), "left");
			})
		</script>
	</button>

	<ui:popup-menu id="${uiidMoreMenu}">
		<c:if test="${queue.configMap['allowCreateProcess'] ne 0}">
			<li draggable="true"><a onclick="$('#processQueueShow').hide(); $('#processQueueCreateProcess').show();">${l.l('Создать процесс')}</a></li>
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
		<c:set var="printConfig" value="${queue.configMap.getConfig('ru.bgcrm.model.process.queue.config.PrintQueueConfig')}"/>
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

		<li draggable="true" id="savedFilters" ${hideWhenFullFilter}><a onclick="$$.process.queue.changed(0);">${l.l('Фильтр - вернуться в полный')}</a></li>
		<c:set var="getSavedSetId">
			var savedSetId = $('#processQueueFilter > #${queue.id}').find( '#savedFilters div.btn-blue' ).attr( 'id' ) ;
			if( !savedSetId )
			{
				alert( '${l.l('Фильтр не выбран')}!' );return;
			}
		</c:set>
		<li draggable="true" id="savedFilters" ${hideWhenFullFilter}>
			<a onclick="if( !confirm( '${l.l('Удалить сохранённый фильтр')}?' ) ){ return; }
				${getSavedSetId}
				$$.ajax.post('/user/process/queue.do?method=queueSavedFilterSet&queueId=${queue.id}&id=' + savedSetId + '&command=delete').done(() => {
					processQueueFilterSetSelect(${queue.id})
				})">${l.l('Фильтр - удалить')}</a>
		</li>
		<li draggable="true" id="savedFilters" ${hideWhenFullFilter}>
			<a onclick="
				${getSavedSetId}
				$$.ajax.post('/user/process/queue.do?method=queueSavedFilterSet&queueId=${queue.id}&id=' + savedSetId + '&command=toFullFilter').done(() => {
					$('#processQueueFilter > div#${queue.id}').remove();
					$$.process.queue.changed(0);
				})">${l.l('Фильтр - извлечь в полный')}</a>
		</li>
		<li draggable="true" id="savedFilters" ${hideWhenFullFilter}><a onclick="addCounterToPanel();">${l.l('Фильтр - счетчик на панель')}</a></li>
		<li draggable="true" id="savedFilters" ${hideWhenFullFilter}><a onclick="delCounterFromPanel();">${l.l('Фильтр - счетчик убрать с панели')}</a></li>

		<li draggable="true" id="${queue.id}-0" ${hideWhenSavedFilter}><a onclick="$$.process.queue.changed(1);">${l.l('Фильтр - сохранённые')}</a></li>
		<li draggable="true" id="${queue.id}-0" ${hideWhenSavedFilter}><a onclick="$('#${saveFilterFormUiid}').css('display','');">${l.l('Фильтр - сохранить')}</a></li>
		<li draggable="true" id="${queue.id}-0" ${hideWhenSavedFilter}>
			<a onclick="if( !confirm( '${l.l('Reset the full filter?')}' ) ){ return; }
				$$.ajax.post('/user/process/queue.do?method=queueSavedFilterSet&queueId=${queue.id}&id=0&command=toFullFilter').done(() => {
					$('#processQueueFilter > div#${queue.id}').remove();
					$$.process.queue.changed(0);
				})">${l.l('Фильтр - сброс')}</a>
		</li>

		<c:forEach var="processor" items="${queue.getProcessors('user')}">
			<li>
				<c:set var="script">
					<c:choose>
						<c:when test="${not empty processor.jsp}">
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
			<li><a onclick="${xls}">${l.l('Выгрузка в Excel')}</a></li>
		</c:if>
	</ui:popup-menu>

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

	<%-- processors --%>
	<div style="display: inline-block;">
		<c:forEach var="processor" items="${queue.getProcessors('user')}">
			<form action="/user/process/queue.do" id="${addActionFormUiid}-${processor.id}" style="display: none;" class="in-mb1-all">
				<input type="hidden" name="method" value="processor"/>
				<input type="hidden" name="queueId" value="${queue.id}"/>
				<input type="hidden" name="processorId" value="${processor.id}"/>
				<input type="hidden" name="processIds"/>

				<c:if test="${not empty processor.jsp}">
					<c:set var="processor" value="${processor}" scope="request"/>
					<jsp:include page="${processor.jsp}"/>
				</c:if>

				<c:set var="doScript">
					const processIds = getCheckedProcessIds();
					if (!processIds) {
						alert('${l.l('Выберите процессы!')}');
						return;
					}

					this.form.processIds.value = processIds;

					const debug = $$.keys.altPressed() ? '&debug=true' : '';
					if (${processor.htmlReport}) {
						const w = window.open( formUrl(this.form) + '&responseType=stream' + debug, 'Print', 'menubar=1, scrollbars=1, height=800, width=800');
						<c:if test="${processor.configMap.openPrintDialog eq '1'}">
							w.addEventListener('load', () => {
								w.focus();
								w.print();
							}, false);
						</c:if>
					} else {
						$$.ajax.post($$.ajax.formUrl(this.form) + debug).done(() => { ${sendCommand} });
					}

					$(this.form).hide();
				</c:set>

				<button id="okButton" type="button" class="btn-grey" onclick="${doScript}">OK</button>
				<button type="button" class="btn-white ml05 mr1" onclick="$(this.form).hide();">${l.l('Cancel')}</button>
			</form>
		</c:forEach>
	</div>

	<%-- saving filter --%>
	<form action="/user/process/queue.do" style="display: none;" id="${saveFilterFormUiid}" class="mb1 mr1">
		${l.l('Название фильтра')}:
		<input type="hidden" name="method" value="queueSavedFilterSet"/>
		<input type="hidden" name="queueId" value="${queue.id}"/>
		<input type="hidden" name="command" value="add"/>
		<input type="hidden" name="url"/>
		<input type="text" name="title" size="20"/>

		<button type="button" class="btn-grey"
			onclick="if( this.form.title.value == '' ){ alert('${l.l('Введите название!')}'); return; }
					this.form.url.value = $$.ajax.formUrl($('#processQueueFilter').find('form#${queue.id}-0'), ['page.pageIndex', 'savedFilterSetId']);
					$$.ajax.post(this).done(() => { processQueueFilterSetSelect(${queue.id}) })">OK</button>
		<button type="button" class="btn-grey" onclick="$(this.form).hide()">${l.l('Cancel')}</button>
	</form>

	<%-- saved filters --%>
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

		<ui:combo-single prefixText="${l.l('Filter')}:" widthTextValue="4em" styleClass="dropFilterArea"/>

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

		<button class="btn-white btn-icon" onclick="showCommonFiltersMenu()" title="${l.l('Общие фильтры')}"><i class="ti-filter"></i></button>

		<div id="commonFiltersPanel" class="box" style="display:none;">
			<button onclick="importFilterFromCommons()" title="${l.l('Импорт себе общего фильтра')}" class="btn-white btn-icon"><i class="ti-arrow-left"></i></button>
			<button onclick="exportFilterToCommons()" title="${l.l('Экспорт своего фильтра в общие')}" class="btn-white btn-icon"><i class="ti-arrow-right"></i></button>
			<button onclick="deleteFilterFromCommons()" title="${l.l('Удалить общий фильтр')}" class="btn-white btn-icon"><i class="ti-trash"></i></button>
			<ui:combo-single hiddenName="currentCommonFilter" prefixText="${l.l('Общий фильтр')}:" list="${commonConfig.queueSavedCommonFilterSetsMap[queue.id]}"
				style="display: inline-block;" widthTextValue="100px"/>
		</div>
	</div>

	<%-- Set используется для установки статусов отображаемым фильтрам --%>
	<c:set var="selectedFiltersStr" value="${ctxUser.personalizationMap['queueSelectedFilters'.concat(queue.id)]}"/>
	<c:set var="selectedFilters" value="${u.toIntegerSet(selectedFiltersStr)}"/>

	<%-- полный фильтр --%>
	<form action="/user/process/queue.do" id="${queue.id}-0" ${currentSavedFilterSetId le 0 ? "active='1'" : "style='display: none;'"} class="in-inline-block in-mr05 in-mb1-all">
		<input type="hidden" name="savedFilterSetId" value="0"/>
		<input type="hidden" name="selectedFilters" value="${selectedFiltersStr}"/>
		<input type="hidden" name="method" value="queueShow"/>
		<input type="hidden" name="pageableId" value="queue"/>

		<input type="hidden" name="id" value="${queue.id}"/>

		<u:sc export="filters">
			<c:set var="valuesHtml">
				<c:forEach var="filterFromList" items="${queue.filterList.filterList}">
					<c:if test="${filterFromList.type == 'code'}">
						<c:set var="filter" value="${filterFromList}"/>
						<c:set var="title" value="${l.l('Process ID')}"/>
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
						<%@ include file="filter/item.jsp"%>
					</c:if>

					<c:if test="${filterFromList.type == 'type'}">
						<c:set var="filter" value="${filterFromList}"/>

						<c:set var="title" value="${l.l('Type')}"/>
						<c:set var="code">
							<u:sc>
								<c:set var="paramName" value="type"/>

								<c:set var="values" value="${filter.defaultValues}"/>
								<c:if test="${not empty savedParamsFilters.getParamValues(paramName)}">
									<c:set var="values" value="${savedParamsFilters.getParamValues(paramName)}"/>
								</c:if>

								<ui:combo-check paramName="${paramName}" values="${values}"
									list="${frd.typeList}" map="${ctxProcessTypeMap}" available="${filter.availableValues}"
									prefixText="${l.l('Type')}:" showFilter="1" widthTextValue="10em"/>
							</u:sc>
					 	</c:set>
						<%@ include file="filter/item.jsp"%>
					</c:if>

					<c:set var="statusFilterId" value="${u:uiid()}"/>

					<c:if test="${filterFromList.type == 'openClose'}">
						<c:set var="filter" value="${filterFromList}"/>
						<c:set var="title" value="${l.l('Open')}"/>
						<c:set var="code">
							<u:sc>
								<span class="dontResetOnHideFilter"></span>

								<c:set var="hiddenName" value="openClose"/>

								<c:set var="value" value="${filter.defaultValue}"/>
								<c:if test="${not empty savedParamsFilters.get( hiddenName ) }">
									<c:set var="value" value="${savedParamsFilters.get( hiddenName ) }"/>
								</c:if>

								<ui:combo-single value="${value}" hiddenName="${hiddenName}" prefixText="${l.l('Closed')}:" widthTextValue="40px" onSelect="${sendCommand}">
									<jsp:attribute name="valuesHtml">
										<li value="none">${l.l('All')}</li>
										<li value="open">${l.l('No')}</li>
										<li value="close">${l.l('Yes')}</li>
									</jsp:attribute>
								</ui:combo-single>
							</u:sc>
						</c:set>

						<c:set var="id" value="${statusFilterId}"/>
						<%@ include file="filter/item.jsp"%>
						<c:remove var="id"/>
					</c:if>

					<c:if test="${filterFromList.type == 'status'}">
						<c:set var="filter" value="${filterFromList}"/>
						<c:set var="title" value="${l.l('Статусы')}"/>
						<c:set var="code">
							<u:sc>
								<c:set var="paramName" value="status"/>
								<c:set var="values" value="${savedParamsFilters.getParamValues(paramName)}"/>
								<ui:combo-check paramName="${paramName}"
									list="${ctxProcessStatusList}" map="${ctxProcessStatusMap}" available="${filter.availableValues}"
									values="${empty values ? filter.defaultValues : values}"
									prefixText="${l.l('Status')}:" widthTextValue="8em"/>
							</u:sc>
						</c:set>

						<c:set var="id" value="${statusFilterId}"/>
						<%@ include file="filter/item.jsp"%>
						<c:remove var="id"/>
					</c:if>

					<c:remove var="statusFilterId"/>

					<c:if test="${filterFromList.type == 'description'}">
						<c:set var="filter" value="${filterFromList}"/>
						<c:set var="title" value="${l.l('Description')}"/>
						<c:set var="code">
							<c:choose>
								<c:when test="${not empty savedParamsFilters.get( 'description' ) }">
									<input type="text" value="${savedParamsFilters.get( 'description' )}" name="description" placeholder="${l.l('Description')}" size="20" style="text-align: center;" onkeypress="if( enterPressed( event ) ){ ${sendCommand}; return false; }"/>
								</c:when>
								<c:otherwise>
									<input type="text" name="description" placeholder="${l.l('Description')}" size="20" style="text-align: center;" onkeypress="if( enterPressed( event ) ){ ${sendCommand}; return false; }"/>
								</c:otherwise>
							</c:choose>
						</c:set>
						<%@ include file="filter/item.jsp"%>
					</c:if>

					<c:if test="${filterFromList.type == 'linkedCustomer:title'}">
						<c:set var="filter" value="${filterFromList}"/>

						<c:set var="title" value="${l.l('Customer')}"/>
						<c:set var="code">
							<input type="text" value="${ savedParamsFilters.get( 'linkedCustomer:title' ) }" name="linkedCustomer:title" placeholder="${l.l('Customer')}" size="20" onkeypress="if( enterPressed( event ) ){ ${sendCommand} }"/>
						</c:set>
						<%@ include file="filter/item.jsp"%>
					</c:if>

					<c:if test="${filterFromList.type == 'message:systemId'}">
						<c:set var="filter" value="${filterFromList}"/>

						<c:set var="title" value="${filter.title}"/>
						<c:set var="code">
							<input type="text" value="${savedParamsFilters.get('message:systemId')}" name="message:systemId" placeholder="${filter.title}" size="6" style="text-align: center;" onkeypress="if( enterPressed( event ) ){ ${sendCommand}; return false; }"/>
						</c:set>
						<%@ include file="filter/item.jsp"%>
					</c:if>

					<c:if test="${filterFromList.type.startsWith('linkedCustomer:param:')}">
						<c:set var="filter" value="${filterFromList}"/>
						<c:set var="title" value="${not empty filter.title ? filter.title : filter.parameter.title}"/>
						<c:set var="code">
							<u:sc>
								<c:set var="paramName" value="param${filter.parameter.id}value"/>
								<c:set var="values" value="${savedParamsFilters.getParamValues(paramName)}"/>

								<ui:combo-check paramName="${paramName}"
									list="${filter.parameter.listParamValues}" available="${filter.availableValues}"
									values="${empty values ? filter.defaultValues : values}"
									showFilter="1" prefixText="${title}:" widthTextValue="12em"/>
							</u:sc>
						</c:set>
						<%@ include file="filter/item.jsp"%>
					</c:if>

					<c:if test="${filterFromList.type == 'create_date'}">
						<c:set var="filter" value="${filterFromList}"/>

						<c:set var="dateFrom" value="${filter.configMap['defaultValueFrom']}"/>
						<c:if test="${ dateFrom eq 'currentDate' }">
							<c:set var="dateFrom" value="${tu.format(curdate, 'dd.MM.yyyy')}"/>
						</c:if>
						<c:if test="${not empty savedParamsFilters.get('dateCreateFrom')}">
							<c:set var="dateFrom" value="${savedParamsFilters.get('dateCreateFrom')}"/>
						</c:if>

						<c:set var="dateTo" value="${filter.configMap['defaultValueTo']}"/>
						<c:if test="${ dateTo eq 'currentDate' }">
							<c:set var="dateTo" value="${tu.format(curdate, 'dd.MM.yyyy')}"/>
						</c:if>
						<c:if test="${not empty savedParamsFilters.get('dateCreateTo')}">
							<c:set var="dateTo" value="${savedParamsFilters.get('dateCreateTo')}"/>
						</c:if>

						<c:set var="title" value="${l.l('Дата создания')}"/>
						<c:set var="code">
							${l.l('Создан с')}&nbsp;<ui:date-time paramName="dateCreateFrom" value="${dateFrom}"/>
							${l.l('по')}&nbsp;<ui:date-time paramName="dateCreateTo" value="${dateTo}"/>
						</c:set>

						<%@ include file="filter/item.jsp"%>
					</c:if>

					<c:if test="${filterFromList.type == 'close_date' }">
						<c:set var="filter" value="${filterFromList}"/>

						<c:set var="dateFrom" value="${filter.configMap['defaultValueFrom']}"/>
						<c:if test="${ dateFrom eq 'currentDate' }">
							<c:set var="dateFrom" value="${tu.format(curdate, 'dd.MM.yyyy')}"/>
						</c:if>

						<c:if test="${not empty savedParamsFilters.get('dateCloseFrom')}">
							<c:set var="dateFrom" value="${savedParamsFilters.get('dateCloseFrom')}"/>
						</c:if>

						<c:set var="dateTo" value="${filter.configMap['defaultValueTo']}"/>
						<c:if test="${ dateTo eq 'currentDate' }">
							<c:set var="dateTo" value="${tu.format(curdate, 'dd.MM.yyyy')}"/>
						</c:if>

						<c:if test="${not empty savedParamsFilters.get('dateCloseTo')}">
							<c:set var="dateTo" value="${savedParamsFilters.get('dateCloseTo')}"/>
						</c:if>

						<c:set var="title" value="${l.l('Дата закрытия')}"/>
						<c:set var="code">
							${l.l('Закрыт с')}&nbsp;<ui:date-time paramName="dateCloseFrom" value="${dateFrom}"/>
							${l.l('по')}&nbsp;<ui:date-time paramName="dateCloseTo" value="${dateTo}"/>
						</c:set>

						<%@ include file="filter/item.jsp"%>
					</c:if>

					<c:if test="${filterFromList.type == 'status_date' }">
						<c:set var="filter" value="${filterFromList}"/>

						<c:set var="title" value="${l.l('Status date')}"/>
						<c:set var="code">
							<u:sc>
								<c:set var="hiddenName" value="dateStatusStatus"/>

								<ui:combo-single hiddenName="${hiddenName}" value="${savedParamsFilters.get(hiddenName)}"
									list="${ctxProcessStatusList}" map="${ctxProcessStatusMap}" available="${filter.availableValues}"
									prefixText="${l.l('Status')}:" widthTextValue="8em"/>

								<c:set var="paramName" value="dateStatusFrom"/>
								&nbsp;${l.l('с')}&nbsp;<ui:date-time paramName="${paramName}" value="${savedParamsFilters.get(paramName)}"/>

								<c:set var="paramName" value="dateStatusTo"/>
								${l.l('по')}&nbsp;<ui:date-time paramName="${paramName}" value="${savedParamsFilters.get(paramName)}"/>
							</u:sc>
						</c:set>

						<%@ include file="filter/item.jsp"%>
					</c:if>

					<c:if test="${filterFromList.type == 'groups'}">
						<c:set var="groupListId" value="${u:uiid()}"/>
						<c:set var="executorListId" value="${u:uiid()}"/>
						<c:set var="groupParamName" value="group"/>
						<c:set var="executorParamName" value="executor"/>

						<c:set var="filter" value="${filterFromList}"/>
						<%@ include file="filter/group.jsp"%>
					</c:if>

					<c:if test="${filterFromList.type == 'executors'}">
						<c:set var="filter" value="${filterFromList}"/>

						<%@ include file="filter/executor.jsp"%>
					</c:if>

					<c:if test="${filterFromList.type == 'create_user' or filterFromList.type == 'close_user'}">
						<c:set var="filter" value="${filterFromList}"/>
						<c:set var="title">
							<c:if test="${filterFromList.type == 'create_user'}">
								${l.l('Создал')}
							</c:if>
							<c:if test="${filterFromList.type == 'close_user'}">
								${l.l('Закрыл')}
							</c:if>
						</c:set>
						<c:set var="code">
							<ui:combo-check list="${ctxUser.getUserListWithSameGroups()}" paramName="${filter.type}"
								prefixText="${title}:" widthTextValue="5em"/>
						</c:set>
						<%@ include file="filter/item.jsp"%>
					</c:if>

					<c:if test="${filterFromList.getClass().simpleName eq 'FilterParam'}">
						<%@ include file="filter/type/param.jsp"%>
					</c:if>

					<c:if test="${filterFromList.type == 'grex'}">
						<c:set var="filterGrEx" value="${filterFromList}"/>
						<c:set var="roleId" value="${filterGrEx.roleId}"/>

						<c:set var="groupListId" value="${u:uiid()}"/>
						<c:set var="executorListId" value="${u:uiid()}"/>
						<c:set var="groupParamName" value="group${roleId}"/>
						<c:set var="executorParamName" value="executor${roleId}"/>

						<c:set var="filter" value="${filterGrEx.groupsFilter}"/>
						<%@ include file="filter/group.jsp"%>

						<c:set var="filter" value="${filterGrEx.executorsFilter}"/>
						<%@ include file="filter/executor.jsp"%>
					</c:if>

					<c:if test="${filterFromList.getClass().simpleName eq 'FilterLinkObject'}">
						<c:set var="filter" value="${filterFromList}"/>
						<c:set var="title" value="${not empty filterFromList.title ? filterFromList.title : 'Код привязки'}"/>

						<c:set var="code">
							<input type="text" name="${filterFromList.paramName}" placeholder="${title}" size="6" style="text-align: center;" onkeypress="if( enterPressed( event ) ){ ${sendCommand}; return false; }"/>
						</c:set>
						<%@ include file="filter/item.jsp"%>
					</c:if>

				</c:forEach>
			</c:set>

			<c:set var="id" value="${u:uiid()}"/>
			<c:set var="onChange" value="$$.process.queue.filter.showSelected('${id}', '${selectorForm}')"/>

			<ui:combo-check id="${id}" valuesHtml="${valuesHtml}" onChange="${onChange}" prefixText="${l.l('Фильтры')}:" styleClass="mr1 filtersSelect" widthTextValue="5em"/>

			<script style="display: none;">
				$(function () {
					${onChange}
				})
			</script>
		</u:sc>

		<%-- the variable is concatenated in filter/item.jsp --%>
		${filters}

		<script style="display: none;">
			$(function () {
				processQueueMarkFilledFilters($('${selectorForm}'));
			})
		</script>

		<c:if test="${queue.sortSet.comboCount gt 0}">
			<div>
				<c:forEach begin="1" end="${queue.sortSet.comboCount}" step="1" varStatus="status">
					<c:set var="value" value="0"/>
					<c:forEach var="mode" items="${queue.sortSet.modeList}" varStatus="statusItem">
						<c:if test="${queue.sortSet.defaultSortValues[status.count] eq statusItem.count}">
							<c:set var="value" value="${mode.orderExpression}"/>
						</c:if>
					</c:forEach>

					<ui:combo-single value="${value}" hiddenName="sort" prefixText="${l.l('Сорт.')}:" widthTextValue="50px" >
						<jsp:attribute name="valuesHtml">
							<li value="0">- ${l.l('нет')} -</li>
							<c:forEach var="mode" items="${queue.sortSet.modeList}" varStatus="statusItem">
								<li value="${mode.orderExpression}">${mode.title}</li>
							</c:forEach>
						</jsp:attribute>
					</ui:combo-single>
				</c:forEach>
			</div>
		</c:if>

		<ui:button type="out" styleClass="out" onclick="const $form = $('${selectorForm}'); processQueueMarkFilledFilters($form); $$.ajax.load($form, $('#processQueueData'), {control: this});"/>
	</form>
</div>
