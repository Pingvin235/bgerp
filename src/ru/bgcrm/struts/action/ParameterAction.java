package ru.bgcrm.struts.action;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForward;
import org.apache.struts.upload.FormFile;
import org.bgerp.action.FileAction;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.app.exception.BGIllegalArgumentException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.cache.ParameterCache;
import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.dao.FileDataDAO;
import org.bgerp.dao.customer.CustomerLogDAO;
import org.bgerp.dao.param.ParamLogDAO;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.dao.process.ProcessLogDAO;
import org.bgerp.model.Pageable;
import org.bgerp.model.base.IdTitle;
import org.bgerp.model.file.FileData;
import org.bgerp.model.param.Parameter;
import org.bgerp.model.param.ParameterValue;

import ru.bgcrm.dao.AddressDAO;
import ru.bgcrm.dao.EntityLogDAO;
import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.event.DateChangingEvent;
import ru.bgcrm.event.ParamChangedEvent;
import ru.bgcrm.event.ParamChangingEvent;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterPhoneValueItem;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.model.param.config.ListParamConfig;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.struts.form.Response;
import ru.bgcrm.util.AddressUtils;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SingleConnectionSet;

@Action(path = "/user/parameter")
public class ParameterAction extends BaseAction {
    protected static final String PATH_JSP = PATH_JSP_USER + "/parameter";

    public ActionForward parameterLog(DynActionForm form, ConnectionSet conSet) throws Exception {
        int id = form.getId();
        String objectType = form.getParam("objectType");
        List<Parameter> paramList = ParameterCache.getObjectTypeParameterList(objectType);
        boolean offEncrypt = form.getPermission().getBoolean("offEncrypt", false);
        form.setResponseData("log", new ParamLogDAO(conSet.getSlaveConnection())
                .getHistory(id, paramList, offEncrypt, new Pageable<>(form)));

        return html(conSet, form, PATH_JSP + "/log.jsp");
    }

    // Used for customers, but not for processes.
    public ActionForward entityLog(DynActionForm form, ConnectionSet con) throws Exception {
        int id = form.getId();
        String type = form.getParam("type");
        EntityLogDAO dao = null;

        if (type.equals(Process.OBJECT_TYPE))
            dao = new ProcessLogDAO(con.getSlaveConnection());
        else if (type.equals(Customer.OBJECT_TYPE))
            dao = new CustomerLogDAO(con.getSlaveConnection());

        form.setResponseData("log", dao.getHistory(id));

        return html(con, form, BaseAction.PATH_JSP + "/entity_log.jsp");
    }

    public ActionForward parameterList(DynActionForm form, ConnectionSet conSet) throws Exception {
        parameterListInternal(form, conSet);
        return html(conSet, form, PATH_JSP + "/list.jsp");
    }

    protected void parameterListInternal(DynActionForm form, ConnectionSet conSet) throws Exception {
        int id = form.getId();
        String objectType = form.getParam("objectType");
        int parameterGroupId = form.getParamInt("parameterGroup", -1); // doesn't work with 0!!
        List<Integer> pids = form.getParamValuesList("paramId");

        List<Parameter> paramList = null;
        if (pids.size() > 0) {
            paramList = ParameterCache.getParameterList(pids);
        } else {
            paramList = ParameterCache.getObjectTypeParameterList(objectType, parameterGroupId);
        }

        Set<Integer> hideParamIds = Collections.emptySet();

        if (Process.OBJECT_TYPE.equals(objectType)) {
            Process process = new ProcessDAO(conSet.getConnection()).getProcessOrThrow(id);
            ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());

            hideParamIds = Utils.toIntegerSet(type.getProperties().getConfigMap().get("hideParamIdsInStatus." + process.getStatusId(), ""));
            if (pids.size() == 0) {
                paramList = Utils.getObjectList(ParameterCache.getParameterMap(), type.getProperties().getParameterIds());
            }

            for (Entry<Integer, ConfigMap> entry : type.getProperties().getConfigMap().subIndexed("showParam.").entrySet()) {
                String expression = entry.getValue().get(Expression.CHECK_EXPRESSION_CONFIG_KEY);

                Map<String, Object> context = Expression.context(conSet, form, null, process);
                if (Utils.notBlankString(expression) && !(new Expression(context).executeCheck(expression))) {
                    hideParamIds.add(entry.getKey());
                }
            }
        }

