<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
<tr>
	<td nowrap="nowrap">
		При открытии договора с контрагентом
	</td>
	<td>
		<u:sc>
			<c:set var="valuesHtml">
				<li value="1">Открыть контрагента</li>
				<li value="2">Открыть только договор</li>
			</c:set>
			<c:set var="key" value="iface.bgbilling.contractOpenMode"/>
			<c:set var="hiddenName" value="${key.replace( '.', '_' )}"/>
			<c:set var="value" value="${u:getFromPers( ctxUser, key, '1' )}"/>
			<c:set var="widthTextValue" value="150px"/>
			<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
		</u:sc>
	</td>
</tr>
 --%>