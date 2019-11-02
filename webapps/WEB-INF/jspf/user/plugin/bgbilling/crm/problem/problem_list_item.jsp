<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="problem" value="${form.response.data.problem}"/> 

<tr>
	<c:if test="${not form.param.showOnly}">
		<td align="center">
			<c:url var="editUrl" value="plugin/bgbilling/proto/billingCrm.do">
				<c:param name="action" value="getRegisterProblem"/>
				<c:param name="problemId" value="${problem.getId()}"/>
				<c:param name="billingId" value="${form.param.billingId}"/>
				<c:param name="processId" value="${form.param.processId}"/>
				<c:param name="description" value="${problem.getComment()}"/>
			</c:url>
			<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
		</td>
	</c:if>
	<td align="center">${form.param.billingId }</td>				
	<td align="center">${problem.getId()}</td>
	<td align="center">${problem.getGroupTitle()}</td>
	<td align="center">${problem.getStatusTitle()}</td>	
	<td align="center">${problem.getStatusDate()}</td>	
	<td align="center">${problem.getStatusUser()}</td>	
	<td align="center">${problem.getDuration()}</td>
</tr>
<tr>
	<td colspan="9">${problem.getComment()}</td>
</tr>

