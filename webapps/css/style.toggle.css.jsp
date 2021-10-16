<%@ page contentType="text/css; charset=UTF-8"%>

/* Colors: Default (blue) */
.btn-toggle {
	color: #fff;
}

.btn-toggle > input + .toggle > .switch {
	background: #fff;
}

.btn-toggle > input + .toggle + .label {
	color: #000;
}

.btn-toggle > input:checked + .toggle {
	background: #0b67a1;
}

.btn-toggle > input:not(:checked) + .toggle {
	background: #ccc;
}

.btn-toggle > input:checked + .toggle > .switch {
	border: 3px solid #0b67a1;
}

.btn-toggle > input:not(:checked) + .toggle > .switch {
	border: 3px solid #ccc;
}

/* for shadows, may be enabled later */
/*.btn-toggle > input:focus + .toggle,*/
/*.btn-toggle > input:active + .toggle {*/
/*	box-shadow: 0 0 5px 3px rgba(0, 119, 200, 0.50);*/
/*}*/

/* CORE STYLES BELOW - DO NOT TOUCH */
.btn-toggle {
	display: inline-flex;
	align-items: center;
	user-select: none;
	position: relative;
	vertical-align: middle;
	margin-bottom: 0;
}

.btn-toggle:hover {
	cursor: pointer;
}

.btn-toggle > input {
	position: absolute;
	opacity: 0;
}

.btn-toggle > input + .toggle {
	align-items: center;
	position: relative;
}

.btn-toggle > input + .toggle {
	overflow: hidden;
	position: relative;
	flex-shrink: 0;
}

.btn-toggle > input[disabled] + .toggle {
	opacity: 0.5;
}

.btn-toggle > input[disabled] + .toggle:hover {
	cursor: not-allowed;
}

.btn-toggle > input + .toggle {
	width: 100%;
	height: 100%;
	margin: 0;
	cursor: pointer;
	border-radius: 3px;
}

.btn-toggle > input + .toggle > .switch {
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
.btn-toggle > input + .toggle:before,
.btn-toggle > input + .toggle:after {
	display: flex;
	align-items: center;
	position: absolute;
	z-index: 2;
	height: 100%;
	font-size: 0.9em;
}

.btn-toggle > input + .toggle:before {
	right: 55%;
	content: attr(data-before);
}

.btn-toggle > input + .toggle:after {
	left: 55%;
	content: attr(data-after);
}

.btn-toggle > input + .toggle + .label {
	margin-left: 10px;
}

.btn-toggle[data-label='left'] > input + .toggle {
	order: 2;
}

.btn-toggle[data-label='left'] > input + .toggle + .label {
	order: 1;
	margin-left: 0;
	margin-right: 10px;
}

/* Show / Hide */
.btn-toggle > input + .toggle:before {
	opacity: 0;
}

.btn-toggle > input:checked + .toggle:before {
	opacity: 1;
}

.btn-toggle > input:checked + .toggle:after {
	opacity: 0;
}

/* Transitions */
.btn-toggle > input + .toggle {
	transition: background 200ms linear, box-shadow 200ms linear;
}

.btn-toggle > input + .toggle:before,
.btn-toggle > input + .toggle:after {
	transition: all 200ms linear;
}

.btn-toggle > input + .toggle > .switch {
	transition: right 200ms linear, border-color 200ms linear;
}

/* CORE STYLES ABOVE - DO NOT TOUCH */

/* Size: Default */
.btn-toggle > input + .toggle {
	width: 62px;
	height: 32px;
}

.btn-toggle > input + .toggle > .switch {
	width: 32px;
}

.btn-toggle > input:not(:checked) + .toggle > .switch {
	right: calc(100% - 32px);
}

.btn-toggle.btn-small > input + .toggle {
	width: 54px;
	height: 26px;
}

/* Size: Small */
.btn-toggle.btn-small > input + .toggle > .switch {
	width: 26px;
}

.btn-toggle.btn-small > input:not(:checked) + .toggle > .switch {
	right: calc(100% - 26px);
}

.btn-toggle.btn-small > input + .toggle:before,
.btn-toggle.btn-small > input + .toggle:after {
	font-size: 0.8em;
}
