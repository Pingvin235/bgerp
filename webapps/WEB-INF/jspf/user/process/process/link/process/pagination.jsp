<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Incoming variables:
	mode - required, 'link' or 'linked'
	linkFormUiid - required, for reloading
	linkAddUiid - when add link form should be shown
--%>

<u:sc>
	<html:form action="${form.httpRequestURI}" styleId="${linkFormUiid}">
		<html:hidden property="action"/>
		<html:hidden property="id"/>

		<div style="display: inline-block;" class="mt1">
			<h2 style="display: inline;">
				<c:choose>
					<c:when test="${mode eq 'link'}">${l.l('Children')}</c:when>
					<c:otherwise>${l.l('Parents')}</c:otherwise>
				</c:choose>
			</h2>
			<c:if test="${not empty linkAddUiid}">
				[<a href="#" onclick="$('#${linkAddUiid}').toggle(); $('#${linkFormUiid}').toggle(); return false;">${l.l('add')}</a>]
			</c:if>
		</div>

		<ui:page-control nextCommand="; $$.ajax.load(this.form, $('#${linkFormUiid}').parent())"/>
	</html:form>
</u:sc>