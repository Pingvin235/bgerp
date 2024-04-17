package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.text.SimpleDateFormat;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;

import org.bgerp.app.exception.BGException;
import org.bgerp.model.base.IdTitle;
import org.bgerp.util.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.proto.model.tariff.ContractPersonalTariff;
import ru.bgcrm.plugin.bgbilling.proto.model.tariff.ContractTariff;
import ru.bgcrm.plugin.bgbilling.proto.model.tariff.ContractTariffGroup;
import ru.bgcrm.plugin.bgbilling.proto.model.tariff.ContractTariffOption;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

public class ContractTariffDAO extends ru.bgcrm.plugin.bgbilling.dao.BillingDAO {
    private static final Log log = Log.getLog();

    private static final String TARIFF_SERVICE_MODULE_ID = "ru.bitel.bgbilling.kernel.tariff";
    private static final String TARIFF_OPTION_SERVICE_MODULE_ID = "ru.bitel.bgbilling.kernel.tariff.option";

    private static final String CONTRACT_MODULE_ID = "contract";
    private static final String TARIFF_OPTION_MODULE_ID = "tariff.option";
    private static final String CONTRACT_TARIFF_MODULE_ID = "contract.tariff";

    public ContractTariffDAO(User user, String billingId) throws BGException {
        super(user, billingId);
    }

    public ContractTariffDAO(User user, DBInfo dbInfo) throws BGException {
        super(user, dbInfo);
    }

    /* private static ArrayList<Element> getSortedTariffList(Iterator<Element> iterator) {
        ArrayList<Element> list = new ArrayList<Element>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }

        boolean swapped = true;
        int j = 0;
        while (swapped) {
            swapped = false;
            j++;
            for (int i = 0; i < list.size() - j; i++) {
                if (Utils.parseInt(list.get(i).getAttribute("id"), -1) > Utils
                        .parseInt(list.get(i + 1).getAttribute("id"), -1)) {
                    Element tmp = list.get(i);
                    list.set(i, list.get(i + 1));
                    list.set(i + 1, tmp);
                    swapped = true;
                }
            }
        }

        return list;
    } */

