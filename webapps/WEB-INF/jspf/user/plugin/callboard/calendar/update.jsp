<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
<c:set var="calendar" value="${form.response.data.calendar}"/>
<c:set var="dayTypes" value="${form.response.data.dayTypes}"/>
<c:set var="dateTypeMap" value="${form.response.data.dateTypeMap}"/>

<c:set var="year" value="${form.param.year}"/>
<c:if test="${empty year}">
	<jsp:useBean id="date" class="java.util.Date" />
	<c:set var="year" value="${tu.format(date, 'yyyy')}"/>
</c:if>

<c:url var="url" value="/user/plugin/callboard/work.do">
	<c:param name="action" value="workDaysCalendarUpdate" />
	<c:param name="calendarId" value="${calendar.id}" />
</c:url>

<c:url var="getUrl" value="/user/plugin/callboard/work.do">
	<c:param name="action" value="workDaysCalendarGet"/>
	<c:param name="id" value="${calendar.id}"/>
</c:url>

<c:url var="paramCopyUrl" value="/user/plugin/callboard/work.do">
	<c:param name="action" value="workDaysCalendarCopy"/>
	<c:param name="calendarId" value="${calendar.id}"/>
</c:url>

<c:set var="uiid" value="${u:uiid()}"/>
<div id="${uiid}" class="in-ml1">
	<h1 style="display: inline-block;">${calendar.title}</h1>

	<u:sc>
		<c:set var="valuesHtml">
			<c:forEach var="item" begin="${year-1}" end="${year+1}">
				<li value="${item}">${item}</li>
			</c:forEach>
		</c:set>
		<c:set var="hiddenName" value="year"/>
		<c:set var="value" value="${year}"/>
		<c:set var="style" value="width: 5em;"/>
		<c:set var="onSelect" value="$$.ajax.loadContent('${getUrl}&year='+ $hidden.val());"/>
		<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
	</u:sc>
	<button  type="button" class="ml1 btn-white" onClick="$$.ajax.loadContent('${form.returnUrl}')" title="Закрыть">&lt;</button>
</div>

<script>
	$(function()
	{
		var $state = $('#title > .status:visible > .wrap > .center');
		$state.html( "" );

		$('#${uiid}').appendTo( $state );
	})
</script>

<c:set var="uiid" value="${u:uiid()}"/>

<div style="text-align: center;" id="${uiid}" >
	<div style="display: inline-block; text-align: left;">
		<div class="workDaysTypeSelect mb1">
			<u:sc>
				<c:set var="valuesHtml">
					<c:forEach var="type" items="${dayTypes}" >
						<li value="${type.id}"><span style="color: ${type.color};">${type.title}</span></li>
					</c:forEach>
				</c:set>
				<c:set var="hiddenName" value="dayType"/>
				<c:set var="prefixText" value="Тип дня:"/>
				<c:set var="widthTextValue" value="120px"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
			</u:sc>
		</div>

		<div style="display: inline-block;" class="datepicker"></div>
	</div>
</div>

<script>
	$(function()
	{
		//глобальный массив объектов - типов рабочих дней ( рабочий день, сокращенный день, выходной и т.д. )
		var dayTypeArray = new Array();

		<c:forEach var="item" items="${dayTypes}" >
			var type = new Object();

			type.id = '${item.id}';
			type.title = '${item.title}';
			type.color = '${item.color}';

			dayTypeArray[type.id] = type;
		</c:forEach>

		var getSelectedYear = function()
		{
			return ${year};
		};

		var makeDate = function( dateString )
		{
			var dateParts = dateString.split(".");
			return new Date(dateParts[2], (dateParts[1] - 1), dateParts[0]);
		};

		//возвращает тип выбранного дня
		var getSelectedDayType = function()
		{
			return $( '#${uiid} .workDaysTypeSelect input[name=dayType]' ).val();
		}

		/*
		* Устанавливает значение для заданной даты в календаре
		* uiOnly - если false, помимо обновления отображения, делает запрос на добавление исключения для заданной даты
		*/
		var setDayTypeForDate = function( date, type, uiOnly )
		{
			if( type <= 0 || getSelectedYear() != date.getFullYear() )
			{
				return ;
			}

			var url = '${url}' + '&date=' + ( $.datepicker.formatDate('dd.mm.yy', date ) );
			url+='&type=' + ( type );

			if( uiOnly || sendAJAXCommand( url ) )
			{
				$( '#${uiid} td#ui-datepicker-calendar-day-'+( date.getMonth()+1 )+'-'+( date.getDate() ) ).children( 'a' ).css( 'color', dayTypeArray[type].color );
			}
		};

		var reloadCalendar = function()
		{
			<c:forEach var="item" items="${dateTypeMap}"><%--
			--%><c:set var="date" value="${item.key}"/><%--
			--%><c:set var="dayType" value="${item.value.first}"/><%--
			--%><c:if test="${not empty dayType}"><%--
				--%>setDayTypeForDate( makeDate( '${tu.format( date, 'dd.MM.yyyy' )}' ), ${dayType.id}, true );<%--
			--%></c:if><%--
		--%></c:forEach>
		};

		//обработчик кликов по дням в календаре
		var onCalendarDateSelect = function( date )
		{
			var dateParts = date.split(".");
			var selectedDate = new Date(dateParts[2], (dateParts[1] - 1), dateParts[0]);

			setDayTypeForDate( selectedDate, getSelectedDayType(), false );
		}

		//сам календарь
		$( '#${uiid} div.datepicker' ).datepicker(
		{
			changeMonth: false,
			changeYear: false,
			stepMonths: 0,
			minDate: new Date( ${year}, 0, 1 ),
			maxDate: new Date( ${year}, 11, 31 ),
			/* defaultDate: new Date(), */
			numberOfMonths: [ 3, 4 ],

			onSelect: function(date, inst)
			{
			    inst.inline = false;
			    onCalendarDateSelect( date );
			}
		});

		reloadCalendar();
	})
</script>