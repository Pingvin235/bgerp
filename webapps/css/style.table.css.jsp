<%@ page contentType="text/css; charset=UTF-8"%>

<c:set var="UI_TABLE_BORDER_COLOR" value="#cadee9"/>
<c:set var="UI_TABLE_HEAD_BACKGROUND_COLOR" value="#eaf3f9"/>

table {
	border-collapse: collapse;
	border-spacing: 0;
}

table.hdata,
table.data {
	width: 100%;
}

table.hdata > thead > tr > td,
table.hdata > tbody > tr > td,
table.data > tbody > tr > td {
	text-align: left;
	font-weight: normal;
}

table.hdata > thead > tr > td,
table.hdata > tbody > tr > td,
table.data > tbody > tr > td {
	padding: 0.5em 1em;
}

table.hdata > thead > tr > td,
table.hdata > tbody > tr > td,
table.data > tbody > tr > td {
	color: #505050;
	border: 1px solid ${UI_TABLE_BORDER_COLOR};
}

table.hdata > thead > tr > td,
table.hdata > tbody > tr > th,
table.hdata > tbody > tr > td.header,
table.hdata > tbody > tr.header > td,
table.data > tbody > tr:nth-child(1) > td {
	color: #346484;
	border: 1px solid ${UI_TABLE_BORDER_COLOR};
	background-color: #eaf3f9;
}

table.hdata.hl > tbody > tr:hover,
table.data.hl tr:hover,
table tr.hl {
	background-color: #A9F5F2;
}

table.fixed-header tr:nth-child(1) td {
	position: sticky;
	top: 0;
}

.border-table {
	border: 1px solid ${UI_TABLE_BORDER_COLOR};
}

td.min {
	width: 1%;
	white-space: nowrap;
}

table.data > tbody > tr > td.group-border-b {
	border-bottom-width: 2px;
	border-bottom-color: black;
}

table.data > tbody > tr > td.group-border-t {
	border-top-width: 2px;
	border-top-color: black;
}

table.data > tbody > tr > td.group-border-l {
	border-left-width: 2px;
	border-left-color: black;
}

table.data > tbody > tr > td.group-border-r {
	border-right-width: 2px;
	border-right-color: black;
}