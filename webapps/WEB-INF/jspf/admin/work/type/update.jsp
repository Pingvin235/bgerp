<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="/admin/work" styleId="${uiid}" styleClass="center1020">
	<input type="hidden" name="action" value="workTypeUpdate" />
	
	<c:set var="workType" value="${form.response.data.workType}"/>
		
	<div class="in-table-cell in-pr1 in-va-top">
		<div style="width: 50%;" id="left">
			<h2>ID</h2>
			<input type="text" name="id" style="width: 100%" value="${workType.id}" disabled="disabled"/>
				
			<h2>${l.l('Наименование')}</h2>
			<div id="sample"></div>					
			<input type="text" name="title" style="width: 100%" value="${workType.title}"/>
					
			<h2>${l.l('Категория')}</h2>
			<u:sc>
				<c:set var="list" value="${allowOnlyCategories}"/> 
				<c:set var="hiddenName" value="categoryId"/>
				<c:set var="value" value="${workType.category}"/>
				<c:set var="style" value="width: 100%;"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
			</u:sc>
			
			<h2>${l.l('Цвет')}</h2>
			<div class="controlset">
				<input type="text" name="color" value="${workType.color}" />					
			</div>
			<div class="hint">Используется в плане работ.</div>
   			
   			
   			<h2>Учитывать в рабочих часах</h2>
			<u:sc>
				<c:set var="valuesHtml">						
					<li value="0">${l.l('Да')}</li>			
					<li value="1">${l.l('Нет')}</li>										
				</c:set>		 
				<c:set var="hiddenName" value="nonWorkHours"/>
				<c:set var="value" value="${workType.nonWorkHours ? 1 : 0}"/>
				<c:set var="style" value="width: 100px;"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
			</u:sc>
			<div class="hint">Учитывать время данного типа работ в рабочих часах графика.</div>
  	
   			<h2>Обозначение в табеле</h2>
			
			<input type="text" name="shortcuts" value="${u:toString( workType.shortcutList )}" style="width: 100%;"/>
			<div class="hint">Сокращения для табеля через запятую.</div>
			
			<%--
			<u:sc>
				<c:set var="valuesHtml">						
					<li value="0">Не установлено</li>			
					<c:forEach var="item" items="${shortcutMap}">
						<li value="${item.key}">${item.value.value} (${item.value.title})</li>
					</c:forEach>
				</c:set>			
				<c:set var="hiddenName" value="shortcutId"/>
				<c:set var="value" value="${workType.shortcutId}"/>
				<c:set var="style" value="width: 100%;"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
			</u:sc>
			 --%>
   	
   			<h2>${l.l('Комментарий')}</h2>
			<html:textarea property="comment" style="width: 100%; resize: none;" value="${workType.comment}"/>
			
		</div><%--	
   	--%><div style="width: 50%;">
   			<h2>Шаг длительности (размер слота)</h2>
			<input type="text" name="timeSetStep" style="width: 100%" value="${workType.timeSetStep}"/>
			<div class="hint">Шаг в минутах, через который устанавливается время процессов. 0 - все процессы назначаются на начало периода типа работ.</div>
			
			<h2>Назначать время на</h2>
			<u:sc>
				<c:set var="valuesHtml">						
					<li value="0">Начало интервала</li>			
					<li value="1">Начало слота</li>										
				</c:set>		 
				<c:set var="hiddenName" value="timeSetMode"/>
				<c:set var="value" value="${workType.timeSetMode}"/>
				<c:set var="style" value="width: 150px;"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
			</u:sc>
						
			<h2>Конфигурация длительности</h2>
			<textarea name="ruleConfig" style="width: 100%; resize: none;" wrap="off" class="layout-height-rest">${workType.ruleConfig}</textarea>  			
		</div>
	</div>
	
	<%-- <u:sc>	
		<c:set var="selectorSample" value="#${uiid} #sample"/>
		<c:set var="selectorTo" value="#${uiid} textarea"/>
		<%@ include file="/WEB-INF/jspf/same_height.jsp"%>
	</u:sc> --%>	
	
	<div class="mt1">
		<%@ include file="/WEB-INF/jspf/send_and_cancel_form.jsp"%></td>
	</div>
	
	<script>
		$(function()
		{
			$('#${uiid} input[name=color]').colorPicker();
		})
	</script>
	
	<%@ include file="/WEB-INF/jspf/layout_process.jsp"%>
</html:form>

<c:set var="state" value="Редактор"/>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>



