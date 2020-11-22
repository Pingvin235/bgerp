<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="perm" value="${p:get(form.user.id, 'ru.bgcrm.struts.action.admin.UserAction:userUpdate')}" />
<c:set var="user" value="${form.response.data.user}" />
<c:set var="grantedPermission" value="${form.response.data.grantedPermission}" scope="request" />

<c:set var="formUiid" value="${u:uiid()}"/>

<html:form action="admin/user" styleId="${formUiid}">
	<input type="hidden" name="action" value="userUpdate" />
	<html:hidden property="id" />

	<h1>Основные свойства (сохранение/отмена по ОК/Отмена)</h1>

	<div class="separator"/>

	<c:set var="uiidMainBlock" value="${u:uiid()}"/>
	<div class="in-pr1 in-inline-block in-va-top" id="${uiidMainBlock}" style="display: table; width: 100%;">
		<div style="width: 15%;">
			<h2>ID</h2>
			<div>
				<input type="text" disabled="disabled" style="width: 100%" value="${form.id}"/>
				<h2>Имя</h2>
				<html:text property="title" style="width: 100%" value="${user.title}"/>
			</div>

			<h2>Логин</h2>
			<div>
				<html:text property="login" style="width: 100%" value="${user.login}"/>
				<h2>Пароль</h2>
				<html:password property="pswd" style="width: 100%" value="${user.password}"/>
				<h2>Статус</h2>
				<ui:combo-single hiddenName="status" value="${user.status}" widthTextValue="120px">
					<jsp:attribute name="valuesHtml">
						<li value="0">Активен</li>
						<li value="1">Заблокирован</li>
					</jsp:attribute>
				</ui:combo-single>
			</div>
		</div><%--
	--%><div style="width: 20%;">
	    	<h2>Комментарии</h2>

	    	<u:sc>
		    	<c:set var="uiid" value="${u:uiid()}"/>
		    	<c:set var="selectorSample" value="#${uiidMainBlock} > div:first > div:first"/>
		    	<c:set var="selectorTo" value="#${uiid}"/>
		    	<textarea id="${uiid}" style="width: 100%; resize: none;" name="description">${user.description}</textarea>
		    	<%@ include file="/WEB-INF/jspf/same_height.jsp"%>
		    </u:sc>

		    <c:if test="${empty perm['configDisable']}">
			    <h2>Конфигурация</h2>

			    <u:sc>
			    	<c:set var="uiid" value="${u:uiid()}"/>
			    	<c:set var="selectorSample" value="#${uiidMainBlock} > div:first > div:nth-of-type(2)"/>
			    	<c:set var="selectorTo" value="#${uiid}"/>
			    	<textarea id="${uiid}" style="width: 100%; resize: none;" wrap="off" name="userConfig">${user.config}</textarea>
			    	<%@ include file="/WEB-INF/jspf/same_height.jsp"%>
			    </u:sc>
			 </c:if>
	    </div><%--
    --%><c:if test="${empty perm['permsetSet']}"><%--
	    --%><div style="width: 20%;">
		    	<h2>Наборы прав</h2>

		    	<u:sc>
		    		<c:set var="list" value="${ctxUserPermsetList}"/>
		    		<c:set var="map" value="${ctxUserPermsetMap}"/>
					<c:set var="hiddenName" value="permset" />
					<c:set var="available" value="${u:toIntegerSet(perm['allowPermsetSet'])}"/>
					<c:set var="values" value="${user.permsetIds}" />
					<c:set var="moveOn" value="1"/>
					<c:set var="style" value="width: 100%;"/>
					<c:set var="styleClass" value="layout-height-rest"/>
					<%@ include file="/WEB-INF/jspf/select_mult.jsp"%>
				</u:sc>
		    </div><%--
	--%></c:if><%--
	--%><c:if test="${empty perm['permDisable']}"><%--
		--%><div style="width: 25%;">
				<h2>Права</h2>

				<c:set var="permissionTreeId" value="${u:uiid()}"/>
				<ul id="${permissionTreeId}" class="layout-height-rest" style="overflow: auto;">
					<c:forEach var="tree" items="${allPermissions}">
						<c:set var="node" value="${tree}" scope="request" />
						<jsp:include page="../check_tree_item.jsp" />
					</c:forEach>
				</ul>

				<script>
					$( function()
					{
						$("#${permissionTreeId}").Tree();
					} );
				</script>
			</div><%--
	--%></c:if><%--
	--%><c:if test="${empty perm['queueSet']}"><%--
		--%><div style="width: 20%;">
				<h2>${l.l('Очереди процессов')}</h2>

				<u:sc>
					<c:set var="list" value="${ctxProcessQueueList}" />
					<c:set var="hiddenName" value="queue"/>
					<c:set var="values" value="${user.queueIds}" />
					<c:set var="style" value="width: 100%;"/>
					<c:set var="styleClass" value="layout-height-rest"/>
					<%@ include file="/WEB-INF/jspf/select_mult.jsp"%>
				</u:sc>
			</div><%--
	--%></c:if><%--
