<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div style="display: table; width: 100%;">
	<div class="in-table-cell">
		<div style="vertical-align: top; width: 30px;" id="typeSelectContainer">
			<h2>${l.l('Type')}</h2>
			<%-- here will be placed type selection --%>
		</div>
		<%@ include file="/WEB-INF/jspf/user/message/process/edit/tags.jsp"%>
	</div>

	<div>
		<h2>Сообщение</h2>
		<textarea rows="20" style="width: 100%; resize: vertical;" name="text" class="tabsupport">${message.text}</textarea>
		<span class="hint">${l.l('message.edit.hint')}</span>
	</div>
	<div>
		<%@ include file="/WEB-INF/jspf/user/message/process/edit/upload_list.jsp"%>
	</div>

	<%@ include file="/WEB-INF/jspf/user/message/process/edit/ok_cancel.jsp"%>
</div>
