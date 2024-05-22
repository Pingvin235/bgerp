<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div style="width: 100%;">
	<div>
		<%@ include file="/WEB-INF/jspf/user/message/message_direction.jsp"%>
		#${message.id}&nbsp;${messageType.title} | ${message.subject}
	</div>
	<div class="mt05">
		<c:choose>
			<c:when test="${message.direction eq 1}">
				${l.l('Отправлено')}: ${tu.format( message.fromTime, 'ymdhm' )} (<a href="mailto:${u.escapeXml( message.from )}">${u.escapeXml( message.from )}</a>) => ${u.escapeXml( message.to )}
				<nobr>
					${l.l('Обработано')}: ${tu.format( message.toTime, 'ymdhm' )}
					(<ui:user-link id="${message.userId}"/>)
				</nobr>
			</c:when>
			<c:otherwise>
				${l.l('Создано')}: ${tu.format( message.fromTime, 'ymdhm' )} (<ui:user-link id="${message.userId}"/>)
				<nobr>
					${l.l('Отправлено')}: ${tu.format( message.toTime, 'ymdhm' )} (<a href="mailto:${u.escapeXml( message.to )}">${u.escapeXml( message.to )}</a>)
				</nobr>
			</c:otherwise>
		</c:choose>
	</div>
</div>