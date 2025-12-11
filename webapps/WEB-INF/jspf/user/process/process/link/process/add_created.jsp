<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${not empty createTypeList}">
	<html:form action="${form.requestURI}">
		<input type="hidden" name="method" value="linkProcessCreate"/>
		<input type="hidden" name="id" value="${form.id}"/>

		<div class="in-table-cell pt05">
			<div style="width: 100%;">
				<ui:combo-single name="createTypeId" style="width: 100%;">
					<jsp:attribute name="valuesHtml">
						<li value="0">-- ${l.l('значение не установлено')} --</li>
						<c:forEach var="item" items="${createTypeList}">
							<c:set var="style" value="${item.second ? '' : ' style=\"text-decoration: line-through;\"'}"/>
							<li value="${item.first.id}">
								<span ${style}>${item.first.title}</span>
							</li>
						</c:forEach>
					</jsp:attribute>
				</ui:combo-single>
			</div>
			<div class="nowrap">
				<button type="button" class="btn-grey ml1" onclick="$$.process.link.process.createAndLink(this, '${form.returnUrl}')">${l.l('Создать и привязать')}</button>
				<button type="button" class="btn-white ml1" onclick="$$.ajax.load('${form.returnUrl}', $(this.form).parent());">${l.l('Cancel')}</button>
			</div>
		</div>
	</html:form>
</c:if>
