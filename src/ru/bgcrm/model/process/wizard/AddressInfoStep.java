package ru.bgcrm.model.process.wizard;

import ru.bgcrm.util.ParameterMap;

public class AddressInfoStep
    extends Step
{
	private final int addressParamId; 
	
	public AddressInfoStep( ParameterMap config )
    {
	    super( config );
	    
	    addressParamId = config.getInt( "addressParamId", 0 );
    }

	@Override
	public String getJspFile()
	{
		return "/WEB-INF/jspf/usermob/process/process/wizard/step_address_info.jsp";
	}

	@Override
	public StepData<?> newStepData( WizardData data )
	{
		return new AddressInfoStepData( this, data );
	}

	public int getAddressParamId()
    {
    	return addressParamId;
    }
}