    public void addTariffPlan(int contractId, int tariffId, int position) throws BGException {
        if (dbInfo.versionCompare("9.2") >= 0) {
            ContractTariff contractTariff = new ContractTariff();
            contractTariff.setContractId(contractId);
            contractTariff.setTariffPlanId(tariffId);
            contractTariff.setDateFrom(new Date());
            contractTariff.setPosition(position);
            contractTariffUpdate(contractTariff);
        } else {
            Request billingRequest = new Request();

            billingRequest.setModule("contract");
            billingRequest.setAction("UpdateContractTariffPlan");
            billingRequest.setAttribute("id", 0);
            billingRequest.setAttribute("pos", position);
            billingRequest.setAttribute("cid", contractId);
            billingRequest.setAttribute("tpid", tariffId);
            billingRequest.setAttribute("date1", new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
            billingRequest.setAttribute("comment", "");

            transferData.postData(billingRequest, user);
        }
    }

    /*public void setTariffPlan(int contractId, int tariffId, int position) throws BGException {
        Request request = new Request();
        request.setModule("contract");
        request.setAction("ContractTariffPlans");
        request.setAttribute("cid", contractId);

        Document doc = transferData.postData(request, user);

        Iterator<Element> it = XMLUtils.selectElements(doc, "/data/table/data/row").iterator();
        ArrayList<Element> tariffPlans = getSortedTariffList(it);

        for (Element e : tariffPlans) {
            if (Utils.parseInt(e.getAttribute("pos")) == position) {
                request = new Request();
                request.setModule(CONTRACT_MODULE_ID);
                request.setAction("UpdateContractTariffPlan");
                request.setAttribute("id", Utils.parseInt(e.getAttribute("id"), -1));
                request.setAttribute("cid", contractId);
                request.setAttribute("tpid", tariffId);
                request.setAttribute("pos", e.getAttribute("pos"));
                request.setAttribute("date1", e.getAttribute("date1"));
                request.setAttribute("date2", e.getAttribute("date2"));
                request.setAttribute("comment", e.getAttribute("comment"));
                transferData.postData(request, user);

                break;
            }
        }
    }*/

    public void setTariffPlan(int contractId, int tariffId) throws BGException {
        List<ContractTariff> list = contractTariffList(contractId);

        ContractTariff contractTariff = null;
        if (list.isEmpty()) {
            contractTariff = new ContractTariff();
            contractTariff.setContractId(contractId);
            contractTariff.setTariffPlanId(tariffId);
            contractTariff.setDateFrom(new Date());
        } else {
            contractTariff = list.get(0);
            contractTariff.setTariffPlanId(tariffId);
        }

        if (dbInfo.versionCompare("9.2") >= 0) {
            contractTariffUpdate(contractTariff);
        } else {
            Request billingRequest = new Request();
            billingRequest.setModule("contract");
            billingRequest.setAction("UpdateContractTariffPlan");
            billingRequest.setAttribute("id", String.valueOf(contractTariff.getId()));
            billingRequest.setAttribute("cid", contractId);
            billingRequest.setAttribute("tpid", tariffId);
            billingRequest.setAttribute("date1", TimeUtils.format(contractTariff.getDateFrom(), TimeUtils.PATTERN_DDMMYYYY));
            billingRequest.setAttribute("comment", "");

            transferData.postData(billingRequest, user);
        }
    }

    /**
     * Возвращает список тарифов договора.
     * @param contractId
     * @return
     */
    public List<ContractTariff> contractTariffList(int contractId) throws BGException {
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(ContractDAO.KERNEL_CONTRACT_API, "ContractTariffService", "contractTariffList");
            req.setParamContractId(contractId);
            req.setParam("entityMid", 0);
            req.setParam("entityId", 0);

            List<ContractTariff> result = readJsonValue(transferData.postDataReturn(req, user).traverse(),
                    jsonTypeFactory.constructCollectionType(List.class, ContractTariff.class));
            setTariffPlanTitles(result);
            return result;
        } else {
            Request request = new Request();
            request.setModule(CONTRACT_MODULE_ID);
            request.setAction("ContractTariffPlans");
            request.setContractId(contractId);

            Document document = transferData.postData(request, user);

            List<ContractTariff> contractTariffList = new ArrayList<ContractTariff>();
            Element dataElement = document.getDocumentElement();
            NodeList nodeList = dataElement.getElementsByTagName("row");

            for (int index = 0; index < nodeList.getLength(); index++) {
                Element rowElement = (Element) nodeList.item(index);
                ContractTariff tariff = new ContractTariff();

                tariff.setId(Utils.parseInt(rowElement.getAttribute("id")));
                tariff.setTariffPlanId(Utils.parseInt(rowElement.getAttribute("f1")));
                tariff.setTitle(rowElement.getAttribute("f2"));
                tariff.setDateFrom(TimeUtils.parse(rowElement.getAttribute("date1"), TimeUtils.FORMAT_TYPE_YMD));
                tariff.setDateTo(TimeUtils.parse(rowElement.getAttribute("date2"), TimeUtils.FORMAT_TYPE_YMD));
                tariff.setPosition(Utils.parseInt(rowElement.getAttribute("pos")));
                tariff.setComment(rowElement.getAttribute("comment"));

                contractTariffList.add(tariff);
            }

            return contractTariffList;
        }
    }

    private void setTariffPlanTitles(Iterable<ContractTariff> list) throws BGException {
        Map<Integer, String> titles = new TreeMap<>();
        for (ContractTariff contractTariff : list) {
            contractTariff.setTitle(titles.computeIfAbsent(contractTariff.getTariffPlanId(), tariffPlanId -> {
                try {
                    RequestJsonRpc req = new RequestJsonRpc(TARIFF_SERVICE_MODULE_ID, "TariffService", "tariffPlanGet");
                    req.setParam("id", tariffPlanId);
                    IdTitle plan = jsonMapper.convertValue(transferData.postDataReturn(req, user), IdTitle.class);
                    return plan.getTitle() != null ? plan.getTitle() : "??? [" + tariffPlanId + "]";
                } catch (BGException e) {
                    log.error(e);
                    return e.getMessage() + ":" + tariffPlanId;
                }
            }));
        }
    }