<%-- 
<table style="width: 100%;" class="data" id="workTypeRules${uiid}">
	<tr>
		<td width="100">Параметр</td>
		<td>Значение</td>
	</tr>
	<tr>
		<td>ID</td>
		<td>${form.id}</td>
	</tr>
	<tr>
		<td>Категория</td>
		<td>
			<select name="categoryId">									
				<c:forEach var="item" items="${allowOnlyCategories}">
					<option value="${item.id}" <c:if test="${item.id == workType.category}">selected="selected"</c:if> >${item.title}</option>
				</c:forEach>					
			</select>
		</td>
	</tr>		
	<tr>
		<td>${l.l('Наименование')}</td>
		<td><html:text property="title" style="width: 100%" value="${workType.title}" /></td>
	</tr>
	<tr>
		<td>${l.l('Комментарий')}</td>
		<td><html:textarea property="comment" style="width: 100%" value="${workType.comment}" /></td>
	</tr>		
	<tr>
		<td>${l.l('Цвет')}:</td>
		<td>
			<div class="controlset">
				<input type="text" name="color" id="ruleColor${uiid}" value="${workType.workTypeConfig.color}" />					
			</div>
		</td>
	</tr>
	<tr valign="top">
		<td>${l.l('Правила')}</td>
		<td>
			<input id="newRuleButton${uiid}" type="button" value="Добавить правило" />
			<table id="ruleList${uiid}" style="margin-top: 10px; width: 100%;" class="data">
				<tr style="font-weight: bold; text-align: center;">
					<td style="width: 30px;"></td>
					<td>${l.l('Процессы')}</td>
					<td>${l.l('Услуги')}</td>
					<td>${l.l('Продолжительность')}</td>
				</tr>				
				<c:forEach var="item" items="${workType.workTypeConfig.rulesMap}">
					<tr>
						<td>
							<input type="checkbox" name="rule" class="ruleVlues${uiid}" value="${u:toString( item.value.processIds )}:${u:toString( item.value.serviceIds )};${item.value.time}" checked="checked" hidden="hidden"/>
							<input type="button" onclick="$(this).parents('tr').first().remove();" title="Удалить" value=" X " />
						</td>
						<td>${item.value.processString}</td>
						<td>${item.value.serviceString}</td>
						<td>${item.value.time} (минут)</td>
					</tr>											
				</c:forEach>
			</table>
		</td>
	</tr>
	<tr>
		<td>${l.l('Обозначение')}</td>
		<td>
			<select name="shortcutId">						
				<option value="0">Не установлено</option>			
				<c:forEach var="item" items="${shortcutMap}">
					<option value="${item.key}" <c:if test="${item.key == workType.shortcutId}">selected="selected"</c:if> >${item.value.value} (${item.value.title})</option>
				</c:forEach>					
			</select>
		</td>
	</tr>
	<tr>
	<tr>
		<td>Не учитывать в рабочих часах</td>
		<td>
			<input type="checkbox" name="nonWorkHours" value="1" <c:if test="${workType.isNonWorkHours == true}">checked="checked"</c:if> />
		</td>
	</tr>
	<tr>
		<td>Тип</td>
		<td>
			<select name="kind" onchange="if( $(this).find('option:selected').val() == 2 ) $( 'tr.dynamicRequired' ).removeClass('hidden'); else $( 'tr.dynamicRequired' ).addClass('hidden');">									
				<option value="1" <c:if test="${workType.type == 1}">selected="selected"</c:if> >Статический</option>
				<option value="2" <c:if test="${workType.type == 2}">selected="selected"</c:if> >Динамический</option>					
			</select>
		</td>
	</tr>
	<tr class="dynamicRequired">
		<td>Пользователи с пересекающимся временем</td>
		<td>
			<select name="usersWithOvelappedTime">
				<option value="0">0 - без ограничений</option>									
				<c:forEach var="i" begin="1" end="10" step="1">						
					<option value="${i}" ${workType.dynamicSettings.userCount == i ? 'selected="selected"' : ''}>${i}</option>						
				</c:forEach>
			</select>
		</td>
	</tr>
	<tr class="dynamicRequired">
		<td>Кто может указывать время работы</td>
		<td>
			<select name="timeControlPolitics">
				<option value="1" ${workType.dynamicSettings.timeControlPolitics == 1 ? 'selected="selected"' : ''}>Любой</option>
				<option value="2" ${workType.dynamicSettings.timeControlPolitics == 2 ? 'selected="selected"' : ''}>Пользователь</option>
				<option value="3" ${workType.dynamicSettings.timeControlPolitics == 3 ? 'selected="selected"' : ''}>Руководитель</option>
			</select>
		</td>
	</tr>		
	<tr class="dynamicRequired">
		<td>Запрос указания времени</td>
		<td>
			<select name="timeSetPolitics">
				<option value="1" ${workType.dynamicSettings.timeSetPolitics == 1 ? 'selected="selected"' : ''}>Произвольно</option>
				<option value="2" ${workType.dynamicSettings.timeSetPolitics == 2 ? 'selected="selected"' : ''}>В начале смены</option>
				<option value="3" ${workType.dynamicSettings.timeSetPolitics == 3 ? 'selected="selected"' : ''}>При составлении графика</option>
			</select>
		</td>
	</tr>		
	<tr>
		<td colspan="2"><%@ include file="/WEB-INF/jspf/send_and_cancel_form.jsp"%></td>			
	</tr>
