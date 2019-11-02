package ru.bgcrm.plugin.bgbilling.proto.model.balance;

import java.math.BigDecimal;

public class ContractBalanceDetail
{
	private String date;
	private BigDecimal summa;
	private String type;
	private String comment;

	public String getDate()
	{
		return date;
	}

	public void setDate( String date )
	{
		this.date = date;
	}

	public BigDecimal getSumma()
	{
		return summa;
	}

	public void setSumma( BigDecimal summa )
	{
		this.summa = summa;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment( String comment )
	{
		this.comment = comment;
	}

	public String getType()
	{
		return type;
	}

	public void setType( String type )
	{
		this.type = type;
	}
}
