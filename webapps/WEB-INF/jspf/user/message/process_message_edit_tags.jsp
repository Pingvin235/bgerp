<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="tagConfig" value="${ctxSetup.getConfig('ru.bgcrm.model.message.TagConfig')}"/>
<c:if test="${not empty tagConfig and not empty tagConfig.tagList}">
	<div class="pl1 w100p">
		<h2>${l.l('Tags')}</h2>
		<input type="hidden" name="updateTags" value="1"/>
		<ui:select-mult list="${tagConfig.tagList}" values="${frd.messageTagIds}" hiddenName="tagId"/>
	</div>
</c:if>