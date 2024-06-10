<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h1>Headers and Text</h1>

<div>
	<h1>Header Level 1 <span class="normal">[<a href="#">action</a>]</span></h1>

	<h2>Header Level 2 <span class="normal">[<a href="#">action</a>]</span></h2>

	<div>
		<b>Header-like bold Text <span class="normal">[<a href="#">action</a>]</span></b>
		<span class="bold">The same bold but using CSS class</span>
	</div>

	<div class="tt">Title Text</div>
</div>

<h1>Code Block</h1>

<div class="cmd">
cmd do && cmd undo
</div>

<h1>Buttons</h1>

<div>
	<h2>Regular</h2>

	<div>
		<ui:button type="add"/>
		<ui:button type="ok"/>
		<ui:button type="cancel"/>
		<ui:button type="close"/>
		<ui:button type="out"/>
		<ui:button type="more"/>

		<button class="btn-white" title="White buttons don't cause data retrieving/modification requests to the server side">Text Button</button>

		<button class="btn-grey" title="Grey buttons cause data retrieving/modification requests to the server side">Text Button</button>

		<button class="btn-blue" onclick="$(this).toggleClass('btn-white btn-blue')" title="Blue-white buttons can be used as toggles">Text Button</button>

		<ui:toggle inputName="test" value="0" title="Toggle"
			prefixText="Toggle" textOn="ON" textOff="OFF"
			onChange="console.log('Is checked: ', this.checked);"/>
	</div>

	<h2>Small (for tables)</h2>

	<div>
		<ui:button type="edit" styleClass="btn-small"/>
		<ui:button type="del" styleClass="btn-small"/>
		<ui:button type="cut" styleClass="btn-small"/>

		<button class="btn-white btn-small"><i class="ti-settings"></i></button>

		<ui:toggle styleClass="btn-toggle-small" inputName="test" value="1"
			prefixText="Prefix"
			textOn="ON" textOff="OFF"
			onChange="console.log('Is checked: ', this.checked);"/>
	</div>

	<h2>Toolbar</h2>

	<div>
		<button class="btn-green btn-start icon"><i class="ti-menu"></i></button>

		<div class="btn-white btn-task">
			<span class="title">Tool 1111111111111111111111</span>
			<span class="icon-close" onclick="alert('Close')"></span>
		</div>

		<div class="btn-blue btn-task-active">
			<span class="title">Tool is ruuuuuuuuuuuuuuuuuuuuuuuuuuuuuuning</span>
			<span class="icon-close" onclick="alert('Close')"></span>
		</div>
	</div>
</div>

<h1>Menu</h1>

<div>
	<b>&lt;ui:popup-menu&gt;</b>

	<c:set var="uiid" value="${u:uiid()}"/>
	<ui:popup-menu id="${uiid}">
		<li>
			<a href="#" onclick="alert('Item 1 pressed!')">Item 1</a>
			<ul>
				<li><a href="#" onclick="alert('Item 1/1 pressed!')">Item 1/1</a></li>
			</ul>
		</li>
		<li><a href="#" onclick="alert('Item 2 pressed!')"><i class="ti-alarm-clock"></i> Item 2</a></li>
		<li><a href="#" onclick="alert('Item 3 pressed!')"><img src="/img/fugue/plug-disconnect.png"/> Item 3</a></li>
	</ui:popup-menu>
	<ui:button type="more" onclick="$$.ui.menuInit($(this), $('#${uiid}'), 'left', true);"/>
</div>

<h1>Text Inputs</h1>

<div>
	<div>
		<b>&lt;ui:ip&gt;</b><br/>
		<ui:ip paramName="ip"/>
	</div>

	<div>
		<b>Dot separated decimal</b><br/>
		<input type="text" onkeydown="return isNumberKey(event)" size="10"/>
		<span class="hint">Hint with some additional info regarding the input</span>
	</div>

	<div>
		<b>&lt;ui:input-text&gt;</b><br/>
		<ui:input-text name="text" onSelect="alert('Selected: ' + this.value)"/>
	</div>
</div>

<h1>Date and Time</h1>

