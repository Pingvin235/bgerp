<%@ page contentType="text/css; charset=UTF-8"%>

/* Plugin Kanban */
.kanban-board {
	display: flex;
	/* stretch (the flex default) is required, not flex-start: otherwise a short/empty column's
	   box only covers its own content height, and everything below the header is background,
	   not part of the drop target - drag-and-drop would then only register on the header. */
	align-items: stretch;
	gap: 1em;
	overflow-x: auto;
	padding-bottom: 1em;
}

.kanban-column {
	flex: 0 0 270px;
	width: 270px;
	display: flex;
	flex-direction: column;
	background-color: #f4f5f7;
	border-radius: 6px;
	height: calc(100vh - 200px);
}

.kanban-column-head {
	padding: .5em .75em;
	font-weight: bold;
	border-radius: 6px 6px 0 0;
}

.kanban-count {
	font-weight: normal;
	opacity: .7;
}

.kanban-column-body {
	flex: 1;
	overflow-y: auto;
	padding: .5em;
}

.kanban-column.kanban-drag-over {
	outline: 2px dashed #4090e0;
	outline-offset: -2px;
}

.kanban-card {
	background-color: #fff;
	border-left: 4px solid #ccc;
	border-radius: 4px;
	padding: .5em .75em;
	margin-bottom: .5em;
	box-shadow: 0 1px 2px rgba(0, 0, 0, .15);
}

.kanban-card[draggable=true] {
	cursor: grab;
}

.kanban-card-title {
	font-weight: 600;
}

.kanban-card-desc {
	font-size: .85em;
	color: #555;
	margin-top: .25em;
}

.kanban-card-executors {
	font-size: .8em;
	color: #777;
	margin-top: .25em;
}
