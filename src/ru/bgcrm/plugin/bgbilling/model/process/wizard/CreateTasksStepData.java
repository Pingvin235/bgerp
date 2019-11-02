package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.AddressDistributionDAO;
import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.dao.CustomerLinkDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.UserEvent;
import ru.bgcrm.event.link.LinkAddingEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Customer;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.wizard.LinkCustomerStepData;
import ru.bgcrm.model.process.wizard.StepData;
import ru.bgcrm.model.process.wizard.WizardData;
import ru.bgcrm.plugin.bgbilling.dao.CommonContractDAO;
import ru.bgcrm.plugin.bgbilling.dao.ContractDAO;
import ru.bgcrm.plugin.bgbilling.model.CommonContract;
import ru.bgcrm.plugin.bgbilling.model.ContractType;
import ru.bgcrm.plugin.bgbilling.model.process.wizard.CreateTasksStep.TaskCreateRule;
import ru.bgcrm.plugin.bgbilling.proto.dao.AddressQuarterDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractTariffDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.CrmDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SingleConnectionConnectionSet;

/** 
 * Использовать процессы BGCRM. 
 */
@Deprecated
public class CreateTasksStepData
extends StepData<CreateTasksStep>
{
	private static final Logger log = Logger.getLogger( CreateTasksStepData.class );
	private static final int TASK_STATUS_CLOSED = 2;
	private static final int PROCESS_PARAM_ADDRESS = 90;
	private static final boolean LOAD_DIRS = true;

	private List<CommonObjectLink> contractLinkList;
	private List<CommonObjectLink> taskLinkList;
	private Customer customer;
	private CommonContract commonContract;

	public CreateTasksStepData( CreateTasksStep step, WizardData data )
	{
		super( step, data );
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean isFilled( DynActionForm form, Connection con )
	throws Exception
	{
		List<StepData<?>> stepDataList = data.getStepDataList();
		ProcessLinkDAO processLinkDao = new ProcessLinkDAO( con );
		CustomerLinkDAO customerLinkDao = new CustomerLinkDAO( con );

		CommonObjectLink customerLink = Utils.getFirst( processLinkDao.getObjectLinksWithType( data.getProcess().getId(), Customer.OBJECT_TYPE ) );
		if( customerLink != null )
		{
			customer = new CustomerDAO( con ).getCustomerById( customerLink.getLinkedObjectId() );
		}

		for( int i = stepDataList.indexOf( this ); i >= 0; i-- )
		{
			StepData<?> stepData = stepDataList.get( i );

			if( stepData instanceof LinkCommonContractStepData )
			{
				commonContract = ((LinkCommonContractStepData)stepData).getCommonContract();
			}
			else if( customer == null && stepData instanceof LinkCustomerStepData )
			{
				customer = ((LinkCustomerStepData)stepData).getCustomer();
			}

			if( commonContract != null && customer != null )
			{
				break;
			}
		}

		if( "contract".equals( step.getObjectType() ) )
		{
			contractLinkList = processLinkDao.getObjectLinksWithType( data.getProcess().getId(), "contract:%" );
		}

		taskLinkList = processLinkDao.getObjectLinksWithType( data.getProcess().getId(), "bgbilling-task:%" );

		// пока только с едиными договорами
		if( customer != null && (("task".equals( step.getObjectType() ) && taskLinkList.isEmpty()) || ("contract".equals( step.getObjectType() ) && contractLinkList.isEmpty())) )
		{
			CommonContractDAO commonContractDAO = new CommonContractDAO( con );
			ParamValueDAO dao = new ParamValueDAO( con );
			Map valuesCache = new HashMap();

			for( TaskCreateRule rule : step.getRuleList() )
			{
				String contractTitle = null;

				for( Integer paramId : rule.contractNumberParamIds )
				{
					contractTitle = dao.getParamText( data.getProcess().getId(), paramId );
					if( Utils.notBlankString( contractTitle ) )
					{
						break;
					}
				}

				// Либо единый договор либо номер указывается в параметре
				if( commonContract == null && Utils.isBlankString( contractTitle ) )
				{
					log.debug( "Common contract is null and number is null!" );
					continue;
				}

				if( Utils.notBlankString( rule.expression ) &&
					!dao.paramValueFilter( rule.expression, data.getProcess().getId(), valuesCache ) )
				{
					log.debug( "Expression filter not ok, rule: " + rule.expression );
					continue;
				}

				log.debug( "Create task, rule: " + rule.expression );

				ContractType contractType = rule.contractType;

				CrmDAO crmDAO = new CrmDAO( data.getUser(), contractType.getBillingId() );

				// Для единого договора получение номера;
				if( Utils.isBlankString( contractTitle ) )
				{
					contractTitle = commonContractDAO.getContractNumber( commonContract.getId(), contractType.getServiceCode() );
				}

				// Ищем id договора по изместному title в привязке процесса, КА, в биллинге;

				Contract contract = new Contract();
				contract.setTitle( contractTitle );

				contract.setId( searchContractIdByTitleInList( contractTitle, processLinkDao.getObjectLinksWithType( data.getProcess().getId(), "contract:" + contractType.getBillingId() ) ) );

				if( contract.getId() <= 0 )
				{
					contract.setId( searchContractIdByTitleInList( contractTitle, customerLinkDao.getObjectLinksWithType( customer.getId(), "contract:" + contractType.getBillingId() ) ) );
				}

				if( contract.getId() <= 0 )
				{
					contract.setId( searchContractInBilling( contractTitle, contractType.getBillingId() ) );
				}

				// Если id договора определить не удалось, пытаемся создать договор в биллинге;											
				if( contract.getId() <= 0 )
				{
					log.debug( "Creating contract: " + contract.getTitle() );

					try
					{
						contract = new ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO( data.getUser(), contractType.getBillingId() ).createContract( contractType.getPatternId(), "", contractTitle, "" );
					}
					catch( BGException e )
					{
						String message = String.format( "Не удалось выполнить создание договора %s:%s, задача с типом %d", contractType.getBillingId(), contractTitle, rule.taskTypeId );
						throw new BGMessageException( message + "\n" + e.getMessage() );
					}
				}

				CommonObjectLink contractLink = new CommonObjectLink( Customer.OBJECT_TYPE, customer.getId(), "contract:" + contractType.getBillingId(), contract.getId(), contract.getTitle() );

				LinkAddingEvent linkAddingEvent = new LinkAddingEvent( new DynActionForm( data.getUser() ), contractLink );
				EventProcessor.processEvent( linkAddingEvent, new SingleConnectionConnectionSet( con ) );

				customerLinkDao.addLinkIfNotExist( contractLink );

				contractLink.setObjectType( Process.OBJECT_TYPE );
				contractLink.setObjectId( data.getProcess().getId() );
				processLinkDao.addLinkIfNotExist( contractLink );

				ContractDAO contractDAO = new ContractDAO( data.getUser(), contractType.getBillingId() );

				// Копирование параметров процесса в договор (адрес);
				if( Utils.notBlankString( rule.copyProcessParamsMapping ) )
				{
					contractDAO.copyObjectParamsToContract( con, rule.copyProcessParamsMapping, data.getProcess().getId(), contract.getId(), null, null );
				}

				// Копирование параметров единого договора, контрагента;
				contractDAO.copyParametersToBilling( con, customer.getId(), contract.getId(), contractTitle );

				// Установка тарифа;
				if( contract.getId() > 0 && rule.tariffId > 0 )
				{
					new ContractTariffDAO( data.getUser(), contractType.getBillingId() ).setTariffPlan( contract.getId(), rule.tariffId, rule.tariffPos );
				}

				//TODO: В шаге смешано создание договоров и процессов, возможно в перспективе сделать отдельный шаг создания договоров.

				if( rule.taskTypeId <= 0 || rule.taskStatusId < 0 || rule.addressParamId <= 0 )
				{
					log.debug( "No task create configured." );
					continue;
				}

				ProcessType type = ProcessTypeCache.getProcessType( data.getProcess().getTypeId() );

				String text = rule.taskDescription;
				if( Utils.isBlankString( text ) )
				{
					GetTaskDescriptionEvent event = new GetTaskDescriptionEvent( new DynActionForm( data.getUser() ), rule, data.getProcess(), contract );
					EventProcessor.processEvent( event, type.getProperties().getActualScriptName(), new SingleConnectionConnectionSet( con ) );
					text = event.getText();
				}

				String executeDate = null;
				String targetDate = null, targetTime = null;
				if( rule.taskStatusId == TASK_STATUS_CLOSED )
				{
					executeDate = targetDate = new SimpleDateFormat( "dd.MM.yyyy" ).format( new Date() );
					targetTime = "00:00";
				}

				AddressHouse addressHouse = Utils.getFirst( dao.getParamAddressExt( data.getProcess().getId(), PROCESS_PARAM_ADDRESS, LOAD_DIRS ).values() )
												 .getHouse();

				// rule.taskGroupId == 0 if value not set in configuration directly;
				// In this case we try get group from billing by address house quarter;		
				if( rule.taskGroupId == 0 )
				{

					rule.taskGroupId = new AddressQuarterDAO( data.getUser(), contractType.getBillingId() )
					.getGruopByQuarter( addressHouse.getQuarterId(), addressHouse.getAddressQuarter().getCityId(), AddressQuarterDAO.QNAME_FILTER_OFF );
				}

				if( !isNewScheme( con, addressHouse ) )
				{
					// создание и привязка задач
					String taskId = crmDAO.createTask( contract.getId(), 0, rule.taskTypeId, rule.taskGroupId, rule.taskStatusId,
													   targetDate, targetTime, executeDate, "", "", rule.addressParamId, text );
					CommonObjectLink link = new CommonObjectLink();
					link.setLinkedObjectId( Utils.parseInt( taskId ) );
					link.setLinkedObjectType( "bgbilling-task:" + contractType.getBillingId() );
					link.setLinkedObjectTitle( "" );
					link.setObjectId( data.getProcess().getId() );
					link.setConfigMap( new Preferences( "taskTypeId=" + rule.taskTypeId ) );

					processLinkDao.addLink( link );
				}
			}
		}

		if( "contract".equals( step.getObjectType() ) )
		{
			contractLinkList = processLinkDao.getObjectLinksWithType( data.getProcess().getId(), "contract:%" );
		}

		taskLinkList = processLinkDao.getObjectLinksWithType( data.getProcess().getId(), "bgbilling-task:%" );

		boolean isFilled = false;
		if( "contract".equals( step.getObjectType() ) )
		{
			isFilled = contractLinkList.size() > 0;
		}
		else if( "task".equals( step.getObjectType() ) )
		{
			isFilled = taskLinkList.size() > 0;
		}

		return isFilled;
	}

	private boolean isNewScheme( Connection con, AddressHouse addressHouse )
	throws BGException
	{
		AddressDistributionDAO distrDAO = new AddressDistributionDAO( con );

		int distrId = step.getConfig().getInt( "distrbutionId", 0 );
		List<Integer> groupIds = Utils.toIntegerList( step.getConfig().get( "groupIds" ) );

		if( distrId > 0 && groupIds.size() > 0 )
		{
			int groupId = distrDAO.getGroupIdByQuarter( distrId, addressHouse.getQuarterId() );
			if( groupIds.contains( groupId ) )
			{
				return true;
			}
		}
		return false;
	}

	private int searchContractIdByTitleInList( String title, List<CommonObjectLink> linkList )
	{
		for( CommonObjectLink link : linkList )
		{
			if( title.equals( link.getLinkedObjectTitle() ) )
			{
				return link.getLinkedObjectId();
			}
		}
		return 0;
	}

	private int searchContractInBilling( String title, String billingId )
	throws BGException
	{
		ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO protoContractDAO = new ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO( data.getUser(), billingId );
		SearchResult<IdTitle> contractResult = new SearchResult<>();
		protoContractDAO.searchContractByTitleComment( contractResult, title, null, null );
		for( IdTitle idTitle : contractResult.getList() )
		{
			if( idTitle.getTitle().contains( title ) )
			{
				return idTitle.getId();
			}
		}
		return 0;
	}

	public List<CommonObjectLink> getTaskLinkList()
	{
		return taskLinkList;
	}

	public CommonContract getCommonContract()
	{
		return commonContract;
	}

	public Customer getCustomer()
	{
		return customer;
	}

	public static class GetTaskDescriptionEvent
	extends UserEvent
	{
		private final TaskCreateRule rule;
		private final Process process;
		private final Contract contract;
		private String text;

		public GetTaskDescriptionEvent( DynActionForm form, TaskCreateRule rule, Process process, Contract contract )
		{
			super( form );
			this.rule = rule;
			this.process = process;
			this.contract = contract;
		}

		public TaskCreateRule getRule()
		{
			return rule;
		}

		public Process getProcess()
		{
			return process;
		}

		public Contract getContract()
		{
			return contract;
		}

		public String getText()
		{
			return text;
		}

		public void setText( String text )
		{
			this.text = text;
		}
	}
}
