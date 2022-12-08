<%@page import="ru.bgcrm.util.TimeUtils"%>
<%@page import="java.util.Date"%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="formUiid" value="${u:uiid()}"/>

<html:form action="/user/plugin/bgbilling/proto/bill" styleId="${formUiid}">
	<html:hidden property="action"/>
	<html:hidden property="billingId"/>
	<html:hidden property="moduleId"/>
	<html:hidden property="contractId"/>
	<html:hidden property="mode"/>

	<c:choose>
		<c:when test="${form.param.mode eq 'bill'}">
			<c:set var="btnStyleBill" value="btn-blue"/>
			<c:set var="btnStyleInvoice" value="btn-white"/>
		</c:when>
		<c:otherwise>
			<c:set var="btnStyleBill" value="btn-white"/>
			<c:set var="btnStyleInvoice" value="btn-blue"/>
		</c:otherwise>
	</c:choose>

	<c:set var="sendForm">$$.ajax.load($('#${formUiid}'), $('#${formUiid}').parent());</c:set>

	<button type="button" class="${btnStyleBill}" onclick="this.form.mode.value='bill'; ${sendForm}">Счета</button>
	<button type="button" class="ml1 ${btnStyleInvoice}" onclick="this.form.mode.value='invoice'; ${sendForm}">Счета-фактуры, акты</button>

	<ui:page-control nextCommand=";${sendForm}" />
</html:form>

<c:url var="baseUrl" value="/user/plugin/bgbilling/proto/bill.do">
	<c:param name="action" value="getDocument"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="moduleId" value="${form.param.moduleId}"/>
	<c:param name="type" value="${form.param.mode}"/>
</c:url>

<c:url var="setPayedUrl" value="/user/plugin/bgbilling/proto/bill.do">
	<c:param name="action" value="setPayed"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="moduleId" value="${form.param.moduleId}"/>
</c:url>

