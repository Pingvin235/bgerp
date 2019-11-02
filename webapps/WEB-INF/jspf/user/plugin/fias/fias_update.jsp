<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<form action="plugin/fias.do" id="fiasUpdate" method="POST" enctype="multipart/form-data">
	<input type="hidden" name="action" value="updateFiasBase"/>
	<div>
		<h2>Обновление базы ФИАС</h2>
		<div style="display:table;width:100%" >
			<div style="display:table-cell;width:100%">
				<div style="display:table;width:100%">
					<div style="display:table-cell; white-space:nowrap">
						Файл с улицами (AS_ADDROBJ)
						</br>
						Файл с интервалами домов (AS_HOUSEINT)
					</div>
					<div style="display:table-cell;width:100%">
						<input type="file" name="file" style="width:100%"/>
					</div>
				</div>
			</div>
			<div style="display:table-cell;align:center;width:100%">
				<input type="button" value="Обновить ФИАС" onclick="$(this.form).submit();"/>
			</div>
		</div>
		</br>
		
		Лог
		<div style="overflow: auto; width: inherit; height: 500px;">
			<textarea name="updateLog" rows="10" cols="300" style="width:100%; height:100%;">${form.response.data.log}</textarea>
		</div>
	</div>
</form>

<script>
	$('#fiasUpdate').iframePostForm
	({
		json : true,
		post : function()
		{
			if( $('#fiasUpdate input[type=file]').val().length == 0 )
			{
				alert( "Не выбран файл!" );
				return false;
			}	
		},
		complete : function( response )
		{
			$('textarea[name=updateLog]').val(response.data.log);
		}
	});
</script>