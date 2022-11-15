<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Incoming variables:
	linkFormUiid
--%>

<u:sc>
	<c:set var="typeId" value="${processType.id}"/>

	<c:if test="${not empty createTypeList}">
		<html:form action="${form.httpRequestURI}">
			<input type="hidden" name="action" value="linkProcessCreate"/>
			<input type="hidden" name="id" value="${form.id}"/>

			<div class="in-table-cell pt1">
				<div style="width: 100%;">
					<ui:combo-single list="${createTypeList}" hiddenName="createTypeId" style="width: 100%;" onSelect="
						$$.ajax.load(
							'/user/process.do?showGroupSelect=1&action=processRequest&parentTypeId=${typeId}&createTypeId=' + this.value,
							$(this.form).find('#additionalParamsSelect'));">
						<jsp:attribute name="valuesHtml">
							<li value="0">-- ${l.l('значение не установлено')} --</li>
						</jsp:attribute>
					</ui:combo-single>
				</div>
				<div style="white-space: nowrap;" class="pl1">
					<c:set var="command">
						$$.ajax.post(this).done((result) => {
							if (result.data.process.id > 0) {
								$$.ajax.load('${form.requestUrl}', $('#${linkFormUiid}').parent(), {control: this});
							} else {
								<%-- open with wizard --%>
								const url = '/user/process.do?id=' + result.data.process.id + '&returnUrl=' + encodeURIComponent('${form.requestUrl}');
								$$.ajax.load(url, $('#${linkFormUiid}').parent(), {control: this});
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


