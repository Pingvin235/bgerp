package ru.bgcrm.event.client;

import java.util.HashMap;
import java.util.Map;

/**
 * Сообщение о необходимости открыть вкладку договора,
 * либо обновить, если она уже открыта.
 */
public class ContractOpenEvent
	extends ClientEventWithParamMap
{
	public ContractOpenEvent( String billingId, int contractId )
	{
		super( getParams( billingId, contractId ) );
	}

	private static Map<String, String> getParams( String billingId, int contractId )
	{
		Map<String, String> params = new HashMap<String, String>();

		params.put( "billingId", billingId );
		params.put( "contractId", String.valueOf( contractId ) );

		return params;
	}
}
