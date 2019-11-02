package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import java.sql.Connection;
import java.util.List;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.process.wizard.StepData;
import ru.bgcrm.model.process.wizard.WizardData;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.struts.form.DynActionForm;

public class FindContractStepData extends StepData<FindContractStep> {
	private List<CommonObjectLink> contractLinkList;

	public FindContractStepData(FindContractStep step, WizardData data) {
		super(step, data);
	}

	@Override
	public boolean isFilled(DynActionForm form, Connection con) throws BGException {
		if (contractLinkList == null) {
			ProcessLinkDAO linkDao = new ProcessLinkDAO(con);
			contractLinkList = linkDao.getObjectLinksWithType(data.getProcess().getId(), Contract.OBJECT_TYPE + "%");
		}

		return contractLinkList.size() == 1;
	}

	public String getBillingId() {
		return step.getBillingId();
	}
}
