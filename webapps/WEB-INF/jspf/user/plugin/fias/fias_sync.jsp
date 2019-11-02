<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h2>ФИАС</h2>
<script>
     $(function() {
         var $fiasTabs = $( "#fiasTabs-Tabs" ).tabs( {spinner: '', refreshButton:true} );

          <c:url var="url" value="plugin/fias.do">
         	 <c:param name="action" value="streetList"/>
		  </c:url>
	      $fiasTabs.tabs( "add", "${url}", "Улицы" );	      

	      <c:url var="url" value="plugin/fias.do">
      	 <c:param name="action" value="houseList"/>
		  </c:url>
		  $fiasTabs.tabs( "add", "${url}", "Дома/Индексы" );

	      <c:url var="url" value="plugin/fias.do">
	      	<c:param name="action" value="updateBase"/>
		  </c:url>
	      $fiasTabs.tabs( "add", "${url}", "Обновление" );	      
     });
</script>

<div id="fiasTabs-Tabs">
	<ul></ul>
</div>