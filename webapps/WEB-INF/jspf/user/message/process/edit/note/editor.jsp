<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div style="display: table; width: 100%;">
	<div class="in-table-cell">
		<div style="vertical-align: top; width: 30px;" id="typeSelectContainer">
			<h2>${l.l('Type')}</h2>
			<%-- here will be placed type selection --%>
		</div>
		<%@ include file="/WEB-INF/jspf/user/message/process_message_edit_tags.jsp"%>
	</div>

	<%@ include file="/WEB-INF/jspf/user/message/process_message_edit_text.jsp"%>
	<div>
		<%@ include file="/WEB-INF/jspf/user/message/process_message_edit_upload_list.jsp"%>
	</div>

	<%@ include file="/WEB-INF/jspf/user/message/process/edit/ok_cancel.jsp"%>
</div>
