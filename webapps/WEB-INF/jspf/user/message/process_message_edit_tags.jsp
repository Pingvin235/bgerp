<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="TAG_PIN_ID"><%=org.bgerp.model.msg.config.TagConfig.Tag.TAG_PIN_ID%></c:set>

<c:set var="tagConfig" value="${ctxSetup.getConfig('org.bgerp.model.msg.config.TagConfig')}"/>
<c:if test="${not empty tagConfig and not empty tagConfig.tagList}">
	<div class="pl1 w100p">
		<h2>${l.l('Tags')}</h2>
		<input type="hidden" name="updateTags" value="1"/>
		<c:if test="${frd.messageTagIds.contains(u:int(TAG_PIN_ID))}">
			<input type="hidden" name="tagId" value="${TAG_PIN_ID}"/>
		</c:if>
		<ui:select-mult list="${tagConfig.tagList}" values="${frd.messageTagIds}" hiddenName="tagId"/>
	</div>
</c:if>