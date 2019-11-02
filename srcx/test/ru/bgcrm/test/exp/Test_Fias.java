package ru.bgcrm.test.exp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SQLUtils;

public class Test_Fias	
	extends DefaultHandler
{
	private final String ADDROBJ_FILE = "/home/shamil/tmp/fias/AS_ADDROBJ_20120826_184c4762-a630-4549-b430-08b20f51989e.XML";
	private final String HOUSEINT_FILE = "/home/shamil/tmp/fias/AS_HOUSEINT_20120826_48a1004e-2475-40a7-bb7d-8d70f27753f3.XML";
	
	//private int counter = 0;
	private Connection con;
	//private AddressDAO addressDao;
	private Map<String, List<AddressHouse>> data = new HashMap<String, List<AddressHouse>>();
	
	
	public Test_Fias()
	{
		long time = System.currentTimeMillis();
		
		Setup setup = Setup.getSetup( "bgcrm_ufanet", true );
		
		try
		{
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			
			con = setup.getDBConnectionFromPool();
			
			con.setAutoCommit( true );
			
			//addressDao = new AddressDAO( con );
			
			String query = 
    			"SELECT street.title, house.id, house.house, house.frac, house.street_id FROM address_house AS house " +
    			"INNER JOIN address_street AS street ON house.street_id=street.id AND street.city_id=32";
    		ResultSet rs = con.createStatement().executeQuery( query );
    		
    		while( rs.next() )
    		{
    			String streetName = rs.getString( 1 );
    			
    			List<AddressHouse> streetData = data.get( streetName );
    			if( streetData == null )
    			{
    				data.put( streetName, streetData = new ArrayList<AddressHouse>() );
    			}
    			
    			AddressHouse house = new AddressHouse();
    			house.setId( rs.getInt( 2 ) );
    			house.setHouse( rs.getInt( 3 ) );
    			house.setFrac( rs.getString( 4 ) );
    			house.setStreetId( rs.getInt( 5 ) );
    			
    			streetData.add( house );
    		}    		
    		rs.close();
    		
    		System.out.println( "House loaded: " + (System.currentTimeMillis() - time) + " ms." );
			time = System.currentTimeMillis();
			
			sp.parse( ADDROBJ_FILE, new StreetHandler() );
			
			System.out.println( "Streets parsed: " + (System.currentTimeMillis() - time) + " ms." );
			time = System.currentTimeMillis();
			
			sp.parse( HOUSEINT_FILE, new HouseIntHandler() );
			
			System.out.println( "HouseInt parsed: " + (System.currentTimeMillis() - time) + " ms." );
			time = System.currentTimeMillis();
			
			int count = 0;			
			for( Map.Entry<String, List<AddressHouse>> me : data.entrySet() )
			{
				count += me.getValue().size();
				
				System.out.print( me.getKey() + ": " + me.getValue().size() + " : " );
				for( AddressHouse house : me.getValue() )
				{
					System.out.print( house.getHouseAndFrac() + " " );
				}
				System.out.println();
			}
			
			System.out.println( "Всего без индекса: "  + count );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}	
		finally
		{
			SQLUtils.closeConnection( con );
		}
	}
	
	private class StreetHandler
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
    			
    			if( "56".equals( regionCode ) && "001".equals( cityCode ) )
    			//if( "02".equals( regionCode ) && "001".equals( cityCode ) )
    			{
    				String actStatus = attributes.getValue( "ACTSTATUS" );
    				if( !"1".equals( actStatus ) )
    				{
    					return;
    				}
    				
    				String streetName = attributes.getValue( "FORMALNAME" );
    				String shortName = attributes.getValue( "SHORTNAME" );
    				if( !"ул".equals( shortName ) )
    				{
    					streetName += " " + shortName;
    				}
    				
    				String postalCode = attributes.getValue( "POSTALCODE" );
    				
    				List<AddressHouse> streetData = data.remove( streetName );
    				if( streetData != null )
    				{
    					if( Utils.notBlankString( postalCode ) )
    					{
    						try
							{
    							String query = "UPDATE address_house SET post_index=? WHERE street_id=?";
        						PreparedStatement ps = con.prepareStatement( query );
        						
        						ps.setString( 1, postalCode );
        						ps.setInt( 2, Utils.getFirst( streetData ).getStreetId() );
        						ps.executeUpdate();
        						ps.close();
							}
							catch( SQLException e )
							{
								throw new SAXException( e );
							}
    						
    						System.out.println( "Found index for street: " + streetName );    						
    					}
    					else
    					{
    						System.out.println( "Street " + streetName + " AOGUID: " + aoGuid );
    						data.put( aoGuid, streetData );
    						
    						/*for( int i = 0; i < attributes.getLength(); i++ )
            				{
            					System.out.print( attributes.getQName( i ) + "=" + attributes.getValue( i ) + "; " );
            				}
            				System.out.println();*/
    					}
    				}
    			}
    		}
    	}
	}
	
	private class HouseIntHandler
		extends DefaultHandler
	{
		@Override
    	public void startElement( String uri, String localName, String qName, Attributes attributes )
    		throws SAXException
    	{
    		if( qName.equals( "HouseInterval" ) )
    		{
    			String postalCode = attributes.getValue( "POSTALCODE" );
    			if( Utils.isBlankString( postalCode ) )
    			{
    				return;
    			}    		
    			
    			String aoGuid = attributes.getValue( "AOGUID" );
    			
    			List<AddressHouse> streetData = data.get( aoGuid );
				if( streetData != null )
				{
					System.out.println( "Found house int for AOGUID: " + aoGuid );
					
					int intStart = Utils.parseInt( attributes.getValue( "INTSTART" ), -1 );
					int intEnd = Utils.parseInt( attributes.getValue( "INTEND"  ), -1 );
					int intStatus = Utils.parseInt( attributes.getValue( "INTSTATUS" ), -1 );
					
					for( int i = 0; i < streetData.size(); i++ )
					{
						AddressHouse house = streetData.get( i );
						
						int houseNumber = house.getHouse();
						if( intStart <= houseNumber && houseNumber <= intEnd )
						{
    						if( intStatus == 3 ||
    							(intStatus == 1 && houseNumber % 2 == 1) ||
    							(intStatus == 2 && houseNumber % 2 == 0) )
    						{
    							streetData.remove( i );
    							
    							try
    							{
        							String query = "UPDATE address_house SET post_index=? WHERE id=?";
            						PreparedStatement ps = con.prepareStatement( query );
            						
            						ps.setString( 1, postalCode );
            						ps.setInt( 2, house.getId() );
            						ps.executeUpdate();
            						ps.close();        						
    							}
    							catch( SQLException e )
    							{
    								throw new SAXException( e );
    							}    							    							
    						}
						}
					}
					
					// все дома обработаны
					if( streetData.size() == 0 )
					{
						data.remove( aoGuid );
					}
				}
    		}
    	}
	}

	public static void main( String[] args )
	{
		new Test_Fias();
	}
}
