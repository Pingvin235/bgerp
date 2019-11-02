package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractService;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

public class ContractServiceDAO
	extends BillingDAO
{
	private static final String CONTRACT_MODULE_ID = "contract";
	
	public ContractServiceDAO( User user, DBInfo dbInfo )
		throws BGException
	{
		super( user, dbInfo );
	}

	public ContractServiceDAO( User user, String billingId )
		throws BGException
	{
		super( user, billingId );
	}
	
	/*http://127.0.0.1:8080/bgbilling/executer?module=contract&action=ContractServices&mid=33&BGBillingSecret=4qjd9t9DooDWkbJnjWOvYnlP&cid=455&
	[ length = 362 ] xml = <?xml version="1.0" encoding="windows-1251"?><data secret="B8174817BC7BF5EB1802AF3DAAEC2CD3" status="ok"><table><data>
	<row f0="487978" f1="Максимальный IPN" f2="13.01.2009-01.11.2009" f3="sdret" f4="74"/>
	<row f0="488254" f1="Внешний вх." f2="14.05.2010-…" f3="" f4="38"/><row f0="488255" f1="Внешний исх." f2="14.05.2010-…" f3="" f4="146"/></data></table></data>*/
	public List<ContractService> getContractServiceList( int contractId, int moduleId )
		throws BGException
	{
		List<ContractService> result = new ArrayList<ContractService>();
		
		Request req = new Request();

		req.setModule( CONTRACT_MODULE_ID );
		req.setAction( "ContractServices" );
		req.setModuleID( moduleId );
		req.setContractId( contractId );

		Document doc = transferData.postData( req, user );
		for( Element el : XMLUtils.selectElements( doc, "/data/table/data/row" ) )
		{
			ru.bgcrm.plugin.bgbilling.proto.model.ContractService service = new ru.bgcrm.plugin.bgbilling.proto.model.ContractService();
			
			service.setId( Utils.parseInt( el.getAttribute( "f0" ) ) );
			service.setServiceTitle( el.getAttribute( "f1" ) );
			service.setServiceId( Utils.parseInt( el.getAttribute( "f4" ) ) );
			TimeUtils.parsePeriod( el.getAttribute( "f2" ), service );
			service.setComment( el.getAttribute( "f3" ) );
			
			result.add( service );
		}
		
		return result;
	}
	
	/*http://127.0.0.1:8080/bgbilling/executer?id=487978&onlyUsing=1&module=contract&action=ContractService&mid=33&BGBillingSecret=vQzeQkX69yctfj0M48E0jb0R&
	[ length = 538 ] xml = <?xml version="1.0" encoding="windows-1251"?><data secret="AB685F93A5DE167303ACD8EBBC03B868" status="ok">
	<service comment="sdret" date1="13.01.2009" date2="01.11.2009" sid="74"/>
	<tree><module id="33" title="IPN"><service id="38" mid="33" title="Внешний вх."/><service id="146" mid="33" title="Внешний исх."/>
	<service id="37" mid="33" title="Исходящий внутренний"/>
	<service id="40" mid="33" title="Локальный вх."/><service id="39" mid="33" title="Локальный исх."/><service id="74" mid="33" title="Максимальный IPN"/></module></tree></data>*/
	public Pair<ContractService, List<IdTitle>> getContractService( int contractId, int moduleId, int id, boolean onlyUsing )
    	throws BGException
    {
		ContractService service = null;
		List<IdTitle> serviceList = new ArrayList<IdTitle>();
		
		Request req = new Request();

		req.setModule( CONTRACT_MODULE_ID );
		req.setAction( "ContractService" );
		req.setModuleID( moduleId );
		req.setContractId( contractId );
		req.setAttribute( "id", id );
		req.setAttribute( "onlyUsing", onlyUsing );
		
		Document doc = transferData.postData( req, user );
		
		Element serviceEl = XMLUtils.selectElement( doc, "/data/service" );
		if( serviceEl != null )
		{
			service = new ContractService();
			service.setId( id );
			service.setContractId( contractId );
			service.setServiceId( Utils.parseInt( serviceEl.getAttribute( "sid" ) ) );
			service.setDateFrom( TimeUtils.parse( serviceEl.getAttribute( "date1" ), TimeUtils.PATTERN_DDMMYYYY ) );
			service.setDateTo( TimeUtils.parse( serviceEl.getAttribute( "date2" ), TimeUtils.PATTERN_DDMMYYYY ) );
			service.setComment( serviceEl.getAttribute( "comment" ) );
		}
		
		for( Element item : XMLUtils.selectElements( doc, "/data/tree/module/service" ) )
		{
			serviceList.add( new IdTitle( item ) );
		}
		
		return new Pair<ContractService, List<IdTitle>>( service, serviceList );
    }
	
	/*http://127.0.0.1:8080/bgbilling/executer?id=487978&sid=74&module=contract&action=UpdateContractService&date2=01.11.2009&
	comment=sdret&BGBillingSecret=3h3nUIHTK1GDEruuJW1dl1Lc&cid=455&date1=13.01.2009&
	[ length = 106 ] xml = <?xml version="1.0" encoding="windows-1251"?><data secret="4231D5728CFA04386FD2B29B4BABB2FA" status="ok"/>*/
	public void updateContractService( ContractService service )
		throws BGException
	{
		Request req = new Request();
		
		req.setModule( CONTRACT_MODULE_ID );
		req.setAction( "UpdateContractService" );
		req.setAttribute( "id", service.getId() );;
		req.setContractId( service.getContractId() );
		req.setAttribute( "date1", TimeUtils.format( service.getDateFrom(), TimeUtils.PATTERN_DDMMYYYY ) );
		req.setAttribute( "date2", TimeUtils.format( service.getDateTo(), TimeUtils.PATTERN_DDMMYYYY ) );
		req.setAttribute( "comment", service.getComment() );
		
		transferData.postData( req, user );		
	}

	/*http://127.0.0.1:8080/bgbilling/executer?id=488387&module=contract&action=DeleteContractService&BGBillingSecret=NUGHzwVCkqzpJDhzOY2kT5N6&
	[ length = 106 ] xml = <?xml version="1.0" encoding="windows-1251"?><data secret="05EFDF4D9BA48E14E46C6CA31674B665" status="ok"/> */
	public void deleteContractService( int contractId, int id )
		throws BGException
	{
		Request req = new Request();
		
		req.setModule( CONTRACT_MODULE_ID );
		req.setAction( "DeleteContractService" );
		req.setAttribute( "id", id );
		req.setContractId( contractId );
		
		transferData.postData( req, user );		
	}
}
