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

<script src="/lib/treetable/jquery.treetable.js"></script>
<link type="text/css" href="/lib/treetable/jquery.treetable.css" rel="stylesheet"/>
<link type="text/css" href="/lib/treetable/jquery.treetable.theme.default.css" rel="stylesheet"/>

<script src="/lib/combobox/combo.js"></script>
<link type="text/css" href="/lib/combobox/combo.css" rel="stylesheet"/>

<script src="/lib/date-time-format.js"></script>

<script src="${u:fileNameWithLastModTime('/lib/preview/preview.js')}"></script>
<link type="text/css" href="${u:fileNameWithLastModTime('/lib/preview/style.css')}" rel="stylesheet"/>

<link type="text/css" href="${u:fileNameWithLastModTime('/lib/themify/themify-icons.css')}" rel="stylesheet"/>

<script src="${u:fileNameWithLastModTime('/js/kernel.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.ajax.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.customer.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.message.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.param.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.process.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.ui.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.shell.js')}"></script>

<link rel="icon" type="image/svg+xml" href="/img/favicon.svg" />

<%--
   МАГИЯ, чтобы input ы, баттоны и div ы одного стиля были одного размера 
   http://stackoverflow.com/questions/4483279/make-form-button-text-field-same-height-in-all-browsers
   http://necolas.github.io/normalize.css/
   чтобы div размер включал бордеры и т.п. 
--%>
<link type="text/css" href="/css/normalize.css" rel="stylesheet"/>

<%-- codemirror --%>
<link type="text/css" href="/lib/codemirror/lib/codemirror.css" rel ="stylesheet">
<link type="text/css" href="/lib/codemirror/bgerp.css" rel ="stylesheet">
<script src="/lib/codemirror/lib/codemirror.js"></script>
<script src="/lib/codemirror/mode/properties/properties.js"></script>
<script src="/lib/codemirror/addon/selection/active-line.js"></script>
<script src="/lib/codemirror/addon/edit/matchbrackets.js"></script>

<style type="text/css">
	<%@include file="/css/style.css.jsp"%>
</style>

<c:forEach items="${ctxPluginManager.pluginList}" var="plugin">
	<c:forEach items="${plugin.getEndpoints('js')}" var="js">
		<script src="${u:fileNameWithLastModTime(js)}"></script>
	</c:forEach>
</c:forEach>

<script>
	$(function () {
		<plugin:include endpoint="js.init"/>
	})
</script>

<%-- Disabled since custom exists, fails for open interface /open/test
   Для подключения своих CSS и JS создайте файл script_css.custom.jsp и используйте в нём конструкции как в этом файле:
    <script src="..."></script>
   <link type="text/css" href="..." rel="stylesheet"/>

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
--%>