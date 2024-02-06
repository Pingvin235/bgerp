<%@ tag body-content="empty" pageEncoding="UTF-8" description="Link to a Process Creation page"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="pageFormSelector" description="jQuery selector of form (only string)"%>
<%@ attribute name="pageFormId" description="CSS ID of form"%>
<%@ attribute name="nextCommand" description="JS command after page switch"%>
<%@ attribute name="styleClass" description="CSS classes of external DIV"%>

<c:set var="page" value="${form.response.data['page']}"/>

<c:if test="${not empty page}">
	<c:choose>
		<%-- picking a form via selector --%>
		<c:when test="${not empty pageFormSelector}">
			<c:set var="pageControlForm" value="$('${pageFormSelector}')[0]"/>
		</c:when>
		<%-- picking a form via id--%>
		<c:when test="${not empty pageFormId}">
			<c:set var="pageControlForm" value="document.getElementById('${pageFormId}')"/>
		</c:when>
		<%-- pagination inside a form --%>
		<c:otherwise>
			<c:set var="pageControlForm" value="this.form"/>
		</c:otherwise>
	</c:choose>

	<c:if test="${empty nextCommand}">
		<c:set var="nextCommand" value="; $$.ajax.load(this.form, $(this.form).parent())"/>
	</c:if>

	<c:set var="command" value="toPage(${pageControlForm},"/>

	<div style="display: inline-block; float: right;" class="page pt05 pb05 ${styleClass}">
		<button type="button" class="btn-white-hover btn-small btn-icon" name="pageControlRefreshButton" onclick="${command} ${page.pageIndex},  ${page.pageSize}, '${pagePrefix}' ) ${nextCommand}">
			<i class="ti-reload"></i>
		</button>
		${l.l('Records')}: <b>${page.recordCount}</b>
		<c:if test="${page.recordCount gt 0}">
			<c:set var="previousPageIndex" value="${page.pageIndex - 1}"/><c:if test="${previousPageIndex < 1}"><c:set var="previousPageIndex" value="1"/></c:if>

			${l.l('Стр.')}:
			<button type="button" class="btn-white btn-small btn-icon" onclick="${command} 1, ${page.pageSize}, '${pagePrefix}' ) ${nextCommand}">
				<i class="ti-control-skip-backward"></i>
			</button>

			<button type="button" class="btn-white btn-small btn-icon" onclick="${command} ${previousPageIndex}, ${page.pageSize}, '${pagePrefix}' ) ${nextCommand}">
				<i class="ti-control-backward"></i>
			</button>

			<select name="pageIndex" class="pageIndex" onchange="${command} this.value, ${page.pageSize}, '${pagePrefix}' ) ${nextCommand}">
				<c:forEach var="i" begin="1" end="${page.pageCount}">
					<c:choose>
						<c:when test="${page.pageIndex == i}"><option value="${i}" selected>${i}</option></c:when>
						<c:otherwise><option value="${i}">${i}</option></c:otherwise>
					</c:choose>
				</c:forEach>
			</select>

			${l.l('из')}&nbsp;${page.pageCount}

			[<select name="pageSize" class="pageSize" onchange="${command} 1, this.value, '${pagePrefix}' ) ${nextCommand}">
				<c:forTokens var="item" items="2 5 10 15 20 25 30 50 100 200 300" delims=" ">
					<c:choose>
						<c:when test="${page.pageSize == item}"><option value="${item}" selected>${item}</option></c:when>
						<c:otherwise><option value="${item}">${item}</option></c:otherwise>
					</c:choose>
				</c:forTokens>
			</select>]

			<c:set var="nextPageIndex" value="${page.pageIndex + 1}"/><c:if test="${nextPageIndex > page.pageCount}"><c:set var="nextPageIndex" value="${page.pageCount}"/></c:if>

			<button type="button" class="btn-white btn-small btn-icon" onclick="${command} ${nextPageIndex},  ${page.pageSize}, '${pagePrefix}' ) ${nextCommand}">
				<i class="ti-control-forward"></i>
			</button>

			<button type="button" class="btn-white btn-small btn-icon" onclick="${command} ${page.pageCount}, ${page.pageSize}, '${pagePrefix}' ) ${nextCommand}">
				<i class="ti-control-skip-forward"></i>
			</button>
		</c:if>
	</div>
</c:if>