    private void contractTariffUpdate(ContractTariff contractTariff) throws BGException {
        RequestJsonRpc req = new RequestJsonRpc(ContractDAO.KERNEL_CONTRACT_API, "ContractTariffService", "contractTariffUpdate");
        req.setParam("contractTariff", contractTariff);
        transferData.postData(req, user);
    }

    /**
     * Возвращает тарифный план договора и заполняет справочник доступных тарифов.
     * @param id код записи, -1 если добавляется новый тариф и необходимы только справочники
     * @param moduleId код экземпляра модуля
     * @param contractId код договора
     * @param useFilter фильтр по договорам для предлагаемых тарифов
     * @param showUsed только используемые тарифы
     * @param tariffList список для загрузки списка тарифов, либо null, если не нужно
     * @return
     */
    public ContractTariff getContractTariffPlan(int id, int moduleId, int contractId, boolean useFilter,
            boolean showUsed, boolean contractGroupFilter, List<IdTitle> tariffList) throws BGException {
        ContractTariff result = null;

        Request request = new Request();
        request.setModule(CONTRACT_MODULE_ID);
        request.setAction("ContractTariffPlan");
        request.setContractId(contractId);
        if (id <= 0) {
            request.setAttribute("id", "new");
        } else {
            request.setAttribute("id", id);
        }
        if (moduleId > 0) {
            request.setAttribute("mid", moduleId);
        }
        request.setAttribute("useFilter", Utils.booleanToStringInt(useFilter));
        request.setAttribute("showUsed", Utils.booleanToStringInt(showUsed));
        request.setAttribute("tariffGroupFilter", Utils.booleanToStringInt(contractGroupFilter));

        Document doc = transferData.postData(request, user);

        Element tariffPlanEl = XMLUtils.selectElement(doc, "/data/tariffPlan");
        if (tariffPlanEl != null) {
            result = new ContractTariff();
            result.setId(id);
            result.setTariffPlanId(Utils.parseInt(tariffPlanEl.getAttribute("tpid")));
            result.setDateFrom(TimeUtils.parse(tariffPlanEl.getAttribute("date1"), TimeUtils.FORMAT_TYPE_YMD));
            result.setDateTo(TimeUtils.parse(tariffPlanEl.getAttribute("date2"), TimeUtils.FORMAT_TYPE_YMD));
            result.setComment(tariffPlanEl.getAttribute("comment"));
            result.setPosition(Utils.parseInt(tariffPlanEl.getAttribute("pos")));
        }

        if (tariffList != null) {
            for (Element rowElement : XMLUtils.selectElements(doc, "/data/tariffPlans/item")) {
                IdTitle tariffPlan = new IdTitle();
                tariffPlan.setId(Utils.parseInt(rowElement.getAttribute("id")));
                tariffPlan.setTitle(rowElement.getAttribute("title"));

                tariffList.add(tariffPlan);
            }
        }

        return result;
    }

    @Deprecated
    public ContractTariff getContractTariffPlan(int id, int moduleId, int contractId, boolean useFilter,
            boolean showUsed, List<IdTitle> tariffList) throws BGException {
        return getContractTariffPlan(id, moduleId, contractId, useFilter, showUsed, false, tariffList);
    }

