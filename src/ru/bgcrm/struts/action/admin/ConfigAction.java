package ru.bgcrm.struts.action.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.ConfigDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.SetupChangedEvent;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.Config;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class ConfigAction extends BaseAction {
    
    public ActionForward list(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        Set<Integer> allowedConfigIds = Utils.toIntegerSet(form.getPermission().get("allowedConfigIds"));
        String filter = CommonDAO.getLikePatternSub(form.getParam("filter"));
        
        SearchResult<Config> result = new SearchResult<Config>(form);
        List<Config> resultList = result.getList();
        
        new ConfigDAO(conSet.getConnection()).searchGlobalConfigList(result, allowedConfigIds, filter);
        Map<Integer, Config> configMap = resultList.stream().collect(Collectors.toMap(Config::getId, c -> c));
        
        for (Config config : new ArrayList<>(resultList)) {
            for (Map.Entry<String, String> me : config.getValueMap().sub(Config.INCLUDE_PREFIX).entrySet()) {
                int configId = Utils.parseInt(me.getKey());
                
                Config included = configMap.get(configId);
                if (included == null) continue;

                resultList.remove(included);
                config.addIncluded(included);
            }
        }

        return data(conSet, mapping, form, "list");
    }

    public ActionForward delete(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        new ConfigDAO(conSet.getConnection()).deleteGlobalConfig(form.getId());

        return status(conSet, form);
    }

    public ActionForward get(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        checkAllowedConfigIds(form);

        Config config = new ConfigDAO(conSet.getConnection()).getGlobalConfig(form.getId());
        if (config != null)
            form.getResponse().setData("config", config);

        return data(conSet, mapping, form, "update");
    }

    public ActionForward update(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
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

        form.getResponse().setData("config", config);

        return status(conSet, form);
    }

    public void checkAllowedConfigIds(DynActionForm form) throws BGMessageException {
        Set<Integer> allowedConfigIds = Utils.toIntegerSet(form.getPermission().get("allowedConfigIds"));
        if (CollectionUtils.isNotEmpty(allowedConfigIds) && !allowedConfigIds.contains(form.getId())) {
            throw new BGMessageException("Работа с данной конфигурацией запрещена.");
        }
    }
}