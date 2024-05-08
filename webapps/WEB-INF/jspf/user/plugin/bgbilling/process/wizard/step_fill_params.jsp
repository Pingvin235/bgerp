<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

${form.setRequestUrl(reopenProcessUrl)}
${form.response.setData("contractParameterList", stepData.values)}
${form.setParam("showEmptyParameters", 1)}
${form.setParam("billingId", stepData.contract.billingId)}
${form.setParam("contractId", stepData.contract.id) }
<c:set var="onlyData" value="1"/>

<%@ include file="/WEB-INF/jspf/user/plugin/bgbilling/contract/parameter_list.jsp"%>