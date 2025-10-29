<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="/WEB-INF/jspf/demo.jsp"%>

<h1>Dialogs</h1>

<div>
	<div>
		<button class="btn-white" onclick="$$.shell.message.show('Title', '<b>HTML</b> Text')">$$.shell.message.show</button>
	</div>
	<div>
		<button class="btn-white" onclick="$('#demo-dialog').dialog('open')">Dialog</button>

		<form id="demo-dialog" method="post" style="display: none;">
			<!-- the HTML of the form can be also dynamically loaded with AJAX request -->
			<input type="text" name="field0" class="w100p"/>
			<input type="text" name="field1" class="w100p mt05"/>
			<div class="mt05">
				<button type="submit" class="btn-grey">OK</button>
				<button type="button" onclick="$(this.form).dialog('close')" class="btn-white ml1">${l.l('Cancel')}</button>
			</div>
		</form>
		<script>
			$(function () {
				const $dialog = $("#demo-dialog").dialog({
					modal: true,
					draggable: false,
					resizable: false,
					title: "Demo Dialog",
					// width: 300,
					position: { my: "center top", at: "center top+100px", of: window }
				}).dialog("close");

				$dialog.submit((e) => {
					console.log("Dialog submit");
					$dialog.dialog("close");
					// prevent the form be submitted by browser
					e.preventDefault();
				});
			})
		</script>
	</div>
</div>

<h1>AJAX</h1>

<div>
	<p:check action="${form.requestURI}:formSend">
		<h2>Send Form with Parameter Validation</h2>
		<form action="${form.requestURI}">
			<input type="hidden" name="method" value="formSend"/>
			<input name="title" type="text" size="50" placeholder="Title"/>
			<button type="button" class="btn-grey ml1" onclick="
				$$.ajax.post(this.form).done((result) =>
					$$.shell.message.show(result.data.messageTitle, result.data.messageText)
				)
			">Send</button>
		</form>
	</p:check>

	<p:check action="${form.requestURI}:entityList">
		<h2>Entities</h2>
		<div>
			<c:url var="url" value="${form.requestURI}">
				<c:param name="method" value="entityList"/>
			</c:url>
			<c:import url="${url}"/>
		</div>
	</p:check>
</div>

<shell:title text="Demo Title"/>

<%-- this tag is only needed here for testing --%>
<u:newInstance clazz="java.util.Random" var="random"/>

<c:set var="rnd" value="${u:newInstance0('java.util.Random').nextInt(3)}"/>
<c:choose>
	<c:when test="${rnd == 0}">
		<shell:state text="Demo State" help="project/index.html#mvc-iface-demo"/>
	</c:when>
	<c:when test="${rnd == 1}">
		<c:set var="uiid" value="${u:uiid()}"/>
		<button id="${uiid}" class="btn-white">Demo State Component</button>
		<shell:state moveSelector="#${uiid}"/>
	</c:when>
	<c:otherwise>
		<shell:state error="Demo State Error"/>
	</c:otherwise>
</c:choose>
