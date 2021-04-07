<%@ tag pageEncoding="UTF-8" description="Report's Bar Chart"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="data" type="org.bgerp.plugin.report.model.Data" required="true" description="Report data"%>
<%@ attribute name="categories" required="true" description="Comma separated list of allowed category column IDs"%>
<%@ attribute name="values" required="true" description="Comma separated list of allowed value column IDs"%>

<li id="${u:uiid()}">
	<a href="#" onclick="$$.report.more(this); return false;">${l.l('Столбчатая диаграмма')}
		<div style="display: none;" class="more-editor">
			<button type="button" class="btn-grey icon ml1" onclick="$$.report.chart(this, 'bar')"><i class="ti-bar-chart"></i></button>
			<button type="button" class="btn-white icon ml05" onclick="$$.report.less(this)"><i class="ti-close"></i></button>
		</div>
	</a>
</li>
