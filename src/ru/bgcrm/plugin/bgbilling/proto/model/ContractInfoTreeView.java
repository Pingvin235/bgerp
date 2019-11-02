package ru.bgcrm.plugin.bgbilling.proto.model;

import java.util.ArrayList;
import java.util.List;

import ru.bgcrm.plugin.bgbilling.proto.model.ContractInfo.ActionName;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractInfo.ParameterValue;

public class ContractInfoTreeView
{
	private String title;
	private String value;
	private ActionName detailInfo;
	private List<ContractInfoTreeView> children;
	private List<ParameterValue> actionParameters;

	public ContractInfoTreeView( List<ContractInfoTreeView> children, String title, String value, ActionName detailInfo, ParameterValue... actionParameters )
	{
		setTitle( title );
		setValue( value );
		detailInfo( detailInfo );
		if( children != null )
		{
			setChildren( children );
		}

		this.actionParameters = new ArrayList<ParameterValue>();
		for( int i = 0; i < actionParameters.length; i++ )
			this.actionParameters.add( actionParameters[i] );
	}

	public ContractInfoTreeView( String title, String value, ActionName detailInfo, ParameterValue... actionParameters )
	{
		setTitle( title );
		setValue( value );
		detailInfo( detailInfo );
		children = new ArrayList<ContractInfoTreeView>();

		this.actionParameters = new ArrayList<ParameterValue>();
		for( int i = 0; i < actionParameters.length; i++ )
			this.actionParameters.add( actionParameters[i] );
	}

	public void addChild( ContractInfoTreeView child )
	{
		if( child != null )
		{
			children.add( child );
		}
	}

	public void addChild( List<ContractInfoTreeView> children, String title, String value, ActionName detailInfo, ParameterValue... actionParameters )
	{
		children.add( new ContractInfoTreeView( children, title, value, detailInfo, actionParameters ) );
	}

	public void addChild( String title, String value, ActionName detailInfo, ParameterValue... actionParameters )
	{
		children.add( new ContractInfoTreeView( title, value, detailInfo, actionParameters ) );
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle( String title )
	{
		this.title = title;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue( String value )
	{
		this.value = value;
	}

	public List<ContractInfoTreeView> getChildren()
	{
		return children;
	}

	public void setChildren( List<ContractInfoTreeView> children )
	{
		this.children = children;
	}

	public ActionName detailInfo()
	{
		return detailInfo;
	}

	public void detailInfo( ActionName detailInfo )
	{
		this.detailInfo = detailInfo;
	}

	public List<ParameterValue> getActionParameterList()
	{
		return actionParameters;
	}
}
