<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<div class="in-table-cell">
	<html:form action="${form.requestURI}" styleClass="in-table-cell">
		<html:hidden property="id"/>
		<html:hidden property="scope"/>
		<html:hidden property="method" value="generateDocument"/>
		<html:hidden property="objectType"/>
		<html:hidden property="objectId"/>

		<c:if test="${not empty patternList}">
			<div style="width: 100%;">
				<c:set var="selectUiid" value="${u:uiid()}"/>
				<c:set var="saveUiid" value="${u:uiid()}"/>
				<c:set var="streamUiid" value="${u:uiid()}"/>
				<c:set var="dynamParamDivUiid" value="${u:uiid()}"/>

				<c:set var="onChangeCode">
					var $selected = $('#${selectUiid}').find('li[selected]');
					if( $selected.length > 0 )
					{
						$$.ajax.load( '/user/empty.do?patternId='+$selected.val()+'&forwardFile=' + '/WEB-INF/jspf/user/plugin/document/additional_parameters.jsp' , $('#${dynamParamDivUiid}') );

						$('#${saveUiid}').toggle($selected.attr('save') == 'true');
						$('#${streamUiid}').toggle($selected.attr('stream') == 'true');
					}
				</c:set>

				<ui:combo-single
					id="${selectUiid}" hiddenName="patternId"
					prefixText="Шаблон:" style="width: 100%;"
					onSelect="${onChangeCode}">
					<jsp:attribute name="valuesHtml">
						<c:forEach var="item" items="${patternList}">
							<li value="${item.id}" save="${item.resultSave}" stream="${item.resultStream}">${item.title}</li>
						</c:forEach>
					</jsp:attribute>
				</ui:combo-single>
			</div>
		</c:if>

		<div id ="${dynamParamDivUiid}" style="white-space: nowrap;">
			<!-- сюда динамически подгружается форма доп параметров -->
		</div>

		<div class="pl1" style="white-space: nowrap;">
			<button type="button" class="btn-green" id="${saveUiid}"
				title="Сгенерировать документ с сохранением"
				onclick="$$.ajax.post(this.form).done(() => $$.ajax.load('${form.requestUrl}', $('#${uiid}').parent()))"
				style="display: none;">+</button>

			<c:set var="script">
				var debug = '';
				// Alt нажат
				if ($$.keys.altPressed()) {
					debug = '&debug=true';
				}
				window.open( formUrl(this.form) + '&responseType=stream' + debug, 'Print', 'menubar=1, scrollbars=1, height=800, width=800' );
			</c:set>

			<button type="button" class="btn-grey" id="${streamUiid}"
				title="Сгенерировать документ 'на лету', без сохранения"
				onclick="${script}"
				style="display: none;">=&gt;</button>
		</div>
	</html:form>

	<p:check action="ru.bgcrm.plugin.document.struts.action.DocumentAction:uploadDocument">
		<form action="${form.requestURI}" method="POST" enctype="multipart/form-data">
			<input type="hidden" name="method" value="uploadDocument"/>
			<input type="hidden" name="responseType" value="json"/>
			<input type="hidden" name="id" value="${id}"/>
			<input type="hidden" name="objectType" value="${form.param.objectType}"/>
			<input type="hidden" name="objectId" value="${form.param.objectId}"/>
			<div style="display: none; max-width: 0; max-height: 0;">
				<input type="file" name="file" multiple="true" style="visibility:hidden; display: none;"/>
			</div>
			<button type="button" class="btn-green ml1" onclick="$$.ajax.fileUpload(this.form).done(() => {
				$$.ajax.load('${form.requestUrl}', $('#${uiid}').parent());
			});" title="Загрузить документ">+?</button>
		</form>
	</p:check>
</div>

<%@ include file="/WEB-INF/jspf/error_div.jsp"%>

<table id="${uiid}" class="data mt05 hl">
	<tr>
		<td width="30">ID</td>
		<td>${l.l('Название')}</td>
		<td>${l.l('Created')}</td>
		<td width="30">&#160;</td>
	</tr>
	<c:forEach var="item" items="${frd.list}" >
		<c:url var="deleteUrl" value="${form.requestURI}">
			<c:param name="method" value="deleteDocument"/>
			<c:param name="id" value="${item.id}"/>
			<c:param name="fileId" value="${item.fileData.id}"/>
			<c:param name="fileSecret" value="${item.fileData.secret}"/>

			<c:param name="scope" value="${form.param['scope']}"/>
			<c:param name="objectType" value="${form.param['objectType']}"/>
			<c:param name="objectId" value="${form.param['objectId']}"/>
		</c:url>

		<tr>
			<td>${item.id}</td>
			<td><ui:file-link file="${item.fileData}"/></td>
			<td>${tu.format(item.fileData.time, 'ymdhms')}</td>
			<td>
				<ui:button type="del" styleClass="btn-small" onclick="
					$$.ajax
						.post('${deleteUrl}', {control: this})
						.done(() => $$.ajax.load('${form.requestUrl}', $('#${uiid}').parent()))"/>
			</td>
		</tr>
	</c:forEach>
</table>

<script>
	$(function () {
		$('#${uiid} a.preview').preview();
	})
</script>
