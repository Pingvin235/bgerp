<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="title" value="Распределение адресов"/>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>


<script>
	$(function() 
	{		
		var $distributionTabs = $( "#distributionTabs" ).tabs( {spinner: '' , refreshButton: true} );
				
		<p:check action="ru.bgcrm.struts.action.AddressDistributionAction:quarterDistribution">		
			<c:url var="url" value="distribution.do">
				<c:param name="action" value="quarterDistribution"/>
		  	</c:url>
  	
			$distributionTabs.tabs( "add", "${url}", "Распределение кварталов" );		
		</p:check>		
	});	
	
</script>

<div id="distributionTabs">
	<ul>
		<li><a href="#distributionTabsHouses">Распределение домов</a></li><!-- 
	 --></ul>
	<div id="distributionTabsHouses">
		<%@ include file="/WEB-INF/jspf/user/distribution/address.jsp"%>
	</div>
</div>
