<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<div id="${uiid}" class="ifaceEditor"><h2> Выберите интерфейс</h2>
	<script>
		const loadIface = (port,title)=>$$.bgbilling.inet.setIface(port,title);
	</script>
	<table class="data mt1" >
		<tr>
			<td >Интерфейс</td>
			<td >Название</td>
			<td >Комментарий</td>
		</tr>
		<c:forEach var="item" items="${form.response.data.interfaces}">
			<tr>
				<td>
					<a href="#" href="#" onclick="loadIface('${item.port}','${item.title}')">${item.port}</a>
				</td>
				<td>
					<a href="#" href="#" onclick="loadIface('${item.port}','${item.title}')">${item.title}</a>
				</td>
				<td>
						${item.comment}
				</td>
			</tr>
		</c:forEach>
	</table>
	<button class="btn-grey" type="button" onclick="$('#${uiid}').parent().text('')">Отмена</button>
</div>