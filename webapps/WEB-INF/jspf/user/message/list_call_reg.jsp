<%@page import="ru.bgcrm.util.Utils"%>
<%@page import="ru.bgcrm.struts.form.DynActionForm"%>
<%@page import="ru.bgcrm.dao.ParamValueDAO"%>
<%@page import="ru.bgcrm.util.sql.SQLUtils"%>
<%@page import="java.sql.Connection"%>
<%@page import="javax.sql.DataSource"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:forEach var="type" items="${config.typeMap.values()}">
	<c:if test="${type.getClass().getName() eq 'ru.bgcrm.dao.message.MessageTypeCall'}">
		<u:sc>
			<c:set var="reg" value="${type.getRegistrationByUser(form.userId)}"/>

			<c:set var="uiid" value="${u:uiid()}"/>
			<div class="mb1" id="${uiid}">
				<c:choose>
					<c:when test="${not empty reg}">
						<span class="tt">${type.title} номер <b>${reg.number}</b></span>
						<c:set var="url" value="/user/messageCall.do?typeId=${type.id}&action=numberFree"/>
						<button 
							type="button" class="btn-grey ml1" 
							onclick="$$.ajax.post('${url}').done(() => { $$.ajax.load('${form.requestUrl}', $('#${uiid}').parent()) })" >${l.l('Освободить')}</button>
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

							<button type="submit" class="btn-grey ml1" onclick="${code}">${l.l('Занять')}</button>
						</form>
					</c:otherwise>
				</c:choose>
			</div>
		</u:sc>
	</c:if>
</c:forEach>