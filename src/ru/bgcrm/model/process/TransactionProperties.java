package ru.bgcrm.model.process;

import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

/**
 * Свойства транзакции (смены статуса).
 */
public class TransactionProperties
{
    private boolean enable = false;
    private String reference = "";
    
    public boolean isEnable()
    {
        return enable;
    }

    public void setEnable( boolean enable )
    {
        this.enable = enable;
    }
    
    public void loadFromData( ParameterMap data, String prefix )
    {
        enable = data.getBoolean( prefix + "enable", false );            
    }
    
    public String getReference()
    {
    	return reference;
    }

	public void setReference( String reference )
    {
    	this.reference = reference;
    }

	public void serializeToData( StringBuilder data, String prefix )
    {
        Utils.addSetupPair( data, prefix, "enable", Utils.booleanToStringInt( enable ) );
        data.append( "#\n" );            
    } 
}