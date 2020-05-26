<%@ page contentType="text/html; charset=UTF-8"%>

<c:set var="UI_TABLE_BORDER_COLOR" value="#cadee9"/>
<c:set var="UI_TABLE_HEAD_BACKGROUND_COLOR" value="#eaf3f9"/>

table {
	border-collapse: collapse;
	border-spacing: 0;
}

table.data, table.hdata {
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

.border-table {
	border: 1px solid ${UI_TABLE_BORDER_COLOR};
}
