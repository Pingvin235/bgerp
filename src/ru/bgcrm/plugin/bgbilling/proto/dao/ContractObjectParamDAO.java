package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;

import org.bgerp.app.exception.BGException;
import org.bgerp.model.base.IdTitle;
import org.bgerp.util.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractObjectParameter;
import ru.bgcrm.plugin.bgbilling.proto.model.ParamAddressValue;
import ru.bgcrm.plugin.bgbilling.proto.model.entity.EntityAttrAddress;
import ru.bgcrm.plugin.bgbilling.proto.model.entity.EntityAttrDate;
import ru.bgcrm.plugin.bgbilling.proto.model.entity.EntityAttrList;
import ru.bgcrm.plugin.bgbilling.proto.model.entity.EntityAttrText;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

public class ContractObjectParamDAO extends BillingDAO {
    private static final Log log = Log.getLog();

    private static final String CONTRACT_OBJECT_MODULE_ID = "contract.object";
    private Map<Integer, Document> contractParameters;

    public ContractObjectParamDAO(User user, String billingId) {
        super(user, billingId);
        contractParameters = new HashMap<Integer, Document>();
    }

    public List<ContractObjectParameter> getParameterList(int objectId) {
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.object",
                    "ContractObjectService",
                    "contractObjectParameters");
            req.setParam("objectId", objectId);
            JsonNode ret = transferData.postDataReturn(req, user);
            return readJsonValue(ret.traverse(), jsonTypeFactory.constructCollectionType(List.class, ContractObjectParameter.class));
        } else {
            List<ContractObjectParameter> parameterList = new ArrayList<>();

            for (int paramId : getContractParamIds(objectId)) {
                int paramType = getParamType(objectId, paramId);
                String paramTitle = getParamTitle(objectId, paramId);
                String paramValue = getTextParam(objectId, paramId);

                parameterList.add(new ContractObjectParameter(paramId, paramType, paramTitle, paramValue, null));
            }

            return parameterList;
        }
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

    public ContractObjectParameter getParameter(int objectId, int paramId) {
        if (dbInfo.versionCompare("9.2") >= 0) {
            ContractObjectParameter parameter = getParameterList(objectId).stream()
                    .filter(op -> op.getParameterId() == paramId)
                    .findFirst().orElse(null);
            return parameter;
        } else {
            getContractParams(objectId);
            int paramType = getParamType(objectId, paramId);
            String paramTitle = getParamTitle(objectId, paramId);
            String paramValue = getTextParam(objectId, paramId);

            return new ContractObjectParameter(paramId, paramType, paramTitle, paramValue,null);
        }

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
    	if( dbInfo.versionCompare( "5.2" ) >= 0 )
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
    		if( dbInfo.versionCompare( "5.1" ) <= 0 )
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

    public ParamAddressValue getAddressParam(int objectId, int paramId) {
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

    public List<IdTitle> getListParam(int objectId, int paramId) {
        if (dbInfo.versionCompare("9.2") >= 0) {

            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.object",
                    "ParameterObjectTypeService",
                    "getValuesForListParameter");
            req.setParam("parameterId", paramId);
            JsonNode ret = transferData.postDataReturn(req, user);
            return readJsonValue(ret.traverse(), jsonTypeFactory.constructCollectionType(List.class, IdTitle.class));

        } else {


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
    }

    /**
     * Use {@link ParamAddressValue#toParameterAddressValue(ParamAddressValue, Connection)}
     */
    @Deprecated
    public static ParameterAddressValue toCrmObject(ParamAddressValue item, Connection con) throws SQLException {
        return item.toParameterAddressValue(con);
    }

    public void updateTextParameter(int  contractId, int objectId, int paramId, String value) {
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.object",
                    "ContractObjectParameterService", "textParameterValueUpdate");
            req.setParam("contractId", contractId);
            req.setParam("parameterId", new EntityAttrText(objectId, paramId, value));
            transferData.postDataReturn(req, user);
        } else {
            Request req = new Request();

            req.setModule(CONTRACT_OBJECT_MODULE_ID);
            req.setAction("TextParamValueUpdate");
            req.setAttribute("object", objectId);
            req.setAttribute("param", paramId);
            req.setAttribute("value", value);

            transferData.postData(req, user);
        }
    }

    public void updateListParameter(int  contractId, int objectId, int paramId, String value) {
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.object",
                    "ContractObjectParameterService", "listParameterValueUpdate");
            req.setParam("contractId", contractId);
            req.setParam("entityAttrList", new EntityAttrList(objectId, paramId, Integer.parseInt(value),null));
            transferData.postDataReturn(req, user);
        } else {
            Request req = new Request();

            req.setModule(CONTRACT_OBJECT_MODULE_ID);
            req.setAction("ListParamValueUpdate");
            req.setAttribute("object", objectId);
            req.setAttribute("param", paramId);
            req.setAttribute("value", value);

            transferData.postData(req, user);
        }
    }

    public void updateAddressParameter(int contractId, int objectId, int paramId, ParamAddressValue address) {

        if (dbInfo.versionCompare("9.2") >= 0) {
            EntityAttrAddress attrAddress = new EntityAttrAddress(objectId, paramId);
            attrAddress.setHouseId(address.getHouseId());
            attrAddress.setPod( Utils.parseInt(address.getPod()));
            attrAddress.setFloor( Utils.parseInt(address.getFloor()));
            attrAddress.setFlat( Utils.maskNull(address.getFlat()));
            attrAddress.setRoom(Utils.maskNull(address.getRoom()));
            attrAddress.setComment(Utils.maskNull(address.getComment()));


            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.object",
                    "ContractObjectParameterService", "addressParameterValueUpdate");
            req.setParam("contractId", contractId);
            req.setParam("entityAttrAddress", attrAddress);
            transferData.postDataReturn(req, user);
        } else {

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
    }

    public void updateDateParameter(int  contractId, int objectId, int paramId, String value) {
        if (dbInfo.versionCompare("9.2") >= 0) {
            Date date = TimeUtils.parse(value, TimeUtils.PATTERN_DDMMYYYY);
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.object",
                    "ContractObjectParameterService", "dateParameterValueUpdate");
            req.setParam("contractId", contractId);
            req.setParam("entityAttrDate", new EntityAttrDate(objectId, paramId, date));
            transferData.postDataReturn(req, user);
        } else {
            Request req = new Request();

            req.setModule(CONTRACT_OBJECT_MODULE_ID);
            req.setAction("DateParamValueUpdate");
            req.setAttribute("object", objectId);
            req.setAttribute("param", paramId);
            req.setAttribute("value", value);

            transferData.postData(req, user);
        }
    }

    public List<IdTitle> getValuesForListParameter(int parameterId) {

        RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.object",
                "ParameterObjectTypeService",
                "getValuesForListParameter");
        req.setParam("parameterId", parameterId);
        JsonNode ret = transferData.postDataReturn(req, user);
        return readJsonValue(ret.traverse(), jsonTypeFactory.constructCollectionType(List.class, IdTitle.class));

    }
}
