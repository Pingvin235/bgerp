package ru.bgcrm.plugin.bgbilling.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.bgcrm.model.ListItem;
import ru.bgcrm.model.SearchableIdTitle;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractParameter;

/**
 * Использовать {@link ru.bgcrm.plugin.bgbilling.proto.model.Contract}.
 */
@Deprecated
public class Contract
    extends SearchableIdTitle
    implements Comparable<Contract>
{
	//public final static String OBJECT_TYPE = "contract";

	private int mode = 0;
    private int face = 0;
    private boolean deleted = false; 
    private String billingId;
    private String comment;
    private String status;
    private String hierarchy;
    private Date fromDate;
    private Date toDate;
    private Date balanceDate;
    private BigDecimal balanceIn = BigDecimal.ZERO;
    private BigDecimal balancePayment = BigDecimal.ZERO;
    private BigDecimal balanceAccount = BigDecimal.ZERO;
    private BigDecimal balanceCharge = BigDecimal.ZERO;
    private BigDecimal balanceOut = BigDecimal.ZERO;
    private BigDecimal balanceLimit = BigDecimal.ZERO;
    private List<ListItem> tariffList = null;
    private List<ListItem> groupList = null;
    private List<ContractParameter> parameterList = null;
    private List<Contract> subContractList = null;
    private Map<String, Object> attributeMap = new HashMap<String, Object>();

    public String getBillingId()
    {
        return billingId;
    }

    public void setBillingId( String billingId )
    {
        this.billingId = billingId;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment( String comment )
    {
        this.comment = comment;
    }

    public Date getBalanceDate()
    {
        return balanceDate;
    }

    public void setBalanceDate( Date balanceDate )
    {
        this.balanceDate = balanceDate;
    }

    public BigDecimal getBalanceIn()
    {
        return balanceIn;
    }

    public void setBalanceIn( BigDecimal balanceIn )
    {
        this.balanceIn = balanceIn;
    }

    public BigDecimal getBalancePayment()
    {
        return balancePayment;
    }

    public void setBalancePayment( BigDecimal balancePayment )
    {
        this.balancePayment = balancePayment;
    }

    public BigDecimal getBalanceAccount()
    {
        return balanceAccount;
    }

    public void setBalanceAccount( BigDecimal balanceAccount )
    {
        this.balanceAccount = balanceAccount;
    }

    public BigDecimal getBalanceCharge()
    {
        return balanceCharge;
    }

    public void setBalanceCharge( BigDecimal balanceCharge )
    {
        this.balanceCharge = balanceCharge;
    }

    public BigDecimal getBalanceOut()
    {
        return balanceOut;
    }

    public void setBalanceOut( BigDecimal balanceOut )
    {
        this.balanceOut = balanceOut;
    }

    public BigDecimal getBalanceLimit()
    {
        return balanceLimit;
    }

    public void setBalanceLimit( BigDecimal balanceLimit )
    {
        this.balanceLimit = balanceLimit;
    }

    public int getMode()
    {
        return mode;
    }

    public void setMode( int mode )
    {
        this.mode = mode;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus( String status )
    {
        this.status = status;
    }

    public int getFace()
    {
        return face;
    }

    public void setFace( int face )
    {
        this.face = face;
    }

    public boolean isDeleted()
    {
        return deleted;
    }

    public void setDeleted( boolean deleted )
    {
        this.deleted = deleted;
    }

    public String getHierarchy()
    {
        return hierarchy;
    }

    public void setHierarchy( String hierarchy )
    {
        this.hierarchy = hierarchy;
    }

    public Date getFromDate()
    {
        return fromDate;
    }

    public void setFromDate( Date fromDate )
    {
        this.fromDate = fromDate;
    }

    public Date getToDate()
    {
        return toDate;
    }

    public void setToDate( Date toDate )
    {
        this.toDate = toDate;
    }

    public List<ListItem> getTariffList()
    {
        return tariffList;
    }

    public void setTariffList( List<ListItem> tariffList )
    {
        this.tariffList = tariffList;
    }

    public List<ListItem> getGroupList()
    {
        return groupList;
    }

    public void setGroupList( List<ListItem> groupList )
    {
        this.groupList = groupList;
    }

    public List<ContractParameter> getParameterList()
    {
        return parameterList;
    }

    public void setParameterList( List<ContractParameter> parameterList )
    {
        this.parameterList = parameterList;
    }
    
    public List<Contract> getSubContractList()
    {
    	return subContractList;
    }

	public void setSubContractList( List<Contract> subContractList )
    {
    	this.subContractList = subContractList;
    }

	public Map<String, Object> getAttributeMap()
    {
    	return attributeMap;
    }

	public void setAttributeMap( Map<String, Object> attributeMap )
    {
    	this.attributeMap = attributeMap;
    }

	@Override
    public int compareTo( Contract o )
    {
        return o.getTitle().compareTo( title );
    }

    @Override
    public boolean equals( Object obj )
    {
        Contract contract = (Contract)obj;
        //
        return 
            contract.getBillingId().equals( billingId ) && 
            contract.getId() == id;
    }
    
    @Override
    public int hashCode()
    {
        return id;
    }
}