    /**
     * Изменяет либо добавляет тариф договора.
     * @param contractId код договора
     * @param id код записи с тарифом, -1 для добавления
     * @param tpid код тарифного плана
     * @param position позиция
     * @param dateFrom с даты
     * @param dateTo по дату
     * @param comment комментарий
     */
    public void updateContractTariffPlan(int contractId, int id, int tpid, int position, String dateFrom, String dateTo, String comment)
            throws BGException {
        if (dbInfo.versionCompare("9.2") >= 0) {
            ContractTariff contractTariff = new ContractTariff();
            contractTariff.setContractId(contractId);
            contractTariff.setId(id);
            contractTariff.setTariffPlanId(tpid);
            contractTariff.setPosition(position);
            contractTariff.setDateFrom(TimeUtils.parse(dateFrom, TimeUtils.PATTERN_DDMMYYYY));
            contractTariff.setDateTo(TimeUtils.parse(dateTo, TimeUtils.PATTERN_DDMMYYYY));
            contractTariff.setComment(comment);
            contractTariffUpdate(contractTariff);
        } else {
            Request request = new Request();
            request.setModule(CONTRACT_MODULE_ID);
            request.setAction("UpdateContractTariffPlan");
            if (id > 0) {
                request.setAttribute("id", id);
            } else {
                request.setAttribute("id", "new");
            }
            request.setContractId(contractId);
            request.setAttribute("tpid", tpid);
            request.setAttribute("pos", position);
            request.setAttribute("date1", dateFrom);
            request.setAttribute("date2", dateTo);
            request.setAttribute("comment", comment);

            transferData.postData(request, user);
        }
    }

    /**
     * Удаляет тариф договора.
     * @param contractId код договора
     * @param id код записи с тарифным планом
     */
    public void deleteContractTariffPlan(int contractId, int id) throws BGException {
        Request request = new Request();
        request.setModule(CONTRACT_MODULE_ID);
        request.setAction("DeleteContractTariffPlan");
        request.setContractId(contractId);
        request.setAttribute("id", id);

        transferData.postData(request, user);
    }

    @Deprecated
    public List<ContractTariffGroup> contractActiveTariffGroup(int contractId) throws BGException {
        return contractTariffGroupList(contractId, true);
    }


    /**
     * Возвращает список всех групп тарифов на договоре.
     * @param contractId
     * @return
     */
    public List<ContractTariffGroup> contractTariffGroupList(int contractId) throws BGException {
        return contractTariffGroupList(contractId, false);
    }

    /**
     * Возвращает список групп тарифов договора.
     * @param contractId код договора.
     * @param active выводить только активные в данный момент группы, с открытой второй датой.
     * @return
     */
    private List<ContractTariffGroup> contractTariffGroupList(int contractId, boolean active) throws BGException {
        if (dbInfo.versionCompare("9.2") >= 0) {
            return Collections.emptyList();
        } else {
            Request request = new Request();
            request.setModule(CONTRACT_MODULE_ID);
            request.setAction("ContractTariffGroupTable");
            request.setContractId(contractId);

            Document document = transferData.postData(request, user);

            List<ContractTariffGroup> contractTariffGroupList = new ArrayList<ContractTariffGroup>();
            Element dataElement = document.getDocumentElement();
            NodeList nodeList = dataElement.getElementsByTagName("row");

            for (int index = 0; index < nodeList.getLength(); index++) {
                Element rowElement = (Element) nodeList.item(index);
                ContractTariffGroup tariffGroup = new ContractTariffGroup();

                tariffGroup.setId(Utils.parseInt(rowElement.getAttribute("id")));
                tariffGroup.setTitle(rowElement.getAttribute("tariff_group"));
                TimeUtils.parsePeriod(rowElement.getAttribute("period"), tariffGroup);
                tariffGroup.setComment(rowElement.getAttribute("comment"));

                if (active) {
                    if (tariffGroup.getDateTo() != null) {
                        contractTariffGroupList.add(tariffGroup);
                    }
                } else {
                    contractTariffGroupList.add(tariffGroup);
                }
            }

            return contractTariffGroupList;
        }
    }

