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
		
		<button class="btn-grey" type="button" onclick="${script}">OK</button>
		<button class="btn-grey ml05" type="button" onclick="$('#${uiid} #linkObjects').hide(); $('#${uiid} #addButton').show();">${l.l('Отмена')}</button>
	</div>
</div>