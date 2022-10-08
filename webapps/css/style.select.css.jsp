<%@ page contentType="text/css; charset=UTF-8"%>

.select {
	border: 1px solid #c5c5c5;
	border-radius: 3px;
	box-shadow: 0px 3px 6px #e2e2e2 inset;
	display: -moz-inline-stack;
	display: inline-block;
	vertical-align: middle;
	*vertical-align: auto;
	*display: inline;
	position: relative;
}

.select input[type=text] {
	margin: 0;
	border: none;
	padding-right: 1.5em;
	width: 100%;
}

.select .icon {
	cursor: pointer;
	position: absolute;
	width: 1.5em;
	top: 0.7em;
	right: 0.15em;
	text-align: center;
}

.select:hover {
	border: 1px solid #5bc5ff;
}

.select:active {
	border: 1px solid #5bc5ff;
}

/* select-mult */

.select-mult .drop-list {
	width: 100%;
	background-color: #ffffff;
	display: block;
}

.select-mult ul.drop-list {
	border: 1px solid #d5d5d5;
	background-color: #ffffff;
	/* чтобы список значений не был шире редактируемой области сверху */
	box-sizing: border-box;
}

.select-mult .btn-add {
	font-size: 1.5em;
	padding: 0.18em 0.4em;
	margin-left: 0.2em;
}

.select-mult ul.drop-list >  li {
	border-top: 1px solid #d5d5d5;
	position: relative;
	padding: 0.5em;
	padding-left: 1.8em;
	color: #505050;
	cursor: pointer;
	white-space: nowrap;
}

.select-mult ul.drop-list.move-on>  li {
	padding-left: 2.8em;
}

.select-mult ul.drop-list >  li .delete {
	position: absolute;
	width: 12px;
	height: 11px;
	top: 0.6em;
	left: 0.5em;
}

.select-mult ul.drop-list > li .up,
.select-mult ul.drop-list > li .down {
	 display: none;
}

.select-mult ul.drop-list.move-on >  li .up {
	display: block;
	position: absolute;
	width: 12px;
	height: 10px;
	top: 0.1em;
	left: 1.6em;
}

.select-mult ul.drop-list.move-on >  li .down {
	display: block;
	position: absolute;
	width: 12px;
	height: 10px;
	bottom: 0.2em;
	left: 1.6em;
}

.select-mult ul.drop-list >  li span.title {
	display: block;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
}

.select-mult ul.drop-list >  li:first-child {
	border-top: none;
}

.select-mult ul.drop-list >  li:hover {
	background-color: #fafafa;
}