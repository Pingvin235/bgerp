<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="customerLinkRoleConfig" value="${ctxSetup.getConfig('ru.bgcrm.model.customer.config.ProcessLinkModesConfig')}"/>

<c:set var="uiid" value="${u:uiid()}" scope="request"/>

<c:set var="linkedObjects" value="['0'"/>
<c:forEach var="item" items="${frd.list}">
	<c:set var="linkedObjects">${linkedObjects},'${item.linkObjectType.replace(":", "-" )}-${item.linkObjectId}'</c:set>
</c:forEach>
<c:set var="linkedObjects">${linkedObjects}]</c:set>

<c:set var="script">
	var customerLinkRoles = [];
	<c:forEach var="item" items="${customerLinkRoleConfig.modeList}">
		customerLinkRoles.push( ['${item.id}', '${item.title}'] );
	</c:forEach>

	<%-- TODO: move the logic to server side, used only in webapps\WEB-INF\jspf\user\plugin\bgbilling\process_link_for_add_list.jsp --%>
	var additionalLinksForAdd = [];

	<plugin:include endpoint="user.process.linkForAdd.list.jsp"/>

	$('#${uiid}addButton').hide();
	objectsToLinkTable( $('#${uiid} #linkTable'), ${form.id}, customerLinkRoles, ${linkedObjects} );
	objectsToLinkTable( $('#${uiid} #linkTableCoupled'), ${form.id}, customerLinkRoles, ${linkedObjects}, additionalLinksForAdd );
	$('#${uiid} #linkObjects').show();
</c:set>

<c:choose>
	<c:when test="${not empty form.param.header}">
		<div class="mt1 mb05">
			<h2>${form.param.header}
				<span id="${uiid}addButton" class="normal"> [<a href="#" onclick="${script}; return false;">${l.l('add')}</a>]</span>
			</h2>
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
		<td>${l.l('Type')}</td>
		<td width="100%">${l.l('Title')}</td>
	</tr>

	<c:forEach var="item" items="${frd.list}">
		<c:set var="item" value="${item}" scope="request"/>

		<c:url var="deleteAjaxUrl" value="/user/link.do">
			<c:param name="action" value="deleteLink"/>
			<c:param name="objectType" value="process"/>
			<c:param name="id" value="${form.id}"/>
			<c:param name="linkedObjectType" value="${item.linkObjectType}"/>
			<c:param name="linkedObjectId" value="${item.linkObjectId}"/>
		</c:url>

		<%-- scope="request", for jsp:include --%>
		<c:set var="delButton" scope="request">
			<ui:button type="del" styleClass="btn-small"
						onclick="$$.ajax.post('${deleteAjaxUrl}').done(() =>{ $$.ajax.load('${form.requestUrl}', $('#${uiid}').parent()); })"/>
		</c:set>

		<c:set var="customerLinkRole" value="${customerLinkRoleConfig.modeMap[item.linkObjectType]}"/>

		<c:if test="${not empty customerLinkRole}">
			<tr>
				<td>${delButton}</td>
				<td>${item.linkObjectId}</td>
				<td>${customerLinkRole}</td>
				<td><ui:customer-link id="${item.linkObjectId}" text="${item.linkObjectTitle}"/></td>
			</tr>
		</c:if>

		<plugin:include endpoint="user.process.link.list.jsp"/>
	</c:forEach>
</table>

<div id="${uiid}">
	<div id="linkObjects" style="display: none;">
		<div id="linkTable" class="mt1">
			<h1>${l.l('Привязать объект из буфера')}</h1>

			<table class="data">
				<tr>
					<td>&nbsp;</td>
					<td>${l.l('Type')}</td>
					<td width="100%">${l.l('Title')}</td>
				</tr>
				<%-- other rows are generated --%>
			</table>
		</div>

		<div id="linkTableCoupled" class="mt1">
			<h1>${l.l('Привязать связанный с уже привязаным объектом')}</h1>

			<table class="data">
				<tr>
					<td>&nbsp;</td>
					<td>${l.l('Type')}</td>
					<td width="100%">${l.l('Title')}</td>
				</tr>
				<%-- other rows are generated --%>
			</table>
		</div>

		<h1>${l.l('Привязать иной объект')}</h1>

		<c:set var="linkObjectItems" scope="request" value=""/>
		<c:set var="linkObjectForms" scope="request" value=""/>

		<%@ include file="list_add_customer_search.jsp"%>

		<plugin:include endpoint="user.process.linkForAddCustom.jsp"/>

		<ui:combo-single
			hiddenName="param" prefixText="${l.l('Type')}:" style="width: 100%;"
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
						<input style="width: 100%;" name="linkedObjectType" placeholder="${l.l('Type')}"/>
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
			<button class="btn-white mr1" type="button" onclick="$('#${uiid} #linkObjects').hide(); $('#${uiid}addButton').show();">${l.l('Отмена')}</button>
		</div>
	</div>
</div>
