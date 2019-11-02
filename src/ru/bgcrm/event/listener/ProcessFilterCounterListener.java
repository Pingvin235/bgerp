package ru.bgcrm.event.listener;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ru.bgcrm.cache.ProcessQueueCache;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.GetPoolTasksEvent;
import ru.bgcrm.event.client.FilterCounterEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.process.Queue;
import ru.bgcrm.model.process.queue.config.SavedFiltersConfig;
import ru.bgcrm.model.process.queue.config.SavedFiltersConfig.SavedFilterSet;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.worker.FilterEntryCounter;

public class ProcessFilterCounterListener
{
	private static final Logger log = Logger.getLogger( ProcessFilterCounterListener.class );
	
	public ProcessFilterCounterListener()
	{
		EventProcessor.subscribe( new EventListener<GetPoolTasksEvent>()
		{
			@Override
			public void notify( GetPoolTasksEvent e, ConnectionSet connectionSet )
				throws BGException
			{
				processListener( e.getForm(), connectionSet );
			}
		}, GetPoolTasksEvent.class );
	}
	
	private void processListener(DynActionForm form, ConnectionSet connectionSet)
	{
		Preferences personalizationMap = form.getUser().getPersonalizationMap();

		// впоследствии они вернуться в это же место кода в processCounterUrls параметре запроса
		SavedFiltersConfig config = personalizationMap.getConfig( SavedFiltersConfig.class );

		// сохранённые в конфигурации счётчики для отображения справа сверху
		Map<Integer, SavedFilterSet> topFilters = config.getTopFilters();
				
		// значения счётчиков
		// id очереди, id счётчика (кнопки), значение
		HashMap<Integer, HashMap<Integer, Integer>> valuesToReturn = new HashMap<>();
		
		String urlsParam = form.getParam( "processCounterUrls" );
		if(  Utils.notBlankString( urlsParam ) )
		{
			String urls[] = urlsParam.split( "," );
			for( String url : urls )
			{
				final String[] tokens = url.split( ":" );
				
				Integer buttonId = Integer.valueOf( tokens[0] );
				Integer queueId = Integer.valueOf( tokens[1] );
				url = tokens[2];
				Queue queue = ProcessQueueCache.getQueue( queueId, form.getUser() );
				int count = 0;
				try
				{
					count = FilterEntryCounter.getInstance().parseUrlAndGetCount( queue, url, form.getUser() );
				}
				catch( Exception e )
				{
					log.error( e.getMessage(), e );
				}

				HashMap<Integer, Integer> btnIdAndEntryCount =  valuesToReturn.get( queueId );
				if (btnIdAndEntryCount == null) 
				{
					valuesToReturn.put( queueId, btnIdAndEntryCount = new HashMap<>() );
				}				
				btnIdAndEntryCount.put( buttonId, count );
			}
		}
		
		FilterCounterEvent filterCountEvent = new FilterCounterEvent();
		filterCountEvent.setCount( valuesToReturn );
		filterCountEvent.setFilters( topFilters );
		form.getResponse().addEvent( filterCountEvent );
	}
}
