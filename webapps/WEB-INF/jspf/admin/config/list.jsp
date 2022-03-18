<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="tabsUiid" value="${u:uiid()}"/>
<div id="${tabsUiid}">
	<ul>
		<li><a href="#${tabsUiid}-1">${l.l('Конфигурация')}</a></li><%--
	--%><li><a href="#${tabsUiid}-2">${l.l('Плагины')} (${ctxPluginManager.pluginList.size()} / ${ctxPluginManager.fullSortedPluginList.size()})</a></li>
	</ul>
	<div id="${tabsUiid}-1">
		<html:form action="/admin/config" styleClass="in-mr1">
			<input type="hidden" name="action" value="list"/>

			<c:url var="url" value="/admin/config.do">
				<c:param name="action" value="get"/>
				<c:param name="id" value="-1"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
			</c:url>
			<ui:button type="add" onclick="$$.ajax.load('${url}', $$.shell.$content(this))"/>

			<ui:input-text name="filter" value="${form.param['filter']}" size="20" placeholder="${l.l('Фильтр')}"
				title="${l.l('Фильтр по содержимому конфигурации')}"
				onSelect="$$.ajax.load(this.form, $$.shell.$content(this)); return false;"/>
		</html:form>

		<table class="data mt1">
			<tr>
				<td width="30">&#160;</td>
				<td width="30">ID</td>
				<td width="50">${l.l('Активный')}</td>
				<td width="50%">${l.l('Наименование')}</td>
				<td width="50%">${l.l('Включенные плагины')}</td>
				<td>&nbsp;</td>
			</tr>
			<c:forEach var="item" items="${form.response.data.list}">
				<c:set var="item" scope="request" value="${item}"/>
				<jsp:include page="config_item.jsp"/>
			</c:forEach>
		</table>
	</div>
	<div id="${tabsUiid}-2" style="width: 100%; max-width: 100%;" class="in-inline-block in-va-top">
		<div style="width: 50%;">
			<h2>${l.l('Активные плагины')}</h2>
			<table class="data">
				<tr>
					<td>ID</td>
					<td>${l.l('Наименование')}</td>
				</tr>
				<c:forEach var="item" items="${ctxPluginManager.pluginList}">
					<tr>
						<td>${item.id}</td>
						<td>${item.title}</td>
					</tr>
				</c:forEach>
			</table>
			<%@ include file="../app/app_restart.jsp"%>
		</div><%--
	--%><div class="pl1" style="width: 50%;">
			<h2>${l.l('Лицензия')}</h2>
			<div class="box p05" style="white-space: nowrap; overflow-x: auto;">
				${u:htmlEncode(license.data)}
			</div>
			<p:check action="ru.bgcrm.struts.action.admin.ConfigAction:licenseUpload">
				<c:set var="uploadFormId" value="${u:uiid()}"/>
				<form id="${uploadFormId}" action="/admin/config.do" method="POST" enctype="multipart/form-data" name="form">
					<input type="hidden" name="action" value="licenseUpload"/>
					<input type="file" name="file" style="visibility: hidden; display: none;"/>
					<button type="button" class="btn-grey w100p mt1" onclick="$$.ajax.triggerUpload('${uploadFormId}');">${l.l('Загрузить файл лицензии')}</button>
				</form>
				<script>
					$(function () {
						$$.ajax.upload('${uploadFormId}', 'lic-upload-iframe', function () {
							$$.ajax.load('${form.requestUrl}', $$.shell.$content(this));
						});
					});
				</script>
			</p:check>
		</div>
	</div>
</div>

<script>
	$(function() {
		$('#${tabsUiid}').tabs();
	});
</script>

<shell:title ltext="Конфигурация"/>
<shell:state help="kernel/setup.html#config"/>
