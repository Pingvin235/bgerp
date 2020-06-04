<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="formUiid" value="${u:uiid()}"/>

<c:set var="selectorSample" value="${u:uiid()}"/>
<c:set var="selectorTo" value="${u:uiid()}"/>

<c:set var="typeChangedScript">
	var $form = $('#${formUiid}');
	
	var type = $form[0].type.value;
	var $listValues = $form.find('#listValues');
	
	if ($listValues.toggle(type === 'tree' || type == 'list' ||  type == 'listcount').is(':visible'))
	{
		$listValues.find('.hint').hide();		
		$listValues.find('.' + type).show();
	}
		
	$('#${selectorTo}').css( "height", $('#${selectorSample}').height() + 'px' );
</c:set>

<%@ include file="/WEB-INF/jspf/admin/directory/directory.jsp"%>

<c:set var="parameter" value="${form.response.data.parameter}"/>

<html:form action="admin/directory" styleClass="center1020" styleId="${formUiid}">
	<input type="hidden" name="action" value="parameterUpdate"/>
	<html:hidden property="directoryId"/>
		
	<div class="in-inline-block in-pr1 in-va-top">
		<div style="width: 50%;">
			<h2>ID</h2>
			<input type="text" name="id" value="${form.id}" disabled="disabled" style="width: 100%;"/>
			
			<div id="${selectorSample}">
				<h2>Название</h2>
				<input type="text" name="title" style="width: 100%" value="${fn:escapeXml( parameter.title )}"/>			
				
				<h2>Тип</h2>
				<c:choose>
					<c:when test="${form.id ne '-1'}">
						<input type="text" name="type" value="${parameter.type}" disabled="disabled" style="width: 100%;"/>
					</c:when>
					<c:otherwise>
						<u:sc>
							<c:set var="valuesHtml">
								<li value="text">text</li>
								<li value="blob">blob</li>
								<li value="date">date</li>
								<li value="datetime">datetime</li>
								<li value="email">email</li>
								<li value="list">list</li>
								<li value="listcount">listcount</li>
								<li value="phone">phone</li>
								<li value="address">address</li>
								<li value="file">file</li>
								<li value="tree">tree</li>
							</c:set>
							<c:set var="hiddenName" value="type"/>
							<c:set var="style" value="width: 100%;"/>
							<c:set var="onSelect">${typeChangedScript}</c:set>
							<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
						</u:sc>				
					</c:otherwise>
				</c:choose>
				
				<div id="listValues">
					<h2>Значения</h2>
				
					<textarea name="listValues" rows="7" style="width: 100%; resize:none;" wrap="off">${parameter.valuesConfig}</textarea>
					
					<span class="hint tree">
						Значения списка в формате <b>&lt;parentNodeIds&gt;.nodeId=value</b>, 
						где <b>&lt;parentNodeIds&gt;</b> - разделённые точками числовые идентификаторы предков (если есть).
						<b>nodeId</b> - числовой положительный идентификатор текущего элемента
						<b>value</b> значение в дереве. Новое значение с новой строки.<br/>
						<b>Пример:</b><br/>
						1=Оборудование<br/>
						1.1=Медный усилитель<br/>
						1.1.1=Вышел из строя<br/>
						1.1.2=Неверная настройка<br/>
					</span>
					
					<span class="hint list listcount">
						Значения списка в формате <b>id=value</b>, 
						где <b>id</b> - числовой положительный идентификатор элемента <b>value</b> в списке.  
						Новое значение с новой строки.<br/>
						<b>Пример:</b><br/>
						1=Первое значение<br/>
						2=Второе значение<br/>
						Заблокированные значения помечаются знаком <b>@</b>, например <b>5=@Значение</b>.<br/>
					</span>
				</div>
				
				<h2>Порядок</h2>
				<input type="text" name="order" value="${parameter.order}" style="width: 100%;"/>
				
				<span class="hint">Определяет позицию параметра в общем списке либо в таблице параметров объектов, 
				если позиция не переопределяется в типе процесса либо в группе параметров.</span>			
			</div>
		</div><%-- 
	--%><div style="width: 50%;">
			<h2>Комментарий</h2>
			<input type="text" name="comment" style="width: 100%" value="${fn:escapeXml( parameter.comment )}"/>
			
			<%--
			<h2>Скрипт</h2>
			<input type="text" name="script" style="width: 100%" value="${parameter.script}"/> 
			--%>
			
			<h2>Конфигурация</h2>
			<textarea id="${selectorTo}" name="config" rows="7" style="width: 100%; resize:none;" wrap="off">${parameter.config}</textarea>
		</div>
	</div>
	
	<div class="mt1">
		<c:set var="toPostNames" value="['listValues','config']"/>
		<%@ include file="/WEB-INF/jspf/send_and_cancel_form.jsp"%>
	</div>	
</html:form>

<script>
	$(function()
	{
		${typeChangedScript}
	})
</script>

<shell:state ltext="Редактор" help="kernel/setup.html#param"/>
