package ru.bgcrm.plugin.bgbilling.proto.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.util.Utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DeviceInfo
{
	protected static Logger log = Logger.getLogger( DeviceInfo.class );

	public DeviceInfo()
	{
	}

	public Set<BaseLink> getDeviceInfo( Integer contractId, Integer cityId )
	throws IOException, BGMessageException
	{
		if( cityId > 0 )
		{
			try
			{
				URL url = new URL( "http://its.core.ufanet.ru/api/erp_get_contract_links?" + "city_id=" + cityId + "&cid=" + contractId );
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.setDoOutput( true );
				connection.setDoInput( true );
				connection.setRequestMethod( "GET" );

				StringBuilder result = new StringBuilder();
				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream(), Utils.UTF8 ) );
				String line;
				while( (line = reader.readLine()) != null )
				{
					result.append( line )
						  .append( "\n" );
				}
				reader.close();
				connection.disconnect();

				return parse( result.toString() );
			}
			catch( Exception ex )
			{
				log.debug( ex.getMessage(), ex );
				return new HashSet<>(  );
			}
		}
		else
		{
			return new HashSet<>(  );
		}
	}

	private Set<BaseLink> parse( String jsonResponse )
	throws IOException
	{
		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj;

		Set<BaseLink> baseLinkSet = new HashSet<>();

		try
		{
			actualObj = mapper.readValue( jsonResponse, JsonNode.class );

			for( JsonNode node : actualObj.findValue( "contract_links" ) )
			{
				BaseLink link = new BaseLink();

				link.setCid( node.get( "cid" )
								 .asInt() );
				link.setObjectId( node.get( "oid" )
									  .asInt() );
				link.setModel( node.get( "network_object_model" )
								   .asText() );
				link.setFake( node.get( "fake" )
								  .asBoolean( false ) );
				link.setVlan( node.get( "vlan_string" )
								  .asText() );
				link.setPort( Utils.parseInt( node.get( "port_name" )
												  .asText()
												  .replace( "\"", "" ) ) );

				link.setHid( node.get( "network_object_location_house_id" )
								 .asInt() );
				link.setAddress( node.get( "network_object_location_string" )
									 .asText() );

				for( String item : node.get( "network_object_string" )
									   .asText()
									   .split( " | " ) )
				{
					if( Utils.notBlankString( item ) )
					{
						String[] keyValue = item.split( "->" );

						switch( keyValue[0] )
						{
							case "IP":
								link.setIp( keyValue[1] );
								break;
							case "MAC":
								link.setMac( keyValue[1] );
								break;
						}

					}
				}

				baseLinkSet.add( link );
			}
		}
		catch( Exception e )
		{
			return null;
		}

		return baseLinkSet;
	}

	public class BaseLink
	{
		private String model;
		private String vlan;
		private int cid;
		private int objectId;
		private int port;
		private String ip;
		private String mac;
		private boolean isFake;

		public String getAddress()
		{
			return address;
		}

		public void setAddress( String address )
		{
			this.address = address;
		}

		public int getHid()
		{
			return hid;
		}

		public void setHid( int hid )
		{
			this.hid = hid;
		}

		private String address;
		private int hid;

		public String getVlan()
		{
			return vlan;
		}

		public void setVlan( String vlan )
		{
			this.vlan = vlan;
		}

		public int getCid()
		{
			return cid;
		}

		public void setCid( int cid )
		{
			this.cid = cid;
		}

		public int getObjectId()
		{
			return objectId;
		}

		public void setObjectId( int objectId )
		{
			this.objectId = objectId;
		}

		public int getPort()
		{
			return port;
		}

		public void setPort( int port )
		{
			this.port = port;
		}

		public String getIp()
		{
			return ip;
		}

		public void setIp( String ip )
		{
			this.ip = ip;
		}

		public String getMac()
		{
			return mac;
		}

		public void setMac( String mac )
		{
			this.mac = mac;
		}

		public boolean isFake()
		{
			return isFake;
		}

		public void setFake( boolean isFake )
		{
			this.isFake = isFake;
		}

		public String getModel()
		{
			return model;
		}

		public void setModel( String model )
		{
			this.model = model;
		}
	}
}
