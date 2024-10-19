<%@ page contentType="text/css; charset=UTF-8"%>

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
	color: var(--p-color);
	border: 1px solid var(--table-border-color);
}

table.hdata > thead > tr > td,
table.hdata > tbody > tr > th,
table.hdata > tbody > tr > td.header,
table.hdata > tbody > tr.header > td,
table.data > tbody > tr:nth-child(1) > td {
	color: var(--table-head-txt-color);
	border: 1px solid var(--table-border-color);
	background-color: var(--table-head-bg-color);
}

table.hdata.hl > tbody > tr:hover,
table.data.hl tr:hover,
table tr.hl {
	background-color: var(--table-hl-bg-color);
}

table.fixed-header tr:nth-child(1) td {
	position: sticky;
	top: 0;
}

.border-table {
	border: 1px solid var(--table-border-color);
}

td.min {
	width: 1%;
	white-space: nowrap;
}

table.data > tbody > tr > td.group-border-b {
	border-bottom-width: 2px;
	border-bottom-color: var(--table-cell-group-color);
}

table.data > tbody > tr > td.group-border-t {
	border-top-width: 2px;
	border-top-color: var(--table-cell-group-color);
}

table.data > tbody > tr > td.group-border-l {
	border-left-width: 2px;
	border-left-color: var(--table-cell-group-color);
}

table.data > tbody > tr > td.group-border-r {
	border-right-width: 2px;
	border-right-color: var(--table-cell-group-color);
}