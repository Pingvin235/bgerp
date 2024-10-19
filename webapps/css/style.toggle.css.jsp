<%@ page contentType="text/css; charset=UTF-8"%>

.btn-toggle {
	display: inline-block;
	border-radius: 3px;
	border: 1px solid #c8c8c8;
	<%@ include file="/WEB-INF/jspf/css/button.white.gradient.jsp"%>
}

.btn-toggle > .text-pref {
	color: var(--p-color);
	padding-left: 0.5em;
	display: inline-block;
	white-space: nowrap;
}

/* Colors: Default (blue) */
.btn-toggle > label {
	color: #fff;
}

.btn-toggle > label > input + .toggle > .switch {
	background: #fff;
}

.btn-toggle > label > input + .toggle + .label {
	color: #000;
}

.btn-toggle > label > input:checked + .toggle {
	background: #0b67a1;
}

.btn-toggle > label > input:not(:checked) + .toggle {
	background: #ccc;
}

.btn-toggle > label > input:checked + .toggle > .switch {
	border: 3px solid #0b67a1;
}

.btn-toggle > label > input:not(:checked) + .toggle > .switch {
	border: 3px solid #ccc;
}

/* for shadows, may be enabled later */
/*.btn-toggle > label > input:focus + .toggle,*/
/*.btn-toggle > label > input:active + .toggle {*/
/*	box-shadow: 0 0 5px 3px rgba(0, 119, 200, 0.50);*/
/*}*/

.btn-toggle > label {
	display: inline-flex;
	align-items: center;
	user-select: none;
	position: relative;
	vertical-align: middle;
	margin-bottom: 0;
}

.btn-toggle > label:hover {
	cursor: pointer;
}

.btn-toggle > label > input {
	position: absolute;
	opacity: 0;
}

.btn-toggle > label > input + .toggle {
	align-items: center;
	position: relative;
}

.btn-toggle > label > input + .toggle {
	overflow: hidden;
	position: relative;
	flex-shrink: 0;
}

.btn-toggle > label > input[disabled] + .toggle {
	opacity: 0.5;
}

.btn-toggle > label > input[disabled] + .toggle:hover {
	cursor: not-allowed;
}

.btn-toggle > label > input + .toggle {
	width: 100%;
	height: 100%;
	margin: 0;
	cursor: pointer;
	border-radius: 3px;
}

.btn-toggle > label > input + .toggle > .switch {
	display: block;
	height: 100%;
	position: absolute;
	right: 0;
	z-index: 3;
	box-sizing: border-box;
	border-radius: 6px;
	<%@ include file="/WEB-INF/jspf/css/button.white.gradient.jsp"%>
}

/* Labels */
.btn-toggle > label > input + .toggle:before,
.btn-toggle > label > input + .toggle:after {
	display: flex;
	align-items: center;
	position: absolute;
	z-index: 2;
	height: 100%;
	font-size: 0.9em;
}

.btn-toggle > label > input + .toggle:before {
	right: 50%;
	content: attr(data-before);
}

.btn-toggle > label > input + .toggle:after {
	left: 50%;
	content: attr(data-after);
}

.btn-toggle[data-label='left'] > input + .toggle {
	order: 2;
}

/* Show / Hide */
.btn-toggle > label > input + .toggle:before {
	opacity: 0;
}

.btn-toggle > label > input:checked + .toggle:before {
	opacity: 1;
}

.btn-toggle > label > input:checked + .toggle:after {
	opacity: 0;
}

/* Transitions */
.btn-toggle > label > input + .toggle {
	transition: background 200ms linear, box-shadow 200ms linear;
}

.btn-toggle > label > input + .toggle:before,
.btn-toggle > label > input + .toggle:after {
	transition: all 200ms linear;
}

.btn-toggle > label > input + .toggle > .switch {
	transition: right 200ms linear, border-color 200ms linear;
}

/* Size: Default */
.btn-toggle > label > input + .toggle {
	width: 5.5em;
	height: 2.4em;
}

.btn-toggle > label > input + .toggle > .switch {
	width: 2.5em;
}

.btn-toggle > label > input:not(:checked) + .toggle > .switch {
	right: calc(100% - 2.5em);
}

/* Size: Small */
.btn-toggle-small > label > input + .toggle {
	width: 4em;
	height: 2em;
}

.btn-toggle-small > label > input + .toggle > .switch {
	width: 2em;
}

.btn-toggle-small > label > input:not(:checked) + .toggle > .switch {
	right: calc(100% - 2em);
}
