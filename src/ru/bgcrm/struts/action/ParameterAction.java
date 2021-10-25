package ru.bgcrm.struts.action;

import static ru.bgcrm.dao.Tables.TABLE_CUSTOMER_LOG;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForward;
import org.apache.struts.upload.FormFile;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.AddressDAO;
import ru.bgcrm.dao.EntityLogDAO;
import ru.bgcrm.dao.FileDataDAO;
import ru.bgcrm.dao.ParamLogDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.expression.ParamValueFunction;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.Tables;
import ru.bgcrm.event.DateChangingEvent;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.ParamChangedEvent;
import ru.bgcrm.event.ParamChangingEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.FileData;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.IdTitleTree;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.JumpRegexp;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.param.ParameterListCountValue;
import ru.bgcrm.model.param.ParameterLogItem;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterPhoneValueItem;
import ru.bgcrm.model.param.ParameterValuePair;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.model.param.config.ListParamConfig;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.struts.form.Response;
import ru.bgcrm.util.AddressUtils;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SingleConnectionConnectionSet;

@Action(path = "/user/parameter")
public class ParameterAction extends BaseAction {
    protected static final String PATH_JSP = PATH_JSP_USER + "/parameter";

    public ActionForward parameterLog(DynActionForm form, ConnectionSet conSet) throws BGException {
        int id = form.getId();
        String objectType = form.getParam("objectType");
        List<Parameter> paramList = ParameterCache.getObjectTypeParameterList(objectType);
        boolean offEncrypt = form.getPermission().getBoolean("offEncrypt", false);
        form.setResponseData("log", new ParamLogDAO(conSet.getSlaveConnection()).getHistory(id, paramList,
                offEncrypt, new SearchResult<ParameterLogItem>(form)));

        return html(conSet, form, PATH_JSP + "/log.jsp");
    }

    // Used for customers, but not for processes.
    public ActionForward entityLog(DynActionForm form, ConnectionSet con) throws BGException {
        int id = form.getId();
        String type = form.getParam("type");
        String table = "";

        if (type.equals(Process.OBJECT_TYPE)) {
            table = Tables.TABLE_PROCESS_LOG;
        } else if (type.equals(Customer.OBJECT_TYPE)) {
            table = TABLE_CUSTOMER_LOG;
        }

        form.setResponseData("log", new EntityLogDAO(con.getSlaveConnection(), table).getHistory(id));

        return html(con, form, BaseAction.PATH_JSP + "/entity_log.jsp");
    }

    public ActionForward parameterList(DynActionForm form, Connection con) throws Exception {
        parameterListInternal(form, con);
        return html(con, form, PATH_JSP + "/list.jsp");
    }

