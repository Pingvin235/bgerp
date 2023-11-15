<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="news" value="${form.response.data.news}"/>

<form id="newsCreateForm" class="center1020" action="/user/news.do" method="post" name="dynForm" onsubmit="return false;">
	<input type="hidden" name="action" value="newsUpdate" />
	<input type="hidden" name="id" value="${news.id}" />

	<c:if test="${form.param.requestUserId > 0}">
		<input type="hidden" name="requestUserId" value="${form.param.requestUserId}" />
	</c:if>

	<h2>${l.l('Заголовок')}</h2>
	<input type="text" name="title" style="width: 100%;" value="${news.title}"/>
	<h2>${l.l('Текст')}&nbsp;${isMsg == 1 ? l.l('of message') : l.l('of news')}</h2>
	<textarea style="width: 100%;" rows="10" name="description"></textarea>
	<span class="hint">${l.l('В тексте новости допустима HTML разметка.')}</span>

	<div>
		<div style="width: 50%; display: inline-block;">
			<h2>${l.l('Время на прочтение')}</h2>
			<input type="text" name="readTime" value="${not empty news ? news.readTime : 240}" style="text-align: center; width: 102px;"/>
			<span class="hint">${l.l('Через какое количество часов новость будет помечена прочитана для адресатов независимо от фактического прочтения.')}</span>

			<h2>${l.l('Время жизни новости')}</h2>
			<input type="text" name="lifeTime" value="${not empty news ? news.lifeTime : 300}" style="text-align: center; width: 102px;"/>
			<span class="hint">${l.l('Через какое количество суток новость будет удалена для всех адресатов.')}</span>

			<h2>${l.l('Тип новости')}</h2>
			<u:sc>
				<c:set var="style" value="width: 150px;"/>
				<c:set var="valuesHtml">
					<li value="-1">${l.l('----')}</li>
					<li value="0">${l.l('Обычная')}</li>
					<li value="1">${l.l('Всплывающая')}</li>
				</c:set>
				<c:set var="hiddenName" value="type"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
			</u:sc>
		 </div><%--
	 --%><div style="width: 50%; display: inline-block; vertical-align: top;">
	 		<c:if test="${not(form.param.requestUserId > 0 )}">
				<h2>${l.l('Группы')}</h2>

				<ui:select-mult list="${ctxUserGroupList}" hiddenName="group" values="${news.groupIds}" style="width: 100%;"/>

				<span class="hint">${l.l('Если группы не указаны - новость получают все пользователи.')}</span>
			</c:if>
		</div>
	</div>

	<ui:form-ok-cancel styleClass="mt1"/>
</form>

<shell:state text="${l.l('Создание новости')}"/>