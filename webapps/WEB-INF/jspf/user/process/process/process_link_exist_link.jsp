<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div id="linkObjects" style="display: none;">
	<div id="linkTable">
		<%-- сюда сгенерируется таблица с процессами --%>
	</div>
	<div class="hint">${l.l('Для привязки доступны процессы, выбранные в буфер')}.</div>

	<div class="tableIndent mt1">
		<c:set var="script">
			const deffs = [];

			const forms = $('#${uiid} #linkObjects form');
			for (var i = 0; i < forms.length; i++) {
				var form = forms[i];

				if (!form.check.checked)
					continue;
				
				deffs.push($$.ajax.post(form));
			}
			$.when.apply($, deffs).done(() => {
				$$.ajax.load('${form.requestUrl}', $('#${uiid}').parent());
			});
		</c:set>

		<ui:button type="ok" onclick="${script}"/>
		<ui:button type="cancel" styleClass="ml1" onclick="$('#${uiid} #linkObjects').hide(); $('#${uiid} #addButton').show();"/>
	</div>
</div>