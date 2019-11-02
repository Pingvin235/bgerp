package ru.bgcrm.model.process;

import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.Utils;


/**
 * Условие, при котором выполняется действие с инцидентом.
 */
public class ActionCondition
{
    public static final String TYPE_CURRENT_STATUS = "current_status";
    
    private String type;
    private String param;
    
    public String getType()
    {
        return type;
    }
    public void setType( String type )
    {
        this.type = type;
    }
    public String getParam()
    {
        return param;
    }
    public void setParam( String param )
    {
        this.param = param;
    }
    
    public boolean check( Process process )
    {
        boolean result = false;
        
        if( type.equals( TYPE_CURRENT_STATUS ) )
        {
            result = Utils.toIntegerSet( param ).contains( process.getStatusId() );
        }
        
        return result;        
    }
    
    public void loadFromData( Preferences data, String prefix )
    {
        type = data.get( prefix + "type" );
        param = data.get( prefix + "param" );
    }

    public void serializeToData( StringBuilder data, String prefix )
    {
        Utils.addSetupPair( data, prefix, "type", type );
        Utils.addSetupPair( data, prefix, "param", param );
    }
    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        
        result.append( "type: " );
        result.append( type );
        result.append( "; param: " );
        result.append( param );
        
        return result.toString();
    }
}