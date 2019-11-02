<%@page import="ru.bgcrm.model.user.Group"%>
<%@page import="ru.bgcrm.cache.UserCache"%>
<%@page import="ru.bgcrm.model.user.UserGroup"%>
<%@page import="ru.bgcrm.model.IdTitle"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:set var="reloadScript">openUrlToParent('${form.requestUrl}', $('#addGroup${uiid}') );</c:set>

<c:if test="${readOnly != true}">
	<html:form action="admin/user" styleId="addGroup${uiid}" styleClass="in-mb1-all" style="display: none;">
		<input type="hidden" name="action" value="userAddGroup" />
		<html:hidden property="id" />		
		
		<table>
			<tr>
				<td nowrap="nowrap">Период с:</td>
				<td class="pl05">
					<ui:date-time paramName="fromDate" value="0"/>
				    по:
			   		<ui:date-time paramName="toDate"/>
			   	</td>	
	   		</tr>
			<tr>
				<td class="pt05">Группа:</td>
				<td style="width: 100%" class="pl05 pt05">
					<u:sc>
						<c:set var="list" value="${ctxUserGroupFullTitledList}"/>
						<c:set var="hiddenName" value="group"/>
						<c:set var="style" value="width: 100%;"/>
						<%@ include file="/WEB-INF/jspf/select_single.jsp"%>
					</u:sc>
				</td>
			</tr>		
		</table>	
		
		<button type="button" class="btn-grey mr1" onclick="if ( sendAJAXCommand( formUrl( $('#addGroup${uiid}') ) ) ) { ${reloadScript} }">ОК</button>
		<button type="button" class="btn-grey" onclick="$('#addGroup${uiid}').hide(); $('#showGroup${uiid}').show();">Отмена</button>
	</html:form>
</c:if>

<html:form action="admin/user" styleId="showGroup${uiid}">
	<input type="hidden" name="action" value="userGroupList" />
	<html:hidden property="id" />

	<c:if test="${readOnly != true}">
		<div class="in-mr1">
			<button type="button" class="btn-green" title="Добавить" onclick="$('#showGroup${uiid}').hide(); $('#addGroup${uiid}').show();">+</button>
					
			<ui:date-time styleClass="ml1" paramName="date" value="${form.param.date}" placeholder="На дату"/>
				        	
        	<button type="button" class="btn-grey" onclick="openUrlToParent( formUrl( this.form ), $('#showGroup${uiid}') );" title="Применить">=&gt;</button>
        </div>	
	</c:if>
			
	<table style="width: 100%;" class="data mt1">
		<tr>
			<c:if test="${readOnly != true}">
				<td width="100">Управление</td>
			</c:if>
			<td width="100">Период</td>
			<td width="100%">Группа</td>
		</tr>
	    
	    <c:set var="list" value="${ctxUserGroupList}" />
		<c:set var="paramName" value="group" />
		<c:set var="values" value="${user.groupIds}" />
		<c:set var="moveOn" value="0"/>
										
	    <c:forEach var="value" items="${userGroupList}">
			<c:forEach var="item" items="${list}">
				<c:if test="${item.id eq value.id}">
					<tr>
						<c:if test="${readOnly != true}">
							<td style="text-align: center;" nowrap="nowrap">
								<button type="button" class="btn-white btn-small" title="Удалить" onclick="if( confirm( 'Вы уверены, что хотите удалить?' ) && sendAJAXCommand( '/admin/user.do?action=userRemoveGroup&userId=${form.id}&groupId=${item.id}&dateFrom=${u:formatDate(value.dateFrom, 'ymdhms')}&dateTo=${u:formatDate(value.dateTo, 'ymdhms')}&markGroup=' ) ){ $(this).parents('tr').first().remove(); }">&nbsp;X&nbsp;</button>
		    					<button type="button" class="btn-white btn-small" title="Закрыть период" onclick="$('#closeGroupId${uiid}').val(${item.id}); $('#dateFrom${uiid}').val('${u:formatDate(value.dateFrom, 'ymdhms')}'); $('#dateTo${uiid}').val('${u:formatDate(value.dateTo, 'ymdhms')}'); $('#showGroup${uiid}').hide(); $('#closeGroup${uiid}').show();">&gt;|</button>
		    				</td>
	    				</c:if>
						<td nowrap="nowrap">${u:formatDate(value.dateFrom, 'ymd')} - ${u:formatDate(value.dateTo, 'ymd')}</td>
                        <td>${item.titleWithPath}</td>
					</tr>
				</c:if>
			</c:forEach>
		</c:forEach>
	</table>	
</html:form>

<html:form action="admin/user" styleId="closeGroup${uiid}" style="display: none;" styleClass="in-inline-block">
	<input type="hidden" name="action" value="userClosePeriodGroup" />
	<input id="dateFrom${uiid}" type="hidden" name="dateFrom" value=""/>
	<input id="dateTo${uiid}" type="hidden" name="dateTo" value=""/>
	<input id="closeGroupId${uiid}" name="groupId" type="hidden" value="-1" />
	<input name="userId" type="hidden" value="${form.id}" />

	Закрыть с даты:
	
	<div>
		<input id="dateClose${uiid}" name="date" type="text"/>	
		<c:set var="selector">input#dateClose${uiid}</c:set>
		<c:set var="initialDate">0</c:set>	
		<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
	</div>
	
	<div class="in-ml1">
		<button type="button" class="btn-grey" onclick="if( confirm( 'Вы уверены, что хотите закрыть период группы?' ) && sendAJAXCommand( formUrl( this.form ) ) ){ ${reloadScript} }">ОК</button>
		<button type="button" class="btn-grey" onclick="$('#closeGroup${uiid}').hide(); $('#showGroup${uiid}').show();">Отмена</button>
	</div>
</html:form>
