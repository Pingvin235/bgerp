package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.exception.BGMessageExceptionTransparent;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.Pageable;
import org.bgerp.model.base.IdTitle;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.JsonNode;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.dao.CustomerLinkDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
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
import ru.bgcrm.plugin.bgbilling.proto.dao.version.v8x.ContractDAO8x;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractFace;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractGroup;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractInfo;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractMemo;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractMode;
import ru.bgcrm.plugin.bgbilling.proto.model.OpenContract;
import ru.bgcrm.plugin.bgbilling.proto.model.limit.LimitChangeTask;
import ru.bgcrm.plugin.bgbilling.proto.model.limit.LimitLogItem;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

public class ContractDAO extends BillingDAO {
    private static final Log log = Log.getLog();

    public static final String KERNEL_CONTRACT_API = "ru.bitel.bgbilling.kernel.contract.api";
    public static final String KERNEL_CONTRACT_LIMIT = "ru.bitel.bgbilling.kernel.contract.limit";

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

    public static ContractDAO getInstance(User user, DBInfo dbInfo) throws BGException {
        if (dbInfo.versionCompare("8.0") > 0) {
            return new ContractDAO8x(user, dbInfo);
        } else {
            return new ContractDAO(user, dbInfo);
        }
    }

    public static ContractDAO getInstance(User user, String billingId) throws BGException {
        if (BillingDAO.getVersion(user, billingId).compareTo("8.0") > 0) {
            return new ContractDAO8x(user, billingId);
        } else {
            return new ContractDAO(user, billingId);
        }
    }

    protected ContractDAO(User user, String billingId) throws BGException {
        super(user, billingId);
    }

    protected ContractDAO(User user, DBInfo dbInfo) throws BGException {
        super(user, dbInfo);
    }

    public Contract getContractById(int contractId) throws BGException {
        Contract result = null;

        if (dbInfo.versionCompare("8.0") > 0) {
            RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_API, "ContractService", "contractList0");
            req.setParamContractId(contractId);
            req.setParam("fc", -1);
            req.setParam("subContracts", true);
            req.setParam("closed", true);
            req.setParam("hidden", true);
            req.setParam("inAllLabels", true);

            JsonNode ret = transferData.postData(req, user);
            List<Contract> contractList = readJsonValue(ret.findValue("return").traverse(),
                    jsonTypeFactory.constructCollectionType(List.class, Contract.class));
            result = contractList.stream().findFirst().orElse(null);
        } else {
            Request req = new Request();
            req.setModule("contract");
            req.setAction("FindContractByID");
            req.setAttribute("id", contractId);

            Document doc = transferData.postData(req, user);

            for (Element contract : XMLUtils.selectElements(doc, "/data/contracts/item")) {
                String title = contract.getAttribute(TITLE);
                result = new Contract(dbInfo.getId(), contractId, StringUtils.substringBefore(title, "[").trim(),
                        StringUtils.substringBetween(title, "[", "]").trim());
            }
        }

