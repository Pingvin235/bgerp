package ru.bgcrm.plugin.fias.struts.action;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

import ru.bgcrm.dao.AddressDAO;
import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.FileDataDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.FileData;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.param.address.AddressCity;
import ru.bgcrm.model.param.address.AddressItem;
import ru.bgcrm.plugin.fias.Fias;
import ru.bgcrm.plugin.fias.dao.FiasDAO;
import ru.bgcrm.plugin.fias.model.CrmHouse;
import ru.bgcrm.plugin.fias.model.FiasHouse;
import ru.bgcrm.plugin.fias.model.FiasStreet;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class FiasAction
	extends BaseAction
{
	public FiasAction()
	{
		super();
	}

	protected ActionForward unspecified( ActionMapping mapping,
										 DynActionForm form,
										 HttpServletRequest request,
										 HttpServletResponse response,
										 ConnectionSet conSet )
		throws Exception
	{

		return processUserTypedForward( conSet, mapping, form, response, "default" );
	}

	public ActionForward streetList( ActionMapping mapping,
									 DynActionForm form,
									 HttpServletRequest request,
									 HttpServletResponse response,
									 ConnectionSet conSet )
		throws Exception
	{
		AddressDAO addressDAO = new AddressDAO( conSet.getConnection() );
		String addressCityTitle = form.getParam( "addressCityTitle" );
		SearchResult<AddressCity> searchResult = new SearchResult<AddressCity>();
		addressDAO.searchAddressCityList( searchResult, 1, CommonDAO.getLikePattern( addressCityTitle, "subs" ), true, null );

		form.getResponse().setData( "cityList", searchResult.getList() );

		return processUserTypedForward( conSet, mapping, form, response, "streetForm" );
	}

	public ActionForward houseList( ActionMapping mapping,
									DynActionForm form,
									HttpServletRequest request,
									HttpServletResponse response,
									ConnectionSet conSet )
		throws Exception
	{
		AddressDAO addressDAO = new AddressDAO( conSet.getConnection() );
		String addressCityTitle = form.getParam( "addressCityTitle" );
		SearchResult<AddressCity> searchResult = new SearchResult<AddressCity>();
		addressDAO.searchAddressCityList( searchResult, 1, CommonDAO.getLikePattern( addressCityTitle, "subs" ), true, null );

		form.getResponse().setData( "cityList", searchResult.getList() );

		return processUserTypedForward( conSet, mapping, form, response, "houseForm" );
	}

	public ActionForward updateBase( ActionMapping mapping,
									 DynActionForm form,
									 HttpServletRequest request,
									 HttpServletResponse response,
									 ConnectionSet conSet )
		throws Exception
	{
		return processUserTypedForward( conSet, mapping, form, response, "updateBaseForm" );
	}

	public ActionForward searchStreetByTerm( ActionMapping mapping,
											 DynActionForm form,
											 HttpServletRequest request,
											 HttpServletResponse response,
											 ConnectionSet conSet )
		throws Exception
	{
		String titleTerm = form.getParam( "titleTerm" );
		Integer cityId = form.getParamInt( "cityId" );

		FiasDAO fiasDAO = new FiasDAO( conSet.getConnection() );
		SearchResult<FiasStreet> searchResult = new SearchResult<FiasStreet>( form );

		fiasDAO.searchFiasStreetByTerm( searchResult, titleTerm, cityId, false );

		return processJsonForward( conSet, form, response );
	}

	public ActionForward linkStreetList( ActionMapping mapping,
										 DynActionForm form,
										 HttpServletRequest request,
										 HttpServletResponse response,
										 ConnectionSet conSet )
		throws Exception
	{
		String titleTerm = form.getParam( "titleTerm" );
		Integer cityId = form.getParamInt( "cityId" );

		FiasDAO fiasDAO = new FiasDAO( conSet.getConnection() );
		SearchResult<FiasStreet> searchResult = new SearchResult<FiasStreet>( form );
		if( Utils.notBlankString( form.getParam( "noLimit" ) ) )
		{
			searchResult.getPage().setPageSize( 0 );
		}

		fiasDAO.searchFiasStreetByTerm( searchResult, titleTerm, cityId, true );

		return processUserTypedForward( conSet, mapping, form, response, "streetList" );
	}

	public ActionForward searchSimilarStreet( ActionMapping mapping,
											  DynActionForm form,
											  HttpServletRequest request,
											  HttpServletResponse response,
											  ConnectionSet conSet )
		throws Exception
	{
		String title = form.getParam( "title" );
		Integer cityId = form.getParamInt( "cityId" );

		FiasDAO fiasDAO = new FiasDAO( conSet.getConnection() );
		SearchResult<FiasStreet> searchResult = new SearchResult<FiasStreet>( form );

		fiasDAO.searchSimilarStreet( searchResult, title, cityId );

		return processJsonForward( conSet, form, response );
	}

	public ActionForward addStreetLink( ActionMapping mapping,
										DynActionForm form,
										HttpServletRequest request,
										HttpServletResponse response,
										ConnectionSet conSet )
		throws SQLException
	{
		Integer crmStreetId = form.getParamInt( "crmStreetId" );
		String fiasStreetId = form.getParam( "fiasStreetId" );
		Integer cityId = form.getParamInt( "cityId" );

		FiasDAO fiasDAO = new FiasDAO( conSet.getConnection() );
		fiasDAO.addStreetLink( crmStreetId, fiasStreetId );
		fiasDAO.addHouseLinks( fiasStreetId, cityId );

		return processJsonForward( conSet, form, response );
	}

	public ActionForward delStreetLink( ActionMapping mapping,
										DynActionForm form,
										HttpServletRequest request,
										HttpServletResponse response,
										ConnectionSet conSet )
		throws SQLException
	{
		String fiasStreetId = form.getParam( "streetId" );

		FiasDAO fiasDAO = new FiasDAO( conSet.getConnection() );
		fiasDAO.delStreetLink( fiasStreetId );
		fiasDAO.delHouseLink( fiasStreetId );

		return processJsonForward( conSet, form, response );
	}

	public ActionForward delHouseLink( ActionMapping mapping,
									   DynActionForm form,
									   HttpServletRequest request,
									   HttpServletResponse response,
									   ConnectionSet conSet )
		throws SQLException
	{
		String fiasStreetId = form.getParam( "houseId" );

		FiasDAO fiasDAO = new FiasDAO( conSet.getConnection() );
		fiasDAO.delHouseLink( fiasStreetId );

		return processJsonForward( conSet, form, response );
	}

	public ActionForward searchAddressStreet( ActionMapping mapping,
											  DynActionForm form,
											  HttpServletRequest request,
											  HttpServletResponse response,
											  ConnectionSet conSet )
		throws SQLException
	{
		int cityId = Utils.parseInt( form.getParam( "cityId" ) );
		String title = form.getParam( "title", "" );

		FiasDAO fiasDAO = new FiasDAO( conSet.getConnection() );
		SearchResult<AddressItem> searchResult = new SearchResult<AddressItem>( form );
		fiasDAO.searchAddressStreetByTerm( searchResult, cityId, CommonDAO.getLikePattern( title, "subs" ) );

		return processJsonForward( conSet, form, response );
	}

	public ActionForward searchHouseByTerm( ActionMapping mapping,
											DynActionForm form,
											HttpServletRequest request,
											HttpServletResponse response,
											ConnectionSet conSet )
		throws SQLException
	{

		String streetId = form.getParam( "streetId" );
		String indexTerm = form.getParam( "houseTerm" );
		Integer cityId = form.getParamInt( "cityId" );
		Integer streetSide = form.getParamInt( "streetSide" );
		boolean isLink = form.getParamInt( "isLink" ) == 1 ? true : false;

		FiasDAO fiasDAO = new FiasDAO( conSet.getConnection() );

		if( isLink )
		{
			SearchResult<FiasHouse> searchResult = new SearchResult<FiasHouse>( form );
			fiasDAO.searchFiasHouseByTerm( searchResult, streetId, indexTerm, cityId, streetSide );

			return processUserTypedForward( conSet, mapping, form, response, "houseList" );
		}
		else
		{
			SearchResult<CrmHouse> searchResult = new SearchResult<CrmHouse>( form );
			fiasDAO.getNotLinkHouseList( searchResult, streetId, indexTerm, streetSide );

			form.getResponse().setData( "postalCodeList", fiasDAO.recommendedPostalCodeList( streetId ) );

			return processUserTypedForward( conSet, mapping, form, response, "notLinkHouseList" );
		}
	}

	public ActionForward copyPostalCode( ActionMapping mapping,
										 DynActionForm form,
										 HttpServletRequest request,
										 HttpServletResponse response,
										 ConnectionSet conSet )
		throws SQLException
	{
		List<Integer> fiasHouseIds = form.getSelectedValuesList( "houseId" );
		String streetId = form.getParam( "streetId" );

		FiasDAO fiasDAO = new FiasDAO( conSet.getConnection() );

		if( Utils.notBlankString( streetId ) )
		{
			fiasHouseIds = fiasDAO.getLinkHouseIdList( streetId );
		}

		fiasDAO.copyPostalCode( fiasHouseIds );

		return processJsonForward( conSet, form, response );
	}

	public ActionForward copyStreetTitle( ActionMapping mapping,
										  DynActionForm form,
										  HttpServletRequest request,
										  HttpServletResponse response,
										  ConnectionSet conSet )
		throws SQLException
	{
		String streetId = form.getParam( "streetId" );

		FiasDAO fiasDAO = new FiasDAO( conSet.getConnection() );
		fiasDAO.copyStreetTitle( streetId );

		return processJsonForward( conSet, form, response );
	}

	public ActionForward manualSetPostalCode( ActionMapping mapping,
											  DynActionForm form,
											  HttpServletRequest request,
											  HttpServletResponse response,
											  ConnectionSet conSet )
		throws SQLException, BGMessageException
	{
		String postalCode = form.getParam( "postalCode" );
		Set<Integer> crmHouseIds = form.getSelectedValues( "houseId" );

		if( crmHouseIds.size() > 0 && Utils.notBlankString( postalCode ) )
		{
			if( postalCode.length() == 6 )
			{
				FiasDAO fiasDAO = new FiasDAO( conSet.getConnection() );
				fiasDAO.manualSetPostalCode( crmHouseIds, postalCode );
			}
			else
			{
				throw new BGMessageException( "Почтовой индекс должен состоять из 6 цифр" );
			}
		}

		return processJsonForward( conSet, form, response );
	}

	public ActionForward updateFiasBase( ActionMapping mapping,
										 DynActionForm form,
										 HttpServletRequest request,
										 HttpServletResponse response,
										 ConnectionSet conSet )
		throws IOException, SQLException, BGException
	{
		FormFile formFile = form.getFile();

		FileDataDAO fileDataDAO = new FileDataDAO( conSet.getConnection() );

		FileData fileData = new FileData();
		fileData.setTitle( formFile.getFileName() );
		FileOutputStream fos = fileDataDAO.add( fileData );

		fos.write( formFile.getFileData() );
		fos.close();

		Fias fias = new Fias();

		String log = "";

		if( formFile.getFileName().contains( "AS_ADDROBJ" ) )
		{

			log += fias.updateStreet( fileDataDAO.getFile( fileData ) );
		}
		else if( formFile.getFileName().contains( "AS_HOUSEINT" ) )
		{
			log += fias.updateHouseInterval( fileDataDAO.getFile( fileData ) );
		}

		form.getResponse().setData( "log", log );

		return processJsonForward( conSet, form, response );
	}
}
