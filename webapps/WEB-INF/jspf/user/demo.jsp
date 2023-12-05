<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="p05">
	<div class="in-mt1 mt1">
		<div>
			<b>&lt;ui:tag-box&gt;</b><br/>
			Lazy loading from AJAX request<br>
			<ui:tag-box showOptions="1" value="mail1@domain.com,Ivan2 Pupkin <mail2@domain.com>" url="/user/demo.do?action=enumValues" style="width: 30em;"/>
		</div>

		<form class="in-mb05">
			<b>$$.shell.message.show()</b><br/>
			<input name="title" type="text" size="50" placeholder="Title"/><br/>
			<textarea name="text" cols="50" rows="2" placeholder="HTML text"/><br/>
			<button type="button" class="btn-white" onclick="$$.shell.message.show(this.form.title.value, this.form.text.value)">SHOW</button>
		</form>
	</div>
</div>

<div class="p05">
	<b>&lt;ui:combo-perm-tree-check&gt;</b><br/>
	<ui:combo-perm-tree-check permTrees="${permTrees}" prefixText="${l.l('Actions')}:" widthTextValue="15em"/>
</div>

<div class="p05" style="display: flex;">
	<div style="flex: 1 0 50%;">
		<b>&lt;ui:tree-single&gt;</b>
		<ui:tree-single rootNode="${form.response.data.treeRootNode}"
			hiddenName="nodeId" value="3" hiddenNameTitle="nodeTitle" selectableFolder="${false}"
			styleClass="mt1" style="height: 20em; overflow: auto;" />
	</div>
	<div style="flex: 1 0 50%;">
		<b>&lt;ui:tree-single&gt; (selectableFolder)</b>
		<ui:tree-single rootNode="${form.response.data.treeRootNode}"
			hiddenName="nodeId" value="3" hiddenNameTitle="nodeTitle"
			styleClass="mt1" style="height: 20em; overflow: auto;" />
	</div>
</div>

<%@ include file="/WEB-INF/jspf/demo.jsp"%>

<shell:title text="Demo Title"/>
<shell:state text="Demo State"/>
