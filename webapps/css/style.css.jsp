<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

/* Welcome to Compass.
 * In this file you should write your main styles. (or centralize your imports)
 * Import this file using the following HTML or equivalent:
 * <link href="/stylesheets/screen.css" media="screen, projection" rel="stylesheet" type="text/css" /> 
 */
html, body, div, убрал, <%-- т.к. span съезжал помещённый в строку span, --%> applet, object, iframe,
h1, h2, h3, h4, h5, h6, p, blockquote, pre,
abbr, acronym, address, big, cite, code,
del, dfn, em, img, ins, kbd, q, s, samp,
small, strike, strong, sub, sup, tt, var,
center,
dl, dt, dd, ol, ul, li,
fieldset, form, label, legend,
table, caption, tbody, tfoot, thead, tr, th, <%-- иначе valign='top' не работает td,  --%>
article, aside, canvas, details, embed,
figure, figcaption, footer, header, hgroup,
menu, nav, output, ruby, section, summary,
time, mark, audio, video {
	margin: 0;
	padding: 0;
	border: 0;
	font: inherit;
	font-size: 100%;
	vertical-align: middle;
}

ol, ul {
	list-style: none;
}


html {
	overflow-y: scroll;
}

a {
	color: #005589;
}

@media all
{ 
	body {
		font: 13px  Arial, Geneva CY, Kalimati, Geneva, sans-serif;
		color: black;
	}
}

@media print
{
	#head, 
	#processQueueFilter,
	table.page,
	.printHide {
		display: none;
	}
}

<%@ include file="style.head.css.jsp"%>

<%@ include file="style.title.css.jsp"%>

body > #content {
	padding: 0px 2em;
	margin-bottom: 2em;
}

body > #content h1 {
	color: #005589;
	font-weight: bold;
	margin: 1em 0 0.5em 0;
	font-size: 1.1em;
}

body > #content h2 {
	color: #353535;
	font-weight: bold;
	margin: 1em 0 0.5em 0;
	font-size: 1.1em;
}

body > #content p {
	color: #505050;
	margin: 0.3em 0;
}

body > #content label {
	margin-left: 0.5em;
}

.separator {
	margin: 0.7em 0;
	border-bottom: 1px solid #e7e7e7;
}

.hint {
	display: block;
	font-size: 0.8em;
	font-style: italic;
	color: #707070;
	padding: 0.5em 0;
}

/* Title Text */
.tt {
	color: #353535;	
	font-size: 1.1em;
}

.red {
	font-size: 1.3em;
	color: #ff0000;
	margin: 0 0.3em;
}

.bold {
	 font-size: 1.1em;
	 font-weight: bold;
}   

.center1020 {
	margin: 0 auto;
	width: 1020px;
}

.center500 {
	margin: 0 auto;
	width: 500px;
}

.text-center {
	text-align: center;
}

.nowrap {
	white-space: nowrap;
}

.in-table-cell > * {
	display: table-cell;
}

.in-table-row > * {
	display: table-row;
}

.in-inline-block > * {
	display: inline-block;
}	

.in-va-top > * {
	vertical-align: top;
}

.in-va-middle > * {
	vertical-align: middle;
}

.in-nowrap > * {
	white-space: nowrap;
}

<%@ include file="style.indent.css.jsp"%>
<%@ include file="style.tabs.css.jsp"%>
<%@ include file="style.table.css.jsp"%>
<%@ include file="style.input.css.jsp"%>
<%@ include file="style.button.css.jsp"%>
<%@ include file="style.combo.css.jsp"%>
<%@ include file="style.select.css.jsp"%>
<%@ include file="style.icon.css.jsp"%>
<%@ include file="style.menu.css.jsp"%>
<%@ include file="style.date.css.jsp"%>
<%@ include file="style.callboard.css.jsp"%>

<%-- приведение JQueryUI стилей к оформлению BGERP --%>
<%@ include file="ui-correct.css.jsp"%>

div, span {
	-moz-box-sizing: border-box;
	-webkit-box-sizing: border-box;
	-box-sizing: border-box; 
}

/* в адресном справочнике в одном месте используется */
.sel {
	font-weight: bold;
}

.box { 
	border-radius: 3px; 
	border: 1px solid #C5C5C5;
	-moz-box-sizing: border-box; 
}

<c:set var="endpoint" value="css.jsp"/>
<%@ include file="/WEB-INF/jspf/plugin_include.jsp"%>