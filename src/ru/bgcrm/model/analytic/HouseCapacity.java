package ru.bgcrm.model.analytic;

import java.util.Date;

public class HouseCapacity
{
    private int cityId = -1;
    private int count1 = 0;
    private int count2 = 0;
    private int available1 = 0;
    private int available2 = 0;
    private int connected1 = 0;
    private int connected2 = 0;
    private Date date1;
    private Date date2;
    private String cityTitle;

    public int getCityId()
    {
        return cityId;
    }

    public void setCityId( int cityId )
    {
        this.cityId = cityId;
    }

    public String getCityTitle()
    {
        return cityTitle;
    }

    public void setCityTitle( String cityTitle )
    {
        this.cityTitle = cityTitle;
    }

    public Date getDate1()
    {
        return date1;
    }

    public void setDate1( Date date1 )
    {
        this.date1 = date1;
    }

    public Date getDate2()
    {
        return date2;
    }

    public void setDate2( Date date2 )
    {
        this.date2 = date2;
    }

    public int getCount1()
    {
        return count1;
    }

    public void setCount1( int count1 )
    {
        this.count1 = count1;
    }

    public int getCount2()
    {
        return count2;
    }

    public void setCount2( int count2 )
    {
        this.count2 = count2;
    }

    public int getAvailable1()
    {
        return available1;
    }

    public void setAvailable1( int available1 )
    {
        this.available1 = available1;
    }

    public int getAvailable2()
    {
        return available2;
    }

    public void setAvailable2( int available2 )
    {
        this.available2 = available2;
    }

    public int getConnected1()
    {
        return connected1;
    }

    public void setConnected1( int connected1 )
    {
        this.connected1 = connected1;
    }

    public int getConnected2()
    {
        return connected2;
    }

    public void setConnected2( int connected2 )
    {
        this.connected2 = connected2;
    }
}
