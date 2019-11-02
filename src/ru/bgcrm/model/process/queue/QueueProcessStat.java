package ru.bgcrm.model.process.queue;

public class QueueProcessStat
{
    private String typeTitle;
    private String statusTitle;
    private int processCount;
    
    public String getTypeTitle()
    {
        return typeTitle;
    }
    public void setTypeTitle( String typeTitle )
    {
        this.typeTitle = typeTitle;
    }
    public String getStatusTitle()
    {
        return statusTitle;
    }
    public void setStatusTitle( String statusTitle )
    {
        this.statusTitle = statusTitle;
    }
    public int getProcessCount()
    {
        return processCount;
    }
    public void setProcessCount( int count )
    {
        this.processCount = count;
    }    
}
