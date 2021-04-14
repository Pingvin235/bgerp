<%@ tag pageEncoding="UTF-8" description="Report's More button"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="data" type="org.bgerp.plugin.report.model.Data" description="Report data"%>

<c:set var="menuButtonUiid" value="${u:uiid()}"/>
<ui:button type="more" styleClass="more" id="${menuButtonUiid}"/>

<c:set var="menuUiid" value="${u:uiid()}"/>
<div style="height: 0px; max-height: 0px; width: 0px; max-width: 0px; display: inline-block;">
	<ul id="${menuUiid}" class="menu">
		<!-- TODO: Export to CSV, JSON -->
		<c:forEach var="chart" items="${data.action.charts}" varStatus="status">
			<li id="${u:uiid()}">
				<c:set var="className" value="${chart.getClass().getSimpleName()}"/>
				<c:set var="icon">
					<c:if test="${className.endsWith('Bar')}">ti-bar-chart</c:if>
					<c:if test="${className.endsWith('Pie')}">ti-pie-chart</c:if>
				</c:set>

				<c:set var="title"><i class="${icon}"></i> ${chart.getTitle(l)}</c:set>
				<a href="#" onclick="$$.report.more(this); $$.report.chart(this, ${status.count}); return false;">
					${title}
					<div style="display: none;" class="more-editor">
						<b>${title}</b>
						<button type="button" class="btn-white icon ml05" onclick="$$.report.less(this)"><i class="ti-close"></i></button>
					</div>
				</a>
			</li>
		</c:forEach>
	</ul>
</div>

<script>
	$(() => {
		$$.ui.menuInit($("#${menuButtonUiid}"), $("#${menuUiid}"), "right");
	})
</script>

<div class="more-editor-container" style="display: inline-block;">
	
</div>