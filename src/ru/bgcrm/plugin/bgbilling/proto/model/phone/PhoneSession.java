package ru.bgcrm.plugin.bgbilling.proto.model.phone;

import java.math.BigDecimal;

public class PhoneSession
{
	private Integer cdrId;
	private String destinationTitle;
	private Integer destinationId;
	private String numberFrom;
	private String numberFromE164;
	private Integer id;
	private String pointTitle;
	private Integer pointId;
	private BigDecimal minuteCost;
	private String serviceTitle;
	private BigDecimal sessionCost;
	private String sessionStart;
	private String sessionTime;
	private String sessionRoundTime;
	private String numberTo;
	private String numberToE164;

	public Integer getCdrId()
	{
		return cdrId;
	}

	public void setCdrId( Integer cdrId )
	{
		this.cdrId = cdrId;
	}

	public String getDestinationTitle()
	{
		return destinationTitle;
	}

	public void setDestinationTitle( String destinationTitle )
	{
		this.destinationTitle = destinationTitle;
	}

	public Integer getDestinationId()
	{
		return destinationId;
	}

	public void setDestinationId( Integer destinationId )
	{
		this.destinationId = destinationId;
	}

	public String getNumberFrom()
	{
		return numberFrom;
	}

	public void setNumberFrom( String numberFrom )
	{
		this.numberFrom = numberFrom;
	}

	public String getNumberFromE164()
	{
		return numberFromE164;
	}

	public void setNumberFromE164( String numberFromE164 )
	{
		this.numberFromE164 = numberFromE164;
	}

	public Integer getId()
	{
		return id;
	}

	public void setId( Integer id )
	{
		this.id = id;
	}

	public String getPointTitle()
	{
		return pointTitle;
	}

	public void setPointTitle( String pointTitle )
	{
		this.pointTitle = pointTitle;
	}

	public Integer getPointId()
	{
		return pointId;
	}

	public void setPointId( Integer pointId )
	{
		this.pointId = pointId;
	}

	public BigDecimal getMinuteCost()
	{
		return minuteCost;
	}

	public void setMinuteCost( BigDecimal minuteCost )
	{
		this.minuteCost = minuteCost;
	}

	public String getServiceTitle()
	{
		return serviceTitle;
	}

	public void setServiceTitle( String serviceTitle )
	{
		this.serviceTitle = serviceTitle;
	}

	public BigDecimal getSessionCost()
	{
		return sessionCost;
	}

	public void setSessionCost( BigDecimal sessionCost )
	{
		this.sessionCost = sessionCost;
	}

	public String getSessionStart()
	{
		return sessionStart;
	}

	public void setSessionStart( String sessionStart )
	{
		this.sessionStart = sessionStart;
	}

	public String getSessionTime()
	{
		return sessionTime;
	}

	public void setSessionTime( String sessionTime )
	{
		this.sessionTime = sessionTime;
	}

	public String getSessionRoundTime()
	{
		return sessionRoundTime;
	}

	public void setSessionRoundTime( String sessionRoundTime )
	{
		this.sessionRoundTime = sessionRoundTime;
	}

	public String getNumberTo()
	{
		return numberTo;
	}

	public void setNumberTo( String numberTo )
	{
		this.numberTo = numberTo;
	}

	public String getNumberToE164()
	{
		return numberToE164;
	}

	public void setNumberToE164( String numberToE164 )
	{
		this.numberToE164 = numberToE164;
	}
}
