<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="${form.httpRequestURI}">
	<html:hidden property="method"/>
	<input type="hidden" name="returnUrl" value="${form.requestUrl}"/>
	<html:hidden property="id"/>
	<html:hidden property="open"/>
	<html:hidden property="categoryId"/>

	<c:if test="${category.add}">
		<ui:button type="add" onclick="$$.process.link.process.addExisting(this)"/>
	</c:if>

	<ui:page-control nextCommand="; $$.ajax.load(this.form, $(this.form).parent())"/>
</html:form>

<c:set var="uiid" value="${u:uiid()}"/>

<table class="data hl mt05" id="${uiid}">
	<c:set var="allowLinkDelete" value="${ctxUser.checkPerm('org.bgerp.action.ProcessLinkProcessAction:linkProcessDelete')}"/>
	<tr>
		<c:if test="${allowLinkDelete}">
			<td class="min">&nbsp;</td>
		</c:if>
		<td class="min">ID</td>
		<td>${l.l('Type')}</td>
		<td>${l.l('Status')}</td>
		<td>${l.l('Description')}</td>
	</tr>

	<c:forEach var="item" items="${frd.list}">
		<c:set var="process" value="${item.second}"/>

		<c:url var="url" value="/user/process.do">
			<c:param name="method" value="process"/>
			<c:param name="id" value="${item.second.id}"/>
			<c:param name="mode" value="linked"/>
			<c:param name="returnUrl" value="${form.requestUrl}"/>
		</c:url>

		<tr openUrl="${url}" title="${l.l('Created')}: ${tu.format(process.createTime, 'ymdhms')}">
			<c:if test="${allowLinkDelete}">
				<td class="min">
					<c:url var="deleteUrl" value="${form.httpRequestURI}">
						<c:param name="method" value="linkProcessDelete"/>
						<c:param name="linkedObjectType" value="${item.first}"/>

						<c:choose>
							<c:when test="${category.link}">
								<c:param name="id" value="${form.id}"/>
								<c:param name="linkedObjectId" value="${process.id}"/>
							</c:when>
							<c:otherwise>
								<c:param name="id" value="${process.id}"/>
								<c:param name="linkedObjectId" value="${form.id}"/>
							</c:otherwise>
						</c:choose>
					</c:url>

					<ui:button type="del" styleClass="btn-small" onclick="
						$$.ajax.post('${deleteUrl}').done(() => {
							$$.ajax.load('${form.requestUrl}', $('#${uiid}').parent());
						})
					"/>
				</td>
			</c:if>
			<td class="min">
				<ui:process-link id="${process.id}"/>
			</td>
			<td>${process.type.title}</td>
			<td>${process.statusTitle}</td>
			<td>${process.reference().description()}</td>
		</tr>
	</c:forEach>
</table>

<script>
	$(function () {
		const $table = $('#${uiid}');
		const callback = ($row) => {
			const openUrl = $row.attr('openUrl');
			if (openUrl)
				$$.ajax.load(openUrl, $table.parent());
		};
		doOnClick($table, 'tr:gt(0)', callback);
	})
</script>