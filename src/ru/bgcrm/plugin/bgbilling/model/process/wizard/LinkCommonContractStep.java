package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import ru.bgcrm.model.process.wizard.Step;
import ru.bgcrm.model.process.wizard.StepData;
import ru.bgcrm.model.process.wizard.WizardData;
import ru.bgcrm.util.ParameterMap;

public class LinkCommonContractStep
    extends Step
{
	private final int addressParamId; 
	private final int customerAddressParamId;
	private final boolean manualTitleInput;
	private final boolean filteredByAddress;
	
	public LinkCommonContractStep( ParameterMap config )
    {
	    super( config );
	    
	    addressParamId = config.getInt( "addressParamId", 0 );
	    customerAddressParamId = config.getInt( "customerAddressParamId", 0 );
	    manualTitleInput = config.getBoolean( "manualTitleInput", false );
	    filteredByAddress = config.getBoolean( "filteredByAddress", false );
	}

	@Override
	public String getJspFile()
	{
		return "/WEB-INF/jspf/usermob/plugin/bgbilling/step_link_common_contract.jsp";
	}

	@Override
	public StepData<?> newStepData( WizardData data )
	{
		return new LinkCommonContractStepData( this, data );
	}
	
	public int getAddressParamId()
    {
    	return addressParamId;
    }

	public int getCustomerAddressParamId()
    {
    	return customerAddressParamId;
    }
	
	public boolean getManualTitleInput()
	{
		return manualTitleInput;
	}

	public boolean isFilteredByAddress()
	{
		return filteredByAddress;
	}
}
