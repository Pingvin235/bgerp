<%@ tag body-content="empty" pageEncoding="UTF-8" description="Link to a Process Creation page"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="pageFormSelectorFunc" description="jQuery selector function of form"%>
<%@ attribute name="pageFormSelector" description="jQuery selector of form (only string)"%>
<%@ attribute name="pageFormId" description="CSS ID of form"%>
<%@ attribute name="nextCommand" description="JS command after page switch"%>
<%@ attribute name="styleClass" description="CSS classes of external DIV"%>

<c:set var="page" value="${form.response.data['page']}"/>

<c:if test="${not empty page}">
	<c:choose>
		<%-- picking a form via selector-function--%>
		<c:when test="${not empty pageFormSelectorFunc}">
			<c:set var="pageControlForm" value="${pageFormSelectorFunc}[0]"/>
		</c:when>
		<%-- picking a form via selector --%>
		<c:when test="${not empty pageFormSelector}">
			<c:set var="pageControlForm" value="$('${pageFormSelector}')[0]"/>
		</c:when>
		<%-- picking a form via id--%>
		<c:when test="${not empty pageFormId}">
			<c:set var="pageControlForm" value="document.getElementById( '${pageFormId}' )"/>
			<c:if test="${empty nextCommand}">
				<c:set var="nextCommand" value="; openUrlTo( formUrl( ${pageControlForm} ), $(this.parentNode.parentNode.parentNode.parentNode.parentNode) )"/>
			</c:if>
		</c:when>
		<%-- pagination inside a form --%>
		<c:otherwise>
			<c:set var="pageControlForm" value="this.form"/>
			<c:if test="${empty nextCommand}">
				<c:set var="nextCommand" value="; openUrlContent( formUrl( ${pageControlForm} ) )"/>
			</c:if>
		</c:otherwise>
	</c:choose>

	<c:set var="command" value="toPage(${pageControlForm},"/>

	<div style="display: inline-block; float: right;" class="pt05 pb05 ${styleClass}">
		<table class="page" align="right" cellpadding="" cellspacing="0" border="0">
			<tr>
				<td>
					<button type="button" class="btn-white-hover btn-small btn-icon" name="pageControlRefreshButton" onclick="${command} ${page.pageIndex},  ${page.pageSize}, '${pagePrefix}' ) ${nextCommand}">
						<i class="ti-reload"></i>
					</button>
				</td>
				<td nowrap="nowrap">&nbsp;${l.l('Записей')}: <b>${page.recordCount}</b></td>
				<c:if test="${page.recordCount gt 0}">
					<c:set var="previousPageIndex" value="${page.pageIndex - 1}"/><c:if test="${previousPageIndex < 1}"><c:set var="previousPageIndex" value="1"/></c:if>
					<td>
						&nbsp;${l.l('Стр.')}: 
						<button type="button" class="btn-white btn-small btn-icon" onclick="${command} 1, ${page.pageSize}, '${pagePrefix}' ) ${nextCommand}">
							<i class="ti-control-skip-backward"></i>
						</button>
					</td>
					<td>
						<button type="button" class="btn-white btn-small btn-icon" onclick="${command} ${previousPageIndex}, ${page.pageSize}, '${pagePrefix}' ) ${nextCommand}">
							<i class="ti-control-backward"></i>
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
					<td nowrap="nowrap">&nbsp;${l.l('из')}&nbsp;${page.pageCount}&nbsp;</td>
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
						<button type="button" class="btn-white btn-small btn-icon" onclick="${command} ${nextPageIndex},  ${page.pageSize}, '${pagePrefix}' ) ${nextCommand}">
							<i class="ti-control-forward"></i>
						</button>
					</td>
					<td>
						<button type="button" class="btn-white btn-small btn-icon" onclick="${command} ${page.pageCount}, ${page.pageSize}, '${pagePrefix}' ) ${nextCommand}">
							<i class="ti-control-skip-forward"></i>
						</button>
					</td>
				</c:if>
			</tr>
		</table>
	</div>
</c:if>