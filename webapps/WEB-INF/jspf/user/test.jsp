<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div style="padding: 0.5em;">
	<div class="in-mt1 mt1">
		<div>
			<b>&lt;ui:tag-box&gt;</b><br/>
			Lazy loading from AJAX request<br>
			<ui:tag-box showOptions="1" value="mail1@domain.com,Ivan2 Pupkin <mail2@domain.com>" url="/user/test.do?action=enumValues" style="width: 30em;"/>
		</div>

		<form class="in-mb05">
			<b>$$.shell.showMessage</b><br/>
			<input name="title" type="text" size="50" placeholder="Title"/><br/>
			<textarea name="text" cols="50" rows="2" placeholder="HTML text"/><br/>
			<button type="button" class="btn-white" onclick="$$.shell.showMessage(this.form.title.value, this.form.text.value)">SHOW</button>
		</form>
	</div>
</div>

<%@ include file="/WEB-INF/jspf/test.jsp"%>

<shell:title ltext="Тест title"/>
<shell:state ltext="Тест state"/>
