package ru.bgcrm.model.process.queue;

public class QueueUserStat
{
    private String userTitle;
    private int processCount;
    
    public String getUserTitle()
    {
        return userTitle;
    }
    public void setUserTitle( String userTitle )
    {
        this.userTitle = userTitle;
    }
    public int getProcessCount()
    {
        return processCount;
    }
    public void setProcessCount( int processCount )
    {
        this.processCount = processCount;
    }
}