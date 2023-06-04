package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.model.base.IdTitle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.DBInfoManager;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.proto.model.ipn.ContractGate;
import ru.bgcrm.plugin.bgbilling.proto.model.ipn.ContractGateInfo;
import ru.bgcrm.plugin.bgbilling.proto.model.ipn.ContractGateLogItem;
import ru.bgcrm.plugin.bgbilling.proto.model.ipn.IpnGate;
import ru.bgcrm.plugin.bgbilling.proto.model.ipn.IpnRange;
import ru.bgcrm.plugin.bgbilling.proto.model.ipn.IpnSource;
import ru.bgcrm.plugin.bgbilling.proto.model.ipn.ResourceCategory;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;
import ru.bgcrm.util.inet.IPUtils;

public class IpnDAO
    extends BillingModuleDAO
{
    private static final String IPN_MODULE_ID = "ipn";

    public IpnDAO( User user, String billingId, int moduleId )
        throws BGException
    {
        super( user, billingId, moduleId );
    }

    @Deprecated
    public IpnDAO( User user, String billingId )
        throws BGException
    {
        super( user, billingId, -1 );
        DBInfo dbinfo = DBInfoManager.getDbInfo( billingId );
        moduleId = dbinfo.getSetup().getInt( "module.ipn.id", -1 );
    }

    public int getGateStatus( int contractId )
        throws BGException
    {
        int gateStatus = -1;

        Request req = new Request();
        req.setModule( IPN_MODULE_ID );
        req.setAction( "GateContractInfo" );
        req.setModuleID( String.valueOf( moduleId ) );
        req.setContractId( contractId );

        Document document = null;
        try
        {
            document = transferData.postData( req, user );
        }
        catch( Exception e )
        {
            throw new BGException( e.getMessage() );
        }

        if( document != null )
        {

            Element dataElement = document.getDocumentElement();
            NodeList nodeList = dataElement.getElementsByTagName( "gate" );
            if( nodeList.getLength() > 0 )
            {
                Element gateEl = (Element)nodeList.item( 0 );
                gateStatus = Utils.parseInt( gateEl.getAttribute( "status" ) );
            }
        }
        return gateStatus;
    }

    public List<IpnRange> getFullIpnRanges( int contractId )
        throws Exception
    {
        return getIpnRanges( contractId, null, false );
    }

    /**
     *
     * @param contractId
     * @return
     * @throws Exception
     */
    public List<IpnRange> getIpnRanges( int contractId, Date date, boolean net )
        throws BGException
    {
        List<IpnRange> rangeList = new ArrayList<IpnRange>();

        Request req = new Request();
        req.setModule( IPN_MODULE_ID );
        req.setAction( "AddressContractInfo" );
        req.setModuleID( String.valueOf( moduleId ) );
        req.setContractId( contractId );
        req.setAttribute( "object_id", 0 );
        if( net )
        {
            req.setAttribute( "net", net );
        }
        if( date != null )
        {
            req.setAttribute( "date",
                              TimeUtils.format( date,
                                                TimeUtils.PATTERN_DDMMYYYY ) );
        }

        Document document = transferData.postData( req, user );
        for( Element rowElement : XMLUtils.selectElements( document,
                                                           "/data/table/data/row" ) )
        {
            IpnRange ipnRange = new IpnRange();

            ipnRange.setId( Utils.parseInt( rowElement.getAttribute( "id" ) ) );
            ipnRange.setAddressRange( rowElement.getAttribute( "address_range" ) );
            ipnRange.setIfaces( rowElement.getAttribute( "ifaces" ) );
            TimeUtils.parsePeriod( rowElement.getAttribute( "period" ),
                                   ipnRange );
            ipnRange.setComment( rowElement.getAttribute( "comment" ) );

            if( net )
            {
                ipnRange.setAddressFrom( StringUtils.substringBefore( ipnRange.getAddressRange(),
                                                                      "/" )
                                                    .trim() );
                ipnRange.setMask( Utils.parseInt( StringUtils.substringAfter( ipnRange.getAddressRange(),
                                                                              "/" )
                                                             .trim() ) );
            }
            else
            {
                ipnRange.setAddressFrom( StringUtils.substringBefore( ipnRange.getAddressRange(),
                                                                      "-" )
                                                    .trim() );
                ipnRange.setAddressTo( StringUtils.substringAfter( ipnRange.getAddressRange(),
                                                                   "-" )
                                                  .trim() );
            }

            rangeList.add( ipnRange );
        }

        return rangeList;
    }

    public IpnRange getIpnRange( int contractId, int rangeId )
        throws BGException
    {
        IpnRange result = null;

        Request req = new Request();
        req.setModule( IPN_MODULE_ID );
        req.setAction( "GetContractAddress" );
        req.setModuleID( moduleId );
        req.setContractId( contractId );
        req.setAttribute( "id", rangeId );

        Document doc = transferData.postData( req, user );
        Element rangeEl = XMLUtils.selectElement( doc, "/data/address" );
        if( rangeEl != null )
        {
            result = new IpnRange();
            result.setDateFrom( TimeUtils.parse( rangeEl.getAttribute( "date1" ),
                                                 TimeUtils.PATTERN_DDMMYYYY ) );
            result.setDateTo( TimeUtils.parse( rangeEl.getAttribute( "date2" ),
                                               TimeUtils.PATTERN_DDMMYYYY ) );
            result.setComment( rangeEl.getAttribute( "comment" ) );
            result.setIfaceList( Utils.toList( rangeEl.getAttribute( "ifaces" ) ) );
            result.setAddressFrom( IPUtils.convertLongIpToString( Utils.parseLong( rangeEl.getAttribute( "addr1" ) ) ) );
            result.setAddressTo( IPUtils.convertLongIpToString( Utils.parseLong( rangeEl.getAttribute( "addr2" ) ) ) );
            result.setMask( Utils.parseInt( rangeEl.getAttribute( "mask" ) ) );
            result.setPlan( Utils.parseInt( rangeEl.getAttribute( "plan" ) ) );
            result.setObjectId( Utils.parseInt( rangeEl.getAttribute( "object_id" ) ) );
        }

        return result;
    }

    public void updateIpnRange( IpnRange range )
        throws BGException
    {
        Request req = new Request();
        req.setModule( IPN_MODULE_ID );
        req.setAction( "UpdateContractAddress" );
        req.setModuleID( moduleId );
        req.setContractId( range.getContractId() );
        req.setAttribute( "id",
                          range.getId() <= 0 ? "new"
                                            : String.valueOf( range.getId() ) );
        req.setAttribute( "addr1",
                          IPUtils.convertStringIPtoLong( range.getAddressFrom() ) );
        req.setAttribute( "addr2",
                          IPUtils.convertStringIPtoLong( range.getAddressTo() ) );
        req.setAttribute( "mask", range.getMask() );
        req.setAttribute( "date1",
                          TimeUtils.format( range.getDateFrom(),
                                            TimeUtils.PATTERN_DDMMYYYY ) );
        req.setAttribute( "date2",
                          TimeUtils.format( range.getDateTo(),
                                            TimeUtils.PATTERN_DDMMYYYY ) );
        req.setAttribute( "plan", range.getPlan() );
        req.setAttribute( "ifaces", Utils.toString( range.getIfaceList() ) );
        req.setAttribute( "comment", range.getComment() );

        transferData.postData( req, user );
    }

    public void deleteIpnRange( int contractId, int rangeId )
        throws BGException
    {
        Request req = new Request();
        req.setModule( IPN_MODULE_ID );
        req.setAction( "DeleteContractAddress" );
        req.setModuleID( moduleId );
        req.setContractId( contractId );
        req.setAttribute( "id", rangeId );

        transferData.postData( req, user );
    }

    public List<IpnSource> getSourceList( Date date )
        throws BGException
    {
        List<IpnSource> result = new ArrayList<IpnSource>();

        Request req = new Request();
        req.setModule( IPN_MODULE_ID );
        req.setAction( "IfaceTree" );
        req.setModuleID( moduleId );
        req.setAttribute( "date",
                          TimeUtils.format( date, TimeUtils.PATTERN_DDMMYYYY ) );

        Document doc = transferData.postData( req, user );

        for( Element sourceEl : XMLUtils.selectElements( doc, "/data/source" ) )
        {
            IpnSource source = new IpnSource( Utils.parseInt( sourceEl.getAttribute( "id" ) ),
                                              sourceEl.getAttribute( "title" ) );
            result.add( source );

            List<IdTitle> ifaceList = source.getIfaceList();
            for( Element ifaceEl : XMLUtils.selectElements( sourceEl, "iface" ) )
            {
                ifaceList.add( new IdTitle( Utils.parseInt( ifaceEl.getAttribute( "id" ) ),
                                            ifaceEl.getAttribute( "title" ) ) );
            }
        }

        return result;
    }

    public List<ResourceCategory> getCategoryList()
        throws BGException
    {
        List<ResourceCategory> result = new ArrayList<ResourceCategory>();

        Request req = new Request();
        req.setModule( "admin.resource" );
        req.setAction( "IPResourceCategoryTable" );
        req.setModuleID( moduleId );

        Document doc = transferData.postData( req, user );

        for( Element categoryEl : XMLUtils.selectElements( doc, "/data/item" ) )
        {
            ResourceCategory category = new ResourceCategory( Utils.parseInt( categoryEl.getAttribute( "id" ) ),
                                                              categoryEl.getAttribute( "title" ) );
            result.add( category );

            loadSubcategories( categoryEl, category );
        }

        return result;
    }

    private void loadSubcategories( Element categoryEl, ResourceCategory cat )
        throws BGException
    {
        for( Element subcatEl : XMLUtils.selectElements( categoryEl, "item" ) )
        {
            ResourceCategory subcat = new ResourceCategory( Utils.parseInt( subcatEl.getAttribute( "id" ) ),
                                                            subcatEl.getAttribute( "title" ) );
            cat.addSubcategory( subcat );

            loadSubcategories( subcatEl, subcat );
        }
    }

    public List<IdTitle> linkPlanList()
        throws BGException
    {
        List<IdTitle> result = new ArrayList<IdTitle>();

        Request req = new Request();
        req.setModule( IPN_MODULE_ID );
        req.setAction( "ServiceLinkPlanList" );
        req.setModuleID( moduleId );

        Document doc = transferData.postData( req, user );

        for( Element planEl : XMLUtils.selectElements( doc, "/data/list/item" ) )
        {
            result.add( new IdTitle( planEl ) );
        }

        return result;
    }

    public ContractGateInfo gateInfo( int contractId )
        throws BGException
    {
        ContractGateInfo result = new ContractGateInfo();

        Request req = new Request();
        req.setModule( IPN_MODULE_ID );
        req.setAction( "GateContractInfo" );
        req.setModuleID( moduleId );
        req.setContractId( contractId );

        Document doc = transferData.postData( req, user );

        result.setStatusId( Utils.parseInt( XMLUtils.selectText( doc,
                                                                 "/data/gate/@status" ) ) );
        for( Element gateEl : XMLUtils.selectElements( doc, "/data/gates/item" ) )
        {
            result.getGateList().add( new ContractGate( gateEl ) );
        }
        for( Element itemEl : XMLUtils.selectElements( doc,
                                                       "/data/table/data/row" ) )
        {
            result.getStatusLog().add( new ContractGateLogItem( itemEl ) );
        }

        return result;
    }

    public String getGateEditorClass( int gateTypeId )
        throws BGException
    {
        Request req = new Request();
        req.setModule( IPN_MODULE_ID );
        req.setAction( "GateTypeClasses" );
        req.setModuleID( moduleId );
        req.setAttribute( "gtid", gateTypeId );

        Document doc = transferData.postData( req, user );
        return XMLUtils.selectText( doc, "/data/classes/@user_rule" );
    }

    public void gateStatusUpdate( int contractId, int statusId )
        throws BGException
    {
        Request req = new Request();
        req.setModule( IPN_MODULE_ID );
        req.setAction( "SetContractStatus" );
        req.setModuleID( moduleId );
        req.setContractId( contractId );
        req.setAttribute( "status", statusId );

        transferData.postData( req, user );
    }

    public List<IdTitle> gateRuleTypeList( int gateTypeId )
        throws BGException
    {
        List<IdTitle> result = new ArrayList<IdTitle>();

        Request req = new Request();
        req.setModule( IPN_MODULE_ID );
        req.setAction( "RuleTypeList" );
        req.setModuleID( moduleId );
        req.setAttribute( "gtid", gateTypeId );

        Document doc = transferData.postData( req, user );
        for( Element itemEl : XMLUtils.selectElements( doc, "/data/list/item" ) )
        {
            result.add( new IdTitle( itemEl ) );
        }

        return result;
    }

    public Pair<Integer, String> getUserGateRule( int userGateId )
        throws BGException
    {
        Request req = new Request();
        req.setModule( IPN_MODULE_ID );
        req.setAction( "GetContractRule" );
        req.setModuleID( moduleId );
        req.setAttribute( "id", userGateId );

        int ruleTypeId = 0;
        String rule = "";

        Element gateEl = XMLUtils.selectElement( transferData.postData( req,
                                                                        user ),
                                                 "/data/rule" );
        if( gateEl != null )
        {
            ruleTypeId = Utils.parseInt( gateEl.getAttribute( "rtid" ) );
            rule = linesToString( gateEl );
        }

        return new Pair<Integer, String>( ruleTypeId, rule );
    }

    public String generateRule( int ruleTypeId,
                                int gateTypeId,
                                String addressList )
        throws BGException
    {
        Request req = new Request();
        req.setModule( IPN_MODULE_ID );
        req.setAction( "GenerateRule" );
        req.setModuleID( moduleId );
        req.setAttribute( "rtid", ruleTypeId );
        req.setAttribute( "gtid", gateTypeId );
        req.setAttribute( "address_list", addressList );

        Document doc = transferData.postData( req, user );

        Element ruleEl = XMLUtils.selectElement( doc, "/data/rule" );
        if( ruleEl != null )
        {
            return linesToString( ruleEl );
        }

        return "";
    }

    public void updateGateRule( int contractId,
                                int id,
                                int gateId,
                                int ruleTypeId,
                                String rule )
        throws BGException
    {
        Request req = new Request();
        req.setModule( IPN_MODULE_ID );
        req.setAction( "UpdateContractRule" );
        req.setModuleID( moduleId );
        req.setContractId( contractId );
        req.setAttribute( "id", id <= 0 ? "new" : String.valueOf( id ) );
        req.setAttribute( "fwid", gateId );
        req.setAttribute( "rtid", ruleTypeId );
        req.setAttribute( "rule", rule );

        transferData.postData( req, user );
    }

    public void deleteGateRule( int contractId, int id )
        throws BGException
    {
        Request req = new Request();
        req.setModule( IPN_MODULE_ID );
        req.setAction( "DeleteContractRule" );
        req.setModuleID( moduleId );
        req.setContractId( contractId );
        req.setAttribute( "id", id );

        transferData.postData( req, user );
    }

    public List<IpnGate> getGateList()
        throws BGException
    {
        List<IpnGate> result = new ArrayList<IpnGate>();

        Request req = new Request();
        req.setModule( IPN_MODULE_ID );
        req.setAction( "GateInfo" );
        req.setModuleID( moduleId );

        Document doc = transferData.postData( req, user );

        for( Element el : XMLUtils.selectElements( doc, "/data/table/data/row" ) )
        {
            result.add( new IpnGate( el ) );
        }

        return result;
    }

    public List<IpnRange> findAddress( Page page,
                                       long address,
                                       int mask,
                                       int port,
                                       Date dateFrom,
                                       Date dateTo,
                                       String comment )
        throws BGException
    {
        Request request = new Request();
        request.setModule( "ipn" );
        request.setAction( "FindAddress" );
        request.setModuleID( getModuleId() );

        request.setPage( page );

        request.setAttribute( "date1",
                              TimeUtils.format( dateFrom,
                                                TimeUtils.PATTERN_DDMMYYYY ) );
        request.setAttribute( "date2",
                              TimeUtils.format( dateTo,
                                                TimeUtils.PATTERN_DDMMYYYY ) );

        request.setAttribute( "addr", address );
        request.setAttribute( "mask", mask );
        request.setAttribute( "port", port );
        request.setAttribute( "comment", comment );

        Document document = transferData.postData( request, user );

        List<IpnRange> addressRecords = new ArrayList<IpnRange>( page.getPageSize() );

        for( Element record : XMLUtils.selectElements( document,
                                                       "/data/table/data/record" ) )
        {
            IpnRange ipnRange = new IpnRange();

            ipnRange.setContractId( Utils.parseInt( record.getAttribute( "cid" ) ) );
            ipnRange.setComment( record.getAttribute( "comment" ) );
            ipnRange.setContractTitle( record.getAttribute( "con_title" ) );
            ipnRange.setIfaces( record.getAttribute( "iface" ) );
            ipnRange.setAddressFrom( StringUtils.substringBefore( record.getAttribute( "ip_range" ),
                                                                  "-" ) );
            ipnRange.setAddressTo( StringUtils.substringAfter( record.getAttribute( "ip_range" ),
                                                               "-" ) );
            ipnRange.setDateFrom( StringUtils.substringBefore( record.getAttribute( "period" ),
                                                               "-" ) );
            ipnRange.setDateTo( StringUtils.substringAfter( record.getAttribute( "period" ),
                                                            "-" ) );
            ipnRange.setPortFrom( Utils.parseInt( StringUtils.substringBefore( record.getAttribute( "port_range" ),
                                                                               " - " ) ) );
            ipnRange.setPortTo( Utils.parseInt( StringUtils.substringAfter( record.getAttribute( "port_range" ),
                                                                            " - " ) ) );

            addressRecords.add( ipnRange );
        }

        return addressRecords;
    }
}