    /**
     * Возвращает группу тарифов договора.
     * @param id код записи.
     * @return
     */
    public ContractTariffGroup getContractTariffGroup(int id) throws BGException {
        Request request = new Request();
        request.setModule(CONTRACT_MODULE_ID);
        request.setAction("GetContractTariffGroup");
        request.setAttribute("id", id);

        Document document = transferData.postData(request, user);

        Element dataElement = document.getDocumentElement();
        NodeList nodeList = dataElement.getElementsByTagName("contract_tariff_group");

        ContractTariffGroup tariffGroup = new ContractTariffGroup();
        if (nodeList.getLength() > 0) {
            Element rowElement = (Element) nodeList.item(0);

            tariffGroup.setId(id);
            tariffGroup.setDateFrom(TimeUtils.parse(rowElement.getAttribute("date1"), TimeUtils.FORMAT_TYPE_YMD));
            tariffGroup.setDateTo(TimeUtils.parse(rowElement.getAttribute("date2"), TimeUtils.FORMAT_TYPE_YMD));
            tariffGroup.setComment(rowElement.getAttribute("comment"));
            tariffGroup.setGroupId(Utils.parseInt(rowElement.getAttribute("tariff_group")));
        }

        return tariffGroup;
    }

    /**
     * Изменяет либо добавляет группу тарифов договора.
     * @param id <= 0 - добавление группы тарифов, иначе - код изменяемой записи.
     * @param contractId код договора.
     * @param tariffGroupId год группы тарифов.
     * @param dateFrom с даты.
     * @param dateTo по дату.
     * @param comment примечение.
     */
    public void updateContractTariffGroup(int id, int contractId, int tariffGroupId, Date dateFrom, Date dateTo,
            String comment) throws BGException {
        Request request = new Request();
        request.setModule(CONTRACT_MODULE_ID);
        request.setAction("UpdateContractTariffGroup");
        request.setContractId(contractId);
        request.setAttribute("tariff_group", tariffGroupId);
        request.setAttribute("date1", TimeUtils.format(dateFrom, TimeUtils.PATTERN_DDMMYYYY));
        request.setAttribute("date2", TimeUtils.format(dateTo, TimeUtils.PATTERN_DDMMYYYY));
        request.setAttribute("comment", comment);

        if (id <= 0) {
            request.setAttribute("id", "new");
        } else {
            request.setAttribute("id", id);
        }
        transferData.postData(request, user);
    }

    @Deprecated
    public void updateContractTariffGroup(int contractId, int id, int tariffGroupId, String dateFrom, String dateTo,
            String comment) throws BGException {
        updateContractTariffGroup(id, contractId, tariffGroupId, TimeUtils.parse(dateFrom, TimeUtils.PATTERN_YYYYMMDD),
                TimeUtils.parse(dateTo, TimeUtils.PATTERN_YYYYMMDD), comment);
    }

    public void deleteContractTariffGroup(int tariffId) throws BGException {
        Request request = new Request();
        request.setModule(CONTRACT_MODULE_ID);
        request.setAction("DeleteContractTariffGroup");
        request.setAttribute("id", tariffId);

        transferData.postData(request, user);
    }

