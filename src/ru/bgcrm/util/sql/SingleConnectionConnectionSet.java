package ru.bgcrm.util.sql;

import java.sql.Connection;

/**
 * ConnectionSet - оболочка над просто соединением к БД,
 * фактически не имеет возможность предоставить Slave соединение, 
 * либо соединение к мусорной БД. Вместо них всегда возвращается
 * то же самое соединение к основной БД.
 */
public class SingleConnectionConnectionSet
    extends ConnectionSet
{
	public SingleConnectionConnectionSet( Connection master )
	{
		super( master );
	}
	
	@Override
	protected void finalize()
	    throws Throwable
	{}
}