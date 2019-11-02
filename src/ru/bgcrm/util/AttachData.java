package ru.bgcrm.util;

/*
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


public class AttachData
{
    private String title;
    private byte[] data;
    private static final Logger log = Logger.getLogger( AttachData.class );
    
    public AttachData(String title, InputStream dataStream)
    {
        this.title = title;
        //тут почему-то dataStream.available() возвращает 0
        //поэтому сделал так, как есть
        List<Integer> streamValues = new ArrayList<Integer>();
        try
        {         
            int b;
            while ((b = dataStream.read()) != -1) 
            { 
                streamValues.add( b );
            }
            data = new byte[streamValues.size()];
            for (int i = 0; i < streamValues.size(); i++)
                data[i] = streamValues.get( i ).byteValue();
        }
        catch( IOException e )
        {
            log.error( "Attach data error: " + e.getMessage(), e);
        }
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public byte[] getData()
    {
        return data;
    }
}
*/