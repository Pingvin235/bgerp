<%@ page contentType="text/css; charset=UTF-8"%>

/* стандартные внешние отступы */
.mt05 {
	margin-top: 0.5em;
}

.mb05 {
	margin-bottom: 0.5em;
}

.ml05 {
	margin-left: 0.5em;
}

.mr05 {
	margin-right: 0.5em;
}

.mt1 {
	margin-top: 1em;
}

.mb1 {
	margin-bottom: 1em;
}

.ml1 {
	margin-left: 1em;
}

.mr1 {
	margin-right: 1em;
}

.mt2 {
	margin-top: 2em;
}

.mb2 {
	margin-bottom: 2em;
}

.ml2 {
	margin-left: 2em;
}

.mr2 {
	margin-right: 2em;
}

/* стандартные внешние отступы внутри другого элемента-контейнера*/
.in-mt05 > *:not(:first-child) {
	margin-top: 0.5em;	
}

.in-mb05 > *:not(:last-child) {
	margin-bottom: 0.5em;	
}

.in-ml05 > *:not(:first-child) {
	margin-left: 0.5em;	
}

.in-mr05 > *:not(:last-child) {
	margin-right: 0.5em;	
}

.in-mt1 > *:not(:first-child) {
	margin-top: 1em;	
}

.in-mb1 > *:not(:last-child) {
	margin-bottom: 1em;	
}

.in-ml1 > *:not(:first-child) {
	margin-left: 1em;	
}

.in-mr1 > *:not(:last-child) {
	margin-right: 1em;	
}

/* -all стили не делают исключений для первых/последний элементов */
.in-mt05-all > * {
	margin-top: 0.5em;	
}

.in-mb05-all > * {
	margin-bottom: 0.5em;	
}

.in-ml05-all > * {
	margin-left: 0.5em;	
}

.in-mr05-all > * {
	margin-right: 0.5em;	
}

.in-mt1-all > * {
	margin-top: 1em;	
}

.in-mb1-all > * {
	margin-bottom: 1em;	
}

.in-ml1-all > * {
	margin-left: 1em;	
}

.in-mr1-all > * {
	margin-right: 1em;	
}

/* стандартные внутренние отступы */
.p05 {
	padding: 0.5em;
}

.pt05 {
	padding-top: 0.5em;
}

.pb05 {
	padding-bottom: 0.5em;
}

.pl05 {
	padding-left: 0.5em;
}

.pr05 {
	padding-right: 0.5em;
}

.p1 {
	padding: 1em;
}

.pt1 {
	padding-top: 1em;
}

.pb1 {
	padding-bottom: 1em;
}

.pl1 {
	padding-left: 1em;
}

.pr1 {
	padding-right: 1em;
}

.p2 {
	padding: 2em;
}

.pt2 {
	padding-top: 2em;
}

.pb2 {
	padding-bottom: 2em;
}

.pl2 {
	padding-left: 2em;
}

.pr2 {
	padding-right: 2em;
}

/* стандартные внутренни отступы внутри другого элемента-контейнера */
.in-pt05 > * {
	padding-top: 0.5em;	
}

.in-pb05 > * {
	padding-bottom: 0.5em;	
}

.in-pl05 > *:not(:first-child) {
	padding-left: 0.5em;	
}

.in-pr05 > *:not(:last-child) {
	padding-right: 0.5em;	
}

.in-pt1 > * {
	padding-top: 1em;	
}

.in-pb1 > * {
	padding-bottom: 1em;	
}

.in-pl1 > *:not(:first-child) {
	padding-left: 1em;	
}

.in-pr1 > *:not(:last-child) {
	padding-right: 1em;	
}

/* стандартные ширины внутри */
.w100p {
	width: 100%;  
}

.in-w100p > *:not(span) {
	width: 100%;	
}
