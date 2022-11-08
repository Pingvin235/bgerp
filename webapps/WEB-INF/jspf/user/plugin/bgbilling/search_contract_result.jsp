<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div>
	<c:set var="result" value="${form.response.data.list}"/>
	<c:set var="billingId" value="${form.param.billingId}"/>

	<html:form action="/user/plugin/bgbilling/proto/contract.do" method="GET" styleId="searchForm-bgbilling-searchContract" styleClass="searchForm">
		<html:hidden property="action" value="searchContract"/>
		<html:hidden property="billingId"/>
		<html:hidden property="searchBy"/>
		<html:hidden property="title"/>
		<html:hidden property="comment"/>
		<html:hidden property="street"/>
		<html:hidden property="house"/>
		<html:hidden property="flat"/>
		<html:hidden property="streetId"/>
		<html:hidden property="houseId"/>

		<c:forEach var="item" items="${ctxSetup.subIndexed( 'bgbilling:search.' ).values()}">
			<c:choose>
				<c:when test="${item.type eq 'dialUpLogin'}">
					<html:hidden property="dialUpLogin_${item.billingId}_${item.moduleId}"/>
				</c:when>
			</c:choose>
		</c:forEach>

		<c:set var="searchBy" value="${form.param.searchBy}"/>
		<c:if test="${not (searchBy eq 'id' or searchBy  eq 'dialUpLogin' ) }">
			<c:set var="nextCommand" value="; $$.ajax.load(this.form, $(this.form).parent());" scope="request"/>
			<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
		</c:if>
	</html:form>

	<h2>Биллинг: ${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[billingId].title}</h2>

	<table style="width: 100%;" class="data">
		<c:choose>
			<c:when test="${searchBy eq 'address'}">
				<tr>
					<td width="30">ID</td>
					<td>Название</td>
					<td>Адрес</td>
				</tr>

				<c:forEach var="item" items="${result}">
					<c:set var="contract" value="${item.object}"/>
					<tr>
						<td>${contract.id}</td>
						<td><a href="#" onclick="bgbilling_openContract( '${billingId}', '${contract.id}' ); return false;">${contract.title} [${contract.comment}]</a></td>
						<td>${item.value}</td>
					</tr>
				</c:forEach>
			</c:when>

			<c:when test="${searchBy.startsWith( 'dialUpLogin' ) }">
				<tr>
					<td width="30">ID</td>
					<td>${l.l('Договор')}</td>
					<td>${l.l('Логин')}</td>
					<td>${l.l('Алиас(ы)')}</td>
				</tr>
				<c:forEach var="login" items="${result}">
					<tr>
						<td>${login.contractId}</td>
						<td><a href="#" onclick="bgbilling_openContract( '${billingId}', '${login.contractId}' ); return false;">${login.contractTitle}</a></td>
						<td>${login.login}</td>
						<td>${login.alias}</td>
					</tr>
				</c:forEach>
			</c:when>
			<c:otherwise>
				<tr>
					<td width="30">ID</td>
					<td>Название</td>
				</tr>

				<c:forEach var="contract" items="${result}">
					<tr>
						<td>${contract.id}</td>
						<td><a href="#" onclick="bgbilling_openContract( '${billingId}', '${contract.id}' ); return false;">${contract.title}</a></td>
					</tr>
				</c:forEach>
			</c:otherwise>
		</c:choose>
	</table>
</div>
