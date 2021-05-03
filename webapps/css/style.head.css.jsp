<%@ page contentType="text/css; charset=UTF-8"%>

#headWrap {
	border: 1px solid #c5c5c5;
	background-image: -webkit-gradient(linear, 50% 0%, 50% 100%, color-stop(0%, #fcfcfc), color-stop(33.33333%, #f8f8f8), color-stop(66.66667%, #ededed), color-stop(100%, #e9e9e9));
	background-image: -webkit-linear-gradient(top, #fcfcfc, #f8f8f8, #ededed, #e9e9e9);
	background-image: -moz-linear-gradient(top, #fcfcfc, #f8f8f8, #ededed, #e9e9e9);
	background-image: -o-linear-gradient(top, #fcfcfc, #f8f8f8, #ededed, #e9e9e9);
	background-image: linear-gradient(top, #fcfcfc, #f8f8f8, #ededed, #e9e9e9);
	margin-bottom: 1em;
	padding: 0.6em 2em;
}

#head {
	display: table-row;
}

#head .btn-start {
	margin-right: 0.5em;
}

#head .btn-task,
#head .btn-task-active {
	margin-left: 0.5em;
}

#head .btn-start {
	font-size: 1.1em;
	font-weight: bold;
	border-radius: 5px;
}

#head .btn-task-active {
	font-weight: bold;
	border-radius: 5px;
}

#head .btn-task .title, .btn-task-active .title {
	text-align: left;
	overflow: hidden;
	white-space: nowrap;
	text-overflow: ellipsis;
}

#head .btn-task span,
#head .btn-task-active span {
	display: table-cell;
}

#head .btn-task .title {
	max-width: 5em;
}

#head .btn-task-active .title {
	font-size: 1.1em;
	max-width: 8em;
}

#head > .right {
	font-size: 1.1em;
	white-space: nowrap;
}

#head .icon-close {
	padding-left: 0.5em;
}

#head > .right a {
	text-decoration: none !important;
	border-bottom: 1px dashed;
	font-weight: bold;
	color: #005589;
}

#head >	* {
	display: table-cell;
}

#head > #taskPanelWrap {
	width: 100%;
}

#head #taskPanel {
	white-space: nowrap;
	overflow-x: auto;
	display: block;
}