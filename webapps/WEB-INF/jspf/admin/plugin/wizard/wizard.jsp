<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="indent" style="margin:5px;width:100%">

	<c:set var="wizardList" value="${form.response.data.wizardList}"/>
	Мастер: 
	<select id="wizardSelect" onchange="wizard.changeWizard()">
		<option value="-1" selected="selected">---</option>
		<c:forEach var="wizard" items="${wizardList}">
			<option value="${wizard.id}">${wizard.title}</option>
		</c:forEach>
	</select>
	
	<script>wizard.changeWizard()</script>
	
	<input type="button" value="Редактировать" onclick="wizard.editWizard()"/>
	||
	<label for="normalMode">⬉ Просмотр</label>
	<input type="radio" id="normalMode" name="wizardModeGroup" value="normalMode" checked="checked"/>
	<label for="linkMode">➜ Связи</label>
	<input type="radio" id="linkMode" name="wizardModeGroup" value="linkMode"/>
	<label for="nodeMode">□ Состояния</label>
	<input type="radio" id="nodeMode" name="wizardModeGroup" value="nodeMode"/>
	||
	Для скроллинга воспользуйтесь стрелками на клавиатуре, либо в режиме просмотра скроллинг можно осуществлять мышкой.
	
	<script>
		$( "#normalMode" ).button();
		$( "#linkMode" ).button();
		$( "#nodeMode" ).button();
		$( "input[name=wizardModeGroup]" ).change( function() { wizard.mode = $( this ).val(); } );
	</script>
	
	<div id="editWizardDialog" title="Редактирование">
		<form action="">
		<div>
			<input name="id" type="hidden" value="-1"/>
			Название:
			<input name="title" type="text" style="width: 100%;"/><br/>
			ID начального состояния:
			<input name="startNodeId" type="text" size="3" style="width: 100%;"/>
		</div>
		<div style="text-align:center;">
			<input type="button" value="Сохранить" onclick="wizard.updateWizard()"/>
		</div>
		</form>
	</div>
	<script>$(function() { $("#editWizardDialog").dialog( { autoOpen: false, modal: true, width: 800, height: 500 } ); });</script>
	
	<div id="editNodeDialog">
		<form action="">
			<input name="id" type="hidden" value="-1"/>
			<input name="wizardId" type="hidden" value="-1"/>
			<input name="x" type="hidden" value="0"/>
			<input name="y" type="hidden" value="0"/>
			Название:
			<input name="title" type="text" value="" style="width: 100%;"/><br/>
			Имя класса:
			<input name="className" type="text" value="" style="width: 100%;"/><br/>
			JSP:
			<input name="jsp" type="text" value="" style="width: 100%;"/><br/>
			Конфигурация:<br/>
			<textarea name="config" style="width: 100%;" rows="20"></textarea><br/>
			<div style="text-align:center;">
				<input type="button" value="OK" onclick="wizard.updateNode()"/>
				<input type="button" value="Удалить" onclick="if( confirm( 'Удалить?' ) ) { wizard.deleteNode(); }">
			</div>
		</form>
	</div>
	<script>$(function() { $("#editNodeDialog").dialog( { autoOpen: false, modal: true, width: 800, height: 600 } ); });</script>
	
	<div id="editLinkDialog" title="Новая связь">
		<form action="">
			<input name="id" type="hidden" value="-1"/>
			<input name="wizardId" type="hidden" value="-1"/>
			<input name="startNodeId" type="hidden" value="-1"/>
			<input name="endNodeId" type="hidden" value="-1"/>
			Название:
			<input name="title" type="text" value="" style="width: 100%;"/>
			<div style="text-align:center;">
				<input type="button" value="Ok" onclick="wizard.updateLink()"/>
				<input type="button" value="Удалить" onclick="if( confirm( 'Удалить?' ) ) { wizard.deleteLink() }"/>
			</div>
		</form>
	</div>
	<script>$(function() { $("#editLinkDialog").dialog( { autoOpen: false, modal: true, width: 800, height: 600 } ); });</script>
	
	<div>
		<canvas id="wizardCanvas" width="1850px" height="770px"></canvas>
	</div>
	
	<script>
		var canvas = document.getElementById( "wizardCanvas" );
		canvas.addEventListener( "mousemove", wizard.mouseMove );
		canvas.addEventListener( "mousedown", wizard.mouseDown );
		canvas.addEventListener( "mouseup", wizard.mouseUp );
		canvas.addEventListener( "dblclick", wizard.dblclick );
	    //document.addEventListener( "keydown", wizard.keydown, false);
		wizard.draw();
	</script>
</div>