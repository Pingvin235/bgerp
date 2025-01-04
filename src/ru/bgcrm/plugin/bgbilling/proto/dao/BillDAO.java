package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bgerp.model.Pageable;
import org.bgerp.model.base.IdTitle;
import org.bgerp.util.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fasterxml.jackson.databind.JsonNode;

import ru.bgcrm.model.Pair;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.proto.model.bill.Attribute;
import ru.bgcrm.plugin.bgbilling.proto.model.bill.AttributeType;
import ru.bgcrm.plugin.bgbilling.proto.model.bill.Bill;
import ru.bgcrm.plugin.bgbilling.proto.model.bill.Invoice;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class BillDAO extends BillingModuleDAO {
    private static final String BILL_MODULE_ID = "bill";
    private static final String BILL_MODULE = "ru.bitel.bgbilling.modules.bill";

    public BillDAO(User user, String billingId, int moduleId) {
        super(user, billingId, moduleId);
    }

    public List<AttributeType> getAttributeTypeList() {
        List<AttributeType> result = new ArrayList<>();
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(BILL_MODULE, moduleId, "BillService", "attributeTypeList");
            JsonNode ret = transferData.postDataReturn(req, user);
            result = readJsonValue(ret.traverse(), jsonTypeFactory.constructCollectionType(List.class, AttributeType.class));

        }
        return result;
    }

    public List<Attribute> getAttributeList(int contractId) {
        List<Attribute> result = new ArrayList<>();
        if (dbInfo.versionCompare("9.2") >= 0) {
            RequestJsonRpc req = new RequestJsonRpc(BILL_MODULE, moduleId, "BillService", "attributeList");
            req.setParam("contractId", contractId);
            JsonNode ret = transferData.postDataReturn(req, user);
            result = readJsonValue(ret.traverse(), jsonTypeFactory.constructCollectionType(List.class, Attribute.class));
            Map<String, String> types = getAttributeTypeList().stream().collect(Collectors.toMap(AttributeType::getName, AttributeType::getTitle));
            result = result.stream().map(attr -> {
                String title = types.get(attr.getTitle());
                if (title != null) {
                    attr.setTitle(title);
                }
                return attr;
            }).collect(Collectors.toList());
        } else {
            Request req = new Request();
            req.setModule(BILL_MODULE_ID);
            req.setAction("Attribute");
            req.setModuleID(moduleId);
            req.setContractId(contractId);

            Document document = transferData.postData(req, user);
            for (Element rowElement : XMLUtils.selectElements(document, "/data/table/data/row")) {
                Attribute item = new Attribute();

                item.setId(Utils.parseInt(rowElement.getAttribute("id")));
                item.setTitle(rowElement.getAttribute("title"));
                TimeUtils.parsePeriod(rowElement.getAttribute("period"), item);
                item.setValue(rowElement.getAttribute("value"));

                result.add(item);
            }
        }

        return result;
    }

    /* http://billing:8081/executer?module=bill&action=DocTypeList&mid=10&type=bill&BGBillingSecret=EYCgQrk02huqe5maTPTTZPdX&cid=1783&
    [ length = 1018 ] xml = <?xml version="1.0" encoding="UTF-8"?><data secret="78E9394018B4F006CF649748BE860984" status="ok"><list_select><item id="5" title="Предоплата"/><item id="19" title="Хостинг"/></list_select><list_avaliable><item id="18" title="Предоплата услуг технической поддержки"/><item id="1" title="Продажа ПО (5.x)"/><item id="33" title="Продажа ПО (6.х)"/><item id="32" title="Продажа ПО (экспорт $)"/><item id="25" title="Продажа ПО (экспорт)"/><item id="24" title="Продление домена (COM)"/><item id="35" title="Продление домена (PRO)"/><item id="20" title="Продление домена (RU)"/><item id="26" title="Продление домена (SU)"/><item id="30" title="Продление домена (РФ)"/><item id="31" title="Работы по настройке программного продукта (модули DialUp, IPN, Bill, Npay)"/><item id="27" title="Регистрация домена (COM)"/><item id="21" title="Регистрация домена (RU)"/><item id="29" title="Регистрация домена (SU)"/><item id="28" title="Регистрация домена (РФ)"/><item id="3" title="Техническая поддержка"/></list_avaliable></data>
     */
    public Pair<List<IdTitle>, List<IdTitle>> getContractDocTypeList(int contractId, String type) {
        List<IdTitle> listSelected = new ArrayList<>();
        List<IdTitle> listAvailable = new ArrayList<>();

        Request req = new Request();
        req.setModule(BILL_MODULE_ID);
        req.setAction("DocTypeList");
        req.setModuleID(moduleId);
        req.setContractId(contractId);
        req.setAttribute("type", type);

        Document document = transferData.postData(req, user);
        for (Element rowElement : XMLUtils.selectElements(document, "/data/list_select/item")) {
            listSelected.add(new IdTitle(Utils.parseInt(rowElement.getAttribute("id")), rowElement.getAttribute("title")));
        }
        for (Element rowElement : XMLUtils.selectElements(document, "/data/list_avaliable/item")) {
            listAvailable.add(new IdTitle(Utils.parseInt(rowElement.getAttribute("id")), rowElement.getAttribute("title")));
        }

        return new Pair<>(listSelected, listAvailable);
    }

    //http://billing:8081/executer?module=bill&selectedItems=5&action=ContractDocTypeAdd&mid=10&BGBillingSecret=M9xZ2FFsNBkYCjSGO3sRNMCn&cid=1783&
    public void contractDocTypeAdd(int contractId, String typeIds) {
        Request req = new Request();
        req.setModule(BILL_MODULE_ID);
        req.setAction("ContractDocTypeAdd");
        req.setModuleID(moduleId);
        req.setContractId(contractId);
        req.setAttribute("selectedItems", typeIds);

        transferData.postData(req, user);
    }

    //http://billing:8081/executer?module=bill&selectedItems=5&action=ContractDocTypeDelete&mid=10&BGBillingSecret=OKcy3Ss6yHxtoYTv3JofEEHC&cid=1783&
    public void contractDocTypeDelete(int contractId, String typeIds) {
        Request req = new Request();
        req.setModule(BILL_MODULE_ID);
        req.setAction("ContractDocTypeDelete");
        req.setModuleID(moduleId);
        req.setContractId(contractId);
        req.setAttribute("selectedItems", typeIds);

        transferData.postData(req, user);
    }

    /*http://billing:8081/executer?module=bill&pageSize=25&action=ContractBill&mid=10&BGBillingSecret=gwO1TWeDulp54LbGpgvVo153&cid=699&pageIndex=1&
        [ length = 4827 ] xml = <?xml version="1.0" encoding="UTF-8"?><data secret="F81B4C82DE0A4D2142426C6882E21579" status="ok">
    <table pageCount="1" pageIndex="1" pageSize="25" recordCount="19"><data><row create_dt="05.08.2013" id="3403" month="2013.08" number="В-02392" pay_dt="21.08.2013" status="оплачен" summ="121000.00" type="3" type_title="Техническая поддержка" unload_status="не выгружен" who_created="Шамиль Вахитов" who_payed="Кирилл Сергеев"/><row create_dt="10.01.2013" id="2967" month="2013.01" number="В-02208" pay_dt="22.01.2013" status="оплачен" summ="45000.00" type="3" type_title="Техническая поддержка" unload_status="не выгружен" who_created="Шамиль Вахитов" who_payed="Кирилл Сергеев"/><row create_dt="18.10.2012" id="2775" month="2012.10" number="В-02103" pay_dt="29.10.2012" status="оплачен" summ="63000.00" type="3" type_title="Техническая поддержка" unload_status="не выгружен" who_created="Шамиль Вахитов" who_payed="Кирилл Сергеев"/><row create_dt="13.06.2012" id="2456" month="2012.05" number="В-01919" pay_dt="25.06.2012" status="оплачен" summ="126000.00" type="3" type_title="Техническая поддержка" unload_status="не выгружен" who_created="Шамиль Вахитов" who_payed="Кирилл Сергеев"/><row create_dt="11.01.2012" id="2102" month="2012.01" number="В-01743" pay_dt="13.01.2012" status="оплачен" summ="139500.00" type="3" type_title="Техническая поддержка" unload_status="не выгружен" who_created="Шамиль Вахитов" who_payed="Кирилл Сергеев"/><row create_dt="25.10.2011" id="1932" month="2011.10" number="...*/
    public void searchBillList(int contractId, Pageable<Bill> searchResult) {
        Request req = new Request();
        req.setModule(BILL_MODULE_ID);
        req.setAction("ContractBill");
        req.setModuleID(moduleId);
        req.setContractId(contractId);

        setPage(req, searchResult.getPage());

        Document doc = transferData.postData(req, user);
        for (Element row : XMLUtils.selectElements(doc, "/data/table/data/row")) {
            Bill bill = new Bill();
            loadDocumentFields(row, bill);
            bill.setStatusTitle(row.getAttribute("status"));
            bill.setCreateUser(row.getAttribute("who_created"));
            bill.setPayDate(TimeUtils.parse(row.getAttribute("pay_dt"), TimeUtils.FORMAT_TYPE_YMD));
            bill.setPayUser(row.getAttribute("who_payed"));

            searchResult.getList().add(bill);
        }

        getPage(searchResult.getPage(), XMLUtils.selectElement(doc, "/data/table"));
    }

    /*http://billing:8081/executer?module=bill&pageSize=25&action=ContractInvoice&mid=10&BGBillingSecret=w5lWl15lnYUP0F4s7qC2ywU8&cid=699&pageIndex=1&
    [ length = 1575 ] xml = <?xml version="1.0" encoding="UTF-8"?><data secret="28F56F1AD23578C3F6F1ED6A27553FC2" status="ok"><table pageCount="1" pageIndex="1" pageSize="25" recordCount="9"><data><row create_dt="21.08.2013" id="1505" month="2013.08" number="A-00312" show_ready="true" summ="121000.00" type="4" type_title="Акт техническая поддержка"/><row create_dt="22.01.2013" id="1215" month="2013.01" number="A-00038" show_ready="true" summ="45000.00" type="4" type_title="Акт техническая поддержка"/><row create_dt="29.10.2012" id="1054" month="2012.10" number="A-00459" show_ready="true" summ="63000.00" type="4" type_title="Акт техническая поддержка"/><row create_dt="26.06.2012" id="855" month="2012.06" number="A-00275" show_ready="true" summ="126000.00" type="4" type_title="Акт техническая поддержка"/><row create_dt="13.01.2012" id="577" month="2012.01" number="A-00015" show_ready="true" summ="139500.00" type="4" type_title="Акт техническая поддержка"/><row create_dt="26.12.2011" id="548" month="2011.12" number="A-00376" show_ready="true" summ="100900.00" type="4" type_title="Акт техническая поддержка"/><row create_dt="26.12.2011" id="547" month="2011.12" number="A-00375" show_ready="true" summ="94000.00" type="4" type_title="Акт техническая поддержка"/><row create_dt="20.06.2011" id="227" month="2011.06" number="A-00577" show_ready="true" summ="94000.00" type="4" type_title="Акт техническая поддержка"/><row create_dt="11.03.2011" id="107" month="2011.03" number="A-00087" show_ready="true" summ="96000....*/
    public void searchInvoiceList(int contractId, Pageable<Invoice> searchResult) {
        Request req = new Request();
        req.setModule(BILL_MODULE_ID);
        req.setAction("ContractInvoice");
        req.setModuleID(moduleId);
        req.setContractId(contractId);

        setPage(req, searchResult.getPage());

        Document doc = transferData.postData(req, user);
        for (Element row : XMLUtils.selectElements(doc, "/data/table/data/row")) {
            Invoice bill = new Invoice();
            loadDocumentFields(row, bill);
            bill.setShowOnWeb(Utils.parseBoolean(row.getAttribute("show_ready")));

            searchResult.getList().add(bill);
        }

        getPage(searchResult.getPage(), XMLUtils.selectElement(doc, "/data/table"));
    }

    private void loadDocumentFields(Element row, ru.bgcrm.plugin.bgbilling.proto.model.bill.Document bill) {
        bill.setId(Utils.parseInt(row.getAttribute("id")));
        bill.setMonth(row.getAttribute("month"));
        bill.setNumber(row.getAttribute("number"));
        bill.setCreateDate(TimeUtils.parse(row.getAttribute("create_dt"), TimeUtils.FORMAT_TYPE_YMD));
        bill.setTypeTitle(row.getAttribute("type_title"));
        bill.setSumma(Utils.parseBigDecimal(row.getAttribute("summ")));
    }

    // http://billing:8081/executer?module=bill&ids=3857%3A%3B&value=false&action=SetPayed&mid=10&BGBillingSecret=2eu4IGpkplearYz8H4riXytI&
    // http://billing:8081/executer?module=bill&summComment=0.00%3B&ids=3857%3A%3B&value=true&action=SetPayed&mid=10&date=04.05.2014&BGBillingSecret=a2bJiB7xQgwCpp6Tb7S309jw
    public void setPayed(String ids, boolean value, Date date, BigDecimal summa, String comment) {
        Request req = new Request();
        req.setModule(BILL_MODULE_ID);
        req.setAction("SetPayed");
        req.setModuleID(moduleId);
        req.setAttribute("value", value);
        req.setAttribute("ids", ids);
        if (value) {
            req.setAttribute("date", TimeUtils.format(date, TimeUtils.PATTERN_DDMMYYYY));
            req.setAttribute("summComment", Utils.format(summa) + ":" + Utils.maskNull(comment));
        }
        transferData.postData(req, user);
    }

    /*http://billing:8081/executer?module=bill&action=ViewDocs&codes=3403&contentType=application%2Fpdf&mid=10&type=bill&*/
    public byte[] getDocumentsPdf(String ids, String type) {
        Request req = new Request();
        req.setModule(BILL_MODULE_ID);
        req.setAction("ViewDocs");
        req.setModuleID(moduleId);
        req.setAttribute("type", type);
        req.setAttribute("codes", ids);
        req.setAttribute("contentType", "application/pdf");

        return transferData.postDataGetBytes(req, user);
    }
}