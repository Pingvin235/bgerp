<%@ tag body-content="empty" pageEncoding="UTF-8" description="Группа меню"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%-- 
  Группа меню.
--%>
<%@ attribute name="title" description="Заголовок"%>
<%@ attribute name="subitems" description="Подпункты"%>

<c:if test="${not empty subitems}">
<li>
	<a href="#">${title}</a>
	<ul>
		${subitems}	
	</ul>
</li>	
</c:if>
