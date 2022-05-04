package ru.bgcrm.test.exp;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
import org.bgerp.plugin.pln.callboard.dao.ShiftDAO;
import org.bgerp.plugin.pln.callboard.dao.WorkTypeDAO;
import org.bgerp.plugin.pln.callboard.model.DayType;
import org.bgerp.plugin.pln.callboard.model.WorkDaysCalendar;
import org.bgerp.plugin.pln.callboard.model.WorkShift;
import org.bgerp.plugin.pln.callboard.model.WorkType;
import org.bgerp.plugin.pln.callboard.model.WorkTypeTime;
import org.bgerp.plugin.pln.callboard.model.config.CalendarConfig;

import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class Test_POIGenerate
{
	private static final Logger log = Logger.getLogger( Test_POIGenerate.class );

	private static final String SHORTCUT_FREE_DAY = "В";

	private static final int USER_ROW_FROM = 25, USER_ROWS = 4;
	private static final int USER_COL_FROM = 0, USER_COLS = 27;

	// правый столбик "Отработано за" по сокращениям позиция в столбе
	private static final Map<String, Integer> SHORTCUT_POS = new HashMap<String, Integer>();
	static
	{
		SHORTCUT_POS.put( "Я", 0 );
		SHORTCUT_POS.put( "Н", 2 );
		SHORTCUT_POS.put( "РВ", 3 );
	}

	private static final Map<Integer, Integer> DAY_TYPE_WORK_HOURS = new HashMap<Integer, Integer>();
	static
	{
		DAY_TYPE_WORK_HOURS.put( 1, 8 );
		DAY_TYPE_WORK_HOURS.put( 3, 7 );
	}

	public static void main( String[] args )
		throws Exception
	{
		final String orgName = "Служба технической поддержки службы удаленной поддержки клиентов";

		Setup setup = Setup.getSetup(  "bgcrm_ufanet", true );
		Connection con = setup.getDBConnectionFromPool();

		final int paramTabelNumber = 884;
		final int paramDolznost = 773;
		final int graphId = 2;
		final int groupId = 9;
		final int calendarId = 1;
		final Calendar dateFrom = new GregorianCalendar( 2013, Calendar.DECEMBER, 1 );
		Calendar dateTo = new GregorianCalendar( 2013, Calendar.DECEMBER, 31 );

		WorkDaysCalendar calendar = setup.getConfig( CalendarConfig.class ).getCalendar( calendarId );
		Map<Date, Integer> excludeDates = new WorkTypeDAO( con ).getWorkDaysCalendarExcludes( calendarId );

		ParamValueDAO paramDao = new ParamValueDAO( con );

		Map<Integer, Map<Date, WorkShift>> userShifts = new ShiftDAO( con ).getUserShifts( graphId, dateFrom.getTime(), dateTo.getTime() );

		List<User> userList = new UserDAO( con ).getUserList( Collections.singleton( groupId ), dateFrom.getTime(), dateTo.getTime() );

		// "/home/shamil/tmp/tabel_blank.xls"
		HSSFWorkbook workbook = new HSSFWorkbook( new FileInputStream( System.getProperty( "template" ) ) );
		HSSFSheet sheet = workbook.getSheetAt( 0 );

		// отдел
		sheet.getRow( 9 ).getCell( 1 ).setCellValue( orgName );

		// даты
		addDates( dateFrom, dateTo, sheet );

		final int days = dateTo.getActualMaximum( Calendar.DAY_OF_MONTH );

		// числа
		addDays( sheet, days, 20, new GetForDay()
		{
			@Override
			public String add( int day, boolean upCell )
			{
				return upCell ? String.valueOf( day ) : "";
			}

			@Override
			public String getDefault( boolean upCell )
			{
				return upCell ? "X" : "";
			}
		});

		final Map<Integer, WorkType> workTypeMap = new WorkTypeDAO( con ).getWorkTypeMap();

		// диапазоны в строках с пользователем
		List<CellRangeAddress> rangesForCopy = getRangesForCopy( sheet );

		// подсчёт количества рабочих часов
		int workHoursSum = 0;

		Calendar date = (Calendar)dateFrom.clone();
		while( TimeUtils.dateBeforeOrEq( date, dateTo ) )
		{
			Pair<DayType, Boolean> typePair = calendar.getDayType( date.getTime(), excludeDates );
			Integer workHours = DAY_TYPE_WORK_HOURS.get( typePair.getFirst().getId() );
			if( workHours != null )
			{
				workHoursSum += workHours;
			}

			date.add( Calendar.DAY_OF_YEAR, 1 );
		}

		// тестирование
		for( int i = 0; i < userList.size(); i++ )
		{
			// Кулаков, Кремер
			if( userList.get( i ).getId() != 2428 /*&&
				userList.get( i ).getId() != 875*/ )
			{
				userList.remove( i );
				i--;
			}
		}

		// обработка пользователей
		final int size = userList.size();
		for( int i = 0; i < size; i++ )
		{
			User user = userList.get( i );

			int offset = i * USER_ROWS;

			if( i > 0 )
			{
				sheet.shiftRows( 30 + offset - USER_ROWS, 34 + offset - USER_ROWS, USER_ROWS );
				createUserRows( sheet, USER_ROW_FROM + offset, rangesForCopy );
			}

			String post = Utils.maskNull( paramDao.getParamText( user.getId(), paramDolznost ) );
			if( Utils.notBlankString( post ) )
			{
				post = ", " + post;
			}

			// порядковый номер и ФИО
			HSSFRow row = sheet.getRow( USER_ROW_FROM + offset );
			row.getCell( 0 ).setCellValue( i + 1 );
			row.getCell( 1 ).setCellValue( user.getTitle() + post );
			row.getCell( 2 ).setCellValue( paramDao.getParamText( user.getId(), paramTabelNumber ) );

			// ключ - строка
			final Map<String, Integer> neyavkMap = new HashMap<String, Integer>( 4 );

			@SuppressWarnings("unchecked")
			final Map<Date, WorkShift> dateShifts = (Map<Date, WorkShift>)Utils.maskNull( userShifts.get( user.getId() ), Collections.emptyMap() );

			// суммы для столбца "Отработано за месяц" кроме сверхурочки
			// по позициям в 0 элементе - сумма дней, в 1 - минут
			final int[][] workedForPeriod = new int[4][2];

			// только для возможности увеличивать из класса
			final AtomicInteger userWorkMinutes = new AtomicInteger();

			addDays( sheet, days, USER_ROW_FROM + offset, new GetForDay()
			{
				// т.к. первый вызов идёт для upCell, чтобы два раза не считать - сюда сохраняется строка для нижней ячейки
				private String valueForDownCell = "";

				@Override
				public String add( int day, boolean upCell )
				{
					if( upCell )
					{
    					Calendar clnd = (Calendar)dateFrom.clone();
    					clnd.set( Calendar.DAY_OF_MONTH, day );

    					Date curDate = TimeUtils.convertCalendarToDate( clnd );
    					Date prevDate = TimeUtils.getPrevDay( curDate );

    					// ключ - код типа работ. значение - суммарное число минут в эти сутки
    					LinkedHashMap<String, Integer> labels = new LinkedHashMap<String,Integer>();

    					// смены предыдущих суток, может что перешло на эти
    					WorkShift shift = dateShifts.get( prevDate );
    					if( shift != null )
    					{
    						for( WorkTypeTime time : shift.getWorkTypeTimeList() )
    						{
    							WorkType type = workTypeMap.get( time.getWorkTypeId() );
        						if( type != null )
        						{
            						if( !type.getIsNonWorkHours() )
            						{
            							int minutes = time.getWorkMinutesInDay( type, prevDate, curDate );
            							if( minutes > 0 )
            							{
                							for( String shortcut : type.getShortcutList() )
                							{
                								labels.put( shortcut, Utils.maskNull( labels.get( shortcut ), 0 ) + minutes );
                							}
                							userWorkMinutes.addAndGet( minutes );
            							}
            						}
        						}
            					else
            					{
        							log.warn( "Can't find workType with id=" + time.getWorkTypeId() );
        						}
        					}
    					}

    					// смена текущих суток
    					shift = dateShifts.get( curDate );
    					if( shift != null )
    					{
    						for( WorkTypeTime time : shift.getWorkTypeTimeList() )
    						{
    							WorkType type = workTypeMap.get( time.getWorkTypeId() );
    							if( type != null )
    							{
    								// выявление неявок
    								if( type.getIsNonWorkHours() )
    								{
    									int hours = time.getMinutesInDay( type, curDate, curDate, false ) / 60;
    									// весь день не учитывамый тип работ - неявка
    									if( hours >= 23 )
    									{
    										String shortcut = Utils.getFirst( type.getShortcutList() );

    										Integer current = Utils.maskNull( neyavkMap.get( shortcut ), 0 );
    										neyavkMap.put( shortcut, ++current );

    										valueForDownCell = "";
    										return shortcut;
    									}
    								}
    								else
    								{
    									int minutes = time.getWorkMinutesInDay( type, curDate, curDate );
    									if( minutes > 0 )
    									{
                							for( String shortcut : type.getShortcutList() )
                							{
                								labels.put( shortcut, Utils.maskNull( labels.get( shortcut ), 1 ) + minutes );
                							}
                							userWorkMinutes.addAndGet( minutes );
    									}
    								}
    							}
    							else
    							{
    								log.warn( "Can't find workType with id=" + time.getWorkTypeId() );
    							}
    						}
    					}

    					for( Map.Entry<String, Integer> me : labels.entrySet() )
						{
							String shortcut = me.getKey();
							int minutes = me.getValue();

							Integer position = SHORTCUT_POS.get( shortcut );
							if( position != null )
							{
								workedForPeriod[position][0] ++;
								workedForPeriod[position][1] += minutes;

								if( log.isDebugEnabled() )
								{
									log.debug( day + " " + shortcut + " " + workedForPeriod[position][0] + " m " + workedForPeriod[position][1] + " h " + workedForPeriod[position][1] / 60 );
								}
							}
						}

    					StringBuilder labelString = new StringBuilder( 100 );
    					StringBuilder hoursString = new StringBuilder( 100 );

    					for( Map.Entry<String, Integer> me : labels.entrySet() )
    					{
    						Utils.addSeparated( labelString, " / ", me.getKey() );
    						Utils.addSeparated( hoursString, " / ", String.valueOf( me.getValue() / 60 ) );
    					}

    					if( labels.size() == 0 )
    					{
    						labelString.append( SHORTCUT_FREE_DAY );
    					}

    					valueForDownCell = hoursString.toString();

    					return labelString.toString();
					}
					else
					{
						return valueForDownCell;
					}
				}

				@Override
				public String getDefault( boolean upCell )
				{
					return "X";
				}
			});

			HSSFRow rowNext = sheet.getRow( USER_ROW_FROM + offset + 2 );

			// раб., команд., ночн., празд.
			for( int k = 0; k < 4; k++ )
			{
				if( workedForPeriod[k][0] > 0 )
				{
					row.getCell( 19 + k ).setCellValue( workedForPeriod[k][0] );
					rowNext.getCell( 19 + k ).setCellValue( workedForPeriod[k][1] / 60 );
				}
			}

			int userWorkHours = userWorkMinutes.get();

			// сверхурочка
			if( userWorkHours > workHoursSum )
			{

			}

			int pos = 0;
			for( Map.Entry<String, Integer> me : neyavkMap.entrySet() )
			{
				row = sheet.getRow( USER_ROW_FROM + offset + pos++ );
				row.getCell( 24 ).setCellValue( me.getKey() );
				row.getCell( 25 ).setCellValue( me.getValue() );
			}

			/*if( i > 4 )
			{
				break;
			}*/
		}

		// "/home/shamil/tmp/new.xls"
		FileOutputStream out = new FileOutputStream( System.getProperty( "result" ) );
		workbook.write( out );
		out.close();

		System.out.println( "Excel written successfully.." );
	}


	public static List<CellRangeAddress> getRangesForCopy( HSSFSheet sheet )
	{
		List<CellRangeAddress> rangesForCopy = new ArrayList<CellRangeAddress>();
		for (int i = 0; i < sheet.getNumMergedRegions(); i++)
		{
			CellRangeAddress range = sheet.getMergedRegion( i );
			if( USER_ROW_FROM <= range.getFirstRow() && range.getFirstRow() < USER_ROW_FROM + USER_ROWS &&
				USER_COL_FROM <= range.getFirstColumn() && range.getLastColumn() < USER_COL_FROM + USER_COLS )
			{
				rangesForCopy.add( range );
			}
		}
		return rangesForCopy;
	}


	public static void addDates( Calendar dateFrom, Calendar dateTo, HSSFSheet sheet )
	{
		HSSFRow row = sheet.getRow( 14 );
		row.getCell( 15 ).setCellValue( TimeUtils.format( dateTo.getTime(), TimeUtils.FORMAT_TYPE_YMD ) );
		row.getCell( 19 ).setCellValue( TimeUtils.format( dateFrom.getTime(), TimeUtils.FORMAT_TYPE_YMD ) );
		row.getCell( 21 ).setCellValue( TimeUtils.format( dateTo.getTime(), TimeUtils.FORMAT_TYPE_YMD ) );
	}

	public static void addDays( HSSFSheet sheet, final int days, int rowNum, GetForDay adder )
	{
		// числа
		final int daysInFirstRow = days / 2;

		int d = 1;

		HSSFRow row1 = sheet.getRow( rowNum );
		HSSFRow row2 = sheet.getRow( rowNum + 1 );
		for( int c = 1; c <= 16; c++ )
		{
			final int cellnum = 2 + c;

			row1.getCell( cellnum ).setCellValue( c <= daysInFirstRow ? String.valueOf( adder.add( d, true ) ) : adder.getDefault( true ) );
			row2.getCell( cellnum ).setCellValue( c <= daysInFirstRow ? String.valueOf( adder.add( d, false ) ) : adder.getDefault( false ) );

			if( c <= daysInFirstRow )
			{
				d++;
			}
		}

		row1 = sheet.getRow( rowNum + 2 );
		row2 = sheet.getRow( rowNum + 3 );
		for( ; d <= 31; d++ )
		{
			final int cellnum = 2 + d - 15;
			row1.getCell( cellnum ).setCellValue( d <= days ? String.valueOf( adder.add( d, true ) ) : adder.getDefault( true ) );
			row2.getCell( cellnum ).setCellValue( d <= days ? String.valueOf( adder.add( d, false ) ) : adder.getDefault( false ) );
		}
	}

	private static interface GetForDay
	{
		public String add( int day, boolean upCell );
		public String getDefault( boolean upCell );
	}

	public static void createUserRows( HSSFSheet sheet, int toRow, List<CellRangeAddress> rangesForCopy )
	{
		for( int rowInd = 0; rowInd < USER_ROWS; rowInd++ )
		{
			HSSFRow rowFrom = sheet.getRow( USER_ROW_FROM + rowInd );
			HSSFRow row = sheet.createRow( toRow + rowInd );

			for( int colInd = USER_COL_FROM; colInd < USER_COLS; colInd++ )
			{
				Cell cellFrom = rowFrom.getCell( colInd );

				Cell cell = row.createCell( colInd );
				cell.setCellStyle( cellFrom.getCellStyle() );
				cell.setCellValue( "" );
			}
		}

		int rowDelta = toRow - USER_ROW_FROM;
		for( CellRangeAddress address : rangesForCopy )
		{
			sheet.addMergedRegion( new CellRangeAddress( address.getFirstRow() + rowDelta, address.getLastRow() + rowDelta,
			                                             address.getFirstColumn(), address.getLastColumn() ) );
		}
	}


/*	HSSFWorkbook workbook = new HSSFWorkbook();
	HSSFSheet sheet = workbook.createSheet( "Table" );

	HSSFFont font10b = workbook.createFont();
	font10b.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
	font10b.setFontHeightInPoints( (short)10 );

	HSSFCellStyle style = null;

	// наименование организации
	HSSFRow row = sheet.createRow( 0 );
	Cell cell = row.createCell( 1 );

	style = workbook.createCellStyle();
	style.setFont(font10b);
	style.setAlignment( HSSFCellStyle.ALIGN_CENTER );
	style.setBorderBottom( HSSFCellStyle.BORDER_THIN );

	cell.setCellStyle( style );
	cell.setCellValue( orgName );

	CellRangeAddress region = new CellRangeAddress( 0, 1, 1, 19 );
	addRegionStyle( sheet, region, style );
	sheet.addMergedRegion( region );

	//

	FileOutputStream out = new FileOutputStream( new File( "/home/shamil/tmp/new.xls" ) );
	workbook.write( out );
	out.close();
	System.out.println( "Excel written successfully.." );*/

	// чтобы у объединённого региона был какой-то стиль, его нужно поставить на все присоединяемые ячейки
	/*private static void addRegionStyle( HSSFSheet sheet, CellRangeAddress region, HSSFCellStyle style )
	{
		HSSFRow row = null;
		Cell cell = null;

		for( int rowNum = region.getFirstRow(); rowNum <= region.getLastRow(); rowNum++ )
		{
			row = sheet.getRow( rowNum );
			if( row == null )
			{
				row = sheet.createRow( rowNum );
			}

			int startColumn = rowNum == region.getFirstRow() ? region.getFirstColumn() + 1 : region.getFirstColumn();
			for( int colNum = startColumn; colNum <= region.getLastColumn(); colNum++ )
    		{
				cell = row.createCell( colNum );
    			cell.setCellStyle( style );
			}
		}
	}*/
}
