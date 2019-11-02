package ru.bgcrm.model.param.address;

public class AddressHouse
	extends AddressBase
{
	private int areaId = -1;
	private int quarterId = -1;
	private int streetId = -1;
	private int house = -1;
	private String frac;
	private String postIndex;
	private String comment;
	private AddressItem addressArea;
	private AddressItem addressQuarter;
	private AddressItem addressStreet;

	public static final String OBJECT_TYPE = "address_house";

	public int getAreaId()
	{
		return areaId;
	}

	public void setAreaId( int areaId )
	{
		this.areaId = areaId;
	}

	public int getQuarterId()
	{
		return quarterId;
	}

	public void setQuarterId( int quarterId )
	{
		this.quarterId = quarterId;
	}

	public int getStreetId()
	{
		return streetId;
	}

	public void setStreetId( int streetId )
	{
		this.streetId = streetId;
	}

	public int getHouse()
	{
		return house;
	}

	public void setHouse( int house )
	{
		this.house = house;
	}

	public String getHouseAndFrac()
	{
		StringBuilder buf = new StringBuilder();
		if( house > 0 )
		{
			buf.append( house );
		}
		if( frac != null )
		{
			buf.append( frac );
		}
		return buf.toString();
	}

	public static AddressHouse extractHouseAndFrac( String house )
	{
		AddressHouse result = new AddressHouse();
		result.setHouseAndFrac( house );
		return result;
	}

	public void setHouseAndFrac( String house )
	{
		if( house != null )
		{
		    this.frac = "";
			StringBuilder str = new StringBuilder();
			for( int i = 0; i < house.length(); i++ )
			{
				if( Character.isDigit( house.charAt( i ) ) )
				{
					str.append( house.charAt( i ) );
				}
				else
				{
					this.frac = house.substring( i );
					break;
				}
			}
			if( str.length() > 0 )
			{
				this.house = Integer.parseInt( str.toString() );
			}
		}
		else
		{
			this.house = 0;
			this.frac = null;
		}
	}

	public String getFrac()
	{
		return frac;
	}

	public void setFrac( String frac )
	{
		this.frac = frac;
	}
	
	public String getPostIndex()
	{
		return postIndex;
	}

	public void setPostIndex( String postIndex )
	{
		this.postIndex = postIndex;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment( String comment )
	{
		this.comment = comment;
	}

	public AddressItem getAddressArea()
	{
		return addressArea;
	}

	public void setAddressArea( AddressItem addressArea )
	{
		this.addressArea = addressArea;
	}

	public AddressItem getAddressQuarter()
	{
		return addressQuarter;
	}

	public void setAddressQuarter( AddressItem addressQuarter )
	{
		this.addressQuarter = addressQuarter;
	}

	public AddressItem getAddressStreet()
	{
		return addressStreet;
	}

	public void setAddressStreet( AddressItem addressStreet )
	{
		this.addressStreet = addressStreet;
	}
}
