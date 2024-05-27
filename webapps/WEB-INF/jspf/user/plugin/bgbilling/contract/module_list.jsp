<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<div class="in-table-cell" id="${uiid}">
	<form style="width: 300; vertical-align: top;" action="/user/plugin/bgbilling/proto/contract.do" id="selected">
		<input type="hidden" name="method" value="updateModules"/>
		<input type="hidden" name="command" value="del"/>
		<input type="hidden" name="billingId" value="${form.param.billingId}"/>
		<input type="hidden" name="contractId" value="${form.param.contractId}"/>

		<h2>Выбранные модули</h2>
		<c:forEach var="item" items="${frd.selectedList}">
			<div class="mb05"><input type="checkbox" name="moduleId" value="${item.id}"/>&nbsp;${item.title}</div>
		</c:forEach>

		<button type="button" class="btn-grey mb1"
			onclick="if (confirm('Удаление модулей приведёт к удалению данных,\nвы уверены?')) $$.ajax.post(this.form).done(() => $$.ajax.load('${form.requestUrl}', $('#${uiid}').parent()))">
			&gt;&gt;&gt;
		</button>
	</form>
	<form style="width: 300; vertical-align: top;" action="/user/plugin/bgbilling/proto/contract.do" id="available">
		<input type="hidden" name="method" value="updateModules"/>
		<input type="hidden" name="command" value="add"/>
		<input type="hidden" name="billingId" value="${form.param.billingId}"/>
		<input type="hidden" name="contractId" value="${form.param.contractId}"/>

		<h2>Доступные модули</h2>
		<c:forEach var="item" items="${frd.availableList}">
			<div class="mb05"><input type="checkbox" name="moduleId" value="${item.id}"/>&nbsp;${item.title}</div>
		</c:forEach>

		<button type="button" class="btn-grey"
			onclick="$$.ajax.post(this.form).done(() => $$.ajax.load('${form.requestUrl}', $('#${uiid}').parent()))">&lt;&lt;&lt;</button>
	</form>
</div>
