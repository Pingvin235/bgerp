package ru.bgcrm.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterPhoneValueItem;

/*
 * # форматирование
phones.numberformat=+X XXX XXX-XX-XX
phones.numberformat.f10=+X XXX-XXX-XX-XX
phones.numberformat.f13=+X (XXX) XXX-XX-XX
phones.numberformat.f14=+X (XXXX) XX-XX-XX
phones.numberformat.f15=+X (XXXXX) X-XX-XX
 */

public class PhoneUtils
{
	private final static String defaultPattern = "${phone} [${comm}]";
	private final static String defaultPhoneFormatPattern = "+X XXX XXX-XX-XX";

	/**
	 * Получить результирующую строку параметра типа телефон
	 * @param s Setup
	 * @param pd код параметра
	 * @param phones список телефонов
	 * @param formats список форматов телефонов
	 * @param comments список комментариев
	 * @return
	 */
	public static final String getPhones( int paramId, ParameterPhoneValue value )
	{
		int countPhone = value.getItemList().size();

		Setup setup = Setup.getSetup();

		StringBuilder result = new StringBuilder();
		String pattern = setup.get( "phones.format." + paramId, null );
		if( pattern == null )
		{
			pattern = setup.get( "phones.format", defaultPattern );
		}

		for( int i = 1; i < countPhone + 1; ++i )
		{
			String patternBufer = pattern;
			String phonePrefix = setup.get( "phones.prefix." + paramId + "." + i, null );
			if( phonePrefix == null )
			{
				phonePrefix = setup.get( "phones.prefix." + i, "" );
			}

			ParameterPhoneValueItem item = value.getItemList().get( i - 1 );
			patternBufer = insertPhonePart( patternBufer, "phone", phoneToFormat( item.getPhone().trim(), item.getFormat().trim(), i, setup, paramId ) );
			patternBufer = patternBufer.replaceAll( "\\$\\{comm\\}", commentToFormat( Utils.maskNull( item.getComment() ), i, setup, paramId ) );
			if( i > 1 )
			{
				result.append( "; " );
			}
			result.append( patternBufer );
		}

		return result.toString();
	}

	private static String commentToFormat( String comment, int i, Setup setup, int paramId )
	{
		String result = "";
		if( comment.isEmpty() )
		{
			return result;
		}

		result = comment;

		String comPattern = setup.get( "phones.comment." + paramId + "." + i, null );
		if( comPattern == null )
		{
			comPattern = setup.get( "phones.comment." + paramId, null );
		}
		if( comPattern == null )
		{
			comPattern = setup.get( "phones.comment." + i, null );
		}
		if( comPattern == null )
		{
			comPattern = setup.get( "phones.comment", null );
		}
		if( comPattern == null )
		{
			return result;
		}

		if( comPattern.startsWith( "\"" ) && comPattern.startsWith( "\"" ) )
		{
			comPattern = comPattern.substring( 1, comPattern.length() - 1 );
		}

		result = comPattern.replaceAll( "\\$\\{comment\\}", comment );

		return result;
	}

	private static String phoneToFormat( String value, String format, int num, Setup setup, int paramId )
	{
		String pattern = null;
		if( format != null )
		{
			pattern = setup.get( "phones.numberformat." + paramId + ".f" + format, null );
		}
		if( pattern == null )
		{
			pattern = setup.get( "phones.numberformat." + paramId, null );
		}
		if( pattern == null && format != null )
		{
			pattern = setup.get( "phones.numberformat.f" + format, null );
		}
		if( pattern == null )
		{
			pattern = setup.get( "phones.numberformat", defaultPhoneFormatPattern );
		}

		String result = pattern;

		for( int i = 0; i < value.length(); ++i )
		{
			result = result.replaceFirst( "X", value.substring( i, i + 1 ) );
		}

		if( result.equals( pattern ) )
		{
			result = "";
		}

		result = result.replaceAll( "X", "" );

		return result;
	}

	private static String insertPhonePart( String phones, String key, String value )
	{
		StringBuilder result = new StringBuilder( phones.length() );
		Pattern p = Pattern.compile( "([^()]*)\\$\\{" + key + "\\}([^()]*)" );

		Matcher m = p.matcher( phones );
		if( m.find() )
		{
			if( Utils.notBlankString( value ) )
			{
				result.append( phones.substring( 0, m.start() ) );
				String prefix = m.group( 1 );
				if( prefix.startsWith( ";" ) && result.length() == 0 )
				{
					prefix = prefix.substring( 1 );
				}
				result.append( prefix );
				result.append( value );
				result.append( m.group( 2 ) );
				result.append( phones.substring( m.end() ) );
			}
			else
			{
				result.append( phones.substring( 0, m.start() ) );
				result.append( phones.substring( m.end() ) );
			}
		}
		else
		{
			result.append( phones );
		}
		return result.toString();
	}
}
