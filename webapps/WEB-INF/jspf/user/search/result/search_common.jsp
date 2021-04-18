<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="pageFormId" value="searchForm-customer" scope="request"/>
<c:set var="nextCommand" value="; $$.ajax.load($('#searchForm-customer'), '#searchResult');" scope="request"/>
<button class="btn-green icon" onclick="$$.customer.createAndEdit({disabled: this});" title="${l.l('Создать нового контрагента')}"><i class="ti-plus"></i></button>
<%@ include file="/WEB-INF/jspf/page_control.jsp"%>