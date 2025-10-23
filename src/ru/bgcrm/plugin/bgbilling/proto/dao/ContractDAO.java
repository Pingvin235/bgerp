package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.app.exception.BGMessageExceptionWithoutL10n;
import org.bgerp.cache.ParameterCache;
import org.bgerp.dao.customer.CustomerDAO;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.Pageable;
import org.bgerp.model.base.IdStringTitle;
import org.bgerp.model.base.IdTitle;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.Log;
import org.bgerp.util.TimeConvert;
import org.bgerp.util.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.JsonNode;

import ru.bgcrm.dao.CustomerLinkDAO;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterSearchedObject;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractFace;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractGroup;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractInfo;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractMode;
import ru.bgcrm.plugin.bgbilling.proto.model.OpenContract;
import ru.bgcrm.plugin.bgbilling.proto.model.contract.ContractCreateData;
import ru.bgcrm.plugin.bgbilling.proto.model.contract.ContractSearchDto;
import ru.bgcrm.plugin.bgbilling.proto.model.limit.LimitChangeTask;
import ru.bgcrm.plugin.bgbilling.proto.model.limit.LimitLogItem;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class ContractDAO extends BillingDAO {
    private static final Log log = Log.getLog();

    public static final String KERNEL_CONTRACT_API = "ru.bitel.bgbilling.kernel.contract.api";
    public static final String KERNEL_CONTRACT_LIMIT = "ru.bitel.bgbilling.kernel.contract.limit";
    private static final String KERNEL_CONTRACT_LABEL = "ru.bitel.bgbilling.kernel.contract.label";

    public static class SearchOptions {
        public boolean showHidden;
        public boolean showClosed;
        public boolean showSub;

        public SearchOptions(boolean showHidden, boolean showClosed, boolean showSub) {
            this.showHidden = showHidden;
            this.showClosed = showClosed;
            this.showSub = showSub;
        }
    }

    public ContractDAO(User user, String billingId) {
        super(user, billingId);
    }

    private ContractDAO(User user, DBInfo dbInfo) {
        super(user, dbInfo);
    }

    @Deprecated
    public static ContractDAO getInstance(User user, String billingId) {
        log.warnd(Log.MSG_DEPRECATED_METHOD_WAS_CALLED + Log.MSG_WS_CREATE_NEW_INSTANCE_INSTEAD, "getInstance");
        return new ContractDAO(user, billingId);
    }

    public Contract getContractById(int contractId) {
        Contract result = null;

        if (dbInfo.versionCompare("8.0") > 0) {
            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractService", dbInfo.versionCompare("9.2") >= 0 ? "contractList" : "contractList0");
            req.setParamContractId(contractId);
            req.setParam("fc", -1);
            req.setParam("groupMask", 0);
            req.setParam("subContracts", true);
            req.setParam("closed", true);
            req.setParam("hidden", true);
            if (dbInfo.versionCompare("9.2501") < 0)
                req.setParam("inAllLabels", true);

            JsonNode ret = transferData.postDataReturn(req, user);
            JsonNode list = ret.findValue("list");
            if (list != null && list.isArray()) {
                List<ContractSearchDto> dtoList = readJsonValue(list.traverse(), jsonTypeFactory.constructCollectionType(List.class, ContractSearchDto.class));
                result = dtoList.stream().map(ContractSearchDto::toContract).findFirst().orElse(null);
            } else { // формат для биллинга старee 9.2501
                List<Contract> contractList = readJsonValue(ret.traverse(), jsonTypeFactory.constructCollectionType(List.class, Contract.class));
                result = contractList.stream().findFirst().orElse(null);
            }
        } else {
            Request req = new Request();
            req.setModule("contract");
            req.setAction("FindContractByID");
            req.setAttribute("id", contractId);

            Document doc = transferData.postData(req, user);

            for (Element contract : XMLUtils.selectElements(doc, "/data/contracts/item")) {
                String title = contract.getAttribute("title");
                result = new Contract(dbInfo.getId(), contractId, StringUtils.substringBefore(title, "[").trim(),
                        StringUtils.substringBetween(title, "[", "]").trim());
            }
        }

        return result;
    }

    public void searchContractByTitleComment(Pageable<IdTitle> searchResult, String title, String comment, SearchOptions searchOptions) {
        if (searchResult != null) {
            if (dbInfo.versionCompare("8.0") > 0) {
                RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractService", "contractList");
                req.setParam("title", title);
                req.setParam("comment", comment);
                req.setParam("fc", -1);
                req.setParam("groupMask", 0);
                req.setParam("entityFilter", null);
                req.setParam("subContracts", searchOptions.showSub);
                req.setParam("closed", !searchOptions.showClosed); //It is turn over in billing. I don't know why!!!
                req.setParam("hidden", searchOptions.showHidden);
                req.setParam("page", searchResult.getPage());

                JsonNode data = transferData.postData(req, user);
                JsonNode ret = data.findValue("return");
                JsonNode list = ret.findValue("list");

                if (list != null && list.isArray()) {
                    List<ContractSearchDto> contractList = readJsonValue(list.traverse(), jsonTypeFactory.constructCollectionType(List.class, ContractSearchDto.class));
                    searchResult.getList().addAll(contractList.stream()
                            .map(c -> new IdTitle(c.getContractId(), c.getContractTitle() + " [ " + c.getContractComment() + " ]")).collect(Collectors.toList()));
                    searchResult.getPage().setData(jsonMapper.convertValue(ret.findValue("page"), Page.class));
                } else { // формат для биллинга старee 9.2501
                    List<Contract> contractList = readJsonValue(ret.traverse(), jsonTypeFactory.constructCollectionType(List.class, Contract.class));
                    searchResult.getList().addAll(contractList.stream()
                            .map(c -> new IdTitle(c.getId(), c.getTitle() + " [ " + c.getComment() + " ]")).collect(Collectors.toList()));
                    searchResult.getPage().setData(jsonMapper.convertValue(data.findValue("page"), Page.class));
                }
            } else {
                Page page = searchResult.getPage();
                List<IdTitle> contractList = searchResult.getList();

                int pageIndex = page.getPageIndex();
                int pageSize = page.getPageSize();

                Request req = new Request();
                req.setModule("contract");
                req.setAction("FilterContract");
                req.setAttribute("filter", 0);
                applySearchOptions(searchOptions, req);
                req.setAttribute("contractComment", comment);
                req.setAttribute("type", -1);
                req.setAttribute("contractMask", title);
                req.setAttribute("pageSize", pageSize);
                req.setAttribute("pageIndex", pageIndex);

                Document document = transferData.postData(req, user);

                Element dataElement = document.getDocumentElement();
                NodeList nodeList = dataElement.getElementsByTagName("item");

                for (int index = 0; index < nodeList.getLength(); index++) {
                    Element rowElement = (Element) nodeList.item(index);

                    contractList.add(new IdTitle(Utils.parseInt(rowElement.getAttribute("id")), rowElement.getAttribute("title")));
                }

                NodeList table = dataElement.getElementsByTagName("contracts");
                if (table.getLength() > 0) {
                    page.setRecordCount(Utils.parseInt(((Element) table.item(0)).getAttribute("recordCount")));
                    page.setPageCount(Utils.parseInt(((Element) table.item(0)).getAttribute("pageCount")));
                }
            }
        }
    }

    public void updateContractDateTo(int contractId, Date date) {
        Request request = new Request();
        request.setAction("UpdateContractDate2");
        request.setModule("contract");
        request.setContractId(contractId);
        request.setAttribute("id", contractId);
        request.setAttribute("value", TimeUtils.format(date, TimeUtils.PATTERN_DDMMYYYY));
        transferData.postData(request, user);
    }

    /**
     * Осуществляет поиск договоров по адресным параметам объекта, либо заданным
     * аргументом, либо, если таковых нет, определяет адресный параметр объекта
     * в конфигурции биллинга.
     * @param result
     * @param options
     * @param paramIds - необязательный параметр, коды адресных параметров
     * @param streetId
     * @param house
     * @param flat
     * @param room
     */
    public void searchContractByObjectAddressParam(Pageable<ParameterSearchedObject<Contract>> result, SearchOptions options,
            Set<Integer> paramIds, int streetId, String house, String flat, String room) {
        final Page page = result.getPage();
        Request req = new Request();
        req.setPage(page);
        req.setModule("contract");
        applySearchOptions(options, req);

        req.setAction("FindContract");
        if (dbInfo.versionCompare("7.0") >= 0)
            req.setAttribute("type", "o4");
        else
            req.setAttribute("type", 14);
        req.setAttribute("street", streetId);

        AddressHouse houseFrac = new AddressHouse().withHouseAndFrac(house);
        req.setAttribute("house", houseFrac.getHouse());
        req.setAttribute("frac", houseFrac.getFrac());
        req.setAttribute("flat", flat);
        req.setAttribute("room", room);

        if (paramIds != null && !paramIds.isEmpty()) {
            req.setAttribute("parameters", Utils.toString(paramIds));
        }

        setPage(req, page);

        Document doc = transferData.postData(req, user);

        Element contracts = XMLUtils.selectElement(doc, "/data/contracts");
        if (contracts != null) {
            getPage(page, contracts);

            List<ParameterSearchedObject<Contract>> list = result.getList();
            for (Element item : XMLUtils.selectElements(contracts, "item")) {
                final String fullTitle = item.getAttribute("title");

                Contract contract = new Contract(dbInfo.getId(), Utils.parseInt(item.getAttribute("id")),
                        StringUtils.substringBefore(fullTitle, "[").trim(), StringUtils.substringBetween(fullTitle, "[", "]").trim());
                list.add(new ParameterSearchedObject<>(contract, 0, StringUtils.substringAfterLast(fullTitle, "]").trim()));
            }
        }
    }

    public void searchContractByAddressParam(Pageable<ParameterSearchedObject<Contract>> result, SearchOptions options, Set<Integer> paramIds,
            int streetId, int houseId, String house, String flat, String room) {
        final Page page = result.getPage();

        Request req = new Request();
        req.setPage(page);
        req.setModule("contract");

        applySearchOptions(options, req);

        req.setAction("FindContract");
        if (dbInfo.versionCompare("9.2") >= 0)
            req.setAttribute("type", "c8");
        else if (dbInfo.versionCompare("7.0") >= 0)
            req.setAttribute("type", "c2");
        else
            req.setAttribute("type", 2);
        req.setAttribute("street", streetId);

        AddressHouse houseFrac = new AddressHouse().withHouseAndFrac(house);
        if (houseId > 0)
            req.setAttribute("houseId", houseId);
        req.setAttribute("house", houseFrac.getHouse());
        req.setAttribute("frac", houseFrac.getFrac());
        req.setAttribute("flat", flat);
        req.setAttribute("room", room);
        if (paramIds != null && !paramIds.isEmpty()) {
            req.setAttribute("parameters", Utils.toString(paramIds));
        }
        setPage(req, page);

        Document doc = transferData.postData(req, user);

        Element contracts = XMLUtils.selectElement(doc, "/data/contracts");
        if (contracts != null) {
            getPage(page, contracts);

            List<ParameterSearchedObject<Contract>> list = result.getList();
            for (Element item : XMLUtils.selectElements(contracts, "item")) {
                if (dbInfo.versionCompare("9.2407") >= 0) {
                    final String comment = item.getAttribute("comment");
                    Contract contract = new Contract(dbInfo.getId(), Utils.parseInt(item.getAttribute("id")), item.getAttribute("title"), comment);
                    list.add(new ParameterSearchedObject<>(contract, 0, StringUtils.substringBetween(comment, "[", "]").trim()));
                } else {
                    final String fullTitle = item.getAttribute("title");
                    Contract contract = new Contract(dbInfo.getId(), Utils.parseInt(item.getAttribute("id")),
                            StringUtils.substringBefore(fullTitle, "[").trim(), StringUtils.substringBetween(fullTitle, "[", "]").trim());
                    list.add(new ParameterSearchedObject<>(contract, 0, StringUtils.substringAfterLast(fullTitle, "]").trim()));
                }
            }
        }
    }

    public void searchContractByTextParam(Pageable<Contract> result, SearchOptions options, Set<Integer> paramIds, String value) {
        final Page page = result.getPage();

        Request req = new Request();
        req.setPage(page);
        req.setModule("contract");

        applySearchOptions(options, req);

        req.setAction("FindContract");
        if (dbInfo.versionCompare("7.0") >= 0)
            req.setAttribute("type", "c1");
        else
            req.setAttribute("type", 1);
        req.setAttribute("parameters", Utils.toString(paramIds));
        req.setAttribute("parameter", value);

        addSearchResult(result, page, req);
    }

    public void searchContractByPhoneParam(Pageable<Contract> result, SearchOptions options, Set<Integer> paramIds, String phone) {
        final Page page = result.getPage();

        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractService", "contractList");
            req.setParam("fc", -1);
            req.setParam("groupMask", 0);
            req.setParam("entityFilter", List.of(Map.of(
                "type", "Phone",
                "entitySpecAttrIds", Utils.toString(paramIds),
                "mode", 2,
                "value", phone
            )));
            req.setParam("subContracts", false);
            req.setParam("closed", true);
            req.setParam("hidden", true);
            req.setParam("page", page);

            JsonNode data = transferData.postData(req, user);
            JsonNode ret = data.findValue("return");
            JsonNode list = ret.findValue("list");

            final var resultList = result.getList();

            if (list != null && list.isArray()) {
                List<ContractSearchDto> dtoList = readJsonValue(list.traverse(), jsonTypeFactory.constructCollectionType(List.class, ContractSearchDto.class));
                resultList.addAll(dtoList.stream().map(ContractSearchDto::toContract).collect(Collectors.toList()));
                page.setData(jsonMapper.convertValue(ret.findValue("page"), Page.class));
            } else { // формат для биллинга старee 9.2501
                resultList.addAll(readJsonValue(ret.traverse(), jsonTypeFactory.constructCollectionType(List.class, Contract.class)));
                page.setData(jsonMapper.convertValue(data.findValue("page"), Page.class));
            }

            for (Contract contract : resultList)
                contract.setBillingId(dbInfo.getId());
        } else {
            Request req = new Request();
            req.setPage(page);
            req.setModule("contract");

            applySearchOptions(options, req);

            req.setAction("FindContract");
            if (dbInfo.versionCompare("7.0") >= 0)
                req.setAttribute("type", "c9");
            else
                req.setAttribute("type", 9);
            req.setAttribute("parameters", Utils.toString(paramIds));
            req.setAttribute("phone", phone);

            addSearchResult(result, page, req);
        }
    }

    public void searchContractByDateParam(Pageable<Contract> result, SearchOptions options, Set<Integer> paramIds, Date dateFrom, Date dateTo) {
        final Page page = result.getPage();

        Request req = new Request();
        req.setPage(page);
        req.setModule("contract");

        applySearchOptions(options, req);

        req.setAction("FindContract");
        if (dbInfo.versionCompare("7.0") >= 0)
            req.setAttribute("type", "c6");
        else
            req.setAttribute("type", 6);
        req.setAttribute("parameters", Utils.toString(paramIds));
        req.setAttribute("date1", TimeUtils.format(dateFrom, TimeUtils.PATTERN_DDMMYYYY));
        req.setAttribute("date2", TimeUtils.format(dateTo, TimeUtils.PATTERN_DDMMYYYY));

        addSearchResult(result, page, req);
    }

    public void searchContractByEmailParam(Pageable<Contract> result, SearchOptions options, Set<Integer> paramIds, String email) {
        final Page page = result.getPage();

        Request req = new Request();
        req.setPage(page);
        req.setModule("contract");

        applySearchOptions(options, req);

        req.setAction("FindContract");
        req.setAttribute("type", "c3");
        req.setAttribute("parameters", Utils.toString(paramIds));
        req.setAttribute("mail", email);

        addSearchResult(result, page, req);
    }

    public void addSearchResult(Pageable<Contract> result, final Page page, Request req) {
        setPage(req, page);

        Document doc = transferData.postData(req, user);
        Element contracts = XMLUtils.selectElement(doc, "/data/contracts");
        if (contracts != null) {
            getPage(page, contracts);

            List<Contract> list = result.getList();
            for (Element item : XMLUtils.selectElements(contracts, "item")) {
                final String fullTitle = item.getAttribute("title");

                Contract contract = new Contract(dbInfo.getId(), Utils.parseInt(item.getAttribute("id")),
                        StringUtils.substringBefore(fullTitle, "[").trim(), StringUtils.substringBetween(fullTitle, "[", "]").trim());
                list.add(contract);
            }
        }
    }

    private void applySearchOptions(SearchOptions options, Request req) {
        if (options == null) {
            req.setAttribute("del", 0);
            req.setAttribute("show_sub", 0);
            req.setAttribute("show_closed", 0);
        } else {
            req.setAttribute("del", Utils.booleanToStringInt(options.showHidden));
            req.setAttribute("show_sub", Utils.booleanToStringInt(options.showSub));
            req.setAttribute("show_closed", Utils.booleanToStringInt(options.showClosed));
        }
    }

    public Contract createContract(int patternId, Date date, String title, String customTitle, int superId) throws BGMessageException {
        if (dbInfo.getCustomerIdParam() <= 0)
            throw new BGMessageExceptionWithoutL10n("Не указан параметр customerIdParam для сервера биллинга.");

        Contract contract = null;

        if (dbInfo.versionCompare("9.2") >= 0) {
            if (date == null)
                date = new Date();

            var builder = ContractCreateData.builder().setPatternId(patternId).setDateFrom(date);

            if (Utils.notBlankString(customTitle))
                builder.setCustomTitle(customTitle);

            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractService", "contractCreate");
            req.setParam("contractCreateData", builder.build());

            int contractId = Utils.parseInt(transferData.postDataReturn(req, user).asText());

            return getContractById(contractId);
        } else {
            Request req = new Request();
            req.setModule("contract");
            req.setAction("NewContract");
            req.setAttribute("pattern_id", patternId);
            req.setAttribute("date", date);
            if (Utils.notBlankString(customTitle)) {
                req.setAttribute("custom_title", customTitle);
            }
            if (Utils.notBlankString(title)) {
                req.setAttribute("title", title);
            }

            if (superId > 0) {
                req.setAttribute("super_id", superId);
            }

            Document result = transferData.postData(req, user);

            int contractId = Utils.parseInt(XMLUtils.selectText(result, "/data/contract/@id"));
            String contractTitle = Utils.maskNull(XMLUtils.selectText(result, "/data/contract/@title"));

            contract = new Contract();
            contract.setId(contractId);
            contract.setTitle(contractTitle);
            contract.setBillingId(this.dbInfo.getId());
        }

        return contract;
    }

    public void faceLog(Pageable<ContractFace> result, int contractId) {
        List<ContractFace> list = result.getList();

        Request request = new Request();
        request.setModule("contract");
        request.setAction("ContractFace");
        request.setContractId(contractId);
        request.setAttribute("type", "face");
        // 1 - прямая сортировка
        request.setAttribute("view", 0);
        setPage(request, result.getPage());

        Document document = transferData.postData(request, user);
        for (Element el : XMLUtils.selectElements(document, "/data/table/data/row")) {
            ContractFace face = new ContractFace();
            face.setTime(TimeUtils.parse(el.getAttribute("date"), TimeUtils.PATTERN_DDMMYYYYHHMMSS));
            face.setUser(el.getAttribute("user"));
            face.setFace(el.getAttribute("value"));

            list.add(face);
        }

        getPage(result.getPage(), XMLUtils.selectElement(document, "/data/table"));
    }

    public void updateFace(int contractId, int face) {
        Request req = new Request();
        req.setModule("contract");
        req.setAction("SetFcContract");
        req.setContractId(contractId);
        req.setAttribute("value", face);

        transferData.postData(req, user);
    }

    public void modeLog(Pageable<ContractMode> result, int contractId) {
        List<ContractMode> list = result.getList();

        Request request = new Request();
        request.setModule("contract");
        request.setAction("ContractMode");
        request.setContractId(contractId);
        request.setAttribute("type", "mode");
        // 1 - прямая сортировка
        request.setAttribute("view", 0);
        setPage(request, result.getPage());

        Document document = transferData.postData(request, user);
        for (Element el : XMLUtils.selectElements(document, "/data/table/data/row")) {
            ContractMode face = new ContractMode();
            face.setTime(TimeUtils.parse(el.getAttribute("date"), TimeUtils.PATTERN_DDMMYYYYHHMMSS));
            face.setUser(el.getAttribute("user"));
            face.setMode(el.getAttribute("value"));

            list.add(face);
        }

        getPage(result.getPage(), XMLUtils.selectElement(document, "/data/table"));
    }

    public void updateMode(int contractId, int mode) {
        Request req = new Request();
        req.setModule("contract");
        req.setAction("UpdateContractMode");
        req.setContractId(contractId);
        req.setAttribute("value", mode == ContractMode.MODE_CREDIT ? "credit" : "debet");

        transferData.postData(req, user);
    }


    public List<ContractGroup> getContractLabelTreeItemList(int contractId) {
        List<ContractGroup> groups = new ArrayList<>();
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_LABEL, "ContractLabelService",
                    "getContractLabelTreeItemList");
            req.setParam("contractId", contractId);
            req.setParam("calcCount", false);
            JsonNode ret = transferData.postDataReturn(req, user);
            groups = readJsonValue(ret.traverse(), jsonTypeFactory.constructCollectionType(List.class, ContractGroup.class));
        }
        return groups;
    }

    public Pair<List<IdTitle>, Set<Integer>> groupsGet(int contractId) {
        List<IdTitle> groupList = new ArrayList<>();
        Set<Integer> selectedIds = new HashSet<>();
        if (dbInfo.versionCompare("9.2") >= 0) {
            List<ContractGroup> groups = getContractLabelTreeItemList(contractId);
            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_LABEL, "ContractLabelService", "getContractLabelIds");
            req.setParam("contractId", contractId);
            JsonNode ret = transferData.postDataReturn(req, user);
            selectedIds = readJsonValue(ret.traverse(), jsonTypeFactory.constructCollectionType(Set.class, Integer.class));
            groupList = groups.stream()
                    .map(gr -> new IdTitle(gr.getId(), gr.getTitle()))
                    .collect(Collectors.toList());
        }
        else {
            Request request = new Request();
            request.setModule("contract");
            request.setAction("ContractGroup");
            request.setContractId(contractId);

            Document document = transferData.postData(request, user);

            long selected = 0L;
            Element groups = XMLUtils.selectElement(document, "/data/groups");
            if (groups != null) {
                selected = Utils.parseLong(groups.getAttribute("selected"));
            }

            for (Element rowElement : XMLUtils.selectElements(document, "/data/groups/group")) {
                IdTitle group = new IdTitle();
                group.setId(Utils.parseInt(rowElement.getAttribute("id")));
                group.setTitle(rowElement.getAttribute("title"));

                groupList.add(group);

                if ((selected & (1L << group.getId())) > 0) {
                    selectedIds.add(group.getId());
                }
            }
        }

        return new Pair<>(groupList, selectedIds);
    }

    public void updateLabels(int contractId, Set<Integer> labelIds) {
        if (dbInfo.versionCompare("9.2") > 0) {
             RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_LABEL, "ContractLabelService", "setContractLabelIds");
            req.setParam("contractId", contractId);
            req.setParam("contractLabelsIds", labelIds);
            transferData.postData(req, user);
        } else {
            Set<Integer> currentGroups = groupsGet(contractId).getSecond();

            for (Integer deleteGroup : (Iterable<Integer>) CollectionUtils.subtract(currentGroups, labelIds)) {
                updateGroup("del", contractId, deleteGroup);
            }
            for (Integer addGroup : (Iterable<Integer>) CollectionUtils.subtract(labelIds, currentGroups)) {
                updateGroup("add", contractId, addGroup);
            }
        }
    }

    private void updateGroup(String command, int contractId, int groupId) {
        if (dbInfo.versionCompare("5.2") < 0) {
            Request request = new Request();
            request.setModule("contract");
            request.setContractId(contractId);
            request.setAttribute("value", groupId);
            request.setAttribute("id", contractId);

            if ("add".equals(command)) {
                request.setAction("UpdateContractGroup");
            } else if ("del".equals(command)) {
                request.setAction("ClearContractGroup");
            }

            transferData.postData(request, user);
        } else {
            RequestJsonRpc req = null;
            if ("add".equals(command)) {
                req = new RequestJsonRpc(KERNEL_CONTRACT_API,
                        "ContractService", "contractGroupAdd");

            } else if ("del".equals(command)) {
                req = new RequestJsonRpc(KERNEL_CONTRACT_API,
                        "ContractService", "contractGroupRemove");
            }
            if (req != null) {
                req.setParamContractId(contractId);
                req.setParam("contractGroupId", groupId);
                transferData.postDataReturn(req, user);
            }
        }
    }

    public List<IdTitle> additionalActionList(int contractId) {
        Request request = new Request();
        request.setModule("contract");
        request.setAction("AdditionalActionList");
        request.setContractId(contractId);

        Document document = transferData.postData(request, user);

        Element dataElement = document.getDocumentElement();
        NodeList nodeList = dataElement.getElementsByTagName("item");

        List<IdTitle> additionalActionList = new ArrayList<>();
        for (int index = 0; index < nodeList.getLength(); index++) {
            Element rowElement = (Element) nodeList.item(index);
            IdTitle additionalAction = new IdTitle();
            additionalAction.setId(Utils.parseInt(rowElement.getAttribute("id")));
            additionalAction.setTitle(rowElement.getAttribute("title"));

            additionalActionList.add(additionalAction);
        }

        return additionalActionList;
    }

    public String executeAdditionalAction(int contractId, int actionId) {
        Request request = new Request();
        request.setModule("contract");
        request.setAction("AdditionalAction");
        request.setContractId(contractId);
        request.setAttribute("action_id", actionId);

        Document document = transferData.postData(request, user);

        Element dataElement = document.getDocumentElement();
        NodeList nodeList = dataElement.getElementsByTagName("row");

        StringBuilder result = new StringBuilder();
        for (int index = 0; index < nodeList.getLength(); index++) {
            Element rowElement = (Element) nodeList.item(index);
            result.append(rowElement.getAttribute("text") + "&#13;&#10;");
        }

        return result.toString();
    }

    public Pair<List<IdTitle>, List<IdTitle>> moduleList(int contractId) {
        List<IdTitle> selectedList = new ArrayList<>(), availableList = new ArrayList<>();

        if (dbInfo.versionCompare("9.2410") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.module", "ModuleService", "contractModules");
            req.setParamContractId(contractId);
            selectedList = readJsonValue(transferData.postDataReturn(req, user).traverse(), jsonTypeFactory.constructCollectionType(List.class, IdTitle.class));

            req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.module", "ModuleService", "moduleList");
            availableList = readJsonValue(transferData.postDataReturn(req, user).traverse(), jsonTypeFactory.constructCollectionType(List.class, IdTitle.class));

            Set<Integer> selectedIds = Utils.getObjectIdsSet(selectedList);
            availableList.removeIf(module -> selectedIds.contains(module.getId()));
        } else {
            Request request = new Request();
            request.setModule("contract");
            request.setAction("ContractModuleList");
            request.setContractId(contractId);

            Document document = transferData.postData(request, user);
            for (Element item : XMLUtils.selectElements(document, "/data/list_select/item")) {
                selectedList.add(new IdTitle(Utils.parseInt(item.getAttribute("id")), item.getAttribute("title")));
            }
            for (Element item : XMLUtils.selectElements(document, "/data/list_avaliable/item")) {
                availableList.add(new IdTitle(Utils.parseInt(item.getAttribute("id")), item.getAttribute("title")));
            }
        }

        return new Pair<>(selectedList, availableList);
    }

    public void updateModule(int contractId, int moduleId, String command) throws BGMessageException {
        if (dbInfo.versionCompare("9.2410") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractService",
                    "del".equals(command) ? "contractModuleDelete" : "contractModuleAdd");
            req.setParamContractId(contractId);
            req.setParam("moduleIds", List.of(moduleId));

            transferData.postData(req, user);
        } else {
            Request request = new Request();
            request.setModule("contract");
            request.setContractId(contractId);
            request.setAttribute("module_id", moduleId);

            if ("add".equals(command)) {
                request.setAction("ContractModuleAdd");
            } else if ("del".equals(command)) {
                request.setAction("ContractModuleDelete");
            } else {
                throw new BGMessageExceptionWithoutL10n("Неверный параметр command");
            }

            transferData.postData(request, user);
        }
    }

    public BigDecimal limit(int contractId, Pageable<LimitLogItem> log, List<LimitChangeTask> taskList) {
        BigDecimal result;
        Document doc = null;

        if (dbInfo.versionCompare("6.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(
                    dbInfo.versionCompare("7.0") >= 0 ? KERNEL_CONTRACT_LIMIT : KERNEL_CONTRACT_API,
                    "ContractLimitService", "contractLimitGet");
            req.setParamContractId(contractId);

            result = jsonMapper.convertValue(transferData.postDataReturn(req, user), BigDecimal.class);
        } else {
            Request request = new Request();
            request.setModule("contract");
            request.setContractId(contractId);
            request.setAction("ContractLimit");
            if (log != null) {
                setPage(request, log.getPage());
            }

            doc = transferData.postData(request, user);
            result = Utils.parseBigDecimal(XMLUtils.selectText(doc, "/data/table/@limitValue"));
        }

        if (log != null) {
            if (dbInfo.versionCompare("6.2") >= 0) {
                RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_LIMIT, "ContractLimitService",
                        "searchContractLimitLogList");
                req.setParamContractId(contractId);
                req.setParam("page", log.getPage());

                JsonNode ret = transferData.postDataReturn(req, user);
                List<LimitLogItem> sessionList = readJsonValue(ret.findValue("list").traverse(),
                        jsonTypeFactory.constructCollectionType(List.class, LimitLogItem.class));
                log.getList().addAll(sessionList);
                log.getPage().setData(jsonMapper.convertValue(ret.findValue("page"), Page.class));
            } else {
                getPage(log.getPage(), XMLUtils.selectElement(doc, "/data/table"));

                for (Element item : XMLUtils.selectElements(doc, "/data/table/data/row")) {
                    LimitLogItem logItem = new LimitLogItem();
                    logItem.setTime(TimeUtils.parse(item.getAttribute("f0"), TimeUtils.PATTERN_DDMMYYYYHHMMSS));
                    logItem.setUser(item.getAttribute("f1"));
                    logItem.setLimit(Utils.parseBigDecimal(item.getAttribute("f2")));
                    logItem.setComment(item.getAttribute("comment"));
                    logItem.setDays(item.getAttribute("days"));

                    log.getList().add(logItem);
                }
            }
        }

        if (taskList != null  ) {
            if (dbInfo.versionCompare("6.2") >= 0) {
                RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_LIMIT, "ContractLimitService",
                        "searchContractLimitAvtoList");
                req.setParamContractId(contractId);
                if (log != null) {
                    req.setParam("page", log.getPage());
                }

                JsonNode ret = transferData.postDataReturn(req, user);
                List<LimitChangeTask> sessionList = readJsonValue(ret.findValue("list").traverse(),
                        jsonTypeFactory.constructCollectionType(List.class, LimitChangeTask.class));
                taskList.addAll(sessionList);
            } else{
                for (Element item : XMLUtils.selectElements(doc, "/data/table_lp/data/row")) {
                    LimitChangeTask task = new LimitChangeTask();
                    task.setId(Utils.parseInt(item.getAttribute("id")));
                    task.setDate(TimeUtils.parse(item.getAttribute("f0"), TimeUtils.PATTERN_DDMMYYYY));
                    task.setUser(item.getAttribute("f1"));
                    task.setLimitChange(Utils.parseBigDecimal(item.getAttribute("f2")));

                    taskList.add(task);
                }
            }
        }

        return result;
    }

    public void updateLimit(int contractId, BigDecimal limit, int days, String comment) {
        if (days > 0) {
            if (dbInfo.versionCompare("6.2") >= 0) {
                RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_LIMIT, "ContractLimitService", "updateContractLimitPeriod");
                req.setParamContractId(contractId);
                req.setParam("limit", limit);
                req.setParam("period", days);
                req.setParam("comment", comment);
                transferData.postData(req, user);
            } else {
                Request request = new Request();
                request.setModule("contract");
                request.setContractId(contractId);
                request.setAttribute("comment", comment);
                request.setAction("UpdateContractLimitPeriod");
                request.setAttribute("limit", Utils.format(limit));
                request.setAttribute("period", days);
                transferData.postData(request, user);
            }
        } else {
            if (dbInfo.versionCompare("9.2") > 0) {
                RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_LIMIT, "ContractLimitService", "updateContractLimit");
                req.setParamContractId(contractId);
                req.setParam("limit", limit);
                req.setParam("comment", comment);
                transferData.postData(req, user);
            } else {
                Request request = new Request();
                request.setModule("contract");
                request.setContractId(contractId);
                request.setAttribute("comment", comment);
                request.setAction("UpdateContractLimit");
                request.setAttribute("value", Utils.format(limit));
                transferData.postData(request, user);
            }
        }
    }

    public void deleteLimitTask(int contractId, int id) {
        if (dbInfo.versionCompare("8.0") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.limit", "ContractLimitService", "cancelLimitChangeTask");
            req.setParamContractId(contractId);
            req.setParam("taskIds", Collections.singletonList(id));
            transferData.postDataReturn(req, user);
        } else {
            Request request = new Request();
            request.setModule("contract");
            request.setAction("LimitChangeTaskDelete");
            request.setContractId(contractId);
            request.setAttribute("id", id);

            transferData.postData(request, user);
        }
    }

    public String getContractStatisticPassword(int contractId) {
        Document contractCard = new ContractDAO(this.user, this.dbInfo).getContractCardDoc(contractId);
        return XMLUtils.selectText(contractCard, "/data/contract/@pswd");
    }

    public List<IdTitle> getContractAddress(int contractId) {
        Request request = new Request();
        request.setModule("contract");
        request.setAction("ContractAddressList");
        request.setContractId(contractId);

        Document document = transferData.postData(request, user);

        List<IdTitle> contractAddress = new ArrayList<>();
        Element dataElement = document.getDocumentElement();
        NodeList nodeList = dataElement.getElementsByTagName("item");

        for (int index = 0; index < nodeList.getLength(); index++) {
            Element rowElement = (Element) nodeList.item(index);
            IdTitle address = new IdTitle();
            address.setId(Utils.parseInt(rowElement.getAttribute("id")));
            address.setTitle(rowElement.getAttribute("title"));

            contractAddress.add(address);
        }

        return contractAddress;
    }

    public void updateContractPassword(int contractId, String value, boolean generate) {
        Request request = new Request();
        request.setModule("contract");
        request.setAction("UpdateContractPassword");
        request.setContractId(contractId);
        request.setAttribute("value", Utils.maskNull(value));

        if (generate) {
            request.setAttribute("set_pswd", 1);
        }

        transferData.postData(request, user);
    }

    public String getContractFullCard(int contractId) {
        Request req = new Request();

        req.setModule("contract");
        req.setAction("ContractCardXml");
        req.setAttribute("contentType", "html");
        req.setContractId(contractId);

        return transferData.postDataGetString(req, user);
    }

    public Document getContractCardDoc(int contractId) {
        Request req = new Request();

        req.setModule("contract");
        req.setAction("ContractCardXml");
        req.setAttribute("contentType", "xml");
        req.setContractId(contractId);

        return transferData.postData(req, user);
    }

    public List<String[]> getContractCardTypes(int contractId) {
        List<String[]> result = new ArrayList<>();

        if (dbInfo.versionCompare("8.0") > 0) {
            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractService", "contractCardList");
            req.setParamContractId(contractId);

            List<IdStringTitle> list = readJsonValue(transferData.postDataReturn(req, user).traverse(),
                    jsonTypeFactory.constructCollectionType(List.class, IdStringTitle.class));
            for (var item : list)
                result.add(new String[] { item.getId(), item.getTitle() });
        } else {
            Request req = new Request();

            req.setModule("contract");
            req.setAction("ContractCard2ListTypes");
            req.setContractId(contractId);

            Document doc = transferData.postData(req, user);
            for (Element el : XMLUtils.selectElements(doc, "/data/combo/el")) {
                result.add(new String[] { el.getAttribute("id"), el.getAttribute("title") });
            }
        }

        return result;
    }

    public byte[] getContractCard2Pdf(int contractId, String type) {
        Request req = new Request();

        req.setModule("contract");
        req.setAction("ContractCard2");
        req.setAttribute("contentType", "application/pdf");
        req.setContractId(contractId);
        req.setAttribute("type", type);

        return transferData.postDataGetBytes(req, user);
    }

    public void bgbillingOpenContract(int contractId) {
        Request req = new Request();

        req.setModule("admin");
        req.setAction("Command");
        req.setAttribute("command", "put");
        req.setAttribute("value", "openContract:" + contractId);

        transferData.postData(req, user);
    }

    public void bgbillingUpdateContractTitleAndComment(int contractId, String comment, int patid) {
        if (dbInfo.versionCompare("8.0") > 0) {
            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractService", "contractTitleAndCommentUpdate");
            req.setParamContractId(contractId);
            req.setParam("title", null);
            req.setParam("comment", comment);
            req.setParam("patternId", patid);
            transferData.postDataReturn(req, user);
        } else {
            Request req = new Request();
            req.setModule("contract");
            req.setAction("UpdateContractTitleAndComment");
            req.setContractId(contractId);
            if (patid > 0) {
                req.setAttribute("patid", patid);
            }
            req.setAttribute("comment", comment);

            transferData.postData(req, user);
        }
    }

    public List<IdTitle> bgbillingGetContractPatternList() {
        Request req = new Request();

        req.setModule("contract");
        req.setAction("GetPatternList");

        Document document = transferData.postData(req, user);

        List<IdTitle> contractPatterns = new ArrayList<>();
        Element dataElement = document.getDocumentElement();
        NodeList nodeList = dataElement.getElementsByTagName("item");

        for (int index = 0; index < nodeList.getLength(); index++) {
            Element rowElement = (Element) nodeList.item(index);
            IdTitle pattern = new IdTitle();
            pattern.setId(Utils.parseInt(rowElement.getAttribute("id")));
            pattern.setTitle(rowElement.getAttribute("title"));

            contractPatterns.add(pattern);
        }

        return contractPatterns;
    }

    public List<IdTitle> getStreetsByCity(int cityId) {
        Request request = new Request();
        request.setModule("contract");
        request.setAction("GetStreetsByCity");

        request.setAttribute("city", cityId);

        Document document = transferData.postData(request, user);

        List<IdTitle> streets = new LinkedList<>();

        for (Element item : XMLUtils.selectElements(document, "/data/streets/item")) {
            streets.add(new IdTitle(Utils.parseInt(item.getAttribute("id")), item.getAttribute("title")));
        }

        return streets;
    }

    public OpenContract openContract() {
        Request request = new Request();
        request.setModule("contract");
        request.setAction("OpenContract");

        Document document = transferData.postData(request, user);
        return new OpenContract(document);
    }

    public List<IdTitle> getParameterList(int parameterTypeId) {
        List<IdTitle> paramList;

        if (dbInfo.versionCompare("8.0") > 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.param", "ContractParameterServiceOld",
                    "getContractParameterPrefList");
            req.setParam("paramType", parameterTypeId);
            JsonNode ret = transferData.postDataReturn(req, user);
            paramList = readJsonValue(ret.traverse(), jsonTypeFactory.constructCollectionType(List.class, IdTitle.class));
        } else if (dbInfo.versionCompare("7.0") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.param", "ContractParameterService",
                    "getContractParameterPrefList");
            req.setParam("paramType", parameterTypeId);
            JsonNode ret = transferData.postDataReturn(req, user);
            paramList = readJsonValue(ret.traverse(), jsonTypeFactory.constructCollectionType(List.class, IdTitle.class));
        } else {
            Request req = new Request();
            req.setModule("admin");
            req.setAction("ContractParameters");
            req.setAttribute("type", parameterTypeId);
            Document document = transferData.postData(req, user);
            paramList = new ArrayList<>();
            for (Element el : XMLUtils.selectElements(document, "/data/table/data/row")) {
                paramList.add(new IdTitle(Utils.parseInt(el.getAttribute("id")), el.getAttribute("title")));
            }
        }

        return paramList;
    }

    public ContractInfo getContractInfo(int contractId) throws Exception {
        ContractInfo result = null;

        if (dbInfo.versionCompare("8.0") > 0) {
            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractService", "contractInfoGet");
            req.setParamContractId(contractId);

            JsonNode contractInfo = jsonMapper.readTree(transferData.postDataReturn(req, user).get("json").asText());
            JsonNode contract = contractInfo.get("contract");
            if (contract != null) {
                result = jsonMapper.convertValue(contract, ContractInfo.class);

                result.setBillingId(dbInfo.getId());
                result.setId(contractId);
                String[] objects = contract.get("objects").asText().split("/");
                result.setObjects(Utils.parseInt(objects[0]), Utils.parseInt(objects[1]));

                if ("super".equals(result.getHierarchy()))
                    result.setSubContractIds(new ContractHierarchyDAO(user, dbInfo).getSubContracts(contractId));

                JsonNode info = contractInfo.get("info");
                if (info != null) {
                    result.setGroupList(readJsonValue(info.get("groups").traverse(), jsonTypeFactory.constructCollectionType(List.class, IdTitle.class)));
                    result.setTariffList(readJsonValue(info.get("tariff").traverse(), jsonTypeFactory.constructCollectionType(List.class, IdTitle.class)));
                    result.setScriptList(readJsonValue(info.get("script").traverse(), jsonTypeFactory.constructCollectionType(List.class, IdTitle.class)));

                    JsonNode modules = info.get("modules");
                    if (modules != null)
                        result.setModuleList(readJsonValue(modules.traverse(), jsonTypeFactory.constructCollectionType(List.class, ContractInfo.ModuleInfo.class)));

                    JsonNode balance = info.get("balance");
                    if (balance != null) {
                        result.setBalanceDate(TimeConvert.toDate(LocalDate.of(balance.get("yy").asInt(), balance.get("mm").asInt(), 1)));
                        result.setBalanceIn(Utils.parseBigDecimal(balance.get("summa1").asText()));
                        result.setBalancePayment(Utils.parseBigDecimal(balance.get("summa2").asText()));
                        result.setBalanceAccount(Utils.parseBigDecimal(balance.get("summa3").asText()));
                        result.setBalanceCharge(Utils.parseBigDecimal(balance.get("summa4").asText()));
                        result.setBalanceOut(Utils.parseBigDecimal(balance.get("summa5").asText()));
                    }
                }
            }
        } else {
            Request req = new Request();
            req.setModule("contract");
            req.setAction("ContractInfo");
            req.setAttribute("cid", contractId);

            Document doc = transferData.postData(req, user);

            Element contract = XMLUtils.selectElement(doc, "/data/contract");
            if (contract != null) {
                result = new ContractInfo();

                result.setBillingId(dbInfo.getId());
                result.setId(contractId);
                result.setComment(contract.getAttribute("comment"));
                result.setObjects(Utils.parseInt(contract.getAttribute("objects").split("/")[0]),
                        Utils.parseInt(contract.getAttribute("objects").split("/")[1]));
                result.setHierarchy(contract.getAttribute("hierarchy"));
                result.setHierarchyDep(Utils.parseInt(contract.getAttribute("hierarchyDep")));
                result.setHierarchyIndep(Utils.parseInt(contract.getAttribute("hierarchyIndep")));
                result.setDeleted(Utils.parseBoolean(contract.getAttribute("del")));
                result.setFace(Utils.parseInt(contract.getAttribute("fc")));
                result.setDateFrom(TimeUtils.parse(contract.getAttribute("date1"), TimeUtils.PATTERN_DDMMYYYY));
                result.setDateTo(TimeUtils.parse(contract.getAttribute("date2"), TimeUtils.PATTERN_DDMMYYYY));
                result.setMode(Utils.parseInt(contract.getAttribute("mode")));
                result.setBalanceLimit(Utils.parseBigDecimal(contract.getAttribute("limit"), BigDecimal.ZERO));
                result.setStatus(contract.getAttribute("status"));
                result.setTitle(contract.getAttribute("title"));
                result.setComments(Utils.parseInt(contract.getAttribute("comments")));

                if ("super".equals(contract.getAttribute("hierarchy"))) {
                    result.setSubContractIds(new ContractHierarchyDAO(user, dbInfo).getSubContracts(contractId));
                }

                result.setGroupList(getList(XMLUtils.selectElement(doc, "/data/info/groups")));
                result.setTariffList(getList(XMLUtils.selectElement(doc, "/data/info/tariff")));
                result.setScriptList(getList(XMLUtils.selectElement(doc, "/data/info/script")));

                Element modules = XMLUtils.selectElement(doc, "/data/info/modules");
                if (modules != null) {
                    List<ContractInfo.ModuleInfo> moduleList = new ArrayList<>();
                    for (Element item : XMLUtils.selectElements(modules, "item")) {
                        moduleList.add(new ContractInfo.ModuleInfo(Utils.parseInt(item.getAttribute("id")), item.getAttribute("title"), item.getAttribute("package"),
                                item.getAttribute("status")));
                    }
                    result.setModuleList(moduleList);
                }

                Element balance = XMLUtils.selectElement(doc, "/data/info/balance");
                if (balance != null) {
                    result.setBalanceDate(
                            new GregorianCalendar(Utils.parseInt(balance.getAttribute("yy")), Utils.parseInt(balance.getAttribute("mm")) - 1, 1)
                                    .getTime());
                    result.setBalanceIn(Utils.parseBigDecimal(balance.getAttribute("summa1"), BigDecimal.ZERO));
                    result.setBalancePayment(Utils.parseBigDecimal(balance.getAttribute("summa2"), BigDecimal.ZERO));
                    result.setBalanceAccount(Utils.parseBigDecimal(balance.getAttribute("summa3"), BigDecimal.ZERO));
                    result.setBalanceCharge(Utils.parseBigDecimal(balance.getAttribute("summa4"), BigDecimal.ZERO));
                    result.setBalanceOut(Utils.parseBigDecimal(balance.getAttribute("summa5"), BigDecimal.ZERO));
                }
            }
        }

        return result;
    }

    private List<IdTitle> getList(Element node) {
        List<IdTitle> result = Collections.emptyList();

        if (node != null) {
            result = new ArrayList<>();
            for (Element item : XMLUtils.selectElements(node, "item")) {
                result.add(new IdTitle(Utils.parseInt(item.getAttribute("id")), item.getAttribute("title")));
            }
        }

        return result;
    }

    public void copyParametersToBilling(Connection con, int customerId, int contractId, String title) throws Exception {
        Customer customer = new CustomerDAO(con).getCustomerById(customerId);

        String copyParamsMapping = dbInfo.getSetup().get("copyParamMapping", "");

        copyObjectParamsToContract(con, copyParamsMapping, customerId, contractId, customer);
    }
    public void copyObjectParamsToContract(Connection con, String copyParamsMapping, int objectId, int contractId,
                                           Customer customer) throws SQLException, BGMessageException {
        ParamValueDAO paramDAO = new ParamValueDAO(con);


        try {
            if (customer != null) {
                bgbillingUpdateContractTitleAndComment(contractId,customer.getTitle(),0);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BGMessageExceptionWithoutL10n("Ошибка копирования имени контрагента: " + customer.getTitle() + "; "
                    + dbInfo.getTitle() + ", " + e.getMessage());
        }

        if (Utils.isBlankString(copyParamsMapping)) {
            return;
        }

        String[] params = copyParamsMapping.split(";");

        for (String pair : params) {
            try {
                String[] keyValue = pair.split(":");

                String fromParamId = keyValue[0].indexOf('[') == -1 ? keyValue[0]
                        : keyValue[0].substring(0, keyValue[0].indexOf('['));
                int toParamId = Utils.parseInt(keyValue[1].indexOf('[') == -1 ? keyValue[1]
                        : keyValue[1].substring(0, keyValue[1].indexOf('[')));

                Request request = new Request();
                request.setModule("contract");
                request.setAttribute("cid", contractId);
                request.setAttribute("pid", toParamId);

                if (fromParamId.equals("customerTitle")) {
                    if (customer != null) {
                        request.setAttribute("action", "UpdateParameterType1");
                        request.setAttribute("value", customer.getTitle());

                        transferData.postData(request, user);
                    }
                } else {
                    Parameter param = ParameterCache.getParameter(Integer.parseInt(fromParamId));
                    if (param == null) {
                        throw new BGMessageException(
                                "Ошибка при копировании параметра: параметр с ID=" + fromParamId + " не существует!");
                    }
                    String type = param.getType();

                    if (Parameter.TYPE_ADDRESS.equals(type)) {
                        SortedMap<Integer, ParameterAddressValue> values = paramDAO.getParamAddress(objectId,
                                Integer.parseInt(fromParamId));

                        if (values.size() > 0) {
                            ParameterAddressValue value = values.get(values.firstKey());

                            request.setAction("UpdateAddressInfo");

                            request.setAttribute("hid", value.getHouseId());
                            request.setAttribute("flat", value.getFlat());
                            request.setAttribute("floor", value.getFloor() == null ? "" : value.getFloor());
                            request.setAttribute("pod", value.getPod());
                            request.setAttribute("room", value.getRoom());
                            request.setAttribute("comment", value.getComment());

                            transferData.postData(request, user);
                        }
                    } else if (Parameter.TYPE_TEXT.equals(type)) {
                        String value = paramDAO.getParamText(objectId, Integer.parseInt(fromParamId));
                        if (Utils.notBlankString(value)) {
                            request.setAction("UpdateParameterType1");
                            request.setAttribute("value", value);

                            transferData.postData(request, user);
                        }
                    } else if (Parameter.TYPE_LIST.equals(type)) {
                        Set<Integer> listValue = paramDAO.getParamList(objectId, Integer.parseInt(fromParamId));

                        if (listValue != null && listValue.size() > 0) {
                            // биллинг не поддерживает множественные значения списков, поэтому берем первый
                            String fromValue = listValue.iterator().next().toString();

                            String toValue = null;
                            // преобразование по карте соответствий
                            if (keyValue[0].indexOf('[') > 0) {
                                String[] fromVals = keyValue[0]
                                        .substring(keyValue[0].indexOf('[') + 1, keyValue[0].indexOf(']')).split(",");
                                String[] toVals = keyValue[1]
                                        .substring(keyValue[1].indexOf('[') + 1, keyValue[1].indexOf(']')).split(",");

                                for (int i = 0; i < fromVals.length; i++) {
                                    if (fromVals[i].equals(fromValue)) {
                                        toValue = toVals[i];
                                        break;
                                    }
                                }
                            } else {
                                toValue = fromValue;
                            }

                            if (Utils.notBlankString(toValue)) {
                                new ContractParamDAO(user, dbInfo).updateListParameter(contractId, toParamId, toValue);
                            }
                        }
                    } else if (Parameter.TYPE_PHONE.equals(type)) {
                        ParameterPhoneValue value = paramDAO.getParamPhone(objectId, Integer.parseInt(fromParamId));
                        if (value != null) {
                            new ContractParamDAO(user, dbInfo).updatePhoneParameter(contractId, toParamId, value);
                        }
                    } else if (Parameter.TYPE_DATE.equals(type)) {
                        Date value = paramDAO.getParamDate(objectId, Integer.parseInt(fromParamId));
                        if (value != null) {
                            new ContractParamDAO(user, dbInfo).updateDateParameter(contractId, toParamId, value);
                        }
                    } else if (Parameter.TYPE_EMAIL.equals(type)) {
                        SortedMap<Integer, ParameterEmailValue> value = paramDAO.getParamEmail(objectId,
                                Integer.parseInt(fromParamId));
                        if (value.size() > 0) {
                            new ContractParamDAO(user, dbInfo).updateEmailParameter(contractId, toParamId,
                                    value.values());
                        }
                    }
                }
            } catch (BGException e) {
                log.error(e.getMessage(), e);
                throw new BGMessageException(
                        "Ошибка при копировании параметра в биллинг! [" + pair + "] " + e.getMessage());
            }
        }
    }

    public static void copyParametersToAllContracts(Connection con, User user, int customerId) throws Exception {
        CustomerLinkDAO linkDao = new CustomerLinkDAO(con);
        for (CommonObjectLink link : linkDao.getObjectLinksWithType(customerId, Contract.OBJECT_TYPE + "%")) {
            Contract contract = new Contract(link);
            new ContractDAO(user, contract.getBillingId()).copyParametersToBilling(con, customerId, contract.getId(), contract.getTitle());
        }
    }
}
