<%@page import="ru.bgcrm.util.Utils"%>
<%@page import="ru.bgcrm.struts.form.DynActionForm"%>
<%@page import="ru.bgcrm.dao.ParamValueDAO"%>
<%@page import="ru.bgcrm.util.sql.SQLUtils"%>
<%@page import="java.sql.Connection"%>
<%@page import="javax.sql.DataSource"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:set var="config" value="${u:getConfig( ctxSetup, 'ru.bgcrm.dao.message.config.MessageTypeConfig' ) }"/>

<c:forEach var="type" items="${config.typeMap.values()}">
	<c:if test="${type.getClass().getName() eq 'ru.bgcrm.dao.message.MessageTypeCall'}">
		<c:set var="reg" value="${type.getRegistrationByUser( form.userId )}"/>
		
		<div class="mb1">
			<c:choose>
				<c:when test="${not empty reg}">
					<span class="tt">${type.title} номер <b>${reg.number}</b></span>
					<c:set var="url" value="messageCall.do?typeId=${type.id}&action=numberFree"/>
					<button type="button" class="btn-grey ml1" onclick="if( sendAJAXCommand( '${url}' ) ){ openUrlToParent( '${form.requestUrl}', $('#${uiid}') ) }" >Освободить</button>
				</c:when>
				<c:otherwise>
					<form action="/user/messageCall.do" style="display: inline-block;">
						<input type="hidden" name="action" value="numberRegister"/>
						<input type="hidden" name="typeId" value="${type.id}"/>
						
						<c:set var="paramId" value="${type.configMap.getInt( 'offerNumberFromParamId', 0 )}"/>
						<c:if test="${paramId gt 0}">
							<%
								DataSource dataSource = (DataSource)request.getAttribute( "ctxDataSource" );
								Connection con = dataSource.getConnection();
								
								try
								{
									ParamValueDAO paramDao = new ParamValueDAO( con );
									String phone = paramDao.getParamText( ((DynActionForm)request.getAttribute( "form" )).getUserId(), 
									                                      (Integer)pageContext.getAttribute( "paramId" ) );
									if( Utils.notBlankString( phone ) )
									{
										pageContext.setAttribute( "phone", phone );
									}
								}
								finally
								{
									SQLUtils.closeConnection( con );
								}
							%>
						</c:if>
						
						<input type="text" name="number" placeholder="${type.title}, номер" class="" value="${phone}"/>
						
						<c:set var="code">
							var result = sendAJAXCommand( formUrl( this.form ) );
						 	if( !result )
						 	{ 
						 		return; 
						 	} 
						 	
						 	var user = result.data.regUser;
						 	if( !user  || 
						 	    ( confirm( 'Номер занят пользователем: ' + user.title + ',\nвсё равно зарегистрировать?' ) && sendAJAXCommand( formUrl( this.form ) + '&check=0' ) ) )
						 	{
						 		openUrlToParent( '${form.requestUrl}', $('#${uiid}') );
						 	}
						 </c:set>
						
						<button type="submit" class="btn-grey ml1" onclick="${code}">Занять</button>
					</form>
				</c:otherwise>
			</c:choose>
		</div>	
	</c:if>	
</c:forEach>

<html:form action="/user/message" styleId="${uiid}" styleClass="in-mr1">
	<input type="hidden" name="action" value="messageList"/>

	<c:set var="unprocessedCountMap" value="${form.response.data.unprocessedCountMap}"/>
	<c:set var="script">openUrlContent( formUrl( $('#${uiid}') ) )</c:set>
	
	<ui:combo-single 
		hiddenName="typeId" value="${form.param.typeId}" 
		prefixText="Тип сообщения:" widthTextValue="50px"
		onSelect="${script}">
		<jsp:attribute name="valuesHtml">
			<li value="-1">-- все --</li>
			<c:forEach var="item" items="${typeMap}">
				<li value="${item.key}">
					${item.value.title}
					<c:set var="count" value="${unprocessedCountMap[item.key]}"/>
					<c:if test="${not empty count}">&nbsp;[${count}]</c:if>
				</li>
			</c:forEach>
		</jsp:attribute>
	</ui:combo-single>
	
	<ui:combo-single
		hiddenName="processed" value="${form.param.processed}"
		prefixText="Обработаны:" widthTextValue="20px"
		onSelect="${script}">
		<jsp:attribute name="valuesHtml">
			<li value="0">Нет</li>
			<li value="1">Да</li>
		</jsp:attribute>
	</ui:combo-single>
	
	<c:if test="${form.param['processed'] eq 1}">
        <ui:date-time type="ymd" paramName="dateFrom" value="${form.param.dateFrom}" placeholder="Дата от"/>
 		<ui:date-time type="ymd" paramName="dateTo" value="${form.param.dateTo}" placeholder="Дата по"/>
		<input type="text" name="from" value="${form.param.from}" placeholder="Отправитель"  />	
	</c:if>
	
	<ui:combo-single 
		hiddenName="order" value="${form.param.order}" prefixText="Сортировка:" 
		widthTextValue="20px" onSelect="${script}">
		<jsp:attribute name="valuesHtml">
			<li value="1">Обратная</li>
			<li value="0">Прямая</li>
		</jsp:attribute>
	</ui:combo-single>	
	
	<button type="button" class="btn-grey ml1" onclick="${script}">=></button>
	
	<c:if test="${form.param['processed'] eq 1}">
		<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
	</c:if>