<table class="mt1 data" style="width: 100%;">
	<c:choose>
		<c:when test="${form.param.mode eq 'bill'}">
			<tr>
				<td>
					<%
						pageContext.setAttribute( "currentDay", new Date() );
						pageContext.setAttribute( "prevDay", TimeUtils.getPrevDay( new Date() ) );
					%>

					&nbsp;
					<div style="max-height: 0px; max-width: 0px;">
						<c:set var="uiid" value="${u:uiid()}"/>
						<ul id="${uiid}" style="display: none;">
							<li><a href="#" date="${tu.format( currentDay, 'dd.MM.yyyy' )}">Оплачено сегодня (${tu.format( currentDay, 'dd.MM E')})</a></li>
							<li><a href="#" date="${tu.format( prevDay, 'dd.MM.yyyy' )}">Оплачено вчера (${tu.format( prevDay, 'dd.MM E')})</a></li>
							<li><a href="#" date="select">Оплачено на дату</a></li>
							<li><a href="#">Не оплачено</a></li>
						</ul>
					</div>

					<script>
						$(function()
						{
							var $menu = $('#${uiid}').menu().hide();

							var $datepickerHidden = null;
							var bill = null;

							var setPayed = function( date )
							{
								var $dlg = $( "<div class=\"in-mt05\">\
									<input name=\"summa\" type=\"text\" placeholder=\"Сумма\" style=\"width: 100%;\" value=\"" + bill.summa + "\"/>\
									<input name=\"comment\" type=\"text\" placeholder=\"Пароль\" style=\"width: 100%;\" value=\"Оплата по счёту " + bill.number + " от " + bill.date +  "\"/>\
									<button type=\"button\" class=\"btn-grey\" id=\"ok\">OK</button>\
									<button type=\"button\" class=\"btn-grey ml1\" id=\"cancel\">Отмена</button>\
								</div>" );

								if( date )
								{
									var $dialog = $dlg.dialog({
										modal: true,
										height: "auto",
										draggable: false,
										resizable: false,
									    title: "Пометка оплаты",
									    position: { my: "center top", at: "center top+" + Math.round( $('#${uiid}').parent().offset().top ), of: window, collision: "none" },
									    close: function()
									    {
									    	$dlg.remove();
									    }
									});

									$dlg.find( "button#ok" ).click( function()
									{
										var summa = $dlg.find( "input[name=summa]" ).text();
										var comment = $dlg.find( "input[name=comment]" ).text();
										var ids = bill.id;

										var url = '${setPayedUrl}&ids=' + ids + '&summa=' + summa + '&comment' + comment +'&date=' + date;
										if( sendAJAXCommand( url ) )
										{
											$dlg.dialog( "close" );
											${sendForm}
										}
									});

									$dlg.find( "button#cancel" ).click( function()
									{
										$dlg.dialog( "close" );
									});
								}
								else
								{
									var url = '${setPayedUrl}&ids=' + bill.id ;
									if( sendAJAXCommand( url ) )
									{
										${sendForm}
									}
								}
							};

							$('#${uiid}').closest('table').find('button.menu').click( function()
							{
								$menu.show().position({
									my: "left top",
									at: "left bottom",
									of: this
								});

								var $row = $(this).closest( "tr" );
								bill = {
									id: $row.find('td:eq(1)').text(),
									number: $row.find('td:eq(3)').text(),
									date: $row.find('td:eq(4)').text(),
									summa: $row.find('td:eq(10)').text(),
								};

								$datepickerHidden = $(this).next("input");

								$(document).one( "click", function()
								{
									$menu.hide();
								});

								return false;
							});

							$('#${uiid}').find('a').click( function()
							{
								var date = $(this).attr('date');

								if( date )
								{
									if( date == 'select' )
									{
										$datepickerHidden.datepicker
										({
											buttonImage: 'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7',
									        buttonImageOnly: true,
											showOn: "both",
											onSelect: function()
											{
												setPayed( $datepickerHidden.val() );
											},
											afterShow: function( input, inst )
											{
												$(inst.dpDiv).css( 'left', (($(window).width() - $(inst.dpDiv).outerWidth()) / 2) + $(window).scrollLeft() + "px" );
												$(inst.dpDiv).css( 'top', $('#${uiid}').parent().offset().top );
											}
										}).datepicker( 'show' );
									}
									else
									{
										setPayed( date );
									}
								}
								else
								{
									setPayed();
								}

								$menu.hide();
								return false;
							});
						})
					</script>
				</td>
				<td>ID</td>
				<td>Год.Месяц</td>
				<td>Номер</td>
				<td nowrap="nowrap">Дата создания</td>
				<td>Статус</td>
				<td width="100%">Тип</td>
				<td>Создал</td>
				<td nowrap="nowrap">Дата оплаты</td>
				<td nowrap="nowrap">Отметил оплату</td>
				<td>Сумма</td>
			</tr>
			<c:forEach var="item" items="${form.response.data.list}">
				<tr>
					<td>
						<button type="button" class="btn-white btn-small menu" title="Меню" billId="${item.id}">*</button>
						<input type="hidden" name="date"/>
					</td>
					<c:url var="url" value="${baseUrl}">
						<c:param name="ids" value="${item.id}"/>
					</c:url>
					<td><a href="${url}">${item.id}</a></td>
					<td>${item.month}</td>
					<td>${item.number}</td>
					<td>${tu.format( item.createDate, 'ymd' )}</td>
					<td nowrap="nowrap">${item.statusTitle}</td>
					<td>${item.typeTitle}</td>
					<td nowrap="nowrap">${item.createUser}</td>
					<td>${tu.format( item.payDate, 'ymd' )}</td>
					<td nowrap="nowrap">${item.payUser}</td>
					<td>${item.summa}</td>
				</tr>
			</c:forEach>
		</c:when>
		<c:otherwise>
			<tr>
				<td>ID</td>
				<td>Год.Месяц</td>
				<td>Номер</td>
				<td nowrap="nowrap">Дата создания</td>
				<td width="100%">Тип</td>
				<td nowrap="nowrap">Показать в ЛК</td>
				<td>Сумма</td>
			</tr>
			<c:forEach var="item" items="${form.response.data.list}">
				<tr>
					<c:url var="url" value="${baseUrl}">
						<c:param name="ids" value="${item.id}"/>
					</c:url>
					<td><a href="${url}">${item.id}</a></td>
					<td>${item.month}</td>
					<td>${item.number }</td>
					<td>${tu.format( item.createDate, 'ymd' )}</td>
					<td>${item.typeTitle}</td>
					<td>${item.showOnWeb ? 'Да' : 'Нет'}</td>
					<td>${item.summa}</td>
				</tr>
			</c:forEach>
		</c:otherwise>
	</c:choose>
</table>