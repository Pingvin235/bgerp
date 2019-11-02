package ru.bgcrm.plugin.bgbilling.proto.model.tariff;

import java.math.BigDecimal;
import java.util.Date;

import ru.bgcrm.model.Id;

public class ContractTariffOption
	extends Id
{
	private int optionId;
	private String optionTitle;
	private BigDecimal summa;
	private int chargeId;
	private int contractId;
	private int activateMode;
	private Date activatedTime;
	private Date timeFrom;
	private Date timeTo;
	private int userId;
	private String userTitle;

	public int getOptionId()
	{
		return optionId;
	}

	public void setOptionId( int optionId )
	{
		this.optionId = optionId;
	}

	public String getOptionTitle()
	{
		return optionTitle;
	}

	public void setOptionTitle( String optionTitle )
	{
		this.optionTitle = optionTitle;
	}

	public BigDecimal getSumma()
	{
		return summa;
	}

	public void setSumma( BigDecimal summa )
	{
		this.summa = summa;
	}

	public int getChargeId()
	{
		return chargeId;
	}

	public void setChargeId( int chargeId )
	{
		this.chargeId = chargeId;
	}

	public int getContractId()
	{
		return contractId;
	}

	public void setContractId( int contractId )
	{
		this.contractId = contractId;
	}

	public int getActivateMode()
	{
		return activateMode;
	}

	public void setActivateMode( int activateMode )
	{
		this.activateMode = activateMode;
	}

	public Date getActivatedTime()
	{
		return activatedTime;
	}

	public void setActivatedTime( Date activatedTime )
	{
		this.activatedTime = activatedTime;
	}

	public Date getTimeFrom()
	{
		return timeFrom;
	}

	public void setTimeFrom( Date timeFrom )
	{
		this.timeFrom = timeFrom;
	}

	public Date getTimeTo()
	{
		return timeTo;
	}

	public void setTimeTo( Date timeTo )
	{
		this.timeTo = timeTo;
	}

	public int getUserId()
	{
		return userId;
	}

	public void setUserId( int userId )
	{
		this.userId = userId;
	}

	public String getUserTitle()
	{
		return userTitle;
	}

	public void setUserTitle( String userTitle )
	{
		this.userTitle = userTitle;
	}
}