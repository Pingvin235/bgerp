package ru.bgcrm.plugin.bgbilling.proto.model.ipn;

import java.util.ArrayList;
import java.util.List;

import org.bgerp.model.base.IdTitle;

public class IpnSource
	extends IdTitle
{
	private List<IdTitle> ifaceList = new ArrayList<IdTitle>();

	public IpnSource()
	{
		super();
	}

	public IpnSource( int id, String title )
	{
		super( id, title );
	}

	public List<IdTitle> getIfaceList()
	{
		return ifaceList;
	}

	public void setIfaceList( List<IdTitle> ifaceList )
	{
		this.ifaceList = ifaceList;
	}
}