        return result;
    }

    /*
     * http://192.168.169.8:8080/bgbilling/executer? show_sub=0& show_closed=0&
     * contractComment=%E2%EE%EB%EA%EE%E2+%FF%EA%EE%E2& del=0& type=-1&
     * filter=0&
     */
    public void searchContractByTitleComment(Pageable<IdTitle> searchResult, String title, String comment, SearchOptions searchOptions)
            throws BGException {
        if (searchResult != null) {
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

                contractList.add(new IdTitle(Utils.parseInt(rowElement.getAttribute("id")), rowElement.getAttribute(TITLE)));
            }

            NodeList table = dataElement.getElementsByTagName("contracts");
            if (table.getLength() > 0) {
                page.setRecordCount(Utils.parseInt(((Element) table.item(0)).getAttribute("recordCount")));
                page.setPageCount(Utils.parseInt(((Element) table.item(0)).getAttribute("pageCount")));
            }
        }
    }

    // http://192.168.169.8:8080/bgbilling/executer?id=698669&module=contract&value=29.08.2014&action=UpdateContractDate2&cid=698669
    public void updateContractDateTo(int contractId, Date date) throws BGException {
        Request request = new Request();
        request.setAction("UpdateContractDate2");
        request.setModule("contract");
        request.setContractId(contractId);
        request.setAttribute("id", contractId);
        request.setAttribute(VALUE, TimeUtils.format(date, TimeUtils.PATTERN_DDMMYYYY));
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
     * @throws BGException
     */
    public void searchContractByObjectAddressParam(Pageable<ParameterSearchedObject<Contract>> result, SearchOptions options,
            Set<Integer> paramIds, int streetId, String house, String flat, String room) throws BGException {
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
            req.setAttribute(PARAMETERS, Utils.toString(paramIds));
        }

        setPage(req, page);

        Document doc = transferData.postData(req, user);

        Element contracts = XMLUtils.selectElement(doc, "/data/contracts");
        if (contracts != null) {
            getPage(page, contracts);

            List<ParameterSearchedObject<Contract>> list = result.getList();
            for (Element item : XMLUtils.selectElements(contracts, "item")) {
                final String fullTitle = item.getAttribute(TITLE);

                Contract contract = new Contract(dbInfo.getId(), Utils.parseInt(item.getAttribute("id")),
                        StringUtils.substringBefore(fullTitle, "[").trim(), StringUtils.substringBetween(fullTitle, "[", "]").trim());
                list.add(new ParameterSearchedObject<>(contract, 0, StringUtils.substringAfterLast(fullTitle, "]").trim()));
            }
        }
    }

    public void searchContractByAddressParam(Pageable<ParameterSearchedObject<Contract>> result, SearchOptions options, Set<Integer> paramIds,
            int streetId, int houseId, String house, String flat, String room) throws BGException {
        final Page page = result.getPage();

        Request req = new Request();
        req.setPage(page);
        req.setModule("contract");

        applySearchOptions(options, req);

        req.setAction("FindContract");
        if (dbInfo.versionCompare("7.0") >= 0)
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
            req.setAttribute(PARAMETERS, Utils.toString(paramIds));
        }
        setPage(req, page);

        Document doc = transferData.postData(req, user);

        Element contracts = XMLUtils.selectElement(doc, "/data/contracts");
        if (contracts != null) {
            getPage(page, contracts);

            List<ParameterSearchedObject<Contract>> list = result.getList();
            for (Element item : XMLUtils.selectElements(contracts, "item")) {
                final String fullTitle = item.getAttribute(TITLE);

                Contract contract = new Contract(dbInfo.getId(), Utils.parseInt(item.getAttribute("id")),
                        StringUtils.substringBefore(fullTitle, "[").trim(), StringUtils.substringBetween(fullTitle, "[", "]").trim());
                list.add(new ParameterSearchedObject<>(contract, 0, StringUtils.substringAfterLast(fullTitle, "]").trim()));
            }
        }
    }

    /*
     * http://192.168.169.8:8080/bgbilling/executer?show_sub=0&module=contract&
     * parameter
     * =%40&pageSize=100&action=FindContract&del=0&parameters=23&type=1&
     * BGBillingSecret=2CUk87dLwIEAaXIpxarVhEzU&pageIndex=1& [ length = 7713 ]
     * xml = <?xml version="1.0" encoding="windows-1251"?> <data
     * secret="E6195D46C93C248248921198BC88C46E" status="ok"><contracts
     * allRecord="22337" pageCount="224" pageIndex="1" pageSize="100"
     * recordCount="22337"><item id="686643" title=
     * "273RK2801-14 [ ООО &quot;РУСИНВЕСТКРЕДИТ&quot; ]"/><item id="686658"
     * title="273SA0122-14 [ ООО &quot;РУСИНВЕСТКРЕДИТ&quot; ]"/><item
     * id="544063" title="7200173701 [ Сулимов Владислав Сагирович ]"/><item
     * id="549533" title="7200761901 [ Баев Валерий Радикович ]"/><item
     * id="553390" title="7201171603 [ Соломко Галина Николаевна ]"/><item
     * id="554417" title="7201279001 [ Терехович Антон Викторович ]"/><item
     * id="554524" title=
     * "7201294301 [ Нугуманов Руслан Мухарамович | Нугуманов Руслан Мухарямович ]"
     * /><item id="555339" title="7201377401 [ Кайбышева Лейла Азатовна ]"
     * /><item id="559172" title="7201782101 [ Архипов Сергей Владимирович ]"
     * /><item id="559380" title="7201802401 [ Глухова Ольга Владимировна ]"
     * /><item id="561451" title="7202019901 [ Миннибаева Рита Кимовна ]"/><item
     * id="567865" title="7202730301 [ Афанасьева Валентина Валентиновна ]"
     * /><item id="569294" title="7202876001 [ Галин Даян Ирекович ]"/><item
     * id="570613" title="7203018401 [ Аркадьева Юлия Сергеевна ]"/><item
     * id="571187" title="7203081701 [ Зотов Максим Ильич ]"/><item id="604741"
     * title="7206980903 [ Ахтямова Кира Николаевна ]"/><item id="654742" title=
     * "7212654601 [ Жук Павел Иванович ]"/> <item id="660583" title=
     * "7213313003 [ Котельников Дмитрий Леонидович ]"/><item id="674046"
     * title="7214493603...
     */
    public void searchContractByTextParam(Pageable<Contract> result, SearchOptions options, Set<Integer> paramIds, String value)
            throws BGException {
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
        req.setAttribute(PARAMETERS, Utils.toString(paramIds));
        req.setAttribute("parameter", value);

        addSearchResult(result, page, req);
    }

    public void searchContractByPhoneParam(Pageable<Contract> result, SearchOptions options, Set<Integer> paramIds, String phone)
            throws BGException {
        final Page page = result.getPage();

        Request req = new Request();
        req.setPage(page);
        req.setModule("contract");

        applySearchOptions(options, req);

        req.setAction("FindContract");
        if (dbInfo.versionCompare("7.0") >= 0)
            req.setAttribute("type", "c9");
        else
            req.setAttribute("type", 9);
        req.setAttribute(PARAMETERS, Utils.toString(paramIds));
        req.setAttribute("phone", phone);

        addSearchResult(result, page, req);
    }

    public void searchContractByDateParam(Pageable<Contract> result, SearchOptions options, Set<Integer> paramIds, Date dateFrom, Date dateTo)
            throws BGException {
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
        req.setAttribute(PARAMETERS, Utils.toString(paramIds));
        req.setAttribute(DATE_1, TimeUtils.format(dateFrom, TimeUtils.PATTERN_DDMMYYYY));
        req.setAttribute(DATE_2, TimeUtils.format(dateTo, TimeUtils.PATTERN_DDMMYYYY));

        addSearchResult(result, page, req);
    }

    /*
     * http://192.168.169.8:8080/bgbilling/executer?show_sub=0&mail=%40&module=
     * contract &pageSize=100&action=FindContract&del=0&parameters=82&type=3&
     * BGBillingSecret =IHxo1p6Yl7rcVZpdgJfJXiJ6&pageIndex=1& [ length = 7430 ]
     * xml = <?xml version="1.0" encoding="windows-1251"?> <data
     * secret="B19AA3C4B83A67C5FFD10928213C0010" status="ok"><contracts
     * allRecord="19966" pageCount="200" pageIndex="1" pageSize="100"
     * recordCount="19966"> <item id="686643" title=
     * "273RK2801-14 [ ООО &quot;РУСИНВЕСТКРЕДИТ&quot; ]"/><item id="686658"
     * title="273SA0122-14 [ ООО &quot;РУСИНВЕСТКРЕДИТ&quot; ]"/><item
     * id="549533" title="7200761901 [ Баев Валерий Радикович ]"/><item
     * id="553390" title="7201171603 [ Соломко Галина Николаевна ]"/><item
     * id="554417" title="7201279001 [ Терехович Антон Викторович ]"/><item
     * id="555339" title="7201377401 [ Кайбышева Лейла Азатовна ]"/><item
     * id="561451" title="7202019901 [ Миннибаева Рита Кимовна ]"/><item
     * id="567865" title="7202730301 [ Афанасьева Валентина Валентиновна ]"
     * /><item id="569294" title="7202876001 [ Галин Даян Ирекович ]"/><item
     * id="570613" title="7203018401 [ Аркадьева Юлия Сергеевна ]"/><item
     * id="571187" title="7203081701 [ Зотов Максим Ильич ]"/><item id="585610"
     * title="7204666703 [ Арсланбекова Алсу Маратовна ]"/><item id="677643"
     * title="7215020101 [ Яковлев Виталий Викторович ]"/><item id="159" title=
     * "A6152-01 [ Потнин Константин Петрович (kpotnin) ]"/><item id="26" title=
     * "A6219-01 [ Нугуманов_Рауф_Самигуллович (raviln) ]"/><item id="464"
     * title="A6272-02 [ Файзуллин Т.А. (fta) ]"/><item id="1029" title=
     * "A6411-03 [ Байдин Олег  Анатольевич ]"/><item id="1176" title=
     * "A6442-03 [ Семьян Александр Прокопьевич ]"/> <item id="5718" title=
     * "AA0023-05 [ Сахибгареева Ирина Фанилевна ]"/><item...
     */
    public void searchContractByEmailParam(Pageable<Contract> result, SearchOptions options, Set<Integer> paramIds, String email)
            throws BGException {
        final Page page = result.getPage();

        Request req = new Request();
        req.setPage(page);
        req.setModule("contract");

        applySearchOptions(options, req);

        req.setAction("FindContract");
        req.setAttribute("type", 3);
        req.setAttribute(PARAMETERS, Utils.toString(paramIds));
        req.setAttribute("mail", email);

        addSearchResult(result, page, req);
    }

    public void addSearchResult(Pageable<Contract> result, final Page page, Request req) throws BGException {
        setPage(req, page);

        Document doc = transferData.postData(req, user);
        //XMLUtils.serialize(doc, System.out, "utf-8");
        Element contracts = XMLUtils.selectElement(doc, "/data/contracts");
        if (contracts != null) {
            getPage(page, contracts);

            List<Contract> list = result.getList();
            for (Element item : XMLUtils.selectElements(contracts, "item")) {
                final String fullTitle = item.getAttribute(TITLE);

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

    public Contract createContract(int patternId, String date, String title, String titlePattern) throws BGMessageException {
        return createContract(patternId, date, title, titlePattern, 0);
    }

    public Contract createContract(int patternId, String date, String title, String titlePattern, int superId) throws BGMessageException {
        if (dbInfo.getCustomerIdParam() <= 0) {
            throw new BGMessageExceptionTransparent("Не указан параметр customerIdParam для сервера биллинга.");
        }

        Request req = new Request();
        req.setModule("contract");
        req.setAction("NewContract");
        req.setAttribute("pattern_id", patternId);
        req.setAttribute("date", date);
        if (Utils.notBlankString(titlePattern)) {
            req.setAttribute("custom_title", titlePattern);
        }
        if (Utils.notBlankString(title)) {
            req.setAttribute(TITLE, title);
        }

        if (superId > 0) {
            req.setAttribute("super_id", superId);
        }

        Document result = transferData.postData(req, user);

        int contractId = Utils.parseInt(XMLUtils.selectText(result, "/data/contract/@id"));
        String contractTitle = Utils.maskNull(XMLUtils.selectText(result, "/data/contract/@title"));

        Contract contract = new Contract();
        contract.setId(contractId);
        contract.setTitle(contractTitle);
        contract.setBillingId(this.dbInfo.getId());

        return contract;
    }

  /*  *//**
     * Возвращает большиство актуальных данных о договоре.
     * @param contractId
     * @return
     * @throws BGException
     *//*
    public ContractInfo getContractInfo(int contractId) throws BGException {
        return contractInfoDAO.getContractInfo(contractId);
    }*/




    public List<ContractMemo> getMemoList(int contractId) throws BGException {
        Request request = new Request();
        request.setModule("contract");
        request.setAction("ContractMemo");
        request.setContractId(contractId);

        Document document = transferData.postData(request, user);

        List<ContractMemo> contractMemos = new ArrayList<>();
        Element dataElement = document.getDocumentElement();
        NodeList nodeList = dataElement.getElementsByTagName("row");

        for (int index = 0; index < nodeList.getLength(); index++) {
            Element rowElement = (Element) nodeList.item(index);
            ContractMemo memo = new ContractMemo();
            memo.setId(Utils.parseInt(rowElement.getAttribute("f0")));
            memo.setTitle(rowElement.getAttribute("f1"));
            memo.setTime(TimeUtils.parse(rowElement.getAttribute("f3"), TimeUtils.PATTERN_DDMMYYYYHHMMSS));
            memo.setUser(rowElement.getAttribute("f4"));
            memo.setText(getMemo(contractId, memo.getId()).getText());

            contractMemos.add(memo);
        }

        return contractMemos;
    }

    public ContractMemo getMemo(int contractId, int memoId) throws BGException {
        ContractMemo memo = null;

        Request request = new Request();
        request.setModule("contract");
        request.setAction("GetContractMemo");
        request.setContractId(contractId);
        request.setAttribute("id", memoId);

        Document doc = transferData.postData(request, user);

        Element commentEl = XMLUtils.selectElement(doc, "/data/comment");
        if (commentEl != null) {
            memo = new ContractMemo();

            memo.setTitle(commentEl.getAttribute("subject"));
            memo.setText(linesToString(commentEl));
            memo.setVisibleForUser(Utils.parseBoolean(commentEl.getAttribute("visibled")));
        }

        return memo;
    }

    public void updateMemo(int contractId, int memoId, String memoTitle, String memoText, boolean visible) throws BGException {
        Request request = new Request();
        request.setModule("contract");
        request.setAction("UpdateContractMemo");
        request.setContractId(contractId);
        request.setAttribute("subject", memoTitle);
        request.setAttribute(COMMENT, memoText);
        request.setAttribute("visibled", visible);
        if (memoId == 0) {
            request.setAttribute("id", "new");
        } else {
            request.setAttribute("id", memoId);
        }

        transferData.postData(request, user);
    }

    public void updateMemo(int contractId, int memoId, String memoTitle, String memoText) throws BGException {
        Request request = new Request();
        request.setModule("contract");
        request.setAction("UpdateContractMemo");
        request.setContractId(contractId);
        request.setAttribute("subject", memoTitle);
        request.setAttribute(COMMENT, memoText);
        request.setAttribute("visibled", false);
        if (memoId == 0) {
            request.setAttribute("id", "new");
        } else {
            request.setAttribute("id", memoId);
        }

        transferData.postData(request, user);
    }

    public void deleteMemo(int contractId, int memoId) throws BGException {
        Request request = new Request();
        request.setModule("contract");
        request.setAction("DeleteContractMemo");
        request.setContractId(contractId);
        request.setAttribute("id", memoId);

        transferData.postData(request, user);
    }

    public void faceLog(Pageable<ContractFace> result, int contractId) throws BGException {
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
            face.setFace(el.getAttribute(VALUE));

            list.add(face);
        }

        getPage(result.getPage(), XMLUtils.selectElement(document, "/data/table"));
    }

    public void updateFace(int contractId, int face) throws BGException {
        Request req = new Request();
        req.setModule("contract");
        req.setAction("SetFcContract");
        req.setContractId(contractId);
        req.setAttribute(VALUE, face);

        transferData.postData(req, user);
    }

    public void modeLog(Pageable<ContractMode> result, int contractId) throws BGException {
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
            face.setMode(el.getAttribute(VALUE));

            list.add(face);
        }

        getPage(result.getPage(), XMLUtils.selectElement(document, "/data/table"));
    }

    public void updateMode(int contractId, int mode) throws BGException {
        Request req = new Request();
        req.setModule("contract");
        req.setAction("UpdateContractMode");
        req.setContractId(contractId);
        req.setAttribute(VALUE, mode == ContractMode.MODE_CREDIT ? "credit" : "debet");

        transferData.postData(req, user);
    }


    public List<ContractGroup> getContractLabelTreeItemList(int contractId) throws BGException {
        List<ContractGroup> groups = new ArrayList<>();
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.label", "ContractLabelService",
                    "getContractLabelTreeItemList");
            req.setParam("contractId", contractId);
            req.setParam("calcCount", false);
            JsonNode ret = transferData.postDataReturn(req, user);
            groups = readJsonValue(ret.traverse(), jsonTypeFactory.constructCollectionType(List.class, ContractGroup.class));
        }
        return groups;
    }

    public Pair<List<IdTitle>, Set<Integer>> groupsGet(int contractId) throws BGException {
        List<IdTitle> groupList = new ArrayList<>();
        Set<Integer> selectedIds = new HashSet<>();
        if (dbInfo.versionCompare("9.2") >= 0) {
            List<ContractGroup> groups = getContractLabelTreeItemList(contractId);
            RequestJsonRpc req = new RequestJsonRpc("ru.bitel.bgbilling.kernel.contract.label", "ContractLabelService",
                    "getContractLabelIds");
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
                group.setTitle(rowElement.getAttribute(TITLE));

                groupList.add(group);

                if ((selected & (1L << group.getId())) > 0) {
                    selectedIds.add(group.getId());
                }
            }
        }

        return new Pair<>(groupList, selectedIds);
    }

    public void updateGroup(String command, int contractId, int groupId) throws BGException {
        if (dbInfo.versionCompare("5.2") < 0) {
            Request request = new Request();
            request.setModule("contract");
            request.setContractId(contractId);
            request.setAttribute(VALUE, groupId);
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

    public List<IdTitle> additionalActionList(int contractId) throws BGException {
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
            additionalAction.setTitle(rowElement.getAttribute(TITLE));

            additionalActionList.add(additionalAction);
        }

        return additionalActionList;
    }

    public String executeAdditionalAction(int contractId, int actionId) throws BGException {
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

    public Pair<List<IdTitle>, List<IdTitle>> moduleList(int contractId) throws BGException {
        List<IdTitle> selectedList = new ArrayList<>();
        List<IdTitle> availableList = new ArrayList<>();

        Request request = new Request();
        request.setModule("contract");
        request.setAction("ContractModuleList");
        request.setContractId(contractId);

        Document document = transferData.postData(request, user);
        for (Element item : XMLUtils.selectElements(document, "/data/list_select/item")) {
            selectedList.add(new IdTitle(Utils.parseInt(item.getAttribute("id")), item.getAttribute(TITLE)));
        }
        for (Element item : XMLUtils.selectElements(document, "/data/list_avaliable/item")) {
            availableList.add(new IdTitle(Utils.parseInt(item.getAttribute("id")), item.getAttribute(TITLE)));
        }

        return new Pair<>(selectedList, availableList);
    }

    public void updateModule(int contractId, int moduleId, String command) throws BGMessageException {
        Request request = new Request();
        request.setModule("contract");
        request.setContractId(contractId);
        request.setAttribute("module_id", moduleId);

        if ("add".equals(command)) {
            request.setAction("ContractModuleAdd");
        } else if ("del".equals(command)) {
            request.setAction("ContractModuleDelete");
        } else {
            throw new BGMessageExceptionTransparent("Неверный параметр command");
        }

        transferData.postData(request, user);
    }

    public BigDecimal limit(int contractId, Pageable<LimitLogItem> log, List<LimitChangeTask> taskList) throws BGException {
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
                    logItem.setComment(item.getAttribute(COMMENT));
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

    public void updateLimit(int contractId, BigDecimal limit, int days, String comment) throws BGException {
        if (days > 0) {
            if (dbInfo.versionCompare("6.2") >= 0) {
                RequestJsonRpc req = new RequestJsonRpc(KERNEL_CONTRACT_LIMIT, "ContractLimitService",
                        "updateContractLimitPeriod");
                req.setParamContractId(contractId);
                req.setParam("limit", limit);
                req.setParam("period", days);
                req.setParam(COMMENT, comment);
                transferData.postData(req, user);
            } else {
                Request request = new Request();
                request.setModule("contract");
                request.setContractId(contractId);
                request.setAttribute(COMMENT, comment);
                request.setAction("UpdateContractLimitPeriod");
                request.setAttribute("limit", Utils.format(limit));
                request.setAttribute("period", days);
                transferData.postData(request, user);
            }
        } else {
            Request request = new Request();
            request.setModule("contract");
            request.setContractId(contractId);
            request.setAttribute(COMMENT, comment);
            request.setAction("UpdateContractLimit");
            request.setAttribute(VALUE, Utils.format(limit));
            transferData.postData(request, user);
        }
    }

    public void deleteLimitTask(int contractId, int id) throws BGException {
        Request request = new Request();
        request.setModule("contract");
        request.setAction("LimitChangeTaskDelete");
        request.setContractId(contractId);
        request.setAttribute("id", id);

        transferData.postData(request, user);
    }

    public String getContractStatisticPassword(int contractId) throws BGException {
        Document contractCard = new ContractDAO(this.user, this.dbInfo).getContractCardDoc(contractId);
        return XMLUtils.selectText(contractCard, "/data/contract/@pswd");
    }

    public List<IdTitle> getContractAddress(int contractId) throws BGException {
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
            address.setTitle(rowElement.getAttribute(TITLE));

            contractAddress.add(address);
        }

        return contractAddress;
    }

    public void updateContractPassword(int contractId, String value, boolean generate) throws BGException {
        Request request = new Request();
        request.setModule("contract");
        request.setAction("UpdateContractPassword");
        request.setContractId(contractId);
        request.setAttribute(VALUE, Utils.maskNull(value));

        if (generate) {
            request.setAttribute("set_pswd", 1);
        }

        transferData.postData(request, user);
    }

    public String getContractFullCard(int contractId) throws BGException {
        Request req = new Request();

        req.setModule("contract");
        req.setAction("ContractCardXml");
        req.setAttribute("contentType", "html");
        req.setContractId(contractId);

        return transferData.postDataGetString(req, user);
    }

    public Document getContractCardDoc(int contractId) throws BGException {
        Request req = new Request();

        req.setModule("contract");
        req.setAction("ContractCardXml");
        req.setAttribute("contentType", "xml");
        req.setContractId(contractId);

        return transferData.postData(req, user);
    }

    public List<String[]> getContractCardTypes(int contractId) throws BGException {
        Request req = new Request();

        req.setModule("contract");
        req.setAction("ContractCard2ListTypes");
        req.setContractId(contractId);

        List<String[]> result = new ArrayList<>(3);

        Document doc = transferData.postData(req, user);
        for (Element el : XMLUtils.selectElements(doc, "/data/combo/el")) {
            result.add(new String[] { el.getAttribute("id"), el.getAttribute(TITLE) });
        }

        return result;
    }

    public byte[] getContractCard2Pdf(int contractId, String type) throws BGException {
        Request req = new Request();

        req.setModule("contract");
        req.setAction("ContractCard2");
        req.setAttribute("contentType", "application/pdf");
        req.setContractId(contractId);
        req.setAttribute("type", type);

        return transferData.postDataGetBytes(req, user);
    }

    public void bgbillingOpenContract(int contractId) throws BGException {
        Request req = new Request();

        req.setModule("admin");
        req.setAction("Command");
        req.setAttribute("command", "put");
        req.setAttribute(VALUE, "openContract:" + contractId);

        transferData.postData(req, user);
    }

    public void bgbillingUpdateContractTitleAndComment(int contractId, String comment, int patid) throws BGException {
        Request req = new Request();

        req.setModule("contract");
        req.setAction("UpdateContractTitleAndComment");
        req.setContractId(contractId);
        if (patid > 0) {
            req.setAttribute("patid", patid);
        }
        req.setAttribute(COMMENT, comment);

        transferData.postData(req, user);
    }

    /*
     * public List<IdTitle> getAdditionalActionList( int contractId ) throws
     * BGException { Request req = new Request(); req.setModule(
     * CONTRACT_MODULE_ID ); req.setAction( "AdditionalActionList" );
     * req.setContractId( contractId );
     *
     * Document doc = transferData.postData( req, user );
     *
     * List<IdTitle> actions = new ArrayList<IdTitle>(); for( Element e :
     * XMLUtils.selectElements( doc, "/data/list/item" ) ) { actions.add( new
     * IdTitle( Utils.parseInt( e.getAttribute( "id" ) ), e.getAttribute(
     * "title" ) ) ); } return actions; }
     */
    public List<IdTitle> bgbillingGetContractPatternList() throws BGException {
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
            pattern.setTitle(rowElement.getAttribute(TITLE));

            contractPatterns.add(pattern);
        }

        return contractPatterns;
    }

    /**
     * Использовать {@link ContractHierarchyDAO#getSubContracts(int)}.
     */
    @Deprecated
    public List<Integer> getSubContracts(int contractId) throws BGException {
        return new ContractHierarchyDAO(user, dbInfo).getSubContracts(contractId);
    }

    /**
     * Использовать {@link ContractHierarchyDAO#addSubcontract(int, int)}.
     */
    @Deprecated
    public void addSubcontract(int superContractId, int subContractId) throws BGException {
        new ContractHierarchyDAO(user, dbInfo).addSubcontract(superContractId, subContractId);
    }

    @Deprecated
    public List<IdTitle> getAdditionalActionList(int contractId) throws BGException {
        return additionalActionList(contractId);
    }

    public List<IdTitle> getStreetsByCity(int cityId) throws BGException {
        Request request = new Request();
        request.setModule("contract");
        request.setAction("GetStreetsByCity");

        request.setAttribute("city", cityId);

        Document document = transferData.postData(request, user);

        List<IdTitle> streets = new LinkedList<>();

        for (Element item : XMLUtils.selectElements(document, "/data/streets/item")) {
            streets.add(new IdTitle(Utils.parseInt(item.getAttribute("id")), item.getAttribute(TITLE)));
        }

        return streets;
    }

    public OpenContract openContract() throws BGException {
        Request request = new Request();
        request.setModule("contract");
        request.setAction("OpenContract");

        Document document = transferData.postData(request, user);
        return new OpenContract(document);
    }

    /**
     * Определение текущего режима управления лимитом по договору из страницы
     * статистики
     *
     * @return 0 - управление разблокировано, 1 - управление заблокировано
     * @throws BGException
     */
    public int getContractLimitManage(int contractId) throws BGException {
        Request billingRequest = new Request();
        billingRequest.setModule("contract");
        billingRequest.setAction("ContractLimitManage");
        billingRequest.setContractId(contractId);

        Document document = transferData.postData(billingRequest, user);
        Element rowElement = (Element) document.getElementsByTagName("table").item(0);

        return Utils.parseInt(rowElement.getAttribute("mode"));
    }

    /**
     * Изменение текущего режима управления лимитом по договору из страницы
     * статистики.
     *
     * @param mode
     *            0 - управление разблокировано, 1 - управление заблокировано
     * @throws BGException
     */
    public void updateContractLimitManage(int contractId, int mode) throws BGException {
        Request billingRequest = new Request();
        billingRequest.setModule("contract");
        billingRequest.setAction("UpdateContractLimitManage");
        billingRequest.setContractId(contractId);
        billingRequest.setAttribute(VALUE, mode);

        transferData.postData(billingRequest, user);
    }

    public enum WebContractLogonLogType {
        OK("ok"), ERROR("error");

        private final String type;

        WebContractLogonLogType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return this.type;
        }
    }

    public static class WebContractLogonLogEntry {
        private Date date;
        private String ip;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public WebContractLogonLogEntry(Element element) throws BGException {
            String dt = element.getAttribute("date");

            if (dt == null || dt.length() == 0) {
                throw new BGException("Не определено значение аттрибута элемента date");
            }

            try {
                setDate(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(dt));
            } catch (ParseException e) {
                throw new BGException("Не удалось разобрать значение даты: \"" + dt + "\" для формата \"dd.MM.yyyy HH:mm:ss\"");
            }

            setIp(element.getAttribute("ip"));

            if (getIp() == null || getIp().length() == 0) {
                throw new BGException("Не определено значение аттрибута элемента ip");
            }
        }
    }

    public static class WebContractLogonLogSuccess extends WebContractLogonLogEntry {
        private String passwordType;
        private String sessionId;

        public String getPasswordType() {
            return passwordType;
        }

        public void setPasswordType(String passwordType) {
            this.passwordType = passwordType;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public WebContractLogonLogSuccess(Element element) throws BGException {
            super(element);

            setPasswordType(element.getAttribute("passwordType"));

            if (getPasswordType() == null || getPasswordType().length() == 0) {
                throw new BGException("Не определено значение аттрибута элемента passwordType");
            }

            setSessionId(element.getAttribute("sessionId"));

            if (getSessionId() == null || getSessionId().length() == 0) {
                throw new BGException("Не определено значение аттрибута элемента sessionId");
            }
        }
    }

    public static class WebContractLogonLogError extends WebContractLogonLogEntry {
        private String login;
        private String errorCode;

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public WebContractLogonLogError(Element element) throws BGException {
            super(element);

            setLogin(element.getAttribute("login"));

            if (getLogin() == null || getLogin().length() == 0) {
                throw new BGException("Не определено значение аттрибута элемента login");
            }

            setErrorCode(element.getAttribute("errorCode"));

            if (getErrorCode() == null || getErrorCode().length() == 0) {
                throw new BGException("Не определено значение аттрибута элемента errorCode");
            }
        }
    }

    public void getWebContractLogonLog(Pageable<WebContractLogonLogEntry> searchResult, int contractId, WebContractLogonLogType type, Date date1,
            Date date2) throws BGException {
        Request request = new Request();
        request.setModule("contract");
        request.setAction("WebContractLogonLog");
        request.setContractId(contractId);
        request.setAttribute("logType", type.toString());

        if (date1 != null) {
            request.setAttribute(DATE_1, new SimpleDateFormat("dd.MM.yyyy").format(date1));
        }
        if (date2 != null) {
            request.setAttribute(DATE_2, new SimpleDateFormat("dd.MM.yyyy").format(date2));
        }

        request.setAttribute("pageSize", searchResult.getPage().getPageSize());
        request.setAttribute("pageIndex", searchResult.getPage().getPageIndex());

        Document document = transferData.postData(request, user);

        for (Element row : XMLUtils.selectElements(document, "/data/table/data/row")) {
            switch (type) {
            case OK: {
                searchResult.getList().add(new WebContractLogonLogSuccess(row));
                break;
            }
            case ERROR: {
                searchResult.getList().add(new WebContractLogonLogError(row));
                break;
            }
            default: {
                throw new BGException("Указан неизвестный тип логов: " + type.toString());
            }
            }
        }
    }

    public static class WebLimit {
        private int configLimit = -1;
        private int count = -1;
        private int limit = -1;
        private int status = -1;

        public int getConfigLimit() {
            return configLimit;
        }

        public void setConfigLimit(int configLimit) {
            this.configLimit = configLimit;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public WebLimit(Element element) {
            if (element == null) {
                return;
            }
            setConfigLimit(Utils.parseInt(element.getAttribute("configLimit")));
            setCount(Utils.parseInt(element.getAttribute("count")));
            setLimit(Utils.parseInt(element.getAttribute("limit")));
            setStatus(Utils.parseInt(element.getAttribute("status")));
        }
    }

    public static class WebRequestLastLogon {
        private int counter = 0;
        private Date datetime = null;
        private String ip = "";

        public int getCounter() {
            return counter;
        }

        public void setCounter(int counter) {
            this.counter = counter;
        }

        public Date getDatetime() {
            return datetime;
        }

        public void setDatetime(Date datetime) {
            this.datetime = datetime;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public WebRequestLastLogon(Element element) throws BGException {
            if (element == null) {
                return;
            }
            setCounter(Utils.parseInt(element.getAttribute("counter")));
            try {
                setDatetime(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(element.getAttribute("datetime")));
            } catch (ParseException e) {
                throw new BGException(e);
            }
            setIp(element.getAttribute("ip"));
        }
    }

    public static class WebRequestLimit {
        private WebLimit webLimit;
        private WebRequestLastLogon lastLogon;

        public WebLimit getWebLimit() {
            return webLimit;
        }

        public void setWebLimit(WebLimit webLimit) {
            this.webLimit = webLimit;
        }

        public WebRequestLastLogon getLastLogon() {
            return lastLogon;
        }

        public void setLastLogon(WebRequestLastLogon lastLogon) {
            this.lastLogon = lastLogon;
        }

        public WebRequestLimit(Document document) throws BGException {
            if (document == null) {
                setWebLimit(new WebLimit(null));
                setLastLogon(new WebRequestLastLogon(null));
            } else {
                setWebLimit(new WebLimit(XMLUtils.selectElement(document, "/data/web_limit")));
                setLastLogon(new WebRequestLastLogon(XMLUtils.selectElement(document, "/data/lastLogon")));
            }
        }
    }

    public WebRequestLimit getWebRequestLimit(int contractId) throws BGException {

        Request request = new Request();
        request.setModule("contract");
        request.setAction("WebRequestLimit");
        request.setContractId(contractId);
        Document doc = null;
        try {
            doc = transferData.postData(request, user);
        } catch (BGException e) {
            log.debug(e);//может быть отказ по правам, ошибка ли это?
        }
        return new WebRequestLimit(doc);
    }

    public enum WebRequestLimitMode {
        COMMON(1), DISABLED(2), PERSONAL(3);

        private final int mode;

        WebRequestLimitMode(int mode) {
            this.mode = mode;
        }

        public int getMode() {
            return this.mode;
        }
    }

    public WebRequestLimit updateWebRequestLimit(int contractId, WebRequestLimitMode mode, int limit) throws BGException {
        Request request = new Request();
        request.setModule("contract");
        request.setAction("WebRequestLimit");
        request.setContractId(contractId);
        request.setAttribute("mode", mode.getMode());

        if (mode == WebRequestLimitMode.PERSONAL) {
            request.setAttribute("limit", limit);
        }

        return new WebRequestLimit(transferData.postData(request, user));
    }

    public List<IdTitle> getParameterList(int parameterTypeId) throws BGException {
        List<IdTitle> paramList;
        if (dbInfo.versionCompare("7.0") >= 0) {
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
                paramList.add(new IdTitle(Utils.parseInt(el.getAttribute("id")), el.getAttribute(TITLE)));
            }
        }
        return paramList;
    }

    public ContractInfo getContractInfo(int contractId) throws BGException {
        ContractInfo result = null;

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
            result.setComment(contract.getAttribute(COMMENT));
            result.setObjects(Utils.parseInt(contract.getAttribute("objects").split("/")[0]),
                    Utils.parseInt(contract.getAttribute("objects").split("/")[1]));
            result.setHierarchy(contract.getAttribute("hierarchy"));
            result.setHierarchyDep(Utils.parseInt(contract.getAttribute("hierarchyDep")));
            result.setHierarchyIndep(Utils.parseInt(contract.getAttribute("hierarchyIndep")));
            result.setDeleted(Utils.parseBoolean(contract.getAttribute("del")));
            result.setFace(Utils.parseInt(contract.getAttribute("fc")));
            result.setDateFrom(TimeUtils.parse(contract.getAttribute(DATE_1), TimeUtils.PATTERN_DDMMYYYY));
            result.setDateTo(TimeUtils.parse(contract.getAttribute(DATE_2), TimeUtils.PATTERN_DDMMYYYY));
            result.setMode(Utils.parseInt(contract.getAttribute("mode")));
            result.setBalanceLimit(Utils.parseBigDecimal(contract.getAttribute("limit"), BigDecimal.ZERO));
            result.setStatus(contract.getAttribute("status"));
            result.setTitle(contract.getAttribute(TITLE));
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
                    moduleList.add(new ContractInfo.ModuleInfo(Utils.parseInt(item.getAttribute("id")), item.getAttribute(TITLE), item.getAttribute("package"),
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
        return result;

    }

    protected List<IdTitle> getList(JSONObject infoJson, String nodeName) {
        List<IdTitle> result = new ArrayList<>();
        JSONArray nodeJson = infoJson.optJSONArray(nodeName);
        if (nodeJson == null) {
            return result;
        }
        for (int index = 0; index < nodeJson.length(); index++) {
            JSONObject itemJson = nodeJson.optJSONObject(index);
            IdTitle item = new IdTitle(itemJson.optInt("id"), itemJson.optString(TITLE));
            result.add(item);
        }
        return result;
    }

    protected List<IdTitle> getList(Element node) {
        List<IdTitle> result = Collections.emptyList();

        if (node != null) {
            result = new ArrayList<>();
            for (Element item : XMLUtils.selectElements(node, "item")) {
                result.add(new IdTitle(Utils.parseInt(item.getAttribute("id")), item.getAttribute(TITLE)));
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
            throw new BGMessageExceptionTransparent("Ошибка копирования имени контрагента: " + customer.getTitle() + "; "
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
                            request.setAttribute("floor", value.getFloor() == -1 ? "" : value.getFloor());
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
            String billingId = StringUtils.substringAfter(link.getLinkObjectType(), ":");
            ContractDAO.getInstance(user, billingId).copyParametersToBilling(con, customerId, link.getLinkObjectId(),
                    link.getLinkObjectTitle());
        }
    }

}
