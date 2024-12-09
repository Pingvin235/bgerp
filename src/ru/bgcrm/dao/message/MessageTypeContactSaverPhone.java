package ru.bgcrm.dao.message;

import java.sql.Connection;

import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.exception.BGException;
import org.bgerp.cache.ParameterCache;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.msg.Message;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.Log;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterPhoneValueItem;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

@Bean
public class MessageTypeContactSaverPhone extends ru.bgcrm.dao.message.MessageTypeContactSaver {
    private static final Log log = Log.getLog();

    private final Parameter param;

    public MessageTypeContactSaverPhone(ConfigMap config) throws Exception {
        super(config);
        int paramId = config.getInt("paramId", -1);
        this.param = ParameterCache.getParameter(paramId);;
        if (param == null)
            throw new BGException(Log.format("Not found parameter with ID: {}", paramId));
    }

    @Override
    public void saveContact(DynActionForm form, Connection con, Message message, Process process, int saveMode) throws Exception {
        int objectId = 0;

        if (Customer.OBJECT_TYPE.equals(param.getObjectType())) {
            CommonObjectLink customerLink = Utils.getFirst(new ProcessLinkDAO(con).getObjectLinksWithType(process.getId(), Customer.OBJECT_TYPE));
            if (customerLink == null)
                return;
            objectId = customerLink.getLinkObjectId();
        } else if (Process.OBJECT_TYPE.equals(param.getObjectType())) {
            objectId = process.getId();
        } else {
            log.error("Unsupported object type: {}", param.getObjectType());
            return;
        }

        ParamValueDAO paramDao = new ParamValueDAO(con);

        ParameterPhoneValue value = paramDao.getParamPhone(objectId, param.getId());
        String phone = message.getFrom();

        if (!value.getItemList().stream().filter(item -> item.getPhone().equals(phone)).findAny().isPresent()) {
            value.addItem(new ParameterPhoneValueItem(phone, ""));
            paramDao.updateParamPhone(objectId, param.getId(), value);
        }
    }
}