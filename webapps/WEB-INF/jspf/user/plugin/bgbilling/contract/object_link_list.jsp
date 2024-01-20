<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<table width="100%" cellpadding="0" cellspacing="1" bgcolor="#C0C0C0" class="oddeven">
    <tr class="header">
        <td colspan="5">
            <b>Привязки объектов договора (base):</b>
            <button style="border:none; background:transparent; cursor: pointer; text-decoration:underline;" onclick="$('#${form.param.contractId}objectLinkList').empty();">[закрыть]</button>
        </td>
    </tr>

    <c:if test="${not empty frd.links}">
        <tr class="header">
            <td>Объект</td>
            <td>Адрес</td>
            <td>Привязка</td>
            <td>VLAN (Тип) </td>
            <td>Оборудование</td>
        </tr>
        <c:forEach var="link" items="${frd.links}">
            <tr>
                <td align="center">${link.getObjectId()}</td>
                <td align="left">${link.getAddress()}</td>
                <td align="center"><a href="telnet://${link.getIp()}">${link.getIp()}</a> f/${link.getPort()}</td>
                <td align="center">${link.getVlan()}</td>
                <td align="center">${link.getModel()}</td>
            </tr>
        </c:forEach>
    </c:if>
</table>
