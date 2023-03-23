<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:set var="range" value="${form.response.data.range}"/>

<c:set var="net" value="${form.param.mode eq 'net'}"/>

<html:form action="/user/plugin/bgbilling/proto/ipn" styleId="${uiid}" style="height: 100%;" styleClass="in-va-top">
	<input type="hidden" name="action" value="rangeUpdate"/>
	<html:hidden property="id"/>
	<html:hidden property="moduleId"/>
	<html:hidden property="contractId"/>
	<html:hidden property="billingId"/>

	<div class="layout-height-rest">
		<div style="width: 50%; display: inline-block; height: 100%;" class="pb1">
			<h2>
				<c:choose>
					<c:when test="${net}">Сеть и маска</c:when>
					<c:otherwise>Диапазон адресов</c:otherwise>
				</c:choose>
			</h2>
			<div class="in-table-cell">
				<div style="width: 50%">
					<input type="text" name="addressFrom" value="${range.addressFrom}" style="width: 100%;"/>
				</div>
				<c:choose>
					<c:when test="${net}">
						<div class="pl05 pr05"> / </div>
						<div style="width: 50%">
							<input type="text" name="mask" value="${range.mask}" size="2"/>
						</div>
					</c:when>
					<c:otherwise>
						<div class="pl05 pr05"> - </div>
						<div style="width: 50%">
							<input type="text" name="addressTo" value="${range.addressTo}" style="width: 100%;"/>
						</div>
					</c:otherwise>
				</c:choose>
				<div class="pl05">
					<button type="button" class="btn-white" title="Взять из пула ресурсов">&lt;&lt;&lt;</button>
				</div>
			</div>
			<h2>Период</h2>
			<div>
				<input type="text" name="dateFrom" value="${tu.format( range.dateFrom, 'dd.MM.yyyy' ) }"/>
				<u:sc>
					<c:set var="selector" value="#${uiid} input[name=dateFrom]"/>
					<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
				</u:sc>
				-
				<input type="text" name="dateTo" value="${tu.format( range.dateTo, 'dd.MM.yyyy' ) }"/>
				<u:sc>
					<c:set var="selector" value="#${uiid} input[name=dateTo]"/>
					<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
				</u:sc>
			</div>
			<h2>План привязок</h2>
			<u:sc>
				<c:set var="valuesHtml">
					<li value="0">План по умолчанию</li>
					<c:forEach var="item" items="${form.response.data.planList}">
						<li value="${item.id}">${item.title}</li>
					</c:forEach>
				</c:set>
				<c:set var="hiddenName" value="plan"/>
				<c:set var="value" value="${range.plan}"/>
				<c:set var="style" value="width: 100%;"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
			</u:sc>

			<h2>Комментарий</h2>
			<textarea name="comment" style="width: 100%; resize: none;" class="layout-height-rest">${range.comment}</textarea>
		</div><%--
	--%><div style="width: 50%; display: inline-block; height: 100%;" class="pl1 pb1">
			<h2>Источники / интерфейсы</h2>

			<div class="mb1">
				На дату:

				<jsp:useBean id="curdate" class="java.util.Date"/>
				<input name="sourceDate" value="${tu.format( curdate, 'dd.MM.yyyy' )}"/>
				<u:sc>
					<c:set var="selector" value="#${uiid} input[name=sourceDate]"/>
					<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
				</u:sc>
			</div>

			<c:set var="treeId" value="${u:uiid()}"/>
			<div id="${treeId}" class="layout-height-rest" style="overflow: auto;">
				<c:forEach var="source" items="${form.response.data.sourceList}">
					<ul>
						<li class="select_node">
							<label class="ml05"><input name="iface" type="checkbox" value="0"/>${source.title}</label>

							<ul>
								<c:forEach var="iface" items="${source.ifaceList}">
									<c:set var="ifaceId" value="${source.id}_${iface.id}"/>

									<c:remove var="selected"/>
									<c:if test="${range.ifaceList.contains(ifaceId)}">
										<c:set var="selected" value=" checked='true' "/>
									</c:if>

									<li class="select_node">
										<label class="ml05"><input name="iface" type="checkbox" value="${ifaceId}" ${selected}/>${iface.title}</label>
									</li>
								</c:forEach>
							</ul>
						</li>
					</ul>
				</c:forEach>
			</div>

			<script>
				$( function()
				{
					$("#${treeId}").Tree();
				} );
			</script>
		</div>
	</div>

	<div>
		<button type="button" class="btn-grey" onclick="if( sendAJAXCommand( formUrl( this.form ) ) ){ openUrlToParent( '${form.returnUrl}', $('#${uiid}') ) }">OK</button>
		<button type="button" class="btn-grey ml1" onclick="openUrlToParent( '${form.returnUrl}', $('#${uiid}') )">${l.l('Отмена')}</button>
	</div>
</html:form>

<%@ include file="/WEB-INF/jspf/layout_process.jsp"%>