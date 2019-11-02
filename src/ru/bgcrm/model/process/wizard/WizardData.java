package ru.bgcrm.model.process.wizard;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Customer;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.Wizard;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;

public class WizardData {
	private final Process process;
	private final DynActionForm form;
	private final User user;
	private final List<StepData<?>> stepDataList = new ArrayList<StepData<?>>();
	private final boolean allFilled;

	public WizardData(Connection con, DynActionForm form, Wizard wizard, Process process, List<Step> stepList)
			throws Exception {
		this.process = process;
		this.form = form;
		this.user = form.getUser();

		boolean allFilled = true;

		for (Step step : stepList) {
			StepData<?> stepData = step.newStepData(this);

			if (stepData == null) {
				throw new BGException("Для шага " + step.getTitle() + " была возвращена нулевая StepData!");
			}

			if (stepData.check(con)) {
				stepDataList.add(stepData);

				// доходим до первого незаполненного параметра
				if (!stepData.isFilled(form, con)) {
					allFilled = false;
					break;
				}
			}
		}

		this.allFilled = allFilled;
	}

	public Process getProcess() {
		return process;
	}

	public DynActionForm getForm() {
		return form;
	}

	public User getUser() {
		return user;
	}

	public List<StepData<?>> getStepDataList() {
		return stepDataList;
	}

	public boolean isAllFilled() {
		return allFilled;
	}

	public Customer getLinkedCustomer() {
		Customer result = null;

		for (StepData<?> stepData : stepDataList) {
			if (stepData instanceof LinkCustomerStepData) {
				result = ((LinkCustomerStepData) stepData).getCustomer();
			}
		}

		return result;
	}
}