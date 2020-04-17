package ru.bgcrm.dao;

import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_HOUSE;
import static ru.bgcrm.dao.Tables.TABLE_CUSTOMER;
import static ru.bgcrm.dao.Tables.TABLE_CUSTOMER_GROUP;
import static ru.bgcrm.dao.Tables.TABLE_CUSTOMER_LINK;
import static ru.bgcrm.dao.Tables.TABLE_CUSTOMER_LOG;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_ADDRESS;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_PHONE_ITEM;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_PREF;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_TEXT;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.event.client.CustomerTitleChangedEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Customer;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.param.ParameterSearchedObject;
import ru.bgcrm.model.param.Pattern;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.struts.form.Response;
import ru.bgcrm.util.PatternFormatter;
import ru.bgcrm.util.PatternFormatter.PatternItemProcessor;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.PreparedDelay;

public class CustomerDAO
    extends CommonDAO
{
	private boolean history;
	private int userId;

	public CustomerDAO( Connection con )
	{
		super( con );
	}

	public CustomerDAO( Connection con, boolean history, int userId )
	{
		super( con );
		this.history = history;
		this.userId = userId;
	}

	/**
	 * Выбирает контрагентов по названию.
	 * @param searchResult
	 * @param title
	 * @throws SQLException
	 */
	public void searchCustomerList( SearchResult<Customer> searchResult, String title )
	    throws BGException
	{
		if( searchResult != null )
		{
			Page page = searchResult.getPage();
			List<Customer> list = searchResult.getList();

			StringBuilder selectPart = new StringBuilder();
			StringBuilder joinPart = new StringBuilder();

			String referenceTemplate = addCustomerReferenceQuery( selectPart, joinPart );

			PreparedDelay ps = new PreparedDelay( con );

			StringBuilder query = new StringBuilder();
			query.append( SQL_SELECT_COUNT_ROWS );
			query.append( selectPart );
			query.append( "customer.* FROM " + TABLE_CUSTOMER + " AS customer" );
			query.append( joinPart );
			if( Utils.notBlankString( title ) )
			{
				query.append( " WHERE title LIKE ? " );
				ps.addString( title );
			}
			query.append( " ORDER BY title" );
			query.append( getPageLimit( page ) );

			if( log.isDebugEnabled() )
			{
				log.debug( query.toString() );
			}

			ps.addQuery( query.toString() );

			extractCustomersWithRef( page, list, referenceTemplate, ps );
		}
	}

	/**
	 * Выбирает контрагентов по группам.
	 * @param searchResult
	 * @param groupIds - группы.
	 * @throws SQLException
	 */
	public void searchCustomerList( SearchResult<Customer> searchResult, Set<Integer> groupIds )
	    throws BGException
	{
		Page page = searchResult.getPage();
		List<Customer> list = searchResult.getList();

		StringBuilder selectPart = new StringBuilder();
		StringBuilder joinPart = new StringBuilder();

		String referenceTemplate = addCustomerReferenceQuery( selectPart, joinPart );

		if( CollectionUtils.isNotEmpty( groupIds ) )
		{
			joinPart.append( " INNER JOIN " + TABLE_CUSTOMER_GROUP + " AS customer_group ON customer.id=customer_group.customer_id " +
			                 "AND customer_group.group_id IN (" + Utils.toString( groupIds ) + ") " );

			PreparedDelay ps = new PreparedDelay( con );

			StringBuilder query = new StringBuilder();
			query.append( SQL_SELECT_COUNT_ROWS + " DISTINCT " );
			query.append( selectPart );
			query.append( "customer.* FROM " + TABLE_CUSTOMER + " AS customer" );
			query.append( joinPart );
			query.append( " ORDER BY title" );
			query.append( getPageLimit( page ) );

			if( log.isDebugEnabled() )
			{
				log.debug( query.toString() );
			}

			ps.addQuery( query.toString() );

			extractCustomersWithRef( page, list, referenceTemplate, ps );
		}

		PreparedDelay ps = new PreparedDelay( con );

		StringBuilder query = new StringBuilder();
		query.append( SQL_SELECT_COUNT_ROWS + " DISTINCT " );
		query.append( selectPart );
		query.append( "customer.* FROM " + TABLE_CUSTOMER + " AS customer" );
		query.append( joinPart );
		query.append( " ORDER BY title" );
		query.append( getPageLimit( page ) );

		if( log.isDebugEnabled() )
		{
			log.debug( query.toString() );
		}

		ps.addQuery( query.toString() );

		extractCustomersWithRef( page, list, referenceTemplate, ps );
	}

	public Customer extractCustomerWithRef( int customerId )
	    throws BGException
	{
		try
		{
			int index = 1;
			StringBuilder query = new StringBuilder();
			StringBuilder selectPart = new StringBuilder();
			StringBuilder joinPart = new StringBuilder();

			String referenceTemplate = addCustomerReferenceQuery( selectPart, joinPart );

			query.append( SQL_SELECT );
			query.append( selectPart );
			query.append( " customer.* " );
			query.append( SQL_FROM );
			query.append( TABLE_CUSTOMER );
			query.append( " AS customer" );
			query.append( joinPart );
			query.append( " WHERE customer.id = ? " );

			PreparedStatement ps = con.prepareStatement( query.toString() );
			ps.setInt( index++, customerId );

			final ResultSet rs = ps.executeQuery();

			while( rs.next() )
			{
				if( Utils.notBlankString( referenceTemplate ) )
				{
					Customer customer = getCustomerFromRs( rs, "" );

					String reference = PatternFormatter.processPattern( referenceTemplate, new PatternItemProcessor()
					{
						@Override
						public String processPatternItem( String variable )
						{
							String value = "";
							try
							{
								if( variable.startsWith( "param:" ) )
								{
									value = rs.getString( variable.replace( ':', '_' ) + "_val" );
								}
							}
							catch( Exception e )
							{
								log.error( e.getMessage(), e );
							}
							return value;
						}
					} );

					ps.close();
					customer.setReference( reference );
					return customer;
				}
			}
			ps.close();
			return null;
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}
	}

	private void extractCustomersWithRef( Page page, List<Customer> list, String referenceTemplate, PreparedDelay ps )
	    throws BGException
	{
		try
		{
			final ResultSet rs = ps.executeQuery();
			while( rs.next() )
			{
				Customer customer = getCustomerFromRs( rs, "" );
				list.add( customer );

				if( Utils.notBlankString( referenceTemplate ) )
				{
					String reference = PatternFormatter.processPattern( referenceTemplate, new PatternItemProcessor()
					{
						@Override
						public String processPatternItem( String variable )
						{
							String value = "";
							try
							{
								if( variable.startsWith( "param:" ) )
								{
									value = rs.getString( variable.replace( ':', '_' ) + "_val" );
								}
							}
							catch( Exception e )
							{
								log.error( e.getMessage(), e );
							}
							return value;
						}
					} );
					customer.setReference( reference );
				}
			}
			if( page != null )
			{
				page.setRecordCount( getFoundRows( ps.getPrepared() ) );
			}
			ps.close();
		}
		catch( SQLException ex )
		{
			throw new BGException( ex );
		}
	}

	private String addCustomerReferenceQuery( final StringBuilder selectPart, final StringBuilder joinPart )
	{
		String referenceTemplate = Setup.getSetup().get( "customer.reference.pattern", "" );
		if( Utils.notBlankString( referenceTemplate ) )
		{
			PatternFormatter.processPattern( referenceTemplate, new PatternItemProcessor()
			{
				@Override
				public String processPatternItem( String variable )
				{
					if( variable.startsWith( "param:" ) )
					{
						ParamValueDAO.paramSelectQuery( variable, "customer.id", selectPart, joinPart, true );
						selectPart.append( ", " );
					}
					return "";
				}
			} );
		}
		return referenceTemplate;
	}

	/**
	 * Выбирает контрагентов по параметру типа E-Mail.
	 * @param searchResult
	 * @param emailParamIdList
	 * @param email E-Mail, поиск идёт по точному совпадению и совпадению домена 
	 * @throws SQLException
	 */
	public void searchCustomerListByEmail( SearchResult<ParameterSearchedObject<Customer>> searchResult,
	                                       List<Integer> emailParamIdList, String email )
	    throws BGException
	{
		new ParamValueSearchDAO(con).searchObjectListByEmail(
				TABLE_CUSTOMER, rs -> getCustomerFromRs(rs, "c."), 
				searchResult, emailParamIdList, email);
	}
	

	/**
	 * Выбирает контрагентов по строковому параметру.
	 * @param searchResult
	 * @param textParamIdList
	 * @param value
	 * @throws SQLException
	 */
	public void searchCustomerListByText( SearchResult<Customer> searchResult,
	                                      List<Integer> textParamIdList,
	                                      String value )
	    throws SQLException
	{
		if( searchResult != null )
		{
			Page page = searchResult.getPage();
			List<Customer> list = searchResult.getList();

			StringBuilder query = new StringBuilder();
			String ids = Utils.toString( textParamIdList );

			query.append( SQL_SELECT );
			query.append( "DISTINCT c.*" );
			query.append( SQL_FROM );
			query.append( TABLE_CUSTOMER );
			query.append( "AS c " );
			query.append( SQL_INNER_JOIN );
			query.append( TABLE_PARAM_TEXT );
			query.append( "AS param ON c.id=param.id AND " );
			query.append( "param.value LIKE ?" );

			if( Utils.notBlankString( ids ) )
			{
				query.append( " AND param.param_id IN (" );
				query.append( ids );
				query.append( ")" );
			}
			query.append( SQL_ORDER_BY );
			query.append( "c.title" );
			query.append( getPageLimit( page ) );

			PreparedStatement ps = con.prepareStatement( query.toString() );
			ps.setString( 1, value );

			ResultSet rs = ps.executeQuery();

			while( rs.next() )
			{
				list.add( getCustomerFromRs( rs, "" ) );
			}
			if( page != null )
			{
				page.setRecordCount( getFoundRows( ps ) );
			}
			ps.close();
		}
	}

	/**
	 * Выбирает контрагентов по адресному параметру.
	 * @param searchResult
	 * @param addressParamIdList
	 * @param streetId
	 * @param house
	 * @param houseFlat
	 * @param houseRoom
	 * @throws SQLException
	 */
	public void searchCustomerListByAddress( SearchResult<ParameterSearchedObject<Customer>> searchResult,
	                                         List<Integer> addressParamIdList,
	                                         int streetId, String house, String houseFlat, String houseRoom )
	    throws SQLException
	{
		if( searchResult != null )
		{
			Page page = searchResult.getPage();
			List<ParameterSearchedObject<Customer>> list = searchResult.getList();

			PreparedDelay ps = new PreparedDelay( con );
			String ids = Utils.toString( addressParamIdList );

			AddressHouse searchParams = AddressHouse.extractHouseAndFrac( house );

			int number = searchParams.getHouse();
			String frac = searchParams.getFrac();

			ps.addQuery( SQL_SELECT_COUNT_ROWS );
			ps.addQuery( "DISTINCT param.param_id, param.value, c.* " );
			ps.addQuery( SQL_FROM );
			ps.addQuery( TABLE_CUSTOMER );
			ps.addQuery( "AS c " );

			ps.addQuery( SQL_INNER_JOIN );
			ps.addQuery( TABLE_PARAM_ADDRESS );
			ps.addQuery( "AS param ON c.id=param.id AND param.param_id IN (" );
			ps.addQuery( ids );
			ps.addQuery( ")" );
			if( Utils.notBlankString( houseFlat ) )
			{
				ps.addQuery( " AND param.flat=?" );
				ps.addString( houseFlat );
			}
			if( Utils.notBlankString( houseRoom ) )
			{
				ps.addQuery( " AND param.room=?" );
				ps.addString( houseRoom );
			}

			ps.addQuery( SQL_INNER_JOIN );
			ps.addQuery( TABLE_ADDRESS_HOUSE );
			ps.addQuery( "AS house ON param.house_id=house.id" );
			ps.addQuery( " AND house.street_id=?" );
			ps.addInt( streetId );
			if( number > 0 )
			{
				ps.addQuery( " AND house.house=?" );
				ps.addInt( number );
			}
			if( Utils.notBlankString( frac ) )
			{
				ps.addQuery( " AND house.frac=?" );
				ps.addString( frac );
			}

			ps.addQuery( SQL_LEFT_JOIN );
			ps.addQuery( TABLE_PARAM_PREF );
			ps.addQuery( "AS pref ON param.param_id=pref.id " );

			ps.addQuery( SQL_ORDER_BY );
			ps.addQuery( "c.title" );
			ps.addQuery( getPageLimit( page ) );

			ResultSet rs = ps.executeQuery();
			while( rs.next() )
			{
				list.add( new ParameterSearchedObject<Customer>( getCustomerFromRs( rs, "c." ), rs.getInt( 1 ), rs.getString( 2 ) ) );
			}

			setRecordCount( page, ps.getPrepared() );
			ps.close();
		}
	}

	/**
	 * Выбирает контрагентов по адресному параметру.
	 * @param searchResult
	 * @param addressParamIdList
	 * @param houseId код дома
	 * @param houseFlat квартира
	 * @param houseRoom комната
	 * @throws SQLException
	 */
	public void searchCustomerListByAddress( SearchResult<ParameterSearchedObject<Customer>> searchResult,
	                                         List<Integer> addressParamIdList,
	                                         int houseId, String houseFlat, String houseRoom )
	    throws BGException
	{
		if( searchResult != null )
		{
			Page page = searchResult.getPage();
			List<ParameterSearchedObject<Customer>> list = searchResult.getList();

			PreparedDelay ps = new PreparedDelay( con );
			String ids = Utils.toString( addressParamIdList );

			ps.addQuery( SQL_SELECT_COUNT_ROWS );
			ps.addQuery( "DISTINCT param.param_id, param.value, c.* " );
			ps.addQuery( SQL_FROM );
			ps.addQuery( TABLE_CUSTOMER );
			ps.addQuery( "AS c " );

			ps.addQuery( SQL_INNER_JOIN );
			ps.addQuery( TABLE_PARAM_ADDRESS );
			ps.addQuery( "AS param ON c.id=param.id AND param.param_id IN (" );
			ps.addQuery( ids );
			ps.addQuery( ")" );

			ps.addQuery( " AND param.house_id=?" );
			ps.addInt( houseId );

			if( Utils.notBlankString( houseFlat ) )
			{
				ps.addQuery( " AND param.flat=?" );
				ps.addString( houseFlat );
			}
			if( Utils.notBlankString( houseRoom ) )
			{
				ps.addQuery( " AND param.room=?" );
				ps.addString( houseRoom );
			}

			ps.addQuery( SQL_ORDER_BY );
			ps.addQuery( "c.title" );
			ps.addQuery( getPageLimit( page ) );

			try
			{
				ResultSet rs = ps.executeQuery();
				while( rs.next() )
				{
					list.add( new ParameterSearchedObject<Customer>( getCustomerFromRs( rs, "c." ), rs.getInt( 1 ), rs.getString( 2 ) ) );
				}

				setRecordCount( page, ps.getPrepared() );
				ps.close();
			}
			catch( SQLException ex )
			{
				throw new BGException( ex );
			}
		}
	}

	/**
	 * Выбирает контрагентов по номеру или номерам телефонов.
	 * @param searchResult
	 * @param phoneParamIdList
	 * @param phoneNumbers
	 * @throws BGException
	 */
	public void searchCustomerListByPhone( SearchResult<Customer> searchResult,
	                                       Collection<Integer> phoneParamIdList,
	                                       String... phoneNumbers )
	    throws BGException
	{
		if( searchResult != null )
		{
			Page page = searchResult.getPage();
			List<Customer> list = searchResult.getList();
			String ids = Utils.toString( phoneParamIdList );

			StringBuilder selectPart = new StringBuilder();
			StringBuilder joinPart = new StringBuilder();
			StringBuilder query = new StringBuilder();

			selectPart.append( SQL_SELECT );
			selectPart.append( " DISTINCT " );

			joinPart.append( SQL_INNER_JOIN );
			joinPart.append( TABLE_PARAM_PHONE_ITEM );
			joinPart.append( " AS p ON customer.id=p.id " );
			if( ids.length() > 0 )
			{
				joinPart.append( "AND p.param_id IN ( " );
				joinPart.append( ids );
				joinPart.append( " ) " );
			}

			// иначе MySQL не понимает, что можно использовать индекс
			for( int i = 0; i < phoneNumbers.length; i++ )
			{
				phoneNumbers[i] = "'" + phoneNumbers[i] + "'";
			}

			joinPart.append( " AND p.phone IN (" );
			joinPart.append( Utils.toString( Arrays.asList( phoneNumbers ) ) );
			joinPart.append( ")" );

			String referenceTemplate = addCustomerReferenceQuery( selectPart, joinPart );

			query.append( selectPart );
			query.append( " customer.* " );
			query.append( SQL_FROM );
			query.append( TABLE_CUSTOMER );
			query.append( joinPart );
			query.append( SQL_WHERE );
			query.append( "1=1 " );

			query.append( " ORDER BY customer.title " );
			query.append( getPageLimit( page ) );

			try
			{
				PreparedDelay ps = new PreparedDelay( con );
				ps.addQuery( query.toString() );
				extractCustomersWithRef( page, list, referenceTemplate, ps );
				ps.close();
			}
			catch( SQLException ex )
			{
				throw new BGException( ex );
			}
		}
	}

	/**
	 * Выбирает контрагентов по привязанном объектам.
	 * @param searchResult
	 * @param linkedObjectTypeLike LIKE строка типа привязанного объекта.
	 * @param linkedObjectTitle LIKE строка наименования привязанного объекта.
	 * @throws BGException
	 */
	public void searchCustomerByLinkedObjectTitle( SearchResult<Customer> searchResult, String linkedObjectTypeLike, String linkedObjectTitle )
	    throws BGException
	{
		if( searchResult != null )
		{
			Page page = searchResult.getPage();
			List<Customer> list = searchResult.getList();

			StringBuilder selectPart = new StringBuilder();
			StringBuilder joinPart = new StringBuilder();
			StringBuilder query = new StringBuilder();

			selectPart.append( SQL_SELECT );
			selectPart.append( " DISTINCT " );

			joinPart.append( SQL_INNER_JOIN );
			joinPart.append( TABLE_CUSTOMER_LINK );
			joinPart.append( " AS link ON link.customer_id = customer.id " );
			joinPart.append( "AND link.object_title LIKE ? " );
			joinPart.append( "AND link.object_type LIKE ? " );

			String referenceTemplate = addCustomerReferenceQuery( selectPart, joinPart );

			query.append( selectPart );
			query.append( " customer.* " );
			query.append( SQL_FROM );
			query.append( TABLE_CUSTOMER );
			query.append( joinPart );

			query.append( " ORDER BY customer.title " );
			query.append( getPageLimit( page ) );

			try
			{
				PreparedDelay ps = new PreparedDelay( con );
				ps.addQuery( query.toString() );
				ps.addString( linkedObjectTitle );
				ps.addString( linkedObjectTypeLike );
				extractCustomersWithRef( page, list, referenceTemplate, ps );
				ps.close();
			}
			catch( SQLException ex )
			{
				throw new BGException( ex );
			}
		}
	}

	/**
	 * Выбирает контрагента по его коду.
	 * @param customerId
	 * @return
	 */
	public Customer getCustomerById( int customerId )
	    throws BGException
	{
		Customer customer = null;

		try
		{
			String sql = "SELECT * FROM customer WHERE id=?";
			PreparedStatement ps = con.prepareStatement( sql );
			ps.setInt( 1, customerId );
			ResultSet rs = ps.executeQuery();
			if( rs.next() )
			{
				customer = getCustomerFromRs( rs, "" );
			}
			ps.close();
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}

		return customer;
	}

	/**
	 * Получение набора контрагентов по их ID
	 * @param customerIds идентификаторы контрагентов
	 * @return
	 * @throws BGException
	 */
	public static Set<Customer> getCustomers( Connection connection, Collection<Integer> customerIds )
	    throws BGException
	{
		Set<Customer> customers = new HashSet<Customer>();

		String sql = "SELECT * FROM customer WHERE customer.id IN ( ";
		sql += Utils.toString( customerIds );
		sql += " )";

		try
		{
			PreparedStatement ps = connection.prepareStatement( sql );
			ResultSet rs = ps.executeQuery();

			while( rs.next() )
			{
				customers.add( getCustomerFromRs( rs, "" ) );
			}

			ps.close();
		}
		catch( SQLException exception )
		{
			throw new BGException( exception );
		}

		return customers;
	}

	/**
	 * Получение набора контрагентов по их ID
	 * @param customerIds идентификаторы контрагентов
	 * @return
	 * @throws BGException
	 */
	public Set<Customer> getCustomers( Collection<Integer> customerIds )
	    throws BGException
	{
		return getCustomers( con, customerIds );
	}

	/**
	 * Выбирает контрагента по названию.
	 * @param customerTitle название
	 * @return
	 */
	public Customer getCustomerByTitle( String customerTitle )
	    throws SQLException
	{
		Customer customer = null;

		int index = 1;

		PreparedStatement ps = con.prepareStatement( "SELECT * FROM customer WHERE UPPER(title)=?" );
		ps.setString( index++, customerTitle.toUpperCase() );
		ResultSet rs = ps.executeQuery();
		while( rs.next() )
		{
			customer = getCustomerFromRs( rs, "" );
		}
		ps.close();

		return customer;
	}

	/**
	 * Обновляет информацию о контрагенте в БД. 
	 * @param customer
	 */
	public void updateCustomer( Customer customer )
	    throws SQLException
	{
		if( customer != null )
		{
			int index = 1;
			PreparedStatement ps = null;
			StringBuilder query = new StringBuilder();

			if( customer.getId() > 0 )
			{
				query.append( SQL_UPDATE );
				query.append( TABLE_CUSTOMER );
				query.append( " SET title=?, title_pattern=?, title_pattern_id=?, param_group_id=?" );
				query.append( " WHERE id=?" );
				ps = con.prepareStatement( query.toString() );
				ps.setString( index++, customer.getTitle() );
				ps.setString( index++, customer.getTitlePattern() );
				ps.setInt( index++, customer.getTitlePatternId() );
				ps.setInt( index++, customer.getParamGroupId() );
				ps.setInt( index++, customer.getId() );
				ps.executeUpdate();
			}
			else
			{
				query.append( SQL_INSERT );
				query.append( TABLE_CUSTOMER );
				query.append( " SET title=?, title_pattern=?, title_pattern_id=?, param_group_id=?," );
				query.append( " date_created=now(), user_id_created=?" );
				ps = con.prepareStatement( query.toString(), Statement.RETURN_GENERATED_KEYS );
				ps.setString( index++, customer.getTitle() );
				ps.setString( index++, customer.getTitlePattern() );
				ps.setInt( index++, customer.getTitlePatternId() );
				ps.setInt( index++, customer.getParamGroupId() );
				ps.setInt( index++, customer.getCreatedUserId() );
				ps.executeUpdate();
				customer.setId( lastInsertId( ps ) );
			}

			ps.close();
		}
	}

	/**
	 * Удаляет контрагента из БД по коду.
	 * @param id
	 * @throws SQLException
	 */
	public void deleteCustomer( int id )
	    throws SQLException
	{
		PreparedStatement ps = con.prepareStatement( "DELETE FROM " + TABLE_CUSTOMER + " WHERE id=?" );
		ps.setInt( 1, id );
		ps.executeUpdate();
		ps.close();
	}

	/**
	 * Обновляет название контрагента,генерируя его из параметров.
	 * @param titleBefore исходное название.
	 * @param customer контрагент.
	 * @param changedParamId код изменённого параметра.
	 * @param response если параметр передан, туда будет добавлено событие о изменении названия договора.
	 * @return
	 * @throws Exception
	 */
	public void updateCustomerTitle( String titleBefore, Customer customer, int changedParamId, Response response )
	    throws Exception
	{
		PatternDAO patternDAO = new PatternDAO( con );
		ParamValueDAO paramValueDAO = new ParamValueDAO( con );

		Customer oldCustomer = getCustomerById( customer.getId() );
		oldCustomer.setGroupIds( this.getGroupIds( customer.getId() ) );

		try
		{
			// предполагаем, что -1 - персональный шаблон
			String titlePattern = customer.getTitlePattern();
			// без шаблона
			if( customer.getTitlePatternId() == 0 )
			{
				customer.setTitlePattern( titlePattern = "" );
			}
			// шаблон из справочника
			else if( customer.getTitlePatternId() > 0 )
			{
				Pattern pattern = patternDAO.getPattern( customer.getTitlePatternId() );
				if( pattern != null )
				{
					titlePattern = pattern.getPattern();
				}
			}

			// формирование названия по шаблону
			if( Utils.notBlankString( titlePattern ) &&
			    (changedParamId < 0 || titlePattern.contains( String.valueOf( changedParamId ) )) )
			{
				customer.setTitle( Utils.formatPatternString( Customer.OBJECT_TYPE, customer.getId(), paramValueDAO, titlePattern ) );
			}

			if( oldCustomer != null )
			{
				logCustomerChange( customer, oldCustomer );
			}

			updateCustomer( customer );

			boolean changed = !titleBefore.equals( customer.getTitle() );
			if( changed && response != null )
			{
				response.addEvent( new CustomerTitleChangedEvent( customer.getId(), customer.getTitle() ) );
			}

			if( changed )
			{
				// обновление наименования контрагента в привязках процессов
				new ProcessLinkDAO( con ).updateLinkTitles( customer.getId(), Customer.OBJECT_TYPE + "%", customer.getTitle() );
			}
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}
	}

	/**
	 * Возвращает названия контрагенов с подстрокой.
	 * @param title подстрока, поиск идёт с помощью LIKE выражения.
	 * @param count количество первых названий.
	 * @return
	 * @throws BGException
	 */
	public List<String> getCustomerTitles( String title, int count )
	    throws BGException
	{
		List<String> result = new ArrayList<String>();

		try
		{
			String query =
			" SELECT title FROM " + TABLE_CUSTOMER +
			" WHERE title LIKE ? " +
			" GROUP BY title ORDER BY title LIMIT ?";

			PreparedStatement ps = con.prepareStatement( query );
			ps.setString( 1, title );
			ps.setInt( 2, count );

			ResultSet rs = ps.executeQuery();
			while( rs.next() )
			{
				result.add( rs.getString( 1 ) );
			}
			ps.close();
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}

		return result;
	}

	public Set<Integer> getGroupIds( int customerId )
	    throws BGException
	{
		return getIds( TABLE_CUSTOMER_GROUP, "customer_id", "group_id", customerId );
	}

	public void updateGroupIds( int customerId, Set<Integer> groupIds )
	    throws BGException
	{
		updateIds( TABLE_CUSTOMER_GROUP, "customer_id", "group_id", customerId, groupIds );
	}

	/**
	 * Возвращает созданный объект {@link Customer} заполненный из {@link ResultSet}.
	 * @param rs
	 * @param prefix
	 * @throws SQLException
	 */
	public static Customer getCustomerFromRs( ResultSet rs, String prefix )
	    throws SQLException
	{
		Customer customer = new Customer();

		customer.setId( rs.getInt( prefix + "id" ) );
		customer.setTitle( rs.getString( prefix + "title" ) );
		customer.setTitlePattern( rs.getString( prefix + "title_pattern" ) );
		customer.setTitlePatternId( rs.getInt( prefix + "title_pattern_id" ) );
		customer.setParamGroupId( rs.getInt( prefix + "param_group_id" ) );
		customer.setCreatedDate( rs.getTimestamp( prefix + "date_created" ) );
		customer.setCreatedUserId( rs.getInt( prefix + "user_id_created" ) );
		customer.setPassword( rs.getString( prefix + "pswd" ) );

		return customer;
	}

	private void logCustomerChange( Customer customer, Customer oldCustomer )
	    throws BGException, SQLException
	{
		if( history )
		{
			EntityLogDAO entityLogDAO = new EntityLogDAO( this.con, TABLE_CUSTOMER_LOG );
			entityLogDAO.insertEntityLog( customer.getId(), userId, customer.toLog( con, oldCustomer ) );
		}
	}
}
