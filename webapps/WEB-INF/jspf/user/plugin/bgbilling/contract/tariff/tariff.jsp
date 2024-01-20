<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<table width="100%">
	<tr>
		<td align="right" nowrap="nowrap">Установленные тарифы:</td>
		<td width="100%"><b>${frd.tariffs}</b></td>
	</tr>
	<tr>
		<td align="right">опции:</td>
		<td width="100%"><b>${frd.options}</b></td>
	</tr>
	<tr>
		<td align="right">группы:</td>
		<td width="100%"><b>${frd.groups}</b></td>
	</tr>
</table>

<script>
     $(function() {
         var $tariffTabs = $( "#${uiid}" ).tabs( {spinner: '' , refreshButton: true} );

         <c:url var="url" value="/user/plugin/bgbilling/proto/contractTariff.do">
		  	<c:param name="action" value="contractTariffList"/>
		  	<c:param name="billingId" value="${form.param.billingId}"/>
		  	<c:param name="contractId" value="${form.param.contractId}"/>
		  </c:url>
	      $tariffTabs.tabs( "add", "${url}", "Глобальные тарифы" );

	      <c:url var="url" value="/user/plugin/bgbilling/proto/contractTariff.do">
		  	<c:param name="action" value="personalTariffList"/>
		  	<c:param name="billingId" value="${form.param.billingId}"/>
		  	<c:param name="contractId" value="${form.param.contractId}"/>
		  </c:url>
	      $tariffTabs.tabs( "add", "${url}", "Персональные тарифы" );

	      <c:url var="url" value="/user/plugin/bgbilling/proto/contractTariff.do">
		  	<c:param name="action" value="tariffOptionList"/>
		  	<c:param name="billingId" value="${form.param.billingId}"/>
		  	<c:param name="contractId" value="${form.param.contractId}"/>
		  </c:url>
		  $tariffTabs.tabs( "add", "${url}", "Тарифные опции" );

		  <c:url var="url" value="/user/plugin/bgbilling/proto/contractTariff.do">
		  	<c:param name="action" value="groupTariffList"/>
		  	<c:param name="billingId" value="${form.param.billingId}"/>
		  	<c:param name="contractId" value="${form.param.contractId}"/>
		  </c:url>
		  $tariffTabs.tabs( "add", "${url}", "Группы тарифов" );
     });
</script>

<div id="${uiid}" class="mt1">
	<ul></ul>
</div>