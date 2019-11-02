<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:sc>
  	<c:set var="title">
 	  	<c:choose>
 	  		<c:when test="${not empty process.reference}">
 	  			${process.reference}
 	  		</c:when>
 	  		<c:otherwise>
 	  			#${process.id}&nbsp;${fn:escapeXml(processType.title)}
 	  		</c:otherwise>
 	  	</c:choose>
   	</c:set> 
 	  	 
 	<%-- если описание не содержит HTML разметки - оборачиваем его в <span class='title'> --%>
 	<c:if test="${not title.contains( '<' ) }">
 		<c:set var="title">
			<span class='title' id='process_title_${process.id}'>${title}</span>
		</c:set>
 	</c:if>
 			
	<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>
	<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>
</u:sc>