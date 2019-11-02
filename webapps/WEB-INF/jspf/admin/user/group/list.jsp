<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>
	
<c:set var="uiid" value="${u:uiid()}"/> 

<c:url var="urlList" value="/admin/user.do">
   <c:param name="action" value="groupList"/>
   <c:param name="parentGroupId" value="${form.param.parentGroupId}"/>
</c:url>

<html:form action="admin/user" styleId="${uiid}" styleClass="in-mr1">
    <input type="hidden" name="action" value="groupList"/>
    <input type="hidden" name="parentGroupId" value="${form.param.parentGroupId}"/>
    <input type="hidden" name="markGroup" value="${form.param.markGroup}"/> 

	<c:url var="url" value="/admin/user.do">
	    <c:param name="action" value="groupGet"/>
	    <c:param name="id" value="-1"/>
	    <c:param name="returnUrl" value="${form.requestUrl}"/>
	    <c:param name="parentGroupId" value="${form.param.parentGroupId}"/>
  	</c:url>
  	<button class="btn-green" type="button" onclick="openUrlContent('${url}' )">+</button>
			
	<c:url value="/admin/user.do" var="url">
		<c:param name="action" value="groupInsertMark"/>
		<c:param name="parentGroupId" value="${form.param.parentGroupId}"/>
	    <c:param name="markGroup" value="${form.param.markGroup}"/>					
	</c:url>
	<button type="button" id="markGroupButton" class="btn-grey ml1" onclick="if( sendAJAXCommand( '${url}' ) ){ openUrlContent('${urlList}');  }">Вставить [${markGroupString}]</button>
		
	<%--
	<div class="ml1" style="display: inline-block;">	
		<u:sc>
			<c:set var="valuesHtml">
				<li value="-1">Все</li>
				<li value="0">Активные</li>
				<li value="1">Скрытые</li>
			</c:set>
			<c:set var="hiddenName" value="archive"/>
			<c:set var="value" value="${form.param['archive']}"/>
			<c:set var="onSelect" value=" openUrlContent( formUrl( $('#${uiid}') ) ) "/>
			<c:set var="prefixText" value="Вывести:"/>
			<c:set var="widthTextValue" value="50px"/>
			<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
	    </u:sc>
	</div>
	 --%>
	
	<ui:input-text name="filter" onSelect="openUrlContent( formUrl( this.form ) ); return false;" placeholder="Фильтр" size="40" value="${form.param['filter']}" title="Фильтр по наименованию, конфигурации"/>
	
	<button class="btn-grey" type="button" onclick="openUrlContent( formUrl( this.form ) )">=&gt;</button>

	<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
</html:form>	

<div class="mt1">
    <c:url var="url" value="/admin/user.do">
        <c:param name="action" value="groupList"/>
        <c:param name="parentGroupId" value="0"/>    
        <c:param name="markGroup" value="${form.param.markGroup}"/>
    </c:url>

	&nbsp;
	<a href="#UNDEF" onClick="openUrlContent('${url}'); return false;">Группы</a>
	
	<c:forEach var="item" items="${groupPath}" varStatus="status">
		<c:url var="url" value="/admin/user.do">
			<c:param name="action" value="groupList"/>
			<c:param name="parentGroupId" value="${item.id}"/>	
			<c:param name="markGroup" value="${form.param.markGroup}"/>
		</c:url>
		-> <a href="#UNDEF" onClick="openUrlContent('${url}'); return false;">${item.title}</a>
	</c:forEach>
</div>

<table style="width: 100%;" class="data mt1">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>		
		<td width="30%">Наименование</td>
		<td width="50">Подгрупп</td>
		<td width="40%">Наборы прав</td>	
		<td width="50">Скрытая</td>		
		<td width="30%">Комментарий</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
			<c:url var="editUrl" value="/admin/user.do">
				<c:param name="action" value="groupGet"/>
				<c:param name="id" value="${item.id}"/>
				<c:param name="parentGroupId" value="${item.parentId}"/>				
				<c:param name="returnUrl" value="${form.requestUrl}"/>
			</c:url>
			<c:url var="deleteAjaxUrl" value="/admin/user.do">
				<c:param name="action" value="groupDelete"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>
			<c:url var="deleteAjaxCommandAfter" value="openUrlContent( '${form.requestUrl}' )"/>
			
			<td nowrap="nowrap">
				<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
				<button type="button" class="btn-white btn-small" 
				    onclick="$('#${uiid}')[0].markGroup.value=${item.id}; 
				             toPage($('#${uiid}')[0], ${page.pageIndex}, ${page.pageSize}, '');
				             openUrlContent( formUrl( $('#${uiid}') ) );" 
					title="Вырезать">C</button>				
			</td>
			
			<td>${item.id}</td>
			
			<td>
				<c:forEach var="items" items="${item.path}" varStatus="status">
					<c:url var="url" value="/admin/user.do">
		                <c:param name="action" value="groupList"/>
		                <c:param name="parentGroupId" value="${item.id}"/>
		                <c:param name="markGroup" value="${form.param.markGroup}"/>
		            </c:url>
					<c:if test="${status.last}">
						<a href="#UNDEF" onclick="openUrlContent('${url}'); return false;">${items.title}</a>
					</c:if>			
					<c:if test="${not empty form.param.filter && not status.last}">
						<a href="#UNDEF" onclick="openUrlContent('${url}'); return false;">${items.title}</a> ->
					</c:if>
				</c:forEach>
			</td>
			
			<td>${item.childCount}</td>
			<td>${u:orderedObjectTitleList( ctxUserPermsetMap, item.permsetIds )}</td>
			
			<td>
				<c:choose>
					<c:when test="${item.archive == 1}">Да</c:when>
					<c:otherwise>Нет</c:otherwise>
				</c:choose>
			</td>		
								
			<td>${item.comment}</td>
		</tr>
	</c:forEach>
</table>

<c:set var="title" value="Группы пользователей"/>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>