<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="mt1 mb1">
	<h2>Выбор номера</h2>

	<c:set var="closeCode">$(this.form).find('#needPhone').toggle(); $(this.form).find('#phoneEditor').toggle();</c:set>

	<div class="in-inline-block">
		<c:set var="style"></c:set>
		<div style="vertical-align: top; width: 50%; min-height: 20em;">
			<ui:tree-single rootNode="${frd.rootCategory}" name="categoryId" onSelect="$$.bgbilling.voice.category(this)" />
		</div><%--
	--%><div id="category" style="vertical-align: top; width: 50%;">
			<%-- loaded --%>
		</div>
	</div>

	<button class="btn-white mr1" type="button" onclick="this.form.number.value = this.form.freeNumber.value; ${closeCode}">OK</button>
	<button class="btn-white" type="button" onclick="${closeCode}">Отмена</button>
</div>