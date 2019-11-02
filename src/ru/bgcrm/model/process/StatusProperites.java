package ru.bgcrm.model.process;

/*
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ru.bgcrm.plugin.bgbilling.ws.bgcrm.util.DefaultSetup;

**
 * Свойства статуса.
 *
public class StatusProperites
    extends ActionList    
{
    private static final Logger log = Logger.getLogger( StatusProperites.class );
    private List<StatusAction> statusActionList = new ArrayList<StatusAction>();
    
    public List<StatusAction> getStatusActionList()
    {
        return statusActionList;
    } 
    
    public void doStatusAction( int id, Process process, Map<String, String> paramMap, int userId )
    {
        if( log.isDebugEnabled() )
        {
            log.debug( "Executing status action: " + id );
        }
        // нумерация акшенов начинается с 1, а в массиве лежат с 0
        id--;        
        
        StatusAction statusAction = null;
        if( id  < statusActionList.size() )
        {
            statusAction = statusActionList.get( id );
        }
        else
        {
            log.error( "Status action with id: " + id + " not found" );
        }
        
        if( statusAction != null )
        {
            List<Integer> actionIdList = statusAction.getActionIdList();
            for( int actionId : actionIdList )
            {
                // нумерация акшенов начинается с 1, а в массиве лежат с  
                actionId--;
                if( actionId >= 0 && actionId < actionList.size() )
                {
                    if( log.isDebugEnabled() )
                    {
                        log.debug( "Executing action: " + (actionId + 1) );
                    }
                    Action action = actionList.get( actionId );
                    action.doAction( process, paramMap, userId, true );
                }
                else
                {
                    log.error( "Action with id: " + (actionId + 1) + " for status action not found"  );
                }
            }
        }        
    }
    
    @Override
    public void loadFromData( DefaultSetup data, String prefix )
    {
        super.loadFromData( data, prefix );
        
        statusActionList.clear();            
        
        for( Map<String, String> actionParam : data.parseObjects( prefix + "status_action." ) )
        {
            String id = actionParam.get( "id" );
            String actionPrefix = prefix + "status_action." + id + "."; 
            
            StatusAction action = new StatusAction();
            action.loadFromData( data, actionPrefix );
            statusActionList.add( action );
        }
    }

    @Override
    public void serializeToData( StringBuilder data, String prefix )
    {
        super.serializeToData( data, prefix );
        
        data.append( "#\n" );
        
        int i = 1;
        for( StatusAction statusAction : statusActionList )
        {
            String pref = prefix + "status_action." + (i++) + ".";
            statusAction.serializeToData( data, pref );
        }
    }
}*/