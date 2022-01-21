<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="invoice" value="${form.response.data.invoice}"/>
<c:set var="process" value="${form.response.data.process}"/>

<html>
	<head>
		<style>
		body {
			height: 297mm;
			width: 210mm;
			padding: 30mm 20mm;
			/* to centre page on screen*/
			margin-left: auto;
			margin-right: auto;
		}
		.in-table-cell > * {
			display: table-cell;
		}
		<%@ include file="/css/style.table.css.jsp"%>
		</style>
	</head>
	<body>
		<h1>Vova Kutin</h1>


		Red Place 1<br/>
		Russia, Mocsow 125009<br/>


		<div class="in-table-cell">
			<div style="width: 60mm;">
				<b>Bill To</b><br/>
				Obama Karak</br>
			</div
		</div>
	</body>
</html>

