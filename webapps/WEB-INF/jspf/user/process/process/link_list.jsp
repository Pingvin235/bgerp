<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="customerLinkRoleConfig" value="${u:getConfig(setup, 'ru.bgcrm.model.customer.config.ProcessLinkModesConfig')}"/>

<c:set var="uiid" value="${u:uiid()}" scope="request"/>

<c:set var="linkedObjects" value="['0'"/>
<c:forEach var="item" items="${form.response.data.list}">
	<c:set var="linkedObjects">${linkedObjects},'${fn:replace( item.linkedObjectType, ":", "-" )}-${item.linkedObjectId}'</c:set>
</c:forEach>
<c:set var="linkedObjects">${linkedObjects}]</c:set>

<c:set var="script">
	var customerLinkRoles = [];
	<c:forEach var="item" items="${customerLinkRoleConfig.modeList}">
		customerLinkRoles.push( ['${item.id}', '${item.title}'] );
	</c:forEach>

	<%-- TODO: move the logic to server side, used only in webapps\WEB-INF\jspf\user\plugin\bgbilling\process_link_for_add_list.jsp --%>
	var additionalLinksForAdd = [];

	<c:set var="endpoint" value="user.process.linkForAdd.list.jsp"/>
	<%@ include file="/WEB-INF/jspf/plugin_include.jsp"%>

	$('#${uiid}addButton').hide();
	objectsToLinkTable( $('#${uiid} #linkTable'), ${form.id}, customerLinkRoles, ${linkedObjects} );
	objectsToLinkTable( $('#${uiid} #linkTableCoupled'), ${form.id}, customerLinkRoles, ${linkedObjects}, additionalLinksForAdd );
	$('#${uiid} #linkObjects').show();
</c:set>

<c:choose>
	<c:when test="${not empty form.param.header}">
		<div class="mt1 mb05">
			<h2 style="display: inline;">${form.param.header}</h2>
			<span id="${uiid}addButton" style="vertical-align: top;">[<a href="#UNDEF" onclick="${script}; return false;">${l.l('привязать')}</a>]</span>
		</div>
	</c:when>
	<c:otherwise>
		<button class="btn-green" id="${uiid}addButton" onclick="${script};" title="${l.l('Привязать')}">+</button>
	</c:otherwise>
</c:choose>

<table class="data mt05" style="width: 100%;">
	<tr>
		<td>&nbsp;</td>
		<td>ID</td>
		<td>${l.l('Тип')}</td>
		<td width="100%">${l.l('Наименование')}</td>
	</tr>

	<c:forEach var="item" items="${form.response.data.list}">
		<c:set var="item" value="${item}" scope="request"/>

		<%-- scope="request", чтобы были доступны в jsp:include --%>
		<c:url var="deleteAjaxUrl" value="link.do" scope="request">
			<c:param name="action" value="deleteLink"/>
			<c:param name="objectType" value="process"/>
			<c:param name="id" value="${form.id}"/>
			<c:param name="linkedObjectType" value="${item.linkedObjectType}"/>
			<c:param name="linkedObjectId" value="${item.linkedObjectId}"/>
		</c:url>
		<c:set var="deleteAjaxCommandAfter" scope="request">$$.ajax.load('${form.requestUrl}', $('#${uiid}').parent());</c:set>

		<c:set var="customerLinkRole" value="${customerLinkRoleConfig.modeMap[item.linkedObjectType]}"/>

		<c:if test="${not empty customerLinkRole}">
			<tr>
				<td><%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%></td>
				<td>${item.linkedObjectId}</td>
				<td>${customerLinkRole}</td>
				<td><ui:customer-link id="${item.linkedObjectId}" text="${item.linkedObjectTitle}"/></td>
			</tr>
		</c:if>

		<c:set var="endpoint" value="user.process.link.list.jsp"/>
		<%@ include file="/WEB-INF/jspf/plugin_include.jsp"%>

		<%-- некоторые привязки (процессы) тут не отображаются, и ссылка на кнопку удаления остаётся --%>
		<c:remove var="deleteAjaxUrl"/>
	</c:forEach>
</table>

<div id="${uiid}">
	<div id="linkObjects" style="display: none;">
		<div id="linkTable" class="mt1">
			<h1>${l.l('Привязать объект из буфера')}</h1>

			<table class="data">
				<tr>
					<td>&nbsp;</td>
					<td>${l.l('Тип')}</td>
					<td width="100%">${l.l('Наименование')}</td>
				</tr>
				<%-- other rows are generated --%>
			</table>
		</div>

		<div id="linkTableCoupled" class="mt1">
			<h1>${l.l('Привязать связанный с уже привязаным объектом')}</h1>

			<table class="data">
				<tr>
					<td>&nbsp;</td>
					<td>${l.l('Тип')}</td>
					<td width="100%">${l.l('Наименование')}</td>
				</tr>
				<%-- other rows are generated --%>
			</table>
		</div>

		<h1>${l.l('Привязать иной объект')}</h1>

		<c:set var="linkObjectItems" scope="request" value=""/>
		<c:set var="linkObjectForms" scope="request" value=""/>

		<%@ include file="link_list_add_customer_search.jsp"%>

		<c:set var="endpoint" value="user.process.linkForAddCustom.jsp"/>
		<%@ include file="/WEB-INF/jspf/plugin_include.jsp"%>

		<ui:combo-single 
			hiddenName="param" prefixText="${l.l('Тип')}:" style="width: 100%;"
			onSelect="$$.process.link.showForm('${uiid}', $hidden.val());">
			<jsp:attribute name="valuesHtml">
				<li value="none">-- ${l.l('нет')} --</li>
				${linkObjectItems}
				<li value="custom">${l.l('Произвольный объект')}</li>
			</jsp:attribute>
		</ui:combo-single>

		<div id="linkEditor" class="in-mt05-all">
			<form action="link.do" class="in-table-cell" id="custom" style="display: none;">
				<input type="hidden" name="action" value="addLink"/>
				<input type="hidden" name="objectType" value="process"/>
				<input type="hidden" name="id" value="${form.id}"/>

				<div class="in-table-cell">
					<div style="width: 30%;">
						<input style="width: 100%;" name="linkedObjectType" placeholder="${l.l('Тип')}"/>
					</div>
					<div style="width: 20%;" class="pl05">
						<input style="width: 100%;" name="linkedObjectId" placeholder="ID"/>
					</div>
					<div style="width: 50%;" class="pl05">
						<input style="width: 100%;" name="linkedObjectTitle" placeholder="${l.l('Заголовок')}"/>
					</div>
				</div>
			</form>
			${linkObjectForms}
		</div>

		<div class="mt1">
			<button class="btn-grey mr1" type="button" onclick="$$.process.link.add('${uiid}', '${form.requestUrl}')">OK</button>
			<button class="btn-grey mr1" type="button" onclick="$('#${uiid} #linkObjects').hide(); $('#${uiid}addButton').show();">${l.l('Отмена')}</button>
		</div>
	</div>
</div>
