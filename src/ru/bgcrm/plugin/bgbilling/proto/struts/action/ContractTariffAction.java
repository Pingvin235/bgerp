package ru.bgcrm.plugin.bgbilling.proto.struts.action;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractTariffDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.DirectoryDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.tariff.ContractPersonalTariff;
import ru.bgcrm.plugin.bgbilling.proto.model.tariff.ContractTariff;
import ru.bgcrm.plugin.bgbilling.proto.model.tariff.ContractTariffGroup;
import ru.bgcrm.plugin.bgbilling.proto.model.tariff.ContractTariffOption;
import ru.bgcrm.plugin.bgbilling.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class ContractTariffAction extends BaseAction {
	public ActionForward tariff(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws BGException, ParseException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		ContractTariffDAO crmDAO = new ContractTariffDAO(form.getUser(), billingId);

		try {
			StringBuilder tariffs = new StringBuilder();

			List<ContractTariff> tariffList = crmDAO.contractTariffList(contractId);
			for (ContractTariff tariff : tariffList) {
				if (tariff.getDateTo() == null) {
					Utils.addCommaSeparated(tariffs, tariff.getTitle());
				}
			}
			List<ContractPersonalTariff> personalTariffList = crmDAO.contractPersonalTaraffList(contractId);
			for (ContractPersonalTariff tariff : personalTariffList) {
				if (tariff.getDateTo() == null) {
					Utils.addCommaSeparated(tariffs, tariff.getTitle());
				}
			}
			form.getResponse().setData("tariffs", tariffs);

		} catch (Exception e) {
			form.getResponse().setData("tariffs", "Нет прав просмотра");
		}

		try {
			StringBuilder options = new StringBuilder();

			List<ContractTariffOption> optionList = crmDAO.contractTariffOptionList(contractId);
			for (ContractTariffOption option : optionList) {
				Utils.addCommaSeparated(options, option.getOptionTitle());
			}
			form.getResponse().setData("options", options);
		} catch (Exception e) {
			form.getResponse().setData("options", "Нет прав просмотра");
		}

		try {
			StringBuilder groups = new StringBuilder();

			List<ContractTariffGroup> groupList = crmDAO.contractTariffGroupList(contractId);
			for (ContractTariffGroup group : groupList) {
				if (group.getDateTo() == null) {
					Utils.addCommaSeparated(groups, group.getTitle());
				}
			}
			form.getResponse().setData("groups", groups);
		} catch (Exception e) {
			form.getResponse().setData("groups", "Нет прав просмотра");
		}

		return html(conSet, mapping, form, "tariff");
	}

	public ActionForward contractTariffList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		ContractTariffDAO crmDAO = new ContractTariffDAO(form.getUser(), billingId);
		form.getResponse().setData("tariffList", crmDAO.contractTariffList(contractId));

		return html(conSet, mapping, form, "contractTariffList");
	}

	public ActionForward getContractTariff(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws BGException {
		String billingId = form.getParam("billingId");
		int contractId = form.getParamInt("contractId");
		int moduleId = form.getParamInt("moduleId");
		boolean useFilter = form.getParamBoolean("useFilter", false);
		boolean showUsed = form.getParamBoolean("showUsed", false);

		List<IdTitle> tariffList = new ArrayList<IdTitle>();
		form.getResponse().setData("tariffList", tariffList);

		ContractTariffDAO crmDAO = new ContractTariffDAO(form.getUser(), billingId);
		form.getResponse().setData("contractTariff", crmDAO.getContractTariffPlan(form.getId(), moduleId, contractId,
				useFilter, showUsed, false, tariffList));
		form.getResponse().setData("moduleList", new DirectoryDAO(form.getUser(), billingId).getBillingModuleList());

		return html(conSet, mapping, form, "contractTariffEditor");
	}

	public ActionForward addContractTariff(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws Exception {
		int tariffId = form.getParamInt("tariffId", -1);

		ContractTariffDAO contractDAO = new ContractTariffDAO(form.getUser(), form.getParam("billingId"));

		contractDAO.addTariffPlan(form.getParamInt("contractId", -1), tariffId, form.getParamInt("position", 0));

		return json(conSet, form);
	}

	public ActionForward setContractTariff(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws Exception {
		int tariffId = form.getParamInt("tariffId", -1);

		ContractTariffDAO contractDAO = new ContractTariffDAO(form.getUser(), form.getParam("billingId"));

		contractDAO.setTariffPlan(form.getParamInt("contractId", -1), tariffId);

		return json(conSet, form);
	}

	public ActionForward updateContractTariff(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws BGException {
		String billingId = form.getParam("billingId");
		String dateFrom = form.getParam("dateFrom");
		String dateTo = form.getParam("dateTo");
		String comment = form.getParam("comment");

		Integer contractId = form.getParamInt("contractId");
		Integer tariffPlanId = form.getParamInt("tariffPlanId");
		Integer position = form.getParamInt("position");

		ContractTariffDAO crmDAO = new ContractTariffDAO(form.getUser(), billingId);
		crmDAO.updateContractTariffPlan(contractId, form.getId(), tariffPlanId, position, dateFrom, dateTo, comment);

		return json(conSet, form);
	}

	public ActionForward deleteСontractTariff(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		ContractTariffDAO crmDAO = new ContractTariffDAO(form.getUser(), billingId);
		crmDAO.deleteContractTariffPlan(contractId, form.getId());

		return json(conSet, form);
	}

	public ActionForward personalTariffList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws BGException {
		String billingId = form.getParam("billingId");
		int contractId = form.getParamInt("contractId");

		ContractTariffDAO crmDAO = new ContractTariffDAO(form.getUser(), billingId);
		form.getResponse().setData("personalTariffList", crmDAO.contractPersonalTaraffList(contractId));

		return html(conSet, mapping, form, "personalTariffList");
	}

	public ActionForward getPersonalTariff(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws BGException {
		String billingId = form.getParam("billingId");

		if (form.getId() > 0) {
			form.getResponse().setData("personalTariff",
					new ContractTariffDAO(form.getUser(), billingId).getPersonalTaraff(form.getId()));
		}

		return html(conSet, mapping, form, "personalTariffEditor");
	}

	public ActionForward updatePersonalTariff(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");
		Integer position = form.getParamInt("position");
		String title = form.getParam("title");
		String dateFrom = form.getParam("dateFrom");
		String dateTo = form.getParam("dateTo");

		ContractTariffDAO crmDAO = new ContractTariffDAO(form.getUser(), billingId);
		crmDAO.updateContractPersonalTariff(contractId, form.getId(), title, position, dateFrom, dateTo);

		return json(conSet, form);
	}

	public ActionForward deletePersonalTariff(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		ContractTariffDAO crmDAO = new ContractTariffDAO(form.getUser(), billingId);
		crmDAO.deleteContractPersonalTariff(contractId, form.getId());

		return json(conSet, form);
	}

	public ActionForward tariffOptionList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws BGException, ParseException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		ContractTariffDAO crmDAO = new ContractTariffDAO(form.getUser(), billingId);

		form.getResponse().setData("list", crmDAO.contractTariffOptionList(contractId));
		form.getResponse().setData("history", crmDAO.contractTariffOptionHistory(contractId));

		return html(conSet, mapping, form, "tariffOptionList");
	}

	public ActionForward tariffOptionEditor(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws BGException {
		String billingId = form.getParam("billingId");
		int contractId = form.getParamInt("contractId");
		int optionId = form.getParamInt("optionId");

		ContractTariffDAO crmDAO = new ContractTariffDAO(form.getUser(), billingId);

		List<IdTitle> optionList = crmDAO.contractAvailableOptionList(contractId);
		if (optionId <= 0 && optionList.size() > 0) {
			optionId = Utils.getFirst(optionList).getId();
			form.setParam("optionId", String.valueOf(optionId));
		}

		form.getResponse().setData("availableOptionList", optionList);
		if (optionId > 0) {
			form.getResponse().setData("activateModeList", crmDAO.activateModeList(contractId, optionId));
		}

		return html(conSet, mapping, form, "tariffOptionEditor");
	}

	public ActionForward activateTariffOption(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		Integer optionId = form.getParamInt("optionId");
		Integer modeId = form.getParamInt("modeId");

		ContractTariffDAO crmDAO = new ContractTariffDAO(form.getUser(), billingId);
		crmDAO.activateContractOption(contractId, optionId, modeId, false);

		return json(conSet, form);
	}

	public ActionForward deleteTariffOption(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws BGException, ParseException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");
		Integer optionId = form.getParamInt("optionId");

		ContractTariffDAO crmDAO = new ContractTariffDAO(form.getUser(), billingId);
		crmDAO.deactivateContractOption(contractId, optionId);

		return json(conSet, form);
	}

	public ActionForward groupTariffList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws BGException {
		String billingId = form.getParam("billingId");
		Integer contractId = form.getParamInt("contractId");

		ContractTariffDAO crmDAO = new ContractTariffDAO(form.getUser(), billingId);
		form.getResponse().setData("tariffGroupList", crmDAO.contractTariffGroupList(contractId));

		return html(conSet, mapping, form, "groupTariffList");
	}

	public ActionForward getContractTariffGroup(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws BGException {
		String billingId = form.getParam("billingId");

		if (form.getId() > 0) {
			ContractTariffGroup groupTariff = new ContractTariffDAO(form.getUser(), billingId)
					.getContractTariffGroup(form.getId());
			form.getResponse().setData("tariffGroup", groupTariff);
		}
		form.getResponse().setData("registredTariffGroupList",
				new DirectoryDAO(form.getUser(), billingId).getRegistredTariffGroupList(0));

		return html(conSet, mapping, form, "groupTariffEditor");
	}

	public ActionForward updateContractTariffGroup(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");
		String dateFrom = form.getParam("dateFrom");
		String dateTo = form.getParam("dateTo");
		String comment = form.getParam("comment");

		Integer contractId = form.getParamInt("contractId");
		Integer tariffGroupId = form.getParamInt("tariffGroupId");

		ContractTariffDAO crmDAO = new ContractTariffDAO(form.getUser(), billingId);
		crmDAO.updateContractTariffGroup(contractId, form.getId(), tariffGroupId, dateFrom, dateTo, comment);

		return json(conSet, form);
	}

	public ActionForward deleteContractTariffGroup(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
		String billingId = form.getParam("billingId");

		ContractTariffDAO crmDAO = new ContractTariffDAO(form.getUser(), billingId);
		crmDAO.deleteContractTariffGroup(form.getId());

		return json(conSet, form);
	}
}
