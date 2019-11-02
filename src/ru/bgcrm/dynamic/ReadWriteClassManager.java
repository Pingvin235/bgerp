package ru.bgcrm.dynamic;

/**
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import ru.bgcrm.dynamic.CompilerWrapper.CompilationFailedException;
import ru.bgcrm.dynamic.CompilerWrapper.CompiledUnit;
import ru.bgcrm.dynamic.dao.DynamicCodeDao;
import ru.bgcrm.dynamic.model.CompilationResult;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Pair;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.sql.SQLUtils;


 * Загрузчик динамического кода в режиме read\write. 
 
public class ReadWriteClassManager
	extends DynamicClassManager
{
	private static final Logger log = Logger.getLogger( ReadWriteClassManager.class );
	
	private CompilerWrapper javac;
	
	public ReadWriteClassManager()
	{
		this( Thread.currentThread().getContextClassLoader() );
	}
	
	public ReadWriteClassManager( ClassLoader parentClassLoader )
    {
	    super( parentClassLoader );
	    this.javac = new CompilerWrapper( DynamicCodeDao.getScriptsDir(), 
	                                      new File( System.getProperty( "java.io.tmpdir" ) ) );
    }
	
    public CompilationResult recompileAll()
    	throws BGException
    {
    	CompilationResult result = null;
		
    	Connection con = Setup.getSetup().getDBConnectionFromPool();
		try
		{
			DynamicCodeDao dao = new DynamicCodeDao( con );
			
			dao.clearTables();
			
			flushLoadedClassCache();
			result = recompile( con, dao.getDynamicClassNames() );
			
			SQLUtils.commitConnection( con );
			
			log.info( "Successfully recompiled dyn classess." );
		}
		catch( CompilationFailedException ex )
		{
			result = ex.getCompilationResult();
		}
        finally
		{
			SQLUtils.closeConnection( con );			
		}
		
		return result;
    }

	private CompilationResult recompile( Connection con, List<String> targetClassNames )
		throws BGException
	{
		DynamicCodeDao dao = new DynamicCodeDao( con );
		
		List<String> srcFiles = new ArrayList<String>();
		for( String name : targetClassNames )
		{
			srcFiles.add( DynamicCodeDao.getClassFile( name ).getAbsolutePath() );
		}
		
		if( srcFiles.size() == 0 )
		{
			return new CompilationResult();
		}
		
		//компилируем..
		Pair<CompilationResult, List<CompiledUnit>> result = javac.compile( srcFiles );
		List<CompiledUnit> units = result.getSecond();
		
		ClassLoader loader = new DatabaseClassLoader( con, parentClassLoader );
		//компиляция успешна - пишем в базу
		for( CompiledUnit unit : units )
		{
			try
			{
				//запись в базу
				FileInputStream fis = new FileInputStream( unit.classFile );
				byte[] data = IOUtils.toByteArray( fis );
				dao.updateClassData( unit.className, data, unit.srcFile.lastModified() );
				
				if( log.isDebugEnabled() )
				{
					log.debug( "Updating class data: " + unit.className + "; size: " + data.length );
				}
			}
			catch( IOException ex )
			{
				throw new BGException( ex );
			}
		}
		
		// удаляем временную папку со скомпилированными файлами
		javac.deleteClassDir();
		
		//перезагрузка классов из базы
		for( CompiledUnit unit : units )
		{
			try
			{
				//подгрузка класслоадером
				Class<?> loaded = loader.loadClass( unit.className );
				loadedClasses.put( unit.className, loaded );
				
				if( log.isDebugEnabled() )
				{
					log.debug( "Add loaded class: " + unit.className );
				}
				
				//получение информации об интерфейсах, запись ее в базу
				dao.updateClassInterfaces( loaded );
			}
			catch( ClassNotFoundException e )
			{
				throw new BGException( e );
			}
			catch( Throwable e )
			{
				//ошибки класслоадера низкого уровня, пока пусть тоже будет BGException
				throw new BGException( e );
			}
		}
		
		return result.getFirst();
	}
}
*/