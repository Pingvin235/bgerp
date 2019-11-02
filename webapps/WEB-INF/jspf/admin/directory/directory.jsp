<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<script>
	function directoryType(dirName) {
		var result = "patternTitle";
		if (dirName.indexOf("ParameterGroup") > 0) {
			result = "parameterGroup";
		} else if (dirName.indexOf("Parameter") > 0) {
			result = "parameter";
		}
		return result;
	}
</script>

<c:set var="uiid" value="${u:uiid()}"/>

<c:set var="directoryChangedScript">
	var form = $('#${uiid}')[0];
	form.action.value=directoryType( form.directoryId.value ) + 'List';
	openUrlContent( formUrl( form ) );
</c:set>

<html:form action="admin/directory" styleId="${uiid}">
	<input type="hidden" name="action" value="" />
	<html:hidden property="id" value="-1" />
	
	<u:sc>
		<c:set var="valuesHtml">
			<c:forEach var="item" items="${directoryList}">
				<li value="${item.key}">${item.title}</li>
			</c:forEach>
		</c:set>
		
		<c:set var="hiddenName" value="directoryId"/>
		<c:if test="${not empty form.param.directoryId and form.param.directoryId ne 'default'}">
			<c:set var="value" value="${form.param['directoryId']}"/>
		</c:if>
			
		<c:set var="widthTextValue" value="200px"/>
		<c:set var="prefixText" value="Справочник:"/>
		<c:set var="onSelect">
			${directoryChangedScript}
		</c:set>
		<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
	</u:sc>	
</html:form>

<script>
	$(function()
	{
		var $left = $('#title > .status:visible > .wrap > .left');
		$left.html("");
		$('#${uiid}').appendTo( $left );
		
		<c:if test="${empty form.param.directoryId or form.param.directoryId eq 'default'}">
			${directoryChangedScript}
		</c:if>
	})
</script>