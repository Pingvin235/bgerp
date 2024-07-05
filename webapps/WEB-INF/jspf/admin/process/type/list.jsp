<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="urlList" value="${form.httpRequestURI}">
	<c:param name="method" value="typeList"/>
	<c:param name="parentTypeId" value="${form.param.parentTypeId}"/>
	<c:param name="filter" value="${form.param.filter}"/>
</c:url>

<html:form  action="${form.httpRequestURI}" styleClass="in-mr1" styleId="${uiid}">
	<input type="hidden" name="method" value="typeList"/>
	<html:hidden property="parentTypeId"/>
	<html:hidden property="markType"/>

	<c:url var="url" value="${form.httpRequestURI}">
		<c:param name="method" value="typeGet"/>
		<c:param name="id" value="-1"/>
		<c:param name="returnUrl" value="${form.requestUrl}"/>
		<c:param name="parentTypeId" value="${form.param.parentTypeId}"/>
	</c:url>
	<ui:button type="add" onclick="$$.ajax.loadContent('${url}', this)"/>

	<c:url var="url" value="${form.httpRequestURI}">
		<c:param name="method" value="typeInsertMark"/>
		<c:param name="markType" value="${form.param.markType}"/>
		<c:param name="parentTypeId" value="${form.param.parentTypeId}"/>
	</c:url>
	<button type="button" class="btn-grey"
		onclick="$$.ajax.post('${url}').done(() => $$.ajax.loadContent('${urlList}', this) )">${l.l('Вставить')} [${markTypeString}]</button>

	<ui:input-text name="filter" placeholder="${l.l('Filter')}" size="40" value="${form.param['filter']}"
		onSelect="$$.ajax.loadContent(this)"
		title="${l.l('Фильтр по наименованию, конфигурации')}"/>

	<ui:page-control/>
</html:form>

<div class="mt1">
	<c:url var="url" value="${form.httpRequestURI}">
		<c:param name="method" value="typeList"/>
		<c:param name="parentTypeId" value="0"/>
		<c:param name="markType" value="${form.param.markType}"/>
	</c:url>

	&#160;
	<a href="#" onClick="$$.ajax.loadContent('${url}', this); return false;">${l.l('Типы процессов')}</a>

	<c:forEach var="item" items="${typePath}" varStatus="status">
		<c:url var="url" value="${form.httpRequestURI}">
			<c:param name="method" value="typeList"/>
			<c:param name="parentTypeId" value="${item.id}"/>
			<c:param name="markType" value="${form.param.markType}"/>
		</c:url>
		/ <a href="#" onClick="$$.ajax.loadContent('${url}', this); return false;">${item.title}</a>
	</c:forEach>
</div>

<table class="data mt1 hl">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>
		<td width="100%">${l.l('Title')}</td>
		<td>${l.l('Subtypes')}</td>
		<td>${l.l('Properties')}</td>
		<td width="20%">&#160;</td>
	</tr>
	<c:forEach var="item" items="${frd.list}">
		<tr>
			<%-- подгрузка типа из кэша позволяет получить число подтипов --%>
			<c:set var="item" value="${ctxProcessTypeMap[item.id]}"/>

			<c:url var="editUrl" value="${form.httpRequestURI}">
				<c:param name="method" value="typeGet"/>
				<c:param name="id" value="${item.id}"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
				<c:param name="parentTypeId" value="${form.param.parentTypeId}"/>
			</c:url>
			<c:url var="deleteUrl" value="${form.httpRequestURI}">
				<c:param name="method" value="typeDelete"/>
				<c:param name="id" value="${item.id}"/>
				<c:param name="parentTypeId" value="${form.param.parentTypeId}"/>
			</c:url>

			<td nowrap="nowrap">
				<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.loadContent('${editUrl}', this)"/>
				<ui:button type="del" styleClass="btn-small" onclick="
					$$.ajax
						.post('${deleteUrl}', {control: this})
						.done(() => $$.ajax.loadContent('${form.requestUrl}', this) )"/>

				<ui:button type="cut" styleClass="btn-small"
					onclick="$('#${uiid}')[0].markType.value=${item.id};
							toPage($('#${uiid}')[0], ${form.page.pageIndex}, ${form.page.pageSize}, '');
							$$.ajax.loadContent($('#${uiid}'), this)"/>
			</td>

			<td>${item.id}</td>

			<td>
				<c:forEach var="itemPath" items="${item.path}" varStatus="status">
					<c:url var="url" value="${form.httpRequestURI}">
						<c:param name="method" value="typeList"/>
						<c:param name="markType" value="${form.param.markType}"/>
						<c:param name="parentTypeId" value="${itemPath.id}"/>
					</c:url>
					<%-- the last path item is always shown --%>
					<c:if test="${status.last}">
						<a href="#" onclick="$$.ajax.loadContent('${url}', this); return false;">${itemPath.title}</a>
					</c:if>
					<%-- search mode, previous path items are shown --%>
					<c:if test="${not empty form.param.filter && not status.last}">
						<a href="#" onclick="$$.ajax.loadContent('${url}', this); return false;">${itemPath.title}</a> /
					</c:if>
				</c:forEach>
			</td>

			<td>${item.childCount}</td>

			<c:url var="url" value="${form.httpRequestURI}">
				<c:param name="method" value="properties"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>
			<td nowrap="nowrap">
				<c:choose>
					<c:when test="${item.useParentProperties}">
						[${l.l('inherited')}]
					</c:when>
					<c:otherwise><a href="#" onclick="$$.ajax.loadContent('${url}', this); return false;">[${l.l('свойства')}]</a></c:otherwise>
				</c:choose>
			</td>

			<td nowrap="nowrap">
				<c:set var="showId" value="${u:uiid()}"/>
				<div class="buttons">
					<c:set var="hideButtonsScript">$(this).closest('.buttons').hide();</c:set>
					<p:check action="ru.bgcrm.struts.action.admin.ProcessAction:typeUsed">
						<c:url var="showUrl" value="${form.httpRequestURI}">
							<c:param name="method" value="typeUsed" />
							<c:param name="typeId" value="${item.id}" />
						</c:url>

						<button type="button" class="btn-grey btn-small icon" title="${l.l('Использующие очереди')}"
							onclick="${hideButtonsScript} $$.ajax.load('${showUrl}', $('#${showId}'));"><i class="ti-search"></i></button>
					</p:check>
					<p:check action="ru.bgcrm.struts.action.admin.ProcessAction:typeCopy">
						<c:url var="showUrl" value="${form.httpRequestURI}">
							<c:param name="method" value="typeCopy" />
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

<shell:title text="${l.l('Типы процессов')}"/>
<shell:state help="kernel/process/index.html#type"/>
