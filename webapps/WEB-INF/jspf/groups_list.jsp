<%@page import="ru.bgcrm.model.process.ProcessGroup"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="ru.bgcrm.model.user.Group"%>
<%@page import="java.util.List"%>
<%@page import="ru.bgcrm.cache.UserCache"%>
<%@page import="ru.bgcrm.model.IdTitle"%>
<%@page import="java.util.ArrayList"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<% 
	IdTitle role = (IdTitle)pageContext.getAttribute( "role" );
	Set<ProcessGroup> groups = (Set<ProcessGroup>)pageContext.getAttribute( "groups" );
							
	List<Map<String, String>> list = new ArrayList<Map<String, String>>( groups.size() );
	for( Group group : UserCache.getUserGroupFullTitledList() )
	{
		String groupAndRole = group.getId() + ":" + role.getId();
		
		Map<String, String> idTitle = new HashMap<String, String>();
		list.add( idTitle );
		
		idTitle.put( "id", groupAndRole );
		idTitle.put( "title", group.getTitle() );								
	}
	
	Set<String> values = new HashSet<String>( groups.size() ); 
	for( ProcessGroup group : groups )
	{
		values.add( group.toGroupRolePair() );
	}
	 
	pageContext.setAttribute( "list", list );
	pageContext.setAttribute( "values", values );
%>