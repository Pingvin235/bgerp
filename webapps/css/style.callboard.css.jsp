<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

/* таблица с графиком дежурств */
#work-callboard .callboard table.minimal td {
	 padding: 0.2em;
	 min-width: 30px;
	 max-width: 30px;
	 width: 30px;

	 user-select: none;
	 -moz-user-select: none;
	 -webkit-user-select: none;
}

#work-callboard .callboard table.minimal .sum {
	background: #F2C777;
	font-weight: bold;
}

#work-callboard .callboard table.minimal td.shiftDateHeader {
	text-align: center;
}

#work-callboard .callboard table.minimal td.shiftUser {
	padding: 0.2em 0.5em;
	min-width: 200px;
	max-width: 200px;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
  	cursor: pointer;
}

#work-callboard .callboard table.minimal .groupHeader td {
	padding-top: 0.5em;
	padding-bottom: 0.5em;
}

#work-callboard .callboard table.minimal td.dayShift {
	cursor: pointer;
}

/* выбор смен */
#work-callboard #editor #shiftArea {
	width: 100%;
	overflow: hidden;
}

#work-callboard #editor #shiftArea .wrap {
	height: 100%;
	white-space: nowrap;
	overflow-x: auto;
}

#work-callboard #editor #shiftArea .wrap .shift {
	white-space: normal;
	display: inline-block;
	height: 100%;
	width: 100px;
	cursor: pointer;
	padding: 0.2em;
}

#work-callboard #editor #shiftArea .wrap .shift :first {
	vertical-align: middle;
	text-align: center;
}

#work-callboard #editor #shiftArea .wrap .shift .text,
#work-callboard #editor #shiftArea .wrap .shift .color {
	overflow: hidden;
	text-overflow: ellipsis;
}

#work-callboard #editor #shiftArea .wrap .shift .text {
	font-size: 0.8em;
	height: 60%;
}

#work-callboard #editor #shiftArea .wrap .shift .color {
	padding-left: 0.5em;
	padding-top: 0.2em;
	vertical-align: middle;
	height: 40%;
}

#work-callboard #editor #shiftArea .wrap .shift {
 	border-style: solid;
	border-width: 2px;
	border-color: white;
}

#work-callboard #editor #shiftArea .wrap .shift.selected {
	border-color: red;
}

/* таблица с графиком дежурств */
#workPlan .plan table.plan td {
	padding: 0.5em;
}

/*
#workPlan .plan table.plan td.shiftUser,
#workPlan .plan table.plan td.timeHead {
	padding: 0.5em;
}
*/

#workPlan .plan table.plan tr.user {
	height: 3em;
}

#workPlan .plan table.plan > tbody > tr > td {
	 min-width: 50px;
	 max-width: 50px;
	 width: 50px;

	 user-select: none;
	 -moz-user-select: none;
	 -webkit-user-select: none;
}

#workPlan .plan table.plan > tbody > tr > td.shiftUser {
	min-width: 200px;
	max-width: 200px;

	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
}

#workPlan .plan table.plan > tbody > tr > td.cell {
	padding: 0em;
}

#workPlan .plan table.workTypeTime > tbody > tr > td:not(:last-child) {
	border-right: 1px solid ${UI_TABLE_BORDER_COLOR};
}

#workPlan .plan table.workTypeTime > tbody > tr > td {
  	padding: 0.5em;
  	font-size: 0.8em;
  	cursor: pointer;
  	overflow: hidden;
	white-space: nowrap;
	text-overflow: ellipsis;
}

#workPlan .plan table.workTypeTime > tbody > tr > td.lock {
  	font-size: 1.2em;
  	text-align: center;
}


td.shiftUser > b {
	cursor: pointer;
}

.contextMenu {

	position: absolute;
	width: 300px;
	z-index: 999;
	background: white;
	text-align: center;
}

#sortMenu ul.drop {
	border: 0px none;
	overflow: hidden;
}

#addGroupToUserPopup {
	text-align: center;
}

