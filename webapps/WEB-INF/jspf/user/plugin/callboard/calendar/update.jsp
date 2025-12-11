<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
<c:set var="calendar" value="${frd.calendar}"/>
<c:set var="dayTypes" value="${frd.dayTypes}"/>
<c:set var="dateTypeMap" value="${frd.dateTypeMap}"/>

<c:set var="year" value="${form.param.year}"/>
<c:if test="${empty year}">
	<jsp:useBean id="date" class="java.util.Date" />
	<c:set var="year" value="${tu.format(date, 'yyyy')}"/>
</c:if>

<c:url var="url" value="/user/plugin/callboard/work.do">
	<c:param name="method" value="workDaysCalendarUpdate" />
	<c:param name="calendarId" value="${calendar.id}" />
</c:url>

<c:url var="getUrl" value="/user/plugin/callboard/work.do">
	<c:param name="method" value="workDaysCalendarGet"/>
	<c:param name="id" value="${calendar.id}"/>
</c:url>

<c:url var="paramCopyUrl" value="/user/plugin/callboard/work.do">
	<c:param name="method" value="workDaysCalendarCopy"/>
	<c:param name="calendarId" value="${calendar.id}"/>
</c:url>

<c:set var="uiid" value="${u:uiid()}"/>
<div id="${uiid}" class="in-ml1">
	<h1 style="display: inline-block;">${calendar.title}</h1>

	<ui:combo-single name="year" value="${year}" style="width: 5em;" onSelect="$$.ajax.loadContent('${getUrl}&year='+ this.value);">
		<jsp:attribute name="valuesHtml">
			<c:forEach var="item" begin="${year-1}" end="${year+1}">
				<li value="${item}">${item}</li>
			</c:forEach>
		</jsp:attribute>
	</ui:combo-single>

	<button  type="button" class="ml1 btn-white icon" onClick="$$.ajax.loadContent('${form.returnUrl}')" title="Закрыть"><i class="ti-close"></i></button>
</div>

<script>
	$(function()
	{
		var $state = $$.shell.$state();
		$state.html( "" );

		$('#${uiid}').appendTo( $state );
	})
</script>

<c:set var="uiid" value="${u:uiid()}"/>

<div style="text-align: center;" id="${uiid}" >
	<div style="display: inline-block; text-align: left;">
		<div class="workDaysTypeSelect mb1">
			<ui:combo-single name="dayType" prefixText="Тип дня:" widthTextValue="120px">
				<jsp:attribute name="valuesHtml">
					<c:forEach var="type" items="${dayTypes}" >
						<li value="${type.id}"><span style="color: ${type.color};">${type.title}</span></li>
					</c:forEach>
				</jsp:attribute>
			</ui:combo-single>
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
		* uiOnly - not used если false, помимо обновления отображения, делает запрос на добавление исключения для заданной даты
		*/
		var setDayTypeForDate = function( date, type, uiOnly )
		{
			if( type <= 0 || getSelectedYear() != date.getFullYear() )
			{
				return ;
			}

			var url = '${url}' + '&date=' + ( $.datepicker.formatDate('dd.mm.yy', date ) );
			url+='&type=' + ( type );

			$$.ajax.post(url).done(() => {
				$( '#${uiid} td#ui-datepicker-calendar-day-'+( date.getMonth()+1 )+'-'+( date.getDate() ) ).children( 'a' ).css( 'color', dayTypeArray[type].color );
			})
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

		$$.callboard.calendar.init('${uiid}', '${year}', onCalendarDateSelect);

		reloadCalendar();
	})
</script>