    /**
     * Возвращает список тарифных опций договора.
     * @param contractId
     * @return
     */
    public List<ContractTariffOption> contractTariffOptionList(int contractId) throws BGException {
        List<ContractTariffOption> list = new ArrayList<>();

        if (dbInfo.versionCompare("6.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(TARIFF_OPTION_SERVICE_MODULE_ID, "TariffOptionService", "contractTariffOptionList");
            req.setParamContractId(contractId);
            req.setParam("date", new Date());

            list = readJsonValue(transferData.postDataReturn(req, user).traverse(),
                    jsonTypeFactory.constructCollectionType(List.class, ContractTariffOption.class));
        } else {
            Request request = new Request();
            request.setModule(TARIFF_OPTION_MODULE_ID);
            request.setAction("ContractTariffOption");
            request.setAttribute("operation", "list");
            request.setContractId(contractId);

            Document document = transferData.postData(request, user);

            Element dataElement = document.getDocumentElement();
            NodeList nodeList = dataElement.getElementsByTagName("item");

            for (int index = 0; index < nodeList.getLength(); index++) {
                Element rowElement = (Element) nodeList.item(index);
                ContractTariffOption tariffOption = convertFromElement(rowElement);

                list.add(tariffOption);
            }
        }

        return list;
    }

    /**
     * Возвращает историю тарифных опций на договоре.
     * @param contractId
     * @return
     */
    public List<ContractTariffOption> contractTariffOptionHistory(int contractId) throws BGException {
        List<ContractTariffOption> list = new ArrayList<>();

        if (dbInfo.versionCompare("6.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(TARIFF_OPTION_SERVICE_MODULE_ID, "TariffOptionService", "contractTariffOptionHistory");
            req.setParamContractId(contractId);
            req.setParam("web", false);

            list = readJsonValue(transferData.postDataReturn(req, user).traverse(),
                    jsonTypeFactory.constructCollectionType(List.class, ContractTariffOption.class));
        }else {
            Request request = new Request();
            request.setModule(TARIFF_OPTION_MODULE_ID);
            request.setAction("ContractTariffOption");
            request.setAttribute("operation", "history");
            request.setContractId(contractId);

            Document document = transferData.postData(request, user);

            Element dataElement = document.getDocumentElement();
            NodeList nodeList = dataElement.getElementsByTagName("item");

            for (int index = 0; index < nodeList.getLength(); index++) {
                Element rowElement = (Element) nodeList.item(index);
                ContractTariffOption tariffOption = convertFromElement(rowElement);

                list.add(tariffOption);
            }
        }

        return list;
    }

    private ContractTariffOption convertFromElement(Element rowElement) {
        ContractTariffOption tariffOption = new ContractTariffOption();

        tariffOption.setId(Utils.parseInt(rowElement.getAttribute("id")));
        tariffOption
                .setActivatedTime(TimeUtils.parse(rowElement.getAttribute("activatedTime"), "yyyy-MM-dd'T'HH:mm:ss"));
        tariffOption.setActivateMode(Utils.parseInt(rowElement.getAttribute("activatedMode")));
        tariffOption.setChargeId(Utils.parseInt(rowElement.getAttribute("chargeId")));
        tariffOption.setContractId(Utils.parseInt(rowElement.getAttribute("contractId")));
        tariffOption.setOptionId(Utils.parseInt(rowElement.getAttribute("optionId")));
        tariffOption.setOptionTitle(rowElement.getAttribute("optionTitle"));
        tariffOption.setSumma(Utils.parseBigDecimal(rowElement.getAttribute("summa")));
        tariffOption.setTimeFrom(TimeUtils.parse(rowElement.getAttribute("timeFrom"), "yyyy-MM-dd'T'HH:mm:ss"));
        tariffOption.setTimeTo(TimeUtils.parse(rowElement.getAttribute("timeTo"), "yyyy-MM-dd'T'HH:mm:ss"));
        tariffOption.setUserId(Utils.parseInt(rowElement.getAttribute("userId")));
        tariffOption.setUserTitle(rowElement.getAttribute("userTitle"));

        return tariffOption;
    }



    /**
     * Возвращает список доступных тарифных опций.
     * @param contractId
     * @return
     */
    public List<IdTitle> contractAvailableOptionList(int contractId) throws BGException {
        List<IdTitle> availableOptionList = new ArrayList<IdTitle>();

        if (dbInfo.versionCompare("6.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(TARIFF_OPTION_SERVICE_MODULE_ID, "TariffOptionService", "tariffOptionListAvailable");
            req.setParamContractId(contractId);
            req.setParam("onlyAvailable", false);
            req.setParam("web", false);

            availableOptionList = readJsonValue(transferData.postDataReturn(req, user).traverse(),
                    jsonTypeFactory.constructCollectionType(List.class, IdTitle.class));
        } else {
            Request request = new Request();
            request.setModule(TARIFF_OPTION_MODULE_ID);
            request.setAction("ContractTariffOption");
            request.setAttribute("operation", "availableOptionList");
            request.setContractId(contractId);

            Document document = transferData.postData(request, user);

            for (Element rowElement : XMLUtils.selectElements(document, "/data/data/item")) {
                IdTitle type = new IdTitle();
                type.setId(Utils.parseInt(rowElement.getAttribute("id")));
                type.setTitle(rowElement.getAttribute("title"));

                availableOptionList.add(type);
            }
        }

        return availableOptionList;
    }


    public List<IdTitle> activateModeList(int contractId, int optionId) throws BGException {
        List<IdTitle> activateModeList = new ArrayList<IdTitle>();

        if (dbInfo.versionCompare("6.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(TARIFF_OPTION_SERVICE_MODULE_ID, "TariffOptionService", "tariffOptionActivateModeList");
            req.setParamContractId(contractId);
            req.setParam("optionId", optionId);

            activateModeList = readJsonValue(transferData.postDataReturn(req, user).traverse(),
                    jsonTypeFactory.constructCollectionType(List.class, IdTitle.class));
        } else {
            Request request = new Request();
            request.setModule(TARIFF_OPTION_MODULE_ID);
            request.setAction("TariffOption");
            request.setAttribute("operation", "activateModeList");
            request.setAttribute("option_id", optionId);

            Document document = transferData.postData(request, user);

            for (Element rowElement : XMLUtils.selectElements(document, "/data/data/item")) {
                IdTitle type = new IdTitle();
                type.setId(Utils.parseInt(rowElement.getAttribute("id")));
                type.setTitle(rowElement.getAttribute("modeTitle"));

                activateModeList.add(type);
            }
        }

        return activateModeList;
    }

    public void activateContractOption(int contractId, int optionId, int modeId, boolean web) throws BGException {
        if (dbInfo.versionCompare("6.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(TARIFF_OPTION_SERVICE_MODULE_ID, "TariffOptionService", "contractTariffOptionActivate");
            req.setParamContractId(contractId);
            req.setParam("optionId", optionId);
            req.setParam("modeId", modeId);
            req.setParam("web", false);

            transferData.postData(req, user);
        }else {
            Request request = new Request();
            request.setModule(TARIFF_OPTION_MODULE_ID);
            request.setAction("ContractTariffOption");
            request.setAttribute("operation", "activate");
            request.setAttribute("mode_id", modeId);
            request.setAttribute("option_id", optionId);
            request.setContractId(contractId);

            transferData.postData(request, user);
        }
    }

    public void reactivateContractOption(int contractId, int id) throws BGException {
        // TODO: Для новых версий.
        Request request = new Request();
        request.setModule(TARIFF_OPTION_MODULE_ID);
        request.setAction("ContractTariffOption");
        request.setAttribute("operation", "reactivate");
        request.setAttribute("id", id);
        request.setContractId(contractId);

        transferData.postData(request, user);
    }

    /**
     * Деактивирует тарифную опцию на договоре.
     * @param contractId
     * @param id
     */
    public void deactivateContractOption(int contractId, int id) throws BGException {
        if (dbInfo.versionCompare("6.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(TARIFF_OPTION_SERVICE_MODULE_ID, "TariffOptionService", "contractTariffOptionDeactivate");
            req.setParamContractId(contractId);
            req.setParam("contractOptionId", id);

            transferData.postData(req, user);
        }else {
            Request request = new Request();
            request.setModule(TARIFF_OPTION_MODULE_ID);
            request.setAction("ContractTariffOption");
            request.setAttribute("operation", "deactivate");
            request.setAttribute("id", id);
            request.setContractId(contractId);

            transferData.postData(request, user);
        }
    }

    /**
     * Возвращает список персональных тарифов договора.
     * @param contractId
     * @return
     */
    public List<ContractPersonalTariff> contractPersonalTariffList(int contractId) throws BGException {
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.api",
                    "ContractTariffService",
                    "personalTariffList");
            req.setParam("contractId", contractId);
            JsonNode ret = transferData.postDataReturn(req, user);
            return readJsonValue(ret.traverse(), jsonTypeFactory.constructCollectionType(List.class, ContractPersonalTariff.class));
        } else {
            Request request = new Request();
            request.setModule(CONTRACT_TARIFF_MODULE_ID);
            request.setAction("PersonalTariffTable");
            request.setContractId(contractId);

            Document document = transferData.postData(request, user);

            List<ContractPersonalTariff> contractPersonalTariffList = new ArrayList<ContractPersonalTariff>();

            for (Element rowElement : XMLUtils.selectElements(document, "/data/table/data/row")) {
                ContractPersonalTariff personalTariff = new ContractPersonalTariff();

                personalTariff.setId(Utils.parseInt(rowElement.getAttribute("id")));
                TimeUtils.parsePeriod(rowElement.getAttribute("period"), personalTariff);
                personalTariff.setPos(Utils.parseInt(rowElement.getAttribute("pos")));
                personalTariff.setTitle(rowElement.getAttribute("title"));
                personalTariff.setTreeId(Utils.parseInt(rowElement.getAttribute("tree_id")));

                contractPersonalTariffList.add(personalTariff);
            }

            return contractPersonalTariffList;
        }
    }

    /**
     * Возвращает персональный тариф договора.
     * @param id
     * @return
     */
    public ContractPersonalTariff getPersonalTariff(int id) throws BGException {
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.api",
                    "ContractTariffService",
                    "personalTariffGet");
            req.setParam("id", id);
            JsonNode ret = transferData.postDataReturn(req, user);
            return jsonMapper.convertValue(ret, ContractPersonalTariff.class);
        } else {
            Request request = new Request();
            request.setModule(CONTRACT_TARIFF_MODULE_ID);
            request.setAction("GetPersonalTariff");
            request.setAttribute("id", id);

            Document document = transferData.postData(request, user);

            ContractPersonalTariff personalTariff = new ContractPersonalTariff();
            Element dataElement = document.getDocumentElement();
            NodeList nodeList = dataElement.getElementsByTagName("tariff");

            if (nodeList.getLength() > 0) {
                Element rowElement = (Element) nodeList.item(0);

                personalTariff.setId(id);
                personalTariff.setDate1(TimeUtils.parse(rowElement.getAttribute("date1"), TimeUtils.FORMAT_TYPE_YMD));
                personalTariff.setDate2(TimeUtils.parse(rowElement.getAttribute("date2"), TimeUtils.FORMAT_TYPE_YMD));
                personalTariff.setPos(Utils.parseInt(rowElement.getAttribute("pos")));
                personalTariff.setTitle(rowElement.getAttribute("title"));
                personalTariff.setTreeId(Utils.parseInt(rowElement.getAttribute("tree_id")));
            }

            return personalTariff;
        }
    }

    public void deleteContractPersonalTariff(int contractId, int id) throws BGException {
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.api",
                    "ContractTariffService",
                    "personalTariffDelete");
            req.setParam("contractId", contractId);
            req.setParam("personalTariffId", id);
            transferData.postDataReturn(req, user);

        } else {
            Request request = new Request();
            request.setModule(CONTRACT_TARIFF_MODULE_ID);
            request.setAction("DeletePersonalTariff");
            request.setAttribute("operation", "deactivate");
            request.setAttribute("id", id);
            request.setContractId(contractId);

            transferData.postData(request, user);
        }
    }

