<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="customerLinkRoleConfig" value="${u:getConfig( setup, 'ru.bgcrm.model.customer.config.ProcessLinkModesConfig' )}"/>

<c:set var="uiid" value="${u:uiid()}"/>

<!-- если нет привязанных объектов, то будем передавать пустышку -->
<c:if test="${empty linkedObjects}">
	<c:set var="linkedObjects" value="['0']"/>
</c:if>
<c:set var="script">
	var customerLinkRoles = [];
	<c:forEach var="item" items="${customerLinkRoleConfig.modeList}">
		customerLinkRoles.push( ['${item[0]}', '${item[1]}'] );
	</c:forEach>
	
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
			<span id="${uiid}addButton" style="vertical-align: top;">[<a href="#UNDEF" onclick="${script}; return false;">привязать</a>]</span>
		</div>		
	</c:when>
	<c:otherwise>
		<button class="btn-green" id="${uiid}addButton" onclick="${script};" title="Привязать">+</button>
	</c:otherwise>
</c:choose>		

<table class="data mt05" style="width: 100%;">
	<tr>
		<td>&nbsp;</td>
		<td>ID</td>
		<td>Тип</td>
		<td width="100%">Наименование</td>
	</tr>
	
	<c:forEach var="item" items="${form.response.data.list}" varStatus="status">
		<c:set var="item" value="${item}" scope="request"/>
		
		<c:if test="${status.first}">
			<c:set var="linkedObjects" value="['0'"/>
		</c:if>
		<c:set var="linkedObjects">${linkedObjects},'${fn:replace( item.linkedObjectType, ":", "-" )}-${item.linkedObjectId}'</c:set>
		<c:if test="${status.last}">
			<c:set var="linkedObjects">${linkedObjects}]</c:set>
		</c:if>
		
		<%-- scope="request", чтобы были доступны в jsp:include --%>
		<c:url var="deleteAjaxUrl" value="link.do" scope="request">
			<c:param name="action" value="deleteLink"/> 
			<c:param name="objectType" value="process"/>
			<c:param name="id" value="${form.id}"/>
			<c:param name="linkedObjectType" value="${item.linkedObjectType}"/>
			<c:param name="linkedObjectId" value="${item.linkedObjectId}"/>
		</c:url>
		<c:set var="deleteAjaxCommandAfter" scope="request">openUrlToParent( '${form.requestUrl}', $('#${uiid}') );</c:set>
		
		<c:set var="customerLinkRole" value="${customerLinkRoleConfig.modeMap[item.linkedObjectType]}"/>
					
		<c:if test="${not empty customerLinkRole}">
			<tr>
				<td><%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%></td>
				<td>${item.linkedObjectId}</td>
				<td>${customerLinkRole}</td>
				<td><a href="#UNDEF" onclick="openCustomer( ${item.linkedObjectId} ); return false;">${fn:escapeXml( item.linkedObjectTitle )}</a></td>
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
		<h1>Привязать объект из буфера</h1>
	
		<div id="linkTable" class="mt1">
			<%-- сюда сгенерируется таблица с объектами из буфера --%>
		</div>
		
		<h1>Привязать связанный с уже привязаным объект</h1>
		
		<div id="linkTableCoupled" class="mt1">
			<%-- сюда сгенерируется таблица с объектами --%>
		</div>
		
		<h1>Привязать иной объект</h1>
		
		<c:set var="linkObjectItems" scope="request" value=""/>
		<c:set var="linkObjectForms" scope="request" value=""/>
		
		<c:set var="endpoint" value="user.process.linkForAddCustom.jsp"/>
		<%@ include file="/WEB-INF/jspf/plugin_include.jsp"%>
		
		<u:sc>
			<c:set var="valuesHtml">
				<li value="none">-- нет --</li>
				${linkObjectItems}
				<li value="custom">Произвольный объект</li>
			</c:set>
			<c:set var="hiddenName" value="param"/>
			<c:set var="prefixText" value="Тип:"/>
			<c:set var="style" value="width: 100%;"/>
			<c:set var="onSelect" value="$('#${uiid} #linkEditor > form').hide().find('input[name=check]')[0].checked = false; $('#${uiid} #linkEditor > form#' + $hidden.val()).show().find('input[name=check]')[0].checked = true;"/>
			<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
		</u:sc>		
		
		<div id="linkEditor" class="in-mt05-all">
			<form action="link.do" class="in-table-cell" id="custom" style="display: none;">
				<input type="hidden" name="action" value="addLink"/>
				<input type="hidden" name="objectType" value="process"/>
				<input type="hidden" name="id" value="${form.id}"/>
				
				<div class="in-table-cell">
					<div style="width: 30%;">
						<input style="width: 100%;" name="linkedObjectType" placeholder="Тип"/>
					</div>	
					<div style="width: 20%;" class="pl05">
						<input style="width: 100%;" name="linkedObjectId" placeholder="ID"/>
					</div>
					<div style="width: 50%;" class="pl05">	
				 		<input style="width: 100%;" name="linkedObjectTitle" placeholder="Заголовок"/>
				 	</div>	
				</div>	
			</form>
			${linkObjectForms}
		</div>
		
		<div class="mt1">
			<c:set var="script">
				var forms = $( '#${uiid} form:visible' );
				for( var i = 0; i < forms.length; i++ )
				{
					var form = forms[i];
					
					if( !form.check.checked )
					{
						continue;
					}
					if( !sendAJAXCommand( formUrl( form ) ) )
					{
						return;
					}
				}	
				
				openUrlToParent( '${form.requestUrl}', $('#${uiid}') );
			</c:set>
			
			<button class="btn-grey mr1" type="button" onclick="${script}">OK</button>
			<button class="btn-grey mr1" type="button" onclick="$('#${uiid} #linkObjects').hide(); $('#${uiid}addButton').show();">Отмена</button>
		</div>
	</div>
</div>
