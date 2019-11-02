package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterPhoneValueItem;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractParameter;
import ru.bgcrm.plugin.bgbilling.proto.model.ParamAddressValue;
import ru.bgcrm.plugin.bgbilling.proto.model.ParamPhoneValueItem;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

public class ContractObjectParamDAO extends BillingDAO {
    private static final Logger log = Logger.getLogger(ContractObjectParamDAO.class);

    private static final String CONTRACT_OBJECT_MODULE_ID = "contract.object";
    private Map<Integer, Document> contractParameters;

    public ContractObjectParamDAO(User user, String billingId) throws BGException {
        super(user, billingId);
        contractParameters = new HashMap<Integer, Document>();
    }

    public List<ContractParameter> getParameterList(int objectId) throws BGException {
        List<ContractParameter> parameterList = new ArrayList<ContractParameter>();

        for (int paramId : getContractParamIds(objectId)) {
            int paramType = getParamType(objectId, paramId);
            String paramTitle = getParamTitle(objectId, paramId);
            String paramValue = getTextParam(objectId, paramId);

            parameterList.add(new ContractParameter(paramId, paramType, paramTitle, paramValue));
        }

        return parameterList;
    }

    private Document getContractParams(int objectId) {
        if (contractParameters.get(objectId) == null) {
            Request request = new Request();
            request.setModule(CONTRACT_OBJECT_MODULE_ID);
            request.setAction("ObjectParamTable");
            request.setAttribute("object", objectId);

            Document document = null;

            try {
                document = transferData.postData(request, user);
            } catch (BGException exception) {
                log.error(exception);
            }

            contractParameters.put(objectId, document);
        }

        return contractParameters.get(objectId);
    }

    public String getTextParam(int objectId, int paramId) {
        getContractParams(objectId);
        return XMLUtils.selectText(getContractParams(objectId), "/data/table/row[@param_id=" + paramId + "]/@value");
    }

    public ContractParameter getParameter(int objectId, int paramId) {
        getContractParams(objectId);
        int paramType = getParamType(objectId, paramId);
        String paramTitle = getParamTitle(objectId, paramId);
        String paramValue = getTextParam(objectId, paramId);

        return new ContractParameter(paramId, paramType, paramTitle, paramValue);
    }

    public int getParamType(int objectId, int paramId) {
        return Utils.parseInt(XMLUtils.selectText(getContractParams(objectId), "/data/table/row[@param_id=" + paramId + "]/@type_id"));
    }

    public String getParamTitle(int objectId, int paramId) {
        return XMLUtils.selectText(getContractParams(objectId), "/data/table/row[@param_id=" + paramId + "]/@title");
    }

    public List<Integer> getContractParamIds(int objectId) {
        NodeList nodes = XMLUtils.selectNodeList(getContractParams(objectId), "/data/table/row/@param_id");

        List<Integer> paramList = new ArrayList<Integer>();

        for (int i = 0; i < nodes.getLength(); i++) {
            paramList.add(Utils.parseInt(nodes.item(i).getNodeValue()));
        }
        return paramList;
    }

    /*public List<ParamPhoneValueItem> getPhoneParam( int contractId, int paramId )
    	throws BGException
    {
    	List<ParamPhoneValueItem> result = new ArrayList<ParamPhoneValueItem>();
    
    	Request billingRequest = new Request();
    	billingRequest.setModule( CONTRACT_MODULE_ID );
    	billingRequest.setAction( "PhoneInfo" );
    	if( dbInfo.getVersion().compareTo( "5.2" ) >= 0 )
    	{
    		billingRequest.setAction( "GetPhoneInfo" );
    	}
    
    	billingRequest.setContractId( contractId );
    	billingRequest.setAttribute( "pid", paramId );
    
    	Document doc = transferData.postData( billingRequest, user );
    
    	Element phone = XMLUtils.selectElement( doc, "/data/phone" );
    	if( phone != null )
    	{
    		int itemCount = Utils.parseInt( phone.getAttribute( "count" ) );
    
    		// до 5.1 было просто зашито 5 телефонов
    		if( dbInfo.getVersion().compareTo( "5.1" ) <= 0 )
    		{
    			itemCount = 5;
    		}
    
    		for( int i = 1; i <= itemCount; i++ )
    		{
    			String number = phone.getAttribute( "phone" + i );
    			String comment = phone.getAttribute( "comment" + i );
    			String format = phone.getAttribute( "format" + i );
    
    			if( Utils.isBlankString( number ) )
    			{
    				continue;
    			}
    
    			ParamPhoneValueItem item = new ParamPhoneValueItem();
    			item.setPhone( number );
    			item.setComment( comment );
    			item.setFormat( format );
    
    			result.add( item );
    		}
    	}
    
    	return result;
    }*/

