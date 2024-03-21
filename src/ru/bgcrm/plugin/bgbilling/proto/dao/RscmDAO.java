package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import org.bgerp.app.exception.BGException;
import org.bgerp.model.Pageable;

import ru.bgcrm.model.Page;
import ru.bgcrm.model.Period;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.proto.model.rscm.RscmService;

public class RscmDAO
	extends BillingModuleDAO
{
	private static final String RSCM_MODULE_ID = "ru.bitel.bgbilling.modules.rscm";

	public RscmDAO( User user, String billingId, int moduleId )
		throws BGException
	{
		super( user, billingId, moduleId );
	}

	public RscmDAO( User user, DBInfo dbInfo, int moduleId )
		throws BGException
	{
		super( user, dbInfo.getId(), moduleId );
	}

	public void getServices( Pageable<RscmService> result, int contractId, Date dateFrom, Date dateTo )
    	throws BGException
    {
        RequestJsonRpc req = new RequestJsonRpc( RSCM_MODULE_ID, moduleId, "RSCMService", "searchRSCMContractService" );
    	req.setParamContractId( contractId );
    	req.setParam( "period", new Period(dateFrom, dateTo) );
    	req.setParam( "page", result.getPage() );

    	JsonNode ret = transferData.postDataReturn( req, user );
    	List<RscmService> serviceList = readJsonValue( ret.findValue( "list" ).traverse(),
    	                                               jsonTypeFactory.constructCollectionType( List.class, RscmService.class ) );

    	result.getList().addAll( serviceList );
    	result.getPage().setData( jsonMapper.convertValue( ret.findValue( "page" ), Page.class ) );
    }

	public RscmService getService(int contractId, int contractServiceId)
		throws BGException
	{
        RequestJsonRpc req = new RequestJsonRpc( RSCM_MODULE_ID, moduleId, "RSCMService", "getRSCMContractService" );
    	req.setParamContractId( contractId );
    	req.setParam( "rscmContractServiceId", contractServiceId );

    	JsonNode ret = transferData.postDataReturn( req, user );
    	return jsonMapper.convertValue( ret, RscmService.class );
	}

	public void updateService(RscmService service)
    	throws BGException
    {
        RequestJsonRpc req = new RequestJsonRpc( RSCM_MODULE_ID, moduleId, "RSCMService", "updateRSCMContractService" );
    	req.setParam( "rscmContractService", service );

    	transferData.postData( req, user );
    }

	public void deleteService(int contractId, int contractServiceId, Date month)
		throws BGException
	{
		RequestJsonRpc req = new RequestJsonRpc( RSCM_MODULE_ID, moduleId, "RSCMService", "deleteRSCMContractService" );
    	req.setParamContractId( contractId );
		req.setParam( "rscmContractServiceId", contractServiceId );
		req.setParam( "month", month );

		transferData.postData( req, user );
	}
}