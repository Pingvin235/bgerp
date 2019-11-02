<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="processId" value="${form.param.processId}"/>
<c:set var="reopenProcessEditorCode">
	openUrlTo( '/usermob/process.do?id=${processId}&wizard=1', $('#processQueueEditProcess') );
</c:set>

<table style="width: 100%;" class="oddeven">
	<c:choose>
		<c:when test="${not empty  form.response.data.list}">
			<c:forEach var="item" items="${form.response.data.list}">
				<tr>
					<td>
						<a href="#UNDEF" 
							onclick="if( deleteLinksWithType( 'process', ${processId}, 'bgbilling-commonContract' ) && addLink( 'process', ${processId}, 'bgbilling-commonContract', ${item.id}, '' ) ){ ${reopenProcessEditorCode } }">
							${item.formatedNumber} (${item.address.value})
						</a>
					</td>			
				</tr>
			</c:forEach>
		</c:when>
		<c:otherwise>
			<tr>
				<td>Единых договоров не найдено.</td>
			</tr>	
		</c:otherwise>	
	</c:choose>
</table>