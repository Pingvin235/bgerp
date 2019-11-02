package ru.bgcrm.plugin.bgbilling.proto.model.inet;

import java.math.BigDecimal;

public class InetSessionLogAccount
{
	private long amount;
	private BigDecimal account;

	public InetSessionLogAccount()
	{}

	public InetSessionLogAccount( long amount, BigDecimal account )
	{
		this.amount = amount;
		this.account = account;
	}

	public long getAmount()
	{
		return amount;
	}

	public void setAmount( long amount )
	{
		this.amount = amount;
	}

	public BigDecimal getAccount()
	{
		return account;
	}

	public void setAccount( BigDecimal account )
	{
		this.account = account;
	}
}
