package ru.bgcrm.plugin.bgbilling.proto.model.ipn;

import org.w3c.dom.Element;

import ru.bgcrm.model.IdTitle;
import ru.bgcrm.util.Utils;

public class ContractGate
	extends IdTitle
{
	private final int gateId;
	private final int typeId;
	
	public ContractGate( Element element )
	{
		super( element );
		
		gateId = Utils.parseInt( element.getAttribute( "gid" ) );
		typeId = Utils.parseInt( element.getAttribute( "gtid" ) );
	}

	public int getGateId()
	{
		return gateId;
	}

	public int getTypeId()
	{
		return typeId;
	}
}
