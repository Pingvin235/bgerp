<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div>
	<html:hidden property="streetId"/>
	<html:hidden property="houseId"/>
	<input type="text" name="street" placeholder="${l.l('Улица')}" style="width: 100%;"
				title="${l.l('Нажатие Enter - поиск по улице')}"
				onkeypress="if( enterPressed( event ) ){ this.form.elements['searchBy'].value='address';
															  openUrl( formUrl( this.form ), '#searchResult' ) }"/>
</div>
<div>
	<div style="display: table-cell; width: 60%">
		<input type="text" name="house" id="house" style="width: 100%;" placeholder="${l.l('Дом/дробь')}"
			 	title="${l.l('Нажатие Enter - поиск по дому')}"
				onkeypress="if( enterPressed( event ) ){ this.form.elements['searchBy'].value='address';
																  openUrl( formUrl( this.form ), '#searchResult' ) }"/>
	</div>
	<div style="display: table-cell; width: 40%;" class="pl1">
		<input type="text" name="flat" id="flat" style="width: 100%;"
						title="${l.l('Нажатие Enter - поиск по квартире')}" placeholder="${l.l('Квартира')}"
						onkeypress="if( enterPressed( event ) ){ this.form.elements['searchBy'].value='address';
																	  openUrl( formUrl( this.form ), '#searchResult' ) }"/>
	</div>
</div>