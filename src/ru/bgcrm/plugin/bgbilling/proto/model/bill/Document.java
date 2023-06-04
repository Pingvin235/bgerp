package ru.bgcrm.plugin.bgbilling.proto.model.bill;

import java.math.BigDecimal;
import java.util.Date;

import org.bgerp.model.base.Id;

public class Document
	extends Id
{
	private String month;
	private String number;
	private Date createDate;
	private String createUser;
	private String typeTitle;
	private BigDecimal summa;

	public String getMonth()
	{
		return month;
	}

	public void setMonth( String month )
	{
		this.month = month;
	}

	public String getNumber()
	{
		return number;
	}

	public void setNumber( String number )
	{
		this.number = number;
	}

	public Date getCreateDate()
	{
		return createDate;
	}

	public void setCreateDate( Date createDate )
	{
		this.createDate = createDate;
	}

	public String getCreateUser()
	{
		return createUser;
	}

	public void setCreateUser( String createUser )
	{
		this.createUser = createUser;
	}

	public String getTypeTitle()
	{
		return typeTitle;
	}

	public void setTypeTitle( String typeTitle )
	{
		this.typeTitle = typeTitle;
	}

	public BigDecimal getSumma()
	{
		return summa;
	}

	public void setSumma( BigDecimal summa )
	{
		this.summa = summa;
	}
}