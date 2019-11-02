package ru.bgcrm.dynamic;

import java.util.Map;

/**
 * Подгружает классы из БД.
 */
public class DatabaseClassLoader
    extends ClassLoader
{
	private Map<String, byte[]> data;
	
	public DatabaseClassLoader( ClassLoader parent )
	{
		this( null, parent );
	}
	
	public DatabaseClassLoader( Map<String, byte[]> data, ClassLoader parent )
	{
		super( parent );
		this.data = data;
	}

	/**
	 * Забираем класс из базы.
	 */
	@Override
	protected Class<?> findClass( String name )
	    throws ClassNotFoundException
	{
		byte[] classData = data.get( name );
		if( classData == null )
		{
			throw new ClassNotFoundException( name );
		}

		return defineClass( name, classData, 0, classData.length );
	}
}