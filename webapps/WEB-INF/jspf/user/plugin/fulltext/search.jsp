<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:choose>
	<c:when test="${mode eq 'items'}">
		<c:if test="${empty allowedForms or allowedForms.contains('fulltext')}">
			<li value="fulltext">${l.l('Полнотекстовый поиск')}</li>
		</c:if>

	</c:when>
	<c:otherwise>
		<html:form action="/user/plugin/fulltext/search.do" method="GET"
			styleId="searchForm-fulltext" styleClass="searchForm in-mb1 mt1 in-w100p">
			<html:hidden property="method" value="search"/>

			<c:set var="config" value="${ctxSetup.getConfig('ru.bgcrm.plugin.fulltext.model.Config')}"/>
			<ui:combo-single hiddenName="objectType" prefixText="${l.l('Искать')}:" list="${config.objectTypeList}"/>

			<ui:input-text name="filter" placeholder="${l.l('Строка поиска')}"
				onSelect="$$.ajax.load(this.form, '#searchResult');" />
		</html:form>
	</c:otherwise>
</c:choose>