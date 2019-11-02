package ru.bgcrm.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import ru.bgcrm.model.BGException;
import ru.bgcrm.util.XMLUtils;

public class PluginManager
{
	private static final Logger log = Logger.getLogger( PluginManager.class );
	
	private static PluginManager instance;
	
	public static void init()
		throws BGException
	{
		instance = new PluginManager();
	}
	
	public static PluginManager getInstance()
	{
		return instance;
	}
	
	private List<Plugin> pluginList = new ArrayList<Plugin>();
	private Map<String, Plugin> pluginMap = new HashMap<String, Plugin>();
	
	private PluginManager()
		throws BGException
	{
		File pluginDir = new File( "plugin" );
		if( !pluginDir.exists() || !pluginDir.canRead() )
		{
			log.error( "Error read plugin dir!" );
			return;
		}
		
		for( File file : pluginDir.listFiles() )
		{
			if( !file.getName().endsWith( ".xml" ) )
			{
				continue;
			}
			
			try
            {
				Document doc = XMLUtils.parseDocument( new InputSource( new FileInputStream( file ) ) );
				
				String pluginPackage = XMLUtils.selectText( doc, "/plugin/@package" );
				Class<? extends Plugin> pluginClass = (Class<? extends Plugin>)Class.forName( pluginPackage + ".Plugin" );
				
				Constructor<? extends Plugin> constructor = pluginClass.getConstructor( Document.class );
				
				Plugin plugin = (Plugin)constructor.newInstance( doc );
				pluginList.add( plugin );
				pluginMap.put( plugin.getName(), plugin );
				
				//plugin.getPluginClass().init();
				
				log.info( "Loaded plugin: " + plugin.getName() );
            }
            catch( Exception e )
            {
	            log.error( "Error load plugin from file: " + file.getName() + ", " + e.getMessage(), e  );
            }
		}
	}
	
	public List<Plugin> getPluginList()
	{
		return pluginList;
	}
	
	public Map<String, Plugin> getPluginMap()
	{
		return pluginMap;
	}
}