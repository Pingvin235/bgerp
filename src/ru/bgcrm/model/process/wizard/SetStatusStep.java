package ru.bgcrm.model.process.wizard;

import ru.bgcrm.util.ParameterMap;

public class SetStatusStep
	extends Step
{
	public SetStatusStep( ParameterMap config )
	{
		super( config );
	}
	
	@Override
	public String getJspFile()
	{
		return "/WEB-INF/jspf/usermob/process/process/wizard/step_set_status.jsp";
	}

	@Override
	public SetStatusStepData newStepData( WizardData data )
	{
		return new SetStatusStepData( this, data );
	}
}