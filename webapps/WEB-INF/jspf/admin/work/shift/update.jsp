<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="/admin/work" styleClass="center1020">
	<input type="hidden" name="action" value="shiftUpdate" />
	
	<c:set var="shift" value="${form.response.data.shift}"/>
	<c:set var="workTypeList" value="${form.response.data.workTypeList}"/>
	<c:set var="workTypeMap" value="${form.response.data.workTypeMap}"/>
	
	<div class="in-inline-block in-pr1 in-va-top">
		<div style="width: 50%;">
			<h2>ID</h2>
			<input type="text" name="id" style="width: 100%" value="${shift.id}" disabled="disabled"/>		
		
			<h2>Категория</h2>
			<u:sc>
				<c:set var="list" value="${allowOnlyCategories}"/> 
				<c:set var="hiddenName" value="categoryId"/>
				<c:if test="${not empty shift}">
					<c:set var="value" value="${shift.category}"/>
				</c:if>	
				<c:set var="style" value="width: 100%;"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
			</u:sc>
			
			<h2>${l.l('Наименование')}</h2>
			<html:text property="title" style="width: 100%" value="${shift.title}"/>
				
			
		</div><%--	
   	--%><div style="width: 50%;">
   			<h2>Использовать цвет смены</h2>
			
			<u:sc>
				<c:set var="valuesHtml">
					<li value="1">Да</li>
					<li value="0">Нет</li>
				</c:set>
				<c:set var="hiddenName" value="useOwnColor"/>
				<c:set var="value" value="${shift.useOwnColor ? 1 : 0}"/>
				<c:set var="style" value="width: 100%"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
			</u:sc>
			
			<h2>Символ смены (не больше 2 символов)</h2>
			<html:text property="symbol" style="width: 100%" value="${shift.symbol}"/>
			
			<h2>Цвет:</h2>
			<div class="controlset">
				<input type="text" name="color" id="shiftColor${uiid}" value="${shift.color}" />					
			</div>
		</div>
	</div>		
			
	<h2>Виды работ</h2>
	<%@ include file="/WEB-INF/jspf/admin/work/shift/work_type_time_editor.jsp"%>							
	
	<ui:form-ok-cancel/>
</html:form>

<script>
	$('input#shiftColor${uiid}').colorPicker();
</script>

<c:set var="state" value="Редактор"/>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>