</html:form>

<script>
	$(function()
	{	
		var $messageQueue = $('#content > #messageQueue');
		// т.к. каждый раз UIID разный - переопределение onShow
		$messageQueue.data('onShow',
			function()
			{
				if( $('#${uiid}').length > 0 )
				{
					${script}
				}
			});		
	});	
</script>

<%-- теперь это идентификатор таблицы --%>
<c:set var="uiid" value="${u:uiid()}"/>

<%@ include file="/WEB-INF/jspf/table_row_edit_mode.jsp"%>

<c:choose>
	<c:when test="${form.param['processed'] eq 1}">
		<table class="data mt1" style="width: 100%;" id="${uiid}">
			<tr>
				<td width="30">&nbsp;</td>
				<td width="30">ID</td>
				<td>Тип</td>
				<td>Тема</td>
				<td>От -&gt; На</td>
				<td>Время</td>				
				<td>Процесс</td>		
			</tr>
		
			<c:forEach var="item" items="${form.response.data.list}">
				<c:url var="url" value="message.do">
					<c:param name="id" value="${item.id}"/>
					<c:param name="returnUrl" value="${form.requestUrl}"/>					
				</c:url>
			
				<tr openUrl="${url}">
					<td>
						<button type="button" class="btn-white btn-small" title="Просмотр" onclick="openUrlContent( '${url}' )">*</button>
					</td>
					<td>${item.id}</td>
					<td>${config.typeMap[item.typeId].title}</td>
					<td>${item.subject}</td>
					<%@ include file="from_to.jsp"%>
					<td>${u:formatDate( item.fromTime, 'ymdhms' ) }</td>
					<td>${ctxProcessTypeMap[item.process.typeId].title}</td>
				</tr>
			</c:forEach>
		</table>
	</c:when>
	<c:otherwise>
	    <form action="message.do">
	        <input type="hidden" name="action" value="messageDelete"/>
	    	<table class="data mt1" style="width: 100%;" id="${uiid}">
				<tr>
					<td width="30">
					     <button type="button" class="btn-white btn-small" title="Удаление выбранных" 
					               onclick="if (confirm('Удалить выбранные?') && sendAJAXCommand(formUrl(this.form))) {
					                   ${script}
					               }">X</button>
                    </td>
					<td>Тип</td>
					<td>Тема</td>
					<td>От -&gt; На</td>
					<td>Время</td>
				</tr>
				<c:forEach var="item" items="${form.response.data.list}">
					<c:url var="url" value="message.do">
						<c:param name="typeId" value="${item.typeId}"/>
						<c:param name="messageId" value="${item.systemId}"/>
						<c:param name="returnUrl" value="${form.requestUrl}"/>
					</c:url>
				
					<tr valign="top" openCommand="openUrlContent('${url}')">
						<td style="text-align: center;">
							<input type="checkbox" name="typeId-systemId" value="${item.typeId}-${item.systemId}"/>
						</td>
						
						<c:set var="type" value="${config.typeMap[item.typeId]}"/>
						
						<td>${type.title}</td>
						<td>${item.subject}</td>
						<%@ include file="from_to.jsp"%>
						<td nowrap="nowrap">${u:formatDate( item.fromTime, 'ymdhms' ) }</td>
					</tr>
				</c:forEach>
			</table>
		</form>		
	</c:otherwise>
</c:choose>	

<c:set var="title" value="Сообщения"/>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>
<c:set var="help" value="http://www.bgcrm.ru/doc/3.0/manual/kernel/message.html"/>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>