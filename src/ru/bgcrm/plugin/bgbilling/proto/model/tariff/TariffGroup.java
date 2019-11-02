package ru.bgcrm.plugin.bgbilling.proto.model.tariff;

import java.util.List;

public class TariffGroup
{
	private int id;
	private String title;
	private int daysForward;
	private int position;
	private List<ContractTariff> tariffList;

	public int getId()
	{
		return id;
	}

	public void setId( int id )
	{
		this.id = id;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle( String title )
	{
		this.title = title;
	}

	public int getDaysForward()
	{
		return daysForward;
	}

	public void setDaysForward( int daysForward )
	{
		this.daysForward = daysForward;
	}

	public int getPosition()
	{
		return position;
	}

	public void setPosition( int position )
	{
		this.position = position;
	}

	public List<ContractTariff> getTariffList()
	{
		return tariffList;
	}

	public void setTariffList( List<ContractTariff> tariffList )
	{
		this.tariffList = tariffList;
	}
}
