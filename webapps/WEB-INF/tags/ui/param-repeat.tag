<%@ tag pageEncoding="UTF-8" description="Iface filter"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="form" type="ru.bgcrm.struts.form.DynActionForm" description="form object"%>
<%@ attribute name="name" description="parameter name"%>

<input type="hidden" name="${name}" value="${form.param[name]}"/>
