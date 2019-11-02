package ru.bgcrm.plugin.bgbilling.dao;

import static ru.bgcrm.dao.Tables.TABLE_PARAM_ADDRESS;
import static ru.bgcrm.plugin.bgbilling.dao.Tables.TABLE_COMMON_CONTRACT;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import ru.bgcrm.dao.AddressDAO;
import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.CounterDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.Counter;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.plugin.bgbilling.CommonContractConfig;
import ru.bgcrm.plugin.bgbilling.model.CommonContract;
import ru.bgcrm.util.PswdUtil;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SQLUtils;

public class CommonContractDAO
    extends CommonDAO
{
	private CommonContractConfig config = Setup.getSetup()
	                                           .getConfig( CommonContractConfig.class );

	public CommonContractDAO( Connection con )
	{
		super( con );
	}

	public String getContractNumber( int commonContractId, int serviceCode )
	    throws BGException
	{
		CommonContract commonContract = getContractById( commonContractId );

		if( commonContract == null )
		{
			throw new BGMessageException( "Не найден единый договор с кодом:" + commonContractId );
		}

		if( serviceCode < 0 )
		{
			throw new BGMessageException( "Не указан код договора услуги." );
		}

		return config.formatServiceContractNumber( commonContract, serviceCode );
	}

	public String getContractNumber( int commonContractId, String serviceCode )
	    throws BGException
	{
		CommonContract commonContract = getContractById( commonContractId );

		if( commonContract == null )
		{
			throw new BGMessageException( "Не найден единый договор с кодом:" + commonContractId );
		}

		if( Utils.isBlankString( serviceCode ) )
		{
			throw new BGMessageException( "Не указан код договора услуги." );
		}

		return Utils.parseInt( serviceCode, -1 ) >= 0 ? config.formatServiceContractNumber( commonContract, Utils.parseInt( serviceCode ) ) : config.formatServiceContractNumber( commonContract, serviceCode );
	}

	private final String SELECT_QUERY_START =
	"SELECT contract.*, param.* FROM " + TABLE_COMMON_CONTRACT + " AS contract " +
	" LEFT JOIN " + TABLE_PARAM_ADDRESS + " AS param ON contract.id=param.id AND param.param_id=? ";

	public CommonContract getContractById( int id )
	    throws BGException
	{
		CommonContract result = null;

		try
		{
			String query = SELECT_QUERY_START + " WHERE contract.id=?";

			PreparedStatement ps = con.prepareStatement( query );
			ps.setInt( 1, config.getAddressParamId() );
			ps.setInt( 2, id );

			result = getContract( result, ps );
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}

		return result;
	}

	public CommonContract getContractByAreaAndNumber( int areaId, int number )
	    throws BGException
	{
		CommonContract result = null;

		try
		{
			String query = SELECT_QUERY_START + " WHERE contract.area_id=? AND contract.number=?";

			PreparedStatement ps = con.prepareStatement( query );
			ps.setInt( 1, config.getAddressParamId() );
			ps.setInt( 2, areaId );
			ps.setInt( 3, number );

			result = getContract( result, ps );
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}

		return result;
	}

	public CommonContract getContract( CommonContract result, PreparedStatement ps )
	    throws BGException
	{
		try
		{
			ResultSet rs = ps.executeQuery();

			if( rs.next() )
			{
				result = getContractFromRs( rs, "contract.", "param." );
			}
			ps.close();
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}

		return result;
	}

	public List<CommonContract> getContractList( int customerId )
	    throws BGException
	{
		List<CommonContract> result = new ArrayList<CommonContract>();

		try
		{
			String query = SELECT_QUERY_START + " WHERE contract.customer_id=? ORDER BY number";

			PreparedStatement ps = con.prepareStatement( query );
			ps.setInt( 1, config.getAddressParamId() );
			ps.setInt( 2, customerId );

			ResultSet rs = ps.executeQuery();
			while( rs.next() )
			{
				result.add( getContractFromRs( rs, "contract.", "param." ) );
			}
			ps.close();
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}

		return result;
	}

	public CommonContract getContractCommon( int customerId, String contractNumber )
	    throws BGException
	{
		CommonContract result = null;

		for( CommonContract cc : getContractList( customerId ) )
		{
			if( contractNumber != null && contractNumber.startsWith( cc.getFormatedNumber() ) )
			{
				result = cc;
				break;
			}
		}

		return result;
	}

	private static final Object createMutex = new Object();

	public CommonContract createCommonContract( int customerId, ParameterAddressValue address )
	    throws BGException
	{
		CommonContract result = new CommonContract();

		AddressHouse house = new AddressDAO( con ).getAddressHouse( address.getHouseId(), false, false, true );
		if( house == null )
		{
			throw new BGMessageException( "Дом не найден в справочнике: " + address.getHouseId() );
		}

		int cityId = house.getAddressStreet()
		                  .getCityId();

		Integer areaId = config.getCityAreaId( cityId );
		if( areaId == null )
		{
			throw new BGMessageException( "Не определён префикс договоров для города: " + cityId );
		}

		synchronized( createMutex )
		{
			try
			{
				int number = getNextCommonContractNumber( cityId, con );

				PswdUtil pswdGen = new PswdUtil( Setup.getSetup(), CommonContractConfig.CONFIG_PREFIX );

				result.setCustomerId( customerId );
				result.setAreaId( areaId );
				result.setNumber( number );
				result.setPassword( pswdGen.generatePassword() );
				result.setDateFrom( new Date() );

				String query = "INSERT INTO " + TABLE_COMMON_CONTRACT + " (customer_id, area_id, number, date_from, pswd) VALUES (?,?,?,CURDATE(),?)";

				PreparedStatement ps = con.prepareStatement( query, PreparedStatement.RETURN_GENERATED_KEYS );
				ps.setInt( 1, customerId );
				ps.setInt( 2, areaId );
				ps.setInt( 3, number );
				ps.setString( 4, result.getPassword() );
				ps.executeUpdate();

				result.setId( SQLUtils.lastInsertId( ps ) );

				ps.close();

				new CounterDAO( con ).incrementCounter( config.getCounterId( cityId ) );
				new ParamValueDAO( con ).updateParamAddress( result.getId(), config.getAddressParamId(), 0, address );
			}
			catch( SQLException e )
			{
				throw new BGException( e );
			}
		}

		return result;
	}

	public void changeCustomerLink( int commonContractId, int customerId )
	    throws BGMessageException
	{
		try
		{
			String query = "UPDATE " + TABLE_COMMON_CONTRACT + " SET customer_id=? WHERE id=?";
			PreparedStatement ps = con.prepareStatement( query );

			ps.setInt( 1, customerId );
			ps.setInt( 2, commonContractId );

			ps.executeUpdate();
		}
		catch( SQLException e )
		{
			throw new BGMessageException( "Ошибка при смене контрагента единого договора!" );
		}
	}

	public void updateCommonContract( CommonContract contract )
	    throws BGException
	{
		try
		{
			new PswdUtil( Setup.getSetup(), CommonContractConfig.CONFIG_PREFIX ).checkPassword( contract.getPassword() );

			String query = "UPDATE " + TABLE_COMMON_CONTRACT + " SET number = ?, date_from = ?, date_to = ?, pswd = ? WHERE id=?";
			PreparedStatement ps = con.prepareStatement( query );

			ps.setInt( 1, contract.getNumber() );
			ps.setDate( 2, TimeUtils.convertDateToSqlDate( contract.getDateFrom() ) );
			ps.setDate( 3, TimeUtils.convertDateToSqlDate( contract.getDateTo() ) );
			ps.setString( 4, contract.getPassword() );
			ps.setInt( 5, contract.getId() );

			ps.executeUpdate();
		}
		catch( SQLException e )
		{
			if( e instanceof MySQLIntegrityConstraintViolationException )
			{
				throw new BGMessageException( "Единый договор c таким номером уже существует!" );
			}
			throw new BGMessageException( "Ошибка при редактировании единого договора!" );
		}
	}

	public int getNextCommonContractNumber( int cityId, Connection con )
	    throws BGException
	{
		int counterId = config.getCounterId( cityId );
		CounterDAO counterDAO = new CounterDAO( con );

		Counter counter = counterDAO.getCounter( counterId );

		while( true )
		{
			counter.setValue( counter.getValue() + 1 );

			if( getContractByAreaAndNumber( config.getCityAreaId( cityId ), counter.getValue() ) == null )
			{
				break;
			}
		}

		return counter.getValue();
	}

	// TODO: быстрый костыль! индусский фикс! надо будет либо убрать, либо сделать нормально
	public CommonContract createCommonContractWithTitle( int customerId, String title, ParameterAddressValue address )
	    throws BGException
	{
		CommonContract result = new CommonContract();

		AddressHouse house = new AddressDAO( con ).getAddressHouse( address.getHouseId(), false, false, true );
		if( house == null )
		{
			throw new BGMessageException( "Дом не найден в справочнике: " + address.getHouseId() );
		}

		int cityId = house.getAddressStreet()
		                  .getCityId();

		Integer areaId = config.getCityAreaId( cityId );
		if( areaId == null )
		{
			throw new BGMessageException( "Не определён префикс договоров для города: " + cityId );
		}

		synchronized( createMutex )
		{
			try
			{
				int number = 1;
				String query;
				PreparedStatement ps;

				if( title.equals( "" ) )
				{
					number = getNextCommonContractNumber( areaId, con );
				}
				else
				{
					number = Utils.parseInt( title );
					query = "SELECT * FROM " + TABLE_COMMON_CONTRACT + " WHERE number = ?";

					ps = con.prepareStatement( query );
					ps.setInt( 1, number );

					ResultSet rs = ps.executeQuery();

					if( rs.next() )
					{
						ps.close();
						throw new BGException( "Единый договор с таким номером уже существует!" );
					}
				}

				PswdUtil pswdGen = new PswdUtil( Setup.getSetup(), CommonContractConfig.CONFIG_PREFIX );

				result.setCustomerId( customerId );
				result.setAreaId( areaId );
				result.setNumber( number );
				result.setPassword( pswdGen.generatePassword() );
				result.setDateFrom( new Date() );

				query = "INSERT INTO " + TABLE_COMMON_CONTRACT + " (customer_id, area_id, number, date_from, pswd) VALUES (?,?,?,CURDATE(),?)";

				ps = con.prepareStatement( query, PreparedStatement.RETURN_GENERATED_KEYS );
				ps.setInt( 1, customerId );
				ps.setInt( 2, areaId );
				ps.setInt( 3, number );
				ps.setString( 4, result.getPassword() );
				ps.executeUpdate();

				result.setId( SQLUtils.lastInsertId( ps ) );

				ps.close();

				new ParamValueDAO( con ).updateParamAddress( result.getId(), config.getAddressParamId(), 0, address );
			}
			catch( SQLException e )
			{
				throw new BGException( e );
			}
		}

		return result;
	}

	public void deleteCommonContract( int id )
	    throws BGException
	{
		try
		{
			String query = "DELETE FROM " + TABLE_COMMON_CONTRACT + " WHERE id=?";

			PreparedStatement ps = con.prepareStatement( query );
			ps.setInt( 1, id );
			ps.executeUpdate();
			ps.close();

			new ParamValueDAO( con ).deleteParams( "common", config.getAddressParamId() );
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}
	}

	private CommonContract getContractFromRs( ResultSet rs, String prefix, String prefixAddress )
	    throws BGException
	{
		CommonContract contract = new CommonContract();

		try
		{
			contract.setId( rs.getInt( prefix + "id" ) );
			contract.setCustomerId( rs.getInt( prefix + "customer_id" ) );
			contract.setAreaId( rs.getInt( prefix + "area_id" ) );
			contract.setNumber( rs.getInt( prefix + "number" ) );
			contract.setAddress( ParamValueDAO.getParameterAddressValueFromRs( rs, prefixAddress ) );
			contract.setPassword( rs.getString( prefix + "pswd" ) );
			contract.setDateFrom( rs.getDate( prefix + "date_from" ) );
			contract.setDateTo( rs.getDate( prefix + "date_to" ) );
			config.formatCommonContractNumber( contract );
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}

		return contract;
	}
}
