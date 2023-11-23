<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:sc>
	<c:set var="uiid" value="${u:uiid()}"/>
	<c:set var="processType" value="${ctxProcessTypeMap[process.typeId]}"/>

	<c:set var="editCommand">
		$('#${uiid} #show').hide(); $('#${uiid} #editorAdd').hide(); $('#${uiid} #editorChange').show();
		var $ta = $('#${uiid} #editorChange textarea'); $ta.focus();
		$ta[0].setSelectionRange( $ta[0].value.length, $ta[0].value.length );
		return false;
	</c:set>

	<script>
		const callback = function () {
			${editCommand }
		};
		doOnClick($('#${uiid} #show'), '', callback);
	</script>
	<c:set var="editStyle">cursor: pointer;</c:set>

	<div class="mt1"id="${uiid}">
		<div class="mt1 mb05" >
			<h2>${l.l('Description')}
				<span style="font-weight: normal;">
					<c:if test="${not empty processType}">
						<p:check action="ru.bgcrm.struts.action.ProcessAction:processDescriptionAdd">
							[<a href="#" title="${l.l('Добавить в конец описания текст с именем автора и временем')}"
								onclick="$('#${uiid} #editorChange').hide(); $('#${uiid} #editorAdd').show(); return false;">${l.l('add')}</a>]
						</p:check>

						<p:check action="ru.bgcrm.struts.action.ProcessAction:processDescriptionUpdate">
							<c:if test="${processType.properties.configMap['hideDescriptionChange'] ne 1}">
								[<a href="#" title="${l.l('Править описание целиком (также можете кликнуть мышью по описанию)')}"
									onclick="${editCommand}">${l.l('править целиком')}</a>]
							</c:if>
						</p:check>
					</c:if>
				</span>
			</h2>
		</div>

		<div class="box" id="show" style="padding: 0.5em; min-height: 2em; overflow: auto; ${editStyle}">
			<pre style="white-space: pre-wrap;">
<ui:text-prepare text="${process.description}"/>
			</pre>
		</div>

		<c:set var="command">if( sendAJAXCommand( formUrl( $(this.form) ), ['description'] ) ){ $$.ajax.load('${requestUrl}', $('#${tableId}').parent()); return false;}</c:set>

		<html:form action="/user/process" styleId="editorChange" style="display: none;" styleClass="editorStopReload">
			<input type="hidden" name="id" value="${process.id}"/>
			<input type="hidden" name="action" value="processDescriptionUpdate"/>

			<textarea name="description" class="mb1" rows="15" style="width: 100%; resize: vertical;">${process.description}</textarea>

			<button class="btn-grey" type="button" onClick="${command}">OK</button>
			<button class="btn-white ml1" type="button" onClick="$('#${uiid} #editorChange').hide(); $('#${uiid} #show').show(); return false;">${l.l('Отмена')}</button>
		</html:form>

		<html:form action="/user/process" styleId="editorAdd" style="display: none;" styleClass="editorStopReload">
			<input type="hidden" name="id" value="${process.id}"/>
			<input type="hidden" name="action" value="processDescriptionAdd"/>

			<input type="text" name="description" class="mb1 mt1" style="width: 100%;" onkeypress="if( enterPressed( event )){ ${command} }"/>

			<button class="btn-grey" type="button" onClick="${command}">OK</button>
			<button class="btn-white ml1" type="button" onClick="$('#${uiid} #editorAdd').hide(); $('#${uiid} #show').show(); return false;">${l.l('Отмена')}</button>
		</html:form>
	</div>
</u:sc>

<% out.flush(); %>