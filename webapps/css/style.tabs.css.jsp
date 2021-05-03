<%@ page contentType="text/css; charset=UTF-8"%>

.ui-tabs.ui-widget-content,
.ui-tabs .ui-widget-header,
.ui-tabs .ui-widget-content,
.ui-tabs .ui-state-active,
.ui-tabs .ui-state-default {
	background: none;
}

.ui-tabs.ui-widget-content,
.ui-tabs .ui-widget-content,
.ui-tabs .ui-state-active,
.ui-tabs .ui-state-default {
	background: none;
	border: none;
}

.ui-tabs .ui-widget-header {
	border-top: none;
	border-left: none;
	border-right: none;
}

.ui-tabs.ui-tabs-nav .ui-widget-header {
	border-bottom: 1px solid #CCCCCC;
}

.ui-tabs-nav {
	border-bottom: 1px solid #CCCCCC;
	margin-bottom: 0.5em;
}

.ui-tabs-nav li,
.ui-tabs-nav li.ui-state-default,
.ui-tabs-nav li.ui-state-hover,
.ui-tabs-nav li.ui-state-focus,
.ui-tabs-nav li.ui-state-active {
	background: none;
	display: inline-block;
	vertical-align: bottom;
	white-space: nowrap;
	padding: 0.5em 0.5em;
	background-color: #e5e5e5;
	border: 1px solid #cccccc;
	border-top: none;
	margin-bottom: -1px;
	margin-right: -1px;
}

.ui-tabs-nav li a,
.ui-tabs-nav li.ui-state-default a,
.ui-tabs-nav li.ui-state-hover a,
.ui-tabs-nav li.ui-state-focus a,
.ui-tabs-nav li.ui-state-active a {
	text-decoration: none;
	color: #1d1d1d;
}

.ui-tabs-nav li:last-child {
	border-radius: 0 3px 0 0;
}

.ui-tabs-nav li:first-child {
	border-radius: 3px 0 0 0;
}

.ui-tabs-nav li.ui-state-active {
	font-weight: bold;
	background-color: #fafafa;
	border-radius: 3px 3px 0 0;
	padding-top: 0.8em;
	border-top: 1px solid #cccccc;
	border-bottom: 1px solid #fafafa;
	font-size: 1em;
}

.ui-tabs-nav li.ui-state-active a {
	color: #303030;
}

button.refreshButton-hidden {
	display: none;
}

button.refreshButton {
	float: right;
	display: inline-block;
}
