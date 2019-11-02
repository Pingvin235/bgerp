<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>
<div>
	<ui:select-mult hiddenName="paramIds" 
	     showId="true" moveOn="true" style="width: 100%;" 
	     list="${form.response.data.paramList}" />
	     
	
	<c:choose>
		<c:when test="${form.response.data.paramType==1}" >
		 	<ui:input-text name="value" placeholder="Значение" style="width: 100%;" styleClass="mt05" 
							onSelect="this.form.elements['searchBy'].value='parameter_text'; 
										openUrl( formUrl( this.form ), '#searchResult' )"/>
		</c:when>
		
		<c:when test="${form.response.data.paramType==6}">
			Дата открытия:
			<ui:date-time styleClass="mt05" paramName="date_from" value="" /></br>
			Дата закрытия: 
			<ui:date-time styleClass="mt05" paramName="date_to" value="" /><br>
			<input class="in-mt05" type="button"  value="Поиск" class="btn-white"  onclick="this.form.elements['searchBy'].value='parameter_date'; 
				openUrl( formUrl( this.form ), '#searchResult' )"/>
			
		 	
		</c:when>
		
		<c:when test="${form.response.data.paramType==9}">
		 	<ui:input-text styleClass="mt05" name="value" placeholder="Телефон" style="width: 100%;" 
							onSelect="this.form.elements['searchBy'].value='parameter_phone'; 
										openUrl( formUrl( this.form ), '#searchResult' )"/>
		</c:when>
	
	</c:choose>
</div>