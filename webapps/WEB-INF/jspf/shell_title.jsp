<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%
	String title = (String)pageContext.getAttribute( "title" );
	if( title != null )
	{
		title = title.replaceAll( "\r", "" ).replaceAll( "\n", " " );
		pageContext.setAttribute( "title", title );
	}
%>

<script>
	$(function()
	{
		$('#title > .status:visible h1.title').html( "${title}" );
	})
</script>