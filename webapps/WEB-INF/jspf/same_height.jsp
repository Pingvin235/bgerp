<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
	JS, устанавливающий высоту элемента на основании высоты другого элемента.
	TODO: Пробовать использовать flex CSS свойства вместо, как в свойствах типа процесса.
--%>
<script>
	$(function () {
		const resize = function () {
			$("${selectorTo}").css("height", $("${selectorSample}").height() + "px" );
		};
		resize();
		<c:if test="${not empty track}">
			$("${selectorSample}").resize(function () {
				resize();
			})
		</c:if>
		<c:remove var="track"/>
	})
</script>