#addGroupToUserPopup div {
	margin-top: 10px;
}

#addGroupToUserPopup input {
	width: 80px;

}

.available-day {
	background-color: #FFFFFF;
}

.nonavailable-day {
	background-color: #CCCCCC;
}

.selected-cel {
	background-color: #EDF551 !important;
}

.contextMenu {

	position: absolute;
	width: 300px;
	z-index: 999;
	background: white;
	text-align: center;
}

#sortMenu ul.drop {
	border: 0px none;
}

#addGroupToUserPopup {
	text-align: center;
}

#addGroupToUserPopup div {
	margin-top: 10px;
}

#addGroupToUserPopup input {
	width: 80px;

}

.available-day {
	background-color: #FFFFFF;
}

.nonavailable-day {
	background-color: #CCCCCC;
}
<%--
a:focus {
	outline: none;
}

.slide {
	margin: 0;
	padding: 0;
	border-top: solid 4px #FFFFFF;
}

.active {
	background-position: right 12px;
}

table.callboard a:link {
	color: #666;
	font-weight: bold;
	text-decoration: none;
}

table.callboard a:visited {
	color: #999999;
	font-weight: bold;
	text-decoration: none;
}

table.callboard a:active,table.callboard a:hover {
	color: #bd5a35;
	text-decoration: underline;
}

table.callboard {
	font-family: Arial, Helvetica, sans-serif;
	color: #666;
	font-size: 14px;
	text-shadow: 1px 1px 0px #fff;
	background: #eaebec;
	margin: 5px 20px 0px;
	border: #ccc 1px solid;
	display: block;
	margin-bottom: 0px; '
	-moz-border-radius: 3px;
	-webkit-border-radius: 3px;
	border-radius: 3px;
	-moz-box-shadow: 0 1px 2px #d1d1d1;
	-webkit-box-shadow: 0 1px 2px #d1d1d1;
	box-shadow: 0 1px 2px #d1d1d1;
}

