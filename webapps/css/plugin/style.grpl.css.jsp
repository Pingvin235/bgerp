<%@ page contentType="text/css; charset=UTF-8"%>

.grpl-board td[bg-column-id] {
	padding: 0 !important;
}

.grpl-non-working-day {
	color: red !important;
	font-weight: bold;
}

.grpl-past, .grpl-past .grpl-board-group {
	background-color: #b7b7b790 !important;
}

.grpl-board-group {
	padding: 0.5em 1em;
	background-color: #b7e1cd80;
}

.grpl-board-process, .grpl-board-process-placement {
	padding: 0.5em 1em;
}

.grpl-board-process-placement {
	background-color: var(--table-hl-bg-color);
}

.grpl-board-process-placement.grpl-board-drop-allowed {
	background-color: #0d726f;
}

.grpl-board-group:hover, .grpl-board-process:hover {
	background-color: var(--table-hl-bg-color) !important;
}
