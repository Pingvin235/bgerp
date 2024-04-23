package ru.bgcrm.plugin.bgbilling.proto.model.ipn;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bgerp.model.base.Id;

import ru.bgcrm.model.PeriodSet;
import ru.bgcrm.util.TimeUtils;

public class IpnRange
    extends Id
    implements PeriodSet
{
    private int contractId;
    private String contractTitle;
    private Date dateFrom;
    private Date dateTo;
    private String addressRange;
    private String addressFrom;
    private String addressTo;
    private int mask;
    private int portFrom;
    private int portTo;
    private int plan;
    private String ifaces;
    private List<String> ifaceList = new ArrayList<>();
    private String comment;
    private int objectId;

    public int getContractId()
    {
        return contractId;
    }

    public void setContractId( int contractId )
    {
        this.contractId = contractId;
    }

    public String getContractTitle()
    {
        return contractTitle;
    }

    public void setContractTitle( String contractTitle )
    {
        this.contractTitle = contractTitle;
    }

    public Date getDateFrom()
    {
        return dateFrom;
    }

    @Override
    public void setDateFrom( Date dateFrom )
    {
        this.dateFrom = dateFrom;
    }

    public void setDateFrom( String dateFrom )
    {
        setDateFrom( TimeUtils.parse( dateFrom, TimeUtils.PATTERN_DDMMYYYY ) );
    }

    public Date getDateTo()
    {
        return dateTo;
    }

    @Override
    public void setDateTo( Date dateTo )
    {
        this.dateTo = dateTo;
    }

    public void setDateTo( String dateTo )
    {
        setDateTo( TimeUtils.parse( dateTo, TimeUtils.PATTERN_DDMMYYYY ) );
    }

    @Deprecated
    public String getPeriod()
    {
        return TimeUtils.formatPeriod( dateFrom, dateTo, TimeUtils.FORMAT_TYPE_YMD );
    }

    public String getAddressRange()
    {
        return addressRange;
    }

    public void setAddressRange( String addressRange )
    {
        this.addressRange = addressRange;
    }

    public String getAddressFrom()
    {
        return addressFrom;
    }

    public void setAddressFrom( String addressFrom )
    {
        this.addressFrom = addressFrom;
    }

    public String getAddressTo()
    {
        return addressTo;
    }

    public void setAddressTo( String addressTo )
    {
        this.addressTo = addressTo;
    }

    public int getMask()
    {
        return mask;
    }

    public void setMask( int mask )
    {
        this.mask = mask;
    }

    public int getPortFrom()
    {
        return portFrom;
    }

    public void setPortFrom( int portFrom )
    {
        this.portFrom = portFrom;
    }

    public int getPortTo()
    {
        return portTo;
    }

    public void setPortTo( int portTo )
    {
        this.portTo = portTo;
    }

    public int getPlan()
    {
        return plan;
    }

    public void setPlan( int plan )
    {
        this.plan = plan;
    }

    public String getIfaces()
    {
        return ifaces;
    }

    public void setIfaces( String ifaces )
    {
        this.ifaces = ifaces;
    }

    public List<String> getIfaceList()
    {
        return ifaceList;
    }

    public void setIfaceList( List<String> ifaceList )
    {
        this.ifaceList = ifaceList;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment( String comment )
    {
        this.comment = comment;
    }

    public int getObjectId()
    {
        return objectId;
    }

    public void setObjectId( int objectId )
    {
        this.objectId = objectId;
    }
}
