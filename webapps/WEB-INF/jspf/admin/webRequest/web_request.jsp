<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ page language="java" import="java.lang.*, java.util.*, java.text.*" %>
<%@ page import="ru.bgcrm.util.PermissionNode"%>

<c:set var="uiid" value="webRequestSearchForm"/>

</br>
<form action="../admin/webRequest.do" id="${uiid}">
	<input type="hidden" name="action" value="findRequests"/>

	<table>
		<tr>
			<td style="vertical-align:top">
				<b>С даты</b>
				</br>
				<input type="text" name="dateFrom" value=""/>
				<c:set var="type" value="ymdhm"/>
				
				<c:set var="selector">#${uiid} input[name='dateFrom']</c:set>
				<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
				</br>
				
				<b>По дату</b>
				</br>
				<input type="text" name="dateTo" value=""/>
				<c:set var="type" value="ymdhm"/>
				
				<c:set var="selector">#${uiid} input[name='dateTo']</c:set>
				<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
				</br>				
				
				<b>Фильтр по IP адресу</b>
				</br>
				<input type="text" name="ipAddress" value=""/>
				</br>
				
				<b>Фильтр по Параметрам</b>
				</br>
				<input type="text" name="parameter" value=""/>
			</td>	
						
			<td>
				<td style="vertical-align:top">
					<c:set var="groupListId" value="${u:uiid()}"/>
					<c:set var="executorListId" value="${u:uiid()}"/>
					<c:set var="width" value="300"/>
					
					<div style="width:200px; display:table">
						<u:sc>
							<c:set var="id" value="${groupListId}"/>
							<c:set var="paramName" value="group"/>
							<c:set var="list" value="${ctxUserGroupList}"/>
							<c:set var="values" value="${filter.defaultValues}"/>
							<c:set var="available" value="${filter.availableValues}"/>
							<c:set var="prefixText" value="Группы:"/>
							<c:set var="widthTextValue" value="300px"/>
							<c:set var="onChange">updateExecutors( $('#${groupListId}'), $('#${executorListId}'), 'group', 'user' , '', '' );</c:set>
							<%@ include file="/WEB-INF/jspf/combo_check.jsp"%>
						</u:sc>
					</div>
				</td>
				<td style="vertical-align:top">
					<div style="width:200px; display:table">
						<u:sc>
							<c:set var="id" value="${executorListId}"/>		
							<c:set var="prefixText" value="Исполнители:"/>
							<c:set var="paramName" value="executor"/>
							<c:set var="list" value="${ctxUserGroupList}"/>		
							<c:set var="values" value="${filter.defaultValues}"/>
							<c:set var="available" value="${filter.availableValues}"/>
							<c:set var="widthTextValue" value="300px"/>
							<%@ include file="/WEB-INF/jspf/combo_check.jsp"%>
						</u:sc>
					</div>
				</td>
			</td>			
			
			<td  style="vertical-align:top">
				<b>Фильтр по действию</b>
				</br>
				<c:set var="permissionTreeId" value="${u:uiid()}"/>

				<ul id="${permissionTreeId}">
					<c:forEach var="tree" items="<%= PermissionNode.getPermissionTrees()%>">
							<c:set var="node" value="${tree}" scope="request" />
							<c:set var="paramName" value="actionTitle" scope="request"/>
							<jsp:include page="check_tree_item.jsp" />
					</c:forEach>
				</ul>
				
				<script>
					$( function() {
						$("#${permissionTreeId}").Tree();					
					} );			
				</script>
				
			</td>
			
			<td style="vertical-align:top">
				<u:sc>
					<c:set var="valuesHtml">
						<li value="0">---</option>
						<li value="uid">Пользователю</li>
						<li value="action">Действию</li>
						<li value="ipAddress">IP адресу</li>
					</c:set>
					
					<c:set var="hiddenName" value="sort"/>
					<c:set var="prefixText" value="Сортировать:"/>
					<c:set var="widthTextValue" value="40px"/>
					<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
				</u:sc>
				
				
			</td>
			
			<td style="vertical-align:top">
				<input type="button" value="Применить" onclick="openUrlTo(formUrl(this.form),$('#${uiid}-requestList'));"/>
			</td>
		</tr>
	</table>
	
</form>

<!-- <script>
	$('#${uiid} select[name=userId]').combobox();
	$('#${uiid} input.ui-combobox-input').autocomplete( "option", "minLength", 3 );
</script> -->

<div width="100%" id="${uiid}-requestList">
</div>