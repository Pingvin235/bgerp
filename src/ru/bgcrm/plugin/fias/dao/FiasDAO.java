package ru.bgcrm.plugin.fias.dao;

import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_CITY;
import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_COUNTRY;
import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_HOUSE;
import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_STREET;
import static ru.bgcrm.dao.Tables.TABLE_FIAS_HOUSE_DATA;
import static ru.bgcrm.dao.Tables.TABLE_FIAS_HOUSE_INTERVAL;
import static ru.bgcrm.dao.Tables.TABLE_FIAS_STREET;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ru.bgcrm.dao.AddressDAO;
import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.param.address.AddressCity;
import ru.bgcrm.model.param.address.AddressCountry;
import ru.bgcrm.model.param.address.AddressItem;
import ru.bgcrm.plugin.fias.model.CrmHouse;
import ru.bgcrm.plugin.fias.model.FiasHouse;
import ru.bgcrm.plugin.fias.model.FiasStreet;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.PreparedDelay;

public class FiasDAO
	extends CommonDAO
{
	public FiasDAO( Connection con )
	{
		super( con );
	}

	public void searchFiasStreetByTerm( SearchResult<FiasStreet> searchResult, String titleTerm, Integer cityId, boolean hasLink )
		throws SQLException
	{
		if( searchResult != null )
		{
			Page page = searchResult.getPage();
			List<FiasStreet> list = searchResult.getList();

			PreparedDelay ps = new PreparedDelay( con );

			StringBuilder query = new StringBuilder();
			query.append( SQL_SELECT_COUNT_ROWS );
			query.append( "* FROM " + TABLE_FIAS_STREET + " AS fStreet" );
			query.append( " LEFT JOIN " + TABLE_ADDRESS_CITY + " as city" );
			query.append( " ON fStreet.crm_city_id = city.id" );
			query.append( " LEFT JOIN " + TABLE_ADDRESS_STREET + " as aStreet" );
			query.append( " ON fStreet.crm_street_id = aStreet.id" );

			if( hasLink )
			{
				query.append( " WHERE crm_street_id IS NOT NULL" );
			}
			else
			{
				query.append( " WHERE crm_street_id IS NULL" );
			}

			if( Utils.notBlankString( titleTerm ) )
			{
				query.append( " AND street_title like '" );
				query.append( getLikePattern( titleTerm, "subs" ) );
				query.append( "'" );
			}

			if( cityId > 0 )
			{
				query.append( " AND crm_city_id = " + cityId );
			}

			query.append( " ORDER BY crm_city_id, street_title" );
			query.append( getPageLimit( page ) );

			ps.addQuery( query.toString() );
			ResultSet rs = ps.executeQuery();

			while( rs.next() )
			{
				FiasStreet street = getStreetFromRs( rs );
				list.add( street );
			}
			if( page != null )
			{
				page.setRecordCount( getFoundRows( ps.getPrepared() ) );
			}
			ps.close();
		}
	}

	public void searchSimilarStreet( SearchResult<FiasStreet> searchResult, String title, Integer cityId )
		throws SQLException
	{
		if( searchResult != null )
		{
			Page page = searchResult.getPage();
			List<FiasStreet> list = searchResult.getList();

			PreparedDelay ps = new PreparedDelay( con );

			StringBuilder query = new StringBuilder();
			query.append( SQL_SELECT_COUNT_ROWS );
			query.append( "* FROM " + TABLE_FIAS_STREET + " AS fStreet" );
			query.append( " LEFT JOIN " + TABLE_ADDRESS_CITY + " as city" );
			query.append( " ON fStreet.crm_city_id = city.id" );
			query.append( " LEFT JOIN " + TABLE_ADDRESS_STREET + " as aStreet" );
			query.append( " ON fStreet.crm_street_id = aStreet.id" );

			query.append( " WHERE crm_street_id IS NULL" );

			if( Utils.notBlankString( title ) )
			{
				query.append( " AND '" + title + "' like" );
				query.append( " CONCAT('%',street_title,'%')" );
			}

			if( cityId > 0 )
			{
				query.append( " AND crm_city_id = " + cityId );
			}

			query.append( " ORDER BY crm_city_id, street_title" );
			query.append( getPageLimit( page ) );

			ps.addQuery( query.toString() );
			ResultSet rs = ps.executeQuery();

			while( rs.next() )
			{
				FiasStreet street = getStreetFromRs( rs );
				list.add( street );
			}
			if( page != null )
			{
				page.setRecordCount( getFoundRows( ps.getPrepared() ) );
			}
			ps.close();
		}
	}

	public void addStreetLink( Integer crmStreetId, String fiasStreetId )
		throws SQLException
	{
		if( crmStreetId > 0 && Utils.notBlankString( fiasStreetId ) )
		{
			PreparedStatement ps = null;
			StringBuilder query = new StringBuilder();

			query.append( "UPDATE " );
			query.append( TABLE_FIAS_STREET );
			query.append( " SET crm_street_id=?" );
			query.append( " WHERE aoguid=?" );
			ps = con.prepareStatement( query.toString(), PreparedStatement.RETURN_GENERATED_KEYS );
			ps.setInt( 1, crmStreetId );
			ps.setString( 2, fiasStreetId );
			ps.executeUpdate();
			ps.close();
		}
	}

	public void addHouseLinks( String fiasStreetId, Integer crmCityId )
		throws SQLException
	{
		List<IdTitle> houseList = getNotLinkCrmHouseList( fiasStreetId );

		for( IdTitle house : houseList )
		{
			Integer postalCode = getPostalCodeByHouse( fiasStreetId, house.getTitle() );
			if( postalCode > 0 )
			{
				addHouseLink( fiasStreetId, crmCityId, house.getId(), postalCode );
			}
		}
	}

	private void addHouseLink( String fiasStreetId, Integer crmCityId, Integer crmHouseId, Integer postalCode )
		throws SQLException
	{
		PreparedStatement ps = null;

		StringBuilder query = new StringBuilder();

		query = new StringBuilder();
		query.append( SQL_INSERT );
		query.append( TABLE_FIAS_HOUSE_DATA );
		query.append( " (aoguid,crm_city_id,crm_house_id,postal_code,last_update_date)" );
		query.append( " VALUES (?,?,?,?,?)" );

		ps = con.prepareStatement( query.toString(), PreparedStatement.RETURN_GENERATED_KEYS );
		ps.setString( 1, fiasStreetId );
		ps.setInt( 2, crmCityId );
		ps.setInt( 3, crmHouseId );
		ps.setInt( 4, postalCode );
		ps.setDate( 5, new Date( new java.util.Date().getTime() ) );

		ps.executeUpdate();
	}

	private List<IdTitle> getNotLinkCrmHouseList( String fiasStreetId )
		throws SQLException
	{
		PreparedStatement ps = null;

		StringBuilder query = new StringBuilder();
		query.append( SQL_SELECT_COUNT_ROWS );
		query.append( " id,house FROM " + TABLE_ADDRESS_HOUSE );
		query.append( " WHERE street_id=(SELECT crm_street_id FROM " + TABLE_FIAS_STREET + " WHERE aoguid=?)" );
		query.append( " AND id NOT IN (SELECT crm_house_id FROM " + TABLE_FIAS_HOUSE_DATA + ")" );

		ps = con.prepareStatement( query.toString(), PreparedStatement.RETURN_GENERATED_KEYS );
		ps.setString( 1, fiasStreetId );
		ResultSet rs = ps.executeQuery();

		List<IdTitle> houseList = new ArrayList<IdTitle>();
		while( rs.next() )
		{
			houseList.add( new IdTitle( rs.getInt( "id" ), rs.getString( "house" ) ) );
		}
		ps.close();
		rs.close();

		return houseList;
	}

	public List<Integer> getLinkHouseIdList( String fiasStreetId )
		throws SQLException
	{
		PreparedStatement ps = null;

		StringBuilder query = new StringBuilder();
		query.append( SQL_SELECT_COUNT_ROWS );
		query.append( " id FROM " + TABLE_FIAS_HOUSE_DATA );
		query.append( " WHERE aoguid=?" );

		ps = con.prepareStatement( query.toString(), PreparedStatement.RETURN_GENERATED_KEYS );
		ps.setString( 1, fiasStreetId );
		ResultSet rs = ps.executeQuery();

		List<Integer> houseIdList = new ArrayList<Integer>();
		while( rs.next() )
		{
			houseIdList.add( rs.getInt( "id" ) );
		}
		ps.close();
		rs.close();

		return houseIdList;
	}

	public List<CrmHouse> getNotLinkHouseList( SearchResult<CrmHouse> searchResult, String fiasStreetId, String indexTerm, Integer streetSide )
		throws SQLException
	{
		Page page = searchResult.getPage();
		List<CrmHouse> houseList = searchResult.getList();

		PreparedDelay ps = new PreparedDelay( con );

		StringBuilder query = new StringBuilder();
		query.append( SQL_SELECT_COUNT_ROWS );
		query.append( " * FROM " + TABLE_ADDRESS_HOUSE );
		query.append( " WHERE street_id=(SELECT crm_street_id FROM " + TABLE_FIAS_STREET + " WHERE aoguid=?)" );

		query.append( " AND id NOT IN (SELECT crm_house_id FROM " + TABLE_FIAS_HOUSE_DATA + " WHERE aoguid=?)" );

		if( Utils.notBlankString( indexTerm ) )
		{
			query.append( " AND house LIKE ?" );
		}

		if( streetSide != 1 )
		{
			query.append( " AND house mod 2 + 2 = ? " );
		}

		query.append( " ORDER BY post_index" );
		query.append( getPageLimit( page ) );

		ps.addQuery( query.toString() );
		ps.addString( fiasStreetId );
		ps.addString( fiasStreetId );
		if( Utils.notBlankString( indexTerm ) )
		{
			ps.addString( getLikePattern( indexTerm, "subs" ) );
		}
		if( streetSide != 1 )
		{
			ps.addInt( streetSide );
		}
		ResultSet rs = ps.executeQuery();

		while( rs.next() )
		{
			CrmHouse house = new CrmHouse();
			house.setId( rs.getInt( "id" ) );
			house.setStreetId( rs.getInt( "street_id" ) );
			house.setNumber( rs.getString( "house" ) );
			house.setFrac( rs.getString( "frac" ) );
			house.setComment( rs.getString( "comment" ) );
			house.setPostalCode( rs.getString( "post_index" ) );

			houseList.add( house );
		}
		rs.close();

		if( page != null )
		{
			page.setRecordCount( getFoundRows( ps.getPrepared() ) );
		}
		ps.close();

		return houseList;
	}

	public String getFiasStreetPostalCode( String fiasStreetId )
		throws SQLException
	{
		PreparedStatement ps = null;

		StringBuilder query = new StringBuilder();
		query.append( SQL_SELECT_COUNT_ROWS );
		query.append( " postal_code FROM " + TABLE_FIAS_STREET );
		query.append( " WHERE aoguid=?" );

		ps = con.prepareStatement( query.toString(), PreparedStatement.RETURN_GENERATED_KEYS );
		ps.setString( 1, fiasStreetId );

		ResultSet rs = ps.executeQuery();

		while( rs.next() )
		{
			return rs.getString( "postal_code" );
		}
		ps.close();
		rs.close();

		return "";
	}

	public List<String> recommendedPostalCodeList( String fiasStreetId )
		throws SQLException
	{
		PreparedStatement ps = null;

		StringBuilder query = new StringBuilder();
		query.append( SQL_SELECT_COUNT_ROWS );
		query.append( " DISTINCT(postal_code) FROM " + TABLE_FIAS_HOUSE_DATA );
		query.append( " WHERE aoguid=?" );

		ps = con.prepareStatement( query.toString(), PreparedStatement.RETURN_GENERATED_KEYS );
		ps.setString( 1, fiasStreetId );

		ResultSet rs = ps.executeQuery();

		List<String> postalCodeList = new ArrayList<String>();
		while( rs.next() )
		{
			postalCodeList.add( rs.getString( "postal_code" ) );
		}
		ps.close();
		rs.close();

		return postalCodeList;
	}

	private Integer getPostalCodeByHouse( String fiasStreetId, String houseNumber )
		throws SQLException
	{
		//ищем в интревалах
		PreparedStatement ps = null;

		StringBuilder query = new StringBuilder();
		query.append( SQL_SELECT_COUNT_ROWS );
		query.append( " postal_code FROM " + TABLE_FIAS_HOUSE_INTERVAL );
		query.append( " WHERE aoguid=?" );
		query.append( " AND start_index<=? AND end_index>=? AND (status=(? mod 2 + 2) OR status=1)" );

		ps = con.prepareStatement( query.toString(), PreparedStatement.RETURN_GENERATED_KEYS );
		ps.setString( 1, fiasStreetId );
		ps.setString( 2, houseNumber );
		ps.setString( 3, houseNumber );
		ps.setString( 4, houseNumber );

		ResultSet rs = ps.executeQuery();

		while( rs.next() )
		{
			return rs.getInt( "postal_code" );
		}
		ps.close();
		rs.close();

		//если не нашли то подставялем postal_code улицы
		query = new StringBuilder();
		query.append( SQL_SELECT_COUNT_ROWS );
		query.append( " postal_code FROM " + TABLE_FIAS_STREET );
		query.append( " WHERE aoguid=?" );

		ps = con.prepareStatement( query.toString(), PreparedStatement.RETURN_GENERATED_KEYS );
		ps.setString( 1, fiasStreetId );

		rs = ps.executeQuery();

		while( rs.next() )
		{
			return rs.getInt( "postal_code" );
		}

		return 0;
	}

	public static FiasStreet getStreetFromRs( ResultSet rs )
		throws SQLException
	{
		FiasStreet street = new FiasStreet();

		street.setId( rs.getString( "aoguid" ) );
		street.setCrmCityId( rs.getInt( "crm_city_id" ) );
		street.setCrmCitytitle( rs.getString( "city.title" ) );
		street.setPostalCode( rs.getString( "postal_code" ) );
		street.setShortName( rs.getString( "street_short_name" ) );
		street.setTitle( rs.getString( "street_title" ) );
		street.setCrmStreetId( rs.getInt( "crm_street_id" ) );
		street.setCrmStreetTitle( rs.getString( "aStreet.title" ) );

		return street;
	}

	public void searchAddressStreetByTerm( SearchResult<AddressItem> searchResult, int cityId, String titleTerm )
		throws SQLException
	{
		if( searchResult != null )
		{
			Page page = searchResult.getPage();
			List<AddressItem> result = searchResult.getList();

			int index = 1;
			ResultSet rs = null;
			PreparedStatement ps = null;
			StringBuilder query = new StringBuilder();

			query.append( "SELECT SQL_CALC_FOUND_ROWS * FROM " );
			query.append( TABLE_ADDRESS_STREET );
			query.append( " AS item" );
			query.append( " LEFT JOIN " );
			query.append( TABLE_ADDRESS_CITY );
			query.append( " AS city ON item.city_id=city.id" );
			query.append( " LEFT JOIN " );
			query.append( TABLE_ADDRESS_COUNTRY );
			query.append( " AS country ON city.country_id=country.id" );
			query.append( " WHERE 1=1" );
			if( cityId > 0 )
			{
				query.append( " AND city_id=?" );
			}
			query.append( " AND " );
			query.append( "item.title LIKE ?" );
			query.append( " AND item.id NOT IN " );
			query.append( " (SELECT DISTINCT(crm_street_id) FROM" );
			query.append( TABLE_FIAS_STREET );
			query.append( " WHERE crm_street_id IS NOT NULL)" );
			query.append( " ORDER BY item.title" );
			query.append( getPageLimit( page ) );

			ps = con.prepareStatement( query.toString() );
			if( cityId > 0 )
			{
				ps.setInt( index++, cityId );
			}

			ps.setString( index++, titleTerm );

			rs = ps.executeQuery();
			while( rs.next() )
			{
				AddressItem addressItem = AddressDAO.getAddressItemFromRs( rs, "item." );
				AddressCity addressCity = AddressDAO.getAddressCityFromRs( rs, "city." );
				addressItem.setAddressCity( addressCity );
				if( addressCity != null )
				{
					AddressCountry addressCountry = AddressDAO.getAddressCountryFromRs( rs, "country." );
					addressCity.setAddressCountry( addressCountry );
				}
				result.add( addressItem );
			}
			if( page != null )
			{
				page.setRecordCount( getFoundRows( ps ) );
			}
			ps.close();
		}
	}

	public void searchFiasHouseByTerm( SearchResult<FiasHouse> searchResult, String streetId, String indexTerm, Integer cityId, Integer streetSide )
		throws SQLException
	{
		if( searchResult != null )
		{
			Page page = searchResult.getPage();
			List<FiasHouse> list = searchResult.getList();

			PreparedDelay ps = new PreparedDelay( con );

			StringBuilder query = new StringBuilder();
			query.append( SQL_SELECT_COUNT_ROWS );
			query.append( " fd.id,fd.postal_code,fs.street_title,ah.house,ah.frac,fd.crm_house_id,ah.post_index FROM " + TABLE_FIAS_HOUSE_DATA + " AS fd" );
			query.append( " LEFT JOIN " + TABLE_FIAS_STREET + " as fs" );
			query.append( " ON fd.aoguid = fs.aoguid" );
			query.append( " LEFT JOIN " + TABLE_ADDRESS_HOUSE + " as ah" );
			query.append( " ON fd.crm_house_id = ah.id" );

			query.append( " WHERE fs.crm_city_id = ?" );

			query.append( " AND fs.aoguid =? " );

			if( streetSide != 1 )
			{
				query.append( " AND ah.house mod 2 + 2 = ? " );
			}

			if( Utils.notBlankString( indexTerm ) )
			{
				query.append( " AND ah.house LIKE '" );
				query.append( getLikePattern( indexTerm, "subs" ) );
				query.append( "'" );
			}

			query.append( " ORDER BY ah.post_index-fd.postal_code, ah.house" );
			query.append( getPageLimit( page ) );

			ps.addQuery( query.toString() );
			ps.addInt( cityId );
			ps.addString( streetId );
			if( streetSide != 1 )
			{
				ps.addInt( streetSide );
			}
			ResultSet rs = ps.executeQuery();

			while( rs.next() )
			{
				FiasHouse house = getHouseFromRs( rs );
				list.add( house );
			}
			if( page != null )
			{
				page.setRecordCount( getFoundRows( ps.getPrepared() ) );
			}
			ps.close();
		}
	}

	public static FiasHouse getHouseFromRs( ResultSet rs )
		throws SQLException
	{
		FiasHouse house = new FiasHouse();

		house.setId( rs.getInt( "id" ) );
		house.setFiasPostalCode( rs.getString( "postal_code" ) );
		house.setCrmPostalCode( rs.getString( "post_index" ) );
		house.setStreetTitle( rs.getString( "street_title" ) );
		house.setHouseNum( rs.getString( "house" ) );
		house.setHouseFrac( rs.getString( "frac" ) );

		return house;
	}

	public void copyPostalCode( List<Integer> fiasHouseIds )
		throws SQLException
	{
		for( Integer houseId : fiasHouseIds )
		{
			PreparedStatement ps = null;
			StringBuilder query = new StringBuilder();

			//update address_house as ah left join fias_house_data as fhd on ah.id = fhd.crm_house_id set post_index = fhd.postal_code where fhd.crm_house_id = 51

			query.append( SQL_UPDATE );
			query.append( TABLE_ADDRESS_HOUSE + " AS ah " );
			query.append( " LEFT JOIN " + TABLE_FIAS_HOUSE_DATA + " AS fhd " );
			query.append( " ON ah.id = fhd.crm_house_id " );
			query.append( " SET post_index=fhd.postal_code " );
			query.append( " WHERE fhd.id=?" );
			ps = con.prepareStatement( query.toString() );

			ps.setInt( 1, houseId );

			ps.executeUpdate();
		}
	}

	public void copyStreetTitle( String streetId )
		throws SQLException
	{
		PreparedStatement ps = null;
		StringBuilder query = new StringBuilder();

		query.append( SQL_UPDATE );
		query.append( TABLE_ADDRESS_STREET + " AS aStr " );

		query.append( " LEFT JOIN " + TABLE_FIAS_STREET + " AS fs " );
		query.append( " ON aStr.id = fs.crm_street_id " );

		query.append( " LEFT JOIN " + TABLE_FIAS_HOUSE_DATA + " AS fhd " );
		query.append( " ON fhd.aoguid = fs.aoguid " );

		query.append( " SET title= CASE fs.street_short_name WHEN 'ул' THEN fs.street_title  ELSE CONCAT(fs.street_short_name,' ',fs.street_title) END " );
		query.append( " WHERE fs.aoguid=?" );
		ps = con.prepareStatement( query.toString() );

		ps.setString( 1, streetId );

		ps.executeUpdate();
	}

	public void manualSetPostalCode( Set<Integer> crmHouseIds, String postalCode )
		throws SQLException
	{
		PreparedStatement ps = null;
		StringBuilder query = new StringBuilder();

		query.append( SQL_UPDATE );
		query.append( TABLE_ADDRESS_HOUSE );

		query.append( " SET post_index=?" );
		query.append( " WHERE id IN (" + crmHouseIds.toString().substring( 1, crmHouseIds.toString().length() - 1 ) + ")" );
		ps = con.prepareStatement( query.toString() );

		ps.setString( 1, postalCode );

		ps.executeUpdate();
	}

	public void delStreetLink( String fiasStreetId )
		throws SQLException
	{
		PreparedStatement ps = null;
		StringBuilder query = new StringBuilder();

		query.append( SQL_UPDATE );
		query.append( TABLE_FIAS_STREET );

		query.append( " SET crm_street_id=null" );
		query.append( " WHERE aoguid=?" );
		ps = con.prepareStatement( query.toString() );

		ps.setString( 1, fiasStreetId );

		ps.executeUpdate();
	}

	public void delHouseLink( String fiasHouseId )
		throws SQLException
	{
		PreparedStatement ps = null;
		StringBuilder query = new StringBuilder();

		query.append( SQL_DELETE );
		query.append( TABLE_FIAS_HOUSE_DATA );

		query.append( " WHERE id=?" );
		ps = con.prepareStatement( query.toString() );

		ps.setString( 1, fiasHouseId );

		ps.executeUpdate();
	}
}
