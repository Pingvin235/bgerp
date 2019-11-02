package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import java.util.Map;

import ru.bgcrm.model.process.wizard.Step;
import ru.bgcrm.model.process.wizard.StepData;
import ru.bgcrm.model.process.wizard.WizardData;
import ru.bgcrm.plugin.bgbilling.ContractTypesConfig;
import ru.bgcrm.plugin.bgbilling.model.ContractType;
import ru.bgcrm.util.ParameterMap;

public class CreateContractsStep  
	extends Step
{
	private final ContractTypesConfig typesConfig;
	private final boolean showContractTitle;

	public CreateContractsStep( ParameterMap config )
    {
	    super( config );
	    typesConfig = new ContractTypesConfig( config, "contractType." );
	    showContractTitle = config.getBoolean( "showContractTitle", false );
    }

	@Override
    public String getJspFile()
    {
	   return "/WEB-INF/jspf/usermob/plugin/bgbilling/step_create_contracts.jsp";
    }

	@Override
    public StepData<?> newStepData( WizardData data )
    {
	    return new CreateContractsStepData( this, data );
    }
	
	public Map<Integer, ContractType> getTypeMap()
	{
		return typesConfig.getTypeMap();
	}

	public boolean getShowContractTitle()
    {
    	return showContractTitle;
    }
}