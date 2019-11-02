package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;

import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Customer;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.process.wizard.FillParamsStepData;
import ru.bgcrm.model.process.wizard.LinkCustomerStepData;
import ru.bgcrm.model.process.wizard.StepData;
import ru.bgcrm.model.process.wizard.WizardData;
import ru.bgcrm.plugin.bgbilling.dao.CommonContractDAO;
import ru.bgcrm.plugin.bgbilling.model.CommonContract;
import ru.bgcrm.struts.form.DynActionForm;

public class LinkCommonContractStepData
    extends StepData<LinkCommonContractStep>
{
	private Customer customer;
	private ParameterAddressValue address;

	private List<CommonContract> customerCommonContractList;
	private CommonContract commonContract;

	public LinkCommonContractStepData( LinkCommonContractStep step, WizardData data )
	{
		super( step, data );
	}

	@Override
	public boolean isFilled( DynActionForm form, Connection connection )
	    throws BGException
	{
		loadCustomer( connection );
		loadAddress( connection );
		copyAddressToCustomer( connection );
		loadCommonContract( connection );

		if( commonContract == null )
		{
			loadCustomerCommonContractList( connection );
		}

		return commonContract != null;
	}

	private void loadCustomer( Connection connection )
	    throws BGException
	{
		List<StepData<?>> stepDataList = data.getStepDataList();

		int stepIndex = stepDataList.indexOf( this );
		while( customer == null && --stepIndex >= 0 )
		{
			StepData<?> stepData = stepDataList.get( stepIndex );

			if( stepData instanceof LinkCustomerStepData )
			{
				LinkCustomerStepData linkCustomerStepData = (LinkCustomerStepData)stepData;
				customer = linkCustomerStepData.getCustomer();
			}
		}

		if( customer == null )
		{
			int customerId = getLinkedCustomerId( connection );

			if( customerId > 0 )
			{
				CustomerDAO customerDAO = new CustomerDAO( connection );
				customer = customerDAO.getCustomerById( customerId );
			}
		}
	}

	/**
	 * Метод возвращает идентификатор контрагента, ранее привязанного
	 * к процессу через поиск связей процесс-контрагент базе данных.
	 * @param connection
	 * @return
	 * @throws BGException
	 */
	private int getLinkedCustomerId( Connection connection )
	    throws BGException
	{
		int customerId = 0;
		ProcessLinkDAO processLinkDAO = new ProcessLinkDAO( connection );
		List<CommonObjectLink> processCustomerlinkList = processLinkDAO.getObjectLinksWithType( data.getProcess().getId(), Customer.OBJECT_TYPE );

		if( processCustomerlinkList.size() > 0 )
		{
			customerId = processCustomerlinkList.get( 0 ).getLinkedObjectId();
		}

		return customerId;
	}

	private void loadCommonContract( Connection connection )
	    throws BGException
	{
		ProcessLinkDAO processLinkDAO = new ProcessLinkDAO( connection );
		List<CommonObjectLink> commonContractLinkList = processLinkDAO.getObjectLinksWithType( data.getProcess().getId(), CommonContract.OBJECT_TYPE );

		if( commonContractLinkList.size() > 0 )
		{
			CommonContractDAO commonContractDAO = new CommonContractDAO( connection );
			commonContract = commonContractDAO.getContractById( commonContractLinkList.get( 0 ).getLinkedObjectId() );
		}
	}

	private void loadAddress( Connection connection )
	    throws BGException
	{
		List<StepData<?>> stepDataList = data.getStepDataList();

		// Ищем заполненный адресный параметр по шагам класса
		// FillParamsStepData до тех пор пока не найдём заполнение
		// адреса процесса или не закончится история шагов.
		int stepIndex = stepDataList.indexOf( this );
		while( address == null && --stepIndex >= 0 )
		{
			StepData<?> stepData = stepDataList.get( stepIndex );

			// Проверяем является ли текущий шаг истории
			// экземпляром класса FillParamsStepData
			if( stepData instanceof FillParamsStepData )
			{
				FillParamsStepData fillParamsStepData = (FillParamsStepData)stepData;
				String objectType = fillParamsStepData.getStep().getType();

				// Проверяем не является ли текущий шаг класса FillParamsStepData
				// операцией по заполнению параметров контрагента или единого договора
				if( !"linkedCustomer".equals( objectType ) && !"bgbilling-commonContract".equals( objectType ) )
				{
					ParamValueDAO paramValueDAO = new ParamValueDAO( connection );
					SortedMap<Integer, ParameterAddressValue> parameterAddressValues = paramValueDAO.getParamAddress( fillParamsStepData.getObjectId(), step.getAddressParamId() );

					Iterator<Integer> iterator = parameterAddressValues.keySet().iterator();
					while( address == null && iterator.hasNext() )
					{
						Integer addressPosition = iterator.next();
						ParameterAddressValue parameterAddressValue = parameterAddressValues.get( addressPosition );

						if( parameterAddressValue != null )
						{
							address = parameterAddressValue;
						}
					}
				}
			}
		}
	}

	private void copyAddressToCustomer( Connection connection )
	    throws BGException
	{
		// добавляем адреса единого договора в контрагент
		if( customer != null && address != null && step.getCustomerAddressParamId() > 0 )
		{
			ParamValueDAO paramValueDAO = new ParamValueDAO( connection );
			SortedMap<Integer, ParameterAddressValue> customerAddress = paramValueDAO.getParamAddress( customer.getId(), step.getCustomerAddressParamId() );

			boolean addAddressFlag = true;

			Iterator<ParameterAddressValue> it = customerAddress.values().iterator();
			while( it.hasNext() )
			{
				ParameterAddressValue addressValue = it.next();
				if( address.equals( addressValue ) )
				{
					addAddressFlag = false;
					break;
				}
			}

			if( addAddressFlag )
			{
				paramValueDAO.updateParamAddress( customer.getId(), step.getCustomerAddressParamId(), -1, address );
			}
		}
	}

	private void loadCustomerCommonContractList( Connection connection )
	    throws BGException
	{
		if( customer != null )
		{
			CommonContractDAO commonContractDAO = new CommonContractDAO( connection );
			customerCommonContractList = commonContractDAO.getContractList( customer.getId() );

			if( step.isFilteredByAddress() && address != null )
			{
				Iterator<CommonContract> iterator = customerCommonContractList.iterator();
				while( iterator.hasNext() )
				{
					CommonContract commonContract = iterator.next();
					if( !address.equals( commonContract.getAddress() ) )
					{
						iterator.remove();
					}
				}
			}
		}
	}

	public List<CommonContract> getCustomerCommonContractList()
	{
		return customerCommonContractList;
	}

	public Customer getCustomer()
	{
		return customer;
	}

	public ParameterAddressValue getAddress()
	{
		return address;
	}

	public CommonContract getCommonContract()
	{
		return commonContract;
	}

	public boolean getManualTitleInput()
	{
		return step.getManualTitleInput();
	}

	public boolean isFilteredByAddress()
	{
		return step.isFilteredByAddress();
	}
}
