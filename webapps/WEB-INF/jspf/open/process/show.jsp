<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="process" value="${form.response.data.process}"/>
<c:if test="${not empty process}">
	<div class="w100p" style="display: flex;">
		<div style="flex-basis: 50%; display: flex; flex-direction: column;">
			<div>
				<h2 style="margin-top: 0;">${l.l('Description')}</h2>
			</div>

			<div class="box" id="show" style="padding: 0.5em; min-height: 2em; overflow: auto; flex-basis: 100%;">
				<pre style="white-space: pre-wrap;">
<ui:text-prepare text="${process.description}"/>
				</pre>
			</div>
		</div>
		<div class="pl1" style="flex-basis: 50%;">
			<shell:title>
				<jsp:attribute name="text">
					<%@ include file="/WEB-INF/jspf/user/process/process/process_title_reference.jsp"%>
				</jsp:attribute>
			</shell:title>

			<%@ include file="/WEB-INF/jspf/user/process/process/process_header.jsp"%>
			<u:sc>
				<c:set var="objectType" value="process"/>
				<c:set var="paramIds" value="${config.showParamIds.clone()}"/>
				<c:set var="null">${paramIds.retainAll(process.type.properties.parameterIds)}</c:set>
				<%@ include file="/WEB-INF/jspf/open/parameter_list.jsp"%>
			</u:sc>
		</div>
	</div>
	<div>
		<h2>${l.l('Сообщения')}</h2>
		<plugin:include endpoint="open.process.message.add.jsp"/>
		<c:import url="/open/process.do?action=messages&id=${process.id}&secret=${form.param.secret}"/>
	</div>
</c:if>
