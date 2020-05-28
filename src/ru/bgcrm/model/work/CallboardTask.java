package ru.bgcrm.model.work;

import java.util.Date;

public class CallboardTask
{
	int process;
	int group;
	int team;
	int graph;
	Date date;
	
	public CallboardTask( int process, int group, int team, int graph, Date date ) 
	{
		this.process = process;
		this.group = group;
		this.team = team;
		this.graph = graph;
		this.date = date;
	}

	public int getProcess()
	{
		return process;
	}

	public void setProcess( int process )
	{
		this.process = process;
	}

	public int getGroup()
	{
		return group;
	}

	public void setGroup( int group )
	{
		this.group = group;
	}

	public int getTeam()
	{
		return team;
	}

	public void setTeam( int team )
	{
		this.team = team;
	}

	public int getGraph()
	{
		return graph;
	}

	public void setGraph( int graph )
	{
		this.graph = graph;
	}

	public Date getDate()
	{
		return date;
	}

	public void setDate( Date date )
	{
		this.date = date;
	}
}
