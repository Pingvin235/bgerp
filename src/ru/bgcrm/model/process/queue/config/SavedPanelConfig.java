package ru.bgcrm.model.process.queue.config;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.Utils;

public class SavedPanelConfig
	extends Config
{
	private static final Logger log = Logger.getLogger( SavedPanelConfig.class );

	public static final String QUEUE_SAVED_PANEL_SET_PREFIX = "queueSavedPanelSet";
	public static final String QUEUE_CURRENT = "queueCurrent";
	
	// ключ - ID очереди
	private Set<Integer> savedPanelSet = new HashSet<Integer>();
	
	private Integer currentSelected = 0;
	
	public SavedPanelConfig( ParameterMap setup )
	{
		super( setup );

		savedPanelSet = Utils.toIntegerSet( setup.get( QUEUE_SAVED_PANEL_SET_PREFIX, "" ) );	
		log.debug("проверяем сет после получения данных из базы: " + savedPanelSet.toString());
		currentSelected = setup.getInt( QUEUE_CURRENT, 0 );		
	}

	public void addSavedPanelSet( Integer queueId )
	{
		savedPanelSet.add( queueId );
		log.debug( "putting " + queueId );

		for( Integer id : savedPanelSet )
		{
			log.debug( "check after put " + savedPanelSet.contains( id ) );
		}
	}
	
	public void changeCurrentSelected( Integer queueId )
	{
		log.debug("Меняем существующую выбранную очередь: " + queueId );
		currentSelected = queueId;
		log.debug("Проверяем обновленную очередь " + currentSelected);
	}
	
	public Set<Integer> getSavedPanelSet()
	{
		return savedPanelSet;
	}
	
	public Integer getCurrentSelected()
	{
		log.debug("Current selected: " + currentSelected.toString() );
		return currentSelected;
	}

	public void removeSavedPanelSet( Integer queueId )
	{
		savedPanelSet.remove( queueId );
		log.debug( "check after remove " + savedPanelSet.contains( queueId ) );
		log.debug( savedPanelSet );
	}

	public void updateConfig( Preferences userConfig )
	{
		//userConfig.removeSub( QUEUE_SAVED_PANEL_SET_PREFIX );
		userConfig.set( QUEUE_SAVED_PANEL_SET_PREFIX, Utils.toString( savedPanelSet ) );
		
		if ( currentSelected > 0 )
		{
			log.debug("записываем текущую выбранную очередь в базу");
			userConfig.set( QUEUE_CURRENT, currentSelected.toString() );
		}
	}

}