    public void updateContractPersonalTariff(int contractId, int tariffId, String title, int position, String dateFrom,
                                             String dateTo) throws BGException {
        if (dbInfo.versionCompare("9.2") >= 0) {
            ContractPersonalTariff personalTariff = getPersonalTariff(tariffId);
            personalTariff = personalTariff != null ? personalTariff : new ContractPersonalTariff();
            personalTariff.setContractId(contractId);
            personalTariff.setTitle(title);
            personalTariff.setPos(position);
            personalTariff.setDate1(TimeUtils.parse(dateFrom, TimeUtils.FORMAT_TYPE_YMD));
            personalTariff.setDate2(TimeUtils.parse(dateTo, TimeUtils.FORMAT_TYPE_YMD));
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.api",
                    "ContractTariffService",
                    "personalTariffUpdate");
            req.setParam("contractId", contractId);
            req.setParam("personalTariff", personalTariff);

            transferData.postDataReturn(req, user);

        } else {
            Request request = new Request();
            request.setModule(CONTRACT_TARIFF_MODULE_ID);
            request.setAction("UpdatePersonalTariff");
            request.setAttribute("id", tariffId);
            request.setContractId(contractId);
            request.setAttribute("title", title);
            request.setAttribute("pos", position);
            request.setAttribute("date1", dateFrom);
            request.setAttribute("date2", dateTo);

            transferData.postData(request, user);
        }
    }
}
