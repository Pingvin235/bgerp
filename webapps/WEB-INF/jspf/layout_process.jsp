<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Вызов функции менеджера компоновки - расстановка размеров элементов.
--%>

<u:sc>
	<c:set var="uiid" value="${u:uiid()}"/>
	<span id="${uiid}" style="display:none;"></span>
	
	<script>
		$(function () {
			$$.ui.layout($('#${uiid}').parent());
			
			// повторный вызов обусловлен тем, что первый не всегда корректно отрабатывает, особенно если есть div табличного вида
			// а если всегда вызывать второй то видно визуально растягивание интерфейса всегда
			setTimeout(function () {
				$$.ui.layout($('#${uiid}').parent());
			}, 0 );
		});
	</script>
</u:sc>