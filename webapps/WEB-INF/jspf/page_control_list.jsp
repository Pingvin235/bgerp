<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<table class="page" cellpadding="0" cellspacing="0" border="0">
		<tr>
			<td nowrap="nowrap">${l.l('Записей')}: ${list.size()}&nbsp;&nbsp;&nbsp;${l.l('Стр')}.: </td>
			<c:set var="previousPageIndex" value="${page.pageIndex - 1}"/><c:if test="${previousPageIndex < 1}"><c:set var="previousPageIndex" value="1"/></c:if>
			<td><input type="button" class="pageButton" value="|&lt;" onclick="${command} 1, ${page.pageSize}, '${pagePrefix}' ) ${nextCommand}"/></td>
			<td><input type="button" class="pageButton" value="&lt;" onclick="${command} ${previousPageIndex}, ${page.pageSize}, '${pagePrefix}' ) ${nextCommand}"/></td>
			<td><input type="text" name="pageIndex" class="pageIndex" value="${page.pageIndex}"/></td>
			<td nowrap="nowrap">${l.l('из')} ${page.pageCount}</td>
			<td nowrap="nowrap">
			[<select name="pageSize" class="pageSize" onchange="${command} 1, this.value, '${pagePrefix}' ) ${nextCommand}">
				<c:forTokens var="item" items="2 5 10 15 20 25 30 50 100" delims=" ">
					<c:choose>
						<c:when test="${page.pageSize == item}"><option value="${item}" selected>${item}</option></c:when>
						<c:otherwise><option value="${item}">${item}</option></c:otherwise>
					</c:choose>
				</c:forTokens>
			</select>]</td>
			<c:set var="nextPageIndex" value="${page.pageIndex + 1}"/><c:if test="${nextPageIndex > page.pageCount}"><c:set var="nextPageIndex" value="${page.pageCount}"/></c:if>
			<td><input type="button" class="pageButton" value="&gt;" onclick="${command} ${nextPageIndex},  ${page.pageSize}, '${pagePrefix}' ) ${nextCommand}"/></td>
			<td><input type="button" class="pageButton" value="&gt;|" onclick="${command} ${page.pageCount}, ${page.pageSize}, '${pagePrefix}' ) ${nextCommand}"/></td>
		</tr>			
	</table>