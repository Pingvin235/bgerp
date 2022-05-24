<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="allowOnlyCategories" value="${form.response.data.allowOnlyCategories}" scope="page" />

<p class="slide"><input id="shiftPanelCloseButton${uiid}" class="shiftPanelButton${uiid}" style="margin-left: 20px; margin-top: 5px;" type="button" value="Смены"  /></p>

<div id="shiftChooseTable${uiid}" style="display: none; width: auto; ">

	<div style="margin: 10px 20px 0;">
		<span>Категория</span>

		<c:url var="url" value="/user/plugin/callboard/work.do">
			<c:param name="action" value="callboardAvailableShift"/>
			<c:param name="returnUrl" value="${form.requestUrl}"/>
		</c:url>

		<select name="categoryId" onchange="openUrlTo('${url}&minimalVersion=${minimalVersion}&categoryId=' + $( this ).find( 'option:selected' ).val(), $('div#shiftChooseTable${uiid}').children( 'table' ).first(), '' );">
			<option value="0">Не выбрано</option>
			<c:forEach var="item" items="${allowOnlyCategories}">
				<option value="${item.id}">${item.title}</option>
			</c:forEach>
		</select>
	</div>

	<table class="callboard shiftPanel">

	</table>
</div>
