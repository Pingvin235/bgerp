package ru.bgcrm.model.process.wizard;

import ru.bgcrm.util.ParameterMap;

public class SetDescriptionStep
	extends Step
{
	public SetDescriptionStep( ParameterMap config )
	{
		super( config );
	}
	
	@Override
	public String getJspFile()
	{
		return "/WEB-INF/jspf/usermob/process/process/wizard/step_set_description.jsp";
	}

	@Override
	public SetDescriptionStepData newStepData( WizardData data )
	{
		return new SetDescriptionStepData( this, data );
	}

}
