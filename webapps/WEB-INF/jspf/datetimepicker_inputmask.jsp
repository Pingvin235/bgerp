<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

$(function()
{
	$$.debug( 'datepicker', 'Init inputmasks' );

	<%-- TODO: Вынести все функции в JS файл!! --%>
	var setFocusAndRangeForDate = function(Selector, from, end, event, preventDefault )
	{
		if( preventDefault )
		{
			event.preventDefault();
		}

		setTimeout( function()
		{
			Selector.setSelectionRange( from, end );
		}, 50 );
	}

	<c:set var="dateFormat" value="${tu.getTypeFormat( type )}"/>
	<c:set var="dateFormat" value="${dateFormat.replace('yyyy', 'y')}"/>
	<c:set var="dateFormat" value="${dateFormat.replace('yy', 'y')}"/>
	<c:set var="dateFormat" value="${dateFormat.replace('MM', 'm')}"/>
	<c:set var="dateFormat" value="${dateFormat.replace('dd', 'd')}"/>
	<c:set var="dateFormat" value="${dateFormat.replace('HH', 'h')}"/>
	<c:set var="dateFormat" value="${dateFormat.replace('mm', 's')}"/>
	<c:set var="dateFormat" value="${dateFormat.replace('ss', 's')}"/>

	<c:if test="${type.startsWith('ymd')}">
		$("${selector}").inputmask("${dateFormat}", { "placeholder": "_" });
	</c:if>

	<%--
	<c:if test="${type eq 'ip'}">
	   	$("${selector}").inputmask("999.999.999.999");
	</c:if>
	--%>

	$("${selector}").keydown( function( e )
	{
		var type = '${type}';
		var start = this.selectionStart;

		<%-- Обработка таба и шифта--%>
		if( !e.shiftKey && e.keyCode === 9 )
		{
			switch ( start )
			{
				case 0:
				case 1:
				case 2:
				{
					setFocusAndRangeForDate( $("${selector}")[0], 3, 5, e, true );
					break;
				}
				case 3:
				case 4:
				case 5:
			{
					setFocusAndRangeForDate( $("${selector}")[0], 6, 10, e, true );
					break;
			}
				case 6:
				case 7:
				case 8:
				case 9:
				case 10:
				{
					if ( type == 'ymd' )
					{
						setFocusAndRangeForDate( $("${selector}")[0], 0, 2, e, false );
						break;
					}
					else
					{
						setFocusAndRangeForDate( $("${selector}")[0], 11, 13, e, true );
						break;
					}
				}
				case 11:
				case 12:
				case 13:
				{
					if ( type == 'ymdh' )
					{
						setFocusAndRangeForDate( $("${selector}")[0], 0, 2, e, false );
						break;
					}
					else
					{
						setFocusAndRangeForDate( $("${selector}")[0], 14, 16, e, true );
						break;
					}
				}
				case 14:
				case 15:
				{
					if ( type == 'ymdhm' )
					{
						setFocusAndRangeForDate( $("${selector}")[0], 0, 2, e, false );
						break;
					}
					else
					{
						setFocusAndRangeForDate( $("${selector}")[0], 17, 19, e, true );
						break;
					}
				}
			}
		}

		<%-- Стрелка вверх, идет движение взад --%>
		if( e.keyCode === 38 )
		{
			var start = this.selectionStart;
			switch ( start )
			{
				case 0:
				case 1:
				case 2:
				{
					if ( type == 'ymd' )
					{
						setFocusAndRangeForDate( $("${selector}")[0], 6, 10, e, true );
						break;
					}
					if ( type == 'ymdh' )
					{
						setFocusAndRangeForDate( $("${selector}")[0], 11, 13, e, true );
						break;
					}
					if ( type == 'ymdhm' )
					{
						setFocusAndRangeForDate( $("${selector}")[0], 14, 16, e, true );
						break;
					}
					if ( type == 'ymdhms' )
					{
						setFocusAndRangeForDate( $("${selector}")[0], 17, 19, e, true );
						break;
					}
				}
				case 3:
				case 4:
				case 5:
				{
					setFocusAndRangeForDate( $("${selector}")[0], 0, 2, e, true );
					break;
				}
				case 6:
				case 7:
				case 8:
				case 9:
				case 10:
				{
					setFocusAndRangeForDate( $("${selector}")[0], 3, 5, e, true );
					break;
				}
				case 11:
				case 12:
				case 13:
				{
					setFocusAndRangeForDate( $("${selector}")[0], 6, 10, e, true );
					break;
				}
				case 14:
				case 15:
				case 16:
				{
					setFocusAndRangeForDate( $("${selector}")[0], 11, 13, e, true );
					break;
				}
				case 17:
				case 18:
				case 19:
				{
					setFocusAndRangeForDate( $("${selector}")[0], 14, 16, e, true );
				}
			}
		}

		<%-- Обработка ввода цифр (в том числе и NumPad) --%>
		if( ( 48 <= e.keyCode && e.keyCode <= 57 ) || ( 96 <= e.keyCode && e.keyCode <= 105 ) )
		{
			var start = this.selectionStart;
			switch( start )
			{
				case 1:
				{
					setFocusAndRangeForDate( $("${selector}")[0], 3, 5, e, false );
					break;
				}
				case 4:
				{
					setFocusAndRangeForDate( $("${selector}")[0], 6, 10, e, false );
					break;
				}
				case 9:
				{
					if ( type != 'ymd' )
					{
						setFocusAndRangeForDate( $("${selector}")[0], 11, 13, e, false );
						break;
					}
				}
				case 12:
				{
					if ( type != 'ymdh' )
					{
						setFocusAndRangeForDate( $("${selector}")[0], 14, 16, e, false );
						break;
					}
				}
				case 15:
				{
					if ( type != 'ymdhm' )
					{
						setFocusAndRangeForDate( $("${selector}")[0], 17, 19, e, false );
						break;
					}
				}
			}
		}

		<%-- Cтрелка влево  --%>
		if( e.keyCode === 37 )
	 	{
			var start = this.selectionStart;
			switch( start )
			{
				case 6:
				{
					setFocusAndRangeForDate( $("${selector}")[0], 3, 5, e, true );
					break;
				}
				case 3:
				{
					setFocusAndRangeForDate( $("${selector}")[0], 0, 2, e, true );
					break;
				}
				case 11:
				{
					if ( type != 'ymd' )
					{
						setFocusAndRangeForDate( $("${selector}")[0], 6, 10, e, true );
						break;
					}
				}
				case 14:
				{
					setFocusAndRangeForDate( $("${selector}")[0], 11, 13, e, true );
					break;
				}
				case 17:
				{
					setFocusAndRangeForDate( $("${selector}")[0], 14, 16, e, true );
					break;
				}
				case 20:
				{
					setFocusAndRangeForDate( $("${selector}")[0], 17, 19, e, true );
					break;
				}
				case 0:
				{
					if ( type == 'ymd' )
					{
				  		setFocusAndRangeForDate( $("${selector}")[0], 6, 10, e, true );
				  		break;
					}
					else if ( type == 'ymdh' )
					{
					 	setFocusAndRangeForDate( $("${selector}")[0], 11, 13, e, true );
					 	break;
					}
					else if ( type == 'ymdhm' )
					{
				 		setFocusAndRangeForDate( $("${selector}")[0], 14, 16, e, true );
				 		break;
					}
					else if ( type == 'ymdhms' )
					{
					 	setFocusAndRangeForDate( $("${selector}")[0], 17, 19, e, true );
					 	break;
					}
				}
			}
	 	}

		<%-- Стрелка вниз, идет движение вперед  --%>
		if (e.keyCode === 40 )
		{
			var start = this.selectionStart;
			switch ( start )
			{
				case 0:
				case 1:
				case 2:
				{
					setFocusAndRangeForDate( $("${selector}")[0], 3, 5, e, true );
					break;
				}
				case 3:
				case 4:
				case 5:
				{
					setFocusAndRangeForDate( $("${selector}")[0], 6, 10, e, true );
					break;
				}
				case 6:
				case 7:
				case 8:
				case 9:
				case 10:
				{
					if ( type == 'ymd' )
					{
						setFocusAndRangeForDate( $("${selector}")[0], 0, 2, e, true );
						break;
					}
					else
					{
						setFocusAndRangeForDate( $("${selector}")[0], 11, 13, e, true );
						break;
					}
				}
				case 11:
				case 12:
				case 13:
				{
					if ( type == 'ymdh' )
					{
						setFocusAndRangeForDate( $("${selector}")[0], 0, 2, e, true );
						break;
					}
					else
					{
						setFocusAndRangeForDate( $("${selector}")[0], 14, 16, e, true );
						break;
					}

				}
				case 14:
				case 15:
				{
					if ( type=='ymdhm' )
					{
						setFocusAndRangeForDate( $("${selector}")[0], 0, 2, e, true );
						break;
					}
					else
					{
						setFocusAndRangeForDate( $("${selector}")[0], 17, 19, e, true );
						break;
					}
				}
				case 17:
				case 18:
				case 19:
				case 20:
				{
					setFocusAndRangeForDate( $("${selector}")[0], 0, 2, e, true );
					break;
				}
			}
		}

	   <%-- Стрелка вправо --%>
	   if ( e.keyCode === 39 )
	   {
			var start = this.selectionStart;
			switch( start )
			{
				case 2:
				{
					setFocusAndRangeForDate( $("${selector}")[0], 3, 5, e, true );
					break;
				}
				case 5:
				{
					setFocusAndRangeForDate( $("${selector}")[0], 6, 10, e, true );
					break;
				}
				case 10:
				{
					if ( type == 'ymd' )
					{
						setFocusAndRangeForDate( $("${selector}")[0], 0, 2, e, true );
						break;
					}
					else
					{
						setFocusAndRangeForDate( $("${selector}")[0], 11, 13, e, true );
						break;
					}
				}
				case 13:
				{
					if ( type == 'ymdh' )
					{
						setFocusAndRangeForDate( $("${selector}")[0], 0, 2, e, true );
						break;
					}
					else
					{
						setFocusAndRangeForDate( $("${selector}")[0], 14, 16, e, true );
						break;
					}
				}
				case 16:
				{
					if ( type == 'ymdhm' )
					{
						setFocusAndRangeForDate( $("${selector}")[0], 0, 2, e, true );
						break;
					}
					else
					{
						setFocusAndRangeForDate( $("${selector}")[0], 17, 19, e, true );
						break;
					}
				}
				case 19:
				{
					setFocusAndRangeForDate( $("${selector}")[0], 0, 2, e, true );
					break;
				}
			 }
		}
	});

	<%--  Focus  --%>
	$( "${selector}" ).focus( function( event )
	{
		$$.debug( 'datepicker', "Focus process" );

		var start = this.selectionStart;
		this.setSelectionRange( 0 , 0 );
		var element = this;
		event.preventDefault( );
		event.stopPropagation();
		setTimeout( function( )
		{
			element.setSelectionRange( 0 , 2 );
		}, 100 );
		return false;
	});

	<%-- Обработка клика --%>
	$( "${selector}" ).click( function( e )
	{
		var type = '${type}';
		var start = this.selectionStart;
		switch ( start )
		{
			case 0:
			case 1:
			case 2:
			{
				setFocusAndRangeForDate( $("${selector}")[0], 0, 2, e, true );
				break;
			}
			case 3:
			case 4:
			case 5:
			{
				setFocusAndRangeForDate( $("${selector}")[0], 3, 5, e, true );
				break;
			}
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			{
				setFocusAndRangeForDate( $("${selector}")[0], 6, 10, e, true );
				break;
			}
			case 11:
			case 12:
			case 13:
			{
				setFocusAndRangeForDate( $("${selector}")[0], 11, 13, e, true );
				break;
			}
			case 14:
			case 15:
			case 16:
			{
				setFocusAndRangeForDate( $("${selector}")[0], 14, 16, e, true );
				break;
			}
			case 17:
			case 18:
			case 19:
			{
				setFocusAndRangeForDate( $("${selector}")[0], 17, 19, e, true );
				break;
			}
		}
	});
})