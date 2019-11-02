package ru.bgcrm.plugin.bgbilling.proto.model.phone;

import java.util.Date;

import ru.bgcrm.model.PeriodSet;
import ru.bgcrm.util.TimeUtils;

/*
 * Класс для представления результатов поиска договора по номеру телефона
 */
public class ContractPhoneRecord
    implements PeriodSet
{
    private int contractId = 0;
    private String contractTitle = null;
    private String contractComment = null;
    private String phone = null;
    private Date dateFrom = null;
    private Date dateTo = null;
    private String comment = null;

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

    public String getContractComment()
    {
        return contractComment;
    }

    public void setContractComment( String contractComment )
    {
        this.contractComment = contractComment;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone( String phone )
    {
        this.phone = phone;
    }

    public Date getDateFrom()
    {
        return dateFrom;
    }

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

    public void setDateTo( String dateTo )
    {
        setDateTo( TimeUtils.parse( dateTo, TimeUtils.PATTERN_DDMMYYYY ) );
    }

    public void setDateTo( Date dateTo )
    {
        this.dateTo = dateTo;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment( String comment )
    {
        this.comment = comment;
    }
}
