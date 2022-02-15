<%@ page contentType="text/css; charset=UTF-8"%>

.btn-blue, .btn-grey, .btn-white, .btn-green, .btn-white-hover {
	padding: 0.6em 1em;
	text-align: center;
}

.btn-blue.icon, .btn-grey.icon, .btn-white.icon, .btn-green.icon, .btn-white-hover.icon,
.btn-blue.btn-icon, .btn-grey.btn-icon, .btn-white.btn-icon, .btn-green.btn-icon, .btn-white-hover.btn-icon {
	padding: 0.68em 0.8em;
}

.btn-small {
	padding: 0.4em 0.6em !important;
}

.btn-small.icon,
.btn-small.btn-icon {
	padding: 0.4em 0.4em !important;
}

.btn-slim {
	padding: 0.6em 0.6em;
}

.btn-blue {
	color: #fff;
	border-radius: 3px;
	border: 1px solid #0b67a1;
	box-shadow: 1px 1px 3px #c5c5c5;
	<%@ include file="/WEB-INF/jspf/css/button.common.jsp"%>
	<%@ include file="/WEB-INF/jspf/css/button.blue.gradient.jsp"%>
}

.btn-blue:hover {
	<%@ include file="/WEB-INF/jspf/css/button.blue.hover.gradient.jsp"%>
}

.btn-blue:active, .btn-blue:disabled {
	<%@ include file="/WEB-INF/jspf/css/button.blue.active.gradient.jsp"%>
}

.btn-green {
	color: #fff;
	border-radius: 3px;
	border: 1px solid #46a438;
	box-shadow: 1px 1px 3px #c5c5c5;
	<%@ include file="/WEB-INF/jspf/css/button.common.jsp"%>
	<%@ include file="/WEB-INF/jspf/css/button.green.gradient.jsp"%>
}

.btn-green:hover {
	<%@ include file="/WEB-INF/jspf/css/button.green.hover.gradient.jsp"%>
}

.btn-green:active, .btn-green:disabled {
	<%@ include file="/WEB-INF/jspf/css/button.green.active.gradient.jsp"%>
}

.btn-grey {
	color: #fff;
	border-radius: 3px;
	border: 1px solid #81838a;
	box-shadow: 1px 1px 3px #c5c5c5;
	<%@ include file="/WEB-INF/jspf/css/button.common.jsp"%>
	<%@ include file="/WEB-INF/jspf/css/button.grey.gradient.jsp"%>
}

.btn-grey:hover {
	<%@ include file="/WEB-INF/jspf/css/button.grey.hover.gradient.jsp"%>
}

.btn-grey:active, .btn-grey:disabled {
	<%@ include file="/WEB-INF/jspf/css/button.grey.active.gradient.jsp"%>
}

.btn-white {
	color: #000;
	border-radius: 3px;
	border: 1px solid #c8c8c8;
	<%@ include file="/WEB-INF/jspf/css/button.common.jsp"%>
	<%@ include file="/WEB-INF/jspf/css/button.white.gradient.jsp"%>
}

.btn-white:hover {
	<%@ include file="/WEB-INF/jspf/css/button.white.hover.gradient.jsp"%>
}

.btn-white:active, .btn-white:disabled {
	<%@ include file="/WEB-INF/jspf/css/button.white.active.gradient.jsp"%>
}

.btn-white-hover {
	border-radius: 3px;
	border: 1px solid transparent;
	background-color: transparent;
	<%@ include file="/WEB-INF/jspf/css/button.common.jsp"%>
}

.btn-white-hover:hover {
	border-color: #c8c8c8;
	<%@ include file="/WEB-INF/jspf/css/button.white.hover.gradient.jsp"%>
}

.btn-panel {
	display: inline-block;
	margin-right: 5px;
	max-width: 150px;
	text-overflow: ellipsis;
	white-space: nowrap;
	overflow: hidden;
}

<%-- not only button related  --%>
.progress-icon {
	display: inline-block;
	animation: spin 2s infinite linear;
}

@keyframes spin {
	0% { transform: rotate(0deg); }
	100% { transform: rotate(-359deg); }
}

button > .progress > .progress-icon {
	margin-right: 0.5em;
}
