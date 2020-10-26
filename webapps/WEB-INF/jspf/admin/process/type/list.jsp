<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="urlList" value="/admin/process.do">
	<c:param name="action" value="typeList"/>
	<c:param name="parentTypeId" value="${form.param.parentTypeId}"/>
	<c:param name="filter" value="${form.param.filter}"/>
</c:url>

<html:form  action="admin/process" styleClass="in-mr1">
	<input type="hidden" name="action" value="typeList"/>
	<input type="hidden" name="parentTypeId" value="${form.param.parentTypeId}"/>

	<c:url var="url" value="/admin/process.do">
		<c:param name="action" value="typeGet"/>
		<c:param name="id" value="-1"/>
		<c:param name="returnUrl" value="${form.requestUrl}"/>
		<c:param name="parentTypeId" value="${form.param.parentTypeId}"/>
	</c:url>
	<button type="button" class="btn-green" onclick="openUrlContent( '${url}' )">+</button>

	<c:url var="url" value="/admin/process.do">
		<c:param name="action" value="typeInsertMark"/>
		<c:param name="markType" value="${form.param.markType}"/>
		<c:param name="parentTypeId" value="${form.param.parentTypeId}"/>
	</c:url>
	<button type="button" id="markTypeButton" class="btn-grey ml1" onclick="if( sendAJAXCommand( '${url}' ) ){ openUrlContent( '${urlList}' ); }">Вставить [${markTypeString}]</button>

	<button type="button" class="btn-grey ml1" onclick="openUrlContent( '${urlList}' )">Сбросить выделение</button>

	<c:set var="doFilterCommand">this.form.parentTypeId.value = this.form.filter.value ? -1 : 0; openUrlContent( formUrl( this.form ) )</c:set>

	<ui:input-text name="filter" onSelect="${doFilterCommand}; return false;"
			placeholder="Фильтр" size="40" value="${form.param['filter']}" title="Фильтр по наименованию, конфигурации"/>

	<button class="btn-grey" type="button" onclick="${doFilterCommand}" title="${l.l('Вывести')}">=&gt;</button>

	<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
</html:form>

<div class="mt1">
	<c:url var="url" value="/admin/process.do">
		<c:param name="action" value="typeList"/>
		<c:param name="parentTypeId" value="0"/>
		<c:param name="markType" value="${form.param.markType}"/>
	</c:url>

	&#160;
	<a href="#UNDEF" onClick="openUrlContent('${url}'); return false;">Типы процессов</a>

	<c:forEach var="item" items="${typePath}" varStatus="status">
		<c:url var="url" value="/admin/process.do">
			<c:param name="action" value="typeList"/>
			<c:param name="parentTypeId" value="${item.id}"/>
			<c:param name="markType" value="${form.param.markType}"/>
		</c:url>
		-> <a href="#UNDEF" onClick="openUrlContent('${url}'); return false;">${item.title}</a>
	</c:forEach>
</div>

<table style="width: 100%;" class="data mt1">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>
		<td width="100%">${l.l('Наименование')}</td>
		<td>Подтипов</td>
		<td>Свойства</td>
		<td width="20%">&#160;</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
			<%-- подгрузка типа из кэша позволяет получить число подтипов --%>
			<c:set var="item" value="${ctxProcessTypeMap[item.id]}"/>

			<c:url var="editUrl" value="/admin/process.do">
				<c:param name="action" value="typeGet"/>
				<c:param name="id" value="${item.id}"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
				<c:param name="parentTypeId" value="${form.param.parentTypeId}"/>
			</c:url>
			<c:url var="deleteAjaxUrl" value="/admin/process.do">
				<c:param name="action" value="typeDelete"/>
				<c:param name="id" value="${item.id}"/>
				<c:param name="parentTypeId" value="${form.param.parentTypeId}"/>
			</c:url>
			<c:url var="deleteAjaxCommandAfter" value="openUrlContent( '${form.requestUrl}' )"/>

			<td nowrap="nowrap">
				<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
				<%-- TODO: некорректно работает, когда выбрана не первая страница, решение см. редактор групп --%>
				<c:url var="url" value="${urlList}">
					<c:param name="markType" value="${item.id}"/>
				</c:url>
				<button type="button" class="btn-white btn-small" onclick="openUrlContent( '${url}' )" title="Вырезать">C</button>
			</td>

			<td>${item.id}</td>

			<td>
				<c:forEach var="itemPath" items="${item.path}" varStatus="status">
					<c:url var="url" value="/admin/process.do">
						<c:param name="action" value="typeList"/>
						<c:param name="markType" value="${form.param.markType}"/>
						<c:param name="parentTypeId" value="${item.id}"/>
					</c:url>
					<c:if test="${status.last}">
						<a href="#UNDEF" onclick="openUrlContent('${url}'); return false;">${itemPath.title}</a>
					</c:if>
					<c:if test="${not empty form.param.filter && not status.last}">
						<a href="#UNDEF" onclick="openUrlContent('${url}'); return false;">${itemPath.title}</a> ->
					</c:if>
				</c:forEach>
			</td>

			<td>${item.childCount}</td>

			<c:url var="url" value="/admin/process.do">
				<c:param name="action" value="properties"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>
			<td nowrap="nowrap">
				<c:choose>
					<c:when test="${item.useParentProperties}">
						[унаследованы]
					</c:when>
					<c:otherwise><a href="#UNDEF" onclick="openUrlContent('${url}'); return false;">[свойства]</a></c:otherwise>
				</c:choose>
			</td>

			<td nowrap="nowrap">
				<c:set var="showId" value="${u:uiid()}"/>
				<div class="buttons">
					<c:set var="hideButtonsScript">$(this).closest('.buttons').hide();</c:set>
					<p:check action="ru.bgcrm.struts.action.admin.ProcessAction:typeUsed">
						<c:url var="showUrl" value="/admin/process.do">
							<c:param name="action" value="typeUsed" />
							<c:param name="typeId" value="${item.id}" />
						</c:url>
	
						<button type="button" class="btn-white btn-small" title="${l.l('Использующие очереди')}"
							onclick="${hideButtonsScript} $$.ajax.load('${showUrl}', $('#${showId}'));">Q?</button>
					</p:check>
					<p:check action="ru.bgcrm.struts.action.admin.ProcessAction:typeCopy">
						<c:url var="showUrl" value="/admin/process.do">
							<c:param name="action" value="typeCopy" />
							<c:param name="id" value="${item.id}"/>
							<c:param name="parentId" value="${form.param.parentTypeId}"/>
						</c:url>
	
						<button type="button" class="btn-white btn-small" title="${l.l('Копировать свойства другого типа')}"
							onclick="${hideButtonsScript} $$.ajax.load('${showUrl}', $('#${showId}'));">PI</button>
					</p:check>
				</div>
				<div id="${showId}" class="editor"></div>
			</td>
		</tr>
	</c:forEach>
</table>

<shell:title ltext="Типы процессов"/>
<shell:state help="kernel/process/index.html#type"/>
