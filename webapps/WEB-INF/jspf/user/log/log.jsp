<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<div id="${uiid}">
	<form action="/user/log.do" onsubmit="return false;" style="display: inline-block;">
		<input type="hidden" name="action" value="log"/>
		<ui:toggle inputName="enable" value="${form.response.data.state}" onChange="$$.ajax.load(this.form, $('#${uiid}').parent())"/>
	</form>
	<textarea style="width: 100%; resize: vertical;" rows="50" wrap="off"> ${form.response.data.log}</textarea>
</div>

<shell:title text="Log"/>
<shell:state help="kernel/extension.html#log-dyn" moveSelector="#${uiid} > form"/>

<script>
	$(function () {
		const $log = $('#${uiid}').parent();
		$log.data('onShow', function () {
			$$.ajax.load("/user/log.do", $log);
		});
	});
</script>