<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="admin/user">
	<input type="hidden" name="action" value="permsetList"/>
	<input type="hidden" name="pageableId" value="permsetList"/>
	
	<c:url var="url" value="/admin/user.do">
	    <c:param name="action" value="permsetGet"/>
	    <c:param name="id" value="-1"/>
	    <c:param name="returnUrl" value="${form.requestUrl}"/>
  	</c:url>
  	<button type="button" class="btn-green mr1" onclick="openUrlContent( '${url}' )">+</button>
			
	<ui:input-text name="filter" styleClass="ml1" value="${form.param.filter}" placeholder="Фильтр" size="40" title="Фильтр по наименованию, комментарию, конфигурации, параметрам действий"
		onSelect="openUrlContent( formUrl( this.form ) ); return false;"/>
			
	<button class="btn-grey ml1" type="button" onclick="openUrlContent( formUrl( this.form ) )">=&gt;</button>
			
	<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
</html:form>

<table style="width: 100%;" class="data mt1">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>
		<td width="30%">Наименование</td>
		<td width="70%">Комментарий</td>
	</tr>
	<c:forEach var="permset" items="${form.response.data.list}">
		<tr>
			<c:url var="editUrl" value="/admin/user.do">
				<c:param name="action" value="permsetGet"/>
				<c:param name="id" value="${permset.id}"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
			</c:url>
			<c:url var="deleteAjaxUrl" value="/admin/user.do">
				<c:param name="action" value="permsetDelete"/>
				<c:param name="id" value="${permset.id}"/>
			</c:url>
			<c:url var="deleteAjaxCommandAfter" value="openUrlContent( '${form.requestUrl}' )"/>
			
			<c:set var="uiid" value="${u:uiid()}"/>
			
			<td nowrap="nowrap" id="${uiid}">
				<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
				
				<button type="button" class="btn-white btn-small"
					title="Заменить права набора на права из другого набора"
					onclick="$('#${uiid} > input').hide(); $('#${uiid} > form').css('display', 'inline');">R</button>				
							
				<html:form style="display: none;" action="/admin/user" onsubmit="return false;" styleClass="ml1">
					<input type="hidden" name="action" value="permsetReplacePermissions"/>
					<input type="hidden" name="id" value="${permset.id}"/>
					
					<u:sc>
						<c:set var="list" value="${ctxUserPermsetList}"/>
						<c:set var="hiddenName" value="fromId"/>
						<c:set var="placeholder" value="Выберите набор"/>
						<c:set var="style" value="width: 200px;"/>
						<%@ include file="/WEB-INF/jspf/select_single.jsp"%>
					</u:sc>
					
					<button 
						type="button" class="btn-grey ml1"  
						onclick="if( confirm( 'Вы уверены, что хотите заменить права\nна права из выбранного набора?' ) && sendAJAXCommand( formUrl( this.form ) ) ){ $('#${uiid} > form').hide(); $('#${uiid} > input').show(); }">OK</button>
					<button 
						type="button" class="btn-grey" 
						onclick="$('#${uiid} > form').hide(); $('#${uiid} > input').show();">Отмена</button>
				</html:form>
			</td>
			
			<td>${permset.id}</td>
			<td>${permset.title}</td>
			<td>${permset.comment}</td>
		</tr>
	</c:forEach>
</table>

<c:set var="title" value="Наборы прав"/>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>