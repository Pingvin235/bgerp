package org.bgerp.action.admin;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.cache.ParameterCache;
import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.dao.param.ParamDAO;
import org.bgerp.dao.param.ParamGroupDAO;
import org.bgerp.model.Pageable;
import org.bgerp.model.base.IdStringTitle;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.Dynamic;
import org.bgerp.util.sql.LikePattern;

import ru.bgcrm.dao.PatternDAO;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.ParameterGroup;
import ru.bgcrm.model.param.Pattern;
import ru.bgcrm.model.param.address.AddressCity;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.model.param.address.AddressStreet;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.admin.ProcessAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

@Action(path = "/admin/directory", pathId = true)
public class DirectoryAction extends BaseAction {
    public static final class Directory extends IdStringTitle {
        private final String action;
        private final String objectType;

        public Directory(String id, String title, String action, String objectType) {
            super(id, title);
            this.action = action;
            this.objectType = objectType;
        }

        @Dynamic
        public String getAction() {
            return action;
        }
    }

    private static final String PATH_JSP = PATH_JSP_ADMIN + "/directory";

    private static final List<Directory> DIRECTORY_LIST = List.of(
        new Directory("processParameter", "Process parameters", "parameterList", Process.OBJECT_TYPE),
        new Directory("userParameter", "User parameters", "parameterList", User.OBJECT_TYPE),
        new Directory("customerParameter", "Customer parameters", "parameterList", Customer.OBJECT_TYPE),
        new Directory("customerParameterGroup", "Customer parameters groups", "parameterGroupList", null),
        new Directory("customerPatternTitle", "Customer title patterns", "patternTitleList", null),
        new Directory("addressCityParameter", "City parameters", "parameterList", AddressCity.OBJECT_TYPE),
        new Directory("addressStreetParameter", "Street parameters", "parameterList", AddressStreet.OBJECT_TYPE),
        new Directory("addressHouseParameter", "House parameters", "parameterList", AddressHouse.OBJECT_TYPE)
    );

    private static final Map<String, Directory> DIRECTORY_MAP = Collections.unmodifiableMap(
        DIRECTORY_LIST.stream().collect(Collectors.toMap(d -> d.getId(), d -> d))
    );

    @Override
    public ActionForward unspecified(DynActionForm form, Connection con) throws Exception {
        form.setParam("directoryId", "processParameter");
        form.setParam(DynActionForm.PARAM_ACTION_METHOD, "parameterList");
        return parameterList(form, con);
    }

    public ActionForward parameterList(DynActionForm form, Connection con) throws Exception {
        var request = form.getHttpRequest();

        setDirectoryList(request);
        Pageable<Parameter> searchResult = new Pageable<>(form);

        new ParamDAO(con).searchParameter(searchResult, getObjectType(form.getParam("directoryId")),
                LikePattern.SUB.get(form.getParam("filter")), 0, null);

        return html(con, form, PATH_JSP + "/parameter/list.jsp");
    }

    public ActionForward parameterUseProcess(DynActionForm form, Connection con) throws Exception {
        Integer paramId = Utils.parseInt(form.getParam("parameterId"));
        List<String> containProcess = new ArrayList<>();
        Map<Integer, ProcessType> processTypeMap = ProcessTypeCache.getProcessTypeMap();

        for (int i = 0; i < processTypeMap.size(); i++) {
            ProcessType pType = (ProcessType) processTypeMap.values().toArray()[i];

            if (!pType.isUseParentProperties()) {
                List<Integer> parameters = pType.getProperties().getParameterIds();
                if (parameters.contains(paramId)) {
                    containProcess.add(pType.getTitle());
                }
            }
        }

        form.setResponseData("containProcess", containProcess);

        return html(con, form, ProcessAction.JSP_USED_IN_TYPES);
    }

    public ActionForward parameterGet(DynActionForm form, Connection con) throws Exception {
        ParamDAO paramDAO = new ParamDAO(con);

        Parameter parameter = paramDAO.getParameter(form.getId());
        if (parameter != null) {
            form.setResponseData("parameter", parameter);
        }

        var request = form.getHttpRequest();
        setDirectoryList(request);
        request.setAttribute("directoryTitle", DIRECTORY_MAP.get(form.getParam("directoryId")));
        request.setAttribute("types", Parameter.TYPES);

        return html(con, form, PATH_JSP + "/parameter/update.jsp");
    }

    public ActionForward parameterUpdate(DynActionForm form, Connection con) throws Exception {
        ParamDAO paramDAO = new ParamDAO(con);

        Parameter parameter = new Parameter();
        parameter.setId(form.getId());
        parameter.setObjectType(getObjectType(form.getParam("directoryId")));
        parameter.setType(form.getParam("type"));

        if (form.getId() > 0) {
            parameter = paramDAO.getParameter(form.getId());
        }

        parameter.setTitle(form.getParam("title"));
        parameter.setOrder(Utils.parseInt(form.getParam("order")));
        parameter.setConfig(form.getParam("config"));
        parameter.setComment(form.getParam("comment"));

        if (Set.of(Parameter.TYPE_LIST, Parameter.TYPE_LISTCOUNT, Parameter.TYPE_TREE, Parameter.TYPE_TREECOUNT).contains((parameter.getType())))
            parameter.setValuesConfig(form.getParam("listValues"));

        paramDAO.updateParameter(parameter);

        ParameterCache.flush(con);

        return json(con, form);
    }

