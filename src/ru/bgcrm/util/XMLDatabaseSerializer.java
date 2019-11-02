package ru.bgcrm.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import ru.bgcrm.util.sql.SQLUtils;

public class XMLDatabaseSerializer
{
	public final static String BINARY_PREFIX = "$BINARY_DATA$";
	
	public static List<String> addItemsFromRS( Connection con, XMLStreamWriter whereAdd, String table,
	                                           String keyColumn, String keyValue, String retColumn, 
	                                           boolean dateFilter, String date1, String date2 )
	   throws SQLException, XMLStreamException
	{
		return addItemsFromRS( con, whereAdd, table, keyColumn, keyValue, retColumn, dateFilter, date1, date2, "" );
	}

	public static List<String> addItemsFromRS( Connection con, XMLStreamWriter whereAdd, String table,
	                                           String keyColumn, String keyValue, String retColumn, boolean dateFilter, String date1, String date2,
	                                           String joins ) 
	   throws SQLException, XMLStreamException
	{
		XMLDatabaseSerializer serilizer = new XMLDatabaseSerializer( whereAdd );
		return serilizer.addItemsFromRS( con, table, keyColumn, keyValue, retColumn, dateFilter, date1, date2, joins );		
	}

	// конец статики
	
	private XMLStreamWriter whereAdd;
	
	protected XMLDatabaseSerializer()
	{}

	public XMLDatabaseSerializer( XMLStreamWriter whereAdd )
	{
		this.whereAdd = whereAdd;
	}
	
	public List<String> addItemsFromRS( Connection con, String table,
										String keyColumn, String keyValue, String retColumn,
										boolean dateFilter, String date1, String date2,
										String joins )
		throws SQLException, XMLStreamException
	{
		List<String> result = new ArrayList<String>();

		if( SQLUtils.tableExists( con, table ) )
		{
			StringBuilder query = addItemFromRsQuery( table, keyColumn, keyValue, dateFilter, date1, date2, joins );

			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery( query.toString() );

			addItemsFromRS( table, retColumn, result, rs );
			st.close();
		}

		return result;
	}

	public void addItemsFromRS( String table, String retColumn, List<String> result, ResultSet rs )
		throws SQLException, XMLStreamException
	{
		ResultSetMetaData md = rs.getMetaData();
		int columns = md.getColumnCount();

		while( rs.next() )
		{
			startElement( table );

			for( int i = 1; i <= columns; i++ )
			{
				String name = md.getColumnLabel( i );
				String val;

				if( md.getColumnType( i ) == java.sql.Types.VARBINARY )
				{
					val = BINARY_PREFIX + Utils.bytesToString( rs.getBytes( i ), false );
				}
				else
				{
					val = rs.getString( i );
				}

				if( val != null )
				{
					writeAtrtibute( name, val );
					if( retColumn != null && name.equals( retColumn ) )
					{
						result.add( rs.getString( i ) );
					}
				}
			}

			stopElement();
		}
	}

	protected void writeAtrtibute( String name, String val )
		throws XMLStreamException
	{
		whereAdd.writeAttribute( name, val );
	}

	protected void stopElement()
		throws XMLStreamException
	{
		whereAdd.writeEndElement();
	}

	protected void startElement( String table )
		throws XMLStreamException
	{
		whereAdd.writeStartElement( table );
	}

	private static StringBuilder addItemFromRsQuery( String table, String keyColumn, String keyValue, boolean dateFilter, String date1, String date2, String joins )
	{
		StringBuilder query = new StringBuilder( "SELECT " + table + ".* FROM " );
		query.append( table );
		query.append( joins );
		query.append( " WHERE " );
		query.append( keyColumn );
		query.append( "='" );
		query.append( keyValue );
		query.append( "'" );

		if( dateFilter && date1 != null && date2 != null )
		{
			query.append( " AND ( " );
			query.append( date1 );
			query.append( " IS NULL OR " );
			query.append( date1 );
			query.append( "<=CURDATE() ) AND ( " );
			query.append( date2 );
			query.append( " IS NULL OR " );
			query.append( date2 );
			query.append( ">=CURDATE() )" );
		}
		return query;
	}

	public static int insertElementToBase( Element el, Connection con )
		throws SQLException
	{
		int result = 0;

		String tableName = el.getNodeName();

		if( SQLUtils.tableExists( con, tableName ) )
		{
			StringBuffer query = new StringBuffer( "INSERT INTO " );
			query.append( tableName );

			StringBuffer columns = new StringBuffer();
			StringBuffer values = new StringBuffer();

			NamedNodeMap attrs = el.getAttributes();
			int size = attrs.getLength();

			List<String> binaryValues = new ArrayList<String>();

			for( int i = 0; i < size; i++ )
			{
				Node node = attrs.item( i );
				String col = node.getNodeName();
				String value = node.getNodeValue();

				boolean isBinary = value.startsWith( BINARY_PREFIX );

				if( isBinary )
				{
					binaryValues.add( value.substring( BINARY_PREFIX.length() ) );
				}

				// столбцы имя которых начинается с "_" - фиктивные
				if( col.startsWith( "_" ) )
				{
					continue;
				}

				columns.append( col );
				columns.append( "," );

				if( (col.equals( "date1" ) || col.equals( "date2" )) &&
					(value.equals( "" ) || value.equals( "0000-00-00" )) )
				{
					values.append( "NULL" );
				}
				else if( value.startsWith( BINARY_PREFIX ) )
				{
					value = value.substring( BINARY_PREFIX.length() );
					values.append( "?" );
				}
				else
				{
					values.append( "'" );
					values.append( isBinary ? "?" : value );
					values.append( "' " );
				}

				values.append( "," );

			}

			//убираем послднюю запятую
			values.setLength( values.length() - 1 );
			columns.setLength( columns.length() - 1 );
			query.append( " ( " );
			query.append( columns );
			query.append( " ) " );

			query.append( " VALUES(" );
			query.append( values );
			query.append( ")" );

			PreparedStatement ps = con.prepareStatement( query.toString(), PreparedStatement.RETURN_GENERATED_KEYS );

			for( int i = 0; i < binaryValues.size(); i++ )
			{
				ps.setBytes( i + 1, Utils.stringToBytes( binaryValues.get( i ) ) );
			}

			ps.executeUpdate();
			result = SQLUtils.lastInsertId( ps );
			ps.close();
		}

		return result;
	}
}