<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Incoming variables:
	listValues      - available list values
	value           - current values
	multiple        - multiple values
	listParamConfig - list parameter configuration
--%>

<c:choose>
	<c:when test="${multiple}">
		<c:forEach var="item" items="${listValues}">
			<table style="width: 100%;" class="nopad">
				<tr>
					<c:set var="checkUiid" value="${u:uiid()}" />
					<c:set var="tdUiid" value="${u:uiid()}" />

					<c:set var="commentInputShow"
						value="${not empty listParamConfig.commentValues[item.id]}" />

					<c:set var="scriptCheck">
						<c:if test="${commentInputShow}">
							onchange="if( this.checked ){ $('#${tdUiid}').show() } else { $('#${tdUiid}').hide() }"
						</c:if>
					</c:set>
					<c:set var="scriptInput">
						<c:if test="${commentInputShow}">
							onchange="$('#${checkUiid}').val( ${item.id} + ':' + this.value )"
						</c:if>
					</c:set>

					<c:set var="hideStyle">
						<c:if test="${not commentInputShow or value[item.id] == null}">
							style="display: none"
						</c:if>
					</c:set>

					<td width="30" align="center">
						<input type="checkbox" name="value" value="${item.id}:${value[item.id]}" id="${checkUiid}"	${u:checkedFromCollection( value, item.id )} ${scriptCheck} />
					</td>
					<td>
						${item.title}
						<span id="${tdUiid}" ${hideStyle}>
							<input type="text" size="30" value="${value[item.id]}" ${scriptInput} />
							<c:if test="${not empty listParamConfig.needCommentValues[item.id]}">
								*
							</c:if>
						</span>
					</td>
				</tr>
			</table>
		</c:forEach>
	</c:when>
	<c:otherwise>
		<c:set var="valueUiid" value="${u:uiid()}"/>
		<c:set var="commentUiid" value="${u:uiid()}"/>
		<c:set var="fullUiid" value="${u:uiid()}"/>

		<c:set var="currentValue" value="0"/>
		<c:set var="currentComment" value=""/>
		<c:set var="currentFull" value=""/>

		<c:forEach var="item" items="${value}">
			<c:set var="currentValue" value="${item.key}"/>
			<c:set var="currentComment" value="${item.value}"/>
			<c:set var="currentFull" value="${item.key}:${item.value}"/>
		</c:forEach>

		<input id="${valueUiid}" type="hidden" value="${currentValue}" />
		<input id="${fullUiid}" type="hidden" name="value" value="${currentFull}" />

		<%-- значения с комментарием --%>
		<c:set var="commentValues" value="" />
		<c:forEach var="item" items="${listValues}">
			<c:if test="${not empty listParamConfig.commentValues[item.id]}">
				<c:if test="${not empty commentValues}">
					<c:set var="commentValues" value="${commentValues}," />
				</c:if>
				<c:set var="commentValues" value="${commentValues}'${item.id}'" />
			</c:if>
		</c:forEach>

		<c:set var="commentValues" value="[${commentValues}]" />

		<c:set var="changeScript">
			var val = $('#${valueUiid}').val();
			console.log( val );
			$('#${fullUiid}').val( val + ':' + $('#${commentUiid}').val() );
			if( ${commentValues}.indexOf( val ) >= 0 ){ $('#${commentUiid}').show() } else { $('#${commentUiid}').hide() };
			<c:if test="${saveOn eq 'select'}">
				${saveCommand}
			</c:if>
		</c:set>

		<c:set var="editAs" value="${parameter.configMap.editAs}"/>

		<c:choose>
			<c:when test="${editAs eq 'radio'}">
				<div>
					<input type="radio" name="rValue" value="0"
						checked="1"
						onchange="if( this.checked ){ $('#${valueUiid}').val( this.value );  ${changeScript} }"/>
						&#160;-- ${l.l('undefined')} --
				</div>
				<c:forEach var="item" items="${listValues}">
					<div class="mt05">
						<input type="radio" id="${radioId}" name="rValue"
							value="${item.id}" ${u:checkedFromCollection( value, item.id )}
							onchange="if( this.checked ){ $('#${valueUiid}').val( this.value );  ${changeScript} }"/>
						&#160;${item.title}
					</div>
				</c:forEach>
			</c:when>
			<c:when test="${editAs eq 'select'}">
				<ui:select-single list="${listValues}" value="${currentValue}" onSelect="$('#${valueUiid}').val( $hidden.val() ); ${changeScript}" styleClass="w100p"/>
			</c:when>
			<c:otherwise>
				<ui:combo-single value="${currentValue}" style="width: 100%;" onSelect="$('#${valueUiid}').val($hidden.val()); ${changeScript}">
					<jsp:attribute name="valuesHtml">
						<li value="0">-- ${l.l('значение не установлено')} --</li>
						<c:forEach var="item" items="${listValues}">
							<li value="${item.id}"	${u:selectedFromCollection( value, item.id )}>${item.title}</li>
						</c:forEach>
					</jsp:attribute>
				</ui:combo-single>
			</c:otherwise>
		</c:choose>

		<c:set var="commentDisplayStyle">display:none;</c:set>
		<c:if test="${not empty listParamConfig.commentValues[currentValue]}">
			<c:remove var="commentDisplayStyle"/>
		</c:if>

		<input id="${commentUiid}" type="text" style="width: 100%; ${commentDisplayStyle}" onchange="${changeScript}"
				value="${currentComment}" placeholder="${l.l('Комментарий')}" class="mt1"/>
	</c:otherwise>
</c:choose>