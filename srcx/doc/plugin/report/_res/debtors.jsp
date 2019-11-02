<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.ArrayList"%>
<%@page import="ru.bgcrm.model.IdTitle"%>
<%@page import="java.util.List"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div >
	<h2>Отчет по должникам</h2>
	
	<%-- Переменная form - объект класса ru.bgcrm.struts.form.DynActionForm, содержащий параметры запроса. --%>
	<c:set var="groupsF" value="${form.getSelectedValues('selectGroups')}" />
	<c:set var="statusesF" value="${form.getSelectedValues('selectStatuses')}" />
	<c:set var="streetF" value="${form.getParamInt('selectStreet')}" />
	<c:set var="homeF" value="${form.getParam('home')}" />
	<c:set var="balanceFromF" value="${form.getParam('balanceFrom')}" />
	<c:set var="balanceToF" value="${form.getParam('balanceTo')}" />
	<c:set var="sortF" value="${form.getSelectedValuesList('sortParam')}" />
	
	<!-- Дин код реализации отчета -->
	<u:newInstance var="data" clazz="ru.bgcrm.dyn.ReportCustomDebtors"/>
	
	<c:set var="dbInfo" value="${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[data.getBillingId()]}"/>
	
	<html:form action="/user/empty">
		<input type="hidden" name="forwardFile" value="${form.forwardFile}"/>
		
		<table>
            <tr>
                <td valign="top">
                		Группы
					<ui:select-mult showId="false" hiddenName="selectGroups" style="width: 200px;" list="${data.getGroups()}" values="${groupsF}" />
				</td>
                <td valign="top">
                		Статусы
                		<ui:select-mult showId="false" hiddenName="selectStatuses" style="width: 180px;" list="${data.getStatus()}" values="${statusesF}" />
                </td>
                	<td valign="top">
                		Улица<br/>
					<ui:select-single hiddenName="selectStreet"  style="width: 250px;" list="${data.getStreetList()}" value="${streetF}"/>
                	</td>
                	<td valign="top">
                		Дом<br/>
            		<ui:input-text name="home" value="${homeF}" />
                	</td>
                	<td valign="top">
                		Баланс ОТ<br/>
                		<ui:input-text name="balanceFrom"  value="${balanceFromF}" />
                	</td>
                	<td valign="top">
                		Баланс ДО<br/>
                		<ui:input-text name="balanceTo"  value="${balanceToF}" />
                	</td>
                <td valign="top">
                		Сортировка<br/>
               		<u:sc>
						<%
							/* По дефолту баланс */
							List<Integer> values = (List<Integer>)pageContext.getAttribute( "sortF" );
							if( values == null || values.isEmpty() )
							{
							    values = new ArrayList<Integer>();
								values.add( 4 );				
								pageContext.setAttribute( "sortF", values );							    
							}
						%>	
						<ui:select-mult hiddenName="sortParam" style="width: 150px;" moveOn="true" list="${data.getSortList()}" map="${data.getSortMap()}" values="${sortF}"/>
					</u:sc>
                </td>	
                	
            </tr>
	    </table>

		<button type="button"  class="btn-grey ml1 mt05" onclick="openUrlToParent( formUrl( this.form ), $(this.form) )">Сформировать</button>
	</html:form>
	
	<%-- Генерация отчёта, если в запросе пришёл параметр группа. --%>
	<c:if test="${not empty groupsF}">
		
	    <table style="width: 100%;" class="data mt1">
            <tr>
            	<td width="30">№</td>
                <td width="70">Id</td>
                <td>Договор</td>
                <td>ФИО</td>
                <td>Адрес подключения</td>
                <td width="100">Баланс</td>
            </tr>   
		    <c:forEach var="debtor" items="${data.getDebters(groupsF,statusesF,streetF,homeF,balanceFromF,balanceToF,sortF)}">
			    <tr>
	                <td>${debtor[0]}</td>
	                <td><a href="#UNDEF" onclick="bgbilling_openContract( '${data.getBillingId()}', '${debtor[1]}' ); return false;">${debtor[1]}</a></td>
	                <td>${debtor[2]}</td>
	                <td>${debtor[3]}</td>
	                <td>${debtor[4]}</td>
	                <td>${debtor[5]}</td>
	            </tr>
		    </c:forEach>
	    </table>
	    
	</c:if>
</div>