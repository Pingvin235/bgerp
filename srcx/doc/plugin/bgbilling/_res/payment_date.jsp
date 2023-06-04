<%@page import="java.util.GregorianCalendar"%>
<%@page import="java.util.Calendar"%>
<%@page import="ru.bgcrm.util.TimeUtils"%>
<%@page import="org.apache.commons.lang3.math.NumberUtils"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="ru.bgcrm.util.Utils"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="org.bgerp.model.base.IdTitle"%>
<%@page import="ru.bgcrm.plugin.bgbilling.proto.model.ContractInfo"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
   Файл поместить в каталог: WEB-INF/custom/plugin/bgbilling

   Включать в WEB-INF/jspf/user/plugin/bgbilling/contract_billing_data.jsp среди
      <u:sc>
         ....
         <%@ include file="contract/tree_item.jsp"%>
      </u:sc>
   следующим образом:

   <%@ include file="/WEB-INF/custom/plugin/bgbilling/payment_date.jsp"%>
--%>
<u:sc>
    <%
        ContractInfo contract = (ContractInfo) request.getAttribute("contract");
    	if (contract != null) {
           float price = 0f;
           // выбор цен из названий тарифов
           if (contract != null && contract.getTariffList() != null)
    	      for (IdTitle tariff : contract.getTariffList())
    	           price += NumberUtils.toFloat(StringUtils.defaultString(StringUtils.substringBetween(tariff.getTitle(), "(", "р.")).trim());

            if (price > 0) {
                float balanceRest = contract.getBalanceOut().floatValue();
                if (balanceRest > 0) {
                	int days = (int) (balanceRest / (price / 30));
                	Calendar curdate = new GregorianCalendar();
                	curdate.add(Calendar.DAY_OF_YEAR, days);
                	pageContext.setAttribute("value", curdate.getTime());
                }
            }
    	}
    %>
	<c:if test="${not empty value}">
		<c:set var="title" value="Оплатить до"/>
		<c:set var="url" value="no"/>
		<c:set var="value" value="${tu.format(value, 'ymd')}"/>
		<%@ include file="/WEB-INF/jspf/user/plugin/bgbilling/contract/tree_item.jsp"%>
	</c:if>
</u:sc>
