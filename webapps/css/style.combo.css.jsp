<%@ page contentType="text/css; charset=UTF-8"%>

.combo {
	position: relative;
	display: inline-flex;
	padding-right: 0;
	padding-left: 0;
}

/* можно посмотреть http://www.howtomake.com.ua/html-and-css/stilizaciya-vsex-elementov-form-s-pomoshhyu-css-i-jquery.html */
.combo .text-pref {
	color: var(--p-color);
	padding-left: 0.5em;
	/*height: 100%;*/
	white-space: nowrap;
}

.combo .text-value {
	text-align: left;
	padding-left: 0.5em;
	padding-right: 0.5em;
	overflow: hidden;
	white-space: nowrap;
	text-overflow: ellipsis;
}

.combo > .icon {
	/*height: 100%;*/
	padding-right: 0.5em;
}

.combo > .drop {
	margin-top: 1.8em;
	overflow-x: auto;
	overflow-y: visible;
	max-height: 300px;
	min-width: 150px;
}

.combo ul.drop li {
	overflow: hidden;
	text-overflow: ellipsis;
}

.combo ul.drop li span {
	vertical-align: bottom;
}

#processQueueSelect .drop li {
	display: flex;
}

#processQueueSelect .drop li div:not(.icon-add) {
	width: 100%;
}
