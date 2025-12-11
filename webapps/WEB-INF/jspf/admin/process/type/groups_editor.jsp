<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:sc>
	<c:set var="tabsId" value="${u:uiid()}"/>
	<ul>
		<c:forEach var="item" items="${ctxUserGroupRoleList}"><%--
		--%><c:set var="count" value="0"/><%--
		--%><c:forEach var="group" items="${groups}"><%--
			--%><c:if test="${item.id == group.roleId}"><%--
				--%><c:set var="count" value="${count + 1}"/><%--
			--%></c:if><%--
		--%></c:forEach><%--
		--%><li>
				<a href="#${tabsId}-${item.id}">${item.title}
					<c:if test="${count >0}">
						(${count})
					</c:if>
				</a>
			</li><%--
	--%></c:forEach>
	</ul>
	<c:forEach var="item" items="${ctxUserGroupRoleList}">
		<ui:select-mult
			id="${tabsId}-${item.id}"  name="${hiddenName}"
			showId="1" style="width:100%;"
			styleClass="layout-height-rest" list="${ctxUserCache.getUserGroupRoleFullTitledList(item.id)}" values="${groups.groupRoleIds}"/>
	</c:forEach>
</u:sc>