table.callboard th {
	padding: 21px 25px 22px 25px;
	border-top: 1px solid #fafafa;
	border-bottom: 1px solid #e0e0e0;
	background: #ededed;
	background: -webkit-gradient(linear, left top, left bottom, from(#ededed),
		to(#ebebeb));
	background: -moz-linear-gradient(top, #ededed, #ebebeb);
}

table.callboard th:first-child {
	text-align: left;
	padding-left: 20px;
}

table.callboard tr:first-child th:first-child {
	-moz-border-radius-topleft: 3px;
	-webkit-border-top-left-radius: 3px;
	border-top-left-radius: 3px;
}

table.callboard tr:first-child th:last-child {
	-moz-border-radius-topright: 3px;
	-webkit-border-top-right-radius: 3px;
	border-top-right-radius: 3px;
}

table.callboard tr {
	text-align: center;
	padding-left: 20px;
}

table.callboard td:first-child {
	text-align: left;
	border-left: 0;
	width: 170px;
	min-width: 200px;
}

table.callboard td {
	border-top: 1px solid #ffffff;
	border-bottom: 1px solid #e0e0e0;
	border-left: 1px solid #e0e0e0;
	background: #fafafa;
	background: -webkit-gradient(linear, left top, left bottom, from(#fbfbfb),
		to(#fafafa));
	background: -moz-linear-gradient(top, #fbfbfb, #fafafa);
}

table.callboard tr.even td {
	background: #f6f6f6;
	background: -webkit-gradient(linear, left top, left bottom, from(#f8f8f8),
		to(#f6f6f6));
	background: -moz-linear-gradient(top, #f8f8f8, #f6f6f6);
}

table.callboard tr:last-child td {
	border-bottom: 0;
}

table.callboard tr:last-child td:first-child {
	-moz-border-radius-bottomleft: 3px;
	-webkit-border-bottom-left-radius: 3px;
	border-bottom-left-radius: 3px;
}

table.callboard tr:last-child td:last-child {
	-moz-border-radius-bottomright: 3px;
	-webkit-border-bottom-right-radius: 3px;
	border-bottom-right-radius: 3px;
}

table.callboard tr:hover td {
	background: #f2f2f2;
	background: -webkit-gradient(linear, left top, left bottom, from(#f2f2f2),
		to(#f0f0f0));
	background: -moz-linear-gradient(top, #f2f2f2, #f0f0f0);
}

table.callboard td.shiftUser {
	text-align: center;
}

table.callboard td.shiftPanelOne {
	padding: 3px;
	max-height: 100px;
	width: 130px;
	height: 50px;
	cursor: pointer;
	border: 3px solid #FFFFFF;
}

table.shiftPanel {
	font-size: 12px;
}

table.shiftPanel td:first-child {
	text-align: center;
	border-left: 0;
	width: 100px;
	min-width: 100px;
	height: 50px;
}

table.callboard div.teamChange {
	height: 0;
	position: relative;
	width: 0;
	z-index: 9999;
}

td.unselectable {
	-webkit-touch-callout: none;
	-webkit-user-select: none;
	-khtml-user-select: none;
	-moz-user-select: none;
	-ms-user-select: none;
	user-select: none;
}

td.finalData {
	background-color: red !important;
	background: -moz-linear-gradient(center top, #F2C777, #F2C777) repeat
		scroll 0 0 transparent !important;
	color: #1C94C4;
	text-shadow: 0px 0px;
	cursor: default !important;
}

td.summary {
	padding: 0px;
	height: 40px;
	max-height: 40px;
	width: 40px;
	min-height: 40px;
	min-width: 40px;
	cursor: pointer;
}

td.summaryMinimal {
	padding: 0px;
	height: 20px;
	max-height: 20px;
	width: 40px;
	min-height: 20px;
	min-width: 40px;
	cursor: pointer;
}

tr.hidden {
	display: none;
}

div.callboardControlGroup {
	margin: 5px;
}

p.shiftSymbol {
	color: #777777;
	position: absolute;
	text-align: center;
	margin: 0;
	z-index: 999;
	text-shadow: 1px 1px 1px #fff;
}

p.shiftSymbolFont {
	font-size: 25px;
}

p.shiftSymbolFontMinimal {
	font-size: 16px;
}

td.dayShift {
	padding: 0px;
	height: 40px;
	max-height: 40px;
	width: 40px;
	min-height: 40px;
	min-width: 40px;
	cursor: pointer;
}

td.dayShiftMinimal {
	padding: 0px;
	height: 20px;
	max-height: 20px;
	width: 40px;
	min-height: 20px;
	min-width: 40px;
	cursor: pointer;
}

p.userName {
	height: 1em;
	overflow: hidden;
	word-break: break-all;
}

.shiftHover {
	background: #f2f2f2 !important;
	background: -webkit-gradient(linear, left top, left bottom, from(#f2f2f2),
		to(#f0f0f0)) !important;
	background: -moz-linear-gradient(top, #f2f2f2, #f0f0f0) !important;
}

.userShiftLine {
	height: 40px;
	max-height: 40px;
}

.userShiftLineMinimal {
	height: 20px;
	max-height: 20px;
}

.hidden {
	display: none;
}

.userWorkType a {
	display: inline-block;
	padding-left: 5px;
	width: 300px;
}

#progressbar .ui-progressbar-value {
	background-color: #ccc;
}

td.groupHeaderStandard {
	height: 40px !important;
	text-align: center !important;
}

td.groupHeaderMinimal {
	height: 20px !important;
	text-align: center !important;
}

.pointer {
	cursor: pointer;
}

.sortable {
	list-style-type: none;
	margin: 0;
	padding: 0;
	width: 60%;
	display: inline;
}

.sortable li {
	margin: 0 3px 3px 3px;
	padding: 0.4em;
	padding-left: 1.5em;
	font-size: 1.4em;
	height: 18px;
}

.sortable li span {
	position: absolute;
	margin-left: -1.3em;
}
--%>
