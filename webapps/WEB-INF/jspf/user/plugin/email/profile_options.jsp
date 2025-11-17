<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<tr>
	<td nowrap="nowrap">
		${l.l('TagBox ввод Email в сообщениях')}
	</td>
	<td>
		<c:set var="key" value="iface.email.message.tag-box.disable"/>
		<ui:combo-single hiddenName="${key}" value="${ctxUser.pers.get(key, '')}" widthTextValue="200px">
			<jsp:attribute name="valuesHtml">
				<li value="0">${l.l('Yes')}</li>
				<li value="1">${l.l('No')}</li>
			</jsp:attribute>
		</ui:combo-single>
	</td>
</tr>

