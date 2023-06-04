package ru.bgcrm.plugin.bgbilling.proto.model.ipn;

import java.util.ArrayList;
import java.util.List;

import org.bgerp.model.base.Id;
import org.w3c.dom.Element;

import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

public class IpnGate
	extends Id
{
	private int parentId;
	private String host;
	private int port;
	private String type;
	private int typeId;
	private String address;
	private String comment;

	private final List<IpnGate> childList = new ArrayList<IpnGate>();

	//<row address=" г. Уфа, 1-й пер. Водников, д. 5В" comment="Ленина 10/2" host="1.1.1.1" id="95" parent_id="0" port="1" type="PPPoe_script_test" typeId="62"/>
	public IpnGate( Element el )
	{
		this.id = Utils.parseInt( el.getAttribute( "id" ) );
		this.host = el.getAttribute( "host" );
		this.port = Utils.parseInt( el.getAttribute( "port" ) );
		this.type = el.getAttribute( "type" );
		this.typeId  = Utils.parseInt( el.getAttribute( "typeId" ) );
		this.address = el.getAttribute( "address" );
		this.parentId = Utils.parseInt( el.getAttribute( "parent_id") );
		this.comment = el.getAttribute( "comment" );

		for( Element childEl : XMLUtils.selectElements( el, "row" ) )
		{
			childList.add( new IpnGate( childEl ) );
		}
	}

	public int getParentId()
	{
		return parentId;
	}

	public String getHost()
	{
		return host;
	}

	public int getPort()
	{
		return port;
	}

	public String getType()
	{
		return type;
	}

	public int getTypeId()
	{
		return typeId;
	}

	public String getAddress()
	{
		return address;
	}

	public String getComment()
	{
		return comment;
	}

	public List<IpnGate> getChildList()
	{
		return childList;
	}
}