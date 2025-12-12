<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:sc>
	<c:set var="uiid" value="${u:uiid()}"/>
	<c:set var="processType" value="${ctxProcessTypeMap[process.typeId]}"/>

	<c:set var="editCommand">
		$$.process.descriptionEdit('${uiid}');
		return false;
	</c:set>

	<script>
		$$.doOnClick($('#${uiid} #show'), '', () => { ${editCommand} });
	</script>
	<c:set var="editStyle">cursor: pointer;</c:set>

	<div class="mt1" id="${uiid}">
		<div class="mt1 mb05" >
			<h2>${l.l('Description')}
				<span class="normal">
					<c:if test="${not empty processType}">
						<p:check action="/user/process:processDescriptionAdd">
							[<a href="#" title="${l.l('Добавить в конец описания текст с именем автора и временем')}"
								onclick="$('#${uiid} #editorChange').hide(); $('#${uiid} #editorAdd').show(); return false;">${l.l('add')}</a>]
						</p:check>

						<p:check action="/user/process:processDescriptionUpdate">
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

		<c:set var="command">$$.ajax.post(this.form).done(() => $$.ajax.load('${requestUrl}', $('#${tableId}').parent())); return false;</c:set>

		<html:form action="/user/process" styleId="editorChange" style="display: none;" styleClass="editorStopReload">
			<input type="hidden" name="id" value="${process.id}"/>
			<input type="hidden" name="method" value="processDescriptionUpdate"/>

			<textarea name="description" class="mb1" rows="15" style="width: 100%; resize: vertical;">${process.description}</textarea>

			<button class="btn-grey" type="button" onClick="${command}">OK</button>
			<button class="btn-white ml1" type="button" onClick="$('#${uiid} #editorChange').hide(); $('#${uiid} #show').show(); return false;">${l.l('Cancel')}</button>
		</html:form>

		<html:form action="/user/process" styleId="editorAdd" style="display: none;" styleClass="editorStopReload">
			<input type="hidden" name="id" value="${process.id}"/>
			<input type="hidden" name="method" value="processDescriptionAdd"/>

			<input type="text" name="description" class="mb1 mt1" style="width: 100%;" onkeypress="if( enterPressed( event )){ ${command} }"/>

			<button class="btn-grey" type="button" onClick="${command}">OK</button>
			<button class="btn-white ml1" type="button" onClick="$('#${uiid} #editorAdd').hide(); $('#${uiid} #show').show(); return false;">${l.l('Cancel')}</button>
		</html:form>
	</div>
</u:sc>

<% out.flush(); %>