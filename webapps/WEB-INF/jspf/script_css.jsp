<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<script src="/lib/jquery-3.3.1.js"></script>
<%-- u:fileNameWithLastModTime has to be used for all of the modified JS and CSS --%>
<script src="${u:fileNameWithLastModTime('/lib/jquery-ui-1.12.1/jquery-ui.js')}"></script>
<script src="/lib/jquery-ui-1.12.1/i18n/jquery.ui.datepicker-ru.js"></script>
<link type="text/css" href="/lib/jquery-ui-1.12.1/jquery-ui-start.css" rel="stylesheet"/> 

<script src="/lib/inputmask/jquery.inputmask.js"></script>
<script src="/lib/inputmask/jquery.inputmask.numeric.extensions.js"></script>
<script src="/lib/inputmask/jquery.inputmask.custom.extensions.js"></script>
<script src="/lib/inputmask/jquery.inputmask.date.extensions.js"></script>
<script src="/lib/inputmask/jquery.inputmask.extensions.js"></script>

<script src="/lib/timepicker-1.3/jquery-ui-timepicker.js"></script>
<script src="/lib/timepicker-1.3/i18n/jquery.ui.timepicker-ru.js"></script>
<link type="text/css" href="/lib/timepicker-1.3/jquery-ui-timepicker.css" rel="stylesheet"/>

<script src="/lib/jquery.serializeanything.js"></script>
<script src="/lib/jquery.base64.js"></script>

<script src="${u:fileNameWithLastModTime('/lib/jquery.iframe-post-form.js')}"></script>

<script src="/lib/tree/jquery.tree.js"></script>
<link type="text/css" href="/lib/tree/jquery.tree.css" rel="stylesheet"/>

<script src="/lib/colorpicker/colorpicker.js"></script>
<link type="text/css" href="/lib/colorpicker/colorpicker.css" rel="stylesheet"/>

<script src="/lib/sprintf.js"></script>

<script src="/lib/context_menu/context.js"></script>
<link type="text/css" href="/lib/context_menu/context.bootstrap.css" rel="stylesheet"/>
<link type="text/css" href="/lib/context_menu/context.standalone.css" rel="stylesheet"/>

<script src="${u:fileNameWithLastModTime('/lib/ctable/ctable.js')}"></script>
<link type="text/css" href="${u:fileNameWithLastModTime('/lib/ctable/ctable.css')}" rel="stylesheet"/>

<%-- remove later together with files
<script src="/lib/treetable/jquery.treetable.js"></script>
<link type="text/css" href="/lib/treetable/jquery.treetable.css" rel="stylesheet"/>
<link type="text/css" href="/lib/treetable/jquery.treetable.theme.default.css" rel="stylesheet"/>

<script src="/lib/jquery.autoresize.js"></script>

<script src="/lib/jquery.selectboxes.js"></script>
<script src="/lib/jquery.appear.js"></script>
<script src="/lib/jquery.resize.js"></script>

<script src="/lib/combobox/combo.js"></script>
<link type="text/css" href="/lib/combobox/combo.css" rel="stylesheet"/>
--%>

<script src="/lib/date-time-format.js"></script>

<script src="${u:fileNameWithLastModTime('/lib/preview/preview.js')}"></script>
<link type="text/css" href="${u:fileNameWithLastModTime('/lib/preview/style.css')}" rel="stylesheet"/>

<link type="text/css" href="/lib/themify/themify-icons.css" rel="stylesheet"/>

<script src="${u:fileNameWithLastModTime('/js/crm.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/crm.address.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/crm.ajax.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/crm.callboard.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/crm.customer.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/crm.process.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/crm.ui.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/crm.shell.js')}"></script>

<link rel="icon" type="image/png" href="/img/favicon.png" />

<%--
   МАГИЯ, чтобы input ы, баттоны и div ы одного стиля были одного размера 
   http://stackoverflow.com/questions/4483279/make-form-button-text-field-same-height-in-all-browsers
   http://necolas.github.io/normalize.css/
   чтобы div размер включал бордеры и т.п. 
--%>
<link type="text/css" href="/css/normalize.css" rel="stylesheet"/>


<style type="text/css">
	<%@include file="/css/style.css.jsp"%>
</style>


<c:forEach items="${ctxPluginManager.pluginList}" var="plugin">
	<c:set var="js" value="${plugin.endpoints['js']}" scope="request"/>
	<c:if test="${not empty js}">
		<script src="${u:fileNameWithLastModTime(js)}"></script>		
	</c:if>
</c:forEach>

<script>
	$(function()
	{
		<c:set var="endpoint" value="js.init"/>
		<%@ include file="/WEB-INF/jspf/plugin_include.jsp"%>
	});
</script>

<%--
   Для подключения своих CSS и JS создайте файл script_css.custom.jsp и используйте в нём конструкции как в этом файле:
    <script src="..."></script>
   <link type="text/css" href="..." rel="stylesheet"/>   	
--%>
<%
	String realPath = getServletContext().getRealPath( "/WEB-INF/jspf/script_css.custom.jsp" );
	if( new java.io.File( realPath ).exists() )
	{
		pageContext.setAttribute( "includeCustom", true );	
	}
%>

<c:if test="${not empty includeCustom}">
	<jsp:include page="/WEB-INF/jspf/script_css.custom.jsp"/>
</c:if>  