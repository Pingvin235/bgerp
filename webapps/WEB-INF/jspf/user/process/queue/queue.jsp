<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="title" value="Очереди процессов"/>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>

<%-- обновление очереди по переходу в неё --%>
<script>
    $(function () {
        var $contentDiv = $('#content > #log');
        
        // т.к. каждый раз UIID промотчика страниц разный - переопределение onShow
        $contentDiv.data('onShow', 
            function()
            {
                $("#${uiid} button[name='pageControlRefreshButton']").click();
                bgcrm.debug('processQueue', 'refresh queue', $("#${uiid} button[name='pageControlRefreshButton']"));
                addProcessQueueIdToUrl(${queue.id});
            });
    });
</script>


<div id="processQueueShow" >
	<table style="width: 100%;"><tr>
		<td nowrap="nowrap">
			<c:choose>
				<c:when test="${not empty form.response.data.list}">
					<c:set var="config" value="${u:getConfig( ctxUser.personalizationMap, 'ru.bgcrm.model.process.queue.config.SavedPanelConfig' )}"/>
					<c:set var="savedPanelMap" value="${config.savedPanelSet}"/>
					<c:set var="valuesHtml">
						<c:forEach items="${form.response.data.list}" var="item">
							<c:if test="${not savedPanelMap.contains(item.id)}">
								<li value="${item.id}" onclick="updateSelectedQueue('${item.id}');showSelectedQueue('${item.id}');"><div>${item.title}</div><div class="icon-add"></div></li>
								<script>
									$("li[value="+${item.id}+"]").find(".icon-add").click(function(event){
										event.stopPropagation();
										addToPanelScript('${item.id}','${item.title}',true);	
									})
								</script>
							</c:if>
						</c:forEach>
					</c:set>
					
					<c:set var="id" value="processQueueSelect"/>
					
					<ui:combo-single id="${id}" 
						value="${ctxUser.personalizationMap['queueLastSelected']}"
						widthTextValue="220px"
						prefixText="Очередь:"
						showFilter="1"
						onSelect="$('.text-value div.icon-add').remove()"
						valuesHtml="${valuesHtml}"/>
					
					<script>
						$(function()
						{
							$('#${id}').appendTo( $('#title > .status:visible > .wrap > .center') );
							
							<c:forEach items="${form.response.data.list}" var="item">	
								<c:if test="${savedPanelMap.contains(item.id)}">
										addToPanelScript('${item.id}', '${item.title}');
								</c:if>
							</c:forEach>
						
							var queueId = "${ctxUser.personalizationMap['queueLastSelected']}";
							
							var state = history.state;
							var pos = state.href.indexOf('#');
							if( state && pos > 0 )
							{
								queueId = state.href.substring( pos + 1 );	
							}
							
							//удаляем иконку добавления на панель из select при загрузке
							$('#processQueueSelect').find('.text-value div.icon-add').remove();
							showSelectedQueue( queueId );						
						})
					</script>
				</c:when>
				<c:otherwise>
					У пользователя <ui:doc-link url="83427342">нет разрешённых очередей</ui:doc-link>
				</c:otherwise>				
			</c:choose>	
		</td>
	</table>
		
	<div id="processQueueFilter" class="tableIndent">
		<%-- сюда динамически грузится фильтр --%>
	</div>
		
	<div id="processQueueData">
		<%-- сюда динамически грузится вывод очереди --%>
	</div>
</div>


<html:form action="/user/process" styleId="processQueueCreateProcess" style="display: none; width: 50%;" styleClass="center1020">
	<input type="hidden" name="action" value="processCreate"/>
	
	<div id="typeTree">
		<%-- сюда динамически грузится дерево типов для очереди --%>
	</div>
	<div id="groupSelect">
		<%-- сюда динамически грузятся группы решения --%>
	</div>
	
	<div id="additionalParamsSelect">
		<%-- сюда динамически грузятся доп параметры для данного типа процесса --%>
	</div>
	
	<div id="constPart" class="mt05">
		<b>Описание:</b><br/>
		<textarea name="description" rows="10" style="width: 100%;"></textarea>
		
		<c:set var="returnToShow">$('#processQueueCreateProcess').hide(); $('#processQueueShow').show();</c:set>
		<c:set var="saveCommand">
			var result = sendAJAXCommand( formUrl( this.form ) ); if( result ){ ${returnToShow} openProcess( result.data.process.id ); };
		</c:set>
		
		<div class="mt1 in-mr1">
			<button class="btn-grey" type="button" onclick="${saveCommand}">ОК</button>
			<button class="btn-grey" type="button" onclick="${returnToShow}">Отмена</button>
		</div>
	</div>
</html:form>