        Set<Integer> restrictedParamIds = Utils.toIntegerSet(form.getPermission().get("restrictedParameterIds"));
        Set<Integer> allowedParamIds = Utils.toIntegerSet(form.getPermission().get("parameterIds"));
        Set<String> allowedTags = Utils.toSet(form.getPermission().get("tags"));

        for (int i = 0; i < paramList.size(); i++) {
            Parameter param = (Parameter) paramList.get(i);

            Set<String> tags = Utils.toSet(param.getConfigMap().get("tagsShow", param.getConfigMap().get("tags")));
            final int paramId = param.getId();

            if (hideParamIds.contains(paramId) || (!restrictedParamIds.isEmpty() && restrictedParamIds.contains(paramId))
                    || (!allowedParamIds.isEmpty() && !allowedParamIds.contains(paramId))
                    || (tags.size() > 0 && CollectionUtils.intersection(tags, allowedTags).size() == 0)) {
                paramList.remove(i);
                i--;
            }
        }

        boolean offEncryption = form.getPermission().getBoolean("offEncrypt", false);

        List<ParameterValue> parameterValuePairList = new ParamValueDAO(conSet.getConnection()).loadParameters(paramList, id, offEncryption);

        form.setResponseData("list", parameterValuePairList);
    }

    public ActionForward parameterGet(DynActionForm form, ConnectionSet conSet) throws Exception {
        int id = form.getId();
        int paramId = Utils.parseInt(form.getParam("paramId"));

        HttpServletRequest request = form.getHttpRequest();
        HttpServletResponse response = form.getHttpResponse();

        ParamValueDAO paramDAO = new ParamValueDAO(conSet.getConnection());
        AddressDAO addressDAO = new AddressDAO(conSet.getConnection());

        Parameter parameter = ParameterCache.getParameter(paramId);
        if (parameter == null) {
            throw new BGMessageException("Параметр не найден.");
        }

        Response resp = form.getResponse();
        request.setAttribute("parameter", parameter);

        boolean offEncryption = form.getPermission().getBoolean("offEncrypt", false);

        if ("encrypted".equals(parameter.getConfigMap().get("encrypt")) && !offEncryption) {
            if (Parameter.TYPE_TEXT.equals(parameter.getType()) || Parameter.TYPE_BLOB.equals(parameter.getType())) {
                resp.setData("value", l.l("<ЗНАЧЕНИЕ ЗАШИФРОВАНО>"));

                return html(conSet,  form, PATH_JSP + "/edit.jsp");
            }
        }

        switch (Parameter.Type.of(parameter.getType())) {
            case ADDRESS -> {
                ParameterAddressValue addressValue = paramDAO.getParamAddress(id, paramId, Utils.parseInt(form.getParam("position")));
                // значений
                if (addressValue != null) {
                    int houseId = addressValue.getHouseId();

                    AddressHouse house = addressDAO.getAddressHouse(houseId, true, true, true);
                    if (house != null) {
                        resp.setData("house", house);
                    }
                }

                resp.setData("address", addressValue);
            }
            case BLOB -> {
                resp.setData("value", paramDAO.getParamBlob(id, paramId));
            }
            case DATE, DATETIME -> {
                if (Utils.notBlankString(form.getParam("newDate"))) {
                    EventProcessor.processEvent(
                            new DateChangingEvent(form, id, parameter, TimeUtils.parse(form.getParam("newDate"), TimeUtils.FORMAT_TYPE_YMD)),
                            conSet);
                    // TODO: Cleanup, but change calendar highlight in Callboard plugin
                    EventProcessor.processEvent(
                            new DateChangingEvent(form, id, parameter, TimeUtils.parse(form.getParam("newDate"), TimeUtils.FORMAT_TYPE_YMD)),
                            conSet);
                }

                if (Parameter.TYPE_DATE.equals(parameter.getType())) {
                    resp.setData("value", paramDAO.getParamDate(id, paramId));
                } else if (Parameter.TYPE_DATETIME.equals(parameter.getType())) {
                    resp.setData("value", paramDAO.getParamDateTime(id, paramId));
                }
            }
            case EMAIL -> {
                resp.setData("values", paramDAO.getParamEmail(id, paramId).values());
            }
            case FILE -> {
                //TODO: Сделать поддержку разных типов.
                response.setContentType("image/jpeg");
                FileData fileData = paramDAO.getParamFile(id, paramId, 1);

                File file = new FileDataDAO(conSet.getConnection()).getFile(fileData);

                FileInputStream in = new FileInputStream(file);
                ServletOutputStream out = response.getOutputStream();

                //TODO: StreamUtils.copy??
                byte[] outputByte = new byte[4096];
                while (in.read(outputByte, 0, 4096) != -1) {
                    out.write(outputByte, 0, 4096);
                }
                in.close();

                return null;
            }
            case LIST -> {
                resp.setData("values", paramDAO.getParamListWithComments(id, paramId));
                listValues(form, parameter);
                request.setAttribute("listParamConfig", parameter.getConfigMap().getConfig(ListParamConfig.class));
            }
            case LISTCOUNT -> {
                resp.setData("values", paramDAO.getParamListCount(id, paramId));
                listValues(form, parameter);
            }
            case MONEY -> {
                resp.setData("value", paramDAO.getParamMoney(id, paramId));
            }
            case PHONE -> {
                resp.setData("value", paramDAO.getParamPhone(id, paramId));
            }
            case TEXT -> {
                resp.setData("value", paramDAO.getParamText(id, paramId));
            }
            case TREE -> {
                resp.setData("values", paramDAO.getParamTree(id, paramId));
                resp.setData("treeRootNode", ParameterCache.getTreeParamRootNode(parameter));
            }
            case TREECOUNT -> {
                resp.setData("values", paramDAO.getParamTreeCount(id, paramId));
                resp.setData("treeValues", ParameterCache.getTreeParamValues(paramId));
                resp.setData("treeRootNode", ParameterCache.getTreeParamRootNode(parameter));
            }
        }

        return html(conSet, form, PATH_JSP + "/edit.jsp");
    }

    private void listValues(DynActionForm form, Parameter param) {
        List<IdTitle> listValues = ParameterCache.getListParamValues(param).stream()
            .filter(item -> !item.getTitle().startsWith("@"))
            .collect(Collectors.toList());

        String valuesSortMode = param.getConfigMap().get("sort.mode", "");
        if (valuesSortMode.equals("byTitle")) {
            Collections.sort(listValues, new Comparator<>() {
                public int compare(IdTitle first, IdTitle second) {
                    return first.getTitle().compareTo(second.getTitle());
                }
            });
        }

        // for external systems
        form.setResponseData("listValues", listValues);
    }

    public ActionForward parameterUpdate(DynActionForm form, Connection con) throws Exception {
        int id = form.getId();
        int paramId = Utils.parseInt(form.getParam("paramId"));
        int userId = form.getUserId();

        Set<Integer> restrictedParamIds = Utils.toIntegerSet(form.getPermission().get("restrictedParameterIds"));
        Set<Integer> allowedParamIds = Utils.toIntegerSet(form.getPermission().get("parameterIds"));

        if (!restrictedParamIds.isEmpty() && restrictedParamIds.contains(paramId)) {
            throw new BGMessageException("Parameter with ID {} is disallowed to edit", paramId);
        } else if (!allowedParamIds.isEmpty() && !allowedParamIds.contains(paramId)) {
            throw new BGMessageException("Parameter with ID {} is disallowed to edit", paramId);
        }

        ParamValueDAO paramValueDAO = new ParamValueDAO(con, true, userId);

        Parameter parameter = ParameterCache.getParameter(paramId);
        if (parameter == null)
            throw new BGMessageException("Parameter not found");

        // проверка тегов
        Set<String> tags = Utils.toSet(parameter.getConfigMap().get("tags"));
        if (tags.size() > 0) {
            Set<String> allowedTags = Utils.toSet(form.getPermission().get("tags"));
            if (CollectionUtils.intersection(tags, allowedTags).size() == 0) {
                throw new BGMessageException("The tagged parameter is disallowed to edit");
            }
        }

        // проверка параметров, которые должны быть заполнены
        final String requireParamName = "requireBeforeFillParamIds";

        Set<Integer> requireBeforeParams = Utils.toIntegerSet(parameter.getConfigMap().get(requireParamName, ""));
        for (int requireParamId : requireBeforeParams) {
            Parameter requireParam = ParameterCache.getParameter(requireParamId);
            if (requireParam == null) {
                throw new BGMessageException(
                        "Параметр с кодом " + requireParamId + " не существует.\nУказан в " + requireParamName + " конфигурации параметра.");
            }

            if (!paramValueDAO.isParameterFilled(id, requireParam)) {
                throw new BGMessageException("Параметр '" + requireParam.getTitle() + "' не заполнен.");
            }
        }

        // проверка параметров, которые не должны быть заполнены
        final String requireEmptyParamName = "requireBeforeEmptyParamIds";

        Set<Integer> requireEmptyBeforeParams = Utils.toIntegerSet(parameter.getConfigMap().get(requireEmptyParamName, ""));
        for (int requireParamId : requireEmptyBeforeParams) {
            Parameter requireParam = ParameterCache.getParameter(requireParamId);
            if (requireParam == null) {
                throw new BGMessageException(
                        "Параметр с кодом " + requireParamId + " не существует.\nУказан в " + requireEmptyParamName + " конфигурации параметра.");
            }

            if (paramValueDAO.isParameterFilled(id, requireParam)) {
                throw new BGMessageException("Параметр '" + requireParam.getTitle() + "' заполнен.");
            }
        }

        Object paramValue = null;

        switch (Parameter.Type.of(parameter.getType())) {
            case ADDRESS -> {
                ParameterAddressValue addressValue = null;
                int houseId = form.getParamInt("houseId", -1);

                int streetId = form.getParamInt("streetId", -1);
                String house = form.getParam("house");
                int position = Utils.parseInt(form.getParam("position"));

                // search house by number and streetId
                if (houseId <= 0 && house != null) {
                    AddressDAO addressDAO = new AddressDAO(con);
                    List<Integer> houses = addressDAO.getHouseIdsByStreetAndHouse(streetId, house, null);

                    if (houses.size() == 1)
                        houseId = houses.get(0);
                    else
                        throw new BGMessageException("house.not.found");
                }

                if (houseId > 0) {
                    addressValue = new ParameterAddressValue();

                    addressValue.setHouseId(houseId);

                    if (addressValue.getHouseId() <= 0)
                        throw new BGMessageException("Не выбран дом.");

                    addressValue.setFlat(form.getParam("flat", ""));
                    addressValue.setRoom(form.getParam("room", ""));
                    addressValue.setPod(Utils.parseInt(form.getParam("pod"), -1));
                    String floor = form.getParam("floor");
                    addressValue.setFloor(Utils.notBlankString(floor) ? Integer.parseInt(floor) : null);
                    addressValue.setComment(form.getParam("comment", ""));
                    addressValue.setValue(AddressUtils.buildAddressValue(addressValue, con));
                }

                paramChangingProcess(con, new ParamChangingEvent(form, parameter, id, paramValue = addressValue));

                paramValueDAO.updateParamAddress(id, paramId, position, addressValue);
            }
            case BLOB -> {
                paramValue = form.getParam("value");
                paramChangingProcess(con, new ParamChangingEvent(form, parameter, id, paramValue));
                paramValueDAO.updateParamBlob(id, paramId, (String) paramValue);
            }
            case DATE -> {
                String value = form.getParam("value");

                paramValue = TimeUtils.parse(value, TimeUtils.FORMAT_TYPE_YMD);
                if (Utils.notBlankString(value) &&
                // при годе большим 4х знаков MySQL сохраняет нули и потом выдаёт ошибку при выборке
                        (paramValue == null || TimeUtils.convertDateToCalendar((Date) paramValue).get(Calendar.YEAR) >= 10000)) {
                    throw new BGMessageException("Неверный формат.");
                }

                paramChangingProcess(con, new ParamChangingEvent(form, parameter, id, paramValue));

                paramValueDAO.updateParamDate(id, paramId, (Date) paramValue);
            }
            case DATETIME -> {
                String value = form.getParam("value");

                paramValue = TimeUtils.parse(value, parameter.getConfigMap().get("type", TimeUtils.FORMAT_TYPE_YMD));
                if (Utils.notBlankString(value) &&
                // при годе большим 4х знаков MySQL сохраняет нули и потом выдаёт ошибку при выборке
                        (paramValue == null || TimeUtils.convertDateToCalendar((Date) paramValue).get(Calendar.YEAR) >= 10000)) {
                    throw new BGMessageException("Неверный формат.");
                }

                paramChangingProcess(con, new ParamChangingEvent(form, parameter, id, paramValue));

                paramValueDAO.updateParamDateTime(id, paramId, (Date) paramValue);
            }
            case EMAIL -> {
                var values = new ArrayList<ParameterEmailValue>();

                Iterator<String> emails = form.getParamValuesListStr("address").iterator();
                Iterator<String> comments = form.getParamValuesListStr("name").iterator();
                while (emails.hasNext())
                    values.add(new ParameterEmailValue(emails.next(), comments.hasNext() ? comments.next() : ""));

                paramChangingProcess(con, new ParamChangingEvent(form, parameter, id, paramValue = values));

                paramValueDAO.updateParamEmail(id, paramId, values);
            }
            case FILE -> {
                FormFile file = form.getFile();

                FileAction.uploadFileCheck(file);

                int position = form.getParamInt("position", 0);

                FileData fileData = null;
                if (file != null) {
                    log.debug("Uploading file: {}, type: {}", file.getFileName(), file.getContentType());

                    fileData = new FileData();

                    FileDataDAO fileDataDAO = new FileDataDAO(con);

                    fileData.setTitle(file.getFileName());
                    try (var fos = fileDataDAO.add(fileData)) {
                        fos.write(file.getFileData());
                    }
                }

                paramChangingProcess(con, new ParamChangingEvent(form, parameter, id, paramValue = fileData));

                paramValueDAO.updateParamFile(id, paramId, position, fileData);
            }
            case LIST -> {
                ListParamConfig config = parameter.getConfigMap().getConfig(ListParamConfig.class);

                Map<Integer, String> values = new HashMap<>();

                for (String value : form.getParamValuesStr("value")) {
                    int val = Utils.parseInt(StringUtils.substringBefore(value, ":"));
                    String comment = Utils.maskNull(StringUtils.substringAfter(value, ":"));

                    if (val <= 0)
                        continue;

                    if (config.getCommentValues().get(val) == null)
                        comment = "";
                    else if (config.getNeedCommentValues().get(val) != null && Utils.isBlankString(comment))
                        throw new BGMessageException("Not defined mandatory comment for a value");

                    values.put(val, comment);
                }

                paramChangingProcess(con, new ParamChangingEvent(form, parameter, id, paramValue = values.keySet()));

                paramValueDAO.updateParamListWithComments(id, paramId, values);
            }
            case LISTCOUNT -> {
                final List<String> emptyValues = List.of("");

                List<String> itemIds = form.getParamValuesListStr("itemId");
                List<String> itemCounts = form.getParamValuesListStr("itemCount");

                Map<Integer, BigDecimal> values = new TreeMap<>();

                // two single empty sting lists mean deletion
                if (!itemIds.equals(emptyValues) || !itemCounts.equals(emptyValues)) {
                    for (int i = 0; i < itemIds.size() && i < itemCounts.size(); i++) {
                        Integer itemId = Utils.parseInt(itemIds.get(i));
                        BigDecimal itemCount = Utils.parseBigDecimal(itemCounts.get(i));

                        if (itemId <= 0 || BigDecimal.ZERO.equals(itemCount))
                            throw new BGIllegalArgumentException("itemCount");

                        values.put(itemId, itemCount);
                    }
                }

                paramChangingProcess(con, new ParamChangingEvent(form, parameter, id, paramValue = values));

                paramValueDAO.updateParamListCount(id, paramId, values);
            }
            case MONEY -> {
                paramValue = Utils.parseBigDecimal(form.getParam("value"));
                paramChangingProcess(con, new ParamChangingEvent(form, parameter, id, paramValue));
                paramValueDAO.updateParamMoney(id, paramId, (BigDecimal) paramValue);
            }
            case PHONE -> {
                ParameterPhoneValue phoneValue = new ParameterPhoneValue();

                Iterator<String> phones = form.getParamValuesListStr("phone").iterator();
                Iterator<String> comments = form.getParamValuesListStr("comment").iterator();
                while (phones.hasNext())
                    phoneValue.addItem(new ParameterPhoneValueItem(phones.next(), comments.hasNext() ? comments.next() : ""));

                paramChangingProcess(con, new ParamChangingEvent(form, parameter, id, paramValue = phoneValue));

                paramValueDAO.updateParamPhone(id, paramId, phoneValue);
            }
            case TEXT -> {
                paramValue = form.getParam("value");
                paramChangingProcess(con, new ParamChangingEvent(form, parameter, id, paramValue));
                paramValueDAO.updateParamText(id, paramId, (String) paramValue);
            }
            case TREE -> {
                Set<String> values = form.getParamValuesStr("value");
                // TODO: Попробовать убрать, проверить.
                values.removeAll(Arrays.asList("0", "-1"));

                paramChangingProcess(con, new ParamChangingEvent(form, parameter, id, paramValue = values));

                paramValueDAO.updateParamTree(id, paramId, values);
            }
            case TREECOUNT -> {
                Map<String, BigDecimal> values = new TreeMap<>();

                List<String> itemIds = form.getParamValuesListStr("itemId");
                List<String> itemCounts = form.getParamValuesListStr("itemCount");

                for (int i = 0; i < itemIds.size() && i < itemCounts.size(); i++) {
                    String itemId = itemIds.get(i);
                    BigDecimal itemCount = Utils.parseBigDecimal(itemCounts.get(i));

                    if (Utils.isBlankString(itemId) || BigDecimal.ZERO.equals(itemCount))
                        throw new BGIllegalArgumentException("itemCount");

                    values.put(itemId, itemCount);
                }

                paramChangingProcess(con, new ParamChangingEvent(form, parameter, id, paramValue = values));

                paramValueDAO.updateParamTreeCount(id, paramId, values);
            }
        }

        ParamChangedEvent changedEvent = new ParamChangedEvent(form, parameter, id, paramValue);
        EventProcessor.processEvent(changedEvent, new SingleConnectionSet(con));

        return json(con, form);
    }

    public ActionForward parameterListCountAddValue(DynActionForm form, ConnectionSet conSet) {
        return html(conSet, form, PATH_JSP + "/edit/listcount/value_row.jsp");
    }

    public ActionForward parameterPhoneAddValue(DynActionForm form, ConnectionSet conSet) {
        return html(conSet, form, PATH_JSP + "/edit/phone/value_row.jsp");
    }

    public ActionForward parameterTreeCountAddValue(DynActionForm form, ConnectionSet conSet) {
        return html(conSet, form, PATH_JSP + "/edit/treecount/value_row.jsp");
    }

    private void paramChangingProcess(Connection con, ParamChangingEvent event) throws Exception {
        EventProcessor.processEvent(event, new SingleConnectionSet(con));
    }
}