--%></div>

	<u:sc>
		<c:set var="selectorSample" value="#${uiidMainBlock} > div:first-child"/>
		<c:set var="selectorTo" value="#${uiidMainBlock} > div:not(:first-child)"/>
		<%@ include file="/WEB-INF/jspf/same_height.jsp"%>
	</u:sc>
</html:form>

<div class="in-mr1 mt1">
	<c:set var="toPostNames" value="['config','userConfig']"/>
	<c:choose>
		<c:when test="${form.id le 0}">
			<c:set var="script">
				var result = sendAJAXCommand( formUrl( $('#${formUiid}') ), ${toPostNames} );
				if( result )
				{
					openUrlContent( '/admin/user.do?action=userGet&id=' + result.data.newUserId + '&returnUrl=' + encodeURIComponent('${form.returnUrl}') );
				}
			</c:set>
			<button class="btn-grey" onclick="${script}">Промежуточное сохранение</button>
		</c:when>
		<c:otherwise>
			<button class="btn-grey" onclick="if( sendAJAXCommand( formUrl( $('#${formUiid}') ), ${toPostNames} ) ){ openUrlContent( '${form.returnUrl}' ) }">ОК</button>
		</c:otherwise>
	</c:choose>
	<button class="btn-grey" onclick="openUrlContent( '${form.returnUrl}' )">Отмена</button>
</div>

<c:if test="${form.id gt 0}">
	<h1>Дополнительные свойства (сохраняются сразу)</h1>

	<div class="separator"/>

	<div style="height: 300px;" class="mt1 in-table-cell in-va-top in-pr1">
		<div <%-- style="min-width: 200px;" --%> class="pr1">
			<h2>Фото</h2>
			<div class="border-table" style="text-align: center; height: 150px; width: 150px; line-height: 150px; vertical-align: middle;">
				<div>НЕТ ФОТО</div>
			</div>
			<button class="btn-white mt05" style="width: 150px;">Обновить фото</button>
		</div>
		<div style="width: 50%;">
			<h2>Группы</h2>

			<c:url var="url" value="/admin/user.do">
				<c:param name="action" value="userGroupList" />
				<c:param name="id" value="${form.id}" />
				<c:param name="objectType" value="user" />
			</c:url>
			<c:import url="${url}" />
		</div>
		<div style="width: 50%;">
			<div>
				<c:url var="url" value="/user/parameter.do">
					<c:param name="action" value="parameterList" />
					<c:param name="id" value="${form.id}" />
					<c:param name="objectType" value="user" />
					<c:param name="header" value="Доп. параметры"/>
				</c:url>
				<c:import url="${url}" />
			</div>
		</div>
	</div>
</c:if>

<shell:state ltext="Редактор" help="kernel/setup.html#user"/>

<%@ include file="/WEB-INF/jspf/layout_process.jsp"%>