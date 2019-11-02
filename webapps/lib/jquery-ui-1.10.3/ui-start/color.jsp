<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
.ui-widget-content {
	border: 1px solid #a6c9e2;
	background: #fcfdfd url(images/ui-bg_inset-hard_100_fcfdfd_1x100.png) 50% bottom repeat-x;
 --%>	
<c:set var="UI_COLOR_CONTENT" value="#FCFDFD" scope="request"/>
<c:set var="UI_COLOR_CONTENT_BORDER" value="#A6C9E2" scope="request"/>

<%--
.ui-widget-header {
	border: 1px solid #4297d7;
	background: #2191c0 url(images/ui-bg_gloss-wave_75_2191c0_500x100.png) 50% 50% repeat-x;
--%>	
<c:set var="UI_COLOR_HEADER" value="#2191C0" scope="request"/>
<c:set var="UI_COLOR_HEADER_BORDER" value="#4297d7" scope="request"/>
 	
<%--
.ui-state-default,
.ui-widget-content .ui-state-default,
.ui-widget-header .ui-state-default {
	border: 1px solid #77d5f7;
	background: #0078ae url(images/ui-bg_glass_45_0078ae_1x400.png) 50% 50% repeat-x;
	font-weight: normal;
	color: #ffffff;
}
--%>
<c:set var="UI_COLOR_STATE_DEFAULT" value="#0078AE" scope="request"/>
<c:set var="UI_COLOR_STATE_DEFAULT_BORDER" value="#77d5f7" scope="request"/>

<%--
.ui-state-hover,
.ui-widget-content .ui-state-hover,
.ui-widget-header .ui-state-hover,
.ui-state-focus,
.ui-widget-content .ui-state-focus,
.ui-widget-header .ui-state-focus {
	border: 1px solid #448dae;
	background: #79c9ec url(images/ui-bg_glass_75_79c9ec_1x400.png) 50% 50% repeat-x;
	font-weight: normal;
	color: #026890;
}
--%>
<c:set var="UI_COLOR_STATE_HOVER" value="#79c9ec" scope="request"/>
<c:set var="UI_COLOR_STATE_HOVER_BORDER" value="#448dae" scope="request"/>

<%--
.ui-state-active,
.ui-widget-content .ui-state-active,
.ui-widget-header .ui-state-active {
	border: 1px solid #acdd4a;
	background: #6eac2c url(images/ui-bg_gloss-wave_50_6eac2c_500x100.png) 50% 50% repeat-x;
	font-weight: normal;
	color: #ffffff;
} 
--%>
<c:set var="UI_COLOR_STATE_ACTIVE" value="#6eac2c" scope="request"/>
<c:set var="UI_COLOR_STATE_ACTIVE_BORDER" value="#acdd4a" scope="request"/>

<%--
.ui-state-highlight,
.ui-widget-content .ui-state-highlight,
.ui-widget-header .ui-state-highlight {
	border: 1px solid #fcd113;
	background: #f8da4e url(images/ui-bg_glass_55_f8da4e_1x400.png) 50% 50% repeat-x;
	color: #915608;
}
--%>
<c:set var="UI_COLOR_HL" value="#f8da4e" scope="request"/>
<c:set var="UI_COLOR_HL_BORDER" value="#fcd113" scope="request"/>
