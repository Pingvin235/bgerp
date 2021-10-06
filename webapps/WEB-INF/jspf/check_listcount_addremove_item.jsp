<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:choose>
	<c:when test="${values.keySet().contains(itemId)}">
		<tr id="${uiid}row${itemId}" style="width:100%;">
	</c:when>
	<c:otherwise>
		<tr id="${uiid}row${itemId}" style="display:none; width:100%;">
	</c:otherwise>
</c:choose>
	<td style="display:none" align="center">
		<input type="checkbox" name="${paramName}" value="${itemId}:${values[itemId].paramDouble}:${list[itemId]}" ${u:checkedFromCollection( values.keySet(), itemId )} ${addToInput}/>
	</td>
	<td>
		<input type="button" value=" X " title="Удалить"
		onclick="${beforeDelCommand};delParameter('${uiid}',${itemId},'${fn:escapeXml( list[itemId] )}', '${paramName}');${afterDelCommand}" />
	</td>
	
	<td nowrap="nowrap" style="width:100%">${list[itemId]}</td>
	<td align="right">
		<input type="text" value="${values[itemId].paramDouble}" style="width:45px"
			 onkeydown="return isNumberKey(event)" 
			 onkeyup="$('#'+'${uiid}'+'row'+'${itemId}').find('input[type=checkbox]').first().val('${itemId}:'+$(this).val()+':${fn:escapeXml( list[itemId] )}');"/>
	</td>
	
	<td	<c:if test="${config['allowComment'] !=1}">style="display:none"</c:if> >
		<input style="display:none" type="checkbox" name="comment" value="${values[itemId].paramString}" ${u:checkedFromCollection( values.keySet(), itemId )} />		
		<input type="text" value="${values[itemId].paramString}" style="width: 300px"
			onkeyup="$(this).prev().val( $(this).val() );" />
	</td>
