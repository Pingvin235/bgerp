package ru.bgcrm.model.param;

import java.util.HashSet;
import java.util.Set;

public class ParameterGroup
{
    private int id = -1;
    private String title;
    private String object;
    private Set<Integer> parameterIds = new HashSet<Integer>();

    public int getId()
    {
        return id;
    }

    public void setId( int id )
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle( String title )
    {
        this.title = title;
    }

    public String getObject()
    {
        return object;
    }

    public void setObject( String object )
    {
        this.object = object;
    }

    public Set<Integer> getParameterIds()
    {
        return parameterIds;
    }

    public void setParameterIds( Set<Integer> parameterIds )
    {
        this.parameterIds = parameterIds;
    }
}
