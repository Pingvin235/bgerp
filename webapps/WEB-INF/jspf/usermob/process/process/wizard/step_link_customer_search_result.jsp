<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="processId" value="${form.param.processId}"/>

<c:set var="reopenProcessEditorCode" value="openUrlToParent( 'process.do?id=${processId}&wizard=1', $('#${form.param.returnChildUiid}') );"/>

<table style="width: 100%;" class="oddeven mt05">
	<c:choose>
		<c:when test="${not empty  form.response.data.list}">
			<c:forEach var="item" items="${form.response.data.list}">
				<tr>
					<td>
						<a href="#UNDEF" 
							onclick="if( deleteLinksWithType( 'process', ${processId}, 'customer' ) && addLink( 'process', ${processId}, 'customer', ${item.id}, '${fn:escapeXml( item.title )}' ) ){ ${reopenProcessEditorCode } }; return false;">
							${item.title} (${item.reference})
						</a>
					</td>			
				</tr>
			</c:forEach>
		</c:when>
		<c:otherwise>
			<tr>
				<td>Контрагентов с таким наименованием не найдено.</td>
			</tr>	
		</c:otherwise>	
	</c:choose>
</table>