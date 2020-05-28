package ru.bgcrm.model.work;

public class WorkTypeDynamic
{
	public static final int TIME_CONTROL_POLITICS_ANYBODY = 1;
	public static final int TIME_CONTROL_POLITICS_USER = 2;
	public static final int TIME_CONTROL_POLITICS_ADMINISTRATOR = 3;
	
	public static final int TIME_SET_POLITICS_WHATEVER = 1;
	public static final int TIME_SET_POLITICS_SHIFT_BEGIN = 2;
	public static final int TIME_SET_POLITICS_SHIFT_SET = 3;
	
	private int userCount;
	private int timeControlPolitics;
	private int timeSetPolitics;

	public int getUserCount()
	{
		return userCount;
	}

	public void setUserCount( int userCount )
	{
		this.userCount = userCount;
	}

	public int getTimeControlPolitics()
	{
		return timeControlPolitics;
	}

	public void setTimeControlPolitics( int timeControlPolitics )
	{
		this.timeControlPolitics = timeControlPolitics;
	}

	public int getTimeSetPolitics()
	{
		return timeSetPolitics;
	}

	public void setTimeSetPolitics( int timeSetPolitics )
	{
		this.timeSetPolitics = timeSetPolitics;
	}	
}
