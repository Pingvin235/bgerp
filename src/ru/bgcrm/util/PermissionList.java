package ru.bgcrm.util;

import java.util.List;

public class PermissionList
{
	List<String> action = null;
	
	public PermissionList()
	{
		//action = new ArrayList<String>();
	}
	
	public PermissionList(List<String> action)
	{
		this.action = action;
	}
	
	public List<String> getAction()
	{
		return action;
	}
	
	public void setAction(List<String> action)
	{
		this.action = action;
	}
	
}
