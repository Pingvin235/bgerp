<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="invoice" value="${form.response.data.invoice}"/>
<c:set var="process" value="${form.response.data.process}"/>

<html>
	<head>
		<%@ include file="style.jsp"%>
	</head>
	<body>
		<h1>Vova Kutin</h1>

		Red Place 1<br/>
		Russia, Mocsow 125009<br/>

		<div class="in-table-cell">
			<div style="width: 60mm;">
				<b>Bill To</b><br/>
				Obama Karak</br>
			</div>
		</div>

		<b>WORK IN PROGRESS</b>
	</body>
</html>

