<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="urlList" value="/admin/process.do">
	<c:param name="action" value="typeList"/>
	<c:param name="parentTypeId" value="${form.param.parentTypeId}"/>
	<c:param name="filter" value="${form.param.filter}"/>
</c:url>

<html:form  action="admin/process" styleClass="in-mr1" styleId="${uiid}">
	<input type="hidden" name="action" value="typeList"/>
	<html:hidden property="parentTypeId"/>
	<html:hidden property="markType"/>

	<c:url var="url" value="/admin/process.do">
		<c:param name="action" value="typeGet"/>
		<c:param name="id" value="-1"/>
		<c:param name="returnUrl" value="${form.requestUrl}"/>
		<c:param name="parentTypeId" value="${form.param.parentTypeId}"/>
	</c:url>
	<ui:button type="add" onclick="$$.ajax.load('${url}', $$.shell.$content())"/>

	<c:url var="url" value="/admin/process.do">
		<c:param name="action" value="typeInsertMark"/>
		<c:param name="markType" value="${form.param.markType}"/>
		<c:param name="parentTypeId" value="${form.param.parentTypeId}"/>
	</c:url>
	<button type="button" class="btn-grey" 
		onclick="$$.ajax.post('${url}').done(() => { $$.ajax.load('${urlList}', $$.shell.$content()) })">${l.l('Вставить')} [${markTypeString}]</button>

	<%-- <button type="button" class="btn-grey ml1" onclick="openUrlContent( '${urlList}' )">${l.l('Сбросить выделение')}</button> --%>

	<ui:input-text name="filter" placeholder="${l.l('Фильтр')}" size="40" value="${form.param['filter']}" 
		onSelect="$$.ajax.load(this.form, $$.shell.$content())"
		title="${l.l('Фильтр по наименованию, конфигурации')}"/>

	<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
</html:form>

<div class="mt1">
	<c:url var="url" value="/admin/process.do">
		<c:param name="action" value="typeList"/>
		<c:param name="parentTypeId" value="0"/>
		<c:param name="markType" value="${form.param.markType}"/>
	</c:url>

	&#160;
	<a href="#" onClick="openUrlContent('${url}'); return false;">${l.l('Типы процессов')}</a>

	<c:forEach var="item" items="${typePath}" varStatus="status">
		<c:url var="url" value="/admin/process.do">
			<c:param name="action" value="typeList"/>
			<c:param name="parentTypeId" value="${item.id}"/>
			<c:param name="markType" value="${form.param.markType}"/>
		</c:url>
		-> <a href="#" onClick="openUrlContent('${url}'); return false;">${item.title}</a>
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
			<c:url var="deleteUrl" value="/admin/process.do">
				<c:param name="action" value="typeDelete"/>
				<c:param name="id" value="${item.id}"/>
				<c:param name="parentTypeId" value="${form.param.parentTypeId}"/>
			</c:url>
			<c:url var="deleteAjaxCommandAfter" value="openUrlContent( '${form.requestUrl}' )"/>

			<td nowrap="nowrap">
				<%-- <%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%> --%>
				<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.load('${editUrl}', $$.shell.$content())"/>
				<ui:button type="del" styleClass="btn-small" onclick="$$.ajax.post('${deleteUrl}').done(() => { $$.ajax.load('${form.requestUrl}', $$.shell.$content()) })"/>
				<%-- TODO: некорректно работает, когда выбрана не первая страница, решение см. редактор групп --%>
				<%-- <c:url var="url" value="${urlList}">
					<c:param name="markType" value="${item.id}"/>
				</c:url> --%>
				<%-- <button type="button" class="btn-white btn-small" onclick="openUrlContent( '${url}' )" title="Вырезать">C</button> --%>

				<ui:button type="cut" styleClass="btn-small"
					onclick="$('#${uiid}')[0].markType.value=${item.id};
							toPage($('#${uiid}')[0], ${form.page.pageIndex}, ${form.page.pageSize}, '');
							$$.ajax.load($('#${uiid}'), $$.shell.$content())"/>
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
						<a href="#" onclick="openUrlContent('${url}'); return false;">${itemPath.title}</a>
					</c:if>
					<c:if test="${not empty form.param.filter && not status.last}">
						<a href="#" onclick="openUrlContent('${url}'); return false;">${itemPath.title}</a> ->
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
					<c:otherwise><a href="#" onclick="openUrlContent('${url}'); return false;">[свойства]</a></c:otherwise>
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
	
						<button type="button" class="btn-white btn-small icon" title="${l.l('Использующие очереди')}"
							onclick="${hideButtonsScript} $$.ajax.load('${showUrl}', $('#${showId}'));"><i class="ti-search"></i></button>
					</p:check>
					<p:check action="ru.bgcrm.struts.action.admin.ProcessAction:typeCopy">
						<c:url var="showUrl" value="/admin/process.do">
							<c:param name="action" value="typeCopy" />
							<c:param name="id" value="${item.id}"/>
							<c:param name="parentId" value="${form.param.parentTypeId}"/>
						</c:url>
	
						<button type="button" class="btn-white btn-small icon" title="${l.l('Копировать свойства другого типа')}"
							onclick="${hideButtonsScript} $$.ajax.load('${showUrl}', $('#${showId}'));"><i class="ti-import"></i></button>
					</p:check>
				</div>
				<div id="${showId}" class="editor"></div>
			</td>
		</tr>
	</c:forEach>
</table>

<shell:title ltext="Типы процессов"/>
<shell:state help="kernel/process/index.html#type"/>
