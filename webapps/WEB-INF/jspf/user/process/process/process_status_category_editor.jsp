<%@ page contentType="text/html; charset=UTF-8" %>
<%@ include file="/WEB-INF/jspf/taglibs.jsp" %>


<c:set var="statusEditorUiid" value="${form.param.statusEditorUiid}"/>
<c:set var="statusSelectUiid" value="${form.param.statusSelectUiid}"/>
<c:set var="statusId" value="${form.param.statusId}"/>
<c:set var="typeId" value="${form.param.typeId}"/>
<c:set var="processType" value="${ctxProcessTypeMap[u:int(typeId)]}"/>
<c:set var="processId" value="${form.param.processId}"/>

<c:set var="key" value="categoryParamMap.${statusId}"/>
<c:set var="paramId" value="${processType.properties.configMap.get(key)}"/>

<c:if test="${empty paramId and processType.properties.closeStatusIds.contains(u:int(statusId))}">
    <c:set var="paramId" value="${processType.properties.configMap['categoryParamId']}"/>
</c:if>

<c:if test="${not empty paramId}">
    <script>
        var url = "/user/parameter.do?action=parameterGet&hideButtons=1&id=" + ${processId} + "&paramId=" + ${paramId} ;
        openUrlTo( url, $('#${statusEditorUiid}-categoryForm') );
    </script>

    <div class="mt1">Категория:</div>
    <div id="${statusEditorUiid}-categoryForm" type="editor" class="mb1">
    </div>
</c:if>

<div id="editor" type="editor" style="" class="mt1 mb1">
    <c:set var="okCommand">

        var url =formUrl( $('#${statusEditorUiid}-categoryForm form') );
        var statusChangeUrl = formUrl( $('#${statusEditorUiid} #statusEdit')[0] );

        var requestFormUrl = '${form.param.requestFormUrl}';
        var processId = '${form.param.processId}';

        // сохранение категории
        if( url && url.length>0 && !sendAJAXCommand( url ) )
        {
          return;
        };

        // изменение статуса
        if( sendAJAXCommand( statusChangeUrl ) )
        {
           openUrlToParent( '${form.returnUrl}', $('#${form.returnChildUiid}') );
        };
    </c:set>
    <c:set var="cancelCommand">
        $('#${statusSelectUiid} .text-value').text(''); $('#${statusEditorUiid} div[type=editor]').hide();
    </c:set>

    <button class="btn-grey mr1" type="button" onclick="${okCommand}">OK</button>
    <button class="btn-grey" type="button" onclick="${cancelCommand}">${l.l('Отмена')}</button>
</div>
