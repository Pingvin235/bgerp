package ru.bgcrm.util;

/*
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternStringGenerator
{
	private static HashMap<String, Pattern> insertPartPatterns = new HashMap<String, Pattern>( 12 );

	public static String insertPatternPart( String address, String key, String value )
	{
		StringBuilder result = new StringBuilder( address.length() );
		Pattern p = insertPartPatterns.get( key );
		if( p == null )
		{
			p = Pattern.compile( "\\(([\\wа-яА-Я\\,\\.\\:\\s\\[\\]\\(\\)\\\\/#]*)\\$\\{" + key
			                     + "\\}([\\wа-яА-Я\\,\\.\\:\\s\\[\\]\\(\\)\\\\/#]*)\\)" );
			insertPartPatterns.put( key, p );
		}
		Matcher m = p.matcher( address );
		if( m.find() )
		{
			if( Utils.notBlankString( value ) )
			{
				result.append( address.substring( 0, m.start() ) );
				String prefix = m.group( 1 );
				if( prefix.startsWith( "," ) && result.length() == 0 )
				{
					prefix = prefix.substring( 1 );
				}
				result.append( prefix );
				result.append( value );
				result.append( m.group( 2 ) );
				result.append( address.substring( m.end() ) );
			}
			else
			{
				result.append( address.substring( 0, m.start() ) );
				result.append( address.substring( m.end() ) );
			}
		}
		else
		{
			result.append( address );
		}
		return result.toString();
	}
}
*/