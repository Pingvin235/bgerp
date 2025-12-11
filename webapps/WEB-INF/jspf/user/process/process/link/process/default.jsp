<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<script>
	$(function () {
		$$.ui.ifaceStateTabUpdate('${uiid}', '${ifaceState.formattedState}');
	})
</script>

<c:choose>
	<c:when test="${empty config}">
		${l.l('no.link.categories.configured')}
	</c:when>
	<c:otherwise>
		<html:form action="${form.requestURI}" styleClass="mb05">
			<html:hidden property="method"/>
			<html:hidden property="id"/>
			<html:hidden property="ifaceId"/>
			<html:hidden property="ifaceState"/>

			<ui:combo-single name="open" value="${form.param.open}" onSelect="$$.ajax.load(this.form, $(this.form).parent())"
				prefixText="${l.l('Open')}:" styleClass="mr05" widthTextValue="5em">
				<jsp:attribute name="valuesHtml">
					<li value="">${l.l('All')}</li>
					<li value="true">${l.l('Yes')}</li>
					<li value="false">${l.l('No')}</li>
				</jsp:attribute>
			</ui:combo-single>

			<c:if test="${not empty createTypeList and ctxUser.checkPerm('/user/process/link/process:addCreated')}">
				<c:url var="url" value="${form.requestURI}">
					<c:param name="method" value="addCreated"/>
					<c:param name="id" value="${form.id}"/>
					<c:param name="returnUrl" value="${form.requestUrl}"/>
				</c:url>

				<ui:button type="add" onclick="$$.ajax.load('${url}', $(this.form).parent())" styleClass="ml1"/>
			</c:if>
		</html:form>

		<script>
			$(function () {
				const $tabs = $('#${uiid}').tabs({refreshButton: true});
				<c:forEach var="item" items="${config.categories.values()}">
					<c:url var="url" value="${form.requestURI}">
						<c:param name="method" value="showCategory"/>
						<c:param name="id" value="${form.id}"/>
						<c:param name="open" value="${form.param.open}"/>
						<c:param name="categoryId" value="${item.id}"/>
					</c:url>
					$tabs.tabs('add', '${url}', '${item.title}');
				</c:forEach>
			})
		</script>
	</c:otherwise>
</c:choose>

<div id="${uiid}">
	<ul></ul>
</div>

