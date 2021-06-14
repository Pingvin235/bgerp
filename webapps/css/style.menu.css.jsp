<%@ page contentType="text/css; charset=UTF-8"%>

/* выпадающие списки и меню */

.ui-menu { width: 200px; }

/* выпадающий список для нередактируемого и редактируемого селекта */
.select ul.ui-menu, .combo ul.drop {
	width: 100%;
	position: absolute;
	left: -1px;
	z-index: 999;
}

.ui-menu, .combo ul.drop, .ui-tabs.ui-widget-content .ui-menu {
	border: 1px solid #d5d5d5;
	background-color: #ffffff;
	box-shadow: 0 0 5px #cbcbcb;
	text-align: left;
	z-index: 999;
}

.select ul.ui-menu {
	overflow-x: hidden;
	overflow-y: visible;
	max-height: 300px;
	min-width: 150px;
}

.select ul.ui-menu li {
	white-space: normal;
}

.ui-menu li a, .ui-menu li > div,
.ui-menu li a.ui-state-hover, .ui-menu li > div.ui-state-hover,
.ui-menu li a.ui-state-focus, .ui-menu li > div.ui-state-focus,
.ui-menu li a.ui-state-active, .ui-menu li > div.ui-state-active,
.combo ul.drop li {
	display: block;
	border: none;
	border-top: 1px solid #d5d5d5;
	padding: 0.5em;
	color: #505050;
	cursor: pointer;
}

.ui-menu li a.ui-state-hover, .ui-menu li > div.ui-state-hover,
.ui-menu li a.ui-state-focus, .ui-menu li > div.ui-state-focus,
.ui-menu li a.ui-state-active, .ui-menu li > div.ui-state-active,
.combo ul.drop li:hover {
	background: #e5e5e5;
	background-image: none;
	background-color: #e5e5e5;
}


/* меню выступающее именно в роли меню, всплывающего по кнопке */
.menu.ui-menu .ui-menu {
	position: absolute;
}

/* т.к. все стили меню закомментированы в JQueryUI Style CSS */
.menu.ui-menu .ui-menu-icon {
	float: right;
}

