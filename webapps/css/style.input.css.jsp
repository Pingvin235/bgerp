<%@ page contentType="text/css; charset=UTF-8"%>

input, textarea {
	outline: none;
}

input:active, textarea:active {
	outline: none;
}

:focus {
	outline: none;
}

textarea {
	border: 1px solid #c5c5c5;
	border-radius: 3px;
	box-shadow: 0px 3px 6px #e2e2e2 inset;
	padding: 0.7em 0.2em 0.2em 0.7em;
}

textarea:focus {
	border: 1px solid #5bc5ff;
}

input {
	border: 1px solid #c5c5c5;
	border-radius: 3px;
	box-shadow: 0px 3px 6px #e2e2e2 inset;
	padding: 0.6em 0.5em;
	display: inline-block;
	vertical-align: middle;
}

input:focus {
	border: 1px solid #5bc5ff;
}

input.error, input.error:focus {
	border: 1px solid #fb8181;
}

input.approved, input.approved:focus {
	border: 1px solid #4dd146;
}
