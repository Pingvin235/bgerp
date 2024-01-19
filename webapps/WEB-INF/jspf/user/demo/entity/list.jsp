<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="editAllowed" value="${ctxUser.checkPerm('${form.httpRequestURI}:entityGet')}"/>
<c:set var="deleteAllowed" value="${ctxUser.checkPerm('${form.httpRequestURI}:entityDelete')}"/>
<c:set var="changeAllowed" value="${editAllowed or deleteAllowed}"/>

<html:form action="${form.httpRequestURI}">
	<html:hidden property="action"/>
	<c:if test="${editAllowed}">
		<c:url var="url" value="${form.httpRequestURI}">
			<c:param name="action" value="entityGet"/>
			<c:param name="returnUrl" value="${form.requestUrl}"/>
		</c:url>
		<ui:button type="add" styleClass="mr1" onclick="$$.ajax.load('${url}', $(this.form).parent(), {control: this})"/>
	</c:if>
	<ui:input-text name="filter" value="${form.param.filter}" onSelect="$$.ajax.load(this.form, $(this.form).parent())"/>
	<ui:page-control/>
</html:form>

<table class="data hl mt1">
	<tr>
		<c:if test="${changeAllowed}">
			<td width="1em"></td>
		</c:if>
		<td width="30em">ID</td>
		<td width="100%">Title</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
			<c:if test="${changeAllowed}">
				<td class="nowrap">
					<c:if test="${editAllowed}">
						<c:url var="url" value="${form.httpRequestURI}">
							<c:param name="action" value="entityGet"/>
							<c:param name="returnUrl" value="${form.requestUrl}"/>
							<c:param name="id" value="${item.id}"/>
						</c:url>
						<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.load('${url}', $(this).closest('table').parent(), {control: this})"/>
					</c:if>
					<c:if test="${deleteAllowed}">
						<c:url var="url" value="${form.httpRequestURI}">
							<c:param name="action" value="entityDelete"/>
							<c:param name="id" value="${item.id}"/>
						</c:url>
						<ui:button type="del" styleClass="btn-small ml05" onclick="$$.ajax.post('${url}', {control: this}).done(() => $$.ajax.load('${form.requestUrl}', $(this).closest('table').parent()))"/>
					</c:if>
				</td>
			</c:if>
			<td>${item.id}</td>
			<td>${item.title}</td>
		</tr>
	</c:forEach>
</table>