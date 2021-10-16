<%@ page contentType="text/css; charset=UTF-8"%>

#title {
	padding: 0 2em;
	margin-bottom: 1em;
}

@media print {
	#title {
		display: none;
	}
}

#title h1 {
	color: #005589;
	font-size: 2em;
}

#title > div {
	display: table-cell;
	vertical-align: middle;
}

#title > .status {
	width: 100%;
}

#title > .status > .wrap {
	display: table;
	width: 100%;
}

#title > .status > .wrap > * {
	display: table-cell;
}

#title > .status > .wrap > .center {
	width: 100%;
	text-align: center;
}

#title > .status > .wrap > .center > .state {
	display: inline;
}

#title > .status > .wrap > .left > .title {
	display: inline-block;
	padding-right: 2em;
	position: relative;
	white-space: nowrap;
}

#title > .status > .wrap > .left > .title > h1.title {
	display: inline-block;
	white-space: nowrap;
	max-width: 600px;
	overflow: hidden;
	text-overflow: ellipsis;
}

#title #objectBuffer li > .icon-close {
	position: absolute;
	right: 0.7em;
	top: 0.6em;
	width: 12px;
	height: 11px;
	cursor: pointer;
}

#title > .status > .wrap > .left > .title > h1.title {
	cursor: pointer;
}

#title #objectBuffer {
	width: 200px;
}

#title #objectBuffer li span {
	white-space: nowrap;
}

#title #objectBuffer div.text-pref, #title #objectBuffer div.text-pref span, #title #objectBuffer li span.title {
	color: #005589;
	font-size: 1.1em;
	font-weight: bold;
	white-space: nowrap;
}

#title #objectBuffer div.text-value {
	padding: 0;
	padding-left: 0.5em;
}

#title #objectBuffer.combo ul.drop {
	width: 600px;
	left: auto;
	right: -1px;
}

#title #objectBuffer.combo ul.drop li {
	position: relative;
}