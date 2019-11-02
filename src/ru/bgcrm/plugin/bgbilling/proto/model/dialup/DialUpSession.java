package ru.bgcrm.plugin.bgbilling.proto.model.dialup;

import ru.bgcrm.model.BGMessageException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import java.net.InetAddress;
import java.util.Date;

public class DialUpSession
{
	private int id;
	private int radiusLogId;
	private int cid;
	private String contract;
	private String fromNumber;
	private String toNumber;
	private long bytesIn;
	private long bytesOut;
	private InetAddress ipAddress;
	private double sessionCost;
	private Date sessionStart;
	private Date sessionStop;
	private long sessionTimeInMillis;
	private boolean active;

	public DialUpSession()
	{

	}

	public int getRadiusLogId()
	{
		return radiusLogId;
	}

	public void setRadiusLogId( int radiusLogId )
	{
		this.radiusLogId = radiusLogId;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive( boolean active )
	{
		this.active = active;
	}

	public long getBytesIn()
	{
		return bytesIn;
	}

	public void setBytesIn( long bytesIn )
	{
		this.bytesIn = bytesIn;
	}

	public long getBytesOut()
	{
		return bytesOut;
	}

	public void setBytesOut( long bytesOut )
	{
		this.bytesOut = bytesOut;
	}

	public String getFromNumber()
	{
		return fromNumber;
	}

	public void setFromNumber( String fromNumber )
	{
		this.fromNumber = fromNumber;
	}

	public String getToNumber()
	{
		return toNumber;
	}

	public void setToNumber( String toNumber )
	{
		this.toNumber = toNumber;
	}

	public long getSessionTimeInMillis()
	{
		return sessionTimeInMillis;
	}

	public void setSessionTimeInMillis( long sessionTimeInMillis )
	{
		this.sessionTimeInMillis = sessionTimeInMillis;
	}

	public int getId()
	{
		return id;
	}

	public void setId( int id )
	{
		this.id = id;
	}

	public int getCid()
	{
		return cid;
	}

	public void setCid( int cid )
	{
		this.cid = cid;
	}

	public String getContract()
	{
		return contract;
	}

	public void setContract( String contract )
	{
		this.contract = contract;
	}

	public InetAddress getIpAddress()
	{
		return ipAddress;
	}

	public void setIpAddress( InetAddress ipAddress )
	{
		this.ipAddress = ipAddress;
	}

	public double getSessionCost()
	{
		return sessionCost;
	}

	public void setSessionCost( double sessionCost )
	{
		this.sessionCost = sessionCost;
	}

	public Date getSessionStart()
	{
		return sessionStart;
	}

	public void setSessionStart( Date sessionStart )
	{
		this.sessionStart = sessionStart;
	}

	public Date getSessionStop()
	{
		return sessionStop;
	}

	public void setSessionStop( Date date )
	{
		this.sessionStop = date;
	}

	public String getSessionTime()
	throws BGMessageException
	{
		try
		{
			Duration duration = DatatypeFactory.newInstance().newDuration( sessionTimeInMillis );

			return String.format( "%02d:%02d:%02d\n", duration.getDays() * 24 + duration.getHours(), duration.getMinutes(), duration.getSeconds() );
		}
		catch( DatatypeConfigurationException e )
		{
			throw new BGMessageException( e.getMessage() );
		}
	}

}
