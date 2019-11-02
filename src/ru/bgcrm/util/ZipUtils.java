package ru.bgcrm.util;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;

public class ZipUtils
{

	public static SortedMap<String, byte[]> getFileEntriesFromZipByPrefix( ZipInputStream zis, String prefix )
    {
        SortedMap<String, byte[]> result = new TreeMap<String, byte[]>();
        ZipEntry ze = null;
        try
        {
            while( (ze = zis.getNextEntry()) != null )
            {
                String name = ze.getName();
                if( name.startsWith( prefix ) )
                {
                    byte[] data = IOUtils.toByteArray( zis );
                    result.put( name, data );
                }
            }
        }
        catch( Exception ex )
        {
            ex.printStackTrace();
        }
    
        return result;
    }

	public static Map<String, byte[]> getEntriesFromZip( ZipInputStream zis, String mask )
    {
        Map<String, byte[]> result = new HashMap<String, byte[]>();
        ZipEntry ze = null;
        try
        {
            while( (ze = zis.getNextEntry()) != null )
            {
                String name = ze.getName();
                if( mask == null || name.indexOf( mask ) >= 0 )
                {
                    byte[] data = IOUtils.toByteArray( zis );
                    result.put( name, data );
                }
            }
        }
        catch( Exception ex )
        {
            ex.printStackTrace();
        }
    
        return result;
    }

}
