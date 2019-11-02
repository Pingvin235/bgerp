package ru.bgcrm.model.process;

/*
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.bgcrm.plugin.bgbilling.ws.bgcrm.util.DefaultSetup;
import ru.bgcrm.plugin.bgbilling.ws.bgcrm.util.Utils;

**
 * Действие, разрешенное к выполнению над инцидентом в каком-либо статусе.
 *
public class StatusAction        
{
    private String title;
    private List<Integer> actionIdList = new ArrayList<Integer>();
    private List<StatusActionParam> paramList = new ArrayList<StatusActionParam>();
    private List<String> areaList = new ArrayList<String>();

    public String getTitle()
    {
        return title;
    }
    
    public List<StatusActionParam> getParamList()
    {
        return paramList;
    }
     
    public List<Integer> getActionIdList()
    {
        return actionIdList;
    }
    
    public void loadFromData( DefaultSetup data, String prefix )
    {
        paramList.clear();
        
        actionIdList = Utils.stringToIntegerList( data.get( prefix + "action_ids" ) );
        areaList = Utils.stringToList( data.get( prefix + "areas" ) );
        title = data.get( prefix + "title" );
        
        for(  Map<String, String> actionParam : data.parseObjects( prefix + "param." ) )
        {
            String id = actionParam.get( "id" );
            String paramPrefix = prefix + "param." + id + ".";
            
            StatusActionParam param = new StatusActionParam();
            param.loadFromData( data, paramPrefix );
            paramList.add( param );
        }
    }

    public void serializeToData( StringBuilder data, String prefix )
    {
        Utils.addSetupPair( data, prefix, "title", title );
        Utils.addSetupPair( data, prefix, "action_ids", Utils.collectionToString( actionIdList ) );
        Utils.addSetupPair( data, prefix, "areas", Utils.collectionToString( areaList ) );
        
        int i = 1;
        for( StatusActionParam param : paramList )
        {
            String pref = prefix + "param." + (i++) + ".";
            param.serializeToData( data, pref );
        }            
    }
    
    @Override
    public String toString()
    {
        return title;
    }  
    
}*/