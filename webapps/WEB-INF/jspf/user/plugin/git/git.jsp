<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h2>${l.l('Create branch')}</h2>
<div class="box cmd">
	${u:htmlEncode(form.response.data.commandBranchCreate)}
</div>

<h2>${l.l('Synch branch')}</h2>
<div class="box cmd">
	${u:htmlEncode(form.response.data.commandBranchSync)}
</div>

<h2>${l.l('Accept branch')}</h2>
<div class="box cmd">
	${u:htmlEncode(form.response.data.commandBranchAccept)}
</div>
