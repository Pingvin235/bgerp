package ru.bgcrm.dao;

import static ru.bgcrm.dao.Tables.TABLE_COUNTER;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bgerp.app.exception.BGException;

import ru.bgcrm.model.Counter;

public class CounterDAO
	extends CommonDAO
{
	public CounterDAO( Connection con )
	{
		super( con );
	}

	/**
	 *  Возвращает значение счетчика по counterId
	 * @param counterId - id счетчика
	 * @return значение (-1 если счетчика с таким id не найдено)
	 * @throws BGException
	 */
	public int getCounterValue( int counterId )
		throws BGException
	{
		String query = "SELECT * FROM " + TABLE_COUNTER + " WHERE id=?";

		try
		{
			PreparedStatement ps = con.prepareStatement( query );
			ps.setInt( 1, counterId );
			ResultSet rs = ps.executeQuery();
			if( rs.next() )
			{
				return rs.getInt( "value" );
			}

			ps.close();
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}

		return -1;
	}

	/**
	 * Возвращает счетик
	 * @param counterId - id счетчика
	 * @return счетчик
	 * @throws BGException
	 */
	public Counter getCounter( int counterId )
		throws BGException
	{
		Counter counter = new Counter();
		String query = "SELECT * FROM " + TABLE_COUNTER + " WHERE id=?";

		try
		{
			PreparedStatement ps = con.prepareStatement( query );
			ps.setInt( 1, counterId );
			ResultSet rs = ps.executeQuery();
			if( rs.next() )
			{
				counter.setId( rs.getInt( "id" ) );
				counter.setTitle( rs.getString( "title" ) );
				counter.setValue( rs.getInt( "value" ) );
				counter.setPrefix( rs.getString( "prefix" ) );
				counter.setPostfix( rs.getString( "postfix" ) );
			}

			ps.close();
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}

		return counter;
	}

	/**
	 * Изменение счетчика
	 * @param counter счетчик
	 * @throws BGException
	 */
	public void updateCounter( Counter counter )
		throws BGException
	{
		String query = "UPDATE " + TABLE_COUNTER + " SET title=? , value=? WHERE id=?";

		boolean exist = getCounter( counter.getId() ).getId() != -1;

		if( !exist )
		{
			query = " INSERT INTO " + TABLE_COUNTER + " ( title, value ) VALUES (?, ?) ";
		}

		try
		{
			PreparedStatement ps = con.prepareStatement( query );

			ps.setString( 1, counter.getTitle() );
			ps.setInt( 2, counter.getValue() );
			if( exist )
			{
				ps.setInt( 3, counter.getId() );
			}

			ps.executeUpdate();
			ps.close();
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}
	}


	/**
	 * Увеличивает счетчик на 1
	 * @param counterId - id счетчика
	 * @throws BGException
	 */
	public void incrementCounter( int counterId ) throws BGException
	{
		String query = "UPDATE " + TABLE_COUNTER + " SET value=value+1 WHERE id=?";

		try
		{
			PreparedStatement ps = con.prepareStatement( query );
			ps.setInt( 1, counterId );

			ps.executeUpdate();
			ps.close();
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}
	}
}
