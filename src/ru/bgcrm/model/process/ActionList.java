package ru.bgcrm.model.process;

/*
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.bitel.bgcrm.util.DefaultSetup;

**
 * Базовый класс - набор действий с инцидентом.
 *
public class ActionList
{
    protected List<Action> actionList = new ArrayList<Action>();
    
    public List<Action> getActionList()
    {
        return actionList;
    }
    
    public void loadFromData( DefaultSetup data, String prefix )
    {
        actionList.clear();
        
        for(  Map<String, String> actionParam : data.parseObjects( prefix + "action." ) )
        {
            String id = actionParam.get( "id" );
            String actionPrefix = prefix + "action." + id + "."; 
            
            Action action = new Action();
            action.loadFromData( data, actionPrefix );
            actionList.add( action );
        }            
    }

    public void serializeToData( StringBuilder data, String prefix )
    {
        int i = 1;
        for( Action action : actionList )
        {
            String pref = prefix + "action." + (i++) + ".";
            action.serializeToData( data, pref );
        }
    } 
}
*/