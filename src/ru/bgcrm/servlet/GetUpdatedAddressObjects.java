package ru.bgcrm.servlet;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.dao.AddressDAO;
import ru.bgcrm.model.param.address.AddressCity;
import ru.bgcrm.model.param.address.AddressCountry;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.model.param.address.AddressItem;
import ru.bgcrm.util.sql.SQLUtils;

/**
 * Сервлет позволяет получить из информацию об адресах из БД
 * (страны, города, районы, кварталы, улицы и дома),
 * измененные после определенной даты.
 */
public class GetUpdatedAddressObjects
    extends BaseServlet
{
    public GetUpdatedAddressObjects()
    {
        super( GetUpdatedAddressObjects.class );
    }
    
    @Override
    public void doGet( HttpServletRequest httpServletRequest, HttpServletResponse response )
    {
        long time;
        try
        {
            time = Long.parseLong( (String) httpServletRequest.getParameter( "time" ) );
        }
        catch (NumberFormatException ex)
        {
            time = 0;
        }
        String selectedCities = httpServletRequest.getParameter( "city" );
        if ( log.isDebugEnabled() )
        {
        	log.debug( "time=" + httpServletRequest.getParameter( "time" ) + "; selectedCities=" + selectedCities );
        }
        
        List<AddressCity> cities = new ArrayList<AddressCity>();
        List<AddressItem> areas = new ArrayList<AddressItem>();
        List<AddressItem> quarters = new ArrayList<AddressItem>();
        List<AddressItem> streets = new ArrayList<AddressItem>();
        List<AddressCountry> countries = new ArrayList<AddressCountry>();
        List<AddressHouse> houses = new ArrayList<AddressHouse>();
        
        Connection con = setup.getDBConnectionFromPool();
		try 
		{
			AddressDAO addressDAO = new AddressDAO( con );
			//если не заданы конкретные города
			//выбираем все объекты согласно времени time
			int[] citiesId = null;
			int[] countriesId = null;
			if (selectedCities != null)
			{
				citiesId = getCitiesId( selectedCities );
				List<Integer> countryIdList = addressDAO.getCountryIdByCityId( citiesId );
				countriesId = new int[countryIdList.size()];
				int i = 0;
				for ( Integer id : countryIdList )
				{
					countriesId[i++] = id.intValue();
				}
			}
			countries = addressDAO.getUpdatedCountries( time, countriesId );
			cities = addressDAO.getUpdatedCities( time, citiesId );
			areas = addressDAO.getUpdatedAreas( time, citiesId );
			quarters = addressDAO.getUpdatedQuarters( time, citiesId );
			streets = addressDAO.getUpdatedStreets( time, citiesId );
			houses = addressDAO.getUpdatedHouses( time, citiesId );
		} 
		catch ( Exception e ) 
		{
			log.error( e.getMessage(), e );
		}
		finally
		{
			SQLUtils.closeConnection( con );
		}   
                
        Document document = buildXMLDocument( countries, cities, areas, quarters, streets, houses );
        putDocumentToResponse( response, document );
    }

    private Document buildXMLDocument( List<AddressCountry> countries,
                                       List<AddressCity> cities,
                                       List<AddressItem> areas,
                                       List<AddressItem> quarters,
                                       List<AddressItem> streets,
                                       List<AddressHouse> houses )
    {
        Document document = null;
        try
        {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder;
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.newDocument();
            Element rootElement = document.createElement("address");
            rootElement.setAttribute( "time", Long.toString( new Date().getTime() ) );
            //
            Element addressAreaElement = document.createElement( "address_area" );
            Element addressCitiesElement = document.createElement( "address_city" );
            Element addressCountriesElement = document.createElement( "address_country" );
            Element addressQuartersElement = document.createElement( "address_quarter" );
            Element addressStreetsElement = document.createElement( "address_street" );
            Element addressHousesElement = document.createElement( "address_house" );
            //
            Element record;
            for (AddressCountry country : countries)
            {
                record = document.createElement( "record" );
                record.setAttribute( "id", Integer.toString( country.getId() ) );
                record.setAttribute( "title", country.getTitle() );
                addConfigElements( record, document, country.getConfig() );
                addressCountriesElement.appendChild( record );
            }  
            for (AddressCity city : cities)
            {
                record = document.createElement( "record" );
                record.setAttribute( "id", Integer.toString( city.getId() ) );
                record.setAttribute( "title", city.getTitle() );
                record.setAttribute( "countryId", Integer.toString( city.getCountryId())  );
                addConfigElements( record, document, city.getConfig() );
                addressCitiesElement.appendChild( record );
            }
            for (AddressItem area : areas)
            {
                record = document.createElement( "record" );
                record.setAttribute( "id", Integer.toString( area.getId() ) );
                record.setAttribute( "title", area.getTitle() );
                record.setAttribute( "cityId", Integer.toString( area.getCityId() ) );
                addConfigElements( record, document, area.getConfig() );
                addressAreaElement.appendChild( record );
            }
            for (AddressItem quarter : quarters)
            {
                record = document.createElement( "record" );
                record.setAttribute( "id", Integer.toString( quarter.getId() ) );
                record.setAttribute( "title", quarter.getTitle() );
                record.setAttribute( "cityId", Integer.toString( quarter.getCityId() ) );
                addConfigElements( record, document, quarter.getConfig() );
                addressQuartersElement.appendChild( record );
            }
            for (AddressItem street : streets)
            {
                record = document.createElement( "record" );
                record.setAttribute( "id", Integer.toString( street.getId() ) );
                record.setAttribute( "title", street.getTitle() );
                record.setAttribute( "cityId", Integer.toString( street.getCityId() ) );
                addConfigElements( record, document, street.getConfig() );
                addressStreetsElement.appendChild( record );
            }
            for (AddressHouse house : houses)
            {
                record = document.createElement( "record" );
                record.setAttribute( "id", Integer.toString( house.getId() ) );
                record.setAttribute( "areaId", Integer.toString( house.getAreaId() ) );
                record.setAttribute( "quarterId", Integer.toString( house.getQuarterId() ) );
                record.setAttribute( "streetId", Integer.toString( house.getStreetId() ) );
                record.setAttribute( "house", Integer.toString( house.getHouse() ) );
                record.setAttribute( "frac", house.getFrac() );
                record.setAttribute( "postIndex", house.getPostIndex() );
                record.setAttribute( "comment", house.getComment() );
                
                // для совместимости с синхронизатором биллинга
                house.getConfig().put( "s.box.index", house.getPostIndex() );
                
                addConfigElements( record, document, house.getConfig() );
                addressHousesElement.appendChild( record );
            }
            //
            rootElement.appendChild( addressAreaElement );
            rootElement.appendChild( addressCitiesElement);
            rootElement.appendChild( addressCountriesElement );
            rootElement.appendChild( addressQuartersElement );
            rootElement.appendChild( addressStreetsElement );
            rootElement.appendChild( addressHousesElement );
            //
            document.appendChild( rootElement );
        }
        catch( ParserConfigurationException e )
        {
            log.error( "GetUpdatedAddressObjects.buildXMLDocument", e );
        }
        return document;
    }

    private int[] getCitiesId( String selectedCities )
    {
        String[] selectedCitiesId = selectedCities.split( "," );
        int[] citiesId = new int[selectedCitiesId.length];
        try
        {
            for (int i = 0; i < selectedCitiesId.length; i++)
            {
                citiesId[i] = Integer.parseInt( selectedCitiesId[i] );
            }
        }
        catch (Exception ex)
        {
            citiesId = new int[0];
        }
        return citiesId;
    }

    
    private void addConfigElements( Element record, Document document, Map<String, String> params)
    {
        Element configElement;
        for (String key : params.keySet())
        {
            configElement = document.createElement( "config" );
            configElement.setAttribute( "name", key );
            configElement.setAttribute( "value", params.get( key ));
            record.appendChild( configElement );
        }
    }
}