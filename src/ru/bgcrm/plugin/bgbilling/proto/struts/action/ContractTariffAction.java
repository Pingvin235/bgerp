package ru.bgcrm.plugin.bgbilling.proto.struts.action;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.struts.action.ActionForward;
import org.bgerp.model.base.IdTitle;

import ru.bgcrm.plugin.bgbilling.Plugin;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractTariffDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.DirectoryDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.tariff.ContractPersonalTariff;
import ru.bgcrm.plugin.bgbilling.proto.model.tariff.ContractTariff;
import ru.bgcrm.plugin.bgbilling.proto.model.tariff.ContractTariffGroup;
import ru.bgcrm.plugin.bgbilling.proto.model.tariff.ContractTariffOption;
import ru.bgcrm.plugin.bgbilling.struts.action.BaseAction;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/bgbilling/proto/contractTariff")
public class ContractTariffAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER + "/contract/tariff";

    public ActionForward tariff(DynActionForm form, ConnectionSet conSet) throws ParseException {
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
            List<ContractPersonalTariff> personalTariffList = crmDAO.contractPersonalTariffList(contractId);
            for (ContractPersonalTariff tariff : personalTariffList) {
                if (tariff.getDate2() == null) {
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

        return html(conSet, form, PATH_JSP + "/tariff.jsp");
    }

    public ActionForward contractTariffList(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        ContractTariffDAO crmDAO = new ContractTariffDAO(form.getUser(), billingId);
        form.getResponse().setData("tariffList", crmDAO.contractTariffList(contractId));

        return html(conSet, form, PATH_JSP + "/contract_tariff_list.jsp");
    }

    public ActionForward getContractTariff(DynActionForm form, ConnectionSet conSet) {
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

        return html(conSet, form, PATH_JSP + "/contract_tariff_editor.jsp");
    }

    public ActionForward addContractTariff(DynActionForm form, ConnectionSet conSet) throws Exception {
        int tariffId = form.getParamInt("tariffId", -1);

        ContractTariffDAO contractDAO = new ContractTariffDAO(form.getUser(), form.getParam("billingId"));

        contractDAO.addTariffPlan(form.getParamInt("contractId", -1), tariffId, form.getParamInt("position", 0));

        return json(conSet, form);
    }

    public ActionForward setContractTariff(DynActionForm form, ConnectionSet conSet) throws Exception {
        int tariffId = form.getParamInt("tariffId", -1);

        ContractTariffDAO contractDAO = new ContractTariffDAO(form.getUser(), form.getParam("billingId"));

        contractDAO.setTariffPlan(form.getParamInt("contractId", -1), tariffId);

        return json(conSet, form);
    }

    public ActionForward updateContractTariff(DynActionForm form, ConnectionSet conSet) {
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

    public ActionForward deleteСontractTariff(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        ContractTariffDAO crmDAO = new ContractTariffDAO(form.getUser(), billingId);
        crmDAO.deleteContractTariffPlan(contractId, form.getId());

        return json(conSet, form);
    }

    public ActionForward personalTariffList(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");

        ContractTariffDAO crmDAO = new ContractTariffDAO(form.getUser(), billingId);
        form.getResponse().setData("personalTariffList", crmDAO.contractPersonalTariffList(contractId));

        return html(conSet, form, PATH_JSP + "/personal_tariff_list.jsp");
    }

    public ActionForward getPersonalTariff(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");

        if (form.getId() > 0) {
            form.getResponse().setData("personalTariff",
                    new ContractTariffDAO(form.getUser(), billingId).getPersonalTariff(form.getId()));
        }

        return html(conSet, form, PATH_JSP + "/personal_tariff_editor.jsp");
    }

    public ActionForward updatePersonalTariff(DynActionForm form, ConnectionSet conSet) {
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

    public ActionForward deletePersonalTariff(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        ContractTariffDAO crmDAO = new ContractTariffDAO(form.getUser(), billingId);
        crmDAO.deleteContractPersonalTariff(contractId, form.getId());

        return json(conSet, form);
    }

    public ActionForward tariffOptionList(DynActionForm form, ConnectionSet conSet) throws ParseException {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        ContractTariffDAO crmDAO = new ContractTariffDAO(form.getUser(), billingId);

        form.getResponse().setData("list", crmDAO.contractTariffOptionList(contractId));
        form.getResponse().setData("history", crmDAO.contractTariffOptionHistory(contractId));

        return html(conSet, form, PATH_JSP + "/tariff_option_list.jsp");
    }

    public ActionForward tariffOptionEditor(DynActionForm form, ConnectionSet conSet) {
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

        return html(conSet, form, PATH_JSP + "/tariff_option_editor.jsp");
    }

    public ActionForward activateTariffOption(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        Integer optionId = form.getParamInt("optionId");
        Integer modeId = form.getParamInt("modeId");

        ContractTariffDAO crmDAO = new ContractTariffDAO(form.getUser(), billingId);
        crmDAO.activateContractOption(contractId, optionId, modeId, false);

        return json(conSet, form);
    }

    public ActionForward deleteTariffOption(DynActionForm form, ConnectionSet conSet)
            throws ParseException {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");
        Integer optionId = form.getParamInt("optionId");

        ContractTariffDAO crmDAO = new ContractTariffDAO(form.getUser(), billingId);
        crmDAO.deactivateContractOption(contractId, optionId);

        return json(conSet, form);
    }

    public ActionForward groupTariffList(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        ContractTariffDAO crmDAO = new ContractTariffDAO(form.getUser(), billingId);
        form.getResponse().setData("tariffGroupList", crmDAO.contractTariffGroupList(contractId));

        return html(conSet, form, PATH_JSP + "/group_tariff_list.jsp");
    }

    public ActionForward getContractTariffGroup(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");

        if (form.getId() > 0) {
            ContractTariffGroup groupTariff = new ContractTariffDAO(form.getUser(), billingId)
                    .getContractTariffGroup(form.getId());
            form.getResponse().setData("tariffGroup", groupTariff);
        }
        form.getResponse().setData("registredTariffGroupList",
                new DirectoryDAO(form.getUser(), billingId).getRegistredTariffGroupList(0));

        return html(conSet, form, PATH_JSP + "/group_tariff_editor.jsp");
    }

    public ActionForward updateContractTariffGroup(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        String dateFrom = form.getParam("dateFrom");
        String dateTo = form.getParam("dateTo");
        String comment = form.getParam("comment");

        Integer contractId = form.getParamInt("contractId");
        Integer tariffGroupId = form.getParamInt("tariffGroupId");

        ContractTariffDAO dao = new ContractTariffDAO(form.getUser(), billingId);
        dao.updateContractTariffGroup(contractId, form.getId(), tariffGroupId, dateFrom, dateTo, comment);

        return json(conSet, form);
    }

    public ActionForward deleteContractTariffGroup(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");

        ContractTariffDAO crmDAO = new ContractTariffDAO(form.getUser(), billingId);
        crmDAO.deleteContractTariffGroup(form.getId());

        return json(conSet, form);
    }
}
