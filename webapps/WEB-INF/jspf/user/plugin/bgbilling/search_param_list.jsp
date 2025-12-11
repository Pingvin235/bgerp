<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>
<div class="in-w100p">
	<ui:select-mult hiddenName="paramIds" showId="true" moveOn="true" styleClass="mb05" list="${frd.paramList}" />

	<u:sc>
		<c:set var="loadCommand" value="$$.ajax.load(this.form, '#searchResult')"/>
		<c:choose>
			<c:when test="${frd.paramType eq 1}" >
				<ui:input-text name="value" placeholder="Значение" styleClass="mt05 w100p"
								onSelect="this.form.elements['searchBy'].value='parameter_text'; ${loadCommand}"/>
			</c:when>

			<c:when test="${frd.paramType eq 6}">
				Дата открытия:
				<ui:date-time styleClass="mt05" name="date_from" value="" /></br>
				Дата закрытия:
				<ui:date-time styleClass="mt05" name="date_to" value="" /><br>
				<input class="in-mt05" type="button" value="Поиск" class="btn-white"
					onclick="this.form.elements['searchBy'].value='parameter_date'; ${loadCommand}" />
			</c:when>

			<c:when test="${frd.paramType eq 9}">
				<ui:input-text name="value" placeholder="Телефон"
					onSelect="this.form.elements['searchBy'].value='parameter_phone'; ${loadCommand}" />
			</c:when>
		</c:choose>
	</u:sc>
</div>