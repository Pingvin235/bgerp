<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:sc>
	<c:set var="ep" value="if (!enterPressed(event)) return;"/>
	<div>
		<html:hidden property="streetId"/>
		<html:hidden property="houseId"/>
		<input type="text" name="street" placeholder="${l.l('Улица')}" style="width: 100%;"
					title="${l.l('Нажатие Enter - поиск по улице')}"
					onkeypress="${ep} this.form.elements['searchBy'].value='address';
									$$.ajax.load(this.form, '#searchResult')"/>
	</div>
	<div>
		<div style="display: table-cell; width: 60%">
			<input type="text" name="house" style="width: 100%;" placeholder="${l.l('Дом/дробь')}"
					title="${l.l('Нажатие Enter - поиск по дому')}"
					onkeypress="${ep} this.form.elements['searchBy'].value='address';
									$$.ajax.load(this.form, '#searchResult')"/>
		</div>
		<div style="display: table-cell; width: 40%;" class="pl1">
			<ui:input-text name="flat" styleClass="w100p" title="${l.l('Нажатие Enter - поиск по квартире')}" placeholder="${l.l('Квартира')}"
				onSelect="this.form.elements['searchBy'].value='address';
										$$.ajax.load(this.form , '#searchResult')"/>
		</div>
	</div>
</u:sc>