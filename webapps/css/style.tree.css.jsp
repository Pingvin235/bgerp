<%@ page contentType="text/css; charset=UTF-8"%>

.tree-single .children {
	padding-left: 1em;
	display: none;
}

.tree-single .title {
	user-select: none;
}

.tree-single [onclick] {
	cursor: pointer;
}

.tree-single .selected .text {
	font-weight: bold;
	color: #00f;
}

.tree-single .expander.folder {
	cursor: pointer;
}

.tree-single .expander:not(.folder) {
	opacity: 0;
}

.tree-single .expander.open {
	display: inline-block;
	transform: rotate(90deg);
}