<div>
	<form action="${form.httpRequestURI}">
		<b>&lt;ui:date-time&gt;</b><br/>

		ymd:
		<ui:date-time type="ymd" paramName="date"/>

		ymdh:
		<ui:date-time type="ymdh" paramName="dateh"/>

		ymdhm:
		<ui:date-time type="ymdhm" paramName="datehm"/>

		ymdhms:
		<c:set var="uiid" value="${u:uiid()}"/>
		<input type="text" id="${uiid}"/>
		<ui:date-time type="ymdhms" selector="#${uiid}"/>

		<button class="btn-white ml1" type="button" onclick="console.log(this.form.date.value)">PRINT VALUES TO LOG</button>
	</form>

	<div>
		<b>&lt;ui:date-month-days&gt;</b><br/>
		<ui:date-month-days/>
	</div>

	<div>
		<b>&lt;ui:date-month&gt;</b><br/>
		<ui:date-month/>
	</div>
</div>

<h1>Selects</h1>

<div>
	<div>
		<b>&lt;ui:combo-single&gt;</b><br/>

		<c:set var="onSelect" value="console.log('this=', this, '$hidden=', $hidden, 'item=', item); alert('A value is chosen, see console log')"/>

		<ui:combo-single
			hiddenName="param" value="2" prefixText="Value:" widthTextValue="12em" onSelect="${onSelect}" showFilter="true">
			<jsp:attribute name="valuesHtml">
				<li value="1">First (current)</li>
				<li value="2 test">Second</li>
				<li value="3">Third with a loooooooooooooooooooong teeeeeeeeeeeeext</li>
				<li value="4">Fourth</li>
			</jsp:attribute>
		</ui:combo-single>

		<ui:combo-single
			hiddenName="param" value="2" style="width: 12em;" onSelect="${onSelect}">
			<jsp:attribute name="valuesHtml">
				<li value="1">First</li>
				<li value="2">Second (current)</li>
				<li value="3">Third</li>
				<li value="4">Fourth</li>
			</jsp:attribute>
		</ui:combo-single>

		<u:sc>
			<ui:combo-single hiddenName="param" widthTextValue="12em" list="${frd.comboSingeList}" onSelect="${onSelect}">
				<jsp:attribute name="valuesHtml">
					<li value="-1">-- select --</li>
				</jsp:attribute>
			</ui:combo-single>
		</u:sc>

		<div style="width: 20em; display: inline-block;">
			<ui:combo-single hiddenName="param" prefixText="Long value selected:" style="width: 100%;">
				<jsp:attribute name="valuesHtml">
					<li value="-1">This is a looooong text, has to be correctly cut from right when selected.</li>
				</jsp:attribute>
			</ui:combo-single>
		</div>
	</div>

	<div>
		<b>&lt;ui:combo-check&gt;</b><br/>

		<ui:combo-check prefixText="Status:" paramName="param" list="${frd.comboCheckList}" values="${frd.comboCheckValues}"
			onChange="console.log('this=', this); alert('Values are chosen, see console log')"
			widthTextValue="15em" showFilter="true"/>
	</div>

	<div>
		<b>&lt;ui:select-single&gt;</b><br/>

		<c:set var="onSelect" value="console.log('this=', this, '$hidden=', $hidden, '$input=', $input); alert('A value is chosen, see console log')"/>

		<ui:select-single hiddenName="param" value="2" style="width: 10em;"
			list="${frd.selectSingle1List}" availableIdSet="${frd.selectSingle1AvailableIdSet}"
			onSelect="${onSelect}"/>

		<ui:select-single hiddenName="param" style="width: 15em;" map="${frd.selectSingle2Map}" availableIdList="${frd.selectSingle2AvailableIdList}"
			onSelect="${onSelect}"/>

		<ui:select-single hiddenName="param" value="1" style="width: 10em;" list="${frd.selectSingle3List}" showId="1" showComment="1"
			onSelect="${onSelect}"/>

		<ui:select-single hiddenName="param" value="2" style="width: 10em;" list="${frd.selectSingle4List}" inputAttrs="disabled='1'"
			onSelect="alert('Must not be selectable!')"/>
	</div>

	<div>
		<b>&lt;ui:select-mult&gt;</b><br/>
		<ui:select-mult
			showId="true"
			hiddenName="param" style="width: 12em;"
			list="${frd.selectMult1List}" values="${frd.selectMult1Values}"/>

		With position changing (preliminary order must be Second before First):
		<ui:select-mult hiddenName="param" style="width: 12em;" moveOn="true"
			list="${frd.selectMult2List}" map="${frd.selectMult2Map}" values="${frd.selectMult2Values}"/>
	</div>

	<div>
		<b>&lt;ui:tag-box&gt;</b><br/>

		Directly provided values<br>
		<ui:tag-box showOptions="1" choices="first,second,third"/>

		<ui:when type="user">
			<br/>Lazy loading from AJAX request<br>
			<ui:tag-box showOptions="1" value="mail1@domain.com,Ivan2 Pupkin <mail2@domain.com>" url="${form.httpRequestURI}?method=enumValues" style="width: 30em;"/>
		</ui:when>
	</div>

	<div>
		<h2>Column Alignment</h2>

		<input type="text" style="width: 10em;"/><br/>

		<ui:combo-single hiddenName="param" value="2" style="width: 10em;">
			<jsp:attribute name="valuesHtml">
				<li value="1">First</li>
				<li value="2">Second</li>
			</jsp:attribute>
		</ui:combo-single><br/>

		<ui:select-single hiddenName="param" value="2" style="width: 10em;" list="${frd.selectSingle1List}"
			onSelect="console.log('this=', this, '$hidden=', $hidden); alert('Value is chosen, see console log')"/>
	</div>
