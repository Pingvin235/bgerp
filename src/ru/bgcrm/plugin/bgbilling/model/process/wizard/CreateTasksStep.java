package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ru.bgcrm.model.process.wizard.Step;
import ru.bgcrm.model.process.wizard.StepData;
import ru.bgcrm.model.process.wizard.WizardData;
import ru.bgcrm.plugin.bgbilling.ContractTypesConfig;
import ru.bgcrm.plugin.bgbilling.model.ContractType;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

/** 
 * Использовать процессы BGCRM. 
 */
@Deprecated
public class CreateTasksStep
    extends Step
{
	private static final Logger log = Logger.getLogger( CreateTasksStep.class );
	
	public static String BILLING_TASK_OBJECT_TYPE = "bgbilling-task";

	public static class TaskCreateRule
	{
		public String expression;
		public ContractType contractType;
		public int taskTypeId;
		public int taskGroupId;
		public int taskStatusId;
		public int addressParamId;
		public String taskDescription;
		public String copyProcessParamsMapping;
		public int tariffId;
		public int tariffPos;
		public List<Integer> contractNumberParamIds;
		// дополнительные параметры, передаются в скрипт
		public ParameterMap config;		
	}
	
	private final ContractTypesConfig typesConfig;
	private final List<TaskCreateRule> ruleList = new ArrayList<CreateTasksStep.TaskCreateRule>();

	private String objectType = null;

	public String getObjectType()
	{
		return objectType;
	}

	public void setObjectType( String objectType )
	{
		this.objectType = objectType;
	}

	public CreateTasksStep( ParameterMap config )
    {
	    super( config );

	    setObjectType( config.get( "objectType", "task" ) );

	    typesConfig = new ContractTypesConfig( config, "contractType." );
	    
	    for( ParameterMap param : config.subIndexed( "rule." ).values() )
	    {
	    	TaskCreateRule rule = new TaskCreateRule();
	    	
	    	rule.expression = param.get( "expression" );
	    	rule.contractType = typesConfig.getTypeMap().get( param.getInt( "contractTypeId", 0 ) );
	    	rule.taskTypeId = param.getInt( "taskTypeId", 0 );
	    	rule.taskGroupId = param.getInt( "taskGroupId", 0 );
	    	rule.taskStatusId = param.getInt( "taskStatusId", -1 );
	    	rule.addressParamId = param.getInt( "addressParamId", 0 );
	    	rule.contractNumberParamIds = Utils.toIntegerList( param.get( "contractNumberParamIds" ) );
	    	rule.taskDescription = param.get( "taskDescription", "" );
	    	rule.copyProcessParamsMapping = param.get( "copyProcessParamsMapping", "" );
	    	rule.tariffId = param.getInt( "tariffId", -1 );
	    	rule.tariffPos = param.getInt( "tariffPos", 0 );
	    	rule.config = param;
	    	
	    	if( Utils.isBlankString( rule.expression ) || rule.contractType == null )
	    	{
	    		log.error( "Incorrect rule, expression: " + rule.expression );
	    		continue;
	    	}
	    	
	    	if( log.isDebugEnabled() )
	    	{
	    		log.debug( "Load rule: " + rule.expression );
	    	}
	    	
	    	ruleList.add( rule );
	    }
    }
	
	public List<TaskCreateRule> getRuleList()
    {
    	return ruleList;
    }

	@Override
    public String getJspFile()
    {
		return "/WEB-INF/jspf/usermob/plugin/bgbilling/step_create_tasks.jsp";
    }

	@Override
    public StepData<?> newStepData( WizardData data )
    {
	    return new CreateTasksStepData( this, data );
    }
}