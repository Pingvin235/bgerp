<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<script src="/lib/jquery-3.3.1.js"></script>
<%-- u:fileNameWithLastModTime has to be used for all of the modified JS and CSS --%>
<script src="${u:fileNameWithLastModTime('/lib/jquery-ui-1.12.1/jquery-ui.js')}"></script>
<link type="text/css" href="/lib/jquery-ui-1.12.1/jquery-ui-start.css" rel="stylesheet"/>

<script src="/lib/inputmask/jquery.inputmask.js"></script>
<script src="/lib/inputmask/jquery.inputmask.numeric.extensions.js"></script>
<script src="/lib/inputmask/jquery.inputmask.custom.extensions.js"></script>
<script src="/lib/inputmask/jquery.inputmask.date.extensions.js"></script>
<script src="/lib/inputmask/jquery.inputmask.extensions.js"></script>

<script src="/lib/timepicker-1.3/jquery-ui-timepicker.js"></script>
<link type="text/css" href="/lib/timepicker-1.3/jquery-ui-timepicker.css" rel="stylesheet"/>

<script src="/lib/jquery.serializeanything.js"></script>
<script src="/lib/jquery.base64.js"></script>

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

<script src="/lib/tagator/fm.tagator.jquery.js"></script>
<link type="text/css" href="/lib/tagator/fm.tagator.jquery.css" rel="stylesheet"/>

<script src="/lib/combobox/combo.js"></script>
<link type="text/css" href="/lib/combobox/combo.css" rel="stylesheet"/>

<script src="/lib/date-time-format.js"></script>

<script src="${u:fileNameWithLastModTime('/lib/preview/preview.js')}"></script>
<link type="text/css" href="${u:fileNameWithLastModTime('/lib/preview/style.css')}" rel="stylesheet"/>

<link type="text/css" href="${u:fileNameWithLastModTime('/lib/themify/themify-icons.css')}" rel="stylesheet"/>

<script src="${u:fileNameWithLastModTime('/js/kernel.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.ajax.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.customer.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.license.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.lock.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.message.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.news.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.param.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.param.address.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.process.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.search.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.table.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.timer.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.ui.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.ui.select.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.ui.tree.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.ui.upload.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.shell.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.shell.login.js')}"></script>
<script src="${u:fileNameWithLastModTime('/js/kernel.shell.message.js')}"></script>

<link rel="icon" type="image/svg+xml" href="/img/favicon.svg" />

<%--
	CSS reset.
	https://dev.to/vkton115/reset-css-normalizecss-320m
	http://necolas.github.io/normalize.css/
--%>
<link type="text/css" href="${u:fileNameWithLastModTime('/css/normalize.css')}" rel="stylesheet"/>
<link type="text/css" href="${u:fileNameWithLastModTime('/css/normalize-correct.css')}" rel="stylesheet"/>


<%-- codemirror --%>
<link type="text/css" href="/lib/codemirror-5.65.10/lib/codemirror.css" rel ="stylesheet">
<link type="text/css" href="/lib/codemirror-5.65.10/bgerp.css" rel ="stylesheet">
<script src="/lib/codemirror-5.65.10/lib/codemirror.js"></script>
<script src="/lib/codemirror-5.65.10/mode/properties/properties.js"></script>
<script src="/lib/codemirror-5.65.10/mode/sql/sql.js"></script>
<script src="/lib/codemirror-5.65.10/addon/selection/active-line.js"></script>
<script src="/lib/codemirror-5.65.10/addon/edit/matchbrackets.js"></script>

<link type="text/css" href="${u:fileNameWithLastModTime('/css/style.css.jsp')}" rel="stylesheet"/>

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
