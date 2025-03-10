package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.bean.annotation.Bean;
import org.bgerp.app.exception.BGException;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.util.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.bgcrm.dao.AddressDAO;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.model.process.wizard.FillParamsStep;
import ru.bgcrm.model.process.wizard.base.StepData;
import ru.bgcrm.model.process.wizard.base.WizardData;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.dao.BGBillingDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

@Bean
public class ShowContractsByAddrParamStep extends BaseStep {
    private final int addressParamId;
    private final SortedMap<Integer, ConfigMap> billingConfig;

    public ShowContractsByAddrParamStep(ConfigMap config) {
        super(config);

        addressParamId = config.getInt("addressParamId", 0);
        billingConfig = config.subIndexed("billing.");
    }

    public int getAddressParamId() {
        return addressParamId;
    }

    public SortedMap<Integer, ConfigMap> getBillingConfig() {
        return billingConfig;
    }

    @Override
    public String getJsp() {
        return PATH_JSP + "/step_show_contract_by_address.jsp";
    }

    @Override
    public StepData<?> data(WizardData data) {
        return new Data(this, data);
    }

    public static class Data extends StepData<ShowContractsByAddrParamStep> {
        private int houseId;
        private Map<String, Document> contracts;

        private Data(ShowContractsByAddrParamStep step, WizardData data) {
            super(step, data);
        }

        public int getHouseId() {
            return houseId;
        }

        public Map<String, Document> getContracts() {
            return contracts;
        }

        @Override
        public boolean isFilled(DynActionForm form, Connection con) throws Exception {
            if (contracts == null) {
                List<StepData<?>> stepDataList = data.getStepDataList();

                for (int i = stepDataList.indexOf(this); i >= 0; i--) {
                    StepData<?> stepData = stepDataList.get(i);

                    if (stepData instanceof FillParamsStep.Data fpStepData) {
                        SortedMap<Integer, ParameterAddressValue> values = new ParamValueDAO(con).getParamAddress(fpStepData.getObjectId(),
                                step.getAddressParamId());
                        ParameterAddressValue val;
                        if (values.size() > 0) {
                            Map<String, Document> result = new HashMap<>();

                            for (Map.Entry<Integer, ConfigMap> entry : step.getBillingConfig().entrySet()) {
                                ConfigMap conf = entry.getValue();
                                String billingId = conf.get("id");

                                val = values.values().iterator().next();

                                AddressDAO addressDAO = new AddressDAO(con);
                                AddressHouse addressHouse = addressDAO.getAddressHouse(val.getHouseId(), true, false, true);

                                Request request = new Request();
                                request.setModule("contract");
                                request.setAction("FindContract");
                                request.setAttribute("filter", 0);
                                request.setAttribute("show_sub", 0);
                                request.setAttribute("show_closed", "1");
                                request.setAttribute("type", 2);
                                request.setAttribute("street", addressHouse.getStreetId());
                                request.setAttribute("house", addressHouse.getHouse());
                                request.setAttribute("frac", addressHouse.getFrac() == null ? "" : addressHouse.getFrac());
                                request.setAttribute("flat", val.getFlat());
                                request.setAttribute("room", val.getRoom());

                                BGBillingDAO billingDAO = new BGBillingDAO();
                                Document doc = billingDAO.doRequestToBilling(billingId, data.getUser(), request);

                                appendBalance(doc, conf);

                                result.put(billingId, doc);
                            }

                            contracts = result;
                        }
                        break;
                    }
                }
            }

            return true;
        }

        private void appendBalance(Document doc, ConfigMap conf) throws BGException {
            Element dataElement = (Element) doc.getElementsByTagName("data").item(0);

            if (dataElement.getAttribute("status").equals("ok")) {
                Element contractsElement = (Element) dataElement.getElementsByTagName("contracts").item(0);

                NodeList nodeList = contractsElement.getChildNodes();

                for (int i = 0; i < nodeList.getLength(); i++) {
                    Element itemElement = (Element) nodeList.item(i);

                    int contractId = Utils.parseInt(itemElement.getAttribute("id"), -1);

                    ContractDAO contractDAO = new ContractDAO(data.getUser(), conf.get("id"));

                    if (checkAndAppendAttrs(contractDAO.getContractCardDoc(contractId), itemElement, conf)) {
                        contractsElement.removeChild(nodeList.item(i));
                    }
                }
            }
        }

        private boolean checkAndAppendAttrs(Document doc, Element e, ConfigMap conf) {
            int dontDisplayStatementsCount = 0;
            Element contractElement = XMLUtils.selectElement(doc, "/data/contract");
            if (contractElement == null) {
                return true;
            }

            if (Utils.notEmptyString(contractElement.getAttribute("date2")))
                dontDisplayStatementsCount++;
            if ("1".equals(contractElement.getAttribute("del")))
                dontDisplayStatementsCount++;

            Element element;

            element = XMLUtils.selectElement(doc, "/data/contract");
            if (element != null) {
                e.setAttribute("contractTitle", element.getAttribute("title"));
                e.setAttribute("balance", element.getAttribute("balance_rest"));
                e.setAttribute("comment", element.getAttribute("comment"));
                int status = Utils.parseInt(element.getAttribute("status"));
                switch (status) {
                case 0:
                    e.setAttribute("status", "Активен");
                    break;
                case 3:
                    e.setAttribute("status", "Закрыт");
                    break;
                case 4:
                    e.setAttribute("status", "Приостановлен");
                    break;
                case 1:
                    e.setAttribute("status", "В отключении");
                    break;
                case 2:
                    e.setAttribute("status", "Отключен");
                    break;
                case 5:
                    e.setAttribute("status", "В подключении");
                    break;
                }
            }

            element = XMLUtils.selectElement(doc, "/data/contract/tariff");
            if (element != null)
                e.setAttribute("tariff", element.getAttribute("tariff_plan"));

            element = XMLUtils.selectElement(doc, "/data/parameters/parameter[@pid=" + conf.getInt("conlusionDateParamId", -1) + "]");
            if (element != null)
                e.setAttribute("conclusionDate", element.getAttribute("value"));

            String[] allowedGroupShowIds = conf.get("allowedGroupShowIds").split(",");
            Iterable<Element> elements = XMLUtils.selectElements(doc, "/data/info/groups/item");
            String groupTitles = "";
            for (Element group : elements) {
                if (conf.get("dissolvedGroupId").equals(group.getAttribute("id"))) {
                    dontDisplayStatementsCount++;
                }

                for (String groupId : allowedGroupShowIds) {
                    if (groupId.equals(group.getAttribute("id"))) {
                        groupTitles += "[ " + group.getAttribute("title") + " ] ";
                    }
                }
            }
            e.setAttribute("groups", groupTitles);

            return dontDisplayStatementsCount > 1;
        }
    }
}
