<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<b>RADIUS лог:</b>
<button style="border:none; background:transparent; cursor: pointer; text-decoration:underline;" onclick="$(this).parent().empty();">[закрыть]</button>
<div style="overflow: auto; width: inherit; height: 200px;" class="box">
		${form.response.data.radiusLog}
</div>