<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.ArrayList"%>
<%@page import="ru.bgcrm.model.IdTitle"%>
<%@page import="java.util.List"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html>
<head>
	<title>TEST</title>
	<%@ include file="/WEB-INF/jspf/script_css.jsp"%>
	
	<style>
		.ui-menu { width: 150px; }
	</style>
</head>

<body style="padding: 0.5em;">
	<ul id="menu" class="menu">
		<li class="ui-state-disabled"><a href="#">Aberdeen</a></li>
		<li><a href="#">Ada</a></li>
		<li><a href="#">Adamsville</a></li>
		<li><a href="#">Addyston</a></li>
		<li><a href="#">Delphi</a>
			<ul>
				<li class="ui-state-disabled"><a href="#">Ada</a></li>
				<li><a href="#">Saarland</a></li>
				<li><a href="#">Salzburg</a></li>
			</ul></li>
		<li><a href="#">Saarland</a></li>
		<li><a href="#">Salzburg</a>
			<ul>
				<li><a href="#">Delphi</a>
					<ul>
						<li><a href="#">Ada</a></li>
						<li><a href="#">Saarland</a></li>
						<li><a href="#">Salzburg</a></li>
					</ul></li>
				<li><a href="#">Delphi</a>
					<ul>
						<li><a href="#">Ada</a></li>
						<li><a href="#">Saarland</a></li>
						<li><a href="#">Salzburg</a></li>
					</ul></li>
				<li><a href="#">Perch</a></li>
			</ul></li>
		<li class="ui-state-disabled"><a href="#">Amesville</a></li>
	</ul>

	<script>
		$(function()
		{
			$('#menu').menu();
		});
	</script>
	
	<div class="in-mt1 mt1">
		<div>
			<b>Выбор даты и времени:</b><br/>
		
			ymd:
			<ui:date-time type="ymd" paramName="date" editable="1"/>
			
			ymdh:
			<ui:date-time type="ymdh" paramName="date" editable="1"/>
			
			ymdhm:
			<ui:date-time type="ymdhm" paramName="date" editable="1"/>
			
			ymdhms:
			<c:set var="uiid" value="${u:uiid()}"/>
			<input type="text" id="${uiid}"/>
			<ui:date-time type="ymdhms" selector="#${uiid}" editable="1"/>		
		</div>
		
		<div>
			<%@ include file="/WEB-INF/jspf/date_month_days.jsp"%>			
		</div>		
		
		<div>	 	
			<b>IPv4:</b><br/>
		
			<ui:ip paramName="ip"/>
		</div>
		
		<div>     
            <b>Текстовое поле с иконками ввода и очистки:</b><br/>
            
            <ui:input-text name="text" onSelect="alert('Выбрано: ' + this.value)"/>
        </div>
		
		<div>
			<b>Кнопки реализованные с помощью button либо div:</b><br/>
		
			<button class="btn-grey">Текст</button>
		
			<button class="btn-grey">Текст</button>
		
			<input type="text" placeholder="Фильтр"/>
			
			<button class="btn-white">Кнопка BUTTON</button>
			
			<div class="btn-white">Кнопка DIV</div>
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
			
			<ui:combo-single 
				hiddenName="param" value="2" prefixText="Значение:" widthTextValue="120px"
				onSelect="alert('Значение выбрано')">
				<jsp:attribute name="valuesHtml">
					<li value="1">Первый</li>
					<li value="2 test"><span class='title'>Второй</span>с выделением</li>
					<li value="3">Третий ddddddddd ddddddddd dddddddddd d</li>
					<li value="4">Четвертый</li>
				</jsp:attribute>
			</ui:combo-single>
			
			<ui:combo-single
				hiddenName="param" value="2" style="width: 120px" 
				onSelect="alert('Значение выбрано')">
				<jsp:attribute name="valuesHtml">
					<li value="1">Первый</li>
					<li value="2"><span class='title'>Второй</span>с выделением</li>
					<li value="3">Третий</li>
					<li value="4">Четвертый</li>
				</jsp:attribute>				
			</ui:combo-single>
			
			<ui:combo-single hiddenName="param" value="2" style="width: 140px" onSelect="alert( 'test!' )">
				<jsp:attribute name="valuesHtml">
					<li value="1">Первый</li>
					<li value="2"><span class='title'>Второй</span>с выделением</li>
					<li value="3">Третий</li>
					<li value="4">Четвертый</li>
				</jsp:attribute>
			</ui:combo-single>
			
			<u:sc>
				<%
					List<IdTitle> list = new ArrayList<IdTitle>();
					list.add( new IdTitle( 1, "Первое значение" ) );
					list.add( new IdTitle( 2, "Второе значение и длинный текст после" ) );
					pageContext.setAttribute( "list", list );					
				%>
				<ui:combo-single hiddenName="param" value="2" widthTextValue="120px" onSelect="alert( 'test!' )" list="${list}"/>
			</u:sc>
		</div>
		
		<div>
			<b>&lt;ui:combo-check&gt;</b><br/>	
					
			<u:sc>
				<%
					List<IdTitle> list = new ArrayList<IdTitle>();
					list.add( new IdTitle( 1, "Первое значение" ) );
					list.add( new IdTitle( 2, "Второе значение и длинный текст после" ) );
					pageContext.setAttribute( "list", list );
					
					Set<Integer> values = new HashSet<Integer>();
					values.add( 2 );
					pageContext.setAttribute( "values", values );
				%>
				<ui:combo-check prefixText="Статус:" paramName="param" list="${list}" values="${values}" widthTextValue="150px"/>
			</u:sc>			
		</div>
		
		<div class="mt05">
			<b>&lt;ui:select-mult&gt;</b><br/>
			<u:sc>
				<%
					List<IdTitle> list = new ArrayList<IdTitle>();
					list.add( new IdTitle( 1, "Первое значение" ) );
					list.add( new IdTitle( 2, "Второе значение и длинный текст после" ) );
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
					list.add( new IdTitle( 1, "Первое значение" ) );
					list.add( new IdTitle( 2, "Второе значение и длинный текст после" ) );
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
			<b>Выравнивание в колонку</b><br/>
		
			<input style="width: 100px;"/><br/>
			
			<ui:combo-single hiddenName="param" value="2" style="width: 100px;">
				<jsp:attribute name="valuesHtml">
					<li value="1">Первый</li>
					<li value="2">Второй</li>			
				</jsp:attribute>
			</ui:combo-single>
			
			<br/>			
			<b>&lt;ui:select-single&gt;</b>
			<br/>
			
			<u:sc>
				<%
					List<IdTitle> list = new ArrayList<IdTitle>();
					list.add( new IdTitle( 1, "Первое значение" ) );
					list.add( new IdTitle( 2, "Второе значение и длинный текст после" ) );
					pageContext.setAttribute( "list", list );					
				%>	
				<ui:select-single hiddenName="param" value="2" style="width: 100px;" list="${list}"/>
			</u:sc>		
		</div>
		
		<div>
			<b>Табы</b><br/>
		
			<div id="tabsTest">
				<ul>
					<li><a href="#tabs-1">Первый таб</a></li><%--
				--%><li><a href="#tabs-2">Второй таб</a></li><%-- 
				--%><li><a href="#tabs-3">Третий таб</a></li>
				</ul>
				<div id="tabs-1">Содержимое первого таба</div>
				<div id="tabs-2">Содержимое второго таба</div>
				<div id="tabs-3">Содержимое третьего таба</div>
			</div>	
		</div>
		
		<script>
			$(function()
			{
				$("#tabsTest").tabs();
			})		
		</script>
	</div>
	
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
	
	<br/><b>Что где посмотреть:</b>
	<ul>
		<li>1) DatePicker в виде диалога с позиционированием и Dialog с позиционированием.
		../jspf/plugin/bgbilling/bill/document_list.jsp</li>
		<li>2) Дерево, использование layout-height-rest
		../jspf/user/plugin/bgbilling/ipn/range_edit.jsp</li>
	</ul>
	
	<br/><b>Font Icons:</b>
	<ul> 
		<li><a href="https://themify.me/themify-icons">Themify Icons</a></li>
	</ul>		
</body>
</html>