<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div id="linkObjects" style="display: none;">
	<div id="linkTable">
		<%-- сюда сгенерируется таблица с процессами --%>
	</div>
	<div class="hint">${l.l('Для привязки доступны процессы, выбранные в буфер')}.</div>
	
	<div class="tableIndent mt1">
		<c:set var="script">
			var forms = $( '#${uiid} #linkObjects form' );
			for( var i = 0; i < forms.length; i++ )
			{
				var form = forms[i];
				
				if( !form.check.checked )
				{
					continue;
				}
				if( !sendAJAXCommand( formUrl( form ) ) )
				{
					return;
				}		
			}	
			
			openUrlToParent( '${form.requestUrl}', $('#${uiid}') );
		</c:set>

		<ui:button type="ok" onclick="${script}"/>
		<ui:button type="cancel" onclick="$('#${uiid} #linkObjects').hide(); $('#${uiid} #addButton').show();"/>
	</div>
</div>