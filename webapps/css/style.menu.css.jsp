<%@ page contentType="text/css; charset=UTF-8"%>

/* выпадающие списки и меню */

.ui-menu { width: 200px; }

/* выпадающий список для нередактируемого и редактируемого селекта */
.select ul.ui-menu, .combo > .drop {
	width: 100%;
	position: absolute;
	left: -1px;
	z-index: 999;
}

.ui-menu, .combo > .drop, .ui-tabs.ui-widget-content .ui-menu {
	border: var(--popup-border);
	background-color: #ffffff;
	box-shadow: var(--popup-box-shadow);
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
	border-top: var(--popup-border);
	padding: 0.5em;
	color: var(--p-color);
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

