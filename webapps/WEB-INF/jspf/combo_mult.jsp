<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Должен выглядеть как combo_single но с возможностью указания нескольких значений через запятую.
Можно ещё в скобках указывать, сколько выбрано и в popup отображать, чтобы быстро посмотреть.
При редактировании вместо него открывается select_mult и можно править.

ЭЛЕМЕНТ НЕ ДОРЕАЛИЗОВАН!!
--%>

<c:set var="uiid" value="${u:uiid()}"/>

<div class="btn-white combo" id="${uiid}">
	<input type="hidden" name="${hiddenName}" value="${value}"/>

	<div class="text-pref">${l.l('Текст')}:</div>
	
	<%-- ширину всего элемента можно задавать только шириной этого блока --%>
	<div class="text-value" style="width: 100px; max-width: 100px;">[8] Значение, значени2, значение3 </div>
	<div class="icon"><img src="/images/arrow-down.png"/></div>
	 
	<ul class="drop" style="display: none;">
		${valuesHtml}
	</ul>
</div>