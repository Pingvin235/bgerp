<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<input type="hidden" name="method" value="slotProcess"/>
<input type="hidden" name="id" value="${form.id}"/>
<input type="hidden" name="processId" value="${form.param.processId}"/>
<input type="hidden" name="date" value="${form.param.date}"/>

<div class="tt bold">
	${frd.column.title}&nbsp;${form.param.date}
</div>

<table class="data hl mb1 mt1">
	<tr>
		<td></td>
		<td width="100%">${l.l('Time')}</td>
	</tr>
	<c:forEach var="item" items="${frd.times}">
		<tr>
			<td>
				<input type="radio" name="time" value="${item}"/>
			</td>
			<td>
				${item}
			</td>
		</tr>
	</c:forEach>
</table>

<div>
	<ui:button type="ok" styleClass="ok mr1"/>
	<ui:button type="cancel" onclick="$(this.form).dialog('close')"/>
</div>