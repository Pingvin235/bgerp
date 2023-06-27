<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
<c:set var="uploadFormId" value="${u:uiid()}"/>

<div class="in-table-cell" id="${uiid}">
	<html:form action="/user/plugin/document/document" style="width: 100%;" styleClass="in-table-cell">
		<html:hidden property="id"/>
		<html:hidden property="scope"/>
		<html:hidden property="action" value="generateDocument"/>
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
				onclick="if( sendAJAXCommand( formUrl( this.form ) ) ){ $$.ajax.load('${form.requestUrl}', $('#${uiid}').parent()) };"
				style="display: none;">+</button>

			<c:set var="script">
				var debug = '';
				// Alt нажат
				if (bgcrm.keys.altPressed()) {
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
		<form id="${uploadFormId}" action="/user/plugin/document/document.do" method="POST" enctype="multipart/form-data" name="form">
			<input type="hidden" name="action" value="uploadDocument"/>
			<input type="hidden" name="responseType" value="json"/>
			<input type="hidden" name="id" value="${id}"/>
			<input type="hidden" name="objectType" value="${form.param.objectType}"/>
			<input type="hidden" name="objectId" value="${form.param.objectId}"/>
			<div style="display: none; max-width: 0; max-height: 0;">
				<input type="file" name="file" style="visibility:hidden; display: none;"/>
			</div>
			<button type="button" class="btn-green ml1" onclick="$$.ajax.triggerUpload('${uploadFormId}');" title="Загрузить документ">+?</button>
		</form>
	</p:check>
</div>


<script>
	$$.ajax.upload('${uploadFormId}', 'document-upload', function () {
		$$.ajax.load('${form.requestUrl}', $('#${uiid}').parent());
	});
</script>

<%@ include file="/WEB-INF/jspf/error_div.jsp"%>

<table style="width: 100%;" class="data mt05">
	<tr>
		<td width="30">ID</td>
		<td>${l.l('Название')}</td>
		<td>${l.l('Creation time')}</td>
		<td width="30">&#160;</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}" >
		<c:url var="url" value="/user/file.do">
			<c:param name="id" value="${item.fileData.id}"/>
			<c:param name="title" value="${item.fileData.title}"/>
			<c:param name="secret" value="${item.fileData.secret}"/>
		</c:url>
		<c:url var="deleteAjaxUrl" value="plugin/document/document.do">
			<c:param name="action" value="deleteDocument"/>
			<c:param name="id" value="${item.id}"/>
			<c:param name="fileId" value="${item.fileData.id}"/>
			<c:param name="fileSecret" value="${item.fileData.secret}"/>

			<c:param name="scope" value="${form.param['scope']}"/>
			<c:param name="objectType" value="${form.param['objectType']}"/>
			<c:param name="objectId" value="${form.param['objectId']}"/>
		</c:url>
		<c:set var="deleteAjaxCommandAfter">$$.ajax.load('${form.requestUrl}', $('#${uiid}').parent())</c:set>

		<tr>
			<td>${item.id}</td>
			<td><a href="${url}" target="_blank">${item.fileData.title}</a></td>
			<td>${tu.format(item.fileData.time, 'ymdhms')}</td>
			<td nowrap="nowrap"><%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%></td>
		</tr>
	</c:forEach>
</table>
