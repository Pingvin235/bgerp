package ru.bgcrm.util;

/*import java.sql.Connection;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.sql.SQLUtils;


*//**
 * Отвечает за интеграцию с АТСкой, софт-свичем или иной системой передачи
 * звонков.
 *//*
public class PhoneProcessor
{
	private static Logger log = Logger.getLogger( PhoneProcessor.class );
	private static PhoneProcessor processor;

	public static PhoneProcessor getProcessor()
	{
		if( processor == null )
		{
			processor = new PhoneProcessor();
		}
		return processor;
	}

	// конец статической части

	// ключ - идентификатор, забитый в пользователе, значение - код пользователя
	// хранит пользователей по идентификаторам, забитым в их свойствах
	private Map<String, Integer> idUserMap = new ConcurrentHashMap<String, Integer>();
	// ключ - код пользователя, значение - номер телефона
	private Map<Integer, String> userRegistredPhones = new ConcurrentHashMap<Integer, String>();

	// пользователи, готовые к вызову
	private Set<Integer> readyToCall = Collections.newSetFromMap( new ConcurrentHashMap<Integer, Boolean>() );

	private PhoneProcessor()
	{
		reloadLoadUserPhones();
	}

	public void registerPhone( int userId, String number )
	{
		userRegistredPhones.put( userId, number );
	}

	public void unregisterPhone( int userId )
	{
		userRegistredPhones.remove( userId );
	}

	public String getRegistredPhone( int userId )
	{
		return userRegistredPhones.get( userId );
	}

	*//**
	 * Возвращает код пользователя по идентификатору.
	 * 
	 * @param phone
	 * @return
	 *//*
	public Integer getUserById( String id )
	{
		return idUserMap.get( id );
	}

	*//**
	 * Перезагрузка номеров, привязанных к пользователям, из БД.
	 *//*
	private void reloadLoadUserPhones()
	{
		Connection con = Setup.getSetup().getDBConnectionFromPool();
		try
		{
			reloadUserPhones( con );
		}
		catch( Exception e )
		{
			log.error( e.getMessage(), e );
		}
		finally
		{
			SQLUtils.closeConnection( con );
		}
	}

	public void reloadUserPhones( Connection con )
	    throws BGException
	{
		UserDAO userDAO = new UserDAO( con );
		for( User user : userDAO.getUserList() )
		{
			List<String> phones = user.getIds();
			for( String phone : phones )
			{
				idUserMap.put( phone, user.getId() );
			}
		}
	}

	public void setReadyToCall( int userId, boolean ready )
	{
		if( ready )
		{
			readyToCall.add( userId );
		}
		else
		{
			readyToCall.remove( userId );
		}
	}

	public boolean isReadyToCall( int userId )
	{
		return readyToCall.contains( userId );
	}
}
*/