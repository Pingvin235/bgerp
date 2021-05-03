<%@ page contentType="text/css; charset=UTF-8"%>

/* Plugin CallBoard */
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
