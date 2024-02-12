<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>
<div>
	<ui:select-mult hiddenName="paramIds"
		showId="true" moveOn="true" style="width: 100%;"
		list="${frd.paramList}" />
	<u:sc>
		<c:set var="loadCommand" value="$$.ajax.load(this.form, '#searchResult')"/>
		<c:choose>
			<c:when test="${frd.paramType==1}" >
				<ui:input-text name="value" placeholder="Значение" styleClass="mt05 w100p"
								onSelect="this.form.elements['searchBy'].value='parameter_text'; ${loadCommand}"/>
			</c:when>

			<c:when test="${frd.paramType==6}">
				Дата открытия:
				<ui:date-time styleClass="mt05" paramName="date_from" value="" /></br>
				Дата закрытия:
				<ui:date-time styleClass="mt05" paramName="date_to" value="" /><br>
				<input class="in-mt05" type="button" value="Поиск" class="btn-white"
					onclick="this.form.elements['searchBy'].value='parameter_date'; ${loadCommand}" />
			</c:when>

			<c:when test="${frd.paramType==9}">
				<ui:input-text styleClass="mt05 w100p" name="value" placeholder="Телефон"
					onSelect="this.form.elements['searchBy'].value='parameter_phone'; ${loadCommand}" />
			</c:when>
		</c:choose>
	</u:sc>
</div>