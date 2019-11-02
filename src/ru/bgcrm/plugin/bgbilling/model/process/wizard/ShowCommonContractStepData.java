package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Customer;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.process.wizard.StepData;
import ru.bgcrm.model.process.wizard.WizardData;
import ru.bgcrm.plugin.bgbilling.dao.CommonContractDAO;
import ru.bgcrm.plugin.bgbilling.model.CommonContract;
import ru.bgcrm.struts.form.DynActionForm;

public class ShowCommonContractStepData
	extends StepData<ShowCommonContractStep>
{
	private int processId;
	private List<CommonContract> commonContractList;
	private Map<Integer, Customer> customerMap;
	private List<CommonObjectLink> processCustomerLinkList = new ArrayList<CommonObjectLink>();

	public ShowCommonContractStepData( ShowCommonContractStep step, WizardData data )
	{
		super( step, data );

		processId = data.getProcess().getId();
		commonContractList = new ArrayList<CommonContract>();
		customerMap = new HashMap<Integer, Customer>();
	}

	@Override
	public boolean isFilled( DynActionForm form, Connection con )
		throws BGException
	{
		ProcessLinkDAO processLinkDAO = new ProcessLinkDAO( con );
		processCustomerLinkList = processLinkDAO.getObjectLinksWithType( processId, Customer.OBJECT_TYPE );

		if( step.getAddressParamId() > 0 && step.getCommonContractAddressParamId() > 0 )
		{
			try
			{
				ParamValueDAO paramValueDAO = new ParamValueDAO( con );
				ParameterAddressValue address = paramValueDAO.getParamAddress( processId, step.getAddressParamId(), 1 );
				if( address == null )
				{
					return true;
				}
				CommonContractDAO commonContractDAO = new CommonContractDAO( con );
				CustomerDAO customerDAO = new CustomerDAO( con );

				String sql = " SELECT cc.id AS contract_id FROM bgbilling_common_contract AS cc " +
							 " INNER JOIN param_address AS pa ON cc.id=pa.id AND pa.param_id=?" +
							 " WHERE pa.house_id=? AND pa.flat=? ";

				PreparedStatement ps = con.prepareStatement( sql );
				ps.setInt( 1, step.getCommonContractAddressParamId() );
				ps.setInt( 2, address.getHouseId() );
				ps.setString( 3, address.getFlat() );

				ResultSet rs = ps.executeQuery();

				while( rs.next() )
				{
					int commonContractId = rs.getInt( "contract_id" );
					CommonContract contract = commonContractDAO.getContractById( commonContractId );
					commonContractList.add( contract );
					customerMap.put( contract.getId(), customerDAO.getCustomerById( contract.getCustomerId() ) );
				}

				ps.close();
			}
			catch( SQLException e )
			{
				throw new BGException( e );
			}
		}

		return true;
	}

	public int getProcessId()
	{
		return processId;
	}

	public List<CommonContract> getCommonContractList()
	{
		return commonContractList;
	}

	public Map<Integer, Customer> getCustomerMap()
	{
		return customerMap;
	}

	public List<CommonObjectLink> getProcessCustomerLinkList()
	{
		return processCustomerLinkList;
	}
}