    protected void parameterListInternal(DynActionForm form, Connection con) throws Exception {
        int id = form.getId();
        String objectType = form.getParam("objectType");
        int parameterGroupId = form.getParamInt("parameterGroup", -1); // doesn't work with 0!!
        List<Integer> pids = form.getSelectedValuesList("paramId");

        List<Parameter> paramList = null;
        if (pids.size() > 0) {
            paramList = ParameterCache.getParameterList(pids);
        } else {
            paramList = ParameterCache.getObjectTypeParameterList(objectType, parameterGroupId);
        }

        Set<Integer> hideParamIds = Collections.emptySet();

        if ("process".equals(objectType)) {
            Process process = new ProcessDAO(con).getProcess(id);
            if (process == null) {
                throw new BGException("Process not found: " + id);
            }

            ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());

            hideParamIds = Utils.toIntegerSet(type.getProperties().getConfigMap().get("hideParamIdsInStatus." + process.getStatusId(), ""));
            if (pids.size() == 0) {
                paramList = Utils.getObjectList(ParameterCache.getParameterMap(), type.getProperties().getParameterIds());
            }

            // показывает параметры процесса только в том случае, если выполняется JEXL выражение: showParam.<paramId>.checkExpression=<expr>
            for (Entry<Integer, ParameterMap> entry : type.getProperties().getConfigMap().subIndexed("showParam.").entrySet()) {
                String expression = entry.getValue().get(Expression.CHECK_EXPRESSION_CONFIG_KEY);

                Map<String, Object> context = new HashMap<String, Object>();
                context.put(User.OBJECT_TYPE, form.getUser());
                context.put(Process.OBJECT_TYPE, process);
                context.put(Process.OBJECT_TYPE + ParamValueFunction.PARAM_FUNCTION_SUFFIX, new ParamValueFunction(con, process.getId()));
                // TODO: Use DefaultProcessChangeListener#initExpression()
                if (Utils.notBlankString(expression) && !(new Expression(context).check(expression))) {
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

        List<ParameterValuePair> parameterValuePairList = new ParamValueDAO(con).loadParameters(paramList, id, offEncryption);

        /* Strange logic, commented out 10.07.21
        for (ParameterValuePair pvp : parameterValuePairList) {
            Parameter parameter = pvp.getParameter();

            if (Utils.parseBoolean(parameter.getConfigMap().get("throwParamListShowListEvent"), false)) {
                EventProcessor.processEvent(new ParamListShowListEvent(form, pvp, id, objectType), parameter.getScript(),
                        new SingleConnectionConnectionSet(con));

                if (Process.OBJECT_TYPE.equals(objectType)) {
                    Process process = new ProcessDAO(con).getProcess(id);
                    ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
                    EventProcessor.processEvent(new ParamListShowListEvent(form, pvp, id, objectType), type.getProperties().getActualScriptName(),
                            new SingleConnectionConnectionSet(con));
                }
            }
        }*/

        form.getResponse().setData("list", parameterValuePairList);
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

        if (Parameter.TYPE_LIST.equals(parameter.getType())) {
            Map<Integer, String> values = paramDAO.getParamListWithComments(id, paramId);
            resp.setData("value", values);

            Parameter param = ParameterCache.getParameter(paramId);
            List<IdTitle> listValues = new ArrayList<IdTitle>(ParameterCache.getListParamValues(param));

            String valuesSortMode = parameter.getConfigMap().get("sort.mode", "");
            if (valuesSortMode.equals("byTitle")) {
                Collections.sort(listValues, new Comparator<IdTitle>() {
                    public int compare(IdTitle first, IdTitle second) {
                        return first.getTitle().compareTo(second.getTitle());
                    }
                });
            }

            /* Strange logic, commented out 10.07.21
            String objectClassName = "";
            if (Process.OBJECT_TYPE.equals(parameter.getObject())) {
                Process process = new ProcessDAO(connectionSet.getConnection()).getProcess(id);
                ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
                objectClassName = type.getProperties().getActualScriptName();
            }

            EventProcessor.processEvent(new ParamListGetEvent(form, "process", id, param, values, listValues), param.getScript(), connectionSet);
            EventProcessor.processEvent(new ParamListGetEvent(form, "process", id, param, values, listValues), objectClassName, connectionSet); */

            request.setAttribute("listValues", listValues);
            // для сторонних систем - значения спискового параметра
            resp.setData("listValues", listValues);
        } else if (Parameter.TYPE_LISTCOUNT.equals(parameter.getType())) {
            Map<Integer, ParameterListCountValue> values = paramDAO.getParamListCount(id, paramId);
            resp.setData("value", values);

            Parameter param = ParameterCache.getParameter(paramId);
            List<IdTitle> listValues = ParameterCache.getListParamValues(param);

            request.setAttribute("listValues", listValues);
            // для сторонних систем - значения спискового параметра
            resp.setData("listValues", listValues);
        } else if (Parameter.TYPE_TREE.equals(parameter.getType())) {
            Set<String> values = paramDAO.getParamTree(id, paramId);
            resp.setData("value", values);

            Parameter param = ParameterCache.getParameter(paramId);
            IdTitleTree treeValues = ParameterCache.getTreeParamValues(param);

            request.setAttribute("treeValues", treeValues);
            // для сторонних систем - значения спискового параметра
            resp.setData("treeValues", treeValues);
        } else if (Parameter.TYPE_PHONE.equals(parameter.getType())) {
            ParameterPhoneValue phoneValue = paramDAO.getParamPhone(id, paramId);
            if (phoneValue != null) {
                List<ParameterPhoneValueItem> itemList = phoneValue.getItemList();

                int i = 1;
                for (ParameterPhoneValueItem item : itemList) {
                    resp.setData("parts" + i, item.getPhoneParts());
                    resp.setData("comment" + i, item.getComment());
                    i++;
                }
            }
        } else if (Parameter.TYPE_FILE.equals(parameter.getType())) {
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
        } else if (Parameter.TYPE_EMAIL.equals(parameter.getType())) {
            ParameterEmailValue emailValue = paramDAO.getParamEmail(id, paramId, form.getParamInt("position", -1));

            if (emailValue != null) {
                resp.setData("email", emailValue);
                resp.setData("parameter", parameter);
            }
        } else if (Parameter.TYPE_ADDRESS.equals(parameter.getType())) {
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
        } else if (Parameter.TYPE_DATETIME.equals(parameter.getType()) || Parameter.TYPE_DATE.equals(parameter.getType())) {
            if (Utils.notBlankString(form.getParam("newDate"))) {
                EventProcessor.processEvent(
                        new DateChangingEvent(form, id, parameter, TimeUtils.parse(form.getParam("newDate"), TimeUtils.FORMAT_TYPE_YMD)),
                        parameter.getScript(), conSet);

                String objectClassName = "";
                if (Process.OBJECT_TYPE.equals(parameter.getObject())) {
                    Process process = new ProcessDAO(conSet.getConnection()).getProcess(id);
                    ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
                    objectClassName = type.getProperties().getActualScriptName();
                }
                EventProcessor.processEvent(
                        new DateChangingEvent(form, id, parameter, TimeUtils.parse(form.getParam("newDate"), TimeUtils.FORMAT_TYPE_YMD)),
                        objectClassName, conSet);
            }

            if (Parameter.TYPE_DATE.equals(parameter.getType())) {
                resp.setData("value", paramDAO.getParamDate(id, paramId));
            } else if (Parameter.TYPE_DATETIME.equals(parameter.getType())) {
                resp.setData("value", paramDAO.getParamDateTime(id, paramId));
            }
        } else if (Parameter.TYPE_TEXT.equals(parameter.getType())) {
            resp.setData("value", paramDAO.getParamText(id, paramId));
        } else if (Parameter.TYPE_BLOB.equals(parameter.getType())) {
            resp.setData("value", paramDAO.getParamBlob(id, paramId));
        }

        Collection<ParameterMap> par = setup.subIndexed("param.phone.part.2.jumpRegexp.").values();
        List<JumpRegexp> regexpList = new ArrayList<JumpRegexp>();

        for (ParameterMap item : par) {
            regexpList.add(new JumpRegexp(item.get("regexp"), Utils.parseBoolean(item.get("moveLastChars"))));
        }

        request.setAttribute("part2Rules", regexpList);

        return html(conSet, form, PATH_JSP + "/edit.jsp");
    }

    public ActionForward parameterUpdate(DynActionForm form, Connection con) throws Exception {
        int id = form.getId();
        int paramId = Utils.parseInt(form.getParam("paramId"));
        int userId = form.getUserId();

        Set<Integer> restrictedParamIds = Utils.toIntegerSet(form.getPermission().get("restrictedParameterIds"));
        Set<Integer> allowedParamIds = Utils.toIntegerSet(form.getPermission().get("parameterIds"));

        if (!restrictedParamIds.isEmpty() && restrictedParamIds.contains(paramId)) {
            throw new BGMessageException("Параметр с кодом " + paramId + " запрещен для изменения.");
        } else if (!allowedParamIds.isEmpty() && !allowedParamIds.contains(paramId)) {
            throw new BGMessageException("Параметр с кодом " + paramId + " запрещен для изменения.");
        }

        ParamValueDAO paramValueDAO = new ParamValueDAO(con, true, userId);

        Parameter parameter = ParameterCache.getParameter(paramId);
        if (parameter == null) {
            throw new BGMessageException("Параметр не найден.");
        }

        // проверка тегов
        Set<String> tags = Utils.toSet(parameter.getConfigMap().get("tagsUpdate", parameter.getConfigMap().get("tags")));
        if (tags.size() > 0) {
            Set<String> allowedTags = Utils.toSet(form.getPermission().get("tags"));
            if (CollectionUtils.intersection(tags, allowedTags).size() == 0) {
                throw new BGMessageException("Данный тегированный параметр запрещён для правки.");
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
        String className = parameter.getScript();
        String objectClassName = "";
        if (Process.OBJECT_TYPE.equals(parameter.getObject())) {
            Process process = new ProcessDAO(con).getProcess(id);
            ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
            objectClassName = type.getProperties().getActualScriptName();
        }

        if (Parameter.TYPE_TEXT.equals(parameter.getType())) {
            paramValue = form.getParam("value");
            paramChangingProcess(con, className, objectClassName, new ParamChangingEvent(form, parameter, id, paramValue));
            paramValueDAO.updateParamText(id, paramId, (String) paramValue);
        } else if (Parameter.TYPE_BLOB.equals(parameter.getType())) {
            paramValue = form.getParam("value");
            paramChangingProcess(con, className, objectClassName, new ParamChangingEvent(form, parameter, id, paramValue));
            paramValueDAO.updateParamBlob(id, paramId, (String) paramValue);
        } else if (Parameter.TYPE_DATE.equals(parameter.getType())) {
            String value = form.getParam("value");

            paramValue = TimeUtils.parse(value, TimeUtils.FORMAT_TYPE_YMD);
            if (Utils.notBlankString(value) &&
            // при годе большим 4х знаков MySQL сохраняет нули и потом выдаёт ошибку при выборке
                    (paramValue == null || TimeUtils.convertDateToCalendar((Date) paramValue).get(Calendar.YEAR) >= 10000)) {
                throw new BGMessageException("Неверный формат.");
            }

            paramChangingProcess(con, className, objectClassName, new ParamChangingEvent(form, parameter, id, paramValue));

            paramValueDAO.updateParamDate(id, paramId, (Date) paramValue);
        } else if (Parameter.TYPE_DATETIME.equals(parameter.getType())) {
            String value = form.getParam("value");

            paramValue = TimeUtils.parse(value, parameter.getConfigMap().get("type", TimeUtils.FORMAT_TYPE_YMD));
            if (Utils.notBlankString(value) &&
            // при годе большим 4х знаков MySQL сохраняет нули и потом выдаёт ошибку при выборке
                    (paramValue == null || TimeUtils.convertDateToCalendar((Date) paramValue).get(Calendar.YEAR) >= 10000)) {
                throw new BGMessageException("Неверный формат.");
            }

            paramChangingProcess(con, className, objectClassName, new ParamChangingEvent(form, parameter, id, paramValue));

            paramValueDAO.updateParamDateTime(id, paramId, (Date) paramValue);
        } else if (Parameter.TYPE_LIST.equals(parameter.getType())) {
            ListParamConfig config = parameter.getConfigMap().getConfig(ListParamConfig.class);

            Map<Integer, String> values = new HashMap<Integer, String>();

            for (String value : form.getSelectedValuesStr("value")) {
                int val = Utils.parseInt(StringUtils.substringBefore(value, ":"));
                String comment = Utils.maskNull(StringUtils.substringAfter(value, ":"));

                if (val <= 0)
                    continue;

                if (config.getCommentValues().get(val) == null)
                    comment = "";
                else if (config.getNeedCommentValues().get(val) != null && Utils.isBlankString(comment))
                    throw new BGMessageException("Не указан обязательный комментарий к значению");

                values.put(val, comment);
            }

            paramChangingProcess(con, className, objectClassName, new ParamChangingEvent(form, parameter, id, paramValue = values.keySet()));

            paramValueDAO.updateParamList(id, paramId, values);
        } else if (Parameter.TYPE_LISTCOUNT.equals(parameter.getType())) {
            Set<String> formValues = form.getSelectedValuesStr("value");
            Map<Integer, BigDecimal> values = new HashMap<>();

            for (String val : formValues) {
                String[] valCount = val.split(":");
                if (valCount.length >= 2) {
                    Integer itemId = Utils.parseInt(valCount[0]);
                    BigDecimal itemCount = Utils.parseBigDecimal(valCount[1]);
                    values.put(itemId, itemCount);
                }
            }

            paramChangingProcess(con, className, objectClassName, new ParamChangingEvent(form, parameter, id, paramValue = values));

            paramValueDAO.updateParamListCount(id, paramId, values);
        } else if (Parameter.TYPE_TREE.equals(parameter.getType())) {
            Set<String> values = form.getSelectedValuesStr("value");
            // TODO: Попробовать убрать, проверить.
            values.removeAll(Arrays.asList("0", "-1"));

            paramChangingProcess(con, className, objectClassName, new ParamChangingEvent(form, parameter, id, paramValue = values));

            paramValueDAO.updateParamTree(id, paramId, values);
        } else if (Parameter.TYPE_FILE.equals(parameter.getType())) {
            FormFile file = form.getFile();

            int position = form.getParamInt("position", 0);

            FileData fileData = null;
            if (file != null) {
                log.debug("Uploading file: {}, type: {}" +  file.getFileName(), file.getContentType());

                fileData = new FileData();

                FileDataDAO fileDataDAO = new FileDataDAO(con);

                fileData.setTitle(file.getFileName());
                try (var fos = fileDataDAO.add(fileData)) {
                    fos.write(file.getFileData());
                }
            }

            paramChangingProcess(con, className, objectClassName, new ParamChangingEvent(form, parameter, id, paramValue = fileData));

            paramValueDAO.updateParamFile(id, paramId, position, form.getParam("comment"), fileData);
        } else if (Parameter.TYPE_PHONE.equals(parameter.getType())) {
            ParameterPhoneValue phoneValue = new ParameterPhoneValue();

            int paramCount = setup.getInt("param.phone.item.count", 4);
            List<ParameterPhoneValueItem> items = new ArrayList<ParameterPhoneValueItem>();
            for (int index = 1; index <= paramCount; index++) {
                ParameterPhoneValueItem item = new ParameterPhoneValueItem();
                String phonePart = null;
                StringBuilder phone = new StringBuilder();
                StringBuilder format = new StringBuilder("");
                phonePart = form.getParam("part1" + index);
                phone.append(phonePart);
                format.append(phonePart.length());
                phonePart = form.getParam("part2" + index);
                phone.append(phonePart);
                format.append(phonePart.length());
                phonePart = form.getParam("part3" + index);
                phone.append(phonePart);
                item.setPhone(phone.toString());
                item.setFormat(format.toString());
                item.setComment(form.getParam("comment" + index));

                if (Utils.isBlankString(item.getPhone()))
                    continue;

                items.add(item);
            }
            phoneValue.setItemList(items);

            paramChangingProcess(con, className, objectClassName, new ParamChangingEvent(form, parameter, id, paramValue = phoneValue));

            paramValueDAO.updateParamPhone(id, paramId, phoneValue);
        } else if (Parameter.TYPE_EMAIL.equals(parameter.getType())) {
            int position = form.getParamInt("position", 0);
            String value = form.getParam("value", "");

            ParameterEmailValue emailValue = null;

            if (Utils.notBlankString(value))
                emailValue = new ParameterEmailValue(value, form.getParam("comment", ""));

            paramChangingProcess(con, className, objectClassName, new ParamChangingEvent(form, parameter, id, paramValue = emailValue));

            paramValueDAO.updateParamEmail(id, paramId, position, emailValue);
        } else if (Parameter.TYPE_ADDRESS.equals(parameter.getType())) {
            ParameterAddressValue addressValue = null;
            int houseId = form.getParamInt("houseId", -1);

            // сначала пытаемся найти hid по улице и строке house (не по houseId)
            int streetId = form.getParamInt("streetId", -1);
            String house = form.getParam("house");

            int position = Utils.parseInt(form.getParam("position"));

            if (houseId <= 0 && (streetId != -1 && house != null)) {
                AddressDAO addressDAO = new AddressDAO(con);
                List<Integer> houses = addressDAO.getHouseIdsByStreetAndHouse(streetId, house, null);

                if (houses.size() == 1) {
                    if (houses.get(0) != houseId) {
                        houseId = houses.get(0);
                    }
                } else
                    throw new BGMessageException(
                            "Не удалось найти дом с таким номером, либо таких домов несколько. Выберите дом из всплывающей подсказки!");
            }

            if (houseId > 0) {
                addressValue = new ParameterAddressValue();

                addressValue.setHouseId(houseId);

                if (addressValue.getHouseId() <= 0)
                    throw new BGMessageException("Не выбран дом.");

                addressValue.setFlat(form.getParam("flat", ""));
                addressValue.setRoom(form.getParam("room", ""));
                addressValue.setPod(Utils.parseInt(form.getParam("pod"), -1));
                addressValue.setFloor(Utils.parseInt(form.getParam("floor"), -1));
                addressValue.setComment(form.getParam("comment", ""));
                addressValue.setValue(AddressUtils.buildAddressValue(addressValue, con));
            }

            //TODO: Возможно, позицию адреса нужно вставить в ParameterAddressValue
            paramChangingProcess(con, className, objectClassName, new ParamChangingEvent(form, parameter, id, paramValue = addressValue));

            paramValueDAO.updateParamAddress(id, paramId, position, addressValue);
        }

        // событие о изменении параметра
        ParamChangedEvent changedEvent = new ParamChangedEvent(form, parameter, id, paramValue);
        EventProcessor.processEvent(changedEvent, className, new SingleConnectionConnectionSet(con));
        EventProcessor.processEvent(changedEvent, objectClassName, new SingleConnectionConnectionSet(con), false);

        return json(con, form);
    }

    private void paramChangingProcess(Connection con, String className, String objectClassName, ParamChangingEvent event) throws Exception {
        EventProcessor.processEvent(event, className, new SingleConnectionConnectionSet(con));
        EventProcessor.processEvent(event, objectClassName, new SingleConnectionConnectionSet(con), false);
    }
}
