package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import ru.bgcrm.model.process.wizard.Step;
import ru.bgcrm.model.process.wizard.StepData;
import ru.bgcrm.model.process.wizard.WizardData;
import ru.bgcrm.util.ParameterMap;

public class ShowCommonContractStep
	extends Step
{
	private final int addressParamId;
	private final int commonContractAddressParamId;

	public ShowCommonContractStep( ParameterMap config )
	{
		super( config );

		addressParamId = config.getInt( "addressParamId", 0 );
		commonContractAddressParamId = config.getInt( "commonContractAddressParamId", 0 );
	}

	@Override
	public String getJspFile()
	{
		return "/WEB-INF/jspf/usermob/plugin/bgbilling/show_common_contract.jsp";
	}

	@Override
	public StepData<?> newStepData( WizardData data )
	{
		return new ShowCommonContractStepData( this, data );
	}

	public int getAddressParamId()
	{
		return addressParamId;
	}

	public int getCommonContractAddressParamId()
	{
		return commonContractAddressParamId;
	}

}
