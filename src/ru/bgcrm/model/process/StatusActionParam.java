package ru.bgcrm.model.process;

/*
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import ru.bgcrm.plugin.bgbilling.ws.bgcrm.util.DefaultSetup;
import ru.bgcrm.plugin.bgbilling.ws.bgcrm.util.TimeUtils;
import ru.bgcrm.plugin.bgbilling.ws.bgcrm.util.Utils;

public class StatusActionParam
{
    private static final String TYPE_HOUR_LIST = "hour_list";
    
    private String type;
    private String param;
    private String name;
    
    public void loadFromData( DefaultSetup data, String prefix )
    {
        type = data.get( prefix + "type" );
        param = data.get( prefix + "param" );
        name = data.get( prefix + "name" );            
    }

    public void serializeToData( StringBuilder data, String prefix )
    {
        Utils.addSetupPair( data, prefix, "type", type );
        Utils.addSetupPair( data, prefix, "param", param );
        Utils.addSetupPair( data, prefix, "name", name );
    }
    
    public String getName()
    {
        return name;
    }  
    
    public String getType()
    {
        return type;
    }
    
    public boolean isList()
    {
        return TYPE_HOUR_LIST.equals( type );
    }
    
    public List<ListValue> getValues()
    {
        List<ListValue> result = new ArrayList<ListValue>();
        //
        if( TYPE_HOUR_LIST.equals( type ) )
        {
            Calendar hour = new GregorianCalendar();
            //
            for( int i = 0; i < Utils.parseIntString( param ); i++ )
            {
                hour.add( Calendar.HOUR, 1 );
                TimeUtils.clear_MIN_MIL_SEC( hour );
                //
                ListValue item = new ListValue();
                item.setValue( TimeUtils.format( hour, "dd.MM.yyyy HH:mm:ss" ) );
                item.setTitle( TimeUtils.format( hour, "dd HH" ) );
                result.add( item );
            }
        }
        //
        return result;
    }
}
*/