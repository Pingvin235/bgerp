package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.status.ContractStatus;
import ru.bgcrm.plugin.bgbilling.proto.model.status.ContractStatusLogItem;
import ru.bgcrm.plugin.bgbilling.ws.contract.status.ContractStatusMonitorService;
import ru.bgcrm.plugin.bgbilling.ws.contract.status.ContractStatusMonitorService_Service;
import ru.bgcrm.plugin.bgbilling.ws.contract.status51.WSContractStatusMonitor;
import ru.bgcrm.plugin.bgbilling.ws.contract.status51.WSContractStatusMonitor_Service;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

public class ContractStatusDAO extends BillingDAO {
    private static final String CONTRACT_STATUS_MODULE_ID = "ru.bitel.bgbilling.kernel.contract.status";
    private static final String MODULE = "contract.status";

    public ContractStatusDAO(User user, String billingId) throws BGException {
        super(user, billingId);
    }

    public ContractStatusDAO(User user, DBInfo dbInfo) throws BGException {
        super(user, dbInfo);
    }

    /* 
     * Возвращает статус из списка статусов договора, у которого пустая дата закрытия
     * @param contractId 
     * @return
     * @throws BGException	
    public ContractStatus getContractStatus( int contractId )
        throws BGException
    {
    	List<ContractStatus> statusList = getContractStatusList( contractId );
    
    	for( ContractStatus status : statusList )
    	{
    		if( status.getDateTo() == null )
    		{
    			return status;
    		}
    	}
    
    	return null;
    }
     */

    /**
     * Возвращает список статусов договора с периодами.
     * @param contractId
     * @return
     * @throws BGException
     */
    public List<ContractStatus> statusList(int contractId) throws BGException {
        List<ContractStatus> statusList = new ArrayList<ContractStatus>();

        Request request = new Request();
        request.setModule(MODULE);
        request.setAction("ContractStatusTable");
        request.setContractId(contractId);

        Document doc = transferData.postData(request, user);

        for (Element element : XMLUtils.selectElements(doc, "/data/table/data/row")) {
            ContractStatus status = new ContractStatus();
            loadContractStatus(element, status);
            statusList.add(status);
        }

        return statusList;
    }

    /**
     * Лог изменений статуса договора.
     * @param contractId
     * @return
     * @throws BGException
     */
    public List<ContractStatusLogItem> statusLog(int contractId) throws BGException {
        List<ContractStatusLogItem> result = new ArrayList<ContractStatusLogItem>();

        Request request = new Request();
        request.setModule(MODULE);
        request.setAction("ContractStatusLog");
        request.setContractId(contractId);

        Document doc = transferData.postData(request, user);

        for (Element element : XMLUtils.selectElements(doc, "/data/table/data/row")) {
            ContractStatusLogItem status = new ContractStatusLogItem();

            loadContractStatus(element, status);
            status.setTime(TimeUtils.parse(element.getAttribute("date"), TimeUtils.PATTERN_DDMMYYYYHHMMSS));
            status.setUser(element.getAttribute("user"));

            result.add(status);
        }

        return result;
    }

    private void loadContractStatus(Element element, ContractStatus status) throws BGException {
        status.setId(Utils.parseInt(element.getAttribute("id")));
        status.setComment(element.getAttribute("comment"));
        status.setStatus(element.getAttribute("status"));
        TimeUtils.parsePeriod(element.getAttribute("period"), status);
    }

    public void updateStatus(int contractId, int statusId, Date dateFrom, Date dateTo, String comment)
            throws BGException {
        if (dbInfo.versionCompare("6.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(CONTRACT_STATUS_MODULE_ID, "ContractStatusMonitorService", "changeContractStatus");
            req.setParam("cid", Collections.singletonList(contractId));
            req.setParam("statusId", statusId);
            req.setParam("dateFrom", dateFrom);
            req.setParam("dateTo", dateTo);
            req.setParam("comment", comment);
            req.setParam("confirmChecked",true);
            
            transferData.postDataReturn(req, user);
        }
        //TODO: Убрать со временем.
        else  if (dbInfo.getVersion().compareTo("5.2") >= 0) {
            try {
                ContractStatusMonitorService service = getWebService(ContractStatusMonitorService_Service.class,
                        ContractStatusMonitorService.class);
                service.changeContractStatus(Collections.singletonList(contractId), statusId, dateFrom, dateTo,
                        comment);
            } catch (Exception e) {
                processWebServiceException(e);
            }
        } else {
            try {
                WSContractStatusMonitor service = getWebService(WSContractStatusMonitor_Service.class,
                        WSContractStatusMonitor.class);
                service.changeContractStatus(Collections.singletonList(contractId), statusId, dateFrom, dateTo,
                        comment);
            } catch (Exception e) {
                processWebServiceException(e);
            }
        }
    }
}