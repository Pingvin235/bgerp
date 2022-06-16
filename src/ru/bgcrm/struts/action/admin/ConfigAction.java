package ru.bgcrm.struts.action.admin;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.struts.action.ActionForward;
import org.bgerp.model.Pageable;
import org.bgerp.util.lic.AppLicense;
import org.bgerp.util.lic.License;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.ConfigDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.SetupChangedEvent;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.Config;
import ru.bgcrm.model.Page;
import ru.bgcrm.plugin.PluginManager;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/admin/config")
public class ConfigAction extends BaseAction {
    private static final String PATH_JSP = PATH_JSP_ADMIN + "/config";

    public ActionForward list(DynActionForm form, ConnectionSet conSet) throws Exception {
        Set<Integer> allowedConfigIds = Utils.toIntegerSet(form.getPermission().get("allowedConfigIds"));
        String filter = CommonDAO.getLikePatternSub(form.getParam("filter"));

        Pageable<Config> result = new Pageable<Config>(form);
        result.getPage().setPageIndex(Page.PAGE_INDEX_NO_PAGING);
        List<Config> resultList = result.getList();

        new ConfigDAO(conSet.getConnection()).searchGlobalConfigList(result, allowedConfigIds, filter);
        Map<Integer, Config> configMap = resultList.stream().collect(Collectors.toMap(Config::getId, c -> c));

        for (Config config : new ArrayList<>(resultList)) {
            if (config.getParentId() <= 0)
                continue;

            Config parent = configMap.get(config.getParentId());
            if (parent == null) {
                log.warn("Not found parent config with ID: {}", config.getParentId());
                continue;
            }

            resultList.remove(config);
            parent.addIncluded(config);
        }

        form.getHttpRequest().setAttribute("license", AppLicense.instance());

        return html(conSet, form, PATH_JSP + "/list.jsp");
    }

    public ActionForward delete(DynActionForm form, ConnectionSet conSet) throws Exception {
        new ConfigDAO(conSet.getConnection()).deleteGlobalConfig(form.getId());

        return json(conSet, form);
    }

    public ActionForward get(DynActionForm form, ConnectionSet conSet) throws Exception {
        checkAllowedConfigIds(form);

        Config config = new ConfigDAO(conSet.getConnection()).getGlobalConfig(form.getId());
        if (config != null)
            form.getResponse().setData("config", config);

        return html(conSet, form, PATH_JSP + "/update.jsp");
    }

    public ActionForward update(DynActionForm form, ConnectionSet conSet) throws Exception {
        checkAllowedConfigIds(form);

        ConfigDAO configDAO = new ConfigDAO(conSet.getConnection());

        int id = form.getId();

        Config config = new Config();
        if (id > 0)
            config = configDAO.getGlobalConfig(form.getId());

        if (config == null)
            throw new BGMessageException("Конфигурация не найдена.");

        config.setData(form.getParam("data", ""));
        config.setTitle(form.getParam("title"));
        config.setActive(Utils.parseBoolean(form.getParam("active")));

        boolean activeAllow = form.getPermission().getBoolean("activeAllow", true);
        if (!activeAllow && config.isActive())
            throw new BGMessageException("Запрещено делать конфигурацию активной.");

        if (Utils.isBlankString(config.getTitle()))
            throw new BGMessageException("Не указано название.");

        Preferences.processIncludes(configDAO, config.getData(), true);

        checkModified(config.getLastModify(), form);
        configDAO.updateGlobalConfig(config);

        if (config.isActive())
            configDAO.setActiveGlobalConfig(config.getId());

        EventProcessor.processEvent(new SetupChangedEvent(form), conSet);

        form.setResponseData("config", config);

        return json(conSet, form);
    }

    public ActionForward addIncluded(DynActionForm form, ConnectionSet conSet) throws Exception {
        String pluginId = form.getParam("pluginId");

        var config = new Config();
        config.setParentId(form.getId());

        if (Utils.notBlankString(pluginId)) {
            var plugin = PluginManager.getInstance().getFullPluginMap().get(pluginId);
            config.setData(plugin.getId() + ":enable=1\n");
            config.setTitle("Plugin " + plugin.getTitle());
        } else {
            config.setTitle(l.l("Новая конфигурация"));
        }

        var dao = new ConfigDAO(conSet.getConnection());
        checkModified(config.getLastModify(), form);
        dao.updateGlobalConfig(config);

        EventProcessor.processEvent(new SetupChangedEvent(form), conSet);

        return json(conSet, form);
    }

    public void checkAllowedConfigIds(DynActionForm form) throws BGMessageException {
        Set<Integer> allowedConfigIds = Utils.toIntegerSet(form.getPermission().get("allowedConfigIds"));
        if (CollectionUtils.isNotEmpty(allowedConfigIds) && !allowedConfigIds.contains(form.getId())) {
            throw new BGMessageException("Работа с данной конфигурацией запрещена");
        }
    }

    public ActionForward licenseUpload(DynActionForm form, ConnectionSet conSet) throws Exception {
        var file = form.getFile();

        byte[] data = IOUtils.toByteArray(file.getInputStream());

        String error = new License(new String(data, StandardCharsets.UTF_8)).getError();
        if (Utils.notBlankString(error))
            throw new BGMessageException(error);

        IOUtils.write(data, new FileOutputStream(License.FILE_NAME));
        AppLicense.init();

        return json(conSet, form);
    }
}