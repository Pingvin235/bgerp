package ru.bgcrm.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Срочное сообщение об ошибке, отправляемое по почте.
 */
public class AlarmErrorMessage
    extends ErrorMessage
{
    // ключ, идентифицирующий аларм, используется для задания максимальной частоты писем по этому аларму
    // т.к. алармы могут возникать очень часто, нецелесообразно при каждом его возникновении слать почту
    // это может привести к фатальному росту нагрузки
    private String key;
    private Throwable exception;
    
    public AlarmErrorMessage( String key, String subject, String text )
    {
        super( subject, text );
        this.key = key;        
    }
    
    public AlarmErrorMessage( String key, String subject, String text, Throwable ex )
    {
        this( key, subject, text );
        this.exception = ex;
    }
    
    public String getKey()
    {
        return key;
    }

    @Override
    public String getText()
    {
        StringBuilder result = new StringBuilder( text );        
        if( exception != null )
        {
            StringWriter sw = new StringWriter();
            exception.printStackTrace( new PrintWriter( sw ) );
            result.append( "\n\n" );
            result.append( sw.toString() );
        }        
        return result.toString();
    }    
}