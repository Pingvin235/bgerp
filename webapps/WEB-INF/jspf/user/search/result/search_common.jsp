<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<button class="btn-green icon" onclick="$$.customer.createAndEdit({disabled: this});" title="${l.l('Создать нового контрагента')}"><i class="ti-plus"></i></button>
<ui:page-control pageFormId="searchForm-customer" nextCommand="; $$.ajax.load($('#searchForm-customer'), '#searchResult');" />
