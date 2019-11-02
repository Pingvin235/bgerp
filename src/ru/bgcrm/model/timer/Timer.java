package ru.bgcrm.model.timer;

import java.util.Date;

import ru.bgcrm.util.TimeUtils;

/**
 * Отложенный таймер, запускающийся в определенное время.
 */
public class Timer
{
    public static final int ENTITY_PROCESS = 1; 
    
    // код таймера
    private int id;
    // тип сущности, к которой привязан таймер
    private int entityType = ENTITY_PROCESS;
    // код сущности, к которой привязан таймер
    private int entityId;
    // имя таймера
    private String name;
    // время отрабатывания
    private Date time;
    // имя скрипта, которому передастся событие таймера
    private String scriptName;
    // конфигурация таймера
    private String data;
    
    public int getId()
    {
        return id;
    }

    public void setId( int id )
    {
        this.id = id;
    }

    public int getEntityType()
    {
        return entityType;
    }

    public void setEntityType( int entityType )
    {
        this.entityType = entityType;
    }

    public Date getTime()
    {
        return time;
    }

    public void setTime( Date time )
    {
        this.time = time;
    }

    public String getData()
    {
        return data;
    }

    public void setData( String data )
    {
        this.data = data;
    }

    public int getEntityId()
    {
        return entityId;
    }

    public void setEntityId( int entityId )
    {
        this.entityId = entityId;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }
    
    public String getScriptName()
    {
    	return scriptName;
    }

	public void setScriptName( String scriptName )
    {
    	this.scriptName = scriptName;
    }

	@Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        
        result.append( "id=" );
        result.append( id );
        result.append( "; entityType=" );
        result.append( entityType );
        result.append( "; entityId=" );
        result.append( entityId );
        result.append( "; name=" );
        result.append( name );
        result.append( "; time=" );
        result.append( TimeUtils.format( time, "dd.MM.yyyy HH:mm:ss" ) );
        result.append( "; data=" );
        result.append( data );        
        
        return result.toString();
    }
}