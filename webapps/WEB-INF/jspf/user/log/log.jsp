<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<div id="${uiid}">
	<form action="${form.requestURI}" onsubmit="return false;" style="display: inline-block;">
		<input type="hidden" name="method" value="log"/>
		<ui:toggle inputName="enable" value="${frd.state}" onChange="$$.ajax.load(this.form, $('#${uiid}').parent())"/>
	</form>
	<textarea style="width: 100%; resize: vertical;" rows="50" wrap="off"> ${frd.log}</textarea>
</div>

<shell:title text="Log (${u.countLines(frd.log)})"/>
<shell:state help="kernel/extension.html#user-session-log" moveSelector="#${uiid} > form"/>

<script>
	$(function () {
		const $content = $('#${uiid}').parent();
		$content.data('onShow', function () {
			$$.ajax.load("${form.requestUrl}", $content);
		});
	});
</script>