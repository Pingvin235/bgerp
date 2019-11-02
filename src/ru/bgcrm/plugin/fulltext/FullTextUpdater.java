package ru.bgcrm.plugin.fulltext;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.model.Customer;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterPhoneValueItem;
import ru.bgcrm.model.param.ParameterValuePair;
import ru.bgcrm.plugin.fulltext.dao.SearchDAO;
import ru.bgcrm.plugin.fulltext.model.Config;
import ru.bgcrm.plugin.fulltext.model.Config.ObjectType;
import ru.bgcrm.plugin.fulltext.model.SearchItem;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.sql.SQLUtils;

/**
 * Задача обновления полнотекстовых индексов. Запускается планировщиком раз в минуту.
 */
public class FullTextUpdater implements Runnable {
    
    private static final Logger log = Logger.getLogger(FullTextUpdater.class); 
    
    private final Config config = Setup.getSetup().getConfig(Config.class); 
    
    public FullTextUpdater() {}

    @Override
    public void run() {
        Connection con = null;
        try {
            con = Setup.getSetup().getDBConnectionFromPool();

            SearchDAO searchDao = new SearchDAO(con);
            ParamValueDAO paramDao = new ParamValueDAO(con);
            
            List<SearchItem> forUpdate = null; 
            while (!(forUpdate = searchDao.getScheduledUpdates(config.getIndexDelay(), 100)).isEmpty()) {
                for (SearchItem item : forUpdate) {
                    ObjectType typeConfig = config.getObjectTypeMap().get(item.getObjectType());
                    if (typeConfig == null) {
                        log.warn("Unconfigured object type: " + item.getObjectType());
                        searchDao.delete(item.getObjectType(), item.getObjectId());
                        continue;
                    }
                    
                    StringBuilder text = new StringBuilder(200);
                    if (Customer.OBJECT_TYPE.equals(item.getObjectType())) {
                        Customer customer = new CustomerDAO(con).getCustomerById(item.getObjectId());
                        if (customer == null) {
                            log.warn("Customer not found: " + item.getObjectId());
                            searchDao.delete(item.getObjectType(), item.getObjectId());
                            continue;
                        }
                        text.append(customer.getTitle());
                        text.append('\n');
                    }
                    
                    List<Parameter> paramList =  
                            typeConfig.getParamIds().stream().map(ParameterCache::getParameter)
                            .collect(Collectors.toList());
                    List<ParameterValuePair> paramValues = 
                            paramDao.loadParameters(paramList, item.getObjectId(), false);
                    
                    for (ParameterValuePair pair : paramValues) {
                        switch (pair.getParameter().getType()) {
                            case Parameter.TYPE_TEXT:
                            case Parameter.TYPE_BLOB:
                                text.append(pair.getValue());
                                text.append('\n');
                                break;
                            case Parameter.TYPE_EMAIL: {
                                Map<Integer, String> valueMap = (Map) pair.getValue();
                                for (String email : valueMap.values()) {
                                    // комментарий - в квадратных скобках
                                    text.append(email);                                    
                                    text.append('\n');
                                }
                                break;
                            }
                            case Parameter.TYPE_LIST:
                            case Parameter.TYPE_LISTCOUNT:
                            case Parameter.TYPE_TREE: {
                                List<IdTitle> values = (List) pair.getValue();
                                for (IdTitle value : values) {
                                    // значение идёт после :, комментарий - в квадратных скобках
                                    text.append(value.getTitle().replace(':', ' '));                                    
                                    text.append('\n');
                                }
                                break;
                            }
                            case Parameter.TYPE_ADDRESS: {
                                Map<Integer, ParameterAddressValue> valueMap = (Map) pair.getValue();
                                for (ParameterAddressValue value : valueMap.values()) {
                                    text.append(value.getValue());                                    
                                    text.append('\n');
                                }
                                break;
                            }
                            case Parameter.TYPE_PHONE: {
                                ParameterPhoneValue value = (ParameterPhoneValue) pair.getValue();
                                for (ParameterPhoneValueItem valueItem : value.getItemList()) {
                                    text.append(valueItem.getPhone());
                                    text.append(' ');
                                    text.append(valueItem.getComment());
                                    text.append('\n');
                                }
                                break;
                            }
                        }
                    }
                    
                    item.setText(text.toString());
                    searchDao.update(item);
                    
                    con.commit();
                    
                    if (log.isDebugEnabled())
                        log.debug("Item type: " + item.getObjectType() + "; id: " + item.getObjectId() + 
                                "; data: " + text.toString());
                }   
            }
        } 
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        finally {
            SQLUtils.closeConnection(con);
        }
    }

}
