package ru.bgcrm.plugin.bgbilling.proto.dao;

import org.bgerp.app.exception.BGException;
import org.bgerp.model.base.IdTitle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;
import ru.bgcrm.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class ContractHierarchyDAO
	extends BillingDAO
{
	private static final String CONTRACT_MODULE_ID = "contract";
	private static final String CONTRACT_HIERARCHY_MODULE_ID = "contract.hierarchy";

	public ContractHierarchyDAO( User user, DBInfo dbInfo )
		throws BGException
	{
		super( user, dbInfo );
	}

	public ContractHierarchyDAO( User user, String billingId )
		throws BGException
	{
		super( user, billingId );
	}

	public List<Integer> getSubContracts( int contractId )
    	throws BGException
    {
    	Request request = new Request();
    	request.setModule( CONTRACT_HIERARCHY_MODULE_ID );
    	request.setAction( "ContractInfo" );
    	request.setContractId( contractId );

    	Document document = transferData.postData( request, user );
    	Element contractElement = (Element)document.getElementsByTagName( CONTRACT_MODULE_ID ).item( 0 );
    	NodeList subContractElementList = contractElement.getElementsByTagName( "sub" );
    	List<Integer> subContractList = new ArrayList<>();

    	for( int index = 0; index < subContractElementList.getLength(); index++ )
    	{
    		Element subContractElement = (Element)subContractElementList.item( index );
    		subContractList.add( Utils.parseInt( subContractElement.getAttribute( "id" ) ) );
    	}

    	return subContractList;
    }

    /**
     * Добавляет субдоговор на родительский договор.
     * @param superContractId id родительского договора.
     * @param subContractId id субдоговора.
     */
    public void addSubcontract( int superContractId, int subContractId )
    	throws BGException
    {
    	Request req = new Request();
    	req.setModule( "contract.hierarchy" );
    	req.setAction( "AddSub" );
    	req.setAttribute( "super", superContractId );
    	req.setAttribute( "sub", subContractId );
    	req.setAttribute( "sub_mode", 0 );

    	transferData.postData( req, user );
    }

    public void addSubcontract( int superContractId, int subContractId, int subMode )
            throws BGException
    {
        Request req = new Request();
        req.setModule( "contract.hierarchy" );
        req.setAction( "AddSub" );
        req.setAttribute( "super", superContractId );
        req.setAttribute( "sub", subContractId );
        req.setAttribute( "sub_mode", subMode );

        transferData.postData( req, user );
    }

	public List<IdTitle> contractSubcontractList( int contractId )
    	throws BGException
    {
    	Request request = new Request();
    	request.setModule( CONTRACT_HIERARCHY_MODULE_ID );
    	request.setAction( "ContractInfo" );
    	request.setContractId( contractId );

    	Document document = transferData.postData( request, user );

    	Element dataElement = document.getDocumentElement();
    	NodeList nodeList = dataElement.getElementsByTagName( "sub" );

    	List<IdTitle> subContractList = new ArrayList<>();
    	for( int index = 0; index < nodeList.getLength(); index++ )
    	{
    		Element rowElement = (Element)nodeList.item( index );
    		IdTitle subContract = new IdTitle();
    		subContract.setId( Utils.parseInt( rowElement.getAttribute( "id" ) ) );
    		subContract.setTitle( rowElement.getAttribute( "title" ) );

    		subContractList.add( subContract );
    	}

    	return subContractList;
    }

    public IdTitle contractSupercontract( int contractId )
    	throws BGException
    {
    	Request request = new Request();
    	request.setModule( CONTRACT_HIERARCHY_MODULE_ID );
    	request.setAction( "ContractInfo" );
    	request.setContractId( contractId );

    	Document document = transferData.postData( request, user );

    	Element dataElement = document.getDocumentElement();
    	NodeList nodeList = dataElement.getElementsByTagName( "super" );

    	if( nodeList.getLength() > 0 )
    	{
    		IdTitle superContract = new IdTitle();
    		Element rowElement = (Element)nodeList.item( 0 );

    		superContract.setId( Utils.parseInt( rowElement.getAttribute( "id" ) ) );
    		superContract.setTitle( rowElement.getAttribute( "title" ) );

    		return superContract;
    	}

    	return null;
    }
}