</table>
--%>

<%--
<div id="workTypeNewRule${uiid}" style="display: none;">
<table style="width: 100%;" class="data">
	<tr>
		<td width="100">Параметр</td>
		<td>Значение</td>
	</tr>
	<tr valign="top">
		<td>Типы процессов</td>
		<td>
			<div style="overflow: auto; height: 600;">			
				<c:set var="processTypeIds" value="${0}" scope="request"/>
				<jsp:include page="/WEB-INF/jspf/admin/process/tree/process_type_check_tree.jsp"/>
			</div>				
		</td>
	</tr>
	<tr>
		<td>Услуги</td>
		<td>
			<c:forEach var="item" items="${servicesList}">
				<input type="checkbox" class="serviceSelector${uiid}" name="type" value="${item.id}" />
				<label>${item.title} (${item.id})</label><br/>
			</c:forEach>
		</td>			
	</tr>	
	<tr>
		<td>Продолжительность работ (минут)</td>
		<td>
			<input id="ruleTime${uiid}" type="text" value="90"/>
		</td>
	</tr>		
</table>

<table>
	<tr>
		<td>
			<input id="addButton${uiid}" type="button" value="OK" />
			<input type="button" value="Отмена" onclick="$('#workTypeNewRule${uiid}').hide(); $('#workTypeRules${uiid}').show();"/>
		</td>
		</tr>
</table>
</div>
 --%>

<%--
$('#processTypeSelect${uiid}').combobox();
$('select[name="kind"]').change();

function clearSelection() 
{
	$('#workTypeNewRule${uiid} input:checked').each(function() {
		
		$(this).removeAttr('checked');
	});	
}

$('#newRuleButton${uiid}').on('click', function() {
		
	clearSelection();
	
	$('#workTypeRules${uiid}').hide(); 
	$('#workTypeNewRule${uiid}').show();	
});

$('#addButton${uiid}').on('click', function() {
	
	var time = $("input#ruleTime${uiid}").val();
	var values = '';
	var valuesBuffer = '';
	var result = '';
	var resultBuffer = '';
	
	$('#workTypeNewRule${uiid} input:checked').each(function() { 
	
		if($(this).hasClass('serviceSelector${uiid}'))
			return ;
	
		if(resultBuffer.length >0)
			resultBuffer+=', ';
		
		if(valuesBuffer.length >0)
			valuesBuffer+=',';
	
		resultBuffer+=$(this).next().text();
		valuesBuffer+=$(this).val();		
	} );
	
	if( resultBuffer.length ==0 ) 
	{
		alert( 'Не выбрано ни одного типа процесса!' );
		return ;
	}
	 
	result+=resultBuffer;	
	resultBuffer = '';
	values+=valuesBuffer+':';
	valuesBuffer = '';
	 
	$('#workTypeNewRule${uiid} input.serviceSelector${uiid}:checked').each(function() { 
	
		if(resultBuffer.length >0)
			resultBuffer+=', ';
		
		if(valuesBuffer.length >0)
			valuesBuffer+=',';
	
		resultBuffer+=$(this).next().text();
		valuesBuffer+=$(this).val();
	} );
	
	if( resultBuffer.length ==0 ) 
	{
		alert( 'Не выбрано ни одной услуги!' );
		return ;
	}
	
	if( time.length == 0 )
	{
		alert( 'Не указана продолжительность работ!' );
		return ;
	}
	
	values+=valuesBuffer;
	values+=";"+time;
	
	var append = '<tr>';
	append+='<td><input type="checkbox" name="rule" class="ruleVlues${uiid}" value="'+values+'" checked="checked" hidden="hidden"/><input type="button" onclick="$(this).parents(\'tr\').first().remove();" title="Удалить" value=" X " /></td>';
	append+='<td>'+result+'</td>';
	append+='<td>'+resultBuffer+'</td>';
	append+='<td>'+time+' (минут)</td>';
	append+='</tr>';
				 		
	$('#ruleList${uiid}').append(append);
	
	$('#workTypeNewRule${uiid}').hide(); 
	$('#workTypeRules${uiid}').show();	 
});
</script>
--%>
