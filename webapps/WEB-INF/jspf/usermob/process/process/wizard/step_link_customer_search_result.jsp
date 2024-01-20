<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="processId" value="${form.param.processId}"/>

<c:set var="reopenProcessEditorCode" value="$$.ajax.load('process.do?id=${processId}&wizard=1', $('#${form.param.returnChildUiid}').parent());"/>

<table style="width: 100%;" class="oddeven mt05">
	<c:choose>
		<c:when test="${not empty  frd.list}">
			<c:forEach var="item" items="${frd.list}">
				<tr>
					<td>
						<a href="#"
							onclick="if( deleteLinksWithType( 'process', ${processId}, 'customer' ) && addLink( 'process', ${processId}, 'customer', ${item.id}, '${u.escapeXml( item.title )}' ) ){ ${reopenProcessEditorCode } }; return false;">
							${item.title} (${item.reference})
						</a>
					</td>
				</tr>
			</c:forEach>
		</c:when>
		<c:otherwise>
			<tr>
				<td>${l.l('Контрагентов с таким наименованием не найдено.')}</td>
			</tr>
		</c:otherwise>
	</c:choose>
</table>