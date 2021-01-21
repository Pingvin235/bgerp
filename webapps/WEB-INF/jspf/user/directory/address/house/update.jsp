<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="house" value="${form.response.data.house}"/>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="user/directory/address" styleId="${uiid}">
<input type="hidden" name="action" value="addressUpdate"/>
<html:hidden property="addressHouseId"/>
<html:hidden property="addressCountryId"/>
<html:hidden property="addressCityId"/>

<div id="houseParam">

<c:if test="${not empty form.param['hideLeftPanel']}">
	<h2>${l.l('Редактор дома')}</h2>
</c:if>

<table style="width: 100%;" class="data">
	<tr>
		<td width="150">${l.l('Параметр')}</td>
		<td>${l.l('Значение')}</td>
	</tr>
	<tr>
		<td>ID</td>
		<td>${form.param['addressHouseId']}</td>
	</tr>
	<tr>
		<td>${l.l('Страна')}</td>
		<td>${form.param['addressCountryTitle']}</td>
	</tr>
	<tr>
		<td>${l.l('Город')}</td>
		<td>${form.param['addressCityTitle']}</td>
	</tr>
	<tr>
		<td>${l.l('Улица')}</td>
		<td>
			<ui:select-single hiddenName="addressItemId" value="${house.addressStreet.id}" list="${parameterAddressStreetList}"/>
		</td>
	</tr>
	<tr>
		<td>${l.l('Район')}</td>
		<td>
			<ui:select-single hiddenName="addressAreaId" value="${house.addressArea.id}" list="${parameterAddressAreaList}"/>
		</td>
	</tr>
	<tr>
		<td>${l.l('Квартал')}</td>
		<td>
			<ui:select-single hiddenName="addressQuarterId" value="${house.addressQuarter.id}" list="${parameterAddressQuarterList}"/>
		</td>
	</tr>
	<tr>
		<td>${l.l('Номер дома с дробью')}</td>
		<td><html:text property="house" style="width: 100%;" value="${house.houseAndFrac}"/></td>
	</tr>
	<tr>
		<td>${l.l('Почтовый индекс')}</td>
		<td><html:text property="postIndex" style="width: 100" value="${house.postIndex}"/></td>
	</tr>
	<tr valign="top" class="even">
		<td>${l.l('Коментарий')}</td>
		<td><html:textarea property="comment" rows="2" style="width: 100%" value="${house.comment}"/></td>
	</tr>
</table>
</div>
<% out.flush(); %>
<div>
	<h2>${l.l('ПАРАМЕТРЫ')}</h2>
				
	<div id="forParamsReload">
		<c:url var="url" value="/user/parameter.do">
	   		<c:param name="action" value="parameterList"/>
			<c:param name="id" value="${form.param['addressHouseId']}"/>
			<c:param name="objectType" value="address_house"/>
			<c:param name="parameterGroup" value="-1"/>
		</c:url>
		<c:import url="${url}"/>
	</div>
</div>
<div class="mt1">
	<c:set var="toPostNames" value="{toPostNames: ['comment']}"/>
	<c:choose>
		<c:when test="${not empty form.param['hideLeftPanel']}">
			<button type="button" class="btn-grey" onclick="$$.ajax.post(this.form, ${toPostNames}).done(() => $$.ajax.load('${form.returnUrl}', $('#${uiid}').closest('div')))">ОК</button>
			<button type="button" class="btn-grey ml1" onclick="$$.ajax.load('${form.returnUrl}', $('#${uiid}').closest('div'))">${l.l('Отмена')}</button>
		</c:when>
		<c:otherwise>
			<button type="button" class="btn-grey" onclick="$$.ajax.post(this.form, ${toPostNames}).done(() => $$.ajax.load('${form.returnUrl}', $$.shell.$content()))">ОК</button>
			<button type="button" class="btn-grey ml1" onclick="$$.ajax.load('${form.returnUrl}', $$.shell.$content())">${l.l('Отмена')}</button>
		</c:otherwise>
	</c:choose>
</div>
</html:form>

<shell:state ltext="Редактирование дома" help="kernel/setup.html#address"/>