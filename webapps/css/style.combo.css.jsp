<%@ page contentType="text/html; charset=UTF-8"%>

/* можно посмотреть http://www.howtomake.com.ua/html-and-css/stilizaciya-vsex-elementov-form-s-pomoshhyu-css-i-jquery.html */
.combo .text-pref {
	color: #505050;
	padding-left: 0.5em;
	display: table-cell;
	height: 100%;
	white-space: nowrap;
}

.combo .text-value {
	display: table-cell;
	text-align: left;
	padding-left: 0.5em;
	padding-right: 0.5em;
	overflow: hidden;
	white-space: nowrap;
	text-overflow: ellipsis;
	-o-text-overflow: ellipsis;
}

.combo .icon {
	display: table-cell;
	height: 100%;
	padding-right: 0.5em;
}

.combo {
   position: relative;  
   padding-right: 0;
   padding-left: 0;
}

.combo ul.drop {
  margin-top: 0.6em;
  overflow-x: hidden;
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

#processQueueSelect .drop li div:not(.icon-add) {
	display: inline-block;
	width: 93%;
}
