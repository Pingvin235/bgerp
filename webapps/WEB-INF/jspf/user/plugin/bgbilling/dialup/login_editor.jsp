<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="data" value="${form.response.data}"/>
<c:set var="uiid" value="${u:uiid()}"/>
<c:set var="updateCommand" value="$$.ajax.load('${form.requestUrl}', $('#${uiid}').parent());"/>

<h1>Редактирование логина</h1>

<c:set var="login" value="${data.login}"/>

<html:form action="/user/plugin/bgbilling/proto/dialup.do" styleId="${uiid}">
	<input type="hidden" name="action" value="updateLogin" />
	<html:hidden property="contractId"/>
	<html:hidden property="billingId"/>
	<html:hidden property="moduleId"/>
	<html:hidden property="id"/>

	<table style="width:100%">
		<tr class="in-pl1">
			<td>
				<h2>Логин</h2>
				<input type="text" style="width: 100%" disabled="disabled" value="${login.login}"/>
			</td>
			<td>
				<h2>Алиас(ы)</h2>
				<input type="text" name="alias" style="width: 100%" value="${login.alias}"/>
			</td>
			<td nowrap="nowrap">
				<h2>Период</h2>
				c
		   		<c:set var="editable" value="true"/>
				<input type="text" name="dateFrom" value="${tu.format( login.dateFrom, 'ymd' ) }" id="${uiid}-dateFrom"/>
				<c:set var="selector" value="#${uiid}-dateFrom"/>
				<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
				по
				<c:set var="editable" value="true"/>
				<input type="text" name="dateTo" value="${tu.format( login.dateTo, 'ymd' ) }" id="${uiid}-dateTo" />
				<c:set var="selector" value="#${uiid}-dateTo"/>
				<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
			</td>
			<td width="50%">
				<h2>Объект</h2>

				<u:sc>
					<c:set var="valuesHtml">
						<li value="0">Без объекта</li>
						<c:forEach var="item" items="${form.response.data.objectList}">
							<li value="${item.id}">${item.title}</li>
						</c:forEach>
					</c:set>
					<c:set var="hiddenName" value="param"/>
					<c:set var="value" value="${login.objectId}"/>
					<c:set var="style" value="width: 100%;"/>
					<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
				</u:sc>
			</td>
		</tr>
		<tr class="in-pl1 in-pt1">
			<td colspan="2" style="min-width: 250px;">
				<h2>Пароль</h2>

				<div class="in-table-cell in-pr1">
					<div style="width: 50%;">
						<input type="password" name="pswd1" style="width: 100%;" value="*******"/>
					</div>
					<div style="width: 50%;">
						<input type="password" name="pswd2" style="width: 100%;" value="*******"/>
					</div>
					<div style="white-space: nowrap;">
						<input type="checkbox" name="pswdAuto" value="1"/>&#160;&#160;авто
					</div>
				</div>
			</td>
			<td>
				<h2>Количество сессий</h2>

				<u:sc>
					<c:set var="valuesHtml">
						<li value="0">Неограниченно</li>
						<c:forEach var="item" begin="1" end="10">
							<li value="${item}">${item}</li>
						</c:forEach>
					</c:set>
					<c:set var="hiddenName" value="sessions"/>
					<c:set var="value" value="${login.session}"/>
					<c:set var="style" value="width: 100%;"/>
					<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
				</u:sc>
			</td>
			<td>
				<h2>Доступ</h2>

				<u:sc>
					<c:set var="valuesHtml">
						<li value="0">Разрешён</li>
						<li value="1">Запрещён</li>
					</c:set>
					<c:set var="hiddenName" value="param"/>
					<c:set var="value" value="${login.status}"/>
					<c:set var="style" value="width: 100%;"/>
					<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
				</u:sc>
			</td>
		</tr>
		<tr>
			<td colspan="4">
				<h2>Комментарий</h2>
				<textarea style="width: 100%; resize: vertical;" rows="3" name="comment">${login.comment}</textarea>
			</td>
		</tr>
	</table>
</html:form>

<c:set var="uiidForms" value="${u:uiid()}"/>

<c:if test="${form.id gt 0}">
	<table style="width: 100%;" id="${uiidForms}">
		<tr>
			<td width="50%" class="pr1">
				<%@ include file="login_editor_ip.jsp"%>
				<%@ include file="login_editor_pswd.jsp"%>
			</td>
			<td width="50%">
				<%@ include file="login_editor_radius.jsp"%>
			</td>
		</tr>
	</table>
</c:if>

<div class="mt1 mb1">
	<c:set var="saveScript">
		var result = sendAJAXCommand( formUrl( $('#${uiid}') ) );
		if( result )
		{
			$('#${uiidForms} > form.enable').each( function()
			{
				sendAJAXCommand( formUrl( this ) ) ;
			});
			$$.ajax.load('${form.returnUrl}', $('#${uiid}').parent());
		}
	</c:set>

	<button class="btn-grey" onclick="${saveScript}">OK</button>
	<button class="btn-grey ml1" onclick="$$.ajax.load('${form.returnUrl}', $('#${uiid}').parent())">Отмена</button>
</div>

