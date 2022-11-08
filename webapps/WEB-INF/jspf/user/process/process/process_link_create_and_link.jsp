<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="typeId" value="${processType.id}"/>

<u:sc>
	<c:if test="${not empty createTypeList}">
		<c:set var="uiid" value="${u:uiid()}"/>

		<html:form action="${form.httpRequestURI}" styleId="${uiid}" >
			<input type="hidden" name="action" value="linkProcessCreate"/>
			<input type="hidden" name="id" value="${form.id}"/>

			<div class="in-table-cell pt1">
				<div style="width: 100%;">
					<u:sc>
						<c:set var="valuesHtml">
							<li value="0">-- ${l.l('значение не установлено')} --</li>
						</c:set>
						<c:set var="list" value="${createTypeList}"/>
						<c:set var="hiddenName" value="createTypeId"/>
						<c:set var="style" value="width: 100%;"/>
						<c:set var="onSelect" value="$$.ajax.load('/user/process.do?showGroupSelect=1&action=processRequest&parentTypeId=${typeId}&createTypeId=' + $hidden.val(), $('#${uiid}').parent().find('#additionalParamsSelect'));"/>
						<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
					</u:sc>
				</div>
				<div style="white-space: nowrap;" class="pl1">
					<c:set var="command">
						$$.ajax.post(this).done((result) => {
							if (result.data.process.id > 0) {
								$$.ajax.load('${requestUrl}', $('#${uiid}').parent(), {control: this});
							} else {
								<%-- open with wizard --%>
								const url = '/user/process.do?id=' + result.data.process.id + '&returnUrl=' + encodeURIComponent('${requestUrl}');
								$$.ajax.load(url, $('#${uiid}').parent(), {control: this});
							}
						});
					</c:set>
					<button type="button" class="btn-grey" onclick="${command}">${l.l('Создать и привязать')}</button>
				</div>
			</div>

			<div id="additionalParamsSelect">
				<%-- сюда динамически грузятся доп параметры для данного типа процесса --%>
			</div>
		</html:form>
	</c:if>

</u:sc>


