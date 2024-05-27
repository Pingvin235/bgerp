<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<div id="${uiid}">
	<c:choose>
		<c:when test="${stepData.houseId gt 0}">
			<c:url var="url" value="/user/directory/address.do">
				<c:param name="method" value="addressGet"/>
				<c:param name="addressHouseId" value="${stepData.houseId}"/>
				<c:param name="forwardFile" value="/WEB-INF/jspf/usermob/process/process/wizard/step_address_info_house_ref.jsp"/>
			</c:url>
			<c:import url="${url}"/>
		</c:when>
		<c:otherwise>
			<div class="odd">
				<div class="tableIndent">Дом не выбран.</div>
			</div>
		</c:otherwise>
	</c:choose>
</div>
