package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.model.base.IdTitle;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.process.wizard.StepData;
import ru.bgcrm.model.process.wizard.WizardData;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractTariffDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.plugin.bgbilling.proto.model.tariff.ContractTariff;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

public class SetContractTariffStepData extends StepData<SetContractTariffStep> {
	private Contract contract;
	private List<IdTitle> tariffList;
	private ContractTariff contractTariff;

	public SetContractTariffStepData(SetContractTariffStep step, WizardData data) {
		super(step, data);
	}

	@Override
	public boolean isFilled(DynActionForm form, Connection con) throws BGException {
		CommonObjectLink contractLink = Utils.getFirst(new ProcessLinkDAO(con).getObjectLinksWithType(data.getProcess().getId(), Contract.OBJECT_TYPE + "%"));
		if (contractLink == null)
			return false;

		contract = new Contract(StringUtils.substringAfter(contractLink.getLinkedObjectType(), ":"),
				contractLink.getLinkedObjectId());

		ContractTariffDAO tariffDao = new ContractTariffDAO(form.getUser(), contract.getBillingId());
		contractTariff = Utils.getFirst(tariffDao.contractTariffList(contract.getId()));
		tariffDao.getContractTariffPlan(-1, -1, contract.getId(), true, true, true, tariffList = new ArrayList<>());

		return contractTariff != null;
	}

	public Contract getContract() {
		return contract;
	}

	public List<IdTitle> getTariffList() {
		return tariffList;
	}

	public ContractTariff getContractTariff() {
		return contractTariff;
	}
}
