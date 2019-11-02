package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import java.util.SortedMap;

import ru.bgcrm.model.process.wizard.Step;
import ru.bgcrm.model.process.wizard.StepData;
import ru.bgcrm.model.process.wizard.WizardData;
import ru.bgcrm.util.ParameterMap;

public class ShowContractsByAddrParamStep
    extends Step
{
	private final int addressParamId;
	private final SortedMap<Integer,ParameterMap> billingConfig;

	public ShowContractsByAddrParamStep( ParameterMap config )
	{
		super( config );

		addressParamId = config.getInt( "addressParamId", 0 );
		billingConfig = config.subIndexed( "billing." );
	}

	@Override
	public String getJspFile()
	{
		return "/WEB-INF/jspf/usermob/plugin/bgbilling/step_show_contract_by_address.jsp";
	}

	@Override
	public StepData<?> newStepData( WizardData data )
	{
		return new ShowContractsByAddrParamStepData( this, data );
	}

	public int getAddressParamId()
	{
		return addressParamId;
	}

	public SortedMap<Integer, ParameterMap> getBillingConfig()
    {
    	return billingConfig;
    }

}
