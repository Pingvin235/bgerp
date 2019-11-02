<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:choose>
	<c:when test="${mode eq 'items'}">
		<c:if test="${empty allowedForms or allowedForms.contains('fulltext-search')}">
			<li value="fulltext-search">Полнотекстовый поиск</li>
		</c:if>
	
	</c:when>
	<c:otherwise>
		<html:form action="/user/plugin/fulltext/search.do" method="GET" 
			styleId="searchForm-fulltext-search" styleClass="searchForm in-mb1 mt1 in-w100p">
			<html:hidden property="action" value="search"/>
						
			<c:set var="config" value="${u:getConfig(ctxSetup, 'ru.bgcrm.plugin.fulltext.model.Config')}"/>
			<ui:combo-single hiddenName="objectType" prefixText="Искать:" list="${config.objectTypeList}"/>
			
			<ui:input-text name="filter" placeholder="Строка поиска"
				onSelect="openUrl(formUrl(this.form), '#searchResult'); return false;"/>
		</html:form>		
	</c:otherwise>
</c:choose>