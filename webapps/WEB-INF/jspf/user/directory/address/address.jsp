<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="disabled" value="false"/><c:if test="${addressTable eq 'houseData'}"><c:set var="disabled" value="true"/></c:if>
<table style="width: 100%; height:100%">
	<tr>
		<c:if test="${empty form.param['hideLeftPanel']}">
			<td width="20px" valign="top">
				<c:set var="formUiid" value="${u:uiid()}"/>

				<html:form action="/user/directory/address" disabled="${disabled}" styleId="${formUiid}">
					<html:hidden property="action" value="address"/>

					<html:hidden property="selectTab" styleId="selectTab"/>
					<html:hidden property="searchMode"/>
					<html:hidden property="addressCountryId"/>
					<html:hidden property="addressCityId"/>
					<html:hidden property="addressItemId"/>
					<html:hidden property="addressHouseId"/>

					<table style="width: 100%;">
						<c:url var="url" value="/user/directory/address.do">
							<c:param name="action" value="addressGet"/>
							<c:param name="returnUrl" value="${form.requestUrl}"/>
							<c:param name="selectTab" value="${form.param.selectTab}"/>
							<c:param name="addressCountryId" value="0"/>
						</c:url>

						<tr><td nowrap="nowrap">
							${l.l('Страна')}:<br>
							<html:text property="addressCountryTitle" style="width: 180px;" onkeypress="addressSearchCountry( this, event );"/>&nbsp;
							<ui:button type="clear" onclick="addressClearCountry( this );"/>
							<ui:button type="add" onclick="$$.ajax.loadContent('${url}');"/>
							<ui:button type="out" onclick="addressSearchCountry( this );"/>
						</td></tr>

						<c:url var="url" value="/user/directory/address.do">
							<c:param name="action" value="addressGet"/>
							<c:param name="returnUrl" value="${form.requestUrl}"/>
							<c:param name="selectTab" value="${form.param.selectTab}"/>
							<c:param name="addressCountryTitle" value="${form.param.addressCountryTitle}"/>
							<c:param name="addressCountryId" value="${form.param.addressCountryId}"/>
							<c:param name="addressCityId" value="0"/>
						</c:url>

						<tr><td nowrap="nowrap">
							${l.l('Город')}:<br>
							<html:text property="addressCityTitle" style="width: 180px;" onkeypress="addressSearchCity( this, event );"/>&nbsp;
							<ui:button type="clear" onclick="addressClearCity( this );"/>
							<ui:button type="add" onclick="if ( '${form.param['addressCountryId']}' == '' ) { alert('Выберите страну'); } else { $$.ajax.loadContent('${url}'); }"/>
							<ui:button type="out" onclick="addressSearchCity( this );"/>
						</td></tr>

						<c:set var="selectTab" value="${form.param.selectTab}"/>
						<c:if test="${empty selectTab}">
							<c:set var="selectTab" value="street"/>
						</c:if>

						<tr><td nowrap="nowrap" style="padding: 0px;">
							<table class="menu"><tr>
							<c:set var="cl" value="nosel"/><c:if test="${selectTab eq 'street'}"><c:set var="cl" value="sel"/></c:if>
							<td class="${cl}"><a href="#" onclick="$('#${formUiid} #selectTab').attr( 'value', 'street' ); $('#${formUiid} input[name=addressItemTitle]').attr( 'value', '' ); $$.ajax.loadContent($('#${formUiid}')[0]); return false;">${l.l('УЛИЦА')}</a></td>

							<c:set var="cl" value="nosel"/><c:if test="${selectTab eq 'area'}"><c:set var="cl" value="sel"/></c:if>
							<td class="${cl}"><a href="#" onclick="$('#${formUiid} #selectTab').attr( 'value', 'area' ); $('#${formUiid} input[name=addressItemTitle]').attr( 'value', '' ); $$.ajax.loadContent($('#${formUiid}')[0]); return false;">${l.l('РАЙОН')}</a></td>

							<c:set var="cl" value="nosel"/><c:if test="${selectTab eq 'quarter'}"><c:set var="cl" value="sel"/></c:if>
							<td class="${cl}"><a href="#" onclick="$('#${formUiid} #selectTab').attr( 'value', 'quarter' ); $('#${formUiid} input[name=addressItemTitle]').attr( 'value', '' ); $$.ajax.loadContent($('#${formUiid}')[0]); return false;">${l.l('КВАРТАЛ')}</a></td>
							<td width="90%" style="border: 0px;">&nbsp;</td></tr></table>
						</td></tr>

						<c:url var="url" value="/user/directory/address.do">
							<c:param name="action" value="addressGet"/>
							<c:param name="returnUrl" value="${form.requestUrl}"/>
							<c:param name="selectTab" value="${form.param.selectTab}"/>
							<c:param name="addressCityId" value="${form.param.addressCityId}"/>
							<c:param name="addressItemId" value="0"/>
							<c:param name="addressCountryTitle" value="${form.param.addressCountryTitle}"/>
							<c:param name="addressCityTitle" value="${form.param.addressCityTitle}"/>
						</c:url>

						<tr><td nowrap="nowrap">
							<html:text property="addressItemTitle" style="width: 180px;" onkeypress="addressSearchItem( this, event );"/>&nbsp;
							<ui:button type="clear" onclick="addressClearItem( this );"/>
							<ui:button type="add" onclick="if ( '${form.param['addressCityId']}' == '' ) { alert('Выберите город'); } else { $$.ajax.loadContent('${url}'); }"/>
							<ui:button type="out" onclick="addressSearchItem( this );"/>
						</td></tr>

						<c:if test="${selectTab eq 'street'}">
							<c:url var="url" value="/user/directory/address.do">
								<c:param name="action" value="addressGet"/>
								<c:param name="returnUrl" value="${form.requestUrl}"/>
								<c:param name="selectTab" value="${form.param.selectTab}"/>
								<c:param name="addressCountryTitle" value="${form.param.addressCountryTitle}"/>
								<c:param name="addressCityTitle" value="${form.param.addressCityTitle}"/>
								<c:param name="addressItemTitle" value="${form.param.addressItemTitle}"/>
								<c:param name="addressCityId" value="${form.param.addressCityId}"/>
								<c:param name="addressItemId" value="${form.param.addressItemId}"/>
								<c:param name="addressHouseId" value="0"/>
							</c:url>
							<tr><td nowrap="nowrap">
								${l.l('Дом')}:<br>
								<html:text property="addressHouse" style="width: 180px;" onkeypress="addressSearchHouse( this, event );"/>&nbsp;
								<ui:button type="clear" onclick="addressClearHouse( this );"/>
								<ui:button type="add" onclick="if ( '${form.param['addressItemId']}' == '' ) { alert('${l.l('Выберите улицу')}'); } else { $$.ajax.loadContent('${url}'); }"/>
								<ui:button type="out" onclick="if ( '${form.param['addressItemId']}' == '' ) { alert('${l.l('Выберите улицу')}'); } else { addressSearchHouse( this ); }"/>
							</td></tr>
						</c:if>
					</table>
				</html:form>
			</td>

			<u:sc>
				<c:set var="title">
					<span class='title'>${l.l('Адресный справочник')}</span>
				</c:set>
				<shell:title text="${title}"/>
				<shell:state/>
			</u:sc>

			<c:set var="paddingLeft" value="pl1"/>
		</c:if>

		<td valign="top" class="${paddingLeft}">
			<c:set var="data" value="${form.response.data}"/>
			<c:set var="searchMode" value="${form.param.searchMode}"/>

			<c:set var="doUrl" value="/user/directory/address.do" scope="request"/>
			<c:choose>
				<c:when test="${form.action eq 'addressGet'}">
					<c:choose>
						<c:when test="${not empty data.country}"><%@ include file="/WEB-INF/jspf/user/directory/address/country/update.jsp" %></c:when>
						<c:when test="${not empty data.city}"><%@ include file="/WEB-INF/jspf/user/directory/address/city/update.jsp" %></c:when>
						<c:when test="${not empty data.street or not empty data.area or not empty data.quarter}">
							<%@ include file="/WEB-INF/jspf/user/directory/address/item/update.jsp" %>
						</c:when>
						<c:when test="${not empty data.house}"><%@ include file="/WEB-INF/jspf/user/directory/address/house/update.jsp" %></c:when>
					</c:choose>
				</c:when>
				<c:otherwise>
					<c:choose>
						<c:when test="${searchMode eq 'country'}"><%@ include file="/WEB-INF/jspf/user/directory/address/country/search.jsp" %></c:when>
						<c:when test="${searchMode eq 'city'}"><%@ include file="/WEB-INF/jspf/user/directory/address/city/search.jsp" %></c:when>
						<c:when test="${searchMode eq 'item' and selectTab eq 'street'}"><c:set var="itemType" value="Street"/><%@ include file="/WEB-INF/jspf/user/directory/address/item/search.jsp"%></c:when>
						<c:when test="${searchMode eq 'item' and selectTab eq 'area'}"><c:set var="itemType" value="Area"/><%@ include file="/WEB-INF/jspf/user/directory/address/item/search.jsp"%></c:when>
						<c:when test="${searchMode eq 'item' and selectTab eq 'quarter' }"><c:set var="itemType" value="Quarter"/><%@ include file="/WEB-INF/jspf/user/directory/address/item/search.jsp"%></c:when>
						<c:when test="${searchMode eq 'house'}"><%@ include file="/WEB-INF/jspf/user/directory/address/house/search.jsp" %></c:when>
						<c:otherwise>${mode}</c:otherwise>
					</c:choose>
				</c:otherwise>
			</c:choose>
		</td>
	</tr>
</table>