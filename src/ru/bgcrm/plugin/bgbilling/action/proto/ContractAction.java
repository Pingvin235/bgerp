package ru.bgcrm.plugin.bgbilling.action.proto;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGIllegalArgumentException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.app.exception.BGMessageExceptionWithoutL10n;
import org.bgerp.model.Pageable;
import org.bgerp.model.base.IdTitle;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import ru.bgcrm.dao.AddressDAO;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.ParamList;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterPhoneValueItem;
import ru.bgcrm.model.param.ParameterSearchedObject;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.Plugin;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO.SearchOptions;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractHierarchyDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractNoteDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractObjectDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractObjectParamDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractParamDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractScriptDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractStatusDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.DirectoryDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractObjectParameter;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractParameter;
import ru.bgcrm.plugin.bgbilling.proto.model.ParamAddressValue;
import ru.bgcrm.plugin.bgbilling.proto.model.ParameterType;
import ru.bgcrm.plugin.bgbilling.proto.model.limit.LimitChangeTask;
import ru.bgcrm.plugin.bgbilling.proto.model.limit.LimitLogItem;
import ru.bgcrm.plugin.bgbilling.proto.model.script.ContractScriptLogItem;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.struts.form.Response;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/bgbilling/proto/contract", pathId = true)
public class ContractAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER;
    private static final String PATH_JSP_CONTRACT = PATH_JSP + "/contract";

    public ActionForward searchContract(DynActionForm form, ConnectionSet conSet) throws Exception {
        String searchBy = form.getParam("searchBy");
        String billingId = form.getParam("billingId");

        String searchBySuffix = form.getParam("searchBySuffix");
        if (Utils.notBlankString(searchBySuffix)) {
            searchBy += searchBySuffix;
        }

        boolean showClosed = form.getParamBoolean("show_closed", false);
        boolean showSub = form.getParamBoolean("show_sub", false);
        boolean showHidden = form.getParamBoolean("show_invisible", false);
        SearchOptions searchOptions = new SearchOptions(showHidden, showClosed, showSub);

        User user = form.getUser();

        if (Utils.notBlankString(searchBy)) {
            ContractDAO contractDAO = new ContractDAO(user, billingId);

            if ("address".equals(searchBy)) {
                Set<Integer> addressParamIds = Utils.toIntegerSet(setup.get(Plugin.ID + ":search.contract.param.address.paramIds"));
                Pageable<ParameterSearchedObject<Contract>> res = new Pageable<>(form);
                contractDAO.searchContractByAddressParam(res, searchOptions, addressParamIds, form.getParamInt("streetId"),
                        form.getParamInt("houseId"), form.getParam("house"), form.getParam("flat"), form.getParam("room"));
            } else if ("addressObject".equals(searchBy)) {
                Pageable<ParameterSearchedObject<Contract>> result = new Pageable<>(form);
                contractDAO.searchContractByObjectAddressParam(result, searchOptions, null, form.getParamInt("streetId"), form.getParam("house"),
                        form.getParam("flat"), form.getParam("room"));
            } else if ("id".equals(searchBy)) {
                Pageable<IdTitle> result = new Pageable<>(form);
                Contract contract = contractDAO.getContractById(form.getParamInt("id"));
                if (contract != null) {
                    result.getList().add(new IdTitle(contract.getId(), contract.getTitle()));
                }
            } else if ("title".equals(searchBy) || "comment".equals(searchBy)) {
                Pageable<IdTitle> result = new Pageable<>(form);
                contractDAO.searchContractByTitleComment(result, form.getParam("title"), form.getParam("comment"), searchOptions);
            } else if (searchBy.equals("parameter_text")) {
                Pageable<Contract> result = new Pageable<>(form);
                contractDAO.searchContractByTextParam(result, searchOptions, getParamIds(form), form.getParam("value"));
            } else if (searchBy.equals("parameter_date")) {
                Pageable<Contract> result = new Pageable<>(form);
                contractDAO.searchContractByDateParam(result, searchOptions, getParamIds(form), form.getParamDate("date_from"),
                        form.getParamDate("date_to"));
            } else if (searchBy.equals("parameter_phone")) {
                Pageable<Contract> result = new Pageable<>(form);
                contractDAO.searchContractByPhoneParam(result, searchOptions, getParamIds(form), form.getParam("value"));
            }
        }

        return html(conSet, form, PATH_JSP + "/search_contract_result.jsp");
    }

    private Set<Integer> getParamIds(DynActionForm form) throws BGMessageException {
        String[] vals = form.getParam().getArray("paramIds");
        if (vals == null) {
            return getParams(form).stream()
                    .map(i -> i.getId())
                    .collect(Collectors.toSet());
        }
        return Arrays.stream(vals).map(i -> Utils.parseInt(i, -1)).collect(Collectors.toSet());
    }

    public ActionForward getParamList(DynActionForm form, ConnectionSet conSet) throws BGMessageException {
        form.setResponseData("paramType", form.getParamInt("paramType"));
        List<IdTitle> list = getParams(form);
        form.setResponseData("paramList", list);
        return html(conSet, form, PATH_JSP + "/search_param_list.jsp");
    }

    private List<IdTitle> getParams(DynActionForm form) throws BGMessageException {
        int type = form.getParamInt("paramType");
        String billingId = form.getParam("billingId");
        if (billingId == null) {
            throw new BGMessageExceptionWithoutL10n("Не указан параметр запроса billingId");
        }
        return new ContractDAO(form.getUser(), billingId).getParameterList(type);
    }

    public ActionForward bgbillingUpdateContractTitleAndComment(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");
        String comment = form.getParam("comment");

        ContractDAO contractDAO = new ContractDAO(form.getUser(), billingId);
        contractDAO.bgbillingUpdateContractTitleAndComment(contractId, comment, 0);

        return json(conSet, form);
    }

    public ActionForward dateToUpdate(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");
        Date date = form.getParamDate("date");

        new ContractDAO(form.getUser(), billingId).dateToUpdate(contractId, date);

        return json(conSet, form);
    }

    public ActionForward bgbillingOpenContract(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        ContractDAO contractDAO = new ContractDAO(form.getUser(), billingId);
        contractDAO.bgbillingOpenContract(contractId);

        return json(conSet, form);
    }

    public ActionForward parameterList(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        ContractParamDAO paramDAO = new ContractParamDAO(form.getUser(), billingId);

        Pair<ParamList, List<ContractParameter>> parameterListWithDir = paramDAO.getParameterListWithDir(contractId, true,
                form.getParamBoolean("onlyFromGroup", false));

        Set<Integer> allowedParamIds = Utils.toIntegerSet(form.getPermission().get("parameterIds"));
        if (!allowedParamIds.isEmpty())
            parameterListWithDir.getSecond().removeIf(cp -> !allowedParamIds.contains(cp.getParamId()));

        form.setResponseData("group", parameterListWithDir.getFirst());
        form.setResponseData("contractParameterList", parameterListWithDir.getSecond());

        return html(conSet, form, PATH_JSP_CONTRACT + "/parameter_list.jsp");
    }

    public ActionForward parameterGet(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");
        Integer paramId = form.getParamInt("paramId");
        int paramType = form.getParamInt("paramType");

        ContractParamDAO paramDAO = new ContractParamDAO(form.getUser(), billingId);

        Response resp = form.getResponse();

        if (paramType <= 0) {
            throw new BGMessageExceptionWithoutL10n("Параметр не поддерживается для редактирования");
        }

        switch (paramType) {
            case ParameterType.ContractType.TYPE_TEXT: {
                break;
            }
            case ParameterType.ContractType.TYPE_ADDRESS: {
                ParameterAddressValue addressValue = ContractParamDAO
                        .toCrmObject(paramDAO.getAddressParam(contractId, paramId), conSet.getConnection());
                if (addressValue != null) {
                    int houseId = addressValue.getHouseId();

                    AddressHouse house = new AddressDAO(conSet.getConnection()).getAddressHouse(houseId, true, true, true);
                    if (house != null) {
                        resp.setData("house", house);
                    }
                }
                resp.setData("address", addressValue);
                break;
            }
            case ParameterType.ContractType.TYPE_DATE: {
                break;
            }
            case ParameterType.ContractType.TYPE_LIST: {
                resp.setData("value", paramDAO.getListParamValue(contractId, paramId));
                break;
            }
            case ParameterType.ContractType.TYPE_PHONE: {
                form.setResponseData("value", paramDAO.getPhoneParam(contractId, paramId));
                break;
            }
            case ParameterType.ContractType.TYPE_EMAIL: {
                resp.setData("emails", paramDAO.getEmailParam(contractId, paramId));
                break;
            }
            default: {
                break;
            }
        }

        return html(conSet, form, PATH_JSP + "/contract/parameter_editor.jsp");
    }

    public ActionForward parameterUpdate(DynActionForm form, ConnectionSet conSet) throws BGMessageException {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        int paramBillingId = form.getParamInt("paramId");
        int parameterType = form.getParamInt("paramType");

        Set<Integer> allowedParamIds = Utils.toIntegerSet(form.getPermission().get("parameterIds"));
        if (!allowedParamIds.isEmpty() && !allowedParamIds.contains(paramBillingId))
            throw new BGMessageExceptionWithoutL10n("Параметр с кодом " + paramBillingId + " запрещен для изменения.");

        ContractParamDAO paramDAO = new ContractParamDAO(form.getUser(), billingId);
        switch (parameterType) {
            case ParameterType.ContractType.TYPE_FLAG: {
                paramDAO.updateFlagParameter(contractId, paramBillingId, form.getParamBoolean("value", false));
                break;
            }
            case ParameterType.ContractType.TYPE_TEXT: {
                paramDAO.updateTextParameter(contractId, paramBillingId, form.getParam("value"));
                break;
            }
            case ParameterType.ContractType.TYPE_ADDRESS: {
                ParamAddressValue address = new ParamAddressValue();

                address.setStreetId(form.getParamInt("streetId "));
                address.setHouseId(form.getParamInt("houseId"));
                address.setStreetTitle(form.getParam("streetTitle"));
                address.setHouse(form.getParam("house"));
                address.setFlat(form.getParam("flat"));
                address.setRoom(form.getParam("room "));
                address.setPod(form.getParam("pod"));
                address.setFloor(form.getParam("floor"));
                address.setComment(form.getParam("comment"));

                paramDAO.updateAddressParameter(contractId, paramBillingId, address);
                break;
            }
            case ParameterType.ContractType.TYPE_DATE: {
                paramDAO.updateDateParameter(contractId, paramBillingId, form.getParam("value"));
                break;
            }
            case ParameterType.ContractType.TYPE_LIST: {
                paramDAO.updateListParameter(contractId, paramBillingId, form.getParam("value"));
                break;
            }
            case ParameterType.ContractType.TYPE_PHONE: {
                ParameterPhoneValue phoneValue = new ParameterPhoneValue();

                Iterator<String> phones = form.getParamValuesListStr("phone").iterator();
                Iterator<String> comments = form.getParamValuesListStr("comment").iterator();
                while (phones.hasNext())
                    phoneValue.addItem(new ParameterPhoneValueItem(phones.next(), comments.next()));

                paramDAO.updatePhoneParameter(contractId, paramBillingId, phoneValue);
                break;
            }
            case ParameterType.ContractType.TYPE_EMAIL: {
                List<ParameterEmailValue> emails = new ArrayList<>();

                List<String> strings = Utils.toList(form.getParam("emails"), "\n");

                if (strings.isEmpty()) {
                    Iterator<String> address = form.getParamValuesListStr("address").iterator();
                    Iterator<String> name = form.getParamValuesListStr("name").iterator();
                    while (address.hasNext())
                        emails.add(new ParameterEmailValue(address.next(), name.next()));
                } else {
                    for (String mail : strings) {
                        try {
                            InternetAddress addr = InternetAddress.parse(mail)[0];
                            emails.add(new ParameterEmailValue(addr.getAddress(), addr.getPersonal()));
                        } catch (AddressException e) {
                            throw new BGException("Некорректный адрес: " + mail, e);
                        }
                    }
                }
                paramDAO.updateEmailParameter(contractId, paramBillingId, emails);
                break;
            }
            default: {
                break;
            }
        }

        return json(conSet, form);
    }

    public ActionForward parameterGroupUpdate(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");
        int paramGroupId = form.getParamInt("paramGroupId");

        new ContractParamDAO(form.getUser(), billingId).updateParameterGroup(contractId, paramGroupId);

        return json(conSet, form);
    }

    public ActionForward additionalActionList(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        ContractDAO crmDAO = new ContractDAO(form.getUser(), billingId);

        form.setResponseData("additionalActionList", crmDAO.additionalActionList(contractId));

        return html(conSet, form, PATH_JSP_CONTRACT + "/additional_action_list.jsp");
    }

    public ActionForward executeAdditionalAction(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");
        Integer actionId = form.getParamInt("actionId");

        ContractDAO crmDAO = new ContractDAO(form.getUser(), billingId);

        form.setResponseData("executeResult", crmDAO.executeAdditionalAction(contractId, actionId));
        form.setResponseData("additionalActionList", crmDAO.additionalActionList(contractId));

        return html(conSet, form, PATH_JSP_CONTRACT + "/additional_action_list.jsp");
    }

    public ActionForward groupList(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        Pair<List<IdTitle>, Set<Integer>> groupsGet = new ContractDAO(form.getUser(), billingId).groupsGet(contractId);

        form.setResponseData("groupList", groupsGet.getFirst());
        form.setResponseData("selectedGroupIds", groupsGet.getSecond());

        return html(conSet, form, PATH_JSP_CONTRACT + "/group_list.jsp");
    }

    public ActionForward updateGroups(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");
        Set<Integer> groupIds = form.getParamValues("groupId");

        ContractDAO contractDao = new ContractDAO(form.getUser(), billingId);

        contractDao.updateLabels(contractId, groupIds);

        return json(conSet, form);
    }

    // TODO: Rename to 'noteList'
    public ActionForward memoList(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        form.setResponseData("list", new ContractNoteDAO(form.getUser(), billingId).list(contractId));

        return html(conSet, form, PATH_JSP_CONTRACT + "/note/list.jsp");
    }

    // TODO: Rename to 'noteGet'
    public ActionForward getMemo(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        if (form.getId() > 0) {
            form.setResponseData("note", new ContractNoteDAO(form.getUser(), billingId).get(contractId, form.getId()));
        }

        return html(conSet, form, PATH_JSP_CONTRACT + "/note/edit.jsp");
    }

    // TODO: Rename to 'noteUpdate'
    public ActionForward updateMemo(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");
        Integer memoId = form.getParamInt("id", 0);
        String memoTitle = form.getParam("title");
        String memoText = form.getParam("text");

        new ContractNoteDAO(form.getUser(), billingId).update(contractId, memoId, memoTitle, memoText);

        return json(conSet, form);
    }

    // TODO: Rename to 'noteDelete'
    public ActionForward deleteMemo(DynActionForm form, ConnectionSet conSet) throws BGMessageException {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");
        Integer memoId = form.getParamInt("id", 0);

        if (memoId <= 0)
            throw new BGIllegalArgumentException();

        new ContractNoteDAO(form.getUser(), billingId).delete(contractId, memoId);

        return json(conSet, form);
    }

    public ActionForward contractObjectList(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        ContractObjectDAO contractObjectDAO = new ContractObjectDAO(form.getUser(), billingId);
        form.setResponseData("objectList", contractObjectDAO.getContractObjects(contractId));

        return html(conSet, form, PATH_JSP_CONTRACT + "/object/object_list.jsp");
    }

    public ActionForward getContractObject(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");
        Integer objectId = form.getParamInt("objectId");

        ContractObjectDAO contractObjectDAO = new ContractObjectDAO(form.getUser(), billingId);
        form.setResponseData("object", contractObjectDAO.getContractObject(contractId, objectId));

        return html(conSet, form, PATH_JSP_CONTRACT + "/object/object_editor.jsp");
    }

    public ActionForward deleteContractObject(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");
        Integer objectId = form.getParamInt("objectId");

        ContractObjectDAO contractObjectDAO = new ContractObjectDAO(form.getUser(), billingId);
        contractObjectDAO.deleteContractObject(contractId, objectId);

        return json(conSet, form);
    }

    public ActionForward updateContractObject(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        int objectId = form.getParamInt("objectId");
        int typeId = form.getParamInt("typeId");
        String title = form.getParam("title");
        Date dateFrom = form.getParamDate("dateFrom");
        Date dateTo = form.getParamDate("dateTo");

        ContractObjectDAO contractObjectDAO = new ContractObjectDAO(form.getUser(), billingId);
        contractObjectDAO.updateContractObject(contractId, objectId, typeId, title, dateFrom, dateTo);

        return json(conSet, form);
    }

    public ActionForward contractObjectParameterList(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        Integer objectId = form.getParamInt("objectId");

        ContractObjectParamDAO paramDAO = new ContractObjectParamDAO(form.getUser(), billingId);
        form.setResponseData("parameterList", paramDAO.getParameterList(contractId, objectId));

        return html(conSet, form, PATH_JSP_CONTRACT + "/object/object_parameter_list.jsp");
    }

    public ActionForward getObjectParameter(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        Integer objectId = form.getParamInt("objectId");
        Integer paramId = form.getParamInt("paramId");

        ContractObjectParamDAO paramDAO = new ContractObjectParamDAO(form.getUser(), billingId);
        ContractObjectParameter parameter = paramDAO.getParameter(contractId, objectId, paramId);

        form.setResponseData("parameter", parameter);

        int paramType = parameter.getTypeId();
        if (paramType <= 0) {
            throw new BGMessageExceptionWithoutL10n("Параметр не поддерживается для редактирования");
        }

        switch (paramType) {
            case ParameterType.ContractObjectType.TYPE_TEXT: {
                break;
            }

            case ParameterType.ContractObjectType.TYPE_ADDRESS: {
                ParameterAddressValue addressValue = paramDAO.getAddressParam(objectId, paramId).toParameterAddressValue(conSet.getConnection());
                if (addressValue != null) {
                    int houseId = addressValue.getHouseId();

                    AddressHouse house = new AddressDAO(conSet.getConnection()).getAddressHouse(houseId, true, true, true);
                    if (house != null) {
                        form.setResponseData("house", house);
                    }
                }

                form.setResponseData("address", addressValue);
                break;
            }

            case ParameterType.ContractObjectType.TYPE_DATE: {
                if (Utils.notBlankString(parameter.getValue())) {
                    form.setResponseData("dateValue", new SimpleDateFormat("yyyy-MM-dd")
                            .format(TimeUtils.parse(parameter.getValue(), TimeUtils.PATTERN_DDMMYYYY)));
                }
                break;
            }

            case ParameterType.ContractObjectType.TYPE_LIST: {
                form.setResponseData("valueList", paramDAO.getListParam(objectId, paramId));
                break;
            }
        }

        return html(conSet, form, PATH_JSP_CONTRACT + "/object/object_parameter_editor.jsp");
    }

    public ActionForward updateObjectParameter(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        Integer objectId = form.getParamInt("objectId");
        Integer paramBillingId = form.getParamInt("paramId");
        Integer parameterType = form.getParamInt("paramType");
        Integer contractId = form.getParamInt("contractId");

        ContractObjectParamDAO paramDAO = new ContractObjectParamDAO(form.getUser(), billingId);

        switch (parameterType) {
            case ParameterType.ContractObjectType.TYPE_TEXT:
                paramDAO.updateTextParameter(contractId,objectId, paramBillingId, form.getParam("textValue"));
                break;

            case ParameterType.ContractObjectType.TYPE_ADDRESS:
                ParamAddressValue address = new ParamAddressValue();

                address.setStreetId(form.getParamInt("streetId "));
                address.setHouseId(form.getParamInt("houseId"));
                address.setStreetTitle(form.getParam("streetTitle"));
                address.setHouse(form.getParam("house"));
                address.setFlat(form.getParam("flat"));
                address.setRoom(form.getParam("room "));
                address.setPod(form.getParam("pod"));
                address.setFloor(form.getParam("floor"));
                address.setComment(form.getParam("comment"));

                paramDAO.updateAddressParameter(contractId, objectId, paramBillingId, address);
                break;

            case ParameterType.ContractObjectType.TYPE_DATE:
                paramDAO.updateDateParameter(contractId, objectId, paramBillingId, form.getParam("dateValue"));
                break;

            case ParameterType.ContractObjectType.TYPE_LIST:
                paramDAO.updateListParameter(contractId, objectId, paramBillingId, form.getParam("listValueId"));
                break;

            default:
                break;
        }

        return json(conSet, form);
    }

    public ActionForward contractObjectModuleInfo(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer objectId = form.getParamInt("objectId");

        ContractObjectDAO dao = new ContractObjectDAO(form.getUser(), billingId);
        form.setResponseData("moduleInfo", dao.contractObjectModuleList(objectId));

        return json(conSet, form);
    }

    public ActionForward contractObjectModuleSummaryTable(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer objectId = form.getParamInt("objectId");

        ContractObjectDAO dao = new ContractObjectDAO(form.getUser(), billingId);
        form.setResponseData("moduleInfo", dao.contractObjectModuleList(objectId));

        return html(conSet, form, PATH_JSP_CONTRACT + "/object/object_module_summary_table.jsp");
    }

    public ActionForward contractSubcontractList(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        ContractHierarchyDAO crmDAO = new ContractHierarchyDAO(form.getUser(), billingId);
        form.setResponseData("subContractList", crmDAO.contractSubcontractList(contractId));
        form.setResponseData("superContract", crmDAO.contractSupercontract(contractId));

        return html(conSet, form, PATH_JSP_CONTRACT + "/subcontract_list.jsp");
    }

    public ActionForward scriptList(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        form.setResponseData("scriptList",
                new ContractScriptDAO(form.getUser(), billingId).contractScriptList(contractId));

        form.setRequestAttribute("contractInfo", new ContractDAO(form.getUser(), billingId).getContractInfo(contractId));

        return html(conSet, form, PATH_JSP_CONTRACT + "/script/script_list.jsp");
    }

    public ActionForward getScript(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer scriptId = form.getParamInt("scriptId");

        ContractScriptDAO crmDAO = new ContractScriptDAO(form.getUser(), billingId);
        form.setResponseData("script", crmDAO.contractScriptGet(scriptId));
        form.setResponseData("scriptTypeList", new DirectoryDAO(form.getUser(), billingId).scriptTypeList());

        return html(conSet, form, PATH_JSP_CONTRACT + "/script/script_editor.jsp");
    }

    public ActionForward scriptLog(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        String dateFrom = form.getParam("dateFrom");
        String dateTo = form.getParam("dateTo");

        Pageable<ContractScriptLogItem> result = new Pageable<>(form);

        ContractScriptDAO crmDAO = new ContractScriptDAO(form.getUser(), billingId);
        crmDAO.contractScriptLogList(result, contractId, dateFrom, dateTo);

        return html(conSet, form, PATH_JSP_CONTRACT + "/script/script_log.jsp");
    }

    public ActionForward deleteScript(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer scriptId = form.getParamInt("scriptId");

        ContractScriptDAO crmDAO = new ContractScriptDAO(form.getUser(), billingId);
        crmDAO.deleteContractScript(scriptId);

        return json(conSet, form);
    }

    public ActionForward updateScript(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");
        Integer scriptId = form.getParamInt("scriptId");
        Integer scriptTypeId = form.getParamInt("scriptTypeId");
        String comment = form.getParam("comment");
        String dateFrom = form.getParam("dateFrom");
        String dateTo = form.getParam("dateTo");

        ContractScriptDAO crmDAO = new ContractScriptDAO(form.getUser(), billingId);
        crmDAO.updateContractScript(contractId, scriptId, scriptTypeId, comment, dateFrom, dateTo);

        return json(conSet, form);
    }

    public ActionForward faceLog(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        ContractDAO contractDao = new ContractDAO(form.getUser(), billingId);
        contractDao.faceLog(new Pageable<>(form), contractId);

        form.setResponseData("contractInfo", new ContractDAO(form.getUser(), billingId).getContractInfo(contractId));

        return html(conSet, form, PATH_JSP_CONTRACT + "/face_log.jsp");
    }

    public ActionForward updateFace(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        new ContractDAO(form.getUser(), billingId).updateFace(contractId, form.getParamInt("value"));

        return json(conSet, form);
    }

    public ActionForward modeLog(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        ContractDAO contractDao = new ContractDAO(form.getUser(), billingId);
        contractDao.modeLog(new Pageable<>(form), contractId);

        form.setResponseData("contractInfo", contractDao.getContractInfo(contractId));

        return html(conSet, form, PATH_JSP_CONTRACT + "/mode_log.jsp");
    }

    public ActionForward updateMode(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        new ContractDAO(form.getUser(), billingId).updateMode(contractId, form.getParamInt("value"));

        return json(conSet, form);
    }

    public ActionForward moduleList(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        Pair<List<IdTitle>, List<IdTitle>> pair = new ContractDAO(form.getUser(), billingId).moduleList(contractId);
        form.setResponseData("selectedList", pair.getFirst());
        form.setResponseData("availableList", pair.getSecond());

        return html(conSet, form, PATH_JSP_CONTRACT + "/module_list.jsp");
    }

    public ActionForward updateModules(DynActionForm form, ConnectionSet conSet) throws BGMessageException {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");
        Set<Integer> moduleIds = form.getParamValues("moduleId");
        String command = form.getParam("command");

        if (Utils.notBlankString(command)) {
            ContractDAO contractDao = new ContractDAO(form.getUser(), billingId);
            for (Integer moduleId : moduleIds) {
                contractDao.updateModule(contractId, moduleId, command);
            }
        }

        return json(conSet, form);
    }

    public ActionForward status(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        DirectoryDAO directoryDAO = new DirectoryDAO(form.getUser(), billingId);
        Map<Integer, String> statusTitleMap = directoryDAO.getContractStatusList(false).stream()
                .collect(Collectors.toMap(IdTitle::getId, IdTitle::getTitle));

        ContractStatusDAO statusDao = new ContractStatusDAO(form.getUser(), billingId);
        form.setResponseData("statusList", statusDao.statusList(contractId, statusTitleMap));
        form.setResponseData("statusLog", statusDao.statusLog(contractId));
        form.setResponseData("statusFutureTasks", statusDao.statusFutureTasks(contractId, statusTitleMap));

        form.setResponseData("availableStatusList", directoryDAO.getContractStatusList(true));

        form.setRequestAttribute("contractInfo", new ContractDAO(form.getUser(), billingId).getContractInfo(contractId));

        return html(conSet, form, PATH_JSP_CONTRACT + "/status.jsp");
    }

    public ActionForward updateStatus(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        new ContractStatusDAO(form.getUser(), billingId).updateStatus(contractId, form.getParamInt("statusId"),
                form.getParamDate("dateFrom"), form.getParamDate("dateTo"), form.getParam("comment"));

        return json(conSet, form);
    }

    public ActionForward limit(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        Pageable<LimitLogItem> limitList = new Pageable<>(form);
        List<LimitChangeTask> taskList = new ArrayList<>();

        BigDecimal limit = new ContractDAO(form.getUser(), billingId).limit(contractId, limitList, taskList);

        form.setResponseData("limit", limit);
        form.setResponseData("taskList", taskList);

        return html(conSet, form, PATH_JSP_CONTRACT + "/limit.jsp");
    }

    public ActionForward updateLimit(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        new ContractDAO(form.getUser(), billingId).updateLimit(contractId,
                Utils.parseBigDecimal(form.getParam("value")), form.getParamInt("period"),
                form.getParam("comment", ""));

        return json(conSet, form);
    }

    public ActionForward deleteLimitTask(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        new ContractDAO(form.getUser(), billingId).deleteLimitTask(contractId, form.getId());

        return json(conSet, form);
    }

    public ActionForward contractCards(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        ContractDAO contractDao = new ContractDAO(form.getUser(), billingId);
        form.setResponseData("cardTypeList", contractDao.getContractCardTypes(contractId));
        form.setResponseData("fullCard", contractDao.getContractFullCard(contractId));

        return html(conSet, form, PATH_JSP_CONTRACT + "/cards.jsp");
    }

    public ActionForward getContractCard(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        String cardType = form.getParam("cardType");

        try {
            OutputStream out = form.getHttpResponse().getOutputStream();
            Utils.setFileNameHeaders(form.getHttpResponse(), "card.pdf");
            out.write(new ContractDAO(form.getUser(), billingId).getContractCard2Pdf(contractId, cardType));
        } catch (Exception ex) {
            throw new BGException(ex);
        }

        return null;
    }
}
