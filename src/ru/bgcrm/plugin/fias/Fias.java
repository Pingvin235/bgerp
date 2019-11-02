package ru.bgcrm.plugin.fias;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SQLUtils;

public class Fias
	extends DefaultHandler
{
	//private final String ADDROBJ_FILE = "/home/lord-baldur/test/AS_ADDROBJ_20121115_c08c0617-4cac-4233-82d3-1a1825f0568d.XML";
	//private final String HOUSEINT_FILE = "/home/lord-baldur/test/AS_HOUSEINT_20121115_f2c565bd-848d-48e6-808e-fd15458d93c7.XML";

	private Connection con;
	private Setup setup;
	Set<Entry<Integer, ParameterMap>> fiasConfig;
	private HashSet<String> streetAOGUIDList = new HashSet<String>();
	private HashSet<String> houseIntGUIDList = new HashSet<String>();
	private StringBuilder log;

	public Fias()
	{
		setup = Setup.getSetup();
		fiasConfig = setup.subIndexed( "fias:area." ).entrySet();
		log = new StringBuilder();
		/*try
		{
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();

			con = setup.getDBConnectionFromPool();

			con.setAutoCommit( true );

			long time = System.currentTimeMillis();
			getStreetAOGUIDList();
			sp.parse( ADDROBJ_FILE, new StreetAddHandler() );
			System.out.println( "Streets parsed: " + (System.currentTimeMillis() - time) + " ms." );

			time = System.currentTimeMillis();
			getStreetAOGUIDList();
			getHouseIntGUIDList();
			sp.parse( HOUSEINT_FILE, new HouseIntervalAddHandler() );
			System.out.println( "Houses intervals parsed: " + (System.currentTimeMillis() - time) + " ms." );

			/*time = System.currentTimeMillis();
			getStreetAOGUIDList();
			getHouseGUIDList();
			sp.parse( HOUSE_FILE, new HouseAddHandler() );
			System.out.println( "Houses parsed: " + (System.currentTimeMillis() - time) + " ms." );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			SQLUtils.closeConnection( con );
		}*/
	}

	public String updateStreet( File streetFile )
	{
		try
		{
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();

			con = setup.getDBConnectionFromPool();

			con.setAutoCommit( true );

			log.setLength( 0 );
			long time = System.currentTimeMillis();
			getStreetAOGUIDList();
			sp.parse( streetFile, new StreetAddHandler() );
			log.append( "\r\nStreets parsed: " + (System.currentTimeMillis() - time) + " ms." );

		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			SQLUtils.closeConnection( con );
		}

		return log.toString();
	}

	public String updateHouseInterval( File houseIntervalFile )
	{
		try
		{
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();

			con = setup.getDBConnectionFromPool();

			con.setAutoCommit( true );

			log.setLength( 0 );
			long time = System.currentTimeMillis();
			getStreetAOGUIDList();
			getHouseIntGUIDList();
			sp.parse( houseIntervalFile, new HouseIntervalAddHandler() );
			log.append( "\r\nHouses intervals parsed: " + (System.currentTimeMillis() - time) + " ms." );

		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			SQLUtils.closeConnection( con );
		}

		return log.toString();
	}

	private class StreetAddHandler
		extends DefaultHandler
	{
		@Override
		public void startElement( String uri, String localName, String qName, Attributes attributes )
			throws SAXException
		{
			if( qName.equals( "Object" ) )
			{
				String aoGuid = attributes.getValue( "AOGUID" );

				String regionCode = attributes.getValue( "REGIONCODE" );
				String cityCode = attributes.getValue( "CITYCODE" );
				String areaCode = attributes.getValue( "AREACODE" );
				String placeCode = attributes.getValue( "PLACECODE" );

				for( Map.Entry<Integer, ParameterMap> me : fiasConfig )
				{
					ParameterMap params = me.getValue();

					if( !params.get( "fiasRegionCode" ).equals( regionCode ) )
					{
						continue;
					}
					if( !params.get( "fiasAreaCode" ).equals( areaCode ) )
					{
						continue;
					}
					if( !params.get( "fiasCityCode" ).equals( cityCode ) )
					{
						continue;
					}
					if( !(params.get( "fiasPlaceCode" ).equals( placeCode ) || params.get( "fiasPlaceCode" ) == null) )
					{
						continue;
					}
					String actStatus = attributes.getValue( "ACTSTATUS" );
					if( !"1".equals( actStatus ) )
					{
						return;
					}

					if( streetAOGUIDList.contains( aoGuid ) )
					{
						try
						{
							Date updateDate = new SimpleDateFormat( "yyyy-MM-dd" ).parse( attributes.getValue( "UPDATEDATE" ) );

							if( getStreetItemUpdateDate( aoGuid ).before( updateDate ) )
							{
								StringBuilder query = new StringBuilder( "DELETE FROM fias_street WHERE aoGuid=?" );
								PreparedStatement ps = con.prepareStatement( query.toString() );

								ps.setString( 1, aoGuid );
								ps.executeUpdate();
								ps.close();

								log.append( "\r\nfind update for street: " + aoGuid + " " + attributes.getValue( "FORMALNAME" ) );
							}
							else
							{
								return;
							}
						}
						catch( ParseException e )
						{
							log.append( "\r\nparse error!!!! " );
						}
						catch( SQLException e )
						{
							log.append( "\r\nsql error!!!! " );
						}
					}

					String streetName = attributes.getValue( "FORMALNAME" );
					String shortName = attributes.getValue( "SHORTNAME" );
					String postalCode = attributes.getValue( "POSTALCODE" );
					String updateDate = attributes.getValue( "UPDATEDATE" );
					try
					{
						StringBuilder query = new StringBuilder( "INSERT INTO fias_street (aoguid,city_code,region_code,crm_city_id,last_update_date,area_code,postal_code,street_title,street_short_name) VALUES (?,?,?,?,?,?,?,?,?);" );

						PreparedStatement ps = con.prepareStatement( query.toString() );

						ps.setString( 1, aoGuid );
						ps.setString( 2, cityCode );
						ps.setString( 3, regionCode );
						ps.setString( 4, params.get( "crmCityId" ) );
						ps.setString( 5, updateDate );
						ps.setString( 6, areaCode );
						ps.setString( 7, postalCode );
						ps.setString( 8, streetName );
						ps.setString( 9, shortName );
						ps.executeUpdate();
						ps.close();
					}
					catch( SQLException e )
					{
						log.append( "\r\nduplicate" );
						//throw new SAXException( e );
					}

					log.append( "\r\nFound street: " + streetName + " " + params.get( "title" ) );
				}

			}
		}
	}

	/*private class HouseAddHandler
		extends DefaultHandler
	{
		@Override
		public void startElement( String uri, String localName, String qName, Attributes attributes )
			throws SAXException
		{
			if( qName.equals( "House" ) )
			{
				String aoGuid = attributes.getValue( "AOGUID" );
				if( !streetAOGUIDList.contains( aoGuid ) )
				{
					return;
				}

				try
				{
					String houseGuid = attributes.getValue( "HOUSEGUID" );
					if( houseGUIDList.contains( houseGuid ) )
					{
						Date updateDate = new SimpleDateFormat( "yyyy-MM-dd" ).parse( attributes.getValue( "UPDATEDATE" ) );

						if( getHouseItemUpdateDate( houseGuid ).before( updateDate ) )
						{
							StringBuilder query = new StringBuilder( "DELETE FROM fias_house WHERE houseguid=?" );
							PreparedStatement ps = con.prepareStatement( query.toString() );

							ps.setString( 1, houseGuid );
							ps.executeUpdate();
							ps.close();

							System.out.println( "find update for house: " + houseGuid );
						}
						else
						{
							return;
						}
					}

					String postalCode = attributes.getValue( "POSTALCODE" );
					String houseNum = attributes.getValue( "HOUSENUM" );
					String buildNum = attributes.getValue( "BUILDNUM" );
					String strucNum = attributes.getValue( "STRUCNUM" );

					if( Utils.notBlankString( postalCode ) )
					{
						StringBuilder query = new StringBuilder( "INSERT INTO fias_house (houseguid, aoguid, postal_code,houseNum,buildNum,strucNum,last_update_date) VALUES (?,?,?,?,?,?,?);" );
						PreparedStatement ps = con.prepareStatement( query.toString() );

						ps.setString( 1, houseGuid );
						ps.setString( 2, aoGuid );
						ps.setString( 3, postalCode );
						ps.setString( 4, houseNum );
						ps.setString( 5, buildNum );
						ps.setString( 6, strucNum );
						ps.setString( 7, attributes.getValue( "UPDATEDATE" ) );
						ps.executeUpdate();
						ps.close();
					}
				}
				catch( SQLException e )
				{
					System.out.println( "duplicate" );
				}

				catch( ParseException e )
				{
					throw new SAXException( e );
				}
			}
		}

	}*/

	private class HouseIntervalAddHandler
		extends DefaultHandler
	{
		@Override
		public void startElement( String uri, String localName, String qName, Attributes attributes )
			throws SAXException
		{
			if( qName.equals( "HouseInterval" ) )
			{
				String aoGuid = attributes.getValue( "AOGUID" );
				if( !streetAOGUIDList.contains( aoGuid ) )
				{
					return;
				}

				try
				{
					String intGuid = attributes.getValue( "INTGUID" );
					if( houseIntGUIDList.contains( intGuid ) )
					{
						Date updateDate = new SimpleDateFormat( "yyyy-MM-dd" ).parse( attributes.getValue( "UPDATEDATE" ) );

						if( getIntervalItemUpdateDate( intGuid ).before( updateDate ) )
						{
							StringBuilder query = new StringBuilder( "DELETE FROM fias_house_interval WHERE intguid=?" );
							PreparedStatement ps = con.prepareStatement( query.toString() );

							ps.setString( 1, intGuid );
							ps.executeUpdate();
							ps.close();

							log.append( "\r\nfind update for house interval: " + intGuid );
						}
						else
						{
							return;
						}
					}

					String postalCode = attributes.getValue( "POSTALCODE" );
					String startIndex = attributes.getValue( "INTSTART" );
					String endIndex = attributes.getValue( "INTEND" );
					String status = attributes.getValue( "INTSTATUS" );

					if( Utils.notBlankString( postalCode ) )
					{
						StringBuilder query = new StringBuilder( "INSERT INTO fias_house_interval (intguid, aoguid, postal_code,start_index,end_index,status,last_update_date) VALUES (?,?,?,?,?,?,?);" );
						PreparedStatement ps = con.prepareStatement( query.toString() );

						ps.setString( 1, intGuid );
						ps.setString( 2, aoGuid );
						ps.setString( 3, postalCode );
						ps.setString( 4, startIndex );
						ps.setString( 5, endIndex );
						ps.setString( 6, status );
						ps.setString( 7, attributes.getValue( "UPDATEDATE" ) );
						ps.executeUpdate();
						ps.close();
					}
				}
				catch( SQLException e )
				{
					log.append( "\r\nduplicate" );
				}

				catch( ParseException e )
				{
					log.append( "\r\nparse error!!!" );
				}
			}
		}

	}

	private Date getStreetItemUpdateDate( String aoGuid )
	{
		ResultSet rs = null;
		PreparedStatement ps = null;
		StringBuilder query = new StringBuilder( "SELECT last_update_date FROM " );
		query.append( "fias_street " );
		query.append( "WHERE aoGuid=?" );

		try
		{
			ps = con.prepareStatement( query.toString() );
			ps.setString( 1, aoGuid );
			rs = ps.executeQuery();

			if( rs.next() )
			{
				SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd" );
				return formatter.parse( rs.getString( 1 ) );
			}

		}
		catch( SQLException e1 )
		{
			log.append( "\r\nExpression error!!!! " );
		}
		catch( ParseException e )
		{
			e.printStackTrace();
		}

		return TimeUtils.parse( "01.01.1900", TimeUtils.PATTERN_DDMMYYYY );
	}

	private void getStreetAOGUIDList()
	{
		streetAOGUIDList.clear();
		ResultSet rs = null;
		PreparedStatement ps = null;
		StringBuilder query = new StringBuilder( "SELECT aoguid FROM " );
		query.append( "fias_street" );

		try
		{
			ps = con.prepareStatement( query.toString() );
			rs = ps.executeQuery();

			while( rs.next() )
			{
				streetAOGUIDList.add( rs.getString( 1 ) );
			}
		}
		catch( SQLException e1 )
		{
			log.append( "\r\nExpression error!!!! " );
		}

	}

	/*private Date getHouseItemUpdateDate( String houseGuid )
	{
		ResultSet rs = null;
		PreparedStatement ps = null;
		StringBuilder query = new StringBuilder( "SELECT last_update_date FROM " );
		query.append( "fias_house " );
		query.append( "WHERE houseGuid=?" );

		try
		{
			ps = con.prepareStatement( query.toString() );
			ps.setString( 1, houseGuid );
			rs = ps.executeQuery();

			if( rs.next() )
			{
				SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd" );
				return formatter.parse( rs.getString( 1 ) );
			}

		}
		catch( SQLException e1 )
		{
			log.append( "\r\nExpression error!!!! " );
		}
		catch( ParseException e )
		{
			e.printStackTrace();
		}

		return new Date( 1900, 1, 1 );
	}*/

	/*private void getHouseGUIDList()
	{
		houseGUIDList.clear();
		ResultSet rs = null;
		PreparedStatement ps = null;
		StringBuilder query = new StringBuilder( "SELECT houseguid FROM " );
		query.append( "fias_house" );

		try
		{
			ps = con.prepareStatement( query.toString() );
			rs = ps.executeQuery();

			while( rs.next() )
			{
				houseGUIDList.add( rs.getString( 1 ) );
			}
		}
		catch( SQLException e1 )
		{
			System.out.println( "Expression error!!!! " );
		}

	}*/

	private Date getIntervalItemUpdateDate( String houseIntGuid )
	{
		ResultSet rs = null;
		PreparedStatement ps = null;
		StringBuilder query = new StringBuilder( "SELECT last_update_date FROM " );
		query.append( "fias_house_interval " );
		query.append( "WHERE intGuid=?" );

		try
		{
			ps = con.prepareStatement( query.toString() );
			ps.setString( 1, houseIntGuid );
			rs = ps.executeQuery();

			if( rs.next() )
			{
				SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd" );
				return formatter.parse( rs.getString( 1 ) );
			}

		}
		catch( SQLException e1 )
		{
			log.append( "\r\nExpression error!!!! " );
		}
		catch( ParseException e )
		{
			e.printStackTrace();
		}

		return TimeUtils.parse( "01.01.1900", TimeUtils.PATTERN_DDMMYYYY );
	}

	private void getHouseIntGUIDList()
	{
		houseIntGUIDList.clear();
		ResultSet rs = null;
		PreparedStatement ps = null;
		StringBuilder query = new StringBuilder( "SELECT intguid FROM " );
		query.append( "fias_house_interval" );

		try
		{
			ps = con.prepareStatement( query.toString() );
			rs = ps.executeQuery();

			while( rs.next() )
			{
				houseIntGUIDList.add( rs.getString( 1 ) );
			}
		}
		catch( SQLException e1 )
		{
			log.append( "\r\nExpression error!!!! " );
		}

	}

}
