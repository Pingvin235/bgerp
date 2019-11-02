package ru.bgcrm.plugin.bgbilling.event.listener;

import net.sf.saxon.s9api.Processor;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.listener.EventListener;
import ru.bgcrm.plugin.bgbilling.docgen.CommonContractExtensionFunction;
import ru.bgcrm.plugin.bgbilling.docgen.ContractCardExtensionFunction;
import ru.bgcrm.plugin.bgbilling.docgen.ContractCardForUserExtensionFunction;
import ru.bgcrm.plugin.bgbilling.docgen.CustomerExtensionFunction;
import ru.bgcrm.plugin.bgbilling.event.RegisterExtensionFunctionsEvent;
import ru.bgcrm.util.sql.ConnectionSet;

public class RegisterExtensionListener
{
	public RegisterExtensionListener()
	{
		EventProcessor.subscribe( new EventListener<RegisterExtensionFunctionsEvent>()
		{
			@Override
			public void notify( RegisterExtensionFunctionsEvent e, ConnectionSet connectionSet )
			{
				Processor proc = e.getProc();

				proc.registerExtensionFunction( new CommonContractExtensionFunction( connectionSet.getConnection() ) );
				proc.registerExtensionFunction( new ContractCardExtensionFunction( connectionSet.getConnection(), e.getUser() ) );
				proc.registerExtensionFunction( new ContractCardForUserExtensionFunction( connectionSet.getConnection() ) );
				proc.registerExtensionFunction( new CustomerExtensionFunction( connectionSet.getConnection(), e.getUser() ) );
			}
		}, RegisterExtensionFunctionsEvent.class );
	}
}
