package ru.bgcrm.model.work;

import java.util.Calendar;
import java.util.Date;

import ru.bgcrm.util.TimeUtils;

public class WorkTask
{
	public static final int PROCESS_ID_LOCK = -100;
	
	private int graphId;
	
	private int groupId;
	// ключ либо группа + юзер, либо группа + бригада
	private int team;	
	private int userId;
	
	// время начала - либо начало диапазона, либо попадает в диапазон
	private Date time;
	// процесс
	private int processId;
	// слот в диапазоне - начало
	private int slotFrom;
	// длительность в минутах
	private int duration;
	// слот в диапазоне - окончание (вычисляется)
	private int slotTo;
	// автоматически генерирующееся описание
	private String reference;

	public int getGraphId()
	{
		return graphId;
	}

	public void setGraphId( int graphId )
	{
		this.graphId = graphId;
	}

	public int getTeam()
	{
		return team;
	}

	public void setTeam( int team )
	{
		this.team = team;
	}

	public int getGroupId()
	{
		return groupId;
	}

	public void setGroupId( int groupId )
	{
		this.groupId = groupId;
	}

	public int getUserId()
	{
		return userId;
	}

	public void setUserId( int userId )
	{
		this.userId = userId;
	}

	public int getProcessId()
	{
		return processId;
	}

	public void setProcessId( int processId )
	{
		this.processId = processId;
	}

	public Date getTime()
	{
		return time;
	}

	public void setTime( Date time )
	{
		this.time = time;
	}
	
	public int getMinuteFrom()
	{
		Calendar c = TimeUtils.convertDateToCalendar( time );
		return c.get( Calendar.HOUR_OF_DAY ) * 60 + c.get( Calendar.MINUTE );
	}

	public int getSlotFrom()
	{
		return slotFrom;
	}

	public void setSlotFrom( int pos )
	{
		this.slotFrom = pos;
	}
	
	public int getDuration()
	{
		return duration;
	}

	public void setDuration( int duration )
	{
		this.duration = duration;
	}
	
	public int getSlotTo()
	{
		return slotTo;
	}

	public void setSlotTo( int positionTo )
	{
		this.slotTo = positionTo;
	}

	public String getReference()
	{
		return reference;
	}

	public void setReference( String reference )
	{
		this.reference = reference;
	}
	
	public boolean isLock()
	{
		return processId == PROCESS_ID_LOCK;
	}
}