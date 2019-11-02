<%@page import="ru.bgcrm.util.inet.IPUtils"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="ru.bgcrm.util.Utils"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.Set"%>
<%@page import="ru.bgcrm.plugin.bgbilling.proto.model.ipn.IpnRange"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:set var="rulePair" value="${form.response.data.rulePair}"/>
<c:set var="ruleTypeId" value="${rulePair.first}"/>
<c:set var="rule" value="${rulePair.second}"/>

<c:url var="baseGenerateUrl" value="plugin/bgbilling/proto/ipn.do">
	<c:param name="action" value="gateRuleGenerate"/>
	<c:param name="moduleId" value="${form.param.moduleId}"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="gateTypeId" value="${form.param.gateTypeId}"/>			
</c:url>

<c:set var="generateRuleCode">
	var addressList = ''; 
	$('#${uiid} #range input[name=address]:checked').each( function()
	{
		addressList += addressList.length == 0 ? this.value : ',' + this.value;	
	});
	
	var netList = '';
	$('#${uiid} #net input[name=net]:checked').each( function()
	{
		netList += net.length == 0 ? this.value : ',' + this.value;	
	});
	
	addressList = addressList + ';' + netList;
	
	var $rule = $('#${uiid} input[name=rule]');
	var $textarea = $('#${uiid} textarea#rules');
	
	var ruleTypeId = $('#${uiid} input[name=ruleTypeId]')[0].value;
	if( ruleTypeId == 0 )
	{
		$textarea.removeAttr('disabled');
		$rule.val($textarea.text()); 
	}
	else
	{	
		$textarea.attr('disabled', 'true');
		
		var url = '${baseGenerateUrl}&ruleTypeId=' + ruleTypeId + '&addressList=' + addressList;
		
		var result = sendAJAXCommand( url );
		$textarea.text( result.data.rule );
		
		$rule.val(addressList);
	}
</c:set>	

<html:form action="/user/plugin/bgbilling/proto/ipn" style="height: 100%;" styleId="${uiid}">
	<input type="hidden" name="action" value="gateRuleUpdate"/>
	<html:hidden property="moduleId"/>
	<html:hidden property="contractId"/>
	<html:hidden property="billingId"/>
	<html:hidden property="id"/>
	<html:hidden property="gateId"/>
	<html:hidden property="gateTypeId"/>
	<input type="hidden" name="rule"/> 
	
	<div style="display: table; width: 100%;" id="${uiid}" class="in-table-cell in-va-top layout-height-rest">
		<div style="width: 100%;">
			<h2>Тип правила</h2>
			<u:sc>
				<c:set var="valuesHtml">
					<li value="0">Пользовательское</li>
					<c:forEach var="item" items="${form.response.data.ruleTypeList}">
						<li value="${item.id}">${item.title}</li>
					</c:forEach>
				</c:set>					
				<c:set var="hiddenName" value="ruleTypeId"/>
				<c:set var="value" value="${rulePair.first}"/>
				<c:set var="style" value="width: 100%;"/>
				<c:set var="onSelect" value="${generateRuleCode}"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
			</u:sc>
			
			<h2>Команды</h2>
			
			<textarea id="rules" style="width: 100%; resize: none;" class="layout-height-rest" onkeypress="this.form.rule.value = this.value;">${rulePair.second}</textarea>			
		</div>
		<c:if test="${ruleTypeId gt 0}"> 
			<%
				String rule = (String)pageContext.getAttribute( "rule" );
				
				String[] addresses_nets = rule.split( ";" );
				String addresses = addresses_nets[0];
				String nets = "";
				if( addresses_nets.length > 1 )
				{
					nets = addresses_nets[1];
				}
				
				pageContext.setAttribute( "addressSet", Utils.toSet( addresses ) );
				pageContext.setAttribute( "netSet", Utils.toSet( nets ) );
			%>
		</c:if>	
		<div style="min-width: 300px;" class="pl1">
			<div class="mb1">
				<u:sc>
					<c:set var="uiid" value="${u:uiid()}"/>
					На дату: <input type="text" name="date" id="${uiid}"/>
					<c:set var="selector" value="#${uiid}"/>		
					<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
				</u:sc>
			</div>
			
			<div id="treeDiv" class="layout-height-rest">
				<ul>
					<li><a href="#range">Диапазоны</a></li><%--
				--%><li><a href="#net">Сети</a></li>
				</ul>
				<div id="range" class="layout-height-rest">
					<div id="tree" class="layout-height-rest" style="overflow: auto;">
						<c:forEach var="range" items="${form.response.data.rangeList}">
							<ul>
								<li class="select_node">
									<label><input name="range" type="checkbox" value="0"/>${range.addressRange} [${range.comment}]</label>
									
									<ul>
										<%
											IpnRange range = (IpnRange)pageContext.getAttribute( "range" );
										
											long addrFrom = IPUtils.convertStringIPtoLong( range.getAddressFrom() );
											long addrTo = IPUtils.convertStringIPtoLong( range.getAddressTo() );
	
											List<String> rangeList = new ArrayList<String>( 200 );
											
											for( long addr = addrFrom; addr <= addrTo; addr++ )
											{
												rangeList.add( String.valueOf( addr ) );
												if( rangeList.size() > 200 )
												{
													break;
												}
											}
											
											pageContext.setAttribute( "addressList", rangeList );
										%>
										
										<c:forEach var="address" items="${addressList}">
											<li class="select_node">
												<label><input name="address" type="checkbox" value="${address}" ${u:checkedFromBool( addressSet.contains( address ) )}/><%= IPUtils.convertLongIpToString( Utils.parseLong( (String)pageContext.getAttribute( "address" ) ) ) %></label>
											</li>	
										</c:forEach>
									</ul>
								</li>	
							</ul>								
						</c:forEach>
					</div>
				</div>
				<div id="net" class="layout-height-rest">
					<div id="tree" class="layout-height-rest" style="overflow: auto;">
						<c:forEach var="net" items="${form.response.data.netList}">
							<ul>
								<%
									IpnRange range = (IpnRange)pageContext.getAttribute( "net" );
									pageContext.setAttribute( "address", IPUtils.convertStringIPtoLong( range.getAddressFrom() ) + "/" + range.getMask() );											
								%>
								
								<li class="select_node">
									<label><input name="net" type="checkbox" value="${address}" ${u:checkedFromBool( netSet.contains( address ) )}/>${net.addressRange} [${net.comment}]</label>									
								</li>	
							</ul>								
						</c:forEach>
					</div>	
				</div>
			</div>			
		</div>
	</div>
		
	<%@ include file="/WEB-INF/jspf/ok_cancel_in_form.jsp"%>
</html:form> 	

<script>
	$(function()
	{
		$('#${uiid} #treeDiv').tabs();
		$("#${uiid} #range #tree").Tree();
		$("#${uiid} #net #tree").Tree();
		
		var generateRule = function()
		{
			${generateRuleCode}
		};
		
		$("#${uiid} #range #tree").click( generateRule );
		$("#${uiid} #net #tree").click( generateRule );
		
		generateRule();
	})
</script>

<%@ include file="/WEB-INF/jspf/layout_process.jsp"%>