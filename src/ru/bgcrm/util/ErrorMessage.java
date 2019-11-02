package ru.bgcrm.util;

import java.util.Date;

public class ErrorMessage
{
    // тема ошибки
    protected String subject;
    // текст ошибки
    protected String text;
    // время регистрации
    private Date registrationTime;
    
    public ErrorMessage( String subject, String text )
    {
        this.subject = subject;
        this.text = text;
        this.registrationTime = new Date();
    }

    public String getSubject()
    {
        return subject;
    }

    public String getText()
    {
        return text;
    }

    public Date getRegistrationTime()
    {
        return registrationTime;
    }
    
    public void setRegistrationTime( Date time )
    {
    	registrationTime = time;
    }
}
