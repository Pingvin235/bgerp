<%@ tag pageEncoding="UTF-8" description="Report's More button"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="data" type="org.bgerp.plugin.report.model.Data" description="Report data"%>

<c:set var="menuButtonUiid" value="${u:uiid()}"/>
<ui:button type="more" styleClass="more" id="${menuButtonUiid}"/>

<c:set var="menuUiid" value="${u:uiid()}"/>
<div style="height: 0px; max-height: 0px; width: 0px; max-width: 0px; display: inline-block;">
	<ul id="${menuUiid}" class="menu">
		<jsp:doBody/>
	</ul>
</div>

<script>
	$(() => {
		$$.ui.menuInit($("#${menuButtonUiid}"), $("#${menuUiid}"), "right");
	})
</script>

<div class="more-editor-container" style="display: inline-block;">
	
</div>