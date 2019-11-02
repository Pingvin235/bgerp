package ru.bgcrm.plugin.bgbilling.proto.model.balance;

import java.math.BigDecimal;

public class ContractAccount
{
	private String contract;
	private String month;
	private int serviceId;
	private BigDecimal sum;
	private String title;

	public String getMonth()
	{
		return month;
	}

	public void setMonth( String month )
	{
		this.month = month;
	}

	public int getServiceId()
	{
		return serviceId;
	}

	public void setServiceId( int serviceId )
	{
		this.serviceId = serviceId;
	}

	public BigDecimal getSum()
	{
		return sum;
	}

	public void setSum( BigDecimal sum )
	{
		this.sum = sum;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle( String title )
	{
		this.title = title;
	}

	public String getContract()
	{
		return contract;
	}

	public void setContract( String contract )
	{
		this.contract = contract;
	}
}
