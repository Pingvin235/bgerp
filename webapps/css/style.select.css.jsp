<%@ page contentType="text/html; charset=UTF-8"%>

.select {
  border: 1px solid #c5c5c5;
  -webkit-border-radius: 3px;
  -moz-border-radius: 3px;
  -ms-border-radius: 3px;
  -o-border-radius: 3px;
  border-radius: 3px;
  -webkit-box-shadow: 0px 3px 6px #e2e2e2 inset;
  -moz-box-shadow: 0px 3px 6px #e2e2e2 inset;
  box-shadow: 0px 3px 6px #e2e2e2 inset;
  display: -moz-inline-stack;
  display: inline-block;
  vertical-align: middle;
  *vertical-align: auto;
  zoom: 1;
  *display: inline;
  position: relative;
}

.select input[type=text] {
  margin: 0;
  border: none;
  padding-right: 1.5em;
  width: 100%;
}

.select .icon {
    cursor: pointer;
    position: absolute;
    width: 1.5em;
    height: 2.4em;
    top: 0em;
    right: 0.2em;
    background: url("/images/arrow-down.png") no-repeat scroll 50% 50% transparent;
}

.select:hover {
  border: 1px solid #5bc5ff;
}

.select:active {
  border: 1px solid #5bc5ff;
}

/* select-mult */

.select-mult .drop-list {
  width: 100%;
  background-color: #ffffff;
  display: block;
}

.select-mult ul.drop-list {
  border: 1px solid #d5d5d5;
  background-color: #ffffff;
  /* чтобы список значений не был шире редактируемой области сверху */ 
  box-sizing: border-box;
  -moz-box-sizing: border-box;
}

.select-mult .btn-add {
	font-size: 1.5em; 
	padding: 0.18em 0.4em;
	margin-left: 0.2em;
}

.select-mult ul.drop-list >  li {
  border-top: 1px solid #d5d5d5;
  position: relative;
  padding: 0.5em;
  padding-left: 1.8em;  
  color: #505050;
  cursor: pointer;
  white-space: no-wrap;  
}

.select-mult ul.drop-list.move-on>  li {
  padding-left: 2.8em;
}

.select-mult ul.drop-list >  li .delete {
  position: absolute;
  background: url("/images/cross.png") no-repeat;
  /* background-position: 0 -24px; */
  width: 12px;
  height: 11px;
  top: 0.7em;
  left: 0.5em;  
}

.select-mult ul.drop-list > li .up,
.select-mult ul.drop-list > li .down {
   display: none;
}

.select-mult ul.drop-list.move-on >  li .up {
  display: block;
  position: absolute;
  background: url("/images/arrow-up.png") no-repeat;
  background-position: top;
  width: 12px;
  height: 10px;
  top: 0.2em;
  left: 1.6em;
}

.select-mult ul.drop-list.move-on >  li .down {
  display: block;
  position: absolute;
  background: url("/images/arrow-down.png") no-repeat;
  background-position: bottom;
  width: 12px;
  height: 10px;
  bottom: 0.2em;
  left: 1.6em;
}

.select-mult ul.drop-list >  li span.title {
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;  
}

.select-mult ul.drop-list >  li:first-child {
  border-top: none;
}

.select-mult ul.drop-list >  li:hover {
  background-color: #fafafa;
}