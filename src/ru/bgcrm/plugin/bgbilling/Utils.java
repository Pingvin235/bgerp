package ru.bgcrm.plugin.bgbilling;

public class Utils
{
	public static String formatSessionTime( int value )
	{
		StringBuilder buf = new StringBuilder();
		if ( value / 3600 < 10 )
			buf.append( '0' );
		buf.append( value / 3600 );
		buf.append( ':' );
		if ( (value % 3600) / 60 < 10 )
			buf.append( '0' );
		buf.append( (value % 3600) / 60 );
		buf.append( ':' );
		if ( value % 60 < 10 )
			buf.append( '0' );
		buf.append( value % 60 );
		buf.append( " [" ).append( value ).append( ']' );
		return buf.toString();
	}

}
