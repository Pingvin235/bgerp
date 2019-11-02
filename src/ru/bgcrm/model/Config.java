package ru.bgcrm.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ru.bgcrm.dao.ConfigDAO;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.Utils;

public class Config implements LastModifySupport {
    private static final Logger log = Logger.getLogger(Config.class);
    
    public static final String INCLUDE_PREFIX = "include.";
    
    private int id = -1;
    private int userId = -1;
    private boolean active = false;
    private Date date;
    private String data;
    private String title;
    /** List of included configurations. */
    private List<Config> includedList;
    private ParameterMap valueMap = new Preferences();
    private LastModify lastModify = new LastModify();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
        this.valueMap = new Preferences(data);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public void addIncluded(Config included) {
        if (includedList == null) 
            includedList = new ArrayList<>();
        includedList.add(included);
    }

    public List<Config> getIncludedList() {
        return includedList;
    }

    public ParameterMap getValueMap() {
        return valueMap;
    }

    public LastModify getLastModify() {
        return lastModify;
    }

    public void setLastModify(LastModify lastModify) {
        this.lastModify = lastModify;
    }
    
    /**
     * Выбирает инклуды из глобальных конфигурации вида include.<id>=1.
     *  
     * @param configDao получение конфигураций.
     * @param data конфигурация, где содержатся инклуды.
     * @param validate выброс исключения при не найденной конфигурации.
     * 
     * @throws Exception
     */
    public static Iterable<ParameterMap> getIncludes(ConfigDAO configDao, ParameterMap data, boolean validate) throws Exception {
        List<ParameterMap> result = new ArrayList<>();
        
        for (Map.Entry<String, String> me : data.sub(Config.INCLUDE_PREFIX).entrySet()) {
            int configId = Utils.parseInt(me.getKey());

            boolean load = Utils.parseBoolean(me.getValue());
            if (load && configId > 0) {
                Config config = configDao.getGlobalConfig(configId);
                if (config == null) {
                    String message = "Not found included config: " + configId;
                    if (validate) throw new BGMessageException(message);
                    log.error(message);
                    continue;
                }
                result.add(new Preferences(config.getData()));
            }
        }
        
        return result;
    }
}