</div>

<h1>Trees</h1>

<div>
	<h2>Flex Layout</h2>
	<div style="display: flex;">
		<div style="flex-grow: 1;">
			<b>&lt;ui:tree-single&gt;</b>
			<ui:tree-single rootNode="${frd.treeRootNode}"
				hiddenName="nodeId" value="3" hiddenNameTitle="nodeTitle" selectableFolder="false"
				styleClass="mt1" style="height: 20em; overflow: auto;" />
		</div>
		<div style="flex-grow: 1;">
			<b>&lt;ui:tree-single&gt; (selectableFolder)</b>
			<ui:tree-single rootNode="${frd.treeRootNode}"
				hiddenName="nodeId" value="3" hiddenNameTitle="nodeTitle"
				styleClass="mt1" style="height: 20em; overflow: auto;" />
		</div>
	</div>
	<ui:when type="user">
		<div>
			<b>&lt;ui:combo-perm-tree-check&gt;</b><br/>
			<ui:combo-perm-tree-check permTrees="${permTrees}" prefixText="${l.l('Actions')}:" widthTextValue="15em"/>
		</div>
	</ui:when>
</div>

<h1>Tabs</h1>

<div>
	<h2>Statically defined</h2>

	<c:set var="uiid" value="${u:uiid()}"/>
	<div id="${uiid}">
		<ul>
			<li><a href="#tabs-1">First tab</a></li><%--
		--%><li><a href="#tabs-2">Second tab</a></li><%--
		--%><li><a href="#tabs-3">Third tab</a></li>
		</ul>
		<div id="tabs-1">First content</div>
		<div id="tabs-2">Second content</div>
		<div id="tabs-3">Third content</div>
	</div>

	<script>
		$("#${uiid}").tabs();
	</script>

	<ui:when type="user">
		<h2>Dynamically added</h2>

		<c:set var="uiid" value="${u:uiid()}"/>
		<div id="${uiid}">
			<ul></ul>
		</div>

		<script>
			$(function () {
				const $tabs = $("#${uiid}").tabs({ refreshButton: true });
				<p:check action="/user/demo:tabContentFirst">
					$tabs.tabs("add", "${form.httpRequestURI}?method=tabContentFirst", "First tab");
				</p:check>
				<p:check action="/user/demo:tabContentSecond">
					$tabs.tabs("add", "${form.httpRequestURI}?method=tabContentSecond", "Second tab");
				</p:check>
			})
		</script>
	</ui:when>
</div>

<h1>Tables</h1>

<div>
	<h2>Data Table with Row highlight</h2>
	<table class="data hl">
		<tr>
			<td>Header 1</td>
			<td>Header 2</td>
			<td>Header 3</td>
		</tr>
		<tr>
			<td>Data 1</td>
			<td>Data 2</td>
			<td>Data 3</td>
		</tr>
	</table>
</div>

<h1>ETC</h1>

<div>
	<h2>Horizontal Separator</h2>
	<div class="separator"></div>

	<h2>Font Icons</h2>
	<ul>
		<li><a href="https://themify.me/themify-icons">Themify Icons</a></li>
	</ul>
</div>