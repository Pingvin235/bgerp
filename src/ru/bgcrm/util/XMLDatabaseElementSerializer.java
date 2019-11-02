package ru.bgcrm.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.w3c.dom.Element;

public class XMLDatabaseElementSerializer 
	extends XMLDatabaseSerializer
{
	public static List<String> addItemsFromRS( Connection con, Element whereAdd, String table, String keyColumn,
	                                           String keyValue, String retColumn, boolean dateFilter, 
	                                           String date1, String date2)
		throws SQLException, XMLStreamException
	{
		XMLDatabaseElementSerializer serilizer = new XMLDatabaseElementSerializer( whereAdd );
		return serilizer.addItemsFromRS( con, table, keyColumn, keyValue, retColumn, dateFilter, date1, date2, "" );
	}
	
	public static List<String> addItemsFromRS( Connection con, Element whereAdd, String table, String keyColumn,
	                                           String keyValue, String retColumn, boolean dateFilter, 
	                                           String date1, String date2, String joins )
		throws SQLException, XMLStreamException
	{
		XMLDatabaseElementSerializer serilizer = new XMLDatabaseElementSerializer( whereAdd );
		return serilizer.addItemsFromRS( con, table, keyColumn, keyValue, retColumn, dateFilter, date1, date2, joins );
	}
	
	// конец статики
	
	private Element whereAdd;
	private Element item;
	
	public XMLDatabaseElementSerializer( Element whereAdd)
	{
		this.whereAdd = whereAdd;
	}
	
	protected void startElement( String table )
    	throws XMLStreamException
	{
		item = whereAdd.getOwnerDocument().createElement( table );
		whereAdd.appendChild( item );
	}
	
	protected  void writeAtrtibute( String name, String val )
    	throws XMLStreamException
	{
		item.setAttribute( name, val );						
	}
	
	protected  void stopElement()
	    throws XMLStreamException
	{}
}