package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import java.sql.Connection;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;

import ru.bgcrm.dao.CommonLinkDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.wizard.StepData;
import ru.bgcrm.model.process.wizard.WizardData;
import ru.bgcrm.plugin.bgbilling.dao.CommonContractDAO;
import ru.bgcrm.plugin.bgbilling.model.CommonContract;
import ru.bgcrm.plugin.bgbilling.model.process.wizard.ContractInfoStep.Rule;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.DialUpDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.struts.form.DynActionForm;

/**
 * Нелогичный класс, удалить впоследствии завязки на конкретные модули..
 */
@Deprecated
public class ContractInfoStepData
	extends StepData<ContractInfoStep>
{
	private String vpnLogin;
	private String vpnPassword;
	private CommonContract commonContract;

	public ContractInfoStepData( ContractInfoStep step, WizardData data )
	{
		super( step, data );
	}

	@Override
	public boolean isFilled( DynActionForm form, Connection con )
		throws Exception
	{
		ParamValueDAO paramValueDAO = new ParamValueDAO( con );
		CommonLinkDAO linkDAO = CommonLinkDAO.getLinkDAO( Process.OBJECT_TYPE, con );

		for( Rule rule : step.getRules() )
		{
			Set<Integer> vals = paramValueDAO.getParamList( data.getProcess().getId(), rule.checkParamId );
			if( !vals.contains( rule.paramValue ) )
			{
				continue;
			}

			List<CommonObjectLink> links = linkDAO.getObjectLinksWithType( data.getProcess().getId(), Contract.OBJECT_TYPE + ":%" );
			for( CommonObjectLink link : links )
			{
				String billingId = rule.billingId;
				int moduleId = rule.moduleId;

				if( billingId.equals( link.getLinkedObjectType().split( ":" )[1] ) )
				{
					int contractId = link.getLinkedObjectId();
					String contractTitle = link.getLinkedObjectTitle();

					ContractDAO contractDAO = ContractDAO.getInstance( data.getUser(), billingId );
					Document doc = contractDAO.getContractCardDoc( contractId );

					vpnLogin = DialUpDAO.getLogin( doc, moduleId, contractId );
					vpnPassword = DialUpDAO.getPassword( doc, moduleId, contractId );

					// если есть флаг, находим ЕД и вытаскиваем его пароль
					if( rule.showCommonContract )
					{
						for( CommonObjectLink commonContractLink : linkDAO.getObjectLinksWithType( data.getProcess().getId(), CommonContract.OBJECT_TYPE ) )
						{
							if( contractTitle.startsWith( commonContractLink.getLinkedObjectTitle() ) )
							{
								commonContract = new CommonContractDAO( con ).getContractById( commonContractLink.getLinkedObjectId() );
								break;
							}
						}
					}
					break;
				}
			}
		}

		return true;
	}

	public String getVpnLogin()
	{
		return vpnLogin;
	}

	public String getVpnPassword()
	{
		return vpnPassword;
	}

	public CommonContract getCommonContract()
	{
		return commonContract;
	}
}
