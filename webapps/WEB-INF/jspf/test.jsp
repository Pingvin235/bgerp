<%@page import="java.util.ArrayList"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.TreeSet"%>

<%@page import="ru.bgcrm.model.IdTitle"%>
<%@page import="ru.bgcrm.model.IdTitleComment"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="p05">
	<div style="display: flex;">
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

	<div class="in-mt1 mt1">
		<div>
			<b>&lt;ui:popup-menu&gt;</b><br/>

			<c:set var="menuUiid" value="${u:uiid()}"/>
			<ui:popup-menu id="${menuUiid}">
				<li>
					<a href="#" onclick="alert('Item 1 pressed!')">Item 1</a>
					<ul>
						<li><a href="#" onclick="alert('Item 1/1 pressed!')">Item 1/1</a></li>
					</ul>
				</li>
				<li><a href="#" onclick="alert('Item 2 pressed!')"><i class="ti-alarm-clock"></i> Item 2</a></li>
			</ui:popup-menu>
			<ui:button type="more" onclick="$$.ui.menuInit($(this), $('#${menuUiid}'), 'left', true);"/>
		</div>

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

		<div>
			<b>IPv4:</b><br/>
			<ui:ip paramName="ip"/>
		</div>

		<div>
			<b>Dot separated decimal:</b><br/>
			<input type="text" onkeydown="return isNumberKey(event)" size="10"/>
		</div>

		<div>
			<b>&lt;ui:input-text&gt;</b><br/>
			<ui:input-text name="text" onSelect="alert('Selected: ' + this.value)"/>
		</div>

		<div>
			<b>Buttons NEW</b><br/>

			<button class="btn-grey"><i class="ti-check mr05"></i>OK</button>
			<button class="btn-white"><i class="ti-cross mr05"></i>Отмена</button>
			<button class="btn-green">+</button>
			<ui:button type="add"/>
			<button class="btn-green">+</button>

			<br/><b>Small</b><br/>

			<button class="btn-white btn-small">*</button>
			<ui:button type="edit" styleClass="btn-small mr05"/>

			<button class="btn-white btn-small">X</button>
			<ui:button type="del" styleClass="btn-small mr05"/>

			<button class="btn-white btn-small">M</button>
			<button class="btn-white btn-small"><i class="ti-settings"></i></button>

			<ui:toggle styleClass="btn-small" inputName="test" value="1"
				textOn="ON" textOff="OFF"
				onChange="console.log('Is checked: ', this.checked);"/>
		</div>

		<div>
			<b>Buttons:</b><br/>

			<button class="btn-grey">Текст</button>

			<button class="btn-grey">Текст</button>

			<input type="text" placeholder="${l.l('Filter')}"/>

			<button class="btn-white">Кнопка BUTTON</button>

			<div class="btn-white">Кнопка DIV</div>

			<ui:toggle inputName="test" value="0"
				prefixText="Super name" textOn="ON" textOff="OFF"
				onChange="console.log('Is checked: ', this.checked);"/>
		</div>

		<div>
			<b>Кнопки панели задач</b><br/>

			<button class="btn-green btn-start">+</button>

			<div class="btn-white btn-task">
				<span class="title">Приложение 1111111111111111111111</span>
				<span class="icon-close" onclick="alert('Закрытие')"></span>
			</div>

			<div class="btn-blue btn-task-active">
				<span class="title">Приложение запущенноееееееееееееееееееееееее</span>
				<span class="icon-close" onclick="alert('Закрытие')"></span>
			</div>
		</div>

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
				<%
					List<IdTitle> list = new ArrayList<>();
					list.add(new IdTitle(1, "First dyn value"));
					list.add(new IdTitle(2, "Second dyn value with a loooooooooooooooooooong teeeeeeeeeeeeext"));
					pageContext.setAttribute("list", list);
				%>
				<ui:combo-single hiddenName="param" widthTextValue="12em" list="${list}" onSelect="${onSelect}">
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

			<u:sc>
				<%
					List<IdTitle> list = new ArrayList<IdTitle>();
					list.add(new IdTitle(1, "First value"));
					list.add(new IdTitle(2, "Second value and a long texttttttttt after it"));
					pageContext.setAttribute( "list", list );

					Set<Integer> values = new HashSet<Integer>();
					values.add(2);
					pageContext.setAttribute("values", values);
				%>
				<ui:combo-check prefixText="Статус:" paramName="param" list="${list}" values="${values}" widthTextValue="150px" showFilter="true"/>
			</u:sc>
		</div>

		<div>
			<b>&lt;ui:select-single&gt;</b><br/>

			<c:set var="onSelect" value="console.log('this=', this, '$hidden=', $hidden, '$input=', $input); alert('A value is chosen, see console log')"/>

			<u:sc>
				<%
					List<IdTitle> list = new ArrayList<>();
					list.add(new IdTitle(1, "First value"));
					list.add(new IdTitle(2, "Current second value and a long texttttttttt after it"));
					list.add(new IdTitle(3, "Must not be available!!!"));
					pageContext.setAttribute("list", list);

					Set<Integer> availableIdSet = new TreeSet<>();
					availableIdSet.add(1);
					availableIdSet.add(2);
					pageContext.setAttribute("availableIdSet", availableIdSet);
				%>
				<ui:select-single hiddenName="param" value="2" style="width: 10em;" list="${list}" availableIdSet="${availableIdSet}" onSelect="${onSelect}"/>
			</u:sc>

			<u:sc>
				<%
					Map<Integer, IdTitle> map = new TreeMap<>();
					map.put(1, new IdTitle(1, "Second value"));
					map.put(2, new IdTitle(2, "First value"));
					map.put(3, new IdTitle(3, "Must not be available!!!"));
					pageContext.setAttribute("map", map);

					List<Integer> availableIdList = new ArrayList<>();
					availableIdList.add(2);
					availableIdList.add(1);
					pageContext.setAttribute("availableIdList", availableIdList);
				%>
				<ui:select-single hiddenName="param" style="width: 15em;" map="${map}" availableIdList="${availableIdList}" onSelect="${onSelect}"/>
			</u:sc>

			<u:sc>
				<%
					List<IdTitleComment> list = new ArrayList<>();
					list.add(new IdTitleComment(1, "First selected value", "Comment 1"));
					list.add(new IdTitleComment(2, "Second value", "Comment 1"));
					pageContext.setAttribute("list", list);
				%>
				<ui:select-single hiddenName="param" value="1" style="width: 10em;" list="${list}" showId="${true}" showComment="${true}" onSelect="${onSelect}"/>
			</u:sc>

			<u:sc>
				<%
					List<IdTitle> list = new ArrayList<>();
					list.add(new IdTitle(1, "Must not be available, because of disabled input!"));
					list.add(new IdTitle(2, "Current second value"));
					pageContext.setAttribute("list", list);
				%>
				<ui:select-single hiddenName="param" value="2" style="width: 10em;" list="${list}" inputAttrs="disabled='1'" onSelect="alert('Must not be selectable!')"/>
			</u:sc>
		</div>

		<div>
			<b>&lt;ui:select-mult&gt;</b><br/>
			<u:sc>
				<%
					List<IdTitle> list = new ArrayList<IdTitle>();
					list.add(new IdTitle(1, "First value"));
					list.add(new IdTitle(2, "Second value and a long texttttttttt after it"));
					pageContext.setAttribute( "list", list );

					Set<Integer> values = new HashSet<Integer>();
					values.add( 2 );
					pageContext.setAttribute( "values", values );
				%>
				<ui:select-mult
					showId="true"
					hiddenName="param" style="width: 150px;"
					list="${list}" values="${values}"/>
			</u:sc>

			С изменением позиции (порядок выбранных должен быть 2 потом 1):

			<u:sc>
				<%
					List<IdTitle> list = new ArrayList<IdTitle>();
					list.add(new IdTitle(1, "First value"));
					list.add(new IdTitle(2, "Second value and a long texttttttttt after it"));
					pageContext.setAttribute( "list", list );

					Map<Integer, IdTitle> map = new HashMap<Integer, IdTitle>();
					for( IdTitle item : list )
					{
						map.put( item.getId(), item );
					}
					pageContext.setAttribute( "map", map );

					List<Integer> values = new ArrayList<Integer>();
					values.add( 2 );
					values.add( 1 );
					pageContext.setAttribute( "values", values );
				%>
				<ui:select-mult hiddenName="param" style="width: 150px;" moveOn="true"
					list="${list}" map="${map}" values="${values}"/>
			</u:sc>
		</div>

		<div>
			<b>&lt;ui:tag-box&gt;</b><br/>
			Provide values directly<br>
			<ui:tag-box showOptions="1" choices="first,second,third"/>
		</div>

		<div>
			<b>Column alignment</b><br/>

			<input type="text" style="width: 10em;"/><br/>

			<ui:combo-single hiddenName="param" value="2" style="width: 10em;">
				<jsp:attribute name="valuesHtml">
					<li value="1">Первый</li>
					<li value="2">Второй</li>
				</jsp:attribute>
			</ui:combo-single><br/>

			<u:sc>
				<%
					List<IdTitle> list = new ArrayList<IdTitle>();
					list.add(new IdTitle(1, "First value"));
					list.add(new IdTitle(2, "Second value and a long texttttttttt after it"));
					pageContext.setAttribute( "list", list );
				%>
				<ui:select-single hiddenName="param" value="2" style="width: 10em;" list="${list}"
					onSelect="console.log('this=', this, '$hidden=', $hidden); alert('Value is chosen, see console log')"/>
			</u:sc>
		</div>

		<div>
			<b>Tabs</b><br/>

			<div id="tabsTest">
				<ul>
					<li><a href="#tabs-1">First tab</a></li><%--
				--%><li><a href="#tabs-2">Second tab</a></li><%--
				--%><li><a href="#tabs-3">Third tab</a></li>
				</ul>
				<div id="tabs-1">First content</div>
				<div id="tabs-2">Second content</div>
				<div id="tabs-3">Third content</div>
			</div>
		</div>

		<script>
			$(function() {
				$("#tabsTest").tabs();
			})
		</script>
	</div>

	<%-- <div class="mt05">
		<b>Year and month</b><br/>

		<input name="startDate" id="startDate" class="date-picker" />
		<script type="text/javascript">
			$(function() {
				$('#startDate').datepicker({
					changeMonth: true,
					changeYear: true,
					showButtonPanel: true,
					dateFormat: 'yy MM',
					onClose: function(dateText, inst) {
						var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
						var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
						$(this).datepicker('setDate', new Date(year, month, 1));
					}
			});
			$("#startDate").focus(function () {
					$(".ui-datepicker-year").hide();
				});
			});
		</script>
	</div> --%>

	<%-- <br/><b>Что где посмотреть:</b>
	<ul>
		<li>1) DatePicker в виде диалога с позиционированием и Dialog с позиционированием.
		../jspf/plugin/bgbilling/bill/document_list.jsp</li>
		<li>2) Дерево, использование layout-height-rest
		../jspf/user/plugin/bgbilling/ipn/range_edit.jsp</li>
	</ul>
	--%>
	<div class="mt05">
		<br/><b>Font Icons:</b>
		<ul>
			<li><a href="https://themify.me/themify-icons">Themify Icons</a></li>
		</ul>
	</div>
</div>

