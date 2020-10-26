<%@ tag body-content="empty" pageEncoding="UTF-8" description="Ссылка на открытие процесса"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="pageFormSelectorFunc" description="jQuery selector function of form"%>
<%@ attribute name="pageFormSelector" description="jQuery selector of form (only string)"%>
<%@ attribute name="pageFormId" description="CSS ID of form"%>
<%@ attribute name="nextCommand" description="JS command after page switch"%>

<c:set var="page" value="${form.response.data['page']}"/>

<c:if test="${not empty page}">
	<c:choose>
		<%-- выбор формы через селектор - функцию--%>
		<c:when test="${not empty pageFormSelectorFunc}">
			<c:set var="pageControlForm" value="${pageFormSelectorFunc}[0]"/>
		</c:when>
		<%-- выбор формы через селектор --%>
		<c:when test="${not empty pageFormSelector}">
			<c:set var="pageControlForm" value="$('${pageFormSelector}')[0]"/>
		</c:when>
		<%-- выбор формы по id--%>
		<c:when test="${not empty pageFormId}">
			<c:set var="pageControlForm" value="document.getElementById( '${pageFormId}' )	"/>
			<c:if test="${empty nextCommand}">
				<c:set var="nextCommand" value="; openUrlTo( formUrl( ${pageControlForm} ), $(this.parentNode.parentNode.parentNode.parentNode.parentNode) )"/>
			</c:if>				
		</c:when>
		<%-- промотчик страниц прямо в форме --%>
		<c:otherwise>
			<c:set var="pageControlForm" value="this.form"/>
			<c:if test="${empty nextCommand}">
				<c:set var="nextCommand" value="; openUrlContent( formUrl( ${pageControlForm} ) )"/>
			</c:if>	
		</c:otherwise>
	</c:choose>		
	
	<c:set var="command" value="toPage(${pageControlForm},"/>

	<div style="display: inline-block; float: right;" class="pt05 pb05">
		<table class="page" align="right" cellpadding="0" cellspacing="0" border="0">
			<tr>
				<td>
					<button type="button" name="pageControlRefreshButton" onclick="${command} ${page.pageIndex},  ${page.pageSize}, '${pagePrefix}' ) ${nextCommand}">
						<span class="ui-icon ui-icon-refresh"></span>
					</button>
				</td>
				<td nowrap="nowrap">${l.l('Записей')}: ${page.recordCount}&nbsp;&nbsp;&nbsp;${l.l('Стр.')}: </td>
				<c:set var="previousPageIndex" value="${page.pageIndex - 1}"/><c:if test="${previousPageIndex < 1}"><c:set var="previousPageIndex" value="1"/></c:if>
				<td>
					<button type="button" onclick="${command} 1, ${page.pageSize}, '${pagePrefix}' ) ${nextCommand}">
						<span class="ui-icon ui-icon-seek-first"></span>
					</button>
				</td>
				<td>
					<button type="button" onclick="${command} ${previousPageIndex}, ${page.pageSize}, '${pagePrefix}' ) ${nextCommand}">
						<span class="ui-icon ui-icon-seek-prev"></span>
					</button>
				</td>
				<td nowrap="nowrap">
					<select name="pageIndex" class="pageIndex" onchange="${command} this.value, ${page.pageSize}, '${pagePrefix}' ) ${nextCommand}">
						<c:forEach var="i" begin="1" end="${page.pageCount}">
							<c:choose>
								<c:when test="${page.pageIndex == i}"><option value="${i}" selected>${i}</option></c:when>
								<c:otherwise><option value="${i}">${i}</option></c:otherwise>
							</c:choose>
						</c:forEach>
					</select>
				</td>
				<td nowrap="nowrap">${l.l('из')} ${page.pageCount}</td>
				<td nowrap="nowrap">
				[<select name="pageSize" class="pageSize" onchange="${command} 1, this.value, '${pagePrefix}' ) ${nextCommand}">
					<c:forTokens var="item" items="2 5 10 15 20 25 30 50 100 200 300" delims=" ">
						<c:choose>
							<c:when test="${page.pageSize == item}"><option value="${item}" selected>${item}</option></c:when>
							<c:otherwise><option value="${item}">${item}</option></c:otherwise>
						</c:choose>
					</c:forTokens>
				</select>]</td>
				<c:set var="nextPageIndex" value="${page.pageIndex + 1}"/><c:if test="${nextPageIndex > page.pageCount}"><c:set var="nextPageIndex" value="${page.pageCount}"/></c:if>
				<td>
					<button type="button" onclick="${command} ${nextPageIndex},  ${page.pageSize}, '${pagePrefix}' ) ${nextCommand}">
						<span class="ui-icon ui-icon-seek-next"></span>
					</button>
				</td>
				<td>
					<button type="button" onclick="${command} ${page.pageCount}, ${page.pageSize}, '${pagePrefix}' ) ${nextCommand}">
						<span class="ui-icon ui-icon-seek-end"></span>
					</button>
				</td>
			</tr>			
		</table>
	</div>	
</c:if>