    public ParamAddressValue getAddressParam(int objectId, int paramId) throws BGException {
        ParamAddressValue result = new ParamAddressValue();

        Request req = new Request();

        req.setModule(CONTRACT_OBJECT_MODULE_ID);
        req.setAction("AddressParamValueGet");
        req.setAttribute("object", objectId);
        req.setAttribute("param", paramId);

        Document doc = transferData.postData(req, user);

        Element address = XMLUtils.selectElement(doc, "/data/address");
        if (address != null && Utils.notBlankString(XMLUtils.selectText(address, "@hid"))) {
            result.setCityId(Utils.parseInt(address.getAttribute("cityid")));
            result.setCityTitle(address.getAttribute("city"));
            result.setAreaTitle(address.getAttribute("areaValue"));
            result.setQuarterTitle(address.getAttribute("quarterValue"));
            result.setStreetId(Utils.parseInt(address.getAttribute("streetid")));
            result.setStreetTitle(address.getAttribute("street"));
            result.setHouseId(Utils.parseInt(address.getAttribute("hid")));
            result.setHouse(address.getAttribute("house"));
            result.setFlat(address.getAttribute("flat"));
            result.setRoom(address.getAttribute("room"));
            result.setComment(address.getAttribute("comment"));
            result.setPod(address.getAttribute("pod"));
            result.setFloor(address.getAttribute("floor"));
        }

        return result;
    }

    public List<IdTitle> getListParam(int objectId, int paramId) throws BGException {
        List<IdTitle> result = new ArrayList<IdTitle>();

        Request req = new Request();

        req.setModule(CONTRACT_OBJECT_MODULE_ID);
        req.setAction("ListParamValueGet");
        req.setAttribute("object", objectId);
        req.setAttribute("param", paramId);

        Document doc = transferData.postData(req, user);

        Element dataElement = doc.getDocumentElement();
        NodeList nodeList = dataElement.getElementsByTagName("item");
        for (int index = 0; index < nodeList.getLength(); index++) {
            IdTitle value = new IdTitle();
            Element element = (Element) nodeList.item(index);

            value.setId(Utils.parseInt(element.getAttribute("id")));
            value.setTitle(element.getAttribute("title"));

            result.add(value);
        }

        return result;
    }

    @Deprecated
    public static ParameterPhoneValue toCrmObject(List<ParamPhoneValueItem> phones) {
        List<ParameterPhoneValueItem> crmItems = new ArrayList<>();
        for (ParameterPhoneValueItem item : phones) {
            ParameterPhoneValueItem crmItem = new ParameterPhoneValueItem();
            crmItem.setPhone(item.getPhone());
            crmItem.setFormat(item.getFormat());
            crmItem.setComment(item.getComment());
            crmItem.setFlags(item.getFlags());
            crmItems.add(crmItem);
        }
        ParameterPhoneValue result = new ParameterPhoneValue();
        result.setItemList(crmItems);

        return result;
    }

    /**
     * Use {@link ParamAddressValue#toParameterAddressValue(ParamAddressValue, Connection)}
     */
    @Deprecated
    public static ParameterAddressValue toCrmObject(ParamAddressValue item, Connection con) throws BGException {
        return item.toParameterAddressValue(con);
    }

    public void updateTextParameter(int objectId, int paramId, String value) throws BGException {
        Request req = new Request();

        req.setModule(CONTRACT_OBJECT_MODULE_ID);
        req.setAction("TextParamValueUpdate");
        req.setAttribute("object", objectId);
        req.setAttribute("param", paramId);
        req.setAttribute("value", value);

        transferData.postData(req, user);
    }

    public void updateListParameter(int objectId, int paramId, String value) throws BGException {
        Request req = new Request();

        req.setModule(CONTRACT_OBJECT_MODULE_ID);
        req.setAction("ListParamValueUpdate");
        req.setAttribute("object", objectId);
        req.setAttribute("param", paramId);
        req.setAttribute("value", value);

        transferData.postData(req, user);
    }

    public void updateAddressParameter(int objectId, int paramId, ParamAddressValue address) throws BGException {
        Request req = new Request();

        req.setModule(CONTRACT_OBJECT_MODULE_ID);
        req.setAction("AddressParamValueUpdate");
        req.setAttribute("object", objectId);
        req.setAttribute("param", paramId);
        req.setAttribute("index", address.getIndex());
        req.setAttribute("cityStr", address.getCityTitle());
        req.setAttribute("streetStr", address.getStreetTitle());
        req.setAttribute("houseAndFrac", address.getHouse());
        req.setAttribute("hid", address.getHouseId());
        req.setAttribute("pod", address.getPod());
        req.setAttribute("floor", address.getFloor());
        req.setAttribute("flat", address.getFlat());
        req.setAttribute("room", address.getRoom());
        req.setAttribute("comment", address.getComment());

        transferData.postData(req, user);
    }

    public void updateDateParameter(int objectId, int paramId, String value) throws BGException {
        Request req = new Request();

        req.setModule(CONTRACT_OBJECT_MODULE_ID);
        req.setAction("DateParamValueUpdate");
        req.setAttribute("object", objectId);
        req.setAttribute("param", paramId);
        req.setAttribute("value", value);

        transferData.postData(req, user);
    }
}
