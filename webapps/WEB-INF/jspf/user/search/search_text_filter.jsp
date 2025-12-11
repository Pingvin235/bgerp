<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div style="display: flex;">
	<ui:combo-single name="textLikeMode" style="width: 10em;">
		<jsp:attribute name="valuesHtml">
			<li value="sub">${l.l('search.sub')}</li>
			<li value="eq">${l.l('search.eq')}</li>
			<li value="start">${l.l('search.prefix')}</li>
			<li value="end">${l.l('search.suffix')}</li>
		</jsp:attribute>
	</ui:combo-single>

	<ui:input-text
		name="text" placeholder="${l.l('Value')}" title="${l.l('To search input a string and press Enter')}"
		onSelect="this.form.searchBy.value='text'; $$.ajax.load(this.form, '#searchResult')" styleClass="ml1"/>
</div>