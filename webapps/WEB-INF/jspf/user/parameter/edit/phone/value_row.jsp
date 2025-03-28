<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<tr>
	<td><ui:input-decimal name="phone" digits="0" value="${item.phone}" styleClass="w100p" /></td>
	<td><input type="text" name="comment" value="${item.comment}" class="w100p"/></td>
	<td><button type="button" class="btn-white btn-small icon" onclick="$$.param.phone.delValue(this)"><i class="ti-trash"></i></button></td>
</tr>
