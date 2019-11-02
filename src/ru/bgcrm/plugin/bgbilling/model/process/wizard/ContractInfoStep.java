package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import java.util.ArrayList;
import java.util.List;

import ru.bgcrm.model.process.wizard.Step;
import ru.bgcrm.model.process.wizard.StepData;
import ru.bgcrm.model.process.wizard.WizardData;
import ru.bgcrm.util.ParameterMap;

/**
 * Нелогичный класс, удалить впоследствии завязки на конкретные модули.
 */
@Deprecated
public class ContractInfoStep
	extends Step
{
	public List<Rule> rules;

	public static class Rule
	{
		public int checkParamId;
		public int paramValue;
		public String billingId;
		public int moduleId;
		public boolean showCommonContract;
	}

	public ContractInfoStep( ParameterMap config )
	{
		super( config );

		rules = new ArrayList<Rule>();
		for( ParameterMap param : config.subIndexed( "rule." ).values() )
		{
			Rule rule = new Rule();

			rule.checkParamId = param.getInt( "checkParamId", 0 );
			rule.paramValue = param.getInt( "paramValue", 0 );
			rule.billingId = param.get( "billingId" );
			rule.moduleId = param.getInt( "vpnModuleId", 0 );
			rule.showCommonContract = param.getBoolean( "showCommonContract", false );

			if( !"".equals( rule.billingId ) && rule.moduleId > 0 && rule.checkParamId > 0 )
			{
				rules.add( rule );
			}
		}
	}

	@Override
	public String getJspFile()
	{
		return "/WEB-INF/jspf/usermob/plugin/bgbilling/step_contract_info.jsp";
	}

	@Override
	public StepData<?> newStepData( WizardData data )
	{
		return new ContractInfoStepData( this, data );
	}

	public List<Rule> getRules()
	{
		return rules;
	}

}
