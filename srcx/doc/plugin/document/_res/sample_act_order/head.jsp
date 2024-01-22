<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%-- установите ваши значения параметров --%>
<c:set var="PROCESS_PARAM_ADDRESS" value="4"/>
<c:set var="PROCESS_PARAM_TIME_FROM" value="5"/>
<c:set var="PROCESS_PARAM_TIME_TO" value="10"/>
<c:set var="PROCESS_PARAM_PHONE" value="16"/>

<html>
	<head>
		<link rel="stylesheet" type="text/css" href="/css/style.css.jsp"/>
		<style>
			@page {
			    size: auto;   /* auto is the initial value */
			    margin: 0;  /* this affects the margin in the printer settings */
			}
			body {
				padding: 2em;
			}
			h2 {
				font-weight: bold;
				text-align: center;
				padding-bottom: 1em;
				padding-top: 1em;
			}
			p {
				padding-bottom: 0.5em;
			}
			table {
				margin-bottom: 2em;
			}
			table td {
				border: 1px black solid;
				padding: 0.5em;
			}
		</style>
	</head>
	<body>
		<u:newInstance var="processDao" clazz="ru.bgcrm.dao.process.ProcessDAO">
			<u:param value="${conSlave}"/>
		</u:newInstance>
		<u:newInstance var="processLinkDao" clazz="ru.bgcrm.dao.process.ProcessLinkDAO">
			<u:param value="${conSlave}"/>
		</u:newInstance>
		<u:newInstance var="paramDao" clazz="org.bgerp.dao.param.ParamValueDAO">
			<u:param value="${conSlave}"/>
		</u:newInstance>

		<u:newInstance var="curdate" clazz="java.util.Date"/>

