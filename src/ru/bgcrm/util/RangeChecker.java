package ru.bgcrm.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Проверяет число на попадание в набор диапазонов, заданных в виде:
 * n1,n2-n3, n4.
 * * - любой диапазон.
 */
public class RangeChecker
{
	private List<long[]> rangeList = new ArrayList<long[]>(); 
	
	public RangeChecker( String ranges )
	{
		StringTokenizer st = new StringTokenizer( ranges, ",;" );
		while( st.hasMoreTokens() )
		{
			String token = st.nextToken().trim();
			
			if( token.equals( "*" ) )
			{
				rangeList.add( new long[]{ Long.MIN_VALUE, Long.MAX_VALUE } );
			}
			else
			{
    			long[] range = new long[2];
    			
    			try
				{
    				String[] pair = token.split( "\\-" );
        			if( pair.length == 2 )
        			{
        				range[0] = Long.parseLong( pair[0].trim() );
        				range[1] = Long.parseLong( pair[1].trim() );
        			}
        			else if( pair.length == 1 )
        			{
        				range[0] = range[1] = Long.parseLong( token );
        			}
        			
        			rangeList.add( range );
				}
				catch( Exception e )
				{}
			}
		}
	}
	
	public boolean check( long value )
	{
		boolean result = false;
		
		for( long[] range : rangeList )
		{
			result = range[0] <= value && value <= range[1];
			if( result )
			{
				break;
			}
		}
		
		return result;
	}
}