    public ActionForward parameterDelete(DynActionForm form, Connection con) throws Exception {
        new ParamDAO(con).deleteParameter(form.getId());

        ParameterCache.flush(con);

        return json(con, form);
    }

    // шаблоны названия
    public ActionForward patternTitleList(DynActionForm form, Connection con) throws Exception {
        PatternDAO patternDAO = new PatternDAO(con);
        var request = form.getHttpRequest();

        String objectType = getObjectType(form.getParam("directoryId"));
        setDirectoryList(request);
        request.setAttribute("patternList", patternDAO.getPatternList(objectType));

        return html(con, form, PATH_JSP + "/pattern/list.jsp");
    }

    public ActionForward patternTitleGet(DynActionForm form, Connection con) throws Exception {
        Pattern pattern = new PatternDAO(con).getPattern(form.getId());
        if (pattern != null) {
            form.setResponseData("pattern", pattern);
        }

        var request = form.getHttpRequest();
        setDirectoryList(request);
        request.setAttribute("directoryTitle", DIRECTORY_MAP.get(form.getParam("directoryId")));

        return html(con, form, PATH_JSP + "/pattern/update.jsp");
    }

    public ActionForward patternTitleUpdate(DynActionForm form, Connection con) throws Exception {
        PatternDAO paramDAO = new PatternDAO(con);

        Pattern pattern = new Pattern();
        pattern.setId(form.getId());
        pattern.setObject(getObjectType(form.getParam("directoryId")));
        pattern.setTitle(form.getParam("title"));
        pattern.setPattern(form.getParam("pattern"));
        paramDAO.updatePattern(pattern);

        return json(con, form);
    }

    public ActionForward patternTitleDelete(DynActionForm form, Connection con) throws Exception {
        new PatternDAO(con).deletePattern(form.getId());

        return json(con, form);
    }

    // группы параметров
    public ActionForward parameterGroupList(DynActionForm form, Connection con) throws Exception {
        ParamGroupDAO paramGroupDAO = new ParamGroupDAO(con);

        var request = form.getHttpRequest();
        setDirectoryList(request);
        request.setAttribute("parameterList", paramGroupDAO.getParameterGroupList(getObjectType(form.getParam("directoryId"))));

        return html(con, form, PATH_JSP + "/parameter/group/list.jsp");
    }

    //переписать "!"
    public ActionForward parameterGroupGet(DynActionForm form, Connection con) throws Exception {
        int id = form.getId();

        ParamDAO paramDAO = new ParamDAO(con);
        ParamGroupDAO paramGroupDAO = new ParamGroupDAO(con);

        ParameterGroup parameterGroup = paramGroupDAO.getParameterGroup(id);
        if (parameterGroup != null) {
            parameterGroup.setParameterIds(paramGroupDAO.getParameterIdsForGroup(id));
            form.setResponseData("group", parameterGroup);
        }

        var request = form.getHttpRequest();
        setDirectoryList(request);
        request.setAttribute("parameterList", paramDAO.getParameterList(getObjectType(form.getParam("directoryId")), 0)); //!!!
        request.setAttribute("directoryTitle", DIRECTORY_MAP.get(form.getParam("directoryId")));

        return html(con, form, PATH_JSP + "/parameter/group/update.jsp");
    }

    public ActionForward parameterGroupUpdate(DynActionForm form, Connection con) throws Exception {
        ParamGroupDAO paramGroupDAO = new ParamGroupDAO(con);

        ParameterGroup parameterGroup = new ParameterGroup();
        parameterGroup.setId(form.getId());
        parameterGroup.setObject(getObjectType(form.getParam("directoryId")));
        parameterGroup.setTitle(form.getParam("title"));
        parameterGroup.setParameterIds(form.getParamValues("param"));
        paramGroupDAO.updateParameterGroup(parameterGroup);

        ParameterCache.flush(con);

        return json(con, form);
    }

    public ActionForward parameterGroupDelete(DynActionForm form, Connection con) throws Exception {
        new ParamGroupDAO(con).deleteParameterGroup(form.getId());

        ParameterCache.flush(con);

        return json(con, form);
    }

    private void setDirectoryList(HttpServletRequest request) {
        request.setAttribute("directoryList", DIRECTORY_LIST);
    }

    private String getObjectType(String directoryId) {
        var directory = DIRECTORY_MAP.get(directoryId);
        if (directory != null)
            return directory.objectType;

        return null;
    }
}
