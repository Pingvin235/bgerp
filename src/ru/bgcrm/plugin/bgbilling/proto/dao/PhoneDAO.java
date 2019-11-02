package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.DBInfoManager;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.proto.model.phone.ContractPhoneRecord;
import ru.bgcrm.plugin.bgbilling.proto.model.phone.PhonePoint;
import ru.bgcrm.plugin.bgbilling.proto.model.phone.PhoneResourceItem;
import ru.bgcrm.plugin.bgbilling.proto.model.phone.PhoneSession;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

public class PhoneDAO
    extends BillingModuleDAO
{
    private static final String PHONE_MODULE_ID = "phone";

    public PhoneDAO( User user, String billingId, int moduleId )
        throws BGException
    {
        super( user, billingId, moduleId );
    }

    @Deprecated
    public PhoneDAO( User user, String billingId )
        throws BGException
    {
        super( user, billingId, -1 );

        DBInfo dbinfo = DBInfoManager.getDbInfo( billingId );
        moduleId = dbinfo.getSetup().getInt( "module.phone.id", -1 );
    }

    public List<PhonePoint> getPhonePoints( int contractId, int objectId )
        throws BGException
    {
        Request req = new Request();
        req.setModule( PHONE_MODULE_ID );
        req.setAction( "ClientItem" );
        req.setModuleID( String.valueOf( moduleId ) );
        req.setContractId( contractId );
        req.setAttribute( "object_id", objectId );

        Document document = transferData.postData( req, user );

        List<PhonePoint> phonePoints = new ArrayList<PhonePoint>();
        if( document != null )
        {
            Element dataElement = document.getDocumentElement();
            NodeList nodeList = dataElement.getElementsByTagName( "row" );

            for( int index = 0; index < nodeList.getLength(); index++ )
            {
                PhonePoint point = new PhonePoint();
                Element element = (Element)nodeList.item( index );
                point.setId( Utils.parseInt( element.getAttribute( "id" ) ) );
                point.setAlias( element.getAttribute( "alias" ) );
                point.setComment( element.getAttribute( "comment" ) );
                point.setDescription( element.getAttribute( "description" ) );
                point.setPeriod( element.getAttribute( "period" ) );

                try
                {
                    String[] fromToDate = point.getPeriod().split( "-" );
                    if( fromToDate.length > 0 )
                    {
                        String dateFrom = fromToDate[0];
                        if( Utils.notBlankString( dateFrom ) )
                        {
                            point.setDateFrom( DateUtils.parseDate( dateFrom,
                                                                    new String[] { "dd.MM.yyyy" } ) );
                        }
                    }
                    if( fromToDate.length > 1 )
                    {
                        String dateTo = fromToDate[1];
                        if( Utils.notBlankString( dateTo ) )
                        {
                            point.setDateTo( DateUtils.parseDate( dateTo,
                                                                  new String[] { "dd.MM.yyyy" } ) );
                        }
                    }
                }
                catch( ParseException e )
                {
                    throw new BGException( e );
                }

                point.setSourceId( Utils.parseInt( element.getAttribute( "sourceId" ) ) );
                point.setType( Utils.parseInt( element.getAttribute( "type" ) ) );
                point.setClientNumbers( element.getAttribute( "clientNumbers" ) );

                phonePoints.add( point );
            }
        }

        return phonePoints;
    }

    public PhonePoint getPoint( int pointId )
        throws BGException
    {
        PhonePoint point = new PhonePoint();
        try
        {
            // получение алиаса
            Request req = new Request();
            req.setModule( PHONE_MODULE_ID );
            req.setAction( "ClientItemGet" );
            req.setModuleID( String.valueOf( moduleId ) );
            req.setAttribute( "id", pointId );
            Document document = transferData.postData( req, user );

            if( document != null )
            {
                Element itemData = document.getDocumentElement();
                NodeList dataNodeList = itemData.getElementsByTagName( "client_item" );

                if( dataNodeList.getLength() > 0 )
                {
                    Element element = (Element)dataNodeList.item( 0 );
                    point.setId( Utils.parseInt( element.getAttribute( "id" ) ) );
                    point.setAlias( element.getAttribute( "alias" ) );
                    point.setComment( element.getAttribute( "comment" ) );
                    point.setObjectId( Utils.parseInt( element.getAttribute( "object_id" ) ) );

                    String dateFrom = element.getAttribute( "date1" );
                    if( Utils.notBlankString( dateFrom ) )
                    {
                        point.setDateFrom( DateUtils.parseDate( dateFrom,
                                                                new String[] { "dd.MM.yyyy" } ) );
                    }
                    String dateTo = element.getAttribute( "date2" );
                    if( Utils.notBlankString( dateTo ) )
                    {
                        point.setDateTo( DateUtils.parseDate( dateTo,
                                                              new String[] { "dd.MM.yyyy" } ) );
                    }
                    point.setPeriod( element.getAttribute( "period" ) );
                    point.setSourceId( Utils.parseInt( element.getAttribute( "sourceId" ) ) );
                    point.setType( Utils.parseInt( element.getAttribute( "type" ) ) );
                    point.setClientNumbers( element.getAttribute( "clientNumbers" ) );
                }
            }
        }
        catch( ParseException e )
        {
            throw new BGException( e );
        }
        return point;
    }

    // http://192.168.169.8:8080/bgbilling/executer?on_date=21.10.2014&from_number=&module=admin.resource&status=free&to_number=&action=NumberResourceTable&category_id=0&mid=33&user=car&pswd=12345car
    public List<PhoneResourceItem> getPhoneResourceTable( int categoryId,
                                                          String fromNumber,
                                                          String toNumber,
                                                          String status,
                                                          Date onDate )
        throws BGException
    {
        Request req = new Request();
        req.setModule( "admin.resource" );
        req.setModuleID( moduleId );
        req.setAction( "NumberResourceTable" );
        req.setAttribute( "category_id", categoryId );
        req.setAttribute( "from_number", fromNumber );
        req.setAttribute( "to_number", toNumber );
        req.setAttribute( "on_date", TimeUtils.format( onDate, "dd.MM.yyyy" ) );
        req.setAttribute( "status", status );

        Document doc = transferData.postData( req, user );
        NodeList nodeList = XMLUtils.selectNodeList( doc,
                                                     "/data/table/data/row" );

        List<PhoneResourceItem> result = new ArrayList<>();

        for( int i = 0; i < nodeList.getLength(); i++ )
        {
            Element element = (Element)nodeList.item( i );
            PhoneResourceItem resourceItem = new PhoneResourceItem();
            resourceItem.setId( Utils.parseInt( element.getAttribute( "id" ) ) );
            resourceItem.setTitle( element.getAttribute( "title" ) );
            resourceItem.setNumber( element.getAttribute( "number" ) );
            resourceItem.setComment( element.getAttribute( "comment" ) );
            resourceItem.setPeriod( element.getAttribute( "period " ) );
            resourceItem.setReserveComment( element.getAttribute( "reserveComment" ) );
            resourceItem.setDateReserve( TimeUtils.parse( element.getAttribute( "dateReserve" ),
                                                          TimeUtils.FORMAT_TYPE_YMD ) );
            result.add( resourceItem );
        }
        return result;
    }

    public List<String> getFreeNumberResourceList( int categoryId, Date date )
        throws BGException
    {
        Request req = new Request();
        req.setModule( "admin.resource" );
        req.setModuleID( moduleId );
        req.setAction( "FreeNumberResourceList" );
        req.setAttribute( "category_id", categoryId );
        req.setAttribute( "from_number", "" );
        req.setAttribute( "to_number", "" );
        req.setAttribute( "date", TimeUtils.format( date, "dd.MM.yyyy" ) );

        List<String> phoneList = new ArrayList<String>();
        Document doc = transferData.postData( req, user );
        NodeList nodeList = XMLUtils.selectNodeList( doc, "/data/list/item" );

        for( int index = 0; index < nodeList.getLength(); index++ )
        {
            Element element = (Element)nodeList.item( index );
            phoneList.add( element.getAttribute( "title" ) );
        }

        return phoneList;
    }

    public void updateClientItem( int contractId,
                                  String clientNumbers,
                                  String alias,
                                  int sourceId,
                                  Date date1,
                                  Date date2,
                                  int id,
                                  String comment,
                                  int objectId,
                                  int type )
        throws BGException
    {
        Request req = new Request();
        req.setModule( "phone" );
        req.setModuleID( moduleId );
        req.setAction( "ClientItemUpdate" );
        req.setContractId( contractId );
        req.setAttribute( "clientNumbers", clientNumbers );
        req.setAttribute( "alias", alias );
        req.setAttribute( "date1", TimeUtils.format( date1, "dd.MM.yyyy" ) );
        req.setAttribute( "date2", TimeUtils.format( date2, "dd.MM.yyyy" ) );
        req.setAttribute( "sourceId", sourceId );

        req.setAttribute( "id", id );
        req.setAttribute( "object_id", objectId );
        req.setAttribute( "type", type );

        transferData.postData( req, user );
    }

    public List<PhoneSession> getPhoneSessionList( int contractId,
                                                   int pointId,
                                                   int days )
        throws BGException
    {
        Calendar curdate = new GregorianCalendar();
        Calendar dateFrom = new GregorianCalendar();
        dateFrom.add( Calendar.DAY_OF_YEAR, -days );

        List<PhoneSession> sessionList = new ArrayList<PhoneSession>();

        while( TimeUtils.dateBeforeOrEq( dateFrom, curdate ) )
        {
            Request req = new Request();
            req.setModule( PHONE_MODULE_ID );
            req.setAction( "ReportSession" );
            req.setModuleID( String.valueOf( moduleId ) );
            req.setContractId( contractId );
            req.setPageSize( 999 );
            req.setAttribute( "date1", TimeUtils.format( dateFrom.getTime(),
                                                         "dd.MM.yyyy" ) );
            req.setAttribute( "date2", TimeUtils.format( dateFrom.getTime(),
                                                         "31.MM.yyyy" ) );
            req.setAttribute( "items", pointId );

            Document document = transferData.postData( req, user );

            if( document != null )
            {
                Element dataElement = document.getDocumentElement();
                NodeList nodeList = dataElement.getElementsByTagName( "row" );

                for( int index = 0; index < nodeList.getLength(); index++ )
                {
                    PhoneSession session = new PhoneSession();
                    Element element = (Element)nodeList.item( index );
                    session.setId( Utils.parseInt( element.getAttribute( "id" ) ) );
                    session.setCdrId( Utils.parseInt( element.getAttribute( "cdr_id" ) ) );
                    session.setDestinationId( Utils.parseInt( element.getAttribute( "dest_id" ) ) );
                    session.setDestinationTitle( element.getAttribute( "dest" ) );
                    session.setMinuteCost( Utils.parseBigDecimal( element.getAttribute( "min_cost" ) ) );
                    session.setNumberFrom( element.getAttribute( "from" ) );
                    session.setNumberFromE164( element.getAttribute( "from164" ) );
                    session.setNumberTo( element.getAttribute( "to" ) );
                    session.setNumberToE164( element.getAttribute( "to164" ) );
                    session.setPointId( Utils.parseInt( element.getAttribute( "item_id" ) ) );
                    session.setPointTitle( element.getAttribute( "item" ) );
                    session.setServiceTitle( element.getAttribute( "service" ) );
                    session.setSessionCost( Utils.parseBigDecimal( element.getAttribute( "session_cost" ) ) );
                    session.setSessionRoundTime( element.getAttribute( "session_time_round" ) );
                    session.setSessionStart( element.getAttribute( "session_start" ) );
                    session.setSessionTime( element.getAttribute( "session_time" ) );

                    sessionList.add( session );
                }
            }

            dateFrom.set( Calendar.DATE, 1 );
            dateFrom.add( Calendar.MONTH, 1 );
        }

        return sessionList;
    }

    public static enum FindPhoneMode
    {
        EQUAL( "equal" ),
        START_WITH( "start" ),
        END_WITH( "end" ),
        INCLUDE( "include" );

        private String mode = null;

        private void setMode( String mode )
        {
            this.mode = mode;
        }

        private String getMode()
        {
            return this.mode;
        }

        private FindPhoneMode( String mode )
        {
            setMode( mode );
        }

        public String toString()
        {
            return getMode();
        }

        public static FindPhoneMode fromString( String mode )
        {
            for( FindPhoneMode findPhoneMode : FindPhoneMode.values() )
            {
                if( findPhoneMode.toString().equals( mode ) )
                {
                    return findPhoneMode;
                }
            }

            return null;
        }
    }

    public static enum FindPhoneSortMode
    {
        BY_NONE( 0 ),
        BY_CONTRACT_TITLE( 1 ),
        BY_PHONE_NUMBER( 2 ),
        BY_PERIOD( 3 );

        private int mode = 0;

        private void setMode( int mode )
        {
            this.mode = mode;
        }

        private int getMode()
        {
            return this.mode;
        }

        private FindPhoneSortMode( int mode )
        {
            setMode( mode );
        }

        public String toString()
        {
            return String.valueOf( getMode() );
        }

        public static FindPhoneSortMode fromString( String mode )
        {
            for( FindPhoneSortMode findPhoneSortMode : FindPhoneSortMode.values() )
            {
                if( findPhoneSortMode.toString().equals( mode ) )
                {
                    return findPhoneSortMode;
                }
            }

            return null;
        }
    }

    public void findPhone( SearchResult<ContractPhoneRecord> searchResult,
                           FindPhoneMode mode,
                           FindPhoneSortMode sort,
                           String phone,
                           Date dateFrom,
                           Date dateTo )
        throws BGException
    {
        Request request = new Request();
        request.setModule( PHONE_MODULE_ID );
        request.setAction( "FindPhone" );
        request.setModuleID( getModuleId() );

        request.setPage( searchResult.getPage() );

        request.setAttribute( "mode", mode.toString() );
        request.setAttribute( "sort", sort.toString() );
        request.setAttribute( "view", 1 );
        request.setAttribute( "phone", phone );
        request.setAttribute( "date1",
                              TimeUtils.format( dateFrom,
                                                TimeUtils.PATTERN_DDMMYYYY ) );
        request.setAttribute( "date2",
                              TimeUtils.format( dateTo,
                                                TimeUtils.PATTERN_DDMMYYYY ) );

        Document document = transferData.postData( request, user );

        for( Element row : XMLUtils.selectElements( document,
                                                    "/data/table/data/row" ) )
        {
            ContractPhoneRecord contractPhoneRecord = new ContractPhoneRecord();
            contractPhoneRecord.setContractId( Utils.parseInt( row.getAttribute( "f0" ) ) );
            contractPhoneRecord.setContractTitle( row.getAttribute( "f1" ) );
            contractPhoneRecord.setContractComment( row.getAttribute( "f2" ) );
            contractPhoneRecord.setPhone( row.getAttribute( "f3" ) );
            contractPhoneRecord.setDateFrom( StringUtils.substringBefore( row.getAttribute( "f4" ),
                                                                          "-" ) );
            contractPhoneRecord.setDateTo( StringUtils.substringAfter( row.getAttribute( "f4" ),
                                                                       "-" ) );
            contractPhoneRecord.setComment( row.getAttribute( "f5" ) );

            searchResult.getList().add( contractPhoneRecord );
        }
    }
}
