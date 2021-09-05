<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div style="width: 100%;">
	<div>
		#${message.id} EMail [${messageType.email}]: ${message.subject}
	</div>
	<div class="mt05">
		<c:choose>
			<c:when test="${message.direction eq 1}">
				${l.l('Отправлено')}: ${u:formatDate( message.fromTime, 'ymdhm' )} (<a href="mailto:${fn:escapeXml( message.from )}">${fn:escapeXml( message.from )}</a>) => ${fn:escapeXml( message.to )}
				<nobr>
					${l.l('Обработано')}: ${u:formatDate( message.toTime, 'ymdhm' )}
					(<ui:user-link id="${message.userId}"/>)
				</nobr>
			</c:when>
			<c:otherwise>
				${l.l('Создано')}: ${u:formatDate( message.fromTime, 'ymdhm' )} (<ui:user-link id="${message.userId}"/>)
				<nobr>
					${l.l('Отправлено')}: ${u:formatDate( message.toTime, 'ymdhm' )} (<a href="mailto:${fn:escapeXml( message.to )}">${fn:escapeXml( message.to )}</a>)
				</nobr>
			</c:otherwise>
		</c:choose>
	</div>
</div>