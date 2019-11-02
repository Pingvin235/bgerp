package ru.bgcrm.plugin.bgbilling.struts.action;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.dao.CustomerLinkDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Customer;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.plugin.bgbilling.CommonContractConfig;
import ru.bgcrm.plugin.bgbilling.dao.CommonContractDAO;
import ru.bgcrm.plugin.bgbilling.dao.ContractDAO;
import ru.bgcrm.plugin.bgbilling.model.CommonContract;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

public class CommonContractAction
    extends BaseAction
{
	public ActionForward commonContract( ActionMapping mapping,
	                                     DynActionForm form, HttpServletRequest request,
	                                     HttpServletResponse response, Connection con )
	    throws BGException
	{
		CommonContract contract = new CommonContractDAO( con ).getContractById( form.getId() );

		form.getResponse().setData( "contract", contract );
		if( contract != null )
		{
			request.setAttribute( "customer", new CustomerDAO( con ).getCustomerById( contract.getCustomerId() ) );

			List<CommonObjectLink> contractList = new ArrayList<CommonObjectLink>();
			request.setAttribute( "contractList", contractList );

			String commonContractTitle = contract.getFormatedNumber();

			CustomerLinkDAO linkDao = new CustomerLinkDAO( con );
			for( CommonObjectLink link : linkDao.getObjectLinksWithType( contract.getCustomerId(), Contract.OBJECT_TYPE + "%" ) )
			{
				if( link.getLinkedObjectTitle().startsWith( commonContractTitle ) )
				{
					contractList.add( link );
				}
			}

			Collections.sort( contractList, new Comparator<CommonObjectLink>()
			{
				@Override
				public int compare( CommonObjectLink o1, CommonObjectLink o2 )
				{
					return o1.getLinkedObjectTitle().compareTo( o2.getLinkedObjectTitle() );
				}
			} );
		}

		return processUserTypedForward( con, mapping, form, response, FORWARD_DEFAULT );
	}

	public ActionForward commonContractList( ActionMapping mapping,
	                                         DynActionForm form, HttpServletRequest request,
	                                         HttpServletResponse response, Connection con )
	    throws BGException
	{
		CommonContractConfig config = setup.getConfig( CommonContractConfig.class );

		int customerId = form.getParamInt( "customerId", 0 );

		form.getResponse().setData( "list", new CommonContractDAO( con ).getContractList( customerId ) );
		request.setAttribute( "customerAddressMap", new ParamValueDAO( con ).getParamAddress( customerId, config.getCustomerAddressParamId() ) );

		return processUserTypedForward( con, mapping, form, response, "list" );
	}

	public ActionForward commonContractCreate( ActionMapping mapping,
	                                           DynActionForm form, HttpServletRequest request,
	                                           HttpServletResponse response, Connection con )
	    throws BGException
	{
		CommonContractConfig config = setup.getConfig( CommonContractConfig.class );

		int customerId = form.getParamInt( "customerId", 0 );

		// в общем случае может передаваться код объекта и код адресного его
		// параметра
		int objectId = form.getParamInt( "id", customerId );
		int addressParamId = form.getParamInt( "addressParamId", config.getCustomerAddressParamId() );
		int addressParamIndex = form.getParamInt( "addressParamPos", 0 );
		String commonContractTitle = form.getParam( "commonContractTitle", "" );

		Parameter addressParameter = ParameterCache.getParameter( addressParamId );

		if( addressParameter == null )
		{
			throw new BGMessageException( "Адресного параметра с ID=" + addressParamId + " не существует!" );
		}

		ParameterAddressValue address = null;
		ParamValueDAO paramValueDAO = new ParamValueDAO( con );

		if( Customer.OBJECT_TYPE.equals( addressParameter.getObject() ) )
		{
			address = paramValueDAO.getParamAddress( customerId, addressParamId, addressParamIndex );
		}
		else if( Process.OBJECT_TYPE.equals( addressParameter.getObject() ) )
		{
			address = paramValueDAO.getParamAddress( objectId, addressParamId, addressParamIndex );
		}
		else
		{
			throw new BGMessageException( "Работа с адресными параметрами объектов типа " + addressParameter.getObject() + " не поддерживается!"  );
		}

		if( address == null )
		{
			throw new BGMessageException( "Не установлено значение адресного параметра с ID=" + addressParamId + " для объекта типа " + addressParameter.getObject() + "!" );
		}

		CommonContract contract;
		if( commonContractTitle != null && !commonContractTitle.equals( "" ) && !commonContractTitle.equals( "undefined" ) )
		{
			if( commonContractTitle.length() != config.getNumberLength() )
			{
				throw new BGMessageException( "Неверное количество цифр в договоре!" );
			}
			if( !Utils.isStringNumber( commonContractTitle ) )
			{
				throw new BGMessageException( "Неверные символы в договоре!" );
			}

			contract = new CommonContractDAO( con ).createCommonContractWithTitle( customerId, commonContractTitle, address );
		}
		else
		{
			contract = new CommonContractDAO( con ).createCommonContract( customerId, address );
			contract.setFormatedNumber( config.formatCommonContractNumber( contract ) );
		}
		form.getResponse().setData( "contract", contract );

		return processJsonForward( con, form, response );
	}

	public ActionForward commonContractUpdate( ActionMapping mapping,
	                                           DynActionForm form, HttpServletRequest request,
	                                           HttpServletResponse response, Connection con )
	    throws BGException
	{
		CommonContractDAO commonContractDAO = new CommonContractDAO( con );

		CommonContract commonContract = commonContractDAO.getContractById( form.getParamInt( "id", -1 ) );

		if( form.getPermission().getBoolean( "allowEditNumber", true ) )
		{
			commonContract.setNumber( Utils.parseInt( form.getParam( "number" ) ) );
		}
		else
		{
			if( commonContract.getNumber() != Utils.parseInt( form.getParam( "number" ) ) )
			{
				throw new BGMessageException( "Запрещено редактировать номер ЕД!" );
			}
		}

		commonContract.setDateFrom( form.getParamDate( "dateFrom" ) );
		commonContract.setDateTo( form.getParamDate( form.getParam( "dateTo" ) ) );
		commonContract.setPassword( form.getParam( "password" ) );

		commonContractDAO.updateCommonContract( commonContract );

		return processUserTypedForward( con, mapping, form, response, "" );
	}

	/*
	 * Экшен для личного кабинета (меняет пароль ЕД по id и title обычного договора)
	 */
	public ActionForward commonContractUpdatePassword( ActionMapping mapping,
	                                                   DynActionForm form,
	                                                   HttpServletRequest request,
	                                                   HttpServletResponse response,
	                                                   Connection con )
	    throws BGException
	{
		String contractTitle = form.getParam( "contractTitle" );

		CommonObjectLink link = new CommonObjectLink();
		link.setLinkedObjectType( "contract:" + form.getParam( "billingId" ) );
		link.setLinkedObjectId( form.getParamInt( "id", 0 ) );

		SearchResult<Customer> customerSearch = new SearchResult<Customer>( form );
		new CustomerLinkDAO( con ).searchCustomerByLink( customerSearch, link );

		if( customerSearch.getList().isEmpty() )
		{
			throw new BGMessageException( "Не удалось найти контрагента для договора " + contractTitle );
		}

		Customer customer = customerSearch.getList().get( 0 );

		CommonContract commonContract = new CommonContractDAO( con ).getContractCommon( customer.getId(), contractTitle.substring( 0, contractTitle.length() - 2 ) );

		if( commonContract == null )
		{
			throw new BGMessageException( "Не удалось найти ЕД для договора " + contractTitle + " среди ЕД контрагента " + customer.getId() );
		}

		commonContract.setPassword( form.getParam( "password" ) );

		new CommonContractDAO( con ).updateCommonContract( commonContract );

		for( CommonObjectLink commonObjectLink : new CustomerLinkDAO( con ).getObjectLinksWithType( commonContract.getCustomerId(), Contract.OBJECT_TYPE + "%" ) )
		{
			if( commonContract != null && commonObjectLink.getLinkedObjectTitle().startsWith( commonContract.getFormatedNumber() ) )
			{
				ContractDAO contractDAO = new ContractDAO( form.getUser(), StringUtils.substringAfter( commonObjectLink.getLinkedObjectType(), ":" ) );
				contractDAO.copyParametersToBilling( con, commonContract.getCustomerId(), commonObjectLink.getLinkedObjectId(), commonObjectLink.getLinkedObjectTitle() );
			}
		}

		return processUserTypedForward( con, mapping, form, response, "" );
	}

	public ActionForward copyParamToContract( ActionMapping mapping,
	                                          DynActionForm form,
	                                          HttpServletRequest request,
	                                          HttpServletResponse response,
	                                          Connection con )
	    throws BGException
	{
		CommonContractDAO commonContractDAO = new CommonContractDAO( con );

		int customerId = form.getParamInt( "customerId", -1 );
		int commonContractId = form.getParamInt( "commonContractId", -1 );

		String commonContractTitle = null;

		CommonContract commonContract = commonContractDAO.getContractById( commonContractId );
		if( commonContract != null )
		{
			commonContractTitle = commonContract.getFormatedNumber();
		}

		if( customerId == -1 )
		{
			customerId = commonContract.getCustomerId();
		}

		CustomerLinkDAO linkDao = new CustomerLinkDAO( con );
		for( CommonObjectLink link : linkDao.getObjectLinksWithType( customerId, Contract.OBJECT_TYPE + "%" ) )
		{
			if( link.getLinkedObjectTitle().startsWith( commonContractTitle ) )
			{
				ContractDAO contractDAO = new ContractDAO( form.getUser(), StringUtils.substringAfter( link.getLinkedObjectType(), ":" ) );
				contractDAO.copyParametersToBilling( con, customerId, link.getLinkedObjectId(), link.getLinkedObjectTitle() );
			}
		}

		return processJsonForward( con, form, response );
	}

	public ActionForward commonContractDelete( ActionMapping mapping,
	                                           DynActionForm form, HttpServletRequest request,
	                                           HttpServletResponse response, Connection con )
	    throws BGException
	{
		new CommonContractDAO( con ).deleteCommonContract( form.getId() );

		return processJsonForward( con, form, response );
	}

	@Override
	protected ActionForward unspecified( ActionMapping mapping,
	                                     DynActionForm form, HttpServletRequest request,
	                                     HttpServletResponse response, Connection con )
	    throws Exception
	{
		return commonContract( mapping, form, request, response, con );
	}
}
