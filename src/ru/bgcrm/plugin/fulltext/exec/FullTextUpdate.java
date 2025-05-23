package ru.bgcrm.plugin.fulltext.exec;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgerp.app.cfg.Setup;
import org.bgerp.app.cfg.bean.annotation.Bean;
import org.bgerp.app.exec.scheduler.Task;
import org.bgerp.cache.ParameterCache;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.msg.Message;
import org.bgerp.model.param.Parameter;
import org.bgerp.model.param.ParameterValue;
import org.bgerp.util.Log;
import org.bgerp.util.sql.pool.ConnectionPool;

import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterPhoneValueItem;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.plugin.fulltext.Plugin;
import ru.bgcrm.plugin.fulltext.dao.SearchDAO;
import ru.bgcrm.plugin.fulltext.model.Config;
import ru.bgcrm.plugin.fulltext.model.Config.ObjectType;
import ru.bgcrm.plugin.fulltext.model.SearchItem;
import ru.bgcrm.util.TimeUtils;

/**
 * Updates full-text indexes.
 *
 * @author Shamil Vakhitov
 */
@Bean(oldClasses = { "ru.bgcrm.plugin.fulltext.FullTextUpdater", "ru.bgcrm.plugin.fulltext.task.FullTextUpdate" })
public class FullTextUpdate extends Task {
    private static final Log log = Log.getLog();

    public FullTextUpdate() {
        super(null);
    }

    @Override
    public String getTitle() {
        return Plugin.INSTANCE.getLocalizer().l("FullText Index Update");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        Config config = Setup.getSetup().getConfig(Config.class);

        ConnectionPool connectionPool = Setup.getSetup().getConnectionPool();
        try (Connection con = connectionPool.getDBConnectionFromPool();
             Connection conSlave = connectionPool.getDBSlaveConnectionFromPool()) {

            SearchDAO searchDao = new SearchDAO(con);
            ParamValueDAO paramDao = new ParamValueDAO(conSlave);

            List<SearchItem> forUpdate = null;
            while (!(forUpdate = searchDao.getScheduledUpdates(config.getIndexDelay(), 100)).isEmpty()) {
                for (SearchItem item : forUpdate) {
                    ObjectType typeConfig = config.getObjectTypeMap().get(item.getObjectType());
                    if (typeConfig == null) {
                        log.warn("Not configured object type: {}", item.getObjectType());
                        searchDao.delete(item.getObjectType(), item.getObjectId());
                        continue;
                    }

                    StringBuilder text = new StringBuilder(200);
                    if (Customer.OBJECT_TYPE.equals(item.getObjectType())) {
                        Customer customer = new CustomerDAO(conSlave).getCustomerById(item.getObjectId());
                        if (customer == null) {
                            log.warn("Customer not found: {}", item.getObjectId());
                            searchDao.delete(item);
                            continue;
                        }
                        text.append(customer.getTitle());
                        text.append('\n');
                    } else if (Process.OBJECT_TYPE.equals(item.getObjectType())) {
                        Process process = new ProcessDAO(conSlave).getProcess(item.getObjectId());
                        if (process == null) {
                            log.warn("Process not found: {}", item.getObjectId());
                            searchDao.delete(item);
                            continue;
                        }
                        text.append(process.getDescription());
                        text.append('\n');
                    } else if (Message.OBJECT_TYPE.equals(item.getObjectType())) {
                        Message message = new MessageDAO(conSlave).getMessageById(item.getObjectId());
                        if (message == null) {
                            log.warn("Message not found: {}", item.getObjectId());
                            searchDao.delete(item);
                            continue;
                        }
                        text.append(message.getText());
                        text.append('\n');
                    }

                    List<Parameter> paramList = ParameterCache.getParameterMap().values().stream()
                        .filter(p -> p.getObjectType().equals(item.getObjectType()) && config.isParamConfigured(p))
                        .collect(Collectors.toList());

                    if (!paramList.isEmpty()) {
                        List<ParameterValue> paramValues = paramDao.loadParameters(paramList, item.getObjectId(), false);

                        for (ParameterValue pair : paramValues) {
                            if (pair.getValue() == null) continue;

                            switch (Parameter.Type.of(pair.getParameter().getType())) {
                                case ADDRESS -> {
                                    var valueMap = (Map<Integer, ParameterAddressValue>) pair.getValue();
                                    for (ParameterAddressValue value : valueMap.values())
                                        text.append(value.getValue()).append('\n');
                                }
                                case BLOB, MONEY, TEXT -> {
                                    text.append(String.valueOf(pair.getValue())).append('\n');
                                }
                                case DATE -> {
                                    text.append(TimeUtils.format((Date) pair.getValue(), TimeUtils.FORMAT_TYPE_YMD)).append('\n');
                                }
                                case DATETIME -> {
                                    text.append(TimeUtils.format((Date) pair.getValue(), TimeUtils.FORMAT_TYPE_YMDHM)).append('\n');
                                }
                                case EMAIL -> {
                                    var valueMap = (Map<Integer, String>) pair.getValue();
                                    for (String email : valueMap.values()) {
                                        // a comment - in square braces
                                        text.append(email);
                                        text.append('\n');
                                    }
                                    break;
                                }
                                case FILE -> {
                                    // TODO: Handle a file value. pair.getValue();
                                }
                                case LIST -> {
                                    text.append(Parameter.Type.listToString(pair.getParameter().getId(), (Map<Integer, String>) pair.getValue())).append("\n");
                                }
                                case LISTCOUNT -> {
                                    text.append(Parameter.Type.listCountToString(pair.getParameter().getId(), (Map<Integer, BigDecimal>) pair.getValue())).append("\n");
                                }
                                case PHONE -> {
                                    var value = (ParameterPhoneValue) pair.getValue();
                                    for (ParameterPhoneValueItem valueItem : value.getItemList()) {
                                        text.append(valueItem.getPhone()).append(' ').append(valueItem.getComment()).append('\n');
                                    }
                                }
                                case TREE -> {
                                    text.append(Parameter.Type.treeToString(pair.getParameter().getId(), (Set<String>) pair.getValue())).append("\n");
                                }
                                case TREECOUNT -> {
                                    text.append(Parameter.Type.treeCountToString(pair.getParameter().getId(), (Map<String, BigDecimal>) pair.getValue())).append("\n");
                                }
                            }
                        }
                    }

                    item.setText(text.toString());
                    searchDao.update(item);

                    con.commit();

                    log.debug("Item type: {}; id: {}; data: {}", item.getObjectType(), item.getObjectId(), text);
                }
            }
        }
        catch (Exception e) {
            log.error(e);
        }
    }

}
