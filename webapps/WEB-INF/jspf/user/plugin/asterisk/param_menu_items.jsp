<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${paramValue.parameter.type eq 'phone' and ctxUser.checkPerm('/user/message/call:outCall')}">
	<c:set var="config" value="${ctxSetup.getConfig('MessageTypeConfig')}"/>
	<c:forEach var="type" items="${config.typeMap.values()}">
		<c:if test="${type.getClass().simpleName eq 'MessageTypeCall'}">
			<c:url var="urlBase" value="/user/message/call.do">
				<c:param name="method" value="outCall"/>
				<c:param name="typeId" value="${type.id}"/>
				<c:param name="processId" value="${process.id}"/>
			</c:url>

			${type.outNumbersPreprocess(paramValue.value, process)}

			<c:forEach var="item" items="${paramValue.value.itemList}">
				<c:url var="url" value="${urlBase}">
					<c:param name="number" value="${item.phone}"/>
				</c:url>

				<li><a href="tel:${item.phone}" onclick="$$.ajax.post('${url}')"><i class="ti-headphone-alt"></i> ${item}</a></li>
			</c:forEach>
		</c:if>
	</c